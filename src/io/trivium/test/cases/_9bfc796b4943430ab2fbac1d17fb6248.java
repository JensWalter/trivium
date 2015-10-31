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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class _9bfc796b4943430ab2fbac1d17fb6248 implements TestCase{
    @Override
    public String getTestName() {
        return "check order on window query";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("node","hallo world");
        ObjectRef typeId = ObjectRef.getInstance("39d3af87-5fca-4066-af7f-b88cc2ae6dc2");
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

        //search for custom meta tag
        Query q = new Query();
        q.criteria.add(new Value("typeId", typeId.toString()));
        q.reducePartitionBy="custom";
        q.reduceOrderBy="created";
        q.reduceOrderDirection="descending";
        q.reduceTo=2;


        Result rslt = store.loadObjects(q);
        HashMap<String,ArrayList<TriviumObject>> list = rslt.partition;

        Assert.isTrue(list.keySet().size()==1);

        ArrayList<TriviumObject> all = list.get("element1");

        TriviumObject first = all.get(0);
        TriviumObject second = all.get(1);

        Instant erster = Instant.parse(first.findMetaValue("created"));
        Instant zweiter = Instant.parse(second.findMetaValue("created"));

        //delete created objects
        store.deleteById(object1Id);
        store.deleteById(object2Id);

        //check test assertion
        Assert.isTrue(erster.isAfter(zweiter));
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("9bfc796b-4943-430a-b2fb-ac1d17fb6248");
    }
}

