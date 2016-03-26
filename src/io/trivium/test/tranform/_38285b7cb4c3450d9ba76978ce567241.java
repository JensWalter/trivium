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
import io.trivium.glue.om.Trivium;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _38285b7cb4c3450d9ba76978ce567241 implements TestCase{
    @Override
    public String getTestName() {
        return "element name value pair serialization";
    }

    @Override
    public void run() throws Exception {
        Element el = new Element("test");

        el.addChild(new Element("e1","v1"));
        el.addChild(new Element("e2","v2"));
        Element arr = new Element("arr");
        arr.addValue("v1");
        arr.addValue("v2");
        el.addChild(arr);

        String str = Trivium.elementToTriviumJson(el);

        Element rslt = Trivium.triviumJsonToElement(str);

        Assert.equalsString(el.getName(),rslt.getName());
    }
}

