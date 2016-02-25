/*
 * Copyright 2016 Jens Walter
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

package io.trivium.profile;

import io.trivium.anystore.AnyClient;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.fact.differential.Differential;
import io.trivium.extension.fact.ticker.Ticker;
import io.trivium.extension.fact.weightedaverage.WeightedAverage;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Profiler extends TimerTask{
    public static Profiler INSTANCE = new Profiler();
    private ConcurrentHashMap<String,Ticker> tickCollector = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Differential> diffCollector = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,WeightedAverage> avgCollector = new ConcurrentHashMap<>();

    public void initTicker(Ticker t){
        if(!tickCollector.containsKey(t.getDatapoint())) {
            tickCollector.put(t.getDatapoint(), t);
        }
    }

    public void initDifferential(Differential d) {
        if (!diffCollector.containsKey(d.getDatapoint())) {
            diffCollector.put(d.getDatapoint(), d);
        }
    }

    public void initAverage(WeightedAverage a) {
        if (!avgCollector.containsKey(a.getDatapoint())) {
            avgCollector.put(a.getDatapoint(), a);
        }
    }

    public void tick(String datapoint){
        Ticker stat = tickCollector.get(datapoint);
        if(stat!=null) {
            stat.tick();
        }
    }

    public void avg(String datapoint,double value){
        WeightedAverage stat = avgCollector.get(datapoint);
        if(stat!=null) {
            stat.avg(value);
        }
    }

    public void increment(String datapoint){
        Differential stat = diffCollector.get(datapoint);
        if(stat!=null) {
            stat.increment();
        }
    }

    public void decrement(String datapoint){
        Differential stat = diffCollector.get(datapoint);
        stat.decrement();
    }

    @Override
    public void run() {
        AnyClient client = AnyClient.INSTANCE;
        for (Ticker t : tickCollector.values()) {
            TriviumObject tvm = TriviumObject.getTriviumObject(t);
            client.storeObject(tvm);
        }
        for (Differential d : diffCollector.values()) {
            TriviumObject tvm = TriviumObject.getTriviumObject(d);
            client.storeObject(tvm);
        }
        for (WeightedAverage a: avgCollector.values()) {
            TriviumObject tvm = TriviumObject.getTriviumObject(a);
            client.storeObject(tvm);
        }
    }
}
