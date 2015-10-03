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

package io.trivium.anystore.query;

import io.trivium.anystore.ObjectRef;

import java.util.ArrayList;

public class Query {
    public ObjectRef id = ObjectRef.getInstance();
    public ArrayList<Criteria> criteria = new ArrayList<Criteria>();
    public ResultType resultType = ResultType.ALL;

    public String reducePartitionBy = "id";
    public String reduceOrderBy = "created";
    public String reduceOrderDirection = "descending";
    public long reduceTo = 1;
}
