package io.trivium.glue.binding.http;

import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.glue.om.Json;
import io.trivium.glue.om.Json;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HttpUtils {
    public static String getInputAsString(HttpRequest request){
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest) request;
            DataInputStream dis = null;
            String result = "";
            try {
                dis = new DataInputStream(r.getEntity().getContent());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[100000];
                while (dis.available() > 0) {
                    int i = dis.read(buf);
                    bos.write(buf, 0, i);
                }
                dis.close();
                bos.close();
                result = bos.toString("UTF-8");
            } catch (IOException e) {
                Logger log = LogManager.getLogger(HttpUtils.class);
                log.error("error while decoding http input stream", e);
            }
            return result;
        }else{
            return "";
        }
    }

    public static NVList getInputAsNVList(HttpRequest request){
        String str = getInputAsString(request);
        NVList list = Json.JsonToNVPairs(str);
        return list;
    }
}
