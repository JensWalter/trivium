package io.trivium.test;

import io.trivium.Central;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Json;
import io.trivium.Central;
import io.trivium.anystore.AnyClient;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Json;

public class Start {

    public static void main(String[] args) throws Exception {
        System.out.println("starting");
        Central.setup(args);
        long count = Long.parseLong(Central.getProperty("test"));

        Central.start();

        AnyClient c = AnyClient.INSTANCE;

        NjamsTestDataGenerator gen = new NjamsTestDataGenerator(2, 5, 3);
        for (long i = 1; i < count; i++) {
            NjamsTestData n = gen.getNJAMSData();
            InfiniObject po = new InfiniObject();
            po.addMetadata("domain", n.domain);
            po.addMetadata("deployment", n.deployment);
            po.addMetadata("process", n.process);
            po.addMetadata("duration", String.valueOf(n.duration));
            po.addMetadata("jobstart", n.jobstart);
            po.addMetadata("jobend", n.jobend);
            po.addMetadata("status", n.status);
            po.addMetadata("logid", n.logid);

            po.setData(Json.JsonToInternal(n.toString()));

            c.storeObject(po);
            try {
//                if(i%12==0)
//                    Thread.sleep(1);
            }catch(Exception ex){
                //ignore
            }
        }
        System.out.println("done.");

    }

}
