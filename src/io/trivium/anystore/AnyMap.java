package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.Hardware;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.om.Json;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileOutputStream;

public class AnyMap extends AnyAbstract {
    ChronicleMap<byte[],byte[]> map =null;

    public void put(byte[] key, byte[] value){
        map.put(key, value);
    }

    public byte[] get(byte[] key){
        return map.get(key);
    }

    @Override
    public void generate() {
        generateUniqueId(type,path);
        buildMap();
    }

    @Override
    public AnyMap cloneStore() {
        AnyMap newMap = new AnyMap();
        newMap.valueSize = this.valueSize;
        newMap.type = this.type;
        newMap.path = this.path;
        newMap.generate();
        return newMap;
    }

    public void buildMap(){
        try {
            File file = new File(fileName+".map");
            ChronicleMapBuilder<byte[], byte[]> builder =
                    ChronicleMapBuilder.of(byte[].class, byte[].class);
            builder.keySize(16);
            builder.immutableKeys();
            builder.valueSize(valueSize);
            //use 5% of ram
            long memSize = Hardware.memSize;
            builder.entries(Math.round(memSize*0.05)/(valueSize+16));
            builder.putReturnsNull(true);
            builder.removeReturnsNull(true);
            map = builder.create(file);
            persist();
        }catch (Exception ex){
            Central.logger.error("creating file store failed",ex);
            System.exit(0);
        }
    }

    private void persist(){
        NVList list = new NVList();
        list.add(new NVPair("valueSize", String.valueOf(valueSize)));
        list.add(new NVPair("fileName", fileName));
        list.add(new NVPair("path", path));
        list.add(new NVPair("id", id));
        String str = Json.NVPairsToJson(list);
        try {
            FileOutputStream fos = new FileOutputStream(fileName + ".json");
            fos.write(str.getBytes());
            fos.close();
        }catch(Exception ex){
            Central.logger.error("creating store meta information failed",ex);
        }
    }

    public void generateUniqueId(String type,String path){
        this.type=type;
        this.path=path;
        boolean exists = true;
        while(exists){
            id = RandomStringUtils.randomAlphanumeric(4);
            idAsBytes = id.getBytes();
            switch (type) {
                case "data":
                    fileName = path + StoreUtils.data + id;
                    break;
                case "meta":
                    fileName = path + StoreUtils.meta + id;
                    break;
                default:
                    fileName = path + StoreUtils.local + id;
            }
            exists = new File(fileName).exists();
        }
    }
}
