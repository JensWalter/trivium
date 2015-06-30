package io.trivium.extension.task;

import io.trivium.extension.type.Typed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Task implements Typed {
    protected Logger log = LogManager.getLogger(getClass());

    public abstract boolean eval() throws Exception;

    public void check() throws Exception{}

    public String getName(){
        return this.getClass().getCanonicalName();
    }
}
