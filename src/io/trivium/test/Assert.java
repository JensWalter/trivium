package io.trivium.test;

public class Assert {
    public static void equalsString(String string1, String string2){
        if(!string1.equals(string2)) {
            throw new AssertionError();
        }
    }
    
    public static void isTrue(boolean bool){
        if(!bool){
            throw new AssertionError();
        }
    }

    public static void isFalse(boolean bool){
        if(bool){
            throw new AssertionError();
        }
    }
}
