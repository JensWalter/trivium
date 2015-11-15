/*
 * Copyright 2015 Jens Walter
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

package io.trivium.extension.task;

import io.trivium.anystore.query.Query;
import io.trivium.dep.org.objectweb.asm.ClassReader;
import io.trivium.dep.org.objectweb.asm.Opcodes;
import io.trivium.dep.org.objectweb.asm.tree.AbstractInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.ClassNode;
import io.trivium.dep.org.objectweb.asm.tree.FieldInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.InsnList;
import io.trivium.dep.org.objectweb.asm.tree.MethodInsnNode;
import io.trivium.dep.org.objectweb.asm.tree.MethodNode;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.fact.Fact;
import io.trivium.extension.Typed;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Task implements Typed {
    protected Logger logger = Logger.getLogger(getClass().getName());

    public abstract boolean eval() throws Exception;

    public void check() throws Exception{}

    public String getName(){
        String name = this.getClass().getCanonicalName();
        return name.substring(name.lastIndexOf('.')+1)+" ["+name+"]";
    }

    public boolean isApplicable(TriviumObject tvm) {
        //TODO only checks for type, not criteria
        HashMap<String,Query> input = getInputQueries();
        for(Query query : input.values()){
            Fact f = query.castType(tvm);
            if(query.condition!=null && query.condition.invoke(f))
                return true;
        }
        return false;
    }

    public HashMap<String,Query> getInputQueries() {
        HashMap<String,Query> list = new HashMap<>();
        try{
            Class<?> c = this.getClass();
            Field[] fields = c.getDeclaredFields();
            for(Field field : fields){
                Class<?> fieldClass = field.getType();
                Class<?>[] interfaces = fieldClass.getInterfaces();
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
                                list.put(field.getName(), query);
                            }else{
                                //normal class
                                Query<Fact> query = (Query<Fact>) q.newInstance();
                                list.put(field.getName(), query);
                            }
                        }
                    }
                }
            }
        }catch(Exception ex){
            logger.log(Level.SEVERE,"failed to reflect on task {}", this.getTypeId().toString());
            logger.log(Level.SEVERE,"got exception", ex);
        }
        return list;
    }
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
            logger.log(Level.SEVERE, "failed to reflect on task {}", this.getTypeId().toString());
            logger.log(Level.SEVERE, "got exception", ex);
        }
        return resultList;
    }

    public void populateInput(TriviumObject tvm) {
        HashMap<String,Query> input = getInputQueries();
        for(String fieldName : input.keySet()){
            try {
                Fact obj = tvm.getTypedData();
                Field field = getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(this,obj);
            } catch(NoSuchFieldException | IllegalAccessException e){
                logger.log(Level.SEVERE,"injecting input field failed",e);
            }
        }
    }
}
