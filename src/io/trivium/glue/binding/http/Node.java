package io.trivium.glue.binding.http;

import io.trivium.Central;
import io.trivium.Central;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Node {
	private int port;
    Logger log = LogManager.getLogger(getClass());
	
	public Node(){
		port = Integer.parseInt(Central.getProperty("httpPort"));
	}

    public void start() {
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("infiniup"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
 
        UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
        
        //internal object handler
        ObjectRequestHandler requesthandler = new ObjectRequestHandler();
        reqistry.register("/object/*", requesthandler);
      
        //channel handler
        ChannelRequestHandler channelhandler = new ChannelRequestHandler();
        reqistry.register("/channel/*", channelhandler);

        //webui handler
        WebUIRequestHandler uihandler = new WebUIRequestHandler();
        reqistry.register("/ui/*", uihandler);

        HttpConnectionHandler protocolHandler = new HttpConnectionHandler(httpproc, reqistry);
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;
        connFactory = new DefaultNHttpServerConnectionFactory(
                    ConnectionConfig.DEFAULT);
 
        final IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        IOReactorConfig config = IOReactorConfig.custom()
            .setIoThreadCount(1)
            .setSoTimeout(3000)
            .setConnectTimeout(3000)
            .build();
        try {
        	final ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
            ioReactor.listen(new InetSocketAddress(port));
            new Thread(() -> {
		            try {
						ioReactor.execute(ioEventDispatch);
					} catch (IOException e) {
						log.error("failed to start http server on port "+port,e);
                        System.exit(0);
					}
			}).start();
        } catch (Exception e) {
        	log.error("failed to start http server on port "+port,e);
            System.exit(0);
        }
        log.info("server listening on port {}",port);
    }
}