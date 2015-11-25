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
import io.trivium.Registry;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.query.SortOrder;
import io.trivium.dep.io.qdb.buffer.MessageCursor;
import io.trivium.Central;
import io.trivium.dep.org.iq80.leveldb.DBIterator;
import io.trivium.dep.org.iq80.snappy.Snappy;
import io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732.Ticker;
import io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c.Differential;
import io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e.WeightedAverage;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.fact.Fact;
import io.trivium.glue.om.Json;
import io.trivium.glue.om.Trivium;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyServer implements Runnable {

	public static AnyServer INSTANCE = new AnyServer();
    Logger logger = Logger.getLogger(getClass().getName());

    //TODO size pool after index use not cpu count
    ExecutorService executors = Executors.newWorkStealingPool();
    protected String path;
    AnyDB dataMap = null;
    AnyDB metaMap = null;
	
	public AnyServer(){
        path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator;

        StoreUtils.createIfNotExists(path);
        StoreUtils.createIfNotExists(path + StoreUtils.meta);
        StoreUtils.createIfNotExists(path + StoreUtils.data);
        StoreUtils.createIfNotExists(path + StoreUtils.local);

        //check for compression flag
        TriviumObject.typeByte = Central.getProperty("compression","true").equals("true") ? (byte) 1 : (byte) 0;

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
        new AnyIndex("id", true);

        logger.log(Level.FINE,"MapStore initialized on " + path);

        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_OUT));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_SIZE));

        Central.isRunning = true;
	}

	@Override
	public void run() {
        logger.log(Level.FINE,"starting anystore server");
	    String locPipeIn = Central.getProperty("basePath") + File.separator + "queues" + File.separator + "ingestQ";
		StoreUtils.createIfNotExists(locPipeIn);
        Queue pipeIn = Queue.getQueue(locPipeIn);
		try {
            MessageCursor cursor = pipeIn.getCursor();
			while (true) {
				if (cursor.next(1000)) {
                    long readId = cursor.getId();
                    byte[] payload = cursor.getPayload();
                    TriviumObject tvm = new TriviumObject();
                    tvm.setBinary(payload);

                    storeObject(tvm);
                    pipeIn.setReadPointer(readId);

                    Profiler.INSTANCE.tick(DataPoints.ANYSTORE_QUEUE_OUT);
                    Profiler.INSTANCE.decrement(DataPoints.ANYSTORE_QUEUE_SIZE);
                    Profiler.INSTANCE.increment(DataPoints.ANYSTORE_SIZE);
                } else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		} catch (Exception e1) {
            logger.log(Level.SEVERE, "error while writing to backend", e1);
		}
	}

    public void storeObject(TriviumObject po) {
        ObjectRef refid = po.getId();
        byte[] id = refid.toBytes();
        //write metadata
        try {
            long start = System.nanoTime();
            byte[] data = po.getMetadataBinary();
            metaMap.put(id, data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_META_WRITE_DURATION, end - start);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error while writing to store", e);
        }
        //write data
        try {
            long start = System.nanoTime();
            byte[] data = po.getDataBinary();
            dataMap.put(id, data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_DATA_WRITE_DURATION, end - start);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error while writing to store", e);
        }
        //update indices
        try {
            long start = System.nanoTime();
            for (NVPair pair : po.getMetadata()) {
                AnyIndex.process(pair, refid);
            }
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_INDEX_WRITE_DURATION, end - start);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error updating index", e);
        }
        //trigger notify
        try {
            Registry.INSTANCE.notify(po);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "error notifying activities", ex);
        }
    }

    public Result loadObjects(Query query) {
        ArrayList<ObjectRef> refs = new ArrayList<>();
        //iterate over all values
        DBIterator iter = AnyIndex.iterator("id");
        while(iter.hasNext()){
            Map.Entry<byte[],byte[]> entry = iter.next();
            byte[] key = entry.getKey();
            //key consists of uuid.toString() + uuid but the constructor only reads the first value
            byte[] uuid = Arrays.copyOfRange(key,key.length-16,key.length);
            ObjectRef ref = ObjectRef.getInstance(uuid);
            refs.add(ref);
        }
        try {
            Result result = new Result();
            //check for sort criteria
            if(query.partitionOrderBy==null){
                query.partitionOrderBy = (obj) -> String.valueOf(obj.hashCode());
            }
            //check for partition criteria
            if(query.partitionOver==null){
                query.partitionOver = (obj) -> ObjectRef.getInstance().toString();
            }
            for(ObjectRef ref : refs) {
                TriviumObject tvm = loadObjectById(ref);
                Class<?> typeClass = Registry.INSTANCE.types.get(tvm.getTypeId());
                if(typeClass.equals(query.targetType)) {
                    Fact fact;
                    if (query.targetType == TriviumObject.class) {
                        fact = tvm;
                    } else {
                        fact = tvm.getTypedData();
                    }
                    if (query.condition.invoke(fact)) {
                        //matches, so now determine partition
                        String partitionKey = query.partitionOver.invoke(fact);

                        if (result.partition.containsKey(partitionKey)) {
                            //window already exists
                            ArrayList<Fact> list = result.partition.get(partitionKey);
                            list.add(fact);
                            list.sort((one, two) -> {
                                if (query.partitionSortOrder == SortOrder.ASCENDING)
                                    return query.partitionOrderBy.invoke(one).compareTo(query.partitionOrderBy.invoke(two));
                                else
                                    //negate the value
                                    return -1 * query.partitionOrderBy.invoke(one).compareTo(query.partitionOrderBy.invoke(two));
                            });
                            if (list.size() > query.partitionReduceTo) {
                                list.remove(list.size() - 1);
                            }
                        } else {
                            //create new window
                            ArrayList<Fact> list = new ArrayList<>((int) query.partitionReduceTo + 1);
                            list.add(fact);
                            result.partition.put(partitionKey, list);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "query was interrupted", e);
        }
        return new Result();
    }

    public TriviumObject loadObjectById(ObjectRef key) {
        TriviumObject po = new TriviumObject(key);

        byte[] b_metadata = metaMap.get(key.toBytes());
        if(b_metadata==null){
            //nothing found here, return empty object
            return po;
        }
        String data;
        if (b_metadata[0] == 1) {
            //decompress
            byte[] b_data = Snappy.uncompress(b_metadata, 1, b_metadata.length - 1);
            data = new String(b_data);
        } else {
            data = new String(Arrays.copyOfRange(b_metadata, 1, b_metadata.length));
        }
        NVList meta = Json.JsonToNVPairs(data);
        for (NVPair pair : meta) {
            if (pair.isArray()) {
                po.replaceMeta(pair.getName(), pair.getValue());
            } else {
                for (String val : pair.getValues()) {
                    po.replaceMeta(pair.getName(), val);
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

    public void deleteById(ObjectRef id) {
        metaMap.delete(id.toBytes());
        dataMap.delete(id.toBytes());
    }

}
