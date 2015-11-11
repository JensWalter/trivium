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

public class Query <T extends Fact>{
    public boolean condition(T item) {
        return true;
    }

    public String context(T item) {
        return "";
    }

    public String partitionOver(T item) {
        return "id";
    }

    public SortOrder partitionSortOrder() {
        return SortOrder.DESCENDING;
    }

    public String partitionOrderBy(T item) {
        return "created";
    }

    public long partitionReduceTo() {
        return 1;
    }

    public <Q extends Fact> boolean connect(T left, Q right) {
        return false;
    }

    public T getObject() {
        return (T) new TriviumObject();
    }

    public T[] getObjects() {
        return (T[]) new TriviumObject[1];
    }

}
