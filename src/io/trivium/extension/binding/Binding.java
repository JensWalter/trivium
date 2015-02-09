package io.trivium.extension.binding;

import io.trivium.extension.type.Typed;

public interface Binding extends Typed {
    public String getName();

    public State getState();

    public void load();

    public void unload();

    public void start();

    public void stop();
}
