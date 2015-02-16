package io.trivium.webui;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class TestRequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
        Central.logger.debug("test request handler");
        NVList test = HttpUtils.getInputAsNVList(request);
        /**
         var tc = {}
         tc.domainCount=this.state.domainCount;
         tc.deploymentCount=this.state.deploymentCount;
         tc.processCount=this.state.processCount;
         tc.messageCount=this.state.messageCount;
         */
        Session s = new Session(request, httpexchange, context, ObjectRef.getInstance());

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
//                InfiniObject po = new InfiniObject();
//                po.addMetadata("domain", n.domain);
//                po.addMetadata("deployment", n.deployment);
//                po.addMetadata("process", n.process);
//                po.addMetadata("duration", String.valueOf(n.duration));
//                po.addMetadata("jobstart", n.jobstart);
//                po.addMetadata("jobend", n.jobend);
//                po.addMetadata("status", n.status);
//                po.addMetadata("logid", n.logid);
//
//                po.setData(Json.JsonToInternal(n.toString()));
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
