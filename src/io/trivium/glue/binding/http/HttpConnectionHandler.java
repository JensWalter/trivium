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

import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.protocol.HttpProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpConnectionHandler extends HttpAsyncService {
    Logger log = LogManager.getLogger(getClass());

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
		log.debug("exception logged {}", ex);
		super.log(ex);
	}

	@Override
	public void exception(NHttpServerConnection conn, Exception cause) {
		log.debug("{}: exeption thrown {}", conn, cause);
		super.exception(conn, cause);
	}
}
