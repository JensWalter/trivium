/*
 * Copyright 2015 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium.anystore;

import io.trivium.dep.com.google.common.collect.Interner;
import io.trivium.dep.com.google.common.collect.Interners;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class ObjectRef {

    private static Interner<ObjectRef> refs;
    public static ObjectRef INVALID;

    private String id;
    private byte[] bytes;

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

    /**
     * returns a uuid representation of the id
     * sample: "00000000-0000-0000-0000-000000000000"
     * @return
     */
    public String toString() {
        return this.id;
    }

    /**
     * returns a string represnting the uuid,
     * but differs in form so it can be used in other environments (code generators)
     * sample: "_00000000000000000000000000000000"
     * @return
     */
    public String toMangledString() {
        return "_"+this.id.replace("-", "");
    }

    /**
     * return a byte[] representation of the uuid
     * uuid.getLeastSignificantBits() + uuid.getMostSignificantBits()
     * @return
     */
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

    /**
     * generates a random object id
     * @return
     */
    public static ObjectRef getInstance() {
        ObjectRef ref = new ObjectRef();
        return refs.intern(ref);
    }

    /**
     * generates an object id from the given string
     * supported formats are:
     *  "00000000-0000-0000-0000-000000000000"
     *  "_00000000000000000000000000000000"
     * @param id
     * @return
     */
    public static ObjectRef getInstance(String id) {
        ObjectRef ref;
        if(id.charAt(0)=='_'){
            //format "_00000000000000000000000000000000"
            String uuid = id.substring(1,9)+"-"+id.substring(9,13)+"-"+id.substring(13,17)
                    +"-"+id.substring(17,21)+"-"+id.substring(21,33);
            ref = new ObjectRef(uuid);
        }else {
            //format "00000000-0000-0000-0000-000000000000"
            ref = new ObjectRef(id);

        }
        return refs.intern(ref);
    }

    /**
     * generates an object id form the given byte[]
     * byte order is uuid.getLeastSignificantBits() + uuid.getMostSignificantBits()
     * @param id
     * @return
     */
    public static ObjectRef getInstance(byte[] id) {
        ObjectRef ref = new ObjectRef(id);
        return refs.intern(ref);
    }
}
