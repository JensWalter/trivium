package io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e;

import com.google.common.util.concurrent.AtomicDouble;
import io.trivium.extension.type.Type;

import java.util.concurrent.atomic.AtomicLong;

public class WeightedAverage implements Type {
    private String datapoint;
    private AtomicDouble avg = new AtomicDouble();
    private AtomicLong count = new AtomicLong(0);

    /**
     * should not be used, only invoked by reflection
     */
    private WeightedAverage(){

    }

    /**
     * @param datapoint name of the aggregator
     */
    public WeightedAverage(String datapoint) {
        this.datapoint = datapoint;

    }

    public String getDatapoint() {
        return datapoint;
    }

    public double getAverage(){
        return avg.get();
    }

    public void avg(double value) {
        long c = count.get();
        avg.set((avg.get() * c + value) / (c + 1));
        count.incrementAndGet();
    }

}
