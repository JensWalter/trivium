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

package io.trivium.test.tranform;

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
}

