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

package io.trivium.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.binding.State;
import io.trivium.extension.task.TaskFactory;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;
import io.trivium.Registry;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.ContentTypes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistryRequestHandler implements HttpHandler {
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        log.log(Level.FINE,"registry handler");

            NVList params = HttpUtils.getInputAsNVList(httpexchange);
            /** list
             {
             "command" : "list"
             }
             */
            /**
             {
             "id" : "...",
             "command" : "start"|"stop"|"status"
             }
             */
            Session s = new Session(httpexchange, ObjectRef.getInstance());
        try {
            String cmd = params.findValue("command");
            if (cmd.equals("list")) {
                Registry.INSTANCE.reload();

                NVList list = new NVList();
                ConcurrentHashMap<ObjectRef, Binding> bindings = Registry.INSTANCE.bindings;
                NVPair nvbind = new NVPair("binding");
                for (Binding binding : bindings.values()) {
                    nvbind.addValue(binding.getTypeId().toString());
                    list.add(new NVPair(binding.getTypeId().toString(),binding.getName()));
                }
                list.add(nvbind);
                ConcurrentHashMap<ObjectRef, TypeFactory> types = Registry.INSTANCE.typeFactory;
                NVPair tybind = new NVPair("type");
                for (TypeFactory t : types.values()) {
                    tybind.addValue(t.getTypeId().toString());
                    list.add(new NVPair(t.getTypeId().toString(),t.getName()));
                }
                list.add(tybind);
                ConcurrentHashMap<ObjectRef, TaskFactory> tasks = Registry.INSTANCE.taskFactory;
                NVPair tskpair = new NVPair("task");
                for (TaskFactory f : tasks.values()) {
                    tskpair.addValue(f.getTypeId().toString());
                    list.add(new NVPair(f.getTypeId().toString(),f.getName()));
                }
                list.add(tskpair);

                String json = Json.NVPairsToJson(list);

                s.ok(ContentTypes.getMimeType("json"), json);
            } else if (cmd.equals("status")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Binding bind =Registry.INSTANCE.bindings.get(ref);
                if(bind!=null) {
                    State state = bind.getState();
                    NVList list = new NVList();
                    switch (state) {
                        case stopped:
                            list.add(new NVPair("state", "stopped"));
                            break;
                        case running:
                            list.add(new NVPair("state", "running"));
                            break;
                    }
                    String json = Json.NVPairsToJson(list);
                    s.ok(ContentTypes.getMimeType("json"), json);
                }else {
                    s.ok();
                }
            } else if (cmd.equals("start")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Registry.INSTANCE.bindings.get(ref).start();
                s.ok();
            } else if (cmd.equals("stop")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Registry.INSTANCE.bindings.get(ref).stop();
                s.ok();
            } else {

                s.ok();
            }
        }catch(Exception ex){
            log.log(Level.SEVERE,"error while processing registry request",ex);
            s.ok();
        }
    }
}
