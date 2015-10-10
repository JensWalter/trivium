package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _1957545098e44302a583f1637d108f4d implements TestCase{
    @Override
    public String getTestName() {
        return "json array with object";
    }

    @Override
    public void run() throws Exception {
        String str= "{\"a\":[{\"b\":\"blah\"}],\"z\":{\"x\":\"y\"}}";
        Element root = Json.jsonToElement(str);
        String s2 = Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("19575450-98e4-4302-a583-f1637d108f4d");
    }
}

