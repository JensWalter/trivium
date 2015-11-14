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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.statics.MimeTypes;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonDataHandler implements HttpHandler{

    private ObjectRef typeId;

    public JsonDataHandler(ObjectRef typeId){
        this.typeId = typeId;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Session session = new Session(httpExchange);
        //only support get parameter for now
        String queryString = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> params = new HashMap<>();
        for (String param : queryString.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                params.put(pair[0], pair[1]);
            }else{
                params.put(pair[0], "");
            }
        }

        Result rslt = AnyClient.INSTANCE.loadObjects(new Query<TriviumObject>(){
            {
                condition = (tvm) -> tvm.getTypeId()==typeId;
            }
        });

        ArrayList<TriviumObject> all = rslt.getAllAsList();

        if(all.size()==0){
            //nothing found
            session.ok();
        }
        if(all.size()==1){
            //one object found
            session.ok(MimeTypes.getMimeType("json"),Json.elementToJson(all.get(0).getData()));
        }
        if(all.size()>1){
            //multiple objects found
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(TriviumObject tvm : all){
                sb.append(Json.elementToJson(tvm.getData()));
            }
            sb.append("]");
            session.ok(MimeTypes.getMimeType("json"),sb.toString());
        }
    }
}
