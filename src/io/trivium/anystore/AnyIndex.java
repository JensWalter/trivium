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

import io.trivium.dep.com.google.common.primitives.Bytes;
import io.trivium.Central;
import io.trivium.NVPair;
import io.trivium.dep.org.iq80.leveldb.CompressionType;
import io.trivium.dep.org.iq80.leveldb.DBIterator;
import io.trivium.dep.org.iq80.leveldb.Options;
import io.trivium.dep.org.iq80.leveldb.impl.Iq80DBFactory;
import io.trivium.dep.org.iq80.leveldb.DB;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyIndex{
    private static ConcurrentHashMap<String,AnyIndex> ALL = new ConcurrentHashMap<>();
    Logger logger = Logger.getLogger(getClass().getName());
    private String name;
    private DB index;

    boolean indexEnabled = true;
    boolean forced = false;

    public AnyIndex(String name,boolean force){
        this(name);
        forced = force;
    }

    private AnyIndex(String name){
        logger.log(Level.FINE,"creating index for field {1}", name);
        this.name=name;

        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator+"local" + File.separator;

        try {
            Options options = new Options();
            options.createIfMissing(true);
            options.compressionType(CompressionType.NONE);
            Iq80DBFactory factory = Iq80DBFactory.factory;
            index = factory.open(new File(path + name+".leveldb"), options);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"cannot initialize index " + name, e);
            System.exit(0);
        }

        ALL.put(name,this);
    }

    private void index(NVPair pair,ObjectRef ref){
        if (indexEnabled) {
            byte[] b_value = pair.getValue().getBytes();
            byte[] b_ref = ref.toBytes();
            byte[] b_valueRef = Bytes.concat(b_value,b_ref);

            index.put(b_valueRef, b_ref);
        }
    }

    private static AnyIndex get(String name){
        AnyIndex idx = ALL.get(name);
        if(idx==null){
            idx = new AnyIndex(name);
        }
        return idx;
    }

    public static ArrayList<ObjectRef> lookup(String name, String value){
        AnyIndex idx = ALL.get(name);
        DBIterator iter = idx.index.iterator();
        byte[] b_value = value.getBytes();
        iter.seek(b_value);
        ArrayList<ObjectRef> keyList = new ArrayList<>();
        while(iter.hasNext()){
            Map.Entry<byte[],byte[]> entry = iter.next();
            byte[] key = entry.getKey();
            if(Bytes.indexOf(key,b_value)==0){
                keyList.add(ObjectRef.getInstance(entry.getValue()));
            }else{
                break;
            }
        }
        return keyList;
    }

    public static DBIterator iterator(String name){
        AnyIndex idx = ALL.get(name);
        return idx.index.iterator();
    }

    public static void process(NVPair pair, ObjectRef ref){
        String name = pair.getName();
        AnyIndex idx = get(name);
        idx.index(pair,ref);
    }
}
