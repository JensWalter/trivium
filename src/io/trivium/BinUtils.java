package io.trivium;

public class BinUtils {

    public static byte[] Int2Bytes(int input){
        return new byte[]{
                (byte) (input >>> 24),
                (byte) (input >>> 16),
                (byte) (input >>> 8),
                (byte) input};
    }
}
