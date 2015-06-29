package io.trivium.extension.task;

import io.trivium.extension.annotation.INPUT;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.Typed;
import io.trivium.glue.TriviumObject;
import io.trivium.anystore.ObjectRef;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public abstract class TaskFactory<T extends Task> implements Typed {
    Logger log = LogManager.getLogger(getClass());
    private boolean scanned = false;
    private FastMap<ObjectRef, InputType[]> inputFields = new FastMap<ObjectRef, InputType[]>();

    public abstract String getName();

    public abstract T getInstance(TriviumObject po);

    private void scanClass() {
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
                    if (inputFields.containsKey(type)) {
                        InputType[] old = inputFields.get(ObjectRef.getInstance(inputType));
                        InputType[] neu = Arrays.copyOf(old, old.length + 1);
                        neu[neu.length - 1] = it;
                        inputFields.put(type, neu);
                    } else {
                        inputFields.put(type, new InputType[]{it});
                    }
                }
            }
        } catch (Exception ex) {
            log.error("failed to reflect on type {}", this.getTypeId().toString());
            log.error("got exception", ex);
        }
        scanned = true;
    }

    public FastList<ObjectRef> getInputTypes() {
        if (!scanned) {
            scanClass();
        }
        Set<ObjectRef> keyset = inputFields.keySet();
        return new FastList<ObjectRef>(keyset);
    }

    public boolean isApplicable(TriviumObject po) {
        boolean result = false;
        InputType[] input = inputFields.get(po.getTypeId());
        Object obj = po.getTypedData();
        for(InputType it : input){
            result |= evalCondition((Type)obj,it);
        }
        return result;
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
            log.error("condition check failed with ",ex);
        }
        return false;
    }

    protected void populateInput(TriviumObject po, T task) {
        InputType[] input = inputFields.get(po.getTypeId());
        for (InputType f : input) {
            Object obj = po.getTypedData();
            try {
                f.field.set(task, obj);
            } catch (Exception ex) {
                log.error("error population activity input", ex);
            }
        }
    }
}
