package io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c;

import io.trivium.extension.type.Type;
import io.trivium.glue.TriviumObject;

import java.util.concurrent.atomic.AtomicLong;

public class Differential implements Type{
    private String datapoint;
    private AtomicLong value = new AtomicLong(0);

    /**
     * should not be used, only invoked by reflection
     */
    private Differential(){
    }

    /**
     * @param datapoint name of the aggregator
     */
    public Differential(String datapoint) {
        this.datapoint = datapoint;

    }

    public String getDatapoint() {
        return datapoint;
    }

    public void increment() {
        value.incrementAndGet();
    }

    public void decrement() {
        value.decrementAndGet();
    }

    public long getValue(){
        return value.longValue();
    }
}
