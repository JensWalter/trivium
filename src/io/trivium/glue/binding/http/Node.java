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

import com.sun.net.httpserver.HttpServer;
import io.trivium.Central;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {
	private int port;
    Logger log = Logger.getLogger(getClass().getName());
	
	public Node(){
		port = Integer.parseInt(Central.getProperty("httpPort"));
	}

    public void start() {
        try {
            //TODO how to specify backlog for the tcp implementation
            HttpServer server = HttpServer.create(new InetSocketAddress(port),100);
            server.setExecutor(null); // creates a default executor

            //internal object handler
            server.createContext("/object/", new ObjectRequestHandler());

            //channel handler
            server.createContext("/channel/", new ChannelRequestHandler());

            //webui handler
            server.createContext("/ui/", new WebUIRequestHandler());

            server.start();
        } catch (Exception e) {
            log.log(Level.SEVERE, "failed to start http server on port " + port, e);
            System.exit(0);
        }
        log.log(Level.INFO, "server listening on port {}", port);
    }
}