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

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.QueryExecutor;
import io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e.WeightedAverage;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Trivium;
import io.trivium.glue.om.Json;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;
import io.trivium.Registry;
import javolution.util.FastList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iq80.snappy.Snappy;

import java.io.File;
import java.util.Arrays;

public class MapStore{
    Logger log = LogManager.getLogger(getClass());
    protected String path;
    AnyDB dataMap = null;
    AnyDB metaMap = null;

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
        TriviumObject.typeByte = Central.getProperty("compression").equals("true") ? (byte)1 : (byte)0;

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

        log.info("MapStore initialized on " + path);

        Central.isRunning=true;
    }

    public void storeObject(TriviumObject po) {
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
            log.error("error while writing to store", e);
        }
        //write data
        try {
            long start = System.nanoTime();
            byte[] data = po.getDataBinary();
            dataMap.put(id,data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_DATA_WRITE_DURATION, end - start);
        } catch (Exception e) {
            log.error("error while writing to store", e);
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
            log.error("error updating index", e);
        }
        //trigger notify
        try{
            Registry.INSTANCE.notify(po);
        }catch(Exception ex){
            log.error("error notifying activities",ex);
        }
    }

    public FastList<TriviumObject> loadObjects(Query query) {
        QueryExecutor qr = new QueryExecutor(query);
        boolean hasResult = qr.execute();

        FastList<TriviumObject> result = new FastList<>();
        if(hasResult) {
            int size = qr.getSize();
            for(int i=0;i<size;i++){
                    result.add(qr.get());
            }
        }
        return result;
    }

    public TriviumObject loadObject(ObjectRef key) throws Exception {
        TriviumObject po = new TriviumObject(key);

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
        for(NVPair pair : meta){
            if(pair.isArray()) {
                po.addMetadata(pair.getName(), pair.getValue());
            }else{
                for(String val : pair.getValues()){
                    po.addMetadata(pair.getName(),val);
                }
            }
        }
        po.setTypeId(ObjectRef.getInstance(meta.findValue("typeId")));

        byte[] b_data = dataMap.get(key.toBytes());
        if (b_data[0] == 1) {
            //decompress
            byte[] in = Snappy.uncompress(b_data, 1, b_data.length - 1);
            po.setData(Trivium.triviumJsonToElement(new String(in)));
        } else {
            po.setData(Trivium.triviumJsonToElement(new String(Arrays.copyOfRange(b_data, 1, b_data.length))));
        }

        return po;
    }

    public void delete(Query query) {
        //TODO implement
    }

}
