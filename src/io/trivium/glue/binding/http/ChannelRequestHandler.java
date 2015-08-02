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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelRequestHandler implements
		HttpAsyncRequestHandler<HttpRequest> {
	private final static Pattern uuidpattern = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    Logger log = LogManager.getLogger(getClass());
    
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
			log.error("error processing request",ex);
			s.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.toString());
			return;
		}
	}

	private boolean in(Session session, ObjectRef sourceId, ObjectRef channelId) throws IOException {
        try{
            Channel channel = Channel.getChannel(channelId);
            channel.process(session, sourceId);
        }catch(Exception ex){
            log.error("error processing request",ex);
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
