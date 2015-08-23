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

package io.trivium.glue.binding.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.ContentTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebUIRequestHandler implements HttpHandler {
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        Session s = new Session(httpexchange, ObjectRef.getInstance());
        String origURI = httpexchange.getRequestURI().getPath();
        String uri = origURI;
        if (uri.equals("/ui/"))
            uri = "io/trivium/webui/index.html";
        else
            uri = "io/trivium/webui" + uri.substring(3);
        log.log(Level.INFO, "receiving request for uri: {0} => {1}", new Object[]{origURI, uri});
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try {
            Class<?> clazz = cl.loadClass(uri.replace('/', '.'));
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> iface : interfaces) {
                if (iface.getCanonicalName().equals("com.sun.net.httpserver.HttpHandler")) {
                    //is request handler
                    HttpHandler handler = (HttpHandler) clazz.newInstance();
                    handler.handle(httpexchange);
                    return;
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        //TODO implement real reader
        InputStream is = cl.getResourceAsStream(uri);
        if (is != null) {
            InputStreamReader isr = new InputStreamReader(is);
            char[] buf = new char[1000000];
            int num = 0;
            try {
                num = isr.read(buf);
            } catch (IOException e) {
                log.log(Level.SEVERE,"error retrieving resource " + uri, e);
            }
            String ending = uri.substring(uri.lastIndexOf('.') + 1);
            String contentType = ContentTypes.getMimeType(ending, "text/plain");

            s.ok(contentType, new String(buf, 0, num));
            return;
        }
        //if no response was send so far
        s.error(404, "resource not found");
    }
}
