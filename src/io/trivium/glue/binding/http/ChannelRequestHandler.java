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
import io.trivium.glue.binding.http.channel.Channel;
import io.trivium.anystore.ObjectRef;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelRequestHandler implements HttpHandler {
	private final static Pattern uuidpattern = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    Logger log = Logger.getLogger(getClass().getName());
    
	@Override
	public void handle(HttpExchange httpexchange) {
		boolean processed;
        ObjectRef sourceId = ObjectRef.getInstance();
		Session s = new Session(httpexchange, sourceId);
		
		try {
			String uri = httpexchange.getRequestURI().getPath();
			String[] parts = uri.split("/");

			// get channelid
			ObjectRef channelId=null;
			Matcher matcher = uuidpattern.matcher(uri);
			if (matcher.find()) {
				channelId = ObjectRef.getInstance(matcher.group());
			} else {
				s.error(/*SC_BAD_REQUEST*/400,
						"channelid unknown\nplease use the following pattern\nhttp://{server}:{port}/channel/{in|out}/{channelid}\n");
				return;
			}

			// get method
			String method = parts[2];
			if (method.equals("in")) {
				processed = in(s, sourceId, channelId);
			}else if (method.equals("out")) {
				//TODO do query or whatever this is
				s.error(/*INTERNAL_SERVER_ERROR*/500, "not implemented");
				return;
			} else {
				// unknown method
				s.error(/*BAD_REQUEST*/400,
						"method unknown\nplease use the following pattern\nhttp://{server}:{port}/channel/{in|out}/{channelid}\n");
				return;
			}

			if (!processed) {
				s.error(/*BAD_REQUEST*/400, "request could not be processed");
				return;
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE,"error processing request", ex);
			s.error(/*INTERNAL_SERVER_ERROR*/500, ex.toString());
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
}
