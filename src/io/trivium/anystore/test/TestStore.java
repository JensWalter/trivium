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

package io.trivium.anystore.test;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.AnyServer;
import io.trivium.glue.TriviumObject;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.glue.om.Element;

import java.util.ArrayList;

public class TestStore {
	public static void main(String[] args) {
		Central.setProperty("basePath", "/Users/jens/tmp/store");
		
		persist();
	}
	
	
	public static void persist(){
		TriviumObject po = new TriviumObject();
        Element el = new Element("node","hallo world");

		po.setData(el);
		po.addMetadata("id", po.getId().toString());
		po.addMetadata("contentType", "text/plain");

		AnyServer.INSTANCE.getStore().storeObject(po);
		
		NVList filter = new NVList();
		filter.add(new NVPair("id",po.getId().toString()));
        Query q = new Query();
        for(NVPair pair:filter){
            q.criteria.add(new Value(pair.getName(), pair.getValue()));
        }
		ArrayList<TriviumObject> list = AnyServer.INSTANCE.getStore().loadObjects(q);
		
		System.out.println(po.getMetadataJson());
		System.out.println(list.get(0).getMetadataJson());
	}
}
