#!/bin/bash
id=`uuidgen | tr '[:upper:]' '[:lower:]'`;
name="_`echo $id | tr -d '-'`";
echo "package io.trivium.test.cases;

import io.trivium.anystore.ObjectType;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class $name implements TestCase{
    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getMethodName() {
        return null;
    }
    
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void run() throws Exception {
    }

    @Override
    public ObjectType getTypeId() {
        return ObjectType.getInstance(\"$id\",\"v1\");
    }
}
" > "$name.java"

echo $name;