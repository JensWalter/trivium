/*
 * Copyright 2016 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium.test.fact;

import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.extension.fact.File;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.time.Instant;

public class _ecb2881f0ddb4e12a6079c1094882ff3 implements TestCase{
    @Override
    public String getTestName() {
        return "file fact serialisation";
    }

    @Override
    public void run() throws Exception {
        File testFile = new File();
        testFile.contentType="text/plain";
        testFile.data="abc123";
        testFile.lastModified= Instant.now();
        testFile.size=6;
        testFile.name="testFile1.txt";
        testFile.metadata=new NVList();
        testFile.metadata.add(new NVPair("originalName","textFile"));

        TriviumObject tvm = testFile.toTriviumObject();

        File testFile2 = tvm.getTypedData();

        Assert.equalsString(testFile.name,testFile2.name);
        Assert.equalsString(testFile.contentType,testFile2.contentType);
        Assert.equalsString(testFile.lastModified.toString(),testFile2.lastModified.toString());
        Assert.equalsString(testFile.data,testFile2.data);
        Assert.equalsString(String.valueOf(testFile.size),String.valueOf(testFile2.size));
       // Assert.equalsString(testFile.metadata.toString(),testFile2.metadata.toString());
    }
}

