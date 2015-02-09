package io.trivium.urlhandler.anystore;

import java.net.URLStreamHandler;

public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ( protocol.equalsIgnoreCase("anystore") )
            return new Handler();
        else
            return null;
    }
}
