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

package io.trivium.extension._f70b024ca63f4b6b80427238bfff101f;

import io.trivium.dep.com.google.common.primitives.Bytes;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.Registry;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.fact.Fact;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.glue.om.Trivium;
import io.trivium.dep.org.iq80.snappy.Snappy;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TriviumObject implements Fact {
    /**
     * typeByte
     * 0 uncompressed json
     * 1 snappy compressed json
     */
    public static byte typeByte = 1;
    Logger logger = Logger.getLogger(getClass().getName());
    ObjectRef id;
    ObjectRef typeId;
    NVList metadata;
    byte[] b_metadata;
    Element data;
    byte[] b_data;

    public TriviumObject(){
        id = ObjectRef.getInstance();
        typeId = ObjectRef.INVALID;
        metadata = new NVList();
        replaceMeta("id", id.toString());
        data = Element.EMPTY;
        checkMetadata();
    }

    public TriviumObject(ObjectRef id){
        this.id = id;
        typeId = ObjectRef.INVALID;
        metadata = new NVList();
        replaceMeta("id",id.toString());
        data = Element.EMPTY;
        checkMetadata();
    }

    public ObjectRef getId(){
        return id;
    }

    public void setId(ObjectRef id){
        this.id=id;
        replaceMeta("id",id.toString());
    }

    @Override
    public ObjectRef getTypeId(){
        return typeId;
    }

    public void setTypeId(ObjectRef newType){
        typeId = newType;
        metadata.replace(new NVPair("typeId",newType.toString()));
    }

    public NVList getMetadata(){
        return metadata;
    }

    public void addMetadata(String name,String value){
        if(name.equals("id")){
            setId(ObjectRef.getInstance(value));
        }else
        if(name.equals("typeId")){
            setTypeId(ObjectRef.getInstance(value));
        }else
            // look for existing entry
            if(hasMetaKey(name)) {
                for (NVPair p : metadata) {
                    if (p.getName().equals(name)) {
                        p.addValue(value);
                    }
                }
            }else {
                metadata.add(new NVPair(name, value));
            }

    }

    public boolean hasMetaKey(String key){
        for(NVPair pair : metadata){
            if(pair.getName().equals(key))
                return true;
        }
        return false;
    }

    public void replaceMeta(String name,String value){
        metadata.replace(new NVPair(name,value));
    }

    public ArrayList<NVPair> findMetadata(String name) {
        ArrayList<NVPair> rslt = new ArrayList<NVPair>();
        for (NVPair pair : metadata) {
            if (pair.getName().equals(name))
                rslt.add(pair);
        }
        return rslt;
    }

    public String findMetaValue(String name) {
        for (NVPair pair : metadata) {
            if (pair.getName().equals(name))
                return pair.getValue();
        }
        return null;
    }

    public String getMetadataJson(){
        String json = Json.NVPairsToJson(metadata);
        return json;
    }

    public byte[] getMetadataBinary(){
        if(b_metadata==null){
            meta2Binary();
        }
        return b_metadata;
    }

    public void setMetadataBinary(byte[] input){
        b_metadata = input;
        binary2Meta();
    }

    private void binary2Meta(){
        String data;
        if (b_metadata[0] == 1) {
            //decompress
            byte[] b_data = Snappy.uncompress(b_metadata, 1, b_metadata.length - 1);
            data = new String(b_data);
        } else {
            data = new String(Arrays.copyOfRange(b_metadata, 1, b_metadata.length));
        }
        metadata = Json.JsonToNVPairs(data);
        checkMetadata();
    }

    private void meta2Binary(){
        byte[] data = Json.NVPairsToJson(metadata).getBytes();
        if (typeByte ==1) {
            byte[] compressed = new byte[Snappy.maxCompressedLength(data.length)];
            int count = Snappy.compress(data, 0, data.length, compressed, 0);
            data = Arrays.copyOf(compressed, count);
        }
        b_metadata = Bytes.concat(new byte[]{typeByte}, data);
    }

    public Element getData(){
        if(data==Element.EMPTY){
            binary2Data();
        }
        return data;
    }

    public void setData(Element input){
        data=input;
        b_data=null;
        checkMetadata();
    }

    public byte[] getDataBinary(){
        if(b_data==null){
            data2Binary();
        }
        return b_data;
    }

    public <T extends Fact> T getTypedData(){
        ObjectRef typeId = this.getTypeId();
        try {
            Class<? extends Fact> clazz = Registry.INSTANCE.types.get(typeId);
            if(clazz==null){
                //no registered type found
                return (T) this;
            }else {
                //build from factory
                T obj = (T) Registry.INSTANCE.types.get(typeId).newInstance();
                obj.populate(this);
                return obj;
            }
        }catch(Exception ex){
            logger.log(Level.SEVERE,"error constructing object", ex);
            return null;
        }
    }

    public void setDataBinary(byte[] input){
        b_data = input;
        data = Element.EMPTY;
    }

    public void setBinary(byte[] input){
        ByteBuffer bb = ByteBuffer.wrap(input);
        int metaSize = bb.getInt();
        int dataSize = bb.getInt();
        byte[] meta = new byte[metaSize];
        byte[] data = new byte[dataSize];
        bb.get(meta);
        setMetadataBinary(meta);
        bb.get(data);
        setDataBinary(data);
    }

    public byte[] getBinary(){
        byte[] data = getDataBinary();
        byte[] meta = getMetadataBinary();
        //allocation size is both data block plus 2 longs for size
        ByteBuffer bb = ByteBuffer.allocate(data.length+meta.length+8);
        bb.putInt(meta.length);
        bb.putInt(data.length);
        bb.put(meta);
        bb.put(data);

        return bb.array();
    }

    private void data2Binary(){
        byte[] b_data = Trivium.elementToTriviumJson(data).getBytes();
        if (typeByte==1) {
            byte[] compressed = new byte[Snappy.maxCompressedLength(b_data.length)];
            int count = Snappy.compress(b_data, 0, b_data.length, compressed, 0);
            b_data = Arrays.copyOf(compressed, count);
        }
        this.b_data = Bytes.concat(new byte[]{typeByte},b_data);
    }

    private void binary2Data(){
        if(b_data[0]==1){
            //decompress
            byte[] in = Snappy.uncompress(b_data,1,b_data.length-1);
            data = Trivium.triviumJsonToElement(new String(in));
        }else{
            data= Trivium.triviumJsonToElement(new String(Arrays.copyOfRange(b_data, 1, b_data.length)));
        }
    }

    private void checkMetadata() {
        if (id == null) {
            if (metadata!=null && metadata.hasKey("id")) {
                id = ObjectRef.getInstance(metadata.findValue("id"));
            }else {
                id = ObjectRef.getInstance();
            }
        }
        if (typeId == null || typeId == ObjectRef.INVALID) {
            if (metadata!=null && metadata.hasKey("typeId")) {
                typeId = ObjectRef.getInstance(metadata.findValue("typeId"));
            }
        }
        NVList meta = metadata;

        if (!meta.hasKey("id")) {
            meta.add(new NVPair("id", id.toString()));
        }
        if (!meta.hasKey("created")) {
            meta.add(new NVPair("created", Instant.now().toString()));
        }
        if (!meta.hasKey("type")) {
            meta.add(new NVPair("type", "object"));
        }
        if (!meta.hasKey("typeId")) {
            meta.add(new NVPair("typeId", getTypeId().toString()));
        }
        if (!meta.hasKey("size") && (b_data != null || data != Element.EMPTY)) {
            if (b_data == null) {
                data2Binary();
            }
            meta.add(new NVPair("size", String.valueOf(b_data.length)));
        }
        if (!meta.hasKey("idempotencyKey") && (b_data != null || data != Element.EMPTY)) {
            //if no idempotency key is present use md4 of the content
            if (b_data == null) {
                data2Binary();
            }
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD4");
                messageDigest.update(b_data);
                String digest = new String(messageDigest.digest());
                meta.add(new NVPair("idempotencyKey", digest));
            } catch (Exception ex) {
                //ignore
            }
        }
    }

    public static TriviumObject getTriviumObject(Fact t){
        try {
            return t.toTriviumObject();
        }catch(Exception ex){
            Logger.getLogger(TriviumObject.class.getName()).log(Level.SEVERE,"error constructing object", ex);
            return null;
        }
    }
}
