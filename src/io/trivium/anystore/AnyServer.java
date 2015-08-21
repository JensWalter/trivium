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
import io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732.Ticker;
import io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c.Differential;
import io.trivium.glue.TriviumObject;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnyServer implements Runnable {

	public static AnyServer INSTANCE = new AnyServer();
    Logger log = Logger.getLogger(getClass().getName());
	private MapStore store = null;
	
	public void init(){
        store = new MapStore();

        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_OUT));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_SIZE));
	}

	@Override
	public void run() {
	    log.info("starting anystore server");
	    if(store==null) init();
		String pipeIn = Central.getProperty("basePath")+"queues"+File.separator+"anystore"+File.separator+"queueIn";
		StoreUtils.createIfNotExists(pipeIn);
		IndexedChronicle chronicle=null;
		try {
			chronicle = new IndexedChronicle(pipeIn);
		}catch(Exception ex){
			log.log(Level.SEVERE,"cannot init anystore chronicle", ex);
		}
		try {
			ExcerptTailer et = chronicle.createTailer();
			while (true) {
				if (et.nextIndex()) {
                    TriviumObject po = new TriviumObject();
                    byte[] id = new byte[16];
					et.read(id);
					byte[] typeId = new byte[16];
					et.read(typeId);
					int metaSize = et.readInt();
                    byte[] meta = new byte[metaSize];
                    et.read(meta);
                    int dataSize = et.readInt();
                    byte[] data = new byte[dataSize];
                    et.read(data);
                    po.setId(ObjectRef.getInstance(id));
                    po.setTypeId(ObjectRef.getInstance(typeId));
                    po.setMetadataBinary(meta);
                    po.setDataBinary(data);

                    store.storeObject(po);
					et.finish();
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
			log.log(Level.SEVERE,"error while writing to backend",e1);
		} finally{
			try {
				chronicle.close();
			}catch(Exception ex){
				//ignore
			}
		}
	}
	
    public MapStore getStore() {
        if (store == null)
            init();
        return this.store;
    }
}
