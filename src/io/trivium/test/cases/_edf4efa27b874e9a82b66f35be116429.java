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

import io.trivium.NVPair;
import io.trivium.anystore.ObjectRef;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _edf4efa27b874e9a82b66f35be116429 implements TestCase{
    @Override
    public Class<?> getTargetClass() {
        return NVPair.class;
    }

    @Override
    public String getTargetMethodName() {
        return "isArray";
    }
    
    @Override
    public String getTestName() {
        return "multiple values for one nvpair";
    }

    @Override
    public void run() throws Exception {
        String key = "key";
        String value = "value1";
        String value2 = "value2";
        NVPair pair = new NVPair(key);
        pair.setValue(value);
        pair.addValue(value2);
        Assert.isTrue(pair.isArray());
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("edf4efa2-7b87-4e9a-82b6-6f35be116429");
    }
}

