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

import io.trivium.anystore.ObjectRef;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Session {

	HttpRequest request;
	HttpAsyncExchange httpexchange;
	HttpContext context;
	ObjectRef id;
    Logger log = LogManager.getLogger(getClass());

	public Session(HttpRequest request, HttpAsyncExchange httpexchange,
			HttpContext context, ObjectRef id) {
		this.request = request;
		this.httpexchange = httpexchange;
		this.context = context;
		this.id = id;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpAsyncExchange getHttpexchange() {
		return httpexchange;
	}

	public HttpContext getContext() {
		return context;
	}

	public void error(int code, String text) {
		try {
			StringEntity entity = new StringEntity(text + "\n",
					ContentType.create("text/plain", "UTF-8"));

			HttpResponse response = httpexchange.getResponse();
			response.setEntity(entity);
			response.setStatusCode(code);

			httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	public void ok() {
		try {
			StringEntity entity = new StringEntity("true\n",
					ContentType.create("text/plain", "UTF-8"));

			HttpResponse response = httpexchange.getResponse();
			response.setEntity(entity);
			response.setStatusCode(HttpStatus.SC_OK);

			httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
		} catch (Exception ex) {
			log.error(ex);
		}
	}

    public void ok(String contentType,String resp) {
        try {
            StringEntity entity = new StringEntity(resp,ContentType.create(contentType,"UTF-8"));
            HttpResponse response = httpexchange.getResponse();
            response.setStatusCode(HttpStatus.SC_OK);
            response.setEntity(entity);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
