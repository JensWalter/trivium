package io.trivium.profile;

import io.trivium.anystore.AnyClient;
import io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c.Differential;
import io.trivium.glue.TriviumObject;
import javolution.util.FastMap;

import java.util.TimerTask;

public class Profiler extends TimerTask{
    public static Profiler INSTANCE = new Profiler();
    private FastMap<String,Ticker> tickCollector = new FastMap<String,Ticker>().shared();
    private FastMap<String,Differential> diffCollector = new FastMap<String,Differential>().shared();
    private FastMap<String,WeightedAverage> avgCollector = new FastMap<String,WeightedAverage>().shared();

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
        for (Ticker t : tickCollector.values()) {
            t.persist();
        }
        for (Differential d : diffCollector.values()) {
            TriviumObject tvm = TriviumObject.getTriviumObject(d);
            AnyClient.INSTANCE.storeObject(tvm);
        }
        for (WeightedAverage a: avgCollector.values()) {
            a.persist();
        }
    }
}
