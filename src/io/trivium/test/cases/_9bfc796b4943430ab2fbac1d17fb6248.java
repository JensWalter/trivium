package io.trivium.test.cases;

import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.query.SortOrder;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class _9bfc796b4943430ab2fbac1d17fb6248 implements TestCase{
    @Override
    public String getTestName() {
        return "check descending order on window query";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("node","hallo world");
        ObjectRef typeId = ObjectRef.getInstance();
        AnyServer store = AnyServer.INSTANCE;

        //object 1
        TriviumObject tvm = new TriviumObject();
        ObjectRef object1Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","1");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //object 2
        tvm = new TriviumObject();
        ObjectRef object2Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","3");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //object 3
        tvm = new TriviumObject();
        ObjectRef object3Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","2");
        tvm.setTypeId(typeId);
        store.storeObject(tvm);

        //search for custom meta tag
        Query<TriviumObject> q = new Query<TriviumObject>(){
            {
                condition = (obj) -> obj.getTypeId()==typeId;
                partitionOver = (obj) -> obj.findMetaValue("custom");
                partitionOrderBy = (obj) -> obj.findMetaValue("order");
                partitionSortOrder = SortOrder.DESCENDING;
                partitionReduceTo = 3;
            }
        };
        Result rslt = store.loadObjects(q);
        HashMap<String,ArrayList<TriviumObject>> list = rslt.partition;

        Assert.isTrue(list.keySet().size()==1);

        ArrayList<TriviumObject> all = list.get("element1");

        Assert.isTrue(all.size()==3);

        TriviumObject first = all.get(0);
        TriviumObject second = all.get(1);
        TriviumObject third = all.get(2);

        int erster = Integer.parseInt(first.findMetaValue("order"));
        int zweiter = Integer.parseInt(second.findMetaValue("order"));
        int dritter = Integer.parseInt(third.findMetaValue("order"));

        //delete created objects
        store.deleteById(object1Id);
        store.deleteById(object2Id);
        store.deleteById(object3Id);

        //check test assertion
        Assert.isTrue(erster > zweiter);
        Assert.isTrue(zweiter > dritter);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("9bfc796b-4943-430a-b2fb-ac1d17fb6248");
    }
}

