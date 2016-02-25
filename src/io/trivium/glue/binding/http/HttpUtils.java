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

package io.trivium.glue.binding.http;

import com.sun.net.httpserver.HttpExchange;
import io.trivium.NVList;
import io.trivium.glue.om.Json;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtils {
    public static String getInputAsString(HttpExchange exchange) {
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
            Logger logger = Logger.getLogger(HttpUtils.class.getName());
            logger.log(Level.SEVERE, "error while decoding http input stream", e);
        }
        return result;
    }

    public static byte[] getInputAsBinary(HttpExchange exchange) {
        InputStream requestStream = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[100000];
            while (requestStream.available() > 0) {
                int i = requestStream.read(buf);
                bos.write(buf, 0, i);
            }
            requestStream.close();
            bos.close();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(HttpUtils.class.getName());
            logger.log(Level.SEVERE, "error while decoding http input stream", e);
        }
        return bos.toByteArray();
    }

    public static NVList getInputAsNVList(HttpExchange exchange) {
        String str = getInputAsString(exchange);
        NVList list = Json.JsonToNVPairs(str);
        return list;
    }

    public static SSLSocketFactory allowAnyCertificateSocketFactory() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (GeneralSecurityException e) {
        }
        return null;
    }
}