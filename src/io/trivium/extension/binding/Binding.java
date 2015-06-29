package io.trivium.extension.binding;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.TypeFactory;
import io.trivium.extension.type.Typed;
import io.trivium.glue.TriviumObject;
import io.trivium.Registry;
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
}
