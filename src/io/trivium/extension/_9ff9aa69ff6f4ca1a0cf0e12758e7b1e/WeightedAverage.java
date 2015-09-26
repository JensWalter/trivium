/*
 * Copyright 2015 Jens Walter
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

package io.trivium.extension._9ff9aa69ff6f4ca1a0cf0e12758e7b1e;

import io.trivium.anystore.statics.MimeTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.dep.com.google.common.util.concurrent.AtomicDouble;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.type.Type;
import io.trivium.glue.om.Element;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class WeightedAverage implements Type {
    private String datapoint;
    private AtomicDouble avg = new AtomicDouble();
    private AtomicLong count = new AtomicLong(0);

    public WeightedAverage(){}

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

    @Override
    public TriviumObject toTriviumObject() {
        Instant now = Instant.now();
        TriviumObject po = new TriviumObject();

        po.addMetadata("contentType", MimeTypes.getMimeType("trivium"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", this.getDatapoint());

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", this.getDatapoint());
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.format("%.2f", this.getAverage()));
        Element el_rawValue = new Element("RawValue");
        Element el_avg = new Element("average", String.format("%.2f", avg.get()));
        Element el_count = new Element("count", String.valueOf(count.get()));
        el_rawValue.addChild(el_avg);
        el_rawValue.addChild(el_count);

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);
        el_root.addChild(el_rawValue);
        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_WEIGHTEDAVERAGE);

        return po;
    }
}
