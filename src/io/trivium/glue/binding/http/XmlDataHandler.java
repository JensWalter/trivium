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

package io.trivium.glue.binding.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Xml;

import java.io.IOException;

public class XmlDataHandler implements HttpHandler{

    private ObjectRef typeId;

    public XmlDataHandler(ObjectRef typeId){
        this.typeId = typeId;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Session session = new Session(httpExchange);
        // read payload
        String requestData = HttpUtils.getInputAsString(httpExchange);

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

        // parse the payload
        Element el = Xml.xmlToElement(requestData);
        po.setData(el);
        po.setTypeId(typeId);
        AnyClient.INSTANCE.storeObject(po);
        session.ok();
    }
}
