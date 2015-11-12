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

import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.task.Task;
import io.trivium.extension.type.Query2;

class Task1 extends Task{
    public TriviumObject input1 = new Query2<TriviumObject>(){}.getObject();
    public TriviumObject input2;

    @Override
    public boolean eval() throws Exception {
        return true;
    }

    public Task1(){

    }
}