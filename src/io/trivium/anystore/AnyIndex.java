package io.trivium.anystore;

import com.google.common.hash.Funnels;
import com.google.common.primitives.Bytes;
import com.google.common.hash.BloomFilter;
import io.trivium.Central;
import io.trivium.NVPair;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.Logger;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Stream;

public class AnyIndex{
    private static FastMap<String,AnyIndex> ALL = new FastMap<String,AnyIndex>().shared();
    Logger log = LogManager.getLogger(getClass());
    private String name;
    private long entries;
    private DB index;
    /**
     * 1mb hash size => 2^20 bit *100 werte
     */
    private int hashSize = 10485760;
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
        log.debug("creating index for field {}",name);
        this.name=name;
        entries=0;

        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator+"local" + File.separator;

        try {
            Options options = new Options();
            options.createIfMissing(true);
            //options.compressionType(CompressionType.SNAPPY);
            options.compressionType(CompressionType.NONE);
            //options.cacheSize(50*1048576);
            //options.writeBufferSize(50*1048576);
            Iq80DBFactory factory = Iq80DBFactory.factory;
            index = factory.open(new File(path + name+".leveldb"), options);
        } catch (Exception e) {
            log.error("cannot initialize index {}", name, e);
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
                    log.debug("disabling index for column {} with variance {}", name,variance);
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
        FastList<ObjectRef> keyList = new FastList<ObjectRef>();
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
