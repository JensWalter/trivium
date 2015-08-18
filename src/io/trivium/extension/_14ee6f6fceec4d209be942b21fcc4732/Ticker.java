package io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732;

import io.trivium.extension.type.Type;
import io.trivium.profile.TimeUtils;
import javolution.util.FastMap;

import java.util.concurrent.atomic.AtomicLong;

public class Ticker implements Type {

    private long interval;
    private String datapoint;
    private FastMap<Long, AtomicLong> values = new FastMap<Long, AtomicLong>().shared();

    /**
     * should not be used, only invoked by reflection
     */
    private Ticker(){

    }

    /**
     * @param interval  aggregation interval
     * @param datapoint name of the aggregator
     */
    public Ticker(long interval, String datapoint) {
        this.datapoint = datapoint;
        this.interval = interval;

    }
    public Ticker(String datapoint) {
        this.datapoint = datapoint;
        this.interval = 60000;
    }

    public String getDatapoint() {
        return datapoint;
    }

    public long getInterval(){
        return interval;
    }

    public FastMap<Long,AtomicLong> getValues(){
        return values;
    }

    public void tick() {
        long timeframestart = TimeUtils.getTimeFrameStart(interval);
        AtomicLong val = values.get(timeframestart);
        if (val != null) {
            val.incrementAndGet();
        } else {
            val = new AtomicLong(1);
            values.put(timeframestart, val);
        }
    }
}
