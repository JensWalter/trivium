package io.trivium.glue;

import com.google.common.primitives.Bytes;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.TypeFactory;
import io.trivium.extension.type.Typed;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Trivium;
import io.trivium.glue.om.Json;
import io.trivium.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iq80.snappy.Snappy;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public class TriviumObject implements Typed {

    /**
     * typeByte
     * 0 uncompressed json
     * 1 snappy compressed json
     */
    public static byte typeByte = 1;
    Logger log = LogManager.getLogger(getClass());
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
    }

    public TriviumObject(ObjectRef id){
        this.id = id;
        typeId = ObjectRef.INVALID;
        metadata = new NVList();
        replaceMeta("id",id.toString());
        data = Element.EMPTY;
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
        for(NVPair entry : metadata){
            if(entry.getName().equals(name)){
                metadata.remove(entry);
            }
        }
        metadata.add(new NVPair(name,value));
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

    public <T> T getTypedData(){
        ObjectRef typeId = this.getTypeId();
        try {
            TypeFactory<T> tf = Registry.INSTANCE.typeFactory.get(typeId);
            T obj = tf.getInstance(this);
            return obj;
        }catch(Exception ex){
            log.error("error constructing object",ex);
            return null;
        }
    }

    public void setDataBinary(byte[] input){
        b_data = input;
        data = Element.EMPTY;
    }

    private void data2Binary(){
        byte[] b_data = Trivium.internalToTrivium(data).getBytes();
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
            data = Trivium.triviumToInternal(new String(in));
        }else{
            data= Trivium.triviumToInternal(new String(Arrays.copyOfRange(b_data, 1, b_data.length)));
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
        //FIXME make == work again
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
        meta.add(new NVPair("sourceKey", "infinup"));
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
}
