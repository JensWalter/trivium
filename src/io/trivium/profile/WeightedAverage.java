package io.trivium.profile;

import com.google.common.util.concurrent.AtomicDouble;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class WeightedAverage implements Persistable {
    private String datapoint;
    private AtomicDouble avg = new AtomicDouble();
    private AtomicLong count = new AtomicLong(0);

    /**
     * @param datapoint name of the aggregator
     */
    public WeightedAverage(String datapoint) {
        this.datapoint = datapoint;

    }

    public String getDatapoint() {
        return datapoint;
    }

    public void avg(double value) {
        long c = count.get();
        avg.set((avg.get() * c + value) / (c + 1));
        count.incrementAndGet();
    }

    @Override
    public void persist() {
        Instant now = Instant.now();
        TriviumObject po = new TriviumObject();

        po.addMetadata("contentType", ContentTypes.getMimeType("trivium"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", datapoint);

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", datapoint);
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.format("%.2f", avg.get()));

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);

        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_WEIGHTEDAVERAGE);

        AnyClient.INSTANCE.storeObject(po);
    }
}
