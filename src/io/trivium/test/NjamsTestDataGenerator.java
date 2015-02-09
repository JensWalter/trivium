package io.trivium.test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class NjamsTestDataGenerator {

    private ArrayList<String[]> objects = new ArrayList<String[]>();
    private int counter = 0;
    private Random duration = new Random();

    public NjamsTestDataGenerator(int domaincount, int deploymentcount, int processcount){
        ArrayList<String> domains = new ArrayList<String>();
        ArrayList<String> template_domain = new ArrayList<String>();
        template_domain.add("dev");
        template_domain.add("test");
        template_domain.add("prod");
        template_domain.add("preprod");
        template_domain.add("useracceptance");
        for(int idx=1;idx<=domaincount;idx++){
            domains.add(template_domain.get(idx % template_domain.size())+idx);
        }
        
        ArrayList<String> deployments = new ArrayList<String>();
        ArrayList<String> template_deployments = new ArrayList<String>();
        template_deployments.add("OrderService");
        template_deployments.add("DWH");
        template_deployments.add("SAP");
        template_deployments.add("CMS");
        template_deployments.add("Portal");
        template_deployments.add("Twitter");
        template_deployments.add("Support");
        for(int idx=1;idx<=deploymentcount;idx++){
            deployments.add(template_deployments.get(idx % template_deployments.size())+idx);
        }
       
        ArrayList<String> processes = new ArrayList<String>();
        ArrayList<String> template_processes = new ArrayList<String>();
        template_processes.add("TimerStarter");
        template_processes.add("Service");
        template_processes.add("JMSSubscriber");
        template_processes.add("HTTPEndpoint");
        template_processes.add("RVReceiver");
        template_processes.add("Webhook");
        for(int idx=1;idx<=processcount;idx++){
            processes.add(template_processes.get(idx % template_processes.size())+idx);
        }
        
        for(String domain:domains){
            for(String deployment:deployments){
                for(String process:processes){
                    objects.add(new String[]{domain,deployment,process});
                }
            }
        }
    }
    
    public NjamsTestData getNJAMSData(){
        int local = counter++;
        String[] t = objects.get(local % objects.size());
        NjamsTestData n = new NjamsTestData();
        n.domain = t[0];
        n.deployment = t[1];
        n.process = t[2];
        n.duration = duration.nextInt(60000);

        Instant now = Instant.now();
        Instant before = now.minus(n.duration, ChronoUnit.MILLIS);
        n.jobstart = before.toString();
        n.jobend = now.toString();
        n.status ="success";
        n.logid = UUID.randomUUID().toString();
        return n;
    }
}
