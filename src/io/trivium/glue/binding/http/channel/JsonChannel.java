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

package io.trivium.glue.binding.http.channel;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;

import java.util.Date;

public class JsonChannel extends Channel {

    public JsonChannel(ObjectRef id) {
        super(id);
    }

    @Override
    public void process(Session session, ObjectRef sourceId) {
        // read payload
        HttpExchange httpexchange = session.getHttpExchange();
        String requestData = HttpUtils.getInputAsString(httpexchange);

        //construct persistence object
        TriviumObject po = new TriviumObject();

        po.addMetadata("contentType", "application/trivium.io");

        // if header starts with trivium - copy value
        Headers headers = session.getHttpExchange().getRequestHeaders();
        for (String headerName : headers.keySet()) {
            if (headerName.startsWith("trivium-")) {
                po.addMetadata(headerName.substring(8), headers.getFirst(headerName));
            }
        }

        //setting channel data
        //ttl -> stale after retention
        po.addMetadata("stale", String.valueOf(new Date().getTime() + config.retention));
        //type = object
        po.addMetadata("type", "object");

        // parse the payload
        Element el = Json.jsonToElement(requestData);


        po.setData(el);
        po.setTypeId(config.getTypeId());

        AnyClient.INSTANCE.storeObject(po);

        session.ok();
    }

}
