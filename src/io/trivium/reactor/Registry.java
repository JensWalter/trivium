package io.trivium.reactor;

import io.trivium.Central;
import io.trivium.InfiniLoader;
import io.trivium.anystore.ObjectType;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Registry {
    public static Registry INSTANCE = new Registry();

    Logger log = LogManager.getLogger(getClass());
    
    public FastMap<ObjectType, TaskFactory> taskFactory = null;
    public FastMap<ObjectType, FastList<TaskFactory>> taskSubscription = null;
    ServiceLoader<TaskFactory> activityLoader = ServiceLoader.load(TaskFactory.class);

    public FastMap<ObjectType,TypeFactory> typeFactory = null;
    ServiceLoader<TypeFactory> typeLoader = ServiceLoader.load(TypeFactory.class);

    public FastMap<ObjectType,Binding> bindings= null;
    ServiceLoader<Binding> bindingLoader = ServiceLoader.load(Binding.class,new InfiniLoader(ClassLoader.getSystemClassLoader()));

    public Registry(){
        taskFactory = new FastMap<ObjectType, TaskFactory>();
        taskFactory.shared();
        taskSubscription = new FastMap<ObjectType,FastList<TaskFactory>>().shared();

        typeFactory = new FastMap<ObjectType,TypeFactory>();
        typeFactory.shared();

        bindings = new FastMap<ObjectType,Binding>();
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
            log.debug("registered type factory for '{}'", type.getName());
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
            log.debug("registered task factory for '{}'", act.getName());
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
            log.debug("registered type factory for '{}'", binding.getName());
        }

    }

    private void refreshSubscriptions(){
        for(TaskFactory activity : taskFactory.values()){
            FastList<ObjectType> inputTypes = activity.getInputTypes();
            for(ObjectType ref : inputTypes){
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
        ObjectType ref = po.getTypeId();
        FastList<TaskFactory> list = taskSubscription.get(ref);
        //calculate activity
        if(list!=null) {
            for (TaskFactory factory : list) {
                FastList<ObjectType> types = factory.getInputTypes();
                for (ObjectType type : types) {
                    if(factory.isApplicable( po))
                    {
                        try {
                            Task task = factory.getInstance(po);
                            task.eval();
                        }catch(Exception ex){
                            log.error("error while running activity '{}'",factory.getName());
                            log.error("got exception",ex);
                        }
                    }
                }
            }
        }
    }
}
