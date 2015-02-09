package io.trivium.test;

import com.google.gson.JsonObject;

public class NjamsTestData {

    public String domain;
    public String deployment;
    public String process;
    public int duration;
    public String jobstart;
    public String jobend;
    public String status;
    public String logid;
    
    public String toString(){
        JsonObject js = new JsonObject();
        js.addProperty("domain", this.domain);
        js.addProperty("deployment", this.deployment);
        js.addProperty("process", this.process);
        js.addProperty("duration", this.duration);
        js.addProperty("jobstart", this.jobstart);
        js.addProperty("jobend", this.jobend);
        js.addProperty("status", this.status);
        js.addProperty("logid", this.logid);
        return js.toString();
    }
}
