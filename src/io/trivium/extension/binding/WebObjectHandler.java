/*
 * Copyright 2016 Jens Walter
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

package io.trivium.extension.binding;

import io.trivium.dep.com.google.common.base.Joiner;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.statics.MimeTypes;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.Binding;
import io.trivium.extension.Fact;
import io.trivium.glue.Http;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebObjectHandler extends Binding implements HttpHandler {
    private final static Pattern uuidpattern = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    @Override
    protected void start() {
        //TODO default port
        Http.registerListener("/object/",this);
    }

    @Override
    protected void stop() {
        Http.unregisterListener(this);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Session s = new Session(httpExchange);
        ObjectRef sourceId = s.getId();

        try {
            Headers headers = httpExchange.getRequestHeaders();
            String uri = httpExchange.getRequestURI().getPath();

            String[] parts = uri.split("/");
            // get method
            String method = parts[2];
            if (method.equals("upsert")) {
                boolean processed = upsert(httpExchange, sourceId, headers, uri);
                if (!processed) {
                    s.error(/*BAD_REQUEST*/400,"request could not be processed");
                    return;
                } else {
                    s.ok();
                }
            } else if (method.equals("query")) {
                // read payload
                NVList criteria = HttpUtils.getInputAsNVList(httpExchange);
                Query q = new Query<TriviumObject>(){
                    {
                        targetType = TriviumObject.class;
                        condition = (tvm) -> {
                            boolean result = true;
                            for (NVPair pair : criteria) {
                                result &= tvm.findMetaValue(pair.getName()).equals(pair.getValue());
                            }
                            return result;
                        };
                    }
                };
                ArrayList<String> sb = new ArrayList<>();
                ArrayList<Fact> objects = AnyClient.INSTANCE.loadObjects(q).getAllAsList();
                for (Fact fact : objects) {
                    TriviumObject tvm = (TriviumObject) fact;
                    if (tvm != null) {
                        sb.add(Json.elementToJson(tvm.getData()));
                    }
                }
                String str = Joiner.on(",").join(sb);
                String responseText = "[" + str + "]";
                s.ok(MimeTypes.getMimeType("json"), responseText);
                return;
            } else if (method.equals("source")) {
                // read payload
                NVList criteria = HttpUtils.getInputAsNVList(httpExchange);
                //ask classloader for source code
                String type = criteria.findValue("typeId");
                InputStream is1 = ClassLoader.getSystemClassLoader().getResourceAsStream(type.replace('.','/'));
                InputStream is2 = this.getClass().getResourceAsStream(type.replace('.','/'));
                String responseText = "source found";
                s.ok(MimeTypes.getMimeType("json"), responseText);
                return;
            } else{
                // unknown method
                s.error(/*BAD_REQUEST*/400,
                        "method unknown\nplease use the following pattern\nhttp://{server}:{port}/object/{upsert|query}/{id}\n");
                return;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"error processing object request", ex);
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
