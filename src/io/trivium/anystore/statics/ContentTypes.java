package io.trivium.anystore.statics;

import javolution.util.FastMap;

public class ContentTypes {
    private static FastMap<String,String> ending2Mime = new FastMap<String,String>();
    private static FastMap<String,String> mime2Ending = new FastMap<String,String>();

    static{
        add("class","application/java-vm");
        add("css","text/css");
        add("htm","text/html");
        add("html","text/html");
        add("infiniup","application/infiniup.com");
        add("java","text/x-java-source");
        add("jar","application/java-archive");
        add("js","application/javascript");
        add("tar.bz2","application/x-bzip2");
        add("tar.gz","application/x-gzip");
        add("txt","text/plain");
        add("zip","application/zip");
        add("json","application/json");
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
