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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FormDataHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getRequestHeaders();
        String contentType = headers.getFirst("Content-Type");
        if(contentType.startsWith("multipart/form-data")){
            //found form data
            String boundary = contentType.substring(contentType.indexOf("boundary=")+9);
            String payload = getInputAsString(httpExchange);
            // as of rfc7578 - prepend "\r\n--"
            String[] parts = payload.split("\r\n--" + boundary);
            ArrayList<MultiPart> list = new ArrayList<>();
            for (String part : parts) {
                MultiPart p = new MultiPart();

                int headerSize = part.indexOf("\r\n\r\n");
                if (headerSize != -1) {
                    String header = part.substring(0, headerSize);
                    // extract name from header
                    int idx = header.indexOf("\r\nContent-Disposition: form-data; name=");
                    if (idx >= 0) {
                        int startMarker = idx + 39;
                        //check for extra filename field
                        int fileNameStart = header.indexOf("; filename=");
                        if(fileNameStart>=0){
                            String filename = header.substring(fileNameStart+11,header.indexOf("\r\n",fileNameStart));
                            p.filename = filename.replace('"', ' ').replace('\'', ' ').trim();
                            p.name = header.substring(startMarker, fileNameStart).replace('"', ' ').replace('\'', ' ').trim();
                        }else{
                            int endMarker = header.indexOf("\r\n", startMarker);
                            if (endMarker == -1)
                                endMarker = header.length();
                            p.name = header.substring(startMarker, endMarker).replace('"', ' ').replace('\'', ' ').trim();
                        }
                    } else {
                        // skip entry if no name is found
                        continue;
                    }
                    // extract content type from header
                    idx = header.indexOf("\r\nContent-Type:");
                    if (idx >= 0) {
                        int startMarker = idx + 15;
                        int endMarker = header.indexOf("\r\n", startMarker);
                        if (endMarker == -1)
                            endMarker = header.length();
                        p.contentType = header.substring(startMarker, endMarker).trim();
                    }
                    p.value = part.substring(headerSize + 4);
                    list.add(p);
                }
            }
            handle(httpExchange,list);
        }else{
            //if no form data is present, still call handle method
            handle(httpExchange,null);
        }
    }

    public abstract void handle(HttpExchange httpExchange,List<MultiPart> multiParts) throws IOException;

    public String getInputAsString(HttpExchange exchange) {
        InputStream requestStream = exchange.getRequestBody();
        String result = "";
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[100000];
            while (requestStream.available() > 0) {
                int i = requestStream.read(buf);
                bos.write(buf, 0, i);
            }
            requestStream.close();
            bos.close();
            result = bos.toString("UTF-8");
        } catch (IOException e) {
            Logger log = Logger.getLogger(this.getClass().getName());
            log.log(Level.SEVERE, "error while decoding http input stream", e);
        }
        return result;
    }

    public static class MultiPart {
        String contentType;
        String name;
        String filename;
        String value;
    }
}
