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

import com.google.common.hash.Funnels;
import com.google.common.primitives.Bytes;
import com.google.common.hash.BloomFilter;
import io.trivium.Central;
import io.trivium.NVPair;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.DB;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class AnyIndex{
    private static ConcurrentHashMap<String,AnyIndex> ALL = new ConcurrentHashMap<>();
    Logger log = Logger.getLogger(getClass().getName());
    private String name;
    private long entries;
    private DB index;

    BloomFilter<String> valueBloomFilter;
    BloomFilter<String> vrBloomFilter;

    long bloomCount = 0;
    boolean indexEnabled = true;
    private long queries = 0;
    boolean forced = false;

    public AnyIndex(String name,boolean force){
        this(name);
        forced = force;
    }

    private AnyIndex(String name){
        log.log(Level.FINE,"creating index for field {}", name);
        this.name=name;
        entries=0;

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
            log.log(Level.SEVERE,"cannot initialize index " + name, e);
            System.exit(0);
        }

        double falsePositiveProbability = 0.1;

        //TODO create file persistence
        valueBloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),1000000,falsePositiveProbability);
        vrBloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),1000000,falsePositiveProbability);
        
        //valueBloomFilter = new BloomFilter<String>(path + name+"_value.bloomfilter",falsePositiveProbability, hashSize);
        //vrBloomFilter = new BloomFilter<String>(path + name+"_valref.bloomfilter",falsePositiveProbability, hashSize);

        ALL.put(name,this);
    }

    private void index(NVPair pair,ObjectRef ref){
        entries++;

        if (indexEnabled) {
            boolean isNew = ! valueBloomFilter.mightContain(pair.getValue());
            valueBloomFilter.put(pair.getValue());
            vrBloomFilter.put(pair.getValue() + ref.toString());

            if (isNew) {
                bloomCount += 1;
            }

            byte[] b_value = pair.getValue().getBytes();
            byte[] b_ref = ref.toBytes();
            byte[] b_valueRef = Bytes.concat(b_value,b_ref);

            index.put(b_valueRef, b_ref);

            if(entries%10000==0){
                long cardinality = bloomCount;
                double variance = (double) cardinality / entries;
                if(variance< 0.01d){
                    indexEnabled=false;
                    //delete the index
                    DBIterator iterator = index.iterator();
                    try {
                        for(iterator.seekToFirst(); iterator.hasNext();) {
                            byte[] k = iterator.next().getKey();
                            index.delete(k);
                        }
                    } finally {
                        try {
                            iterator.close();
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                    valueBloomFilter =null;
                    vrBloomFilter = null;
                    log.log(Level.FINE,"disabling index for column {} with variance {}", new Object[]{name,variance});
                }
            }
        }
    }

    private static AnyIndex get(String name){
        AnyIndex idx = ALL.get(name);
        if(idx==null){
            idx = new AnyIndex(name);
        }
        return idx;
    }

    public static Stream<ObjectRef> lookup(String name, String value){
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
        return keyList.stream();
    }

    public static double getVariance(String name){
        AnyIndex idx = get(name);
        long cardinality = idx.bloomCount;
        double variance = (double) cardinality / idx.entries;
        return variance;
    }

    public static void process(NVPair pair, ObjectRef ref){
        String name = pair.getName();
        AnyIndex idx = get(name);
        idx.index(pair,ref);
    }

    public static boolean check(String name,String value){
        AnyIndex idx = ALL.get(name);
        if(idx!=null){
            idx.queries+=1;
            if(idx.indexEnabled) {
                return idx.valueBloomFilter.mightContain(value);
            }else{
                return true;
            }
        }else{
            //no check possible
            return true;
        }
    }
}
