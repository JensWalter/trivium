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

import io.trivium.NVList;
import io.trivium.glue.om.Json;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HttpUtils {
    public static String getInputAsString(HttpRequest request){
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest) request;
            DataInputStream dis = null;
            String result = "";
            try {
                dis = new DataInputStream(r.getEntity().getContent());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[100000];
                while (dis.available() > 0) {
                    int i = dis.read(buf);
                    bos.write(buf, 0, i);
                }
                dis.close();
                bos.close();
                result = bos.toString("UTF-8");
            } catch (IOException e) {
                Logger log = LogManager.getLogger(HttpUtils.class);
                log.error("error while decoding http input stream", e);
            }
            return result;
        }else{
            return "";
        }
    }

    public static NVList getInputAsNVList(HttpRequest request){
        String str = getInputAsString(request);
        NVList list = Json.JsonToNVPairs(str);
        return list;
    }
}
