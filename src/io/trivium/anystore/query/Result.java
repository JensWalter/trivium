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

import io.trivium.extension.fact.Fact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Result {
    public HashMap<String, ArrayList<Fact>> partition = new HashMap<>();

    public ArrayList<Fact> getAllAsList(){
        ArrayList<Fact> list = new ArrayList<>();
        for(ArrayList<Fact> objects: partition.values()){
            list.addAll(objects);
        }
        return list;
    }

    public <T> ArrayList<T> getAllAsTypedList(){
        ArrayList<T> list = new ArrayList<>();
        for(ArrayList<Fact> objects: partition.values()){
            list.addAll((Collection<? extends T>) objects);
        }
        return list;
    }
}
