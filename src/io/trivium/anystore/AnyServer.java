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

import io.trivium.dep.io.qdb.buffer.MessageCursor;
import io.trivium.Central;
import io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732.Ticker;
import io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c.Differential;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;

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
	    log.log(Level.FINE,"starting anystore server");
	    if(store==null) init();
		String locPipeIn = Central.getProperty("basePath") + File.separator + "queues" + File.separator + "queueIn";
		StoreUtils.createIfNotExists(locPipeIn);
        Queue pipeIn = Queue.getQueue(locPipeIn);
		try {
            MessageCursor cursor = pipeIn.getCursor(pipeIn.readPointer);
			while (true) {
				if (cursor.next(1000)) {
                    long readId = cursor.getId();
                    byte[] payload = cursor.getPayload();
                    TriviumObject tvm = new TriviumObject();
                    tvm.setBinary(payload);

                    store.storeObject(tvm);
                    pipeIn.readPointer = readId;

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
	
    public MapStore getStore() {
        if (store == null)
            init();
        return this.store;
    }
}
