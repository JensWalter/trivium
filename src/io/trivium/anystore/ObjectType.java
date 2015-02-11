package io.trivium.anystore;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class ObjectType {

    //FIXME make this correct
    public static ObjectType INVALID = ObjectType.getInstance(ObjectRef.INVALID.toString(),"v1");
    
    private ObjectRef ref;
    private String version;

    private ObjectType(String typeId,String version) {
        this.ref = ObjectRef.getInstance(typeId);
        this.version = version;
    }
    
    public ObjectRef getObjectRef(){
        return ref;
    }

    public String toString() {
        return ref.toString()+version;
    }

    public String toPackageString() {
        return "_"+ref.toString().replace("-","")+"."+version;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }else if(obj instanceof ObjectType){
            ObjectType type = (ObjectType) obj;
            //TODO make better
            return type.ref==this.ref && type.version.equals(this.version);
        }
        else return false;
    }

    @Override
    public int hashCode(){
        return ByteBuffer.wrap(ref.toBytes()).getInt();
    }

    public static ObjectType getInstance(String typeId,String version) {
        ObjectType ref = new ObjectType(typeId, version);
        return ref;
    }
}
