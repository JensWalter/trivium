package io.trivium.test.cases;

import io.trivium.Central;
import io.trivium.anystore.ObjectType;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _d716ac2d8d2c42009e430061c3739340 implements TestCase{
    @Override
    public String getClassName() {
        return "io.trivium.Central";
    }

    @Override
    public String getMethodName() {
        return "getProperty";
    }
    
    @Override
    public String getName() {
        return "Central set property";
    }

    @Override
    public void run() throws Exception {
        String value1 = "value";
        String key1 = "key";
        Central.setProperty(key1,value1);
        String value2 = Central.getProperty(key1);
        Assert.equalsString(value1,value2);
    }

    @Override
    public ObjectType getTypeId() {
        return ObjectType.getInstance("d716ac2d-8d2c-4200-9e43-0061c3739340","v1");
    }
}

