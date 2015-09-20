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
import io.trivium.extension.task.Task;
import io.trivium.extension.type.Type;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;
import io.trivium.Registry;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.MimeTypes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistryRequestHandler implements HttpHandler {
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        log.log(Level.FINE, "registry handler");

        NVList params = HttpUtils.getInputAsNVList(httpexchange);
        /** list
         {"command" : "list"}
         */
        /**
         {"id" : "...",
         "command" : "start"|"stop"|"status"}
         */
        Session s = new Session(httpexchange);
        try {
            String cmd = params.findValue("command");
            if (cmd.equals("list")) {
                Registry.INSTANCE.reload();

                NVList list = new NVList();
                ConcurrentHashMap<ObjectRef, Class<? extends Binding>> bindings = Registry.INSTANCE.bindings;
                NVPair nvbind = new NVPair("binding");
                for (Class<? extends Binding> bindingClass : bindings.values()) {
                    Binding binding = bindingClass.newInstance();
                    nvbind.addValue(binding.getTypeId().toString());
                    list.add(new NVPair(binding.getTypeId().toString(), binding.getName()));
                }
                list.add(nvbind);
                ConcurrentHashMap<ObjectRef, Class<?extends Type>> types = Registry.INSTANCE.types;
                NVPair tybind = new NVPair("type");
                for (Class<? extends Type> clazz : types.values()) {
                    Type prototype = clazz.newInstance();
                    tybind.addValue(prototype.getTypeId().toString());
                    list.add(new NVPair(prototype.getTypeId().toString(), prototype.getTypeName()));
                }
                list.add(tybind);
                ConcurrentHashMap<ObjectRef, Class<? extends Task>> tasks = Registry.INSTANCE.tasks;
                NVPair tskpair = new NVPair("task");
                for (Class<? extends Task> clazz : tasks.values()) {
                    Task prototype = clazz.newInstance();
                    tskpair.addValue(prototype.getTypeId().toString());
                    list.add(new NVPair(prototype.getTypeId().toString(), prototype.getName()));
                }
                list.add(tskpair);

                String json = Json.NVPairsToJson(list);

                s.ok(MimeTypes.getMimeType("json"), json);
            } else if (cmd.equals("status")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Binding bind = Registry.INSTANCE.bindingInstances.get(ref);
                if (bind != null) {
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
                    s.ok(MimeTypes.getMimeType("json"), json);
                } else {
                    s.ok();
                }
            } else if (cmd.equals("start")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Registry.INSTANCE.bindingInstances.get(ref).start();
                s.ok();
            } else if (cmd.equals("stop")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Registry.INSTANCE.bindingInstances.get(ref).stop();
                s.ok();
            } else {

                s.ok();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error while processing registry request", ex);
            s.ok();
        }
    }
}
