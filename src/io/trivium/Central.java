/*
 * Copyright 2015 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium;

import io.trivium.anystore.ObjectRef;
import io.trivium.extension.binding.Binding;
import io.trivium.profile.Profiler;
import io.trivium.profile.TimeUtils;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.StoreUtils;
import io.trivium.dep.org.apache.commons.cli.CommandLine;
import io.trivium.dep.org.apache.commons.cli.CommandLineParser;
import io.trivium.dep.org.apache.commons.cli.HelpFormatter;
import io.trivium.dep.org.apache.commons.cli.Options;
import io.trivium.dep.org.apache.commons.cli.PosixParser;
import sun.misc.UUEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Central {

    public static HashMap<String, String> properties = new HashMap<>();
    private static Logger log = Logger.getLogger(Central.class.getName());

    public static ArrayList<String> peers = new ArrayList<>();
    public static AtomicInteger currentPeer = new AtomicInteger(0);

    public static volatile boolean isRunning = false;

    public synchronized static void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public static String getProperty(String name) {
        return properties.get(name);
    }

    public static String getProperty(String name, String defaultvalue) {
        String value = properties.get(name);
        if (value == null || value.length() == 0)
            return defaultvalue;
        else
            return value;
    }

    public static void setLogLevel(String level) {
        Logger log = Logger.getLogger("");
        log.setLevel(Level.parse(level.toUpperCase()));
    }

    public static String getPeer() {
        int size = peers.size();
        int pos = currentPeer.incrementAndGet() % size - 1;
        return peers.get(pos);
    }

    /**
     * parses command line arguments and sets up the server.
     *
     * @param args command line arguments
     * @return true, if normal startup can proceed
     * @throws Exception
     */
    public static boolean setup(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opts = new Options();
        opts.addOption("ll", "loglevel", true, "turn logging to one of the following levels: fine,info,warning,severe");
        opts.addOption("h", "help", false, "show help");
        opts.addOption("?", "help", false, "show help");
        opts.addOption("p", "path", true, "base path for the local storage");
        opts.addOption("hp", "http", true, "port for the http interface, defaults to port 12345");
        opts.addOption("cs", "cleanStore", false, "re-initializes the local store (all information will be lost)");
        opts.addOption("cq", "cleanQueue", false, "re-initializes the local ingestion queue");
        opts.addOption("t", "test", true, "starts test mode with {x} dummy messages");
        opts.addOption("c", "compress", true, "enable/disbale snappy comrepssion (default=true)");
        opts.addOption("b", "build", false, "generate executable shell script");

        CommandLine cmd = parser.parse(opts, args);
        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("trivium", opts);
            return false;
        }
        if (cmd.hasOption("build")) {
            uuencode();
            return false;
        }

        if (cmd.hasOption("loglevel")) {
            String val = cmd.getOptionValue("loglevel");
            try {
                Level l = Level.parse(val.toUpperCase());
                Central.setLogLevel(val);
            }catch(Exception ex){
                log.log(Level.WARNING,"log level was not recognized, falling back to default value");
            }
        }
        if (cmd.hasOption("compress")) {
            String val = cmd.getOptionValue("compress");
            if (val.equalsIgnoreCase("false")) {
                Central.setProperty("compression", "false");
            } else {
                Central.setProperty("compression", "true");
            }
        }else{
            Central.setProperty("compression","true");
        }
        if (cmd.hasOption("test")) {
            String target = cmd.getOptionValue("test");
            Central.setProperty("test", target);
        }
        if (cmd.hasOption("path")) {
            String val = cmd.getOptionValue("path");
            if (val != null && val.length() > 1) {
                if (!val.endsWith(File.separator))
                    val += File.separator;
                Central.setProperty("basePath", val);
            } else {
                Central.setProperty("basePath", System.getProperty("user.dir"));
            }
        } else {
            Central.setProperty("basePath", System.getProperty("user.dir"));
        }
        if (cmd.hasOption("cleanStore")) {
            StoreUtils.cleanStore();
        }
        if (cmd.hasOption("cleanQueue")) {
            StoreUtils.cleanQueue();
        }

        if (cmd.hasOption("http")) {
            String val = cmd.getOptionValue("http");
            if (val != null && val.length() > 1) {
                try {
                    int port = Integer.parseInt(val);
                    if (port > 0 && port < 65535)
                        Central.setProperty("httpPort", val);
                    else
                        throw new Exception("out of range");
                } catch (Exception ex) {
                    Central.setProperty("httpPort", "12345");
                }
            } else {
                Central.setProperty("httpPort", "12345");
            }
        } else {
            Central.setProperty("httpPort", "12345");
        }

        Hardware.discover();

        return true;
    }

    /**
     * transform human readable units into machine values
     * 1g=1000000000
     * 1m=1000000
     * 1k=1000
     * @param val
     * @return
     */
    public static String resolveUnits(String val) {
        String rslt ;
        if (val.contains("g") || val.contains("b"))
            rslt = val.substring(0, val.length() - 1) + "000000000";
        else if (val.contains("m"))
            rslt = val.substring(0, val.length() - 1) + "000000";
        else if (val.contains("k"))
            rslt = val.substring(0, val.length() - 1) + "000";
        else rslt = val;
        return rslt;
    }

    /**
     * creates a executable shell script from the original trivium.jar file
     */
    public static void uuencode(){
        try {
            FileInputStream fis = new FileInputStream(new File("trivium.jar"));
            FileOutputStream fos = new FileOutputStream(new File("trivium.sh"));
            String head="#!/bin/bash\n" +
                    "uudecode $0\n" +
                    "java -jar trivium.jar -Djava.system.class.loader=io.trivium.TriviumLoader -Djava.protocol.handler.pkgs=io.trivium.urlhandler\n" +
                    "exit\n\n";
            fos.write(head.getBytes());
            UUEncoder uuec = new UUEncoder("trivium.jar");
            uuec.encodeBuffer(fis, fos);
            fos.flush();
            fos.close();
            fis.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "error creating script",e);
        }
    }

    public static void start() {

        //register activities
        Registry.INSTANCE.reload();

        try {
            //init ui handler
            Binding b = Registry.INSTANCE.getBinding(ObjectRef.getInstance("8c419189-0b68-4fe3-a807-34ad3287800d"));
            b.startBinding();
            //init object handler
            b = Registry.INSTANCE.getBinding(ObjectRef.getInstance("3d63321d-f553-4c3d-a8e7-b6159e7ee35b"));
            b.startBinding();
            //init channel handler
            b = Registry.INSTANCE.getBinding(ObjectRef.getInstance("3df01ff5-37cd-4dc0-a303-f9b897487494"));
            b.startBinding();
        }catch (Exception ex){
            log.log(Level.SEVERE,"error initializing the builtin http handler",ex);
        }
        //start anystore server
        Thread td = new Thread(AnyServer.INSTANCE, "anystore");
        td.setDaemon(true);
        td.start();

        // init profiler
        Timer t = new Timer();
        //one second after next time frame starts
        long start = TimeUtils.getTimeFrameStart(new Date().getTime() + 60000);
        t.schedule(Profiler.INSTANCE, new Date(start), 60000);

        log.log(Level.INFO,"trivium is now running and accessible through the web interface on http://localhost:12345/ui/");
    }
}
