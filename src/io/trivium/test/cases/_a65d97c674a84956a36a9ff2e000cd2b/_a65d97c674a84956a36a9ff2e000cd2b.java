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

package io.trivium.test.cases._a65d97c674a84956a36a9ff2e000cd2b;

import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

import java.util.HashMap;

public class _a65d97c674a84956a36a9ff2e000cd2b implements TestCase{

    @Override
    public String getTestName() {
        return "get input of task";
    }

    @Override
    public void run() throws Exception {
        Task1 t = new Task1();
        HashMap<String,Query> queries = t.getInputQueries();

        Assert.isTrue(queries.size()==1);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("a65d97c6-74a8-4956-a36a-9ff2e000cd2b");
    }
}

