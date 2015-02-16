package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.om.Json;
import org.iq80.leveldb.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.FileOutputStream;

public class AnyDB extends AnyAbstract {
    DB map =null;

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
    public AnyDB cloneStore() {
        AnyDB newMap = new AnyDB();
        newMap.type = this.type;
        newMap.path = this.path;
        newMap.generate();
        return newMap;
    }

    public void buildMap(){
        try {
            File file = new File(fileName+".leveldb");
            try {
                Options options = new Options();
                options.createIfMissing(true);
                //options.compressionType(CompressionType.SNAPPY);
                options.compressionType(CompressionType.NONE);
                //options.cacheSize(50*1048576);
                //options.writeBufferSize(50*1048576);
                Iq80DBFactory factory = Iq80DBFactory.factory;
                map = factory.open(file, options);
            } catch (Exception e) {
                Central.logger.error("cannot initialize leveldb store {}", fileName, e);
            }
            persist();
        }catch (Exception ex){
            Central.logger.error("creating file store failed",ex);
            System.exit(0);
        }
    }

    private void persist(){
        NVList list = new NVList();
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
