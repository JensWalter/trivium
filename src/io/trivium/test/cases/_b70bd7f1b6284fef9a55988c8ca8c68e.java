package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _b70bd7f1b6284fef9a55988c8ca8c68e implements TestCase{
    @Override
    public String getTestName() {
        return "json array as top level element";
    }

    @Override
    public void run() throws Exception {
        String str= "[\"a\",\"b\",\"blah\",true,123]";
        Element root = Json.jsonToElement(str);
        String s2 = Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("b70bd7f1-b628-4fef-9a55-988c8ca8c68e");
    }
}

