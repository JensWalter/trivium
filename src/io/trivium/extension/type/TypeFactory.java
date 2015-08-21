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

package io.trivium.extension.type;

import com.google.common.reflect.TypeToken;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;

import java.lang.reflect.*;
import java.util.logging.Level;

public interface TypeFactory<T> extends Typed{
    String getName();
    default T getInstance(TriviumObject tvm){
        TypeToken<T> token = new TypeToken<T>(getClass()){};
        Class<?> aClass  = token.getRawType();
        T t = null;
        try {
            //private constructor support
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            t = (T) constructor.newInstance();
            Field[] fields = aClass.getDeclaredFields();
            Element el = tvm.getData();
            el = el.getChild(0);
            for (Field field : fields) {
                String name = field.getName();
                field.setAccessible(true);
                try {
                    if (field.getType() == long.class) {
                        field.setLong(t, Long.parseLong(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == int.class) {
                        field.setInt(t, Integer.parseInt(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == boolean.class) {
                        field.setBoolean(t, Boolean.parseBoolean(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == byte.class) {
                        field.setByte(t, Byte.parseByte(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == float.class) {
                        field.setFloat(t, Float.parseFloat(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == double.class) {
                        field.setDouble(t, Double.parseDouble(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == short.class) {
                        field.setShort(t, Short.parseShort(el.getFirstChild(name).getValue()));
                    } else if (field.getType() == char.class) {
                        field.setChar(t, el.getFirstChild(name).getValue().charAt(0));
                    } else {
                        //try String
                        field.set(t, el.getFirstChild(name).getValue());
                    }
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE,"error while building typed object", ex);
                }
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE,"error while constructing typed object", ex);
        }
        return t;
    }

    default TriviumObject getTriviumObject(T instance){
        Class<?> aClass = instance.getClass();
        Field[] fields = aClass.getDeclaredFields();
        TriviumObject tvm = new TriviumObject();

        String path = aClass.getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        String uuid = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
        tvm.setTypeId(ObjectRef.getInstance(uuid));
        Element root = new Element("dummy");
        for(Field field : fields){
            String name = field.getName();
            field.setAccessible(true);
            try {
                if (field.getType() == long.class || field.getType() == int.class
                        || field.getType() == boolean.class || field.getType() == byte.class || field.getType() == float.class
                        || field.getType() == double.class || field.getType() == short.class || field.getType() == char.class
                        || field.getType() == String.class) {
                    root.addChild(new Element(name,field.get(instance).toString()));
                }
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE,"error while building typed object", ex);
            }
        }
        tvm.setData(root);
        return tvm;
    }
}
