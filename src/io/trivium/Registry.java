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
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.type.TypeFactory;
import io.trivium.extension.task.Task;
import io.trivium.extension.task.TaskFactory;
import io.trivium.test.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Registry {
    public static Registry INSTANCE = new Registry();

    Logger log = Logger.getLogger(getClass().getName());
    
    public ConcurrentHashMap<ObjectRef, TaskFactory> taskFactory = new ConcurrentHashMap<>();
    public ConcurrentHashMap<ObjectRef, ArrayList<TaskFactory>> taskSubscription = new ConcurrentHashMap<>();
    ServiceLoader<TaskFactory> taskLoader = ServiceLoader.load(TaskFactory.class);

    public ConcurrentHashMap<ObjectRef,TypeFactory> typeFactory = new ConcurrentHashMap<>();
    ServiceLoader<TypeFactory> typeLoader = ServiceLoader.load(TypeFactory.class);

    public ConcurrentHashMap<ObjectRef,Binding> bindings= new ConcurrentHashMap<>();
    ServiceLoader<Binding> bindingLoader = ServiceLoader.load(Binding.class,new TriviumLoader(ClassLoader.getSystemClassLoader()));

    public ConcurrentHashMap<ObjectRef,TestCase> testcases= new ConcurrentHashMap<>();
    ServiceLoader<TestCase> testcaseLoader = ServiceLoader.load(TestCase.class,new TriviumLoader(ClassLoader.getSystemClassLoader()));

    public void reload(){
        //types
        typeLoader.reload();
        Iterator<TypeFactory> typeIter = typeLoader.iterator();
        while(typeIter.hasNext()){
            TypeFactory type = typeIter.next();
            if(!typeFactory.containsKey(type.getTypeId())){
                typeFactory.put(type.getTypeId(), type);
            }
        }
        //printing registered Types
        for(TypeFactory type : typeFactory.values()){
            log.log(Level.FINE,"registered type factory for '{}'", type.getName());
        }

        //activity
        taskLoader.reload();
        Iterator<TaskFactory> tskIter = taskLoader.iterator();
        while(tskIter.hasNext()){
            TaskFactory activity = tskIter.next();
            if(!taskFactory.containsKey(activity.getTypeId())){
                taskFactory.put(activity.getTypeId(), activity);
            }
        }
        //prepare subscriptions
        refreshSubscriptions();

        //bindings
        bindingLoader.reload();
        Iterator<Binding> bindIter = bindingLoader.iterator();
        while(bindIter.hasNext()){
            Binding binding = bindIter.next();
            if(!bindings.containsKey(binding.getTypeId())){
                bindings.put(binding.getTypeId(),binding);
            }
        }

        //testcases
        testcaseLoader.reload();
        Iterator<TestCase> testIter = testcaseLoader.iterator();
        while(testIter.hasNext()){
            TestCase testcase = testIter.next();
            if(!testcases.containsKey(testcase.getTypeId())){
                testcases.put(testcase.getTypeId(),testcase);
            }
        }
    }

    private void refreshSubscriptions(){
        for(TaskFactory task : taskFactory.values()){
            ArrayList<ObjectRef> inputTypes = task.getInputTypes();
            for(ObjectRef ref : inputTypes){
                ArrayList<TaskFactory> a = taskSubscription.get(ref);
                if(a== null){
                    ArrayList<TaskFactory> all = new ArrayList<>();
                    all.add(task);
                    taskSubscription.put(ref,all);
                }else{
                    if(!a.contains(task)) {
                        a.add(task);
                    }
                }
            }
        }
    }

    public void notify(TriviumObject po){
        ObjectRef ref = po.getTypeId();
        ArrayList<TaskFactory> list = taskSubscription.get(ref);
        //calculate activity
        if(list!=null) {
            for (TaskFactory factory : list) {
                ArrayList<ObjectRef> types = factory.getInputTypes();
                for (ObjectRef type : types) {
                    if(factory.isApplicable( po))
                    {
                        try {
                            Task task = factory.getInstance(po);
                            factory.populateInput(po, task);
                            task.eval();
                            ArrayList<TriviumObject> output = factory.extractOutput(task);
                            for(TriviumObject o : output) {
                                AnyClient.INSTANCE.storeObject(o);
                            }
                        }catch(Exception ex){
                            log.log(Level.SEVERE, "error while running task '{}'", factory.getName());
                            log.log(Level.SEVERE, "got exception", ex);
                        }
                    }
                }
            }
        }
    }
}
