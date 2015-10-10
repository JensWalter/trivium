package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Xml;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _9755bc3af75e45b1836acce70e2154ab implements TestCase{
    @Override
    public String getTestName() {
        return "xml with namespace";
    }

    @Override
    public void run() throws Exception {
        //TODO make work
        String str= "<a xmlns=\"urn://ns1\" xmlns:b=\"urn://urn2\"><b>blah</b><b:c>blah2</b:c></a>";
        Element root = Xml.xmlToElement(str);
        Assert.equalsString(str,root.toString());
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("9755bc3a-f75e-45b1-836a-cce70e2154ab");
    }
}

