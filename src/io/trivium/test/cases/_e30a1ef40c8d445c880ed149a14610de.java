package io.trivium.test.cases;

import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.SortOrder;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;

public class _e30a1ef40c8d445c880ed149a14610de implements TestCase{
    @Override
    public String getTestName() {
        return "simple store query";
    }

    @Override
    public void run() throws Exception {
        TriviumObject tvm = new TriviumObject();
        Element el = new Element("node","hallo world");

        tvm.setData(el);
        tvm.replaceMeta("id", tvm.getId().toString());
        tvm.replaceMeta("contentType", "text/plain");
        ObjectRef typeId = ObjectRef.getInstance("39d3af87-5fca-4066-ae7f-b88bc2ae6dc2");
        tvm.setTypeId(typeId);

        AnyServer.INSTANCE.storeObject(tvm);

        Query<TriviumObject> q = new Query<TriviumObject>(){
            {
                targetType = TriviumObject.class;
                condition = (obj) -> obj.getId()==tvm.getId();
                partitionOver = (obj) -> obj.getTypeId().toString();
                partitionOrderBy = (obj) -> obj.findMetaValue("created");
                partitionSortOrder = SortOrder.DESCENDING;
            }
        };
        ArrayList<TriviumObject> list = AnyServer.INSTANCE.loadObjects(q).getAllAsList();
        String str1 = tvm.getMetadataJson();
        String str2 = list.get(0).getMetadataJson();
        Assert.equalsString(str1,str2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("e30a1ef4-0c8d-445c-880e-d149a14610de");
    }
}

