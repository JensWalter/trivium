package io.trivium.test;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class whatever {

    public static void main(String[] args) throws Exception {
            String pathname = "/Users/jens/tmp/map.data";
            File file = new File(pathname);

            
            ChronicleMapBuilder<String, byte[]> builder =
                ChronicleMapBuilder.of(String.class,byte[].class);
            ChronicleMap<String, byte[]> map = builder.entries(100000000).create(file);

            map.put("a", new byte[]{1});
            
//            long count = 1000;
//            JsonGenerator gen = new JsonGenerator(2, 5, 3);
//            for(long i=1;i<count;i++){
//                njams n = gen.getNJAMSData();
//                PersistenceObject po = new PersistenceObject();
//                NVList l = new NVList();
//                l.add(new NVPair("domain", n.domain));
//                l.add(new NVPair("deployment", n.deployment));
//                l.add(new NVPair("process", n.process));
//                l.add(new NVPair("duration", String.valueOf(n.duration)));
//                l.add(new NVPair("jobstart", n.jobstart));
//                l.add(new NVPair("jobend", n.jobend));
//                l.add(new NVPair("status", n.status));
//                l.add(new NVPair("logid", n.logid));
//                po.metadata=l;
//                po.binaryData=n.toString().getBytes();
//                map.put(po.id.getIdString(), po.binaryData);
//             }
             map.close();
    }

}
