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

package io.trivium.test.generic;

import io.trivium.anystore.query.Query;
import io.trivium.extension.fact.TriviumObject;
import io.trivium.extension.task.Task;
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

    class Task1 extends Task {
        public TriviumObject input1 = new Query<TriviumObject>(){}.getObject();
        public TriviumObject input2;

        @Override
        public boolean eval() throws Exception {
            return true;
        }

        public Task1(){

        }
    }
}

