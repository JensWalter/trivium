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

import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.om.Json;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.DB;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyDB {
    DB map =null;
    Logger log = Logger.getLogger(getClass().getName());
    public String fileName;
    public String path;
    public String id;
    public byte[] idAsBytes;
    /**
     * valid values are "meta" or "data"
     */
    public String type;


    public void put(byte[] key, byte[] value){
        map.put(key, value);
    }

    public byte[] get(byte[] key){
        return map.get(key);
    }

    public void generate() {
        generateUniqueId(type,path);
        buildMap();
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
                log.log(Level.SEVERE,"cannot initialize leveldb store "+fileName, e);
            }
            persist();
        }catch (Exception ex){
            log.log(Level.SEVERE,"creating file store failed",ex);
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
            log.log(Level.SEVERE, "creating store meta information failed", ex);
        }
    }

    public void generateUniqueId(String type,String path){
        this.type=type;
        this.path=path;
        boolean exists = true;
        while(exists){
            //generate random string
            String randomStr = new BigInteger(130, new SecureRandom()).toString(32);
            id = randomStr.substring(0,3);
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
