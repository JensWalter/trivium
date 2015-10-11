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

package io.trivium.glue;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.trivium.extension.binding.Binding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http {
    private static ConcurrentHashMap<String,HttpHandler> listeners = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, HttpServer> servers = new ConcurrentHashMap<>();
    static Logger log = Logger.getLogger(Http.class.getName());

    public static void registerListener(String uri, HttpHandler handler) {
        registerListener("localhost", 12345, uri, handler);
    }

    public static void registerListener(int port, String uri, HttpHandler handler) {
        registerListener("localhost", port, uri, handler);
    }

    public static boolean registerListener(String host, int port, String uri, HttpHandler handler) {
        String endpoint = host+port+uri;
        String binding = host+port;
        synchronized (listeners) {
            for (String entry : listeners.keySet()) {
                if (entry.startsWith(endpoint)) {
                    return false;
                }
            }
            listeners.put(endpoint, handler);
        }
        //look for an existing server
        HttpServer server = null;
        synchronized (servers) {
            for (String entry : servers.keySet()) {
                if (entry.equals(binding)) {
                    server = servers.get(entry);
                }
            }
            if(server == null) {
                try {
                    server = HttpServer.create(new InetSocketAddress(port),100);
                    server.start();
                } catch (IOException e) {
                    log.log(Level.SEVERE,"server could not be initialized",e);
                    return false;
                }
                servers.put(binding, server);
            }
        }
        //bind uri
        server.createContext(uri,handler);


        return true;
    }

    public static void unregisterListener(HttpHandler handler){}
}
