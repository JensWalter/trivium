package io.trivium.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileTest {

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        long size = Long.parseLong(args[1]);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        if (raf.length() != size) {

            raf.setLength(size);
            FileChannel fc = raf.getChannel();
          //  fc.map(MapMode.READ_WRITE,0L, size);
            
            Method map0 = fc.getClass().getDeclaredMethod("map0",
                    new Class[] { Integer.TYPE, Long.TYPE, Long.TYPE });
            map0.setAccessible(true);
            Object o = map0.invoke(fc,
                    new Object[] { Integer.valueOf("1"), Long.valueOf("0"), Long.valueOf(size) });

            System.out.println(o.toString());
        }
        
        raf.close();
    }

}
