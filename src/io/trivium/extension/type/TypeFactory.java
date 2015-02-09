package io.trivium.extension.type;

import io.trivium.glue.InfiniObject;
import io.trivium.glue.InfiniObject;

public interface TypeFactory<T> extends Typed{
    public String getName();
    public T getInstance(InfiniObject po);
    public InfiniObject getPersistenceObject(T instance);
}
