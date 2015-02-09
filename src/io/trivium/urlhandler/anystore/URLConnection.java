package io.trivium.urlhandler.anystore;

import io.trivium.Central;
import io.trivium.anystore.AnyClient;
import io.trivium.glue.InfiniObject;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._e53042cbab0b4479958349320e397141.v1.FileType;
import io.trivium.extension._e53042cbab0b4479958349320e397141.v1.FileTypeFactory;
import javolution.util.FastList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class URLConnection extends java.net.URLConnection {
    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected URLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {

    }

    synchronized public InputStream getInputStream()
            throws IOException {
        Central.logger.debug("looking for url {}",url.toString());
        //query anystore
        Query query = new Query();
        query.criteria.add(new Value("id", url.getHost()));
        FastList<InfiniObject> objects = AnyClient.INSTANCE.loadObjects(query);
        FileTypeFactory factory = new FileTypeFactory();
        byte[] b=new byte[0];
        for(InfiniObject po : objects){
            FileType file = factory.getInstance(po);
            b = Base64.getDecoder().decode(file.data);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        return bis;
    }
}
