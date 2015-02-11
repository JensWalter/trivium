package io.trivium.test;

import io.trivium.anystore.ObjectRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Tester {
    public static Logger logger = LogManager.getLogger(Tester.class);

    public static void success(String... args){
        if(args!=null && args.length>1){
            logger.info("success "+args[0],args);
        }else {
            logger.info("succes", args);
        }
    }

    public static void error(String... args){
        if(args!=null && args.length>1){
            logger.info("error "+args[0],args);
        }else {
            logger.info("error", args);
        }
    }
}
