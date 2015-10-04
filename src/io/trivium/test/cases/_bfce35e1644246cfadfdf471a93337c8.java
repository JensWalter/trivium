package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _bfce35e1644246cfadfdf471a93337c8 implements TestCase{
    @Override
    public String getClassName() {
        return "io.trivium.glue.om.Json";
    }

    @Override
    public String getMethodName() {
        return "elementToJson";
    }
    
    @Override
    public String getTestName() {
        return "simple json test";
    }

    @Override
    public void run() throws Exception {
        String str= "{\"a\":{\"b\":\"blah\"},\"z\":\"h\",\"y\":{\"c\":\"blah2\"}}";
        Element root = Json.jsonToElement(str);
        String s2 = Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("bfce35e1-6442-46cf-adfd-f471a93337c8");
    }
}

