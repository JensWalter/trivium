package io.trivium.extension.task;

import io.trivium.Central;
import io.trivium.anystore.ObjectType;
import io.trivium.extension.annotation.INPUT;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.Typed;
import io.trivium.glue.InfiniObject;
import io.trivium.Central;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.annotation.INPUT;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.Typed;
import io.trivium.glue.InfiniObject;
import javolution.util.FastList;
import javolution.util.FastMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public abstract class TaskFactory implements Typed {
    private boolean scanned = false;
    private FastMap<ObjectType, InputType[]> inputFields = new FastMap<ObjectType, InputType[]>();

    public abstract String getName();

    public abstract Task getInstance(InfiniObject po);

    private void scanClass() {
        try {
            Class<?> factoryClass = this.getClass();
            Method method = factoryClass.getMethod("getInstance", InfiniObject.class);
            Class<?> activityclass = method.getReturnType();
            Field[] fields = activityclass.getFields();
            for (Field field : fields) {
                INPUT input = field.getAnnotation(INPUT.class);
                if (input != null) {
                    //FIXME derive type from package name
                    String inputType = "";
                    String condition = input.condition();
                    ObjectType typeId = ObjectType.getInstance(inputType,"v1");
                    InputType it = new InputType();
                    it.condition = condition;
                    it.typeId = typeId;
                    it.field = field;
                    if (inputFields.containsKey(typeId)) {
                        InputType[] old = inputFields.get(ObjectRef.getInstance(inputType));
                        InputType[] neu = Arrays.copyOf(old, old.length + 1);
                        neu[neu.length - 1] = it;
                        inputFields.put(typeId, neu);
                    } else {
                        inputFields.put(typeId, new InputType[]{it});
                    }
                }
            }
        } catch (Exception ex) {
            Central.logger.error("failed to reflect on type {}", this.getTypeId().toString());
            Central.logger.error("got exception", ex);
        }
        scanned = true;
    }

    public FastList<ObjectType> getInputTypes() {
        if (!scanned) {
            scanClass();
        }
        Set<ObjectType> keyset = inputFields.keySet();
        return new FastList<ObjectType>(keyset);
    }

    public boolean isApplicable(InfiniObject po) {
        boolean result = false;
        InputType[] input = inputFields.get(po.getTypeId());
        Object obj = po.getTypedData();
        for(InputType it : input){
            result |= evalCondition((Type)obj,it);
        }
        return result;
    }

    private boolean evalCondition(Type obj, InputType input){
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
            Central.logger.error("condition check failed with ",ex);
        }
        return false;
    }

    protected void populateInput(InfiniObject po, Task task) {
        InputType[] input = inputFields.get(po.getTypeId());
        for (InputType f : input) {
            Object obj = po.getTypedData();
            try {
                f.field.set(task, obj);
            } catch (Exception ex) {
                Central.logger.error("error population activity input", ex);
            }
        }
    }
}
