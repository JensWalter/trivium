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
import javolution.util.FastMap;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.snappy.Snappy;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class MapStore{

    protected String path;
    DB metaIndex = null;
    DB dataIndex = null;
    FastMap<String, AnyAbstract> dataMaps = new FastMap<String, AnyAbstract>();
    FastMap<String, AnyAbstract> metaMaps = new FastMap<String, AnyAbstract>();

    AnyAbstract dataBelow500b = null;
    AnyAbstract dataBelow1k = null;
    AnyAbstract dataBelow10k = null;
    AnyAbstract dataBelow100k = null;
    AnyAbstract dataBelow1m = null;
    AnyAbstract dataBigger = null;

    AnyAbstract metaBelow500b = null;
    AnyAbstract metaBelow1k = null;
    AnyAbstract metaBelow10k = null;
    AnyAbstract metaBelow100k = null;
    AnyAbstract metaBelow1m = null;
    AnyAbstract metaBigger = null;

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

        //create index
        try {
            Options options = new Options();
            options.createIfMissing(true);
            options.compressionType(CompressionType.NONE);
            Iq80DBFactory factory = Iq80DBFactory.factory;
            metaIndex = factory.open(new File(path + StoreUtils.local + "metaIndex.leveldb"), options);
            dataIndex = factory.open(new File(path + StoreUtils.local + "dataIndex.leveldb"), options);
        } catch (Exception e) {
            Central.logger.error("cannot initialize index leveldb", e);
            System.exit(0);
        }
        //create data map
        dataBelow500b = new AnyMap();
        dataBelow500b.valueSize = 500;
        dataBelow500b.type = "data";
        dataBelow500b.path = path;
        dataBelow500b.generate();
        dataMaps.put(dataBelow500b.id, dataBelow500b);

        dataBelow1k = new AnyMap();
        dataBelow1k.valueSize = 1024;
        dataBelow1k.type = "data";
        dataBelow1k.path = path;
        dataBelow1k.generate();
        dataMaps.put(dataBelow1k.id, dataBelow1k);

        dataBelow10k = new AnyMap();
        dataBelow10k.valueSize = 10240;
        dataBelow10k.type = "data";
        dataBelow10k.path = path;
        dataBelow10k.generate();
        dataMaps.put(dataBelow10k.id, dataBelow10k);

        dataBelow100k = new AnyMap();
        dataBelow100k.valueSize = 102400;
        dataBelow100k.type = "data";
        dataBelow100k.path = path;
        dataBelow100k.generate();
        dataMaps.put(dataBelow100k.id, dataBelow100k);

        dataBelow1m = new AnyMap();
        dataBelow1m.valueSize = 1024000;
        dataBelow1m.type = "data";
        dataBelow1m.path = path;
        dataBelow1m.generate();
        dataMaps.put(dataBelow1m.id, dataBelow1m);

        dataBigger = new AnyDB();
        dataBigger.valueSize = 10240000;
        dataBigger.type = "data";
        dataBigger.path = path;
        dataBigger.generate();
        dataMaps.put(dataBigger.id, dataBigger);

        //create meta map
        metaBelow500b = new AnyMap();
        metaBelow500b.valueSize = 500;
        metaBelow500b.type = "meta";
        metaBelow500b.path = path;
        metaBelow500b.generate();
        metaMaps.put(metaBelow500b.id, metaBelow500b);

        metaBelow1k = new AnyMap();
        metaBelow1k.valueSize = 1024;
        metaBelow1k.type = "meta";
        metaBelow1k.path = path;
        metaBelow1k.generate();
        metaMaps.put(metaBelow1k.id, metaBelow1k);

        metaBelow10k = new AnyMap();
        metaBelow10k.valueSize = 10240;
        metaBelow10k.type = "meta";
        metaBelow10k.path = path;
        metaBelow10k.generate();
        metaMaps.put(metaBelow10k.id, metaBelow10k);

        metaBelow100k = new AnyMap();
        metaBelow100k.valueSize = 102400;
        metaBelow100k.type = "meta";
        metaBelow100k.path = path;
        metaBelow100k.generate();
        metaMaps.put(metaBelow100k.id, metaBelow100k);

        metaBelow1m = new AnyMap();
        metaBelow1m.valueSize = 1024000;
        metaBelow1m.type = "meta";
        metaBelow1m.path = path;
        metaBelow1m.generate();
        metaMaps.put(metaBelow1m.id, metaBelow1m);

        metaBigger = new AnyDB();
        metaBigger.valueSize = 10240000;
        metaBigger.type = "meta";
        metaBigger.path = path;
        metaBigger.generate();
        metaMaps.put(metaBigger.id, metaBigger);

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
            int len = data.length;
            current = getMetaAbstract4Size(len);
            metaIndex.put(id, current.idAsBytes);
            current.put(id, data);
            //Central.logger.info("put metadata {} to {}",po.getId().toString(),current.id);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_META_WRITE_DURATION, end - start);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            //IllegalStateException -> store full
            //IllegalArgumentException -> segment full
            current = needNewStore(current);
            //retry
            metaIndex.put(id, current.idAsBytes);
            byte[] data = po.getMetadataBinary();
            current.put(id, data);
            //Central.logger.info("retry put metadata {} to {}",po.getId().toString(),current.id);
        } catch (Exception e) {
            Central.logger.error("error while writing to store", e);
        }
        //write data
        try {
            long start = System.nanoTime();
            byte[] data = po.getDataBinary();
            int len = data.length;
            current = getDataAbstract4Size(len);
            dataIndex.put(id,current.idAsBytes);
            current.put(id,data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_DATA_WRITE_DURATION, end - start);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            //IllegalStateException -> store full
            //IllegalArgumentException -> segment full
            current = needNewStore(current);
            byte[] data = po.getDataBinary();
            dataIndex.put(id,current.idAsBytes);
            current.put(id,data);
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

    private AnyAbstract needNewStore(AnyAbstract current) {
        Central.logger.debug("generating new store for size {} {}",current.valueSize,current.fileName);
        AnyAbstract newMap = current.cloneStore();
        switch(current.type){
            case "data":
                dataMaps.put(newMap.id,newMap);
                if(newMap.valueSize==500){
                    dataBelow500b=newMap;
                }else if(newMap.valueSize==1024){
                    dataBelow1k=newMap;
                }else if(newMap.valueSize==10240){
                    dataBelow10k=newMap;
                }else if(newMap.valueSize==102400){
                    dataBelow100k=newMap;
                }else if(newMap.valueSize==1024000){
                    dataBelow1m=newMap;
                }else{
                    dataBigger =newMap;
                }
                break;
            case "meta":
                metaMaps.put(newMap.id,newMap);
                if(newMap.valueSize==500){
                    metaBelow500b=newMap;
                }else if(newMap.valueSize==1024){
                    metaBelow1k=newMap;
                }else if(newMap.valueSize==10240){
                    metaBelow10k=newMap;
                }else if(newMap.valueSize==102400){
                    metaBelow100k=newMap;
                }else if(newMap.valueSize==1024000){
                    metaBelow1m=newMap;
                }else{
                    metaBigger =newMap;
                }
                break;
        }
        return newMap;
    }

    private AnyAbstract getDataAbstract4Size(int size){
        if(size<=500){
            return dataBelow500b;
        }else if(size<=1024){
            return dataBelow1k;
        }else if(size<=10240){
            return dataBelow10k;
        }else if(size<=102400){
            return dataBelow100k;
        }else if(size<=1024000){
            return dataBelow1m;
        }else{
            return dataBigger;
        }
    }

    private AnyAbstract getMetaAbstract4Size(int size){
        if(size<=500){
            return metaBelow500b;
        }else if(size<=1024){
            return metaBelow1k;
        }else if(size<=10240){
            return metaBelow10k;
        }else if(size<=102400){
            return metaBelow100k;
        }else if(size<=1024000){
            return metaBelow1m;
        }else{
            return metaBigger;
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

    public InfiniObject loadObject(ObjectRef key) throws Exception{
        InfiniObject po = new InfiniObject(key);
        byte[] b_metastore = metaIndex.get(key.toBytes());
        if( b_metastore != null){
            String storeid = new String(b_metastore);
            AnyAbstract metaMap = metaMaps.get(storeid);
            if(metaMap==null){
                Central.logger.error("meta store missing {}",storeid);
            }else {
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
                po.setTypeId(ObjectRef.getInstance(meta.findValue("typeId")));
            }
        }else{
            Central.logger.error("no meta store for id {}",key.toString());
            throw new Exception("no meta store for id "+key.toString());
        }
        byte[] b_datastore = dataIndex.get(key.toBytes());
        if( b_datastore != null){
            String storeid = new String(b_datastore);
            AnyAbstract dataMap = dataMaps.get(storeid);
            if(dataMap==null){
                Central.logger.error("data store missing {}",storeid);
            }else {
                byte[] b_data = dataMap.get(key.toBytes());
                if(b_data[0]==1){
                    //decompress
                    byte[] in = Snappy.uncompress(b_data,1,b_data.length-1);
                    po.setData(Infinup.infiniupToInternal(new String(in)));
                }else{
                    po.setData(Infinup.infiniupToInternal(new String(Arrays.copyOfRange(b_data,1,b_data.length))));
                }
            }
        }else{
            Central.logger.error("no data store for id {}",key.toString());
            throw new Exception("no data store for id "+key.toString());
        }
        return po;
    }

    public void delete(Query query) {
        //TODO implement
    }

}
