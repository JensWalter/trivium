package io.trivium.test.cases;

import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.MapStore;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class _e30a1ef40c8d445c880ed149a14610de implements TestCase{
    @Override
    public Class<?> getTargetClass() {
        return MapStore.class;
    }

    @Override
    public String getTargetMethodName() {
        return "loadObjects";
    }
    
    @Override
    public String getTestName() {
        return "simple store query";
    }

    @Override
    public void run() throws Exception {
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
        String str1 = tvm.getMetadataJson();
        String str2 = list.get(0).getMetadataJson();
        Assert.equalsString(str1,str2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("e30a1ef4-0c8d-445c-880e-d149a14610de");
    }
}

