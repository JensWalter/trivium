/*
 * Copyright 2016 Jens Walter
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

package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.dep.org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreUtils {

    final static protected String meta = "meta" + File.separator;
    final static protected String data = "data" + File.separator;
    final static protected String local = "local" + File.separator;

    public static void cleanStore() {
        Logger logger = Logger.getLogger(StoreUtils.class.getName());
        logger.info("cleaning persistence store");
        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator;
        try {
            File f = new File(path + meta);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            logger.log(Level.SEVERE,"cleaning meta store failed", e1);
        }
        try {
            File f = new File(path + data);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            logger.log(Level.SEVERE,"cleaning data store failed", e1);
        }
        try {
            File f = new File(path + local);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            logger.log(Level.SEVERE,"cleaning local store failed", e1);
        }
    }

    public static void cleanQueue() {
        Logger logger = Logger.getLogger(StoreUtils.class.getName());
        logger.info("cleaning queue storage");
        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        try {
            File f = new File(path + "queues" + File.separator);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            logger.log(Level.SEVERE,"cleaning queue storage failed", e1);
        }
    }

    public static void createIfNotExists(String url) {
        Logger logger = Logger.getLogger(StoreUtils.class.getName());
        File m = new File(url);
        if (!m.exists()) {
            m.mkdirs();
            logger.log(Level.FINE,"creating directory {}", url);
        }
    }
}
