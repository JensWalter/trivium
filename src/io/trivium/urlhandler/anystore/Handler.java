package io.trivium.urlhandler.anystore;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends java.net.URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url)
            throws IOException {
        return new io.trivium.urlhandler.anystore.URLConnection(url);
    }
}
