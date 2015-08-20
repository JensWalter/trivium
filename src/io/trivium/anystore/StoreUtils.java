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

package io.trivium.anystore;

import io.trivium.Central;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class StoreUtils {

    final static protected String meta = "meta" + File.separator;
    final static protected String data = "data" + File.separator;
    final static protected String local = "local" + File.separator;

    public static void cleanStore() {
        Logger log = LogManager.getLogger(StoreUtils.class);
        log.info("cleaning persistence store");
        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store" + File.separator;
        try {
            File f = new File(path + meta);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            log.error("cleaning meta store failed", e1);
        }
        try {
            File f = new File(path + data);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            log.error("cleaning data store failed", e1);
        }
        try {
            File f = new File(path + local);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            log.error("cleaning local store failed", e1);
        }
    }

    public static void cleanQueue() {
        Logger log = LogManager.getLogger(StoreUtils.class);
        log.info("cleaning queue storage");
        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        try {
            File f = new File(path + "queues" + File.separator);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            log.error("cleaning queue storage failed", e1);
        }
    }

    public static void createIfNotExists(String url) {
        Logger log = LogManager.getLogger(StoreUtils.class);
        File m = new File(url);
        if (!m.exists()) {
            m.mkdirs();
            log.debug("creating directory {}", url);
        }
    }
}
