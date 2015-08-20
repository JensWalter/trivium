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

package io.trivium.urlhandler.anystore;

import io.trivium.anystore.AnyClient;
import io.trivium.glue.TriviumObject;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._e53042cbab0b4479958349320e397141.FileType;
import io.trivium.extension._e53042cbab0b4479958349320e397141.FileTypeFactory;
import javolution.util.FastList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class URLConnection extends java.net.URLConnection {
    Logger log = LogManager.getLogger(getClass());
    
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
        log.debug("looking for url {}",url.toString());
        //query anystore
        Query query = new Query();
        query.criteria.add(new Value("id", url.getHost()));
        FastList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query);
        FileTypeFactory factory = new FileTypeFactory();
        byte[] b=new byte[0];
        for(TriviumObject po : objects){
            FileType file = factory.getInstance(po);
            b = Base64.getDecoder().decode(file.data);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        return bis;
    }
}
