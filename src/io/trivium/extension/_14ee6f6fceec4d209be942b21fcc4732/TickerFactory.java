package io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.profile.TimeUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class TickerFactory implements TypeFactory<Ticker> {
    @Override
    public String getName() {
        return "ticker type factory";
    }

    @Override
    public TriviumObject getTriviumObject(Ticker instance) {
        long ts = TimeUtils.getTimeFrameStart(instance.getInterval()) - 1;
        long timeframestart = TimeUtils.getTimeFrameStart(instance.getInterval(), ts);
        long timeframeend = TimeUtils.getTimeFrameEnd(instance.getInterval(), ts);
        Instant start = Instant.ofEpochMilli(timeframestart);
        Instant end = Instant.ofEpochMilli(timeframeend);
        AtomicLong value = instance.getValues().remove(timeframestart);
        if (value != null) {
            long val = value.get();
            TriviumObject po = new TriviumObject();
            po.addMetadata("contentType", ContentTypes.getMimeType("infinup"));
            po.addMetadata("type", "object");
            po.addMetadata("timeFrameStart", start.toString());
            po.addMetadata("timeFrameEnd", end.toString());
            po.addMetadata("created", end.toString());
            po.addMetadata("datapoint", instance.getDatapoint());

            Element el_root = new Element("statisticData");
            Element el_datapoint = new Element("datapoint", instance.getDatapoint());
            Element el_timeFrameStart = new Element("timeFrameStart", start.toString());
            Element el_timeFrameEnd = new Element("timeFrameEnd", end.toString());
            Element el_value = new Element("value", String.valueOf(val));

            el_root.addChild(el_datapoint);
            el_root.addChild(el_timeFrameStart);
            el_root.addChild(el_timeFrameEnd);
            el_root.addChild(el_value);

            po.setData(el_root);
            po.setTypeId(TypeIds.PROFILER_TICKER);
            return po;
        }
        return null;
    }
}
