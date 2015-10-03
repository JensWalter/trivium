/*
 * Copyright 2015 Jens Walter
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

package io.trivium.test.cases;

import io.trivium.Central;
import io.trivium.anystore.ObjectRef;
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
    public String getTestName() {
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
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("d716ac2d-8d2c-4200-9e43-0061c3739340");
    }
}

