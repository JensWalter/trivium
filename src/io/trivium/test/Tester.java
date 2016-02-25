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

package io.trivium.test;

import io.trivium.Central;
import io.trivium.Registry;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tester {
    public static Logger logger = Logger.getLogger(Tester.class.getName());

    public static void success(String... args){
        if(args!=null && args.length>1){
            logger.log(Level.INFO,"success " + args[0], args);
        }else {
            logger.log(Level.INFO,"success", args);
        }
    }

    public static void error(String... args){
        if(args!=null && args.length>1){
            logger.log(Level.INFO,"error "+args[0],args);
        }else {
            logger.log(Level.INFO,"error", args);
        }
    }

    /**
     * runs all registered test cases
     * @return all tests ran successful
     */
    public static boolean runAll(){
        String scope = Central.getProperty("test","core");
        logger.log(Level.INFO,"running test suite for target {0}",scope);
        int count=0;
        int success=0;
        Iterator<TestCase> iter = Registry.INSTANCE.testcases.values().iterator();
        while(iter.hasNext()){
            count++;
            TestCase tc = iter.next();
            logger.log(Level.INFO,"{0} -> test {1}",new String[]{tc.getTypeRef().toString(),tc.getTestName()});
            try {
                tc.run();
                success++;
                logger.log(Level.INFO,"test {0}: succeeded", tc.getTypeRef().toString());
            }catch(Exception ex){
                logger.log(Level.SEVERE,"test "+tc.getTypeRef().toString()+": failed with exception", ex);
            }
        }
        logger.log(Level.INFO,"tests completed with {0}/{1} successful",
                    new String[]{String.valueOf(success),String.valueOf(count)});
        return count==success;
    }
}
