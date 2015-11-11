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

package io.trivium.extension.type;

import io.trivium.anystore.query.SortOrder;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.fact.Fact;

import java.util.ArrayList;

public class Query2<T extends Fact>{
    public BooleanClosure<T> condition;

    public StringClosure<T> context;

    public StringClosure<T> partitionOver;

    public SortOrder partitionSortOrder =  SortOrder.DESCENDING;

    public StringClosure<T> partitionOrderBy;

    public long partitionReduceTo = 1;

    public ConnectClosure<T,? extends Fact> connect;

    /**
     * dummy return method to ensure compile safety
     * @return always null
     */
    public T getObject() {
        return null;
    }

    /**
     * dummy return method to ensure compile safety
     * @return always null
     */
    public T[] getObjects() {
        return null;
    }

}
