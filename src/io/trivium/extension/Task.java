/*
 * Copyright 2016 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium.extension;

import io.trivium.Registry;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.TypeRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.dep.org.objectweb.asm.ClassReader;
import io.trivium.dep.org.objectweb.asm.Opcodes;
import io.trivium.dep.org.objectweb.asm.tree.AbstractInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.ClassNode;
import io.trivium.dep.org.objectweb.asm.tree.FieldInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.InsnList;
import io.trivium.dep.org.objectweb.asm.tree.MethodInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.MethodNode;
import io.trivium.extension.fact.TriviumObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Task implements Typed {
    protected Logger logger = Logger.getLogger(getClass().getName());

    /**
     * implementation of the actual task
     * @return return the success of the evaluation.
     *      If false is return, no data will be persisted.
     * @throws Exception
     */
    public abstract boolean eval() throws Exception;

    /**
     * every task is allowed to bring its own check method.
     * In here the developer should place all external dependency check the task needs to fulfill its job.
     * this method is called regularly, but not for every invocation.
     * @throws Exception
     */
    public void check() throws Exception{}

    /**
     * name of the task, defaults to its class name.
     * @return task name
     */
    public String getName(){
        return this.getClass().getSimpleName();
    }

    /**
     * checks whether the given type is applicable to this task
     * @param tvm type to check against
     * @return success if type is applicable
     */
    public boolean checkInputTypes(TriviumObject tvm) {
        TypeRef typeRef = tvm.getTypeRef();
        Class<?> typeClass = Registry.INSTANCE.types.get(typeRef);
        try {
            Class<?> c = this.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                Class<?> fieldClass = field.getType();
                //check for matching or generic type
                if (fieldClass == tvm.getClass() || fieldClass == typeClass) {
                    Query<Fact> query = getInputQuery(field);
                    if (query == null) {
                        return false;
                    } else {
                        Class<? extends Fact> clazz = query.targetType;
                        Fact fact;
                        if(clazz == TriviumObject.class) {
                            fact = tvm;
                        }else{
                            fact = tvm.getTypedData();
                        }
                        return query.condition.invoke(fact);
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "failed to reflect on task {0}", this.getTypeRef().toString());
            logger.log(Level.SEVERE, "got exception", ex);
        }
        return false;
    }

    /**
     * return all necesasry input queries to actually trigger this task
     * @return list of all fields with their corresponding queries.
     */
    public HashMap<Field,Query> getInputQueries() {
        HashMap<Field,Query> list = new HashMap<>();
        try{
            Class<?> c = this.getClass();
            Field[] fields = c.getDeclaredFields();
            for(Field field : fields){
                Class<?> fieldClass = field.getType();
                Class<?>[] interfaces = null;
                if(fieldClass.isArray()){
                    interfaces = fieldClass.getComponentType().getInterfaces();
                }else{
                    interfaces = fieldClass.getInterfaces();
                }
                for(Class<?> iface : interfaces){
                    if(iface.isAssignableFrom(Fact.class)){
                        String queryClass = getFieldAssignment(field.getName());
                        if(queryClass.length()>0) {
                            Class<?> q = Class.forName(queryClass.replace('/', '.'));
                            if(queryClass.contains("$")){
                                //inner class
                                Constructor con = q.getDeclaredConstructors()[0];
                                con.setAccessible(true);
                                Object obj = con.newInstance(new Object[]{this});
                                Query<Fact> query = (Query<Fact>) obj;
                                list.put(field, query);
                            }else{
                                //normal class
                                Query<Fact> query = (Query<Fact>) q.newInstance();
                                list.put(field, query);
                            }
                        }
                    }
                }
            }
        }catch(Exception ex){
            logger.log(Level.SEVERE,"failed to reflect on task {0}", this.getTypeRef().toString());
            logger.log(Level.SEVERE,"got exception", ex);
        }
        return list;
    }

    /**
     * return an input query for the given field
     * @param inputField field to get the query from
     * @return query for the given field
     */
    private Query<Fact> getInputQuery(Field inputField) {
        try {
            Class<?> fieldClass = inputField.getType();
            Class<?>[] interfaces = null;
            if(fieldClass.isArray()){
                interfaces = fieldClass.getComponentType().getInterfaces();
            }else{
                interfaces = fieldClass.getInterfaces();
            }
            for (Class<?> iface : interfaces) {
                if (iface.isAssignableFrom(Fact.class)) {
                    String queryClass = getFieldAssignment(inputField.getName());
                    if (queryClass.length() > 0) {
                        Class<?> q = Class.forName(queryClass.replace('/', '.'));
                        if (queryClass.contains("$")) {
                            //inner class
                            Constructor con = q.getDeclaredConstructors()[0];
                            con.setAccessible(true);
                            Object obj = con.newInstance(new Object[]{this});
                            return (Query<Fact>) obj;
                        } else {
                            //normal class
                            return (Query<Fact>) q.newInstance();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "failed to reflect on task {0}", this.getTypeRef().toString());
            logger.log(Level.SEVERE, "got exception", ex);
        }
        return null;
    }

    /**
     * check the given field, if it can be assigned by the given trivium object
     * @param field field to check against
     * @param tvm input to be assigned
     * @return tvm can safely be assigned to field
     */
    private static boolean isTypeMatching(Field field, TriviumObject tvm){
        Class<?> fieldClass = field.getType();
        Class<?> effectiveClass = fieldClass;
        if(fieldClass.isArray()){
            effectiveClass = fieldClass.getComponentType();
        }
        Class targetType = null;
        try {
            targetType = Class.forName(tvm.getTypeRef().toString());
        } catch (ClassNotFoundException e) {
            Logger.getLogger(Task.class.getCanonicalName()).log(Level.SEVERE,"could not resolve type",e);
        }
        if(effectiveClass.equals(targetType)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * if a field has a default assignment, this method will find the source within the byte code
     * and returns the name of the class.
     * @param fieldName
     * @return
     * @throws IOException
     */
    private String getFieldAssignment(String fieldName) throws IOException {
        String url = "/" + getClass().getName().replace('.', '/') + ".class";
        InputStream in = getClass().getResourceAsStream(url);
        ClassReader classReader = new ClassReader(in);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        List<MethodNode> methods = classNode.methods;
        String lastOwner = "";

        for (MethodNode method : methods) {
            // only look for constructor
            if (method.name.equals("<init>")) {
                InsnList instructions = method.instructions;
                for (AbstractInsnNode instr : instructions.toArray()) {
                    if (instr instanceof FieldInsnNode) {
                        FieldInsnNode n = (FieldInsnNode) instr;
                        if (fieldName.equals(n.name)) {
                            return lastOwner;
                        }
                    }
                    if (instr instanceof MethodInsnNode) {
                        MethodInsnNode m = (MethodInsnNode) instr;
                        if (Opcodes.INVOKESPECIAL == m.getOpcode()) {
                            lastOwner = m.owner;
                        }
                    }
                }

            }
        }
        return "";
    }

    /**
     * extract the output a task and return all objects as serialized list
     * @return
     */
    public ArrayList<TriviumObject> extractOutput() {
        ArrayList<TriviumObject> resultList = new ArrayList<>();
        ArrayList<Field> fieldList = new ArrayList<>();
        try {
            Class<?> c = this.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                Class<?> fieldClass = field.getType();
                Class<?>[] interfaces = fieldClass.getInterfaces();
                for (Class<?> iface : interfaces) {
                    if (iface.isAssignableFrom(Fact.class)) {
                        String queryClass = getFieldAssignment(field.getName());
                        if (queryClass.length() == 0) {
                            fieldList.add(field);
                        }
                    }
                }
            }
            for (Field field : fieldList) {
                try {
                    field.setAccessible(true);
                    Fact obj = (Fact) field.get(this);
                    if (obj != null)
                        resultList.add(TriviumObject.getTriviumObject(obj));
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "error population activity input", ex);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "failed to reflect on task {0}", this.getTypeRef().toString());
            logger.log(Level.SEVERE, "got exception", ex);
        }
        return resultList;
    }

    /**
     * populates a necessary inputs of the current task,
     * if not sufficient data is available, the method returns false
     * @return success of the population
     */
    public boolean populateInput(TriviumObject tvm) {
        HashMap<Field, Query> all = getInputQueries();
        //TODO make this more error resilient
        if(all.size()==1 && ! all.keySet().iterator().next().getType().isArray()){
            //only one input needed -> use the provided tvm
            Field field = all.keySet().iterator().next();
            field.setAccessible(true);
            try {
                field.set(this,tvm.getTypedData());
            } catch (IllegalAccessException e) {}
        }else {
            //more than one field found
            for(Map.Entry<Field,Query> one : all.entrySet()){
                //check if field macthes tvm
                if(isTypeMatching(one.getKey(),tvm)){
                    Field field = one.getKey();
                    field.setAccessible(true);
                    try {
                        field.set(this,tvm.getTypedData());
                    } catch (IllegalAccessException e) {}
                }else {
                    //run query and set input
                    Result result = AnyClient.INSTANCE.loadObjects(one.getValue());
                    if (!result.partition.isEmpty()) {
                        Field field = one.getKey();
                        field.setAccessible(true);
                        try {
                            if (field.getType().isArray()) {
                                Fact[] facts = result.getAllAsList().toArray(new Fact[1]);
                                field.set(this, facts);
                            } else {
                                Fact fact = result.getAllAsList().get(0);
                                field.set(this, fact);
                            }
                            continue;
                        } catch (IllegalAccessException e) {
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
