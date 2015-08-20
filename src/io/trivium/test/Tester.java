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

package io.trivium.test;

import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.TypeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Tester {
    public static Logger logger = LogManager.getLogger(Tester.class);

    public static void success(String... args){
        if(args!=null && args.length>1){
            logger.info("success "+args[0],args);
        }else {
            logger.info("success", args);
        }
    }

    public static void error(String... args){
        if(args!=null && args.length>1){
            logger.info("error "+args[0],args);
        }else {
            logger.info("error", args);
        }
    }
    
    public static void runAll(){
        logger.info("running test suite");
        ServiceLoader<TestCase> typeLoader = ServiceLoader.load(TestCase.class);
        typeLoader.reload();
        Iterator<TestCase> iter = typeLoader.iterator();
        while(iter.hasNext()){
            TestCase tc = iter.next();
            logger.info("test {}: {} {}",tc.getTypeId().toString(),tc.getClassName(),tc.getMethodName());
            try {
                tc.run();
                logger.info("test {}: succeeded", tc.getTypeId().toString());
            }catch(Exception ex){
                logger.error("test {}: failed with exception {}", ex);
            }
        }
    }
}
