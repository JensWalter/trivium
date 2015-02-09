package io.trivium.reactor;

import io.trivium.Central;
import io.trivium.InfiniLoader;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.InfiniObject;
import io.trivium.Central;
import io.trivium.InfiniLoader;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.task.Task;
import io.trivium.extension.task.TaskFactory;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.InfiniObject;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Registry {
    public static Registry INSTANCE = new Registry();

    public FastMap<ObjectRef, TaskFactory> taskFactory = null;
    public FastMap<ObjectRef, FastList<TaskFactory>> taskSubscription = null;
    ServiceLoader<TaskFactory> activityLoader = ServiceLoader.load(TaskFactory.class);

    public FastMap<ObjectRef,TypeFactory> typeFactory = null;
    ServiceLoader<TypeFactory> typeLoader = ServiceLoader.load(TypeFactory.class);

    public FastMap<ObjectRef,Binding> bindings= null;
    ServiceLoader<Binding> bindingLoader = ServiceLoader.load(Binding.class,new InfiniLoader(ClassLoader.getSystemClassLoader()));

    public Registry(){
        taskFactory = new FastMap<ObjectRef, TaskFactory>();
        taskFactory.shared();
        taskSubscription = new FastMap<ObjectRef,FastList<TaskFactory>>().shared();

        typeFactory = new FastMap<ObjectRef,TypeFactory>();
        typeFactory.shared();

        bindings = new FastMap<ObjectRef,Binding>();
        bindings.shared();
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
            Central.logger.debug("registered type factory for '{}'", type.getName());
        }

        //activity
        activityLoader.reload();
        Iterator<TaskFactory> tskIter = activityLoader.iterator();
        while(tskIter.hasNext()){
            TaskFactory activity = tskIter.next();
            if(!taskFactory.containsKey(activity.getTypeId())){
                taskFactory.put(activity.getTypeId(), activity);
            }
        }
        //printing registered Activities
        for(TaskFactory act : taskFactory.values()){
            Central.logger.debug("registered task factory for '{}'", act.getName());
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
        //printing registered Bindings
        for(Binding binding : bindings.values()){
            Central.logger.debug("registered type factory for '{}'", binding.getName());
        }

    }

    private void refreshSubscriptions(){
        for(TaskFactory activity : taskFactory.values()){
            FastList<ObjectRef> inputTypes = activity.getInputTypes();
            for(ObjectRef ref : inputTypes){
                FastList<TaskFactory> a = taskSubscription.get(ref);
                if(a== null){
                    FastList<TaskFactory> all = new FastList<TaskFactory>();
                    all.add(activity);
                    all.shared();
                    taskSubscription.put(ref,all);
                }else{
                    if(!a.contains(activity)) {
                        a.add(activity);
                    }
                }
            }
        }
    }

    public void notify(InfiniObject po){
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
                            task.eval();
                        }catch(Exception ex){
                            Central.logger.error("error while running activity '{}'",factory.getName());
                            Central.logger.error("got exception",ex);
                        }
                    }
                }
            }
        }
    }
}
