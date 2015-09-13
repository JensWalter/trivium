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

package io.trivium.extension._2a4a0814f16c4f2b8c9ab1f51289b00c;

import io.trivium.extension.type.Type;

import java.util.concurrent.atomic.AtomicLong;

public class Differential implements Type{
    private String datapoint;
    private AtomicLong value = new AtomicLong(0);

    /**
     * should not be used, only invoked by reflection
     */
    private Differential(){
    }

    /**
     * @param datapoint name of the aggregator
     */
    public Differential(String datapoint) {
        this.datapoint = datapoint;

    }

    public String getDatapoint() {
        return datapoint;
    }

    public void increment() {
        value.incrementAndGet();
    }

    public void decrement() {
        value.decrementAndGet();
    }

    public long getValue(){
        return value.longValue();
    }
}
