package io.trivium.profile;

import io.trivium.glue.InfiniObject;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.glue.om.Element;
import io.trivium.glue.InfiniObject;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Differential implements Persistable {
    private String datapoint;
    private AtomicLong value = new AtomicLong(0);

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

    @Override
    public void persist() {
        Instant now = Instant.now();
        InfiniObject po = new InfiniObject();

        po.addMetadata("contentType", ContentTypes.getMimeType("infiniup"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", datapoint);

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", datapoint);
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.valueOf(value.get()));

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);

        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_DIFFERENTIAL);

        AnyClient.INSTANCE.storeObject(po);
    }
}
