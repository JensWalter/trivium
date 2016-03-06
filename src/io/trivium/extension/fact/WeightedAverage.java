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

package io.trivium.extension.fact;

import io.trivium.dep.com.google.common.util.concurrent.AtomicDouble;
import io.trivium.extension.Fact;
import io.trivium.glue.om.Element;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class WeightedAverage implements Fact {
    private String datapoint;
    private AtomicDouble avg = new AtomicDouble();
    private AtomicLong count = new AtomicLong(0);

    public WeightedAverage(){}

    /**
     * @param datapoint name of the aggregator
     */
    public WeightedAverage(String datapoint) {
        this.datapoint = datapoint;

    }

    public String getDatapoint() {
        return datapoint;
    }

    public double getAverage(){
        return avg.get();
    }

    public void avg(double value) {
        long c = count.get();
        avg.set((avg.get() * c + value) / (c + 1));
        count.incrementAndGet();
    }

    @Override
    public TriviumObject toTriviumObject() {
        TriviumObject tvm = Fact.super.toTriviumObject();
        Element el = tvm.getData();
        el.addChild(new Element("timestamp", Instant.now().toString()));
        tvm.setData(el);
        return tvm;
    }
}
