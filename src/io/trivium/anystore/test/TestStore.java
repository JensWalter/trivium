package io.trivium.anystore.test;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.AnyServer;
import io.trivium.glue.TriviumObject;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.glue.om.Element;
import javolution.util.FastList;

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
		FastList<TriviumObject> list = AnyServer.INSTANCE.getStore().loadObjects(q);
		
		System.out.println(po.getMetadataJson());
		System.out.println(list.get(0).getMetadataJson());
	}
}
