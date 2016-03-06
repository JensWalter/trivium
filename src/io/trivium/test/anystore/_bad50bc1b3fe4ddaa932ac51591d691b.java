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
import io.trivium.extension.fact.TriviumObject;
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
        TypeRef typeRef1 = TypeRef.getInstance("dummy._bad50bc1b3fe4ddaa932ac51591d691b.type1");
        TypeRef typeRef2 = TypeRef.getInstance("dummy._bad50bc1b3fe4ddaa932ac51591d691b.type2");
        AnyServer store = AnyServer.INSTANCE;

        //object 1
        TriviumObject tvm = new TriviumObject();
        ObjectRef object1Id = tvm.getId();
        tvm.setData(el);
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeRef(typeRef1);
        store.storeObject(tvm);

        //object 2
        tvm = new TriviumObject();
        ObjectRef object2Id = tvm.getId();
        tvm.setData(new Element("node","whatever"));
        tvm.addMetadata("contentType", "text/plain");
        tvm.addMetadata("custom", "element1");
        tvm.setTypeRef(typeRef2);
        store.storeObject(tvm);

        Query<TriviumObject> query1 = new Query<TriviumObject>(){
            {
                targetType = TriviumObject.class;
                condition = (tvm) ->  tvm.findMetaValue("custom").equals("element1")
                            && tvm.getData().getValue().equals("hallo world");
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
}

