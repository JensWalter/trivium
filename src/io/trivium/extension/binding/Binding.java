package io.trivium.extension.binding;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.Type;
import io.trivium.extension.type.TypeFactory;
import io.trivium.extension.type.Typed;
import io.trivium.glue.TriviumObject;
import io.trivium.reactor.Registry;

public interface Binding extends Typed {
    public String getName();

    public State getState();

    public void load();

    public void unload();

    public void start();

    public void stop();

    public default void emit(Type object){
        ObjectRef typeId = object.getTypeId();
        TypeFactory<Type> factory =  Registry.INSTANCE.typeFactory.get(typeId);
        TriviumObject obj = factory.getTriviumObject(object);
        AnyClient.INSTANCE.storeObject(obj);
    }
}
