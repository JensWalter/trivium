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

package io.trivium.extension;

import io.trivium.anystore.AnyClient;
import io.trivium.extension.fact.TriviumObject;

import java.util.logging.Logger;

public abstract class Binding implements Typed {
    protected Logger logger = Logger.getLogger(getClass().getName());
    private BindingState state = BindingState.stopped;

    public void startBinding(){
        start();
        setState(BindingState.running);
    }

    protected abstract void start();

    public void stopBinding(){
        stop();
        setState(BindingState.stopped);
    }

    protected abstract void stop();

    public void check() throws Exception{}

    public BindingState getState(){
        return state;
    }

    protected void setState(BindingState newState){
        state=newState;
    }

    public String getName(){
        return this.getClass().getSimpleName();
    }

    protected void emit(Fact object){
        TriviumObject obj = object.toTriviumObject();
        AnyClient.INSTANCE.storeObject(obj);
    }
}
