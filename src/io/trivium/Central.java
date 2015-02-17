package io.trivium;

import io.trivium.profile.Profiler;
import io.trivium.profile.TimeUtils;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.StoreUtils;
import io.trivium.glue.binding.http.Node;
import io.trivium.reactor.Registry;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class Central {

    public static FastMap<String, String> properties = new FastMap<String, String>()
            .shared();

    public static FastList<String> peers = new FastList<String>();
    public static AtomicInteger currentPeer = new AtomicInteger(0);

    Logger log = LogManager.getLogger(getClass());

    public static boolean isRunning = false;

    public static void setProperty(String name, String value) {
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
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        LoggerConfig lconf = conf.getLoggerConfig("core");
        lconf.setLevel(Level.getLevel(level.toUpperCase()));
        ctx.updateLoggers();
    }

    public static String getPeer() {
        int size = peers.size();
        int pos = currentPeer.incrementAndGet() % size - 1;
        return peers.get(pos);
    }

    public static void setup(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opts = new Options();
        opts.addOption("ll", "loglevel", true, "turn logging to one of the following levels: trace,debug,info,warning,error");
        opts.addOption("h", "help", false, "show help");
        opts.addOption("?", "help", false, "show help");
        opts.addOption("p", "path", true, "base path for the local storage");
        opts.addOption("hp", "http", true, "port for the http interface, defaults to port 12345");
        opts.addOption("cs", "cleanStore", false, "re-initializes the local store (all information will be lost)");
        opts.addOption("cq", "cleanQueue", false, "re-initializes the local ingestion queue");
        opts.addOption("a", "autosize", false, "sizes the jvm according to the environment");
        opts.addOption("t", "test", true, "starts test mode with {x} dummy messages");
        opts.addOption("se", "storageEngine", true, "choose between file and map backend");
        opts.addOption("c", "compress", true, "enable/disbale snappy comrepssion (default=true)");

        CommandLine cmd = parser.parse(opts, args);
        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("inifniup", opts);
            return;
        }
        if (cmd.hasOption("loglevel")) {
            String val = cmd.getOptionValue("loglevel");
            //TODO validate
            Central.setLogLevel(val);
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
        if (cmd.hasOption("storageEngine")) {
            String val = cmd.getOptionValue("storageEngine");
            if (val.equals("file") || val.equals("map")) {
                Central.setProperty("storageEngine", val);
            }
        }
        if (cmd.hasOption("test")) {
            String count = cmd.getOptionValue("test");
            Central.setProperty("test", resolveUnits(count));
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
    }

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

    public static void start() {
        // start http node
        if (Central.getProperty("httpPort") != null) {
            Node node = new Node();
            node.start();
        }

        //register activities
        Registry.INSTANCE.reload();

        //start anystore server
        Thread td = new Thread(AnyServer.INSTANCE, "anystore");
        td.start();

        // init profiler
        Timer t = new Timer();
        //one second after next time frame starts
        long start = TimeUtils.getTimeFrameStart(new Date().getTime() + 60000);
        t.schedule(Profiler.INSTANCE, new Date(start), 60000);
    }
}
