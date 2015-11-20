package io.trivium.test.cases;

import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;

public class _bad50bc1b3fe4ddaa932ac51591d691b implements TestCase{
    @Override
    public String getTestName() {
        return "query with two connected objects";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("node","hallo world");
        ObjectRef typeId1 = ObjectRef.getInstance();
        ObjectRef typeId2 = ObjectRef.getInstance();
        AnyServer store = AnyServer.INSTANCE;

        //object 1
        TriviumObject tvm = new TriviumObject();
        ObjectRef object1Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeId(typeId1);
        store.storeObject(tvm);

        //object 2
        tvm = new TriviumObject();
        ObjectRef object2Id = tvm.getId();
        tvm.setData(new Element("node","whatever"));
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeId(typeId2);
        store.storeObject(tvm);

        Query<TriviumObject> query1 = new Query<TriviumObject>(){
            {
                targetType = TriviumObject.class;
                condition = (tvm) ->  tvm.findMetaValue("custom").equals("element1")
                            && tvm.getData().getChild(0).getValue().equals("hallo world");
            }
        };
        ArrayList<TriviumObject> list1 = store.loadObjects(query1).getAllAsTypedList();
        Assert.isTrue(list1.size()==1);

        TriviumObject result1 = list1.get(0);
        Query<TriviumObject> query2 = new Query<TriviumObject>(){
            {
                targetType = TriviumObject.class;
                condition = (tvm) -> tvm.findMetaValue("custom").equals("element1")
                        && tvm.findMetaValue("custom").equals(tvm.findMetaValue("custom"))
                        && tvm.getId()!=result1.getId();
            }
        };
        ArrayList<TriviumObject> list2 = store.loadObjects(query2).getAllAsTypedList();
        Assert.isTrue(list2.size()==1);
        Assert.isTrue(list1.get(0).getId()!=list2.get(0).getId());

        store.deleteById(object1Id);
        store.deleteById(object2Id);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("bad50bc1-b3fe-4dda-a932-ac51591d691b");
    }
}

