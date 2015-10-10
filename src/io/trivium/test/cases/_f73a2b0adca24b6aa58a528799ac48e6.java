package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _f73a2b0adca24b6aa58a528799ac48e6 implements TestCase{
    @Override
    public String getTestName() {
        return "json array in array";
    }

    @Override
    public void run() throws Exception {
        String str= "{\"a\":[[\"b\"]]}";
        Element root = Json.jsonToElement(str);
        String s2 = Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("f73a2b0a-dca2-4b6a-a58a-528799ac48e6");
    }
}

