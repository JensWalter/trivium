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

package io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c;

import io.trivium.anystore.statics.MimeTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.fact.Fact;
import io.trivium.glue.om.Element;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Differential implements Fact{
    private String datapoint;
    private AtomicLong value = new AtomicLong(0);

    public Differential(){
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

    @Override
    public TriviumObject toTriviumObject(){
        TriviumObject po = new TriviumObject();
        Instant now = Instant.now();
        po.addMetadata("contentType", MimeTypes.getMimeType("trivium"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", this.getDatapoint());

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", this.getDatapoint());
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.valueOf(this.getValue()));

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);

        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_DIFFERENTIAL);
        return po;
    }
}
