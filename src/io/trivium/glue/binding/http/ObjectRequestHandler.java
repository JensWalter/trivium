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

import com.google.common.base.Joiner;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Json;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.anystore.statics.ContentTypes;
import javolution.util.FastList;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectRequestHandler implements HttpHandler {
    private final static Pattern uuidpattern = Pattern
            .compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    Logger log = Logger.getLogger(getClass().getName());
    
    @Override
    public void handle(HttpExchange httpexchange) {

        ObjectRef sourceId = ObjectRef.getInstance();
        Session s = new Session(httpexchange, sourceId);

        try {
            Headers headers = httpexchange.getRequestHeaders();
           // Header[] headers = request.getAllHeaders();
            String uri = httpexchange.getRequestURI().getPath();

            String[] parts = uri.split("/");
            // get method
            String method = parts[2];
            if (method.equals("upsert")) {
                boolean processed = upsert(httpexchange, sourceId, headers, uri);
                if (!processed) {
                    s.error(/*BAD_REQUEST*/400,"request could not be processed");
                    return;
                } else {
                    s.ok();
                }
            } else if (method.equals("query")) {
                // read payload
                NVList criteria = HttpUtils.getInputAsNVList(httpexchange);
                Query q = new Query();
                for (NVPair pair : criteria) {
                    q.criteria.add(new Value(pair.getName(), pair.getValue()));
                }
                FastList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(q);
                FastList<String> sb = new FastList<>();
                for (TriviumObject po : objects) {
                    if (po != null) {
                        sb.add(Json.elementToJson(po.getData()));
                    }
                }
                String str = Joiner.on(",").join(sb);
                String responseText = "[" + str + "]";
                s.ok(ContentTypes.getMimeType("json"), responseText);
                return;
            } else {
                // unknown method
                s.error(/*BAD_REQUEST*/400,
                        "method unknown\nplease use the following pattern\nhttp://{server}:{port}/object/{upsert|search|get|update}/{id}\n");
                return;
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE,"error processing object request", ex);
            s.error(/*INTERNAL_SERVER_ERROR*/500,ex.toString());
            return;
        }
    }

    private boolean upsert(HttpExchange httpexchange, ObjectRef sourceId, Headers headers, String uri) throws IOException {
        boolean processed = false;
        // look for id in uri
        ObjectRef id;
        Matcher matcher = uuidpattern.matcher(uri);
        if (matcher.find()) {
            id = ObjectRef.getInstance(matcher.group());
        } else {
            id = ObjectRef.getInstance();
        }
        // read payload
        byte[] payload = HttpUtils.getInputAsBinary(httpexchange);

        // send object to backend
        TriviumObject po = new TriviumObject();
        po.setId(id);

        String contentType = "application/octet-stream";
        if (headers.containsKey("Content-Type")) {
            contentType = headers.getFirst("Content-Type");
        }
        po.addMetadata("contentType", contentType);

        // if header starts with trivium - copy value
        for (String headerName : headers.keySet()) {
            if (headerName.startsWith("trivium-")) {
                po.addMetadata(headerName.substring(9), headers.getFirst(headerName));
            }
        }

        po.setDataBinary(payload);

        AnyClient.INSTANCE.storeObject(po);
        processed = true;

        return processed;
    }
}
