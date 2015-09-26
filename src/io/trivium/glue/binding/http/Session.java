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
import io.trivium.anystore.ObjectRef;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Session {

	HttpExchange httpexchange;
	ObjectRef id;
    Logger log = Logger.getLogger(getClass().getName());

	public Session(HttpExchange httpexchange) {
		this.httpexchange = httpexchange;
		this.id = ObjectRef.getInstance();
	}

	public HttpExchange getHttpExchange() {
		return httpexchange;
	}

    public ObjectRef getId(){
        return id;
    }

	public void error(int code, String text) {
		try {
            httpexchange.getResponseHeaders().set("Content-Type","text/plain; charset=UTF-8");
            httpexchange.sendResponseHeaders(code, text.length());
            httpexchange.getResponseBody().write(text.getBytes());
            httpexchange.getResponseBody().close();
            httpexchange.close();
		} catch (Exception ex) {
			log.log(Level.FINE,"error while sending 'error' response",ex);
		}
	}

	public void ok() {
		try {
            httpexchange.getResponseHeaders().set("Content-Type","text/plain; charset=UTF-8");
            httpexchange.sendResponseHeaders(200, 4);
            httpexchange.getResponseBody().write("true".getBytes());
            httpexchange.getResponseBody().close();
            httpexchange.close();
		} catch (Exception ex) {
            log.log(Level.FINE, "error while sending 'ok' response", ex);
		}
	}

    public void ok(String contentType,String resp) {
        try {
            httpexchange.getResponseHeaders().set("Content-Type",contentType+"; charset=UTF-8");
            byte[] responseData = resp.getBytes();
            httpexchange.sendResponseHeaders(200, responseData.length);
            httpexchange.getResponseBody().write(responseData);
            httpexchange.getResponseBody().close();
            httpexchange.close();

        } catch (Exception ex) {
            log.log(Level.FINE, "error while sending 'ok' response", ex);
        }
    }
}
