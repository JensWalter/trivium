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

package io.trivium.extension._14ee6f6fceec4d209be942b21fcc4732;

import io.trivium.anystore.statics.MimeTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.fact.Fact;
import io.trivium.glue.om.Element;
import io.trivium.profile.TimeUtils;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Ticker implements Fact {

    private long interval;
    private String datapoint;
    private ConcurrentHashMap<Long, AtomicLong> values = new ConcurrentHashMap<>();

    public Ticker(){
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

    public ConcurrentHashMap<Long,AtomicLong> getValues(){
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

    @Override
    public TriviumObject toTriviumObject() {
        long ts = TimeUtils.getTimeFrameStart(this.getInterval()) - 1;
        long timeframestart = TimeUtils.getTimeFrameStart(this.getInterval(), ts);
        long timeframeend = TimeUtils.getTimeFrameEnd(this.getInterval(), ts);
        Instant start = Instant.ofEpochMilli(timeframestart);
        Instant end = Instant.ofEpochMilli(timeframeend);
        AtomicLong value = this.getValues().remove(timeframestart);
        if (value != null) {
            long val = value.get();
            TriviumObject po = new TriviumObject();
            po.addMetadata("contentType", MimeTypes.getMimeType("infinup"));
            po.addMetadata("type", "object");
            po.addMetadata("timeFrameStart", start.toString());
            po.addMetadata("timeFrameEnd", end.toString());
            po.addMetadata("created", end.toString());
            po.addMetadata("datapoint", this.getDatapoint());

            Element el_root = new Element("statisticData");
            Element el_datapoint = new Element("datapoint", this.getDatapoint());
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
