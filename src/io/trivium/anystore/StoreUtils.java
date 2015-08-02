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
