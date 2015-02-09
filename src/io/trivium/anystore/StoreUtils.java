package io.trivium.anystore;

import io.trivium.Central;
import org.apache.commons.io.FileUtils;

import java.io.File;

public abstract class StoreUtils {

    final static protected String meta = "meta" + File.separator;
    final static protected String data = "data" + File.separator;
    final static protected String local = "local" + File.separator;
    
    public static void cleanStore(){
        Central.logger.info("cleaning persistence store");
        String path = Central.getProperty("basePath");
        if (!path.endsWith(File.separator))
            path += File.separator;
        path += "store"+File.separator;
        try {
            File f = new File(path+meta);
            if(f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            Central.logger.error("cleaning meta store failed",e1);
        }
        try {
            File f = new File(path + data);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            Central.logger.error("cleaning data store failed",e1);
        }
        try {
            File f = new File(path + local);
            if (f.exists())
                FileUtils.deleteQuietly(f);
        } catch (Exception e1) {
            Central.logger.error("cleaning local store failed",e1);
        }
    }
    
    public static void cleanQueue(){
            Central.logger.info("cleaning queue storage");
            String path = Central.getProperty("basePath");
            if (!path.endsWith(File.separator))
                path += File.separator;
            try {
                File f = new File(path+"queues"+File.separator);
                if(f.exists())
                    FileUtils.deleteQuietly(f);
            } catch (Exception e1) {
                Central.logger.error("cleaning queue storage failed",e1);
            }
        }   
    
    public static void createIfNotExists(String url) {
        File m = new File(url);
        if (!m.exists()) {
            m.mkdirs();
            Central.logger.debug("creating directory {}", url);
        }
    }
}
