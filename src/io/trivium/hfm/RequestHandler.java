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

package io.trivium.hfm;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.binding.http.Session;

import java.util.ArrayList;

public class RequestHandler implements HttpHandler{

	@Override
	public void handle(HttpExchange httpexchange) {

		//find hfm related headers
		Headers headers = httpexchange.getRequestHeaders();
		String contentType = "";
		NVList list = new NVList();
		for(String headerName : headers.keySet()){
			if(headerName.startsWith("hfm:")){
				list.add(new NVPair(headerName.substring(3), headers.getFirst(headerName)));
			}
			if(headerName.equalsIgnoreCase("Content-Type")){
				contentType=headers.getFirst(headerName);
			}
		}
		//switch request method
		String method = httpexchange.getRequestMethod();
		if(method.equals("GET")){
			//read from store
            Query q = new Query();
            for(NVPair pair:list){
                q.criteria.add(new Value(pair.getName(), pair.getValue()));
            }
			ArrayList<TriviumObject> all = AnyServer.INSTANCE.getStore().loadObjects(q);
			if(all.size()>0){
				TriviumObject po = all.get(0);
				String type = po.findMetaValue("contentType");
			}
		}
		if(method.equals("POST") || method.equals("PUT")){
			//write to store
			String uri =httpexchange.getRequestURI().getPath();
		}
		
		 new Session(httpexchange, ObjectRef.getInstance()).ok();
	}
}
