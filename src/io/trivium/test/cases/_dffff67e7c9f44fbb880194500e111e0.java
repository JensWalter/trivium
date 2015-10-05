package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Xml;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _dffff67e7c9f44fbb880194500e111e0 implements TestCase{
    @Override
    public String getClassName() {
        return "io.trivium.glue.om.Xml";
    }

    @Override
    public String getMethodName() {
        return "xmlToElement";
    }
    
    @Override
    public String getTestName() {
        return "simple xml test";
    }

    @Override
    public void run() throws Exception {
        String str= "<a><b>blah</b><c>blah2</c></a>";
        Element root = Xml.xmlToElement(str);
        Assert.equalsString(str,root.toString());
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("dffff67e-7c9f-44fb-b880-194500e111e0");
    }
}

