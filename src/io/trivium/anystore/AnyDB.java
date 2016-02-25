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

import io.trivium.dep.org.iq80.leveldb.CompressionType;
import io.trivium.dep.org.iq80.leveldb.Options;
import io.trivium.dep.org.iq80.leveldb.impl.Iq80DBFactory;
import io.trivium.dep.org.iq80.leveldb.DB;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyDB {
    private DB map =null;
    private Logger logger = Logger.getLogger(getClass().getName());
    public String path;
    /**
     * valid values are "meta" or "data" or "local"
     */
    public String type;


    public void put(byte[] key, byte[] value){
        map.put(key, value);
    }

    public byte[] get(byte[] key){
        return map.get(key);
    }

    public void generate() {
        try {
            File file = new File(path+type);
            try {
                Options options = new Options();
                options.createIfMissing(true);
                options.compressionType(CompressionType.NONE);
                Iq80DBFactory factory = Iq80DBFactory.factory;
                map = factory.open(file, options);
            } catch (Exception e) {
                logger.log(Level.SEVERE,"cannot initialize leveldb store "+path, e);
            }
        }catch (Exception ex){
            logger.log(Level.SEVERE,"creating file store failed",ex);
            System.exit(0);
        }
    }

    public void delete(byte[] key){
        map.delete(key);
    }
}
