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

package io.trivium.extension.binding.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.TypeRef;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.binding.State;
import io.trivium.extension.fact.Fact;
import io.trivium.extension.task.Task;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;
import io.trivium.Registry;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.MimeTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class RegistryRequestHandler implements HttpHandler {
    Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        logger.log(Level.FINE, "registry handler");

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
                Collection<Binding> bindings = Registry.INSTANCE.getAllBindings();
                NVPair nvbind = new NVPair("binding");
                for (Binding binding : bindings) {
                    nvbind.addValue(binding.getTypeRef().toString());
                    list.add(new NVPair(binding.getTypeRef().toString(), binding.getName()));
                }
                list.add(nvbind);
                ConcurrentHashMap<TypeRef, Class<?extends Fact>> types = Registry.INSTANCE.types;
                NVPair tybind = new NVPair("type");
                for (Class<? extends Fact> clazz : types.values()) {
                    Fact prototype = clazz.newInstance();
                    tybind.addValue(prototype.getTypeRef().toString());
                    list.add(new NVPair(prototype.getTypeRef().toString(), prototype.getFactName()));
                }
                list.add(tybind);
                ConcurrentHashMap<TypeRef, Class<? extends Task>> tasks = Registry.INSTANCE.tasks;
                NVPair tskpair = new NVPair("task");
                for (Class<? extends Task> clazz : tasks.values()) {
                    Task prototype = clazz.newInstance();
                    tskpair.addValue(prototype.getTypeRef().toString());
                    list.add(new NVPair(prototype.getTypeRef().toString(), prototype.getName()));
                }
                list.add(tskpair);

                String json = Json.NVPairsToJson(list);

                s.ok(MimeTypes.getMimeType("json"), json);
            } else if (cmd.equals("listTypes")) {
                Registry.INSTANCE.reload();
                NVList list = new NVList();
                ConcurrentHashMap<TypeRef, Class<?extends Fact>> types = Registry.INSTANCE.types;
                for (Class<? extends Fact> clazz : types.values()) {
                    Fact prototype = clazz.newInstance();
                    list.add(new NVPair(prototype.getTypeRef().toString(), prototype.getFactName()));
                }
                String json = Json.NVPairsToJson(list);
                s.ok(MimeTypes.getMimeType("json"), json);
            } else if (cmd.equals("listTasks")) {
                Registry.INSTANCE.reload();
                NVList list = new NVList();
                ConcurrentHashMap<TypeRef, Class<? extends Task>> tasks = Registry.INSTANCE.tasks;
                for (Class<? extends Task> clazz : tasks.values()) {
                    Task prototype = clazz.newInstance();
                    list.add(new NVPair(prototype.getTypeRef().toString(), prototype.getName()));
                }
                String json = Json.NVPairsToJson(list);
                s.ok(MimeTypes.getMimeType("json"), json);
            } else if (cmd.equals("listBindings")) {
                Registry.INSTANCE.reload();
                NVList list = new NVList();
                Collection<Binding> bindings = Registry.INSTANCE.getAllBindings();
                for (Binding binding : bindings) {
                    list.add(new NVPair(binding.getTypeRef().toString(), binding.getName()));
                }
                String json = Json.NVPairsToJson(list);
                s.ok(MimeTypes.getMimeType("json"), json);
            } else if (cmd.equals("status")) {
                String id = params.findValue("id");
                TypeRef ref = TypeRef.getInstance(id);
                Binding bind = Registry.INSTANCE.getBinding(ref);
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
                TypeRef ref = TypeRef.getInstance(id);
                ArrayList<LogRecord> logs = Registry.INSTANCE.startBinding(ref);
                if(logs.size()==0) {
                    s.ok();
                }else{
                    StringBuilder sb = new StringBuilder();
                    for(LogRecord record: logs) {
                        sb.append(record.getLevel().toString() + " "+record.getLoggerName()
                                + ": " + record.getMessage());
                    }
                    s.ok("text/plain",sb.toString());
                }
            } else if (cmd.equals("stop")) {
                String id = params.findValue("id");
                TypeRef ref = TypeRef.getInstance(id);
                Registry.INSTANCE.getBinding(ref).stopBinding();
                s.ok();
            } else {

                s.ok();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "error while processing registry request", ex);
            s.ok();
        }
    }
}
