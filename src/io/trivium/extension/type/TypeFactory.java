package io.trivium.extension.type;

import io.trivium.glue.TriviumObject;

public interface TypeFactory<T> extends Typed{
    String getName();
    T getInstance(TriviumObject tvm);
    TriviumObject getTriviumObject(T instance);
}
