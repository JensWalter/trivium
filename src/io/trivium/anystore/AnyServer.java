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
import io.trivium.anystore.query.Criteria;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Range;
import io.trivium.anystore.query.RangeType;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.query.Value;
import io.trivium.dep.io.qdb.buffer.MessageCursor;
import io.trivium.Central;
import io.trivium.dep.org.iq80.snappy.Snappy;
import io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732.Ticker;
import io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c.Differential;
import io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e.WeightedAverage;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Json;
import io.trivium.glue.om.Trivium;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyServer implements Runnable {

	public static AnyServer INSTANCE = new AnyServer();
    Logger log = Logger.getLogger(getClass().getName());

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

        log.log(Level.FINE,"MapStore initialized on " + path);

        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_OUT));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_SIZE));

        Central.isRunning = true;
	}

	@Override
	public void run() {
	    log.log(Level.FINE,"starting anystore server");
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
			log.log(Level.SEVERE, "error while writing to backend", e1);
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
            log.log(Level.SEVERE, "error while writing to store", e);
        }
        //write data
        try {
            long start = System.nanoTime();
            byte[] data = po.getDataBinary();
            dataMap.put(id, data);
            long end = System.nanoTime();
            Profiler.INSTANCE.avg(DataPoints.ANYSTORE_DATA_WRITE_DURATION, end - start);
        } catch (Exception e) {
            log.log(Level.SEVERE, "error while writing to store", e);
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
            log.log(Level.SEVERE, "error updating index", e);
        }
        //trigger notify
        try {
            Registry.INSTANCE.notify(po);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error notifying activities", ex);
        }
    }

    public Result loadObjects(Query query) {
        //run query
        ArrayList<Criteria> criteria = query.criteria;
        ArrayList<Callable<ArrayList<ObjectRef>>> jobs = new ArrayList<>();
        //run index jobs
        for (Criteria crit : criteria) {
            if (crit instanceof Value) {
                jobs.add(() -> {
                    try {
                        Value val = (Value) crit;
                        ArrayList<ObjectRef> refs = AnyIndex.lookup(val.getName(), val.getValue());
                        return refs;
                    }catch (Throwable e){
                        //ignore
                    }
                    return new ArrayList<>();
                });
            }
        }
        try {
            ArrayList<ObjectRef> refs = executors.invokeAny(jobs);
            //evaluate index results and filter results
            Result rslt = new Result();
            refs.forEach((ref) -> evaluate(ref, query, rslt));
            return rslt;
        } catch (Exception e) {
            log.log(Level.SEVERE, "query was interrupted", e);
        }
        return new Result();
    }

    private void evaluate(ObjectRef ref, Query query, Result result){
        TriviumObject tvm = loadObjectById(ref);
        ScriptEngine engine = result.scriptEngine;
        String id = "_"+ref.toString();
        //put data object
        engine.put(id,tvm.getTypedData());
        //add header data
        engine.put("header",tvm.getMetadata());
        try {
            String partitionKey = "";
            //get partition
            if(tvm.getMetadata().hasKey(query.reducePartitionBy)){
                //partition key is in the header
                partitionKey = tvm.getMetadata().findValue(query.reducePartitionBy);
            }else{
                //partition key is in the content
                engine.eval("var partition = "+id+"."+query.reducePartitionBy);
                partitionKey = engine.get("partition").toString();
            }
            //check criteria
            boolean valid = true;
            for (Criteria crit : query.criteria) {
                if (crit instanceof Value) {
                    Value val = (Value) crit;
                    if (!(tvm.hasMetaKey(val.getName()) &&
                            tvm.findMetaValue(val.getName()).equals(val.getValue()))) {
                        valid = false;
                    }
                } else if (crit instanceof Range) {
                    Range range = (Range) crit;
                    if (range.getRangeOption() == RangeType.within) {
                        if (tvm.hasMetaKey(range.getName())) {
                            String value = tvm.findMetaValue(range.getName());
                            //check for int type
                            try {
                                Double.parseDouble(value);
                            } catch (NumberFormatException nfe) {
                                log.log(Level.FINE, "looking for number, but value is not convertible", nfe);
                            }
                        }
                    }
                }
            }
            if(valid) {
                if (result.partition.containsKey(partitionKey)) {
                    ArrayList<TriviumObject> list = result.partition.get(partitionKey);
                    if(list.size()>=query.reduceTo) {
                        list.remove(0);
                    }
                    list.add(tvm);
                } else {
                    ArrayList<TriviumObject> list = new ArrayList<>();
                    list.add(tvm);
                    result.partition.put(partitionKey,list);
                }
            }
        } catch (ScriptException e) {
            //ignore
        }
    }

    public TriviumObject loadObjectById(ObjectRef key) {
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
