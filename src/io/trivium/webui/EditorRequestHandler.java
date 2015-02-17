package io.trivium.webui;

import io.trivium.Central;
import io.trivium.glue.binding.http.Session;
import io.trivium.Central;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.binding.http.Session;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class EditorRequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
    Logger log = LogManager.getLogger(getClass());
    
    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
        log.debug("editor request handler");

        Session s = new Session(request, httpexchange, context, ObjectRef.getInstance());
        log.info(context.getAttribute("id"));

        s.ok();
    }
}
