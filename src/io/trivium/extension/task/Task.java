package io.trivium.extension.task;

import io.trivium.extension.binding.State;
import io.trivium.extension.type.Typed;
import io.trivium.extension.binding.State;
import io.trivium.extension.type.Typed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Task implements Typed {
    protected Logger log = LogManager.getLogger(getClass());
    
    private State state;
    public abstract String getName();

    public abstract void load();

    public abstract void unload();

    public abstract boolean eval() throws Exception;

    public State getState(){
        return state;
    }

    protected void setState(State newState){
        state=newState;
    }
}
