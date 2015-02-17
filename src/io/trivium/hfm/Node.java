package io.trivium.hfm;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;

import io.trivium.Central;
import io.trivium.glue.binding.http.HttpConnectionHandler;
import io.trivium.glue.binding.http.WebUIRequestHandler;
import io.trivium.Central;
import io.trivium.glue.binding.http.HttpConnectionHandler;
import io.trivium.glue.binding.http.WebUIRequestHandler;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Node {
    Logger log = LogManager.getLogger(getClass());
	private int port = 8734;
	
	public Node(){
		port = Integer.parseInt(Central.getProperty("hfmPort"));
	}
	
	public void start() {
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
 
        UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
        reqistry.register("*", new WebUIRequestHandler());
        HttpAsyncService protocolHandler = new HttpConnectionHandler(httpproc, reqistry);
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;
        connFactory = new DefaultNHttpServerConnectionFactory(
                    ConnectionConfig.DEFAULT);
 
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        IOReactorConfig config = IOReactorConfig.custom()
            .setIoThreadCount(1)
            .setSoTimeout(3000)
            .setConnectTimeout(3000)
            .build();
        try {
        	ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
            ioReactor.listen(new InetSocketAddress(port));
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            log.error(ex);
        } catch (IOException e) {
        	log.error(e);
        }
        log.info("shutting down http node on port {}",port);
    }
}
