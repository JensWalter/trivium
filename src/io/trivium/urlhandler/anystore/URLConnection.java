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
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.extension._e53042cbab0b4479958349320e397141.FileType;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class URLConnection extends java.net.URLConnection {
    Logger log = Logger.getLogger(getClass().getName());
    
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
        log.log(Level.FINE,"looking for url {}", url.toString());
        //query anystore
        Query query = new Query();
        query.criteria.add(new Value("id", url.getHost()));
        ArrayList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query).list;
        byte[] b=new byte[0];
        for(TriviumObject po : objects){
            FileType file = new FileType();
            file.populate(po);
            b = Base64.getDecoder().decode(file.data);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        return bis;
    }
}
