package io.trivium.extension.type;

import io.trivium.glue.TriviumObject;

public interface TypeFactory<T> extends Typed{
    public String getName();
    public T getInstance(TriviumObject tvm);
    public TriviumObject getTriviumObject(T instance);
}
