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
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.Registry;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestRequestHandler implements HttpHandler{
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        log.log(Level.FINE,"test request handler");
        NVList test = HttpUtils.getInputAsNVList(httpexchange);
        /**
         var tc = {}
         tc.domainCount=this.state.domainCount;
         tc.deploymentCount=this.state.deploymentCount;
         tc.processCount=this.state.processCount;
         tc.messageCount=this.state.messageCount;
         */
        Session s = new Session(httpexchange, ObjectRef.getInstance());

        //use command structure
        String cmd = test.findValue("command");
        switch(cmd){
            case "list":
                Registry registry = Registry.INSTANCE;
                registry.reload();
                
                break;
            default:
                //nothing happens
                s.ok();
                break;
        }
        final int domainCount = Integer.parseInt(test.findValue("domainCount"));
        final int deploymentCount = Integer.parseInt(test.findValue("deploymentCount"));
        final int processCount = Integer.parseInt(test.findValue("processCount"));
        final int messageCount = Integer.parseInt(test.findValue("messageCount"));

        final int threadCount = 1;

        Runnable r = () -> {
            AnyClient c = AnyClient.INSTANCE;
//TODO implement new load generator
//            NjamsTestDataGenerator gen = new NjamsTestDataGenerator(domainCount, deploymentCount, processCount);
//            long iterCount = messageCount/threadCount;
//            for (long i = 1; i < iterCount; i++) {
//                NjamsTestData n = gen.getNJAMSData();
//                TriviumObject po = new TriviumObject();
//                po.addMetadata("domain", n.domain);
//                po.addMetadata("deployment", n.deployment);
//                po.addMetadata("process", n.process);
//                po.addMetadata("duration", String.valueOf(n.duration));
//                po.addMetadata("jobstart", n.jobstart);
//                po.addMetadata("jobend", n.jobend);
//                po.addMetadata("status", n.status);
//                po.addMetadata("logid", n.logid);
//
//                po.setData(Json.jsonToElement(n.toString()));
//
//                c.storeObject(po);
//            }

        };
        for(int i=0;i<threadCount;i++) {
            Thread td = new Thread(r);
            td.setName("test thread "+td.getId());
            td.start();
        }

        s.ok();
    }
}
