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
import io.trivium.extension.binding.Binding;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.TriviumObject;
import io.trivium.extension.task.Task;
import io.trivium.extension.task.TaskFactory;
import io.trivium.test.TestCase;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Registry {
    public static Registry INSTANCE = new Registry();

    Logger log = Logger.getLogger(getClass().getName());
    
    public FastMap<ObjectRef, TaskFactory> taskFactory = null;
    public FastMap<ObjectRef, FastList<TaskFactory>> taskSubscription = null;
    ServiceLoader<TaskFactory> taskLoader = ServiceLoader.load(TaskFactory.class);

    public FastMap<ObjectRef,TypeFactory> typeFactory = null;
    ServiceLoader<TypeFactory> typeLoader = ServiceLoader.load(TypeFactory.class);

    public FastMap<ObjectRef,Binding> bindings= null;
    ServiceLoader<Binding> bindingLoader = ServiceLoader.load(Binding.class,new TriviumLoader(ClassLoader.getSystemClassLoader()));

    public FastMap<ObjectRef,TestCase> testcases= null;
    ServiceLoader<TestCase> testcaseLoader = ServiceLoader.load(TestCase.class,new TriviumLoader(ClassLoader.getSystemClassLoader()));

    public Registry(){
        taskFactory = new FastMap<ObjectRef, TaskFactory>();
        taskFactory.shared();
        taskSubscription = new FastMap<ObjectRef,FastList<TaskFactory>>().shared();

        typeFactory = new FastMap<ObjectRef,TypeFactory>();
        typeFactory.shared();

        bindings = new FastMap<ObjectRef,Binding>();
        bindings.shared();

        testcases = new FastMap<ObjectRef,TestCase>();
        testcases.shared();
    }

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
            FastList<ObjectRef> inputTypes = task.getInputTypes();
            for(ObjectRef ref : inputTypes){
                FastList<TaskFactory> a = taskSubscription.get(ref);
                if(a== null){
                    FastList<TaskFactory> all = new FastList<TaskFactory>();
                    all.shared();
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
        FastList<TaskFactory> list = taskSubscription.get(ref);
        //calculate activity
        if(list!=null) {
            for (TaskFactory factory : list) {
                FastList<ObjectRef> types = factory.getInputTypes();
                for (ObjectRef type : types) {
                    if(factory.isApplicable( po))
                    {
                        try {
                            Task task = factory.getInstance(po);
                            factory.populateInput(po, task);
                            task.eval();
                            FastList<TriviumObject> output = factory.extractOutput(task);
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
