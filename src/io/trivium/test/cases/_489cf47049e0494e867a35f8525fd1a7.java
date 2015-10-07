package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _489cf47049e0494e867a35f8525fd1a7 implements TestCase{
    @Override
    public Class<?> getTargetClass() {
        return Json.class;
    }

    @Override
    public String getTargetMethodName() {
        return "elementToJson";
    }
    
    @Override
    public String getTestName() {
        return "test json array";
    }

    @Override
    public void run() throws Exception {
        String str= "{\"a\":[\"b\",\"blah\",\"c\",\"blah2\",true,123]}";
        Element root = Json.jsonToElement(str);
        String s2=Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("489cf470-49e0-494e-867a-35f8525fd1a7");
    }
}

