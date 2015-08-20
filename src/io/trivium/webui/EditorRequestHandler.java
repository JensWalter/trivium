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

import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.ObjectRef;
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
