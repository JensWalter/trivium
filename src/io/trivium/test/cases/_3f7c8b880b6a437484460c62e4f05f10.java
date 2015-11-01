package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Xml;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _3f7c8b880b6a437484460c62e4f05f10 implements TestCase{
    @Override
    public String getTestName() {
        return "xml with attribute";
    }

    @Override
    public void run() throws Exception {
        String str= "<?xml version=\"1.0\" ?><a attribute=\"value1\"></a>";
        Element root = Xml.xmlToElement(str);
        String str2 = Xml.elementToString(root);
        Assert.equalsString(str,str2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("3f7c8b88-0b6a-4374-8446-0c62e4f05f10");
    }
}

