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

import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.om.Element;

import java.time.Instant;

public class DifferentialFactory implements TypeFactory<Differential> {
    @Override
    public String getName() {
        return "differential type factory";
    }

    @Override
    public TriviumObject getTriviumObject(Differential instance){
        TriviumObject po = new TriviumObject();
        Instant now = Instant.now();
        po.addMetadata("contentType", ContentTypes.getMimeType("trivium"));
        po.addMetadata("type", "object");
        po.addMetadata("created", now.toString());
        po.addMetadata("datapoint", instance.getDatapoint());

        Element el_root = new Element("statisticData");
        Element el_datapoint = new Element("datapoint", instance.getDatapoint());
        Element el_timestamp = new Element("timestamp", now.toString());
        Element el_value = new Element("value", String.valueOf(instance.getValue()));

        el_root.addChild(el_datapoint);
        el_root.addChild(el_timestamp);
        el_root.addChild(el_value);

        po.setData(el_root);
        po.setTypeId(TypeIds.PROFILER_DIFFERENTIAL);
        return po;
    }
}
