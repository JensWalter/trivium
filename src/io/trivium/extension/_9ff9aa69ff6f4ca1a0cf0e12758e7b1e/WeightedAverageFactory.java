package io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e;

import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;

import java.time.Instant;

public class WeightedAverageFactory implements TypeFactory<WeightedAverage> {
    @Override
    public String getName() {
        return "weighter average type factory";
    }

    @Override
    public TriviumObject getTriviumObject(WeightedAverage instance) {
        Instant now = Instant.now();
        TriviumObject po = new TriviumObject();

        po.addMetadata("contentType", ContentTypes.getMimeType("trivium"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", instance.getDatapoint());

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", instance.getDatapoint());
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.format("%.2f", instance.getAverage()));

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);

        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_WEIGHTEDAVERAGE);

        return po;
    }
}
