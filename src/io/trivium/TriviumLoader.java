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

package io.trivium;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.statics.MimeTypes;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.fact.File;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class TriviumLoader extends ClassLoader {

    //TODO is a class loader single threaded or multi threaded?
    private ConcurrentHashMap<String,Class<?>> classes = new ConcurrentHashMap<>();

    public TriviumLoader(ClassLoader parent) {
        super(parent);
    }

    private Class<?> getClass(String name) throws ClassNotFoundException {
        String file = name.replace('.', java.io.File.separatorChar) + ".class";
        byte[] b = null;
        try {
            // This loads the byte code data from the file
            b = loadClassData(file);
            if((b==null || b.length==0 ) && Central.isRunning && name.startsWith("io.trivium.")){
                //load from anystore
                Query<TriviumObject> query = new Query<TriviumObject>(){
                    {
                        condition = (tvm) ->
                            tvm.findMetaValue("canonicalName").equals(name)
                            && tvm.findMetaValue("typeId").equals(File.class.getCanonicalName())
                            && tvm.findMetaValue("contentType").equals(MimeTypes.getMimeType("class"));
                    }
                };
                ArrayList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query).getAllAsTypedList();
                for (TriviumObject po : objects) {
                    File memFile = new File();
                    memFile.populate(po);
                    b = Base64.getDecoder().decode(memFile.data);
                }
            }
            // defineClass is inherited from the ClassLoader class
            // and converts the byte array into a Class
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> orgResult;
        Vector<URL> result = new Vector<URL>();

            orgResult = super.getResources(name);
            while (orgResult.hasMoreElements()) {
                result.add(orgResult.nextElement());
            }
        //do internal lookup on trivium types
        if(Central.isRunning && name.startsWith("META-INF/services/io.trivium.")){
            //enrich with anystore
            Query<TriviumObject> query = new Query<TriviumObject>(){
                {
                    condition = (tvm) ->
                            tvm.findMetaValue("name").equals(name)
                            && tvm.findMetaValue("typeId").equals(File.class.getCanonicalName());
                }
            };
            ArrayList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query).getAllAsTypedList();
            for (TriviumObject po : objects) {
                String uri = "anystore://" + po.getId().toString();
                URL url = new URL(uri);
                result.add(url);
            }
        }
        return result.elements();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        return ClassLoader.getSystemResources(name);
    }

    public Class<?> fromBytes(String name, byte[] input) {
        try{
            Class<?> c = defineClass(name,input,0,input.length);
            return c;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = super.loadClass(name);
        if(clazz == null) {
            //try anystore
            try {
                //look up anystore
                if (Central.isRunning) {
                    if (classes.containsKey(name)) {
                        return classes.get(name);
                    }
                    Query<TriviumObject> query = new Query<TriviumObject>(){
                        {
                            condition = (tvm) ->
                                    tvm.findMetaValue("canonicalName").equals(name)
                                            && tvm.findMetaValue("typeId").equals(File.class.getCanonicalName())
                                            && tvm.findMetaValue("contentType").equals(MimeTypes.getMimeType("class"));
                        }
                    };
                    ArrayList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query).getAllAsTypedList();
                    for (TriviumObject po : objects) {
                        File file = new File();
                        file.populate(po);
                        if (file.contentType.equals(MimeTypes.getMimeType("class"))
                                && file.name.replace('/', '.').equals(name + ".class")) {
                            byte[] bytes = Base64.getDecoder().decode(file.data);
                            Class<?> c = defineClass(name, bytes, 0, bytes.length);
                            classes.put(name, c);
                            clazz=c;
                            //TODO break inner and outer loop
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
            }
        }
        return clazz;
    }

    private byte[] loadClassData(String name) throws IOException {
        // Opening the file
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        // Reading the binary data
        in.readFully(buff);
        in.close();
        return buff;
    }
}