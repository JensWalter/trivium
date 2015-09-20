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

package io.trivium.anystore.statics;

import java.util.HashMap;

public class MimeTypes {
    private static HashMap<String,String> ending2Mime = new HashMap<>();
    private static HashMap<String,String> mime2Ending = new HashMap<>();

    static{
        add("class","application/java-vm");
        add("css","text/css");
        add("htm","text/html");
        add("html","text/html");
        add("trivium","application/trivium.io");
        add("java","text/x-java-source");
        add("jar","application/java-archive");
        add("js","application/javascript");
        add("tar.bz2","application/x-bzip2");
        add("tar.gz","application/x-gzip");
        add("txt","text/plain");
        add("zip","application/zip");
        add("json","application/json");
        add("xml","application/xml");
    }

    public static void add(String ending,String mime){
        ending2Mime.put(ending,mime);
        mime2Ending.put(mime,ending);
    }

    public static String getMimeType(String ending){
        return ending2Mime.get(ending);
    }

    public static String getMimeType(String ending,String fallback){
        String str = ending2Mime.get(ending);
        if(str==null){
            str = fallback;
        }
        return str;
    }

    public static String getEnding(String mime){
        return mime2Ending.get(mime);
    }

    public static String getEnding(String mime,String fallback){
        String str = mime2Ending.get(mime);
        if(str==null){
            str = fallback;
        }
        return str;
    }
}
