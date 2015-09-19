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

import io.trivium.anystore.ObjectRef;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.annotation.INPUT;
import io.trivium.extension.annotation.OUTPUT;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.Typed;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Task implements Typed {
    protected Logger log = Logger.getLogger(getClass().getName());
    public final ObjectRef instanceId = ObjectRef.getInstance();

    public abstract boolean eval() throws Exception;

    public void check() throws Exception{}

    public String getName(){
        String name = this.getClass().getCanonicalName();
        return name.substring(name.lastIndexOf('.')+1)+" ["+name+"]";
    }

    public boolean isApplicable(TriviumObject po) {
        boolean result = false;
        ArrayList<InputType> input = getInputTypes();
        Type obj = po.getTypedData();
        for(InputType it : input){
            if(it.typeId==obj.getTypeId()) {
                result |= evalCondition(obj, it);
            }
        }
        return result;
    }

    public ArrayList<InputType> getInputTypes() {
        ArrayList<InputType> inputFields = new ArrayList<>();
        try {
            Class<?> factoryClass = this.getClass();
            Method method = factoryClass.getMethod("getInstance", TriviumObject.class);
            Class<?> activityclass = method.getReturnType();
            Field[] fields = activityclass.getDeclaredFields();
            for (Field field : fields) {
                INPUT input = field.getAnnotation(INPUT.class);
                if (input != null) {
                    String path = field.getType().getCanonicalName();
                    //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
                    String[] arr = path.split("\\.");
                    String typeId = arr[arr.length-2];
                    String inputType = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                            +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
                    String condition = input.condition();
                    ObjectRef type = ObjectRef.getInstance(inputType);
                    InputType it = new InputType();
                    it.condition = condition;
                    it.typeId = type;
                    it.field = field;
                    inputFields.add(it);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE,"failed to reflect on task {}", this.getTypeId().toString());
            log.log(Level.SEVERE,"got exception", ex);
        }
        return inputFields;
    }

    public ArrayList<OutputType> getOutputTypes() {
        ArrayList<OutputType> outputFields = new ArrayList<>();
        try {
            Class<?> factoryClass = this.getClass();
            Method method = factoryClass.getMethod("getInstance", TriviumObject.class);
            Class<?> activityclass = method.getReturnType();
            Field[] fields = activityclass.getDeclaredFields();
            for (Field field : fields) {
                OUTPUT output = field.getAnnotation(OUTPUT.class);
                if(output != null) {
                    String path = field.getType().getCanonicalName();
                    //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
                    String[] arr = path.split("\\.");
                    String typeId = arr[arr.length-2];
                    String outputType = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                            +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
                    ObjectRef type = ObjectRef.getInstance(outputType);
                    OutputType ot = new OutputType();
                    ot.typeId = type;
                    ot.field = field;
                    outputFields.add(ot);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE,"failed to reflect on task {}", this.getTypeId().toString());
            log.log(Level.SEVERE,"got exception", ex);
        }
        return outputFields;
    }

    private boolean evalCondition(Type obj, InputType input){
        if(input.condition==null || input.condition.length()==0){
            //no condition present
            return true;
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String script = "";
        String fieldName = input.field.getName();
        try {
            engine.put(fieldName, obj);
            script+= "\n if( " + input.condition +" ){ $result=\"true\"; } else { $result=\"false\"; }";

            engine.eval(script);
            Object returnObject = engine.get("$result");
            String returnString = returnObject.toString();
            if(returnString.equals("true")){
                return true;
            }else{
                if(returnString.equals("false")){
                    return false;
                }else{
                    //unknown state
                    throw new Exception("condition check returned unknown state "+returnString);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE,"condition check failed with ",ex);
        }
        return false;
    }

    public void populateInput(TriviumObject tvm) {
        //TODO populate all inputs
        ArrayList<InputType> input = getInputTypes();
        for (InputType f : input) {
            Type obj = tvm.getTypedData();
            if (obj.getTypeId() == f.typeId) {
                try {
                    f.field.setAccessible(true);
                    f.field.set(this, obj);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "error population activity input", ex);
                }
            }
        }
    }

    public ArrayList<TriviumObject> extractOutput() {
        ArrayList<TriviumObject> result = new ArrayList<>();
        ArrayList<OutputType> outputs = getOutputTypes();
        for (OutputType f : outputs) {
            try {
                f.field.setAccessible(true);
                Type obj = (Type) f.field.get(this);
                result.add(TriviumObject.getTriviumObject(obj));
            } catch (Exception ex) {
                log.log(Level.SEVERE,"error population activity input", ex);
            }
        }
        return result;
    }
}
