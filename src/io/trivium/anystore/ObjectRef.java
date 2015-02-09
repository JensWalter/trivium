package io.trivium.anystore;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class ObjectRef {

    private static Interner<ObjectRef> refs;
    public static ObjectRef INVALID;

    String id;
    byte[] bytes;

    static {
        refs = Interners.newWeakInterner();
        INVALID = ObjectRef.getInstance("00000000-0000-0000-0000-000000000000");
    }

    private ObjectRef() {
        UUID uuid = UUID.randomUUID();
        bytes = UUID2Bytes(uuid);
        id = uuid.toString();
    }

    private ObjectRef(String id) {
        if (id!=null && id.length() > 0) {
            UUID uuid = UUID.fromString(id);
            this.bytes = UUID2Bytes(uuid);
            this.id = id;
        } else {
            UUID uuid = UUID.randomUUID();
            this.bytes = UUID2Bytes(uuid);
            this.id = uuid.toString();
        }
    }

    private ObjectRef(byte[] bytes) {
        this.bytes = bytes;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long leastl = bb.getLong();
        long mostl = bb.getLong();
        UUID uuid = new UUID(mostl, leastl);
        this.id = uuid.toString();
    }

    private byte[] UUID2Bytes(UUID uuid) {
        long least = uuid.getLeastSignificantBits();
        long most = uuid.getMostSignificantBits();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(least);
        bb.putLong(most);
        return bb.array();
    }

    public String toString() {
        return this.id;
    }

    public byte[] toBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }else
        if(obj instanceof String){
            return this.id.equals(obj.toString());
        }else
        if(obj instanceof byte[]){
            return Arrays.equals(bytes,(byte[])obj);
        }
        else if(obj instanceof ObjectRef){
            return Arrays.equals(((ObjectRef) obj).bytes,bytes);
        }
        else return false;
    }

    @Override
    public int hashCode(){
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static ObjectRef getInstance() {
        ObjectRef ref = new ObjectRef();
        return refs.intern(ref);
    }

    public static ObjectRef getInstance(String id) {
        ObjectRef ref = new ObjectRef(id);
        return refs.intern(ref);
    }

    public static ObjectRef getInstance(byte[] id) {
        ObjectRef ref = new ObjectRef(id);
        return refs.intern(ref);
    }
}
