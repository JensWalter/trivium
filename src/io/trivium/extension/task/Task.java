package io.trivium.extension.task;

import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.Typed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Task implements Typed {
    protected Logger log = LogManager.getLogger(getClass());
    
    private State state = State.stopped;

    public abstract boolean eval() throws Exception;

    public void check() throws Exception{}

    public void start(){}

    public void stop(){}

    public State getState(){
        return state;
    }

    protected void setState(State newState){
        state=newState;
    }

    public String getName(){
        return this.getClass().getCanonicalName();
    }

    public ObjectRef getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.TaskABC
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        String uuid = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
        return ObjectRef.getInstance(uuid);
    }
}
