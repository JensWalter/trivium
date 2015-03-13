package io.trivium.extension.binding;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.TypeFactory;
import io.trivium.extension.type.Typed;
import io.trivium.glue.TriviumObject;
import io.trivium.reactor.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Binding implements Typed {
    protected Logger log = LogManager.getLogger(getClass());
    private State state = State.stopped;

    public abstract void start();

    public abstract void stop();

    public void check() throws Exception{}

    public State getState(){
        return state;
    }

    protected void setState(State newState){
        state=newState;
    }

    public String getName(){
        return this.getClass().getCanonicalName();
    }

    protected void emit(Type object){
        ObjectRef typeId = object.getTypeId();
        TypeFactory<Type> factory =  Registry.INSTANCE.typeFactory.get(typeId);
        TriviumObject obj = factory.getTriviumObject(object);
        AnyClient.INSTANCE.storeObject(obj);
    }

    public ObjectRef getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.BindingABC
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        return ObjectRef.getInstance(typeId);
    }
}
