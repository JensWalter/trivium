package io.trivium.glue.binding.http;

import io.trivium.Central;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.protocol.HttpProcessor;

public class HttpConnectionHandler extends HttpAsyncService {

	public HttpConnectionHandler(HttpProcessor httpProcessor,
			HttpAsyncRequestHandlerMapper handlerMapper) {
		super(httpProcessor, handlerMapper);
	}

	@Override
	public void connected(final NHttpServerConnection conn) {
//		Central.logger.debug("{}: connection open", conn);
		super.connected(conn);
		//10 minutes timeout
		conn.setSocketTimeout(600000);
	}

	@Override
	public void closed(final NHttpServerConnection conn) {
//		Central.logger.debug("{}: connection closed", conn);
		super.closed(conn);
	}

	@Override
	protected void log(Exception ex) {
		Central.logger.debug("exception logged {}", ex);
		super.log(ex);
	}

	@Override
	public void exception(NHttpServerConnection conn, Exception cause) {
		Central.logger.debug("{}: exeption thrown {}", conn, cause);
		super.exception(conn, cause);
	}
}
