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

import io.trivium.glue.binding.http.channel.Channel;
import io.trivium.anystore.ObjectRef;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelRequestHandler implements
		HttpAsyncRequestHandler<HttpRequest> {
	private final static Pattern uuidpattern = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    Logger log = Logger.getLogger(getClass().getName());
    
	@Override
	public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) {
		boolean processed = false;
        ObjectRef sourceId = ObjectRef.getInstance();
		Session s = new Session(request, httpexchange, context, sourceId);
		
		try {
			String uri = request.getRequestLine().getUri();
			String[] parts = uri.split("/");

			// get channelid
			ObjectRef channelId=null;
			Matcher matcher = uuidpattern.matcher(uri);
			if (matcher.find()) {
				channelId = ObjectRef.getInstance(matcher.group());
			} else {
				s.error(HttpStatus.SC_BAD_REQUEST,
						"channelid unknown\nplease use the following pattern\nhttp://{server}:{port}/channel/{in|out}/{channelid}\n");
				return;
			}

			// get method
			String method = parts[2];
			if (method.equals("in")) {
				processed = in(s, sourceId, channelId);
			}else if (method.equals("out")) {
				//TODO do query or whatever this is
				processed = false;
				s.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"not implemented");
				return;
			} else {
				// unknown method
				s.error(HttpStatus.SC_BAD_REQUEST,
						"method unknown\nplease use the following pattern\nhttp://{server}:{port}/channel/{in|out}/{channelid}\n");
				return;
			}

			if (!processed) {
				s.error(HttpStatus.SC_BAD_REQUEST, "request could not be processed");
				return;
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE,"error processing request", ex);
			s.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.toString());
			return;
		}
	}

	private boolean in(Session session, ObjectRef sourceId, ObjectRef channelId) throws IOException {
        try{
            Channel channel = Channel.getChannel(channelId);
            channel.process(session, sourceId);
        }catch(Exception ex){
            log.log(Level.SEVERE,"error processing request",ex);
            return false;
        }
        return true;
	}

	@Override
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest arg0, HttpContext arg1) throws HttpException,
			IOException {
		return new BasicAsyncRequestConsumer();
	}

}
