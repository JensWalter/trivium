/*
 * Copyright 2016 Jens Walter
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

public class TypeRef {

    private static Interner<TypeRef> refs;
    public static TypeRef INVALID;

    private String id;
    private byte[] bytes;

    static {
        refs = Interners.newWeakInterner();
        INVALID = TypeRef.getInstance("dummy");
    }

    private TypeRef(String id) {
        if (id!=null && id.length() > 0) {
            this.bytes = id.getBytes();
            this.id = id;
        } else {
            this.bytes = "".getBytes();
            this.id = "";
        }
    }

    private TypeRef(byte[] bytes) {
        this.bytes = bytes;
        this.id = new String(bytes);
    }

    /**
     * returns a string representation of the id
     * sample: "io.trivium.extension.type.FileType"
     * @return
     */
    public String toString() {
        return this.id;
    }

    /**
     * return a byte[] representation of the id
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
        else if(obj instanceof TypeRef){
            return Arrays.equals(((TypeRef) obj).bytes,bytes);
        }
        else return false;
    }

    @Override
    public int hashCode(){
        return id.hashCode();
    }

    /**
     * generates an type id from the given string
     * supported are valid package names which can be resolved by the classloader
     * @param id
     * @return
     */
    public static TypeRef getInstance(String id) {
        TypeRef ref = new TypeRef(id);
        return refs.intern(ref);
    }

    /**
     * generates an type id form the given byte[]
     * @param id
     * @return
     */
    public static TypeRef getInstance(byte[] id) {
        TypeRef ref = new TypeRef(id);
        return refs.intern(ref);
    }
}
