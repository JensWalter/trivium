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
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.fact.differential.Differential;
import io.trivium.extension.fact.ticker.Ticker;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;

import java.io.File;
import java.util.logging.Logger;

public class AnyClient {

	public static AnyClient INSTANCE = new AnyClient();
	private Queue pipeIn;
    Logger logger = Logger.getLogger(getClass().getName());

	public AnyClient() {
		String locPipeIn = Central.getProperty("basePath") + File.separator + "queues" + File.separator + "ingestQ";
        StoreUtils.createIfNotExists(locPipeIn);

        pipeIn = Queue.getQueue(locPipeIn);

        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_IN));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
    }

    public synchronized void storeObject(TriviumObject tvm) {
        if(tvm == null){
            return;
        }
        Profiler.INSTANCE.tick(DataPoints.ANYSTORE_QUEUE_IN);
        Profiler.INSTANCE.increment(DataPoints.ANYSTORE_QUEUE_SIZE);

        byte[] msg = tvm.getBinary();
        pipeIn.append(msg);
    }

	public void deleteById(ObjectRef id) {
        AnyServer.INSTANCE.deleteById(id);
	}

	public Result loadObjects(Query query) {
		return AnyServer.INSTANCE.loadObjects(query);
	}
}
