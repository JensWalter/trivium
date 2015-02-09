package io.trivium.test;

import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;

/**
 * Created by jens on 18/10/14.
 */
public class MapTest {
    public static void main(String[] args) throws Exception {


        String str = "{\"typeId\":\"e53042cb-ab0b-4489-9583-49320e397142\","
                +"\"datapoint\":\"anystore_size\",\"created\":[ \"2014-04-04T00:00:00Z\""
                +", \"2014-12-31T00:00:00Z\" ]} ";
        Element query = Json.JsonToInternal(str);

        System.out.println(str);

        System.out.println(query.toString());

        System.out.println(Json.InternalToJson(query));


//        FastBitSet bitset = new FastBitSet(Integer.MAX_VALUE);
//        bitset.set(123,false);
//        for(int i=0;i<10000;i++){
//            bitset.set(i,true);
//        }
//        bitset.set(1234,true);
//        bitset.set(2432,true);
//        long start =0;
//        long count=0;
//        for(int i=0;i<10;i++) {
//            start = System.nanoTime();
//            Iterator<Index> idx = bitset.iterator();
//            int count2 = 0;
//            while (idx.hasNext()) {
//                idx.next();
//                count2++;
//            }
//            System.out.println("iter " + (System.nanoTime() - start));
//            start = System.nanoTime();
//            count = bitset.stream().count();
//            System.out.println("stream " + (System.nanoTime() - start));
//            start = System.nanoTime();
//            count = bitset.parallelStream().count();
//            System.out.println("para " + (System.nanoTime() - start));
//        }

                /*

        ChronicleMap<byte[], byte[]> map = null;
        File file = new File("/Users/jens/tmp/sample.map");
        ChronicleMapBuilder<byte[], byte[]> builder =
                ChronicleMapBuilder.of(byte[].class,byte[].class);
        try {
            builder.keySize(16);
            builder.valueSize(8);
            builder.immutableKeys();
            builder.minSegments(2048);
            builder.entries(1024*1024L);
            builder.putReturnsNull(true);
            builder.removeReturnsNull(true);
System.out.println(builder.toString());
            map = builder.create(file);
        } catch (IOException e) {
            Central.logger.error("cannot initialize chronicle map",e);
            System.exit(0);
        }*/
    }
}
