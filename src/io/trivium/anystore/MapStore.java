package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.QueryExecutor;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Infinup;
import io.trivium.glue.om.Json;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;
import io.trivium.profile.WeightedAverage;
import io.trivium.reactor.Registry;
import javolution.util.FastList;
import org.iq80.snappy.Snappy;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class MapStore{

    protected String path;
    AnyAbstract dataMap = null;
    AnyAbstract metaMap = null;

    AtomicLong queryCount = new AtomicLong(0);

    public MapStore() {
        path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator;

        StoreUtils.createIfNotExists(path);
        StoreUtils.createIfNotExists(path + StoreUtils.meta);
        StoreUtils.createIfNotExists(path + StoreUtils.data);
        StoreUtils.createIfNotExists(path + StoreUtils.local);

        //check for compression flag
        InfiniObject.typeByte = Central.getProperty("compression").equals("true") ? (byte)1 : (byte)0;

        //init profiler
        Profiler.INSTANCE.initAverage(new WeightedAverage(DataPoints.ANYSTORE_DATA_WRITE_DURATION));
        Profiler.INSTANCE.initAverage(new WeightedAverage(DataPoints.ANYSTORE_META_WRITE_DURATION));
        Profiler.INSTANCE.initAverage(new WeightedAverage(DataPoints.ANYSTORE_INDEX_WRITE_DURATION));

        //create data map
        dataMap = new AnyDB();
        dataMap.type = "data";
        dataMap.path = path;
        dataMap.generate();

        //create meta map
        metaMap = new AnyDB();
        metaMap.type = "meta";
        metaMap.path = path;
        metaMap.generate();

        //create primary key index - just in case
        new AnyIndex("id",true);

        Central.logger.info("MapStore initialized on " + path);

        Central.isRunning=true;
    }

    public void storeObject(InfiniObject po) {
        AnyAbstract current=null;
        ObjectRef refid = po.getId();
        byte[] id=refid.toBytes();
       // Central.logger.info("trying to store metadata {} {}",po.findMetaValue("datapoint"),po.getId().toString());
        //write metadata
        try {
            long start = System.nanoTime();
            byte[] data = po.getMetadataBinary();
            metaMap.put(id, data);
            //Central.logger.info("put metadata {} to {}",po.getId().toString(),current.id);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_META_WRITE_DURATION, end - start);
        } catch (Exception e) {
            Central.logger.error("error while writing to store", e);
        }
        //write data
        try {
            long start = System.nanoTime();
            byte[] data = po.getDataBinary();
            dataMap.put(id,data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_DATA_WRITE_DURATION, end - start);
        } catch (Exception e) {
            Central.logger.error("error while writing to store", e);
        }
        //update indices
        try {
            long start = System.nanoTime();
            for(NVPair pair : po.getMetadata()){
                AnyIndex.process(pair,refid);
            }
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_INDEX_WRITE_DURATION, end - start);
        } catch (Exception e) {
            Central.logger.error("error updating index", e);
        }
        //trigger notify
        try{
            Registry.INSTANCE.notify(po);
        }catch(Exception ex){
            Central.logger.error("error notifying activities",ex);
        }
    }

    public FastList<InfiniObject> loadObjects(Query query) {
        QueryExecutor qr = new QueryExecutor(query);
        boolean hasResult = qr.execute();

        FastList<InfiniObject> result = new FastList<InfiniObject>();
        if(hasResult) {
            int size = qr.getSize();
            for(int i=0;i<size;i++){
                    result.add(qr.get());
            }
        }
        return result;
    }

    public InfiniObject loadObject(ObjectRef key) throws Exception {
        InfiniObject po = new InfiniObject(key);

        byte[] b_metadata = metaMap.get(key.toBytes());
        String data;
        if (b_metadata[0] == 1) {
            //decompress
            byte[] b_data = Snappy.uncompress(b_metadata, 1, b_metadata.length - 1);
            data = new String(b_data);
        } else {
            data = new String(Arrays.copyOfRange(b_metadata, 1, b_metadata.length));
        }
        NVList meta = Json.JsonToNVPairs(data);
        po.setMetadata(meta);
        //FIXME find correct version
        po.setTypeId(ObjectType.getInstance(meta.findValue("typeId"), "v1"));

        byte[] b_data = dataMap.get(key.toBytes());
        if (b_data[0] == 1) {
            //decompress
            byte[] in = Snappy.uncompress(b_data, 1, b_data.length - 1);
            po.setData(Infinup.infiniupToInternal(new String(in)));
        } else {
            po.setData(Infinup.infiniupToInternal(new String(Arrays.copyOfRange(b_data, 1, b_data.length))));
        }

        return po;
    }

    public void delete(Query query) {
        //TODO implement
    }

}
