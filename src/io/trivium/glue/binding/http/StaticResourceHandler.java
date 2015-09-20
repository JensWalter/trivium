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
import io.trivium.anystore.statics.MimeTypes;
import io.trivium.dep.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class StaticResourceHandler implements HttpHandler {
    private String httpUri;
    private String packageUri;

    public StaticResourceHandler(String httpUri, String packageUri) {
        if (httpUri.endsWith("/")) {
            this.httpUri = httpUri;
        } else {
            this.httpUri = httpUri+"/";
        }
        this.packageUri = packageUri+".";
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Session session = new Session(httpExchange);
        String origURI = httpExchange.getRequestURI().getPath();
        String uri = origURI.replace(httpUri, packageUri).replace('/', '.');
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        InputStream is = cl.getResourceAsStream(uri);
        if (is != null) {
            byte[] buf = IOUtils.toByteArray(is);
            String ending = uri.substring(uri.lastIndexOf('.') + 1);
            String contentType = MimeTypes.getMimeType(ending, "text/plain");
            session.ok(contentType, new String(buf));
            return;
        }
        //if no response was send so far
        session.error(404, "resource not found");
    }
}
