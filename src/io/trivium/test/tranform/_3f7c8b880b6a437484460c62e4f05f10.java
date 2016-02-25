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
import io.trivium.glue.om.Xml;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _3f7c8b880b6a437484460c62e4f05f10 implements TestCase{
    @Override
    public String getTestName() {
        return "xml with attribute";
    }

    @Override
    public void run() throws Exception {
        String str= "<?xml version=\"1.0\" ?><a attribute=\"value1\"></a>";
        Element root = Xml.xmlToElement(str);
        String str2 = Xml.elementToString(root);
        Assert.equalsString(str,str2);
    }
}

