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
        //TODO make work
        String str= "<a xmlns=\"urn://ns1\" xmlns:b=\"urn://urn2\"><b>blah</b><b:c>blah2</b:c></a>";
        Element root = Xml.xmlToElement(str);
        Assert.equalsString(str,root.toString());
    }
}

