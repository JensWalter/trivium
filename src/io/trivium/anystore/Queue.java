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

import io.qdb.buffer.MessageCursor;
import io.qdb.buffer.PersistentMessageBuffer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Queue {
    static HashMap<String,Queue> queueList = new HashMap<>();
    Logger log = Logger.getLogger(getClass().getName());

    PersistentMessageBuffer queue;
    volatile long writePointer = 0;
    //buffer for splitting both volatiles into 2 cache lines
    long[] buffer = new long[32];
    volatile long readPointer = 0;

    public Queue(String path){
        try {
            queue = new PersistentMessageBuffer(new File(path));
            //set size to 100mb
            queue.setMaxSize(100*1024*1024);
        }catch(Exception ex){
            log.log(Level.SEVERE,"error initializing queue on path "+path,ex);
        }
    }

    public void append(byte[] msg){
        try {
            //routing key is empty since it is not used
            long id = queue.append(System.currentTimeMillis(),"",msg);
            writePointer = id;
        } catch (IOException e) {
            log.log(Level.SEVERE,"error appending message to queue",e);
        }
    }

    public MessageCursor getCursor(long pos){
        try {
            return queue.cursor(pos);
        } catch (IOException e) {
            log.log(Level.SEVERE,"error creating cursor",e);
        }
        return null;
    }

    public synchronized static Queue getQueue(String path){
        if(queueList.containsKey(path)){
            return queueList.get(path);
        }else{
            Queue q = new Queue(path);
            queueList.put(path,q);
            return q;
        }
    }
}
