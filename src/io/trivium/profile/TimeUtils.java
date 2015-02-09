package io.trivium.profile;

public class TimeUtils {

    public static long getTimeFrameStart(long interval){
        long now = System.currentTimeMillis();
        return (now - now%interval);
    }
    public static long getTimeFrameStart(long interval,long timestamp){
        return (timestamp - timestamp%interval);
    }
    public static long getTimeFrameEnd(long interval, long timestamp){
        return (timestamp - timestamp%interval + interval-1);
    }
}
