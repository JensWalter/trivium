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
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;

import java.util.ArrayList;
import java.util.HashMap;

public class TestStore {
	public static void main(String[] args) throws InterruptedException {
		Central.setProperty("basePath", "/Users/jens/tmp/store");
		Central.start();
        Thread.sleep(1000);
		test();
	}
	
	
	public static void test(){
		TriviumObject tvm = new TriviumObject();
        Element el = new Element("node","hallo world");

		tvm.setData(el);
		tvm.addMetadata("id", tvm.getId().toString());
		tvm.addMetadata("contentType", "text/plain");
        ObjectRef typeId = ObjectRef.getInstance("39d3af87-5fca-4066-ae7f-b88bc2ae6dc2");
        tvm.setTypeId(typeId);

		AnyServer.INSTANCE.getStore().storeObject(tvm);
		
		NVList filter = new NVList();
		filter.add(new NVPair("id",tvm.getId().toString()));
        Query q = new Query();
        for(NVPair pair:filter){
            q.criteria.add(new Value(pair.getName(), pair.getValue()));
        }
        q.reducePartitionBy="typeId";
        q.reduceOrderBy="created";
        q.reduceOrderDirection="desc";
        HashMap<String,ArrayList<TriviumObject>> all = AnyServer.INSTANCE.getStore().loadObjects(q).partition;
        ArrayList<TriviumObject> list = new ArrayList<>();
        for(ArrayList<TriviumObject> objects: all.values()){
            list.addAll(objects);
        }
		System.out.println(tvm.getMetadataJson());
		System.out.println(list.get(0).getMetadataJson());

        //do partition query
        q = new Query();
        q.criteria.add(new Value("typeId", typeId.toString()));
        all = AnyServer.INSTANCE.getStore().loadObjects(q).partition;
        list = new ArrayList<>();
        for(ArrayList<TriviumObject> objects: all.values()){
            list.addAll(objects);
        }

        System.out.println("query results : "+list.size());
        System.out.println(list.get(0).getMetadataJson());

        System.exit(0);
	}
}
