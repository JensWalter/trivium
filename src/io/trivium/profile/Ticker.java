package io.trivium.profile;

import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Element;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Element;
import javolution.util.FastMap;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Ticker implements Persistable {
    private long interval;
    private String datapoint;
    private FastMap<Long, AtomicLong> values = new FastMap<Long, AtomicLong>().shared();

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

    @Override
    public void persist() {
        long ts = TimeUtils.getTimeFrameStart(interval) - 1;
        long timeframestart = TimeUtils.getTimeFrameStart(interval, ts);
        long timeframeend = TimeUtils.getTimeFrameEnd(interval, ts);
        Instant start = Instant.ofEpochMilli(timeframestart);
        Instant end = Instant.ofEpochMilli(timeframeend);
        AtomicLong value = values.remove(timeframestart);
        if (value != null) {
            long val = value.get();
            InfiniObject po = new InfiniObject();
            po.addMetadata("contentType", ContentTypes.getMimeType("infinup"));
            po.addMetadata("type", "object");
            po.addMetadata("timeFrameStart", start.toString());
            po.addMetadata("timeFrameEnd", end.toString());
            po.addMetadata("created", end.toString());
            po.addMetadata("datapoint", datapoint);

            Element el_root = new Element("statisticData");
            Element el_datapoint = new Element("datapoint", datapoint);
            Element el_timeFrameStart = new Element("timeFrameStart", start.toString());
            Element el_timeFrameEnd = new Element("timeFrameEnd", end.toString());
            Element el_value = new Element("value", String.valueOf(val));

            el_root.addChild(el_datapoint);
            el_root.addChild(el_timeFrameStart);
            el_root.addChild(el_timeFrameEnd);
            el_root.addChild(el_value);

            po.setData(el_root);
            po.setTypeId(TypeIds.PROFILER_TICKER);

            AnyClient.INSTANCE.storeObject(po);
        }
    }
}
