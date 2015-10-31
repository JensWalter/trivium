package io.trivium.test.cases;

import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.query.Value;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;

public class _45c1bb4cba834156a83935d23578f4f8 implements TestCase{

    @Override
    public String getTestName() {
        return "store query with window aggregate";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("node","hallo world");
        ObjectRef typeId = ObjectRef.getInstance("39d3af87-5fca-4066-ae7f-b88cc2ae6dc2");
        AnyServer store = AnyServer.INSTANCE;

        //object 1
        TriviumObject tvm = new TriviumObject();
        ObjectRef object1Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //object 2
        tvm = new TriviumObject();
        ObjectRef object2Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //object 3
        tvm = new TriviumObject();
        ObjectRef object3Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element2");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //search for custom meta tag
        Query q = new Query();
        q.criteria.add(new Value("typeId", typeId.toString()));
        q.reducePartitionBy="custom";


        Result rslt = store.loadObjects(q);
        ArrayList<TriviumObject> list = rslt.getAllAsList();

        //delete created objects
        store.deleteById(object1Id);
        store.deleteById(object2Id);
        store.deleteById(object3Id);

        //check test assertion
        Assert.isTrue(list.size()==2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("45c1bb4c-ba83-4156-a839-35d23578f4f8");
    }
}

