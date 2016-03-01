/*
 * Copyright 2016 Jens Walter
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

package io.trivium.test.anystore;

import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.TypeRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Result;
import io.trivium.anystore.query.SortOrder;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.Fact;
import io.trivium.glue.om.Element;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class _9a03753646024475b7c75ab34f76cec2 implements TestCase{
    static TypeRef typeRef = TypeRef.getInstance("dummy._9a03753646024475b7c75ab34f76cec2.type");

    static Query<TriviumObject> query = new Query<TriviumObject>(){
        {
            targetType = TriviumObject.class;
            condition = (tvm) -> tvm.getTypeRef() == typeRef;
            partitionOver = (tvm) -> tvm.findMetaValue("custom");
            partitionOrderBy = (tvm) -> tvm.findMetaValue("order");
            partitionSortOrder = SortOrder.ASCENDING;
            partitionReduceTo = 3;
        }
    };

    @Override
    public String getTestName() {
        return "check ascending order on window query";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("node","hallo world");

        AnyServer store = AnyServer.INSTANCE;

        //object 1
        TriviumObject tvm = new TriviumObject();
        ObjectRef object1Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","1");
        tvm.setTypeRef(typeRef);
        store.storeObject(tvm);

        //object 2
        tvm = new TriviumObject();
        ObjectRef object2Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","3");
        tvm.setTypeRef(typeRef);
        store.storeObject(tvm);

        //object 3
        tvm = new TriviumObject();
        ObjectRef object3Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.addMetadata("order","2");
        tvm.setTypeRef(typeRef);
        store.storeObject(tvm);

        Result rslt = store.loadObjects(query);
        HashMap<String,ArrayList<Fact>> list = rslt.partition;

        Assert.isTrue(list.keySet().size()==1);

        ArrayList<Fact> all = list.get("element1");

        Assert.isTrue(all.size()==3);

        TriviumObject first = (TriviumObject)all.get(0);
        TriviumObject second = (TriviumObject)all.get(1);
        TriviumObject third = (TriviumObject)all.get(2);

        int erster = Integer.parseInt(first.findMetaValue("order"));
        int zweiter = Integer.parseInt(second.findMetaValue("order"));
        int dritter = Integer.parseInt(third.findMetaValue("order"));

        //delete created objects
        store.deleteById(object1Id);
        store.deleteById(object2Id);
        store.deleteById(object3Id);

        //check test assertion
        Assert.isTrue(erster < zweiter);
        Assert.isTrue(zweiter < dritter);
    }
}

