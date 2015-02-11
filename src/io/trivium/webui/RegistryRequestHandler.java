package io.trivium.webui;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.ObjectType;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.binding.State;
import io.trivium.extension.task.TaskFactory;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;
import io.trivium.reactor.Registry;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.extension.binding.Binding;
import io.trivium.extension.binding.State;
import io.trivium.extension.task.TaskFactory;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Json;
import io.trivium.reactor.Registry;
import javolution.util.FastMap;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class RegistryRequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
        Central.logger.debug("registry handler");

            NVList params = HttpUtils.getInputAsNVList(request);
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
            Session s = new Session(request, httpexchange, context, ObjectRef.getInstance());
        try {
            String cmd = params.findValue("command");
            if (cmd.equals("list")) {
                Registry.INSTANCE.reload();

                NVList list = new NVList();
                FastMap<ObjectType, Binding> bindings = Registry.INSTANCE.bindings;
                NVPair nvbind = new NVPair("binding");
                for (Binding binding : bindings.values()) {
                    nvbind.addValue(binding.getTypeId().toString());
                    list.add(new NVPair(binding.getTypeId().toString(),binding.getName()));
                }
                list.add(nvbind);
                FastMap<ObjectType, TypeFactory> types = Registry.INSTANCE.typeFactory;
                NVPair tybind = new NVPair("type");
                for (TypeFactory t : types.values()) {
                    tybind.addValue(t.getTypeId().toString());
                    list.add(new NVPair(t.getTypeId().toString(),t.getName()));
                }
                list.add(tybind);
                FastMap<ObjectType, TaskFactory> tasks = Registry.INSTANCE.taskFactory;
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
                        case undeployed:
                            list.add(new NVPair("state", "undeployed"));
                            break;
                        case deployed:
                            list.add(new NVPair("state", "deployed"));
                            break;
                        case running:
                            list.add(new NVPair("state", "running"));
                            break;
                    }
                    String json = Json.NVPairsToJson(list);
                    s.ok(ContentTypes.getMimeType("json"), json);
                }else{
                    s.ok();
                }
            } else if (cmd.equals("deploy")) {
                String id = params.findValue("id");
                ObjectRef ref = ObjectRef.getInstance(id);
                Registry.INSTANCE.bindings.get(ref).load();
                s.ok();
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
            Central.logger.error("error while processing registry request",ex);
            s.ok();
        }
    }
}
