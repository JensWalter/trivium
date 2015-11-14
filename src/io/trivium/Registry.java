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

package io.trivium;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.dep.org.apache.commons.io.IOUtils;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.fact.Fact;
import io.trivium.extension.task.Task;
import io.trivium.test.TestCase;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public enum Registry {
    INSTANCE;

    Logger log = Logger.getLogger(getClass().getName());

    public ConcurrentHashMap<ObjectRef, Class<? extends Task>> tasks = new ConcurrentHashMap<>();

    public ConcurrentHashMap<ObjectRef, Class<? extends Fact>> types = new ConcurrentHashMap<>();

    private ConcurrentHashMap<ObjectRef, Class<? extends Binding>> bindings = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ObjectRef, Binding> bindingInstances = new ConcurrentHashMap<>();

    public ConcurrentHashMap<ObjectRef, TestCase> testcases = new ConcurrentHashMap<>();

    public void reload() {
        final String PREFIX = "META-INF/services/";
        ClassLoader tvmLoader = ClassLoader.getSystemClassLoader();
        //types
        try {
            Enumeration<URL> resUrl = tvmLoader.getResources(PREFIX + "io.trivium.extension.fact.Fact");
            while (resUrl.hasMoreElements()) {
                URL url = resUrl.nextElement();
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                List<String> lines = IOUtils.readLines(is, "UTF-8");
                is.close();
                for (String line : lines) {
                    Class<? extends Fact> clazz = (Class<? extends Fact>) Class.forName(line);
                    Fact prototype = clazz.newInstance();
                    if (!types.containsKey(prototype.getTypeId())) {
                        types.put(prototype.getTypeId(), clazz);
                    }
                    log.log(Level.FINE, "registered type '{0}'", prototype.getFactName());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "dynamically loading types failed", ex);
        }

        //bindings
        try {
            Enumeration<URL> resUrl = tvmLoader.getResources(PREFIX + "io.trivium.extension.binding.Binding");
            while (resUrl.hasMoreElements()) {
                URL url = resUrl.nextElement();
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                List<String> lines = IOUtils.readLines(is, "UTF-8");
                is.close();
                for (String line : lines) {
                    Class<? extends Binding> clazz = (Class<? extends Binding>) Class.forName(line);
                    Binding prototype = clazz.newInstance();
                    if (!bindings.containsKey(prototype.getTypeId())) {
                        bindings.put(prototype.getTypeId(), clazz);
                        //register prototype
                        bindingInstances.put(prototype.getTypeId(),prototype);
                    }
                    log.log(Level.FINE, "registered binding '{0}'", prototype.getName());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "dynamically loading bindings failed", ex);
        }

        //tasks
        try {
            Enumeration<URL> resUrl = tvmLoader.getResources(PREFIX + "io.trivium.extension.task.Task");
            while (resUrl.hasMoreElements()) {
                URL url = resUrl.nextElement();
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                List<String> lines = IOUtils.readLines(is, "UTF-8");
                is.close();
                for (String line : lines) {
                    Class<? extends Task> clazz = (Class<? extends Task>) Class.forName(line);
                    Task prototype = clazz.newInstance();
                    if (!tasks.containsKey(prototype.getTypeId())) {
                        tasks.put(prototype.getTypeId(), clazz);
                    }
                    log.log(Level.FINE, "registered binding '{0}'", prototype.getName());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "dynamically loading bindings failed", ex);
        }

        //testcases
        try {
            Enumeration<URL> resUrl = tvmLoader.getResources(PREFIX + "io.trivium.test.TestCase");
            while (resUrl.hasMoreElements()) {
                URL url = resUrl.nextElement();
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                List<String> lines = IOUtils.readLines(is, "UTF-8");
                is.close();
                for (String line : lines) {
                    Class<? extends TestCase> clazz = (Class<? extends TestCase>) Class.forName(line);
                    TestCase prototype = clazz.newInstance();
                    if (!testcases.containsKey(prototype.getTypeId())) {
                        testcases.put(prototype.getTypeId(), prototype);
                    }
                    log.log(Level.FINE, "registered testcase '{0}'", prototype.getTypeId());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "dynamically loading test cases failed", ex);
        }
    }

    public ArrayList<LogRecord> startBinding(ObjectRef bindingTypeId){
        Logger logger = Logger.getLogger("io.trivium.extension."+bindingTypeId.toMangledString());
        ArrayList<LogRecord> logs = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logs.add(record);
            }
            @Override
            public void flush() {}
            @Override
            public void close() throws SecurityException {}
        };
        logger.addHandler(handler);
        Registry.INSTANCE.getBinding(bindingTypeId).startBinding();
        logger.removeHandler(handler);
        return logs;
    }

    public void notify(TriviumObject tvm) {
        for(Class<? extends Task> taskClass : tasks.values()){
            try {
                Task task = taskClass.newInstance();
                if(task.isApplicable(tvm)){
                    task.populateInput(tvm);
                    task.eval();
                    ArrayList<TriviumObject> output = task.extractOutput();
                    for (TriviumObject o : output) {
                        AnyClient.INSTANCE.storeObject(o);
                    }
                }
            } catch (Exception ex) {
                log.log(Level.SEVERE, "error while running task '{0}'", taskClass.getName());
                log.log(Level.SEVERE, "got exception", ex);
            }
        }
    }

    public Binding getBinding(ObjectRef typeId){
        return bindingInstances.get(typeId);
    }

    public Collection<Binding> getAllBindings(){
        return bindingInstances.values();
    }
}
