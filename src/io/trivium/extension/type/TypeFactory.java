package io.trivium.extension.type;

import com.google.common.reflect.TypeToken;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;

import java.lang.reflect.*;

public interface TypeFactory<T> extends Typed{
    String getName();
    default T getInstance(TriviumObject tvm){
        TypeToken<T> token = new TypeToken<T>(getClass()){};
        Class<?> aClass  = token.getRawType();
        T t = null;
        try {
            t = (T)aClass.newInstance();
        } catch (Exception ex) {
            getLogger().error("error while constructing typed object", ex);
        }
        Field[] fields = aClass.getDeclaredFields();
        Element el = tvm.getData();
        for(Field field:fields){

            String name = field.getName();
            field.setAccessible(true);
            try {
                if(field.getType()==long.class) {
                    field.setLong(t, Long.parseLong(el.getFirstChild(name).getValue()));
                }else if(field.getType()==int.class) {
                    field.setInt(t, Integer.parseInt(el.getFirstChild(name).getValue()));
                }else if(field.getType()==boolean.class) {
                    field.setBoolean(t, Boolean.parseBoolean(el.getFirstChild(name).getValue()));
                }else if(field.getType()==byte.class) {
                    field.setByte(t, Byte.parseByte(el.getFirstChild(name).getValue()));
                }else if(field.getType()==float.class) {
                    field.setFloat(t, Float.parseFloat(el.getFirstChild(name).getValue()));
                }else if(field.getType()==double.class) {
                    field.setDouble(t, Double.parseDouble(el.getFirstChild(name).getValue()));
                }else if(field.getType()==short.class) {
                    field.setShort(t, Short.parseShort(el.getFirstChild(name).getValue()));
                }else if(field.getType()==char.class) {
                    field.setChar(t, el.getFirstChild(name).getValue().charAt(0));
                }else{
                    //try String
                    field.set(t, el.getFirstChild(name).getValue());
                }
            }catch(Exception ex){
                getLogger().error("error while building typed object",ex);
            }
        }
        return t;
    }
    TriviumObject getTriviumObject(T instance);
}
