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

public class _9755bc3af75e45b1836acce70e2154ab implements TestCase{
    @Override
    public String getTestName() {
        return "xml with namespace";
    }

    @Override
    public void run() throws Exception {
        String str = "<?xml version=\"1.0\" ?><ns0:a xmlns:ns0=\"urn://ns1\"><ns0:b>blah</ns0:b><ns1:c xmlns:ns1=\"urn://urn2\">blah2</ns1:c></ns0:a>";
        Element root = Xml.xmlToElement(str);
        String str2 = Xml.elementToString(root);
        Assert.equalsString(str,str2);
    }
}

