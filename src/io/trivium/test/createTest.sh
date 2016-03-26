#!/bin/bash
id=`uuidgen | tr '[:upper:]' '[:lower:]'`;
name="_`echo $id | tr -d '-'`";
echo "package io.trivium.test;

import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class $name implements TestCase{
    @Override
    public String getTestName() {
        return null;
    }

    @Override
    public void run() throws Exception {
    }
}
" > "$name.java"

echo $name;