/*
 * Copyright 2016 Jens Walter
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

import io.trivium.extension.fact.WeightedAverage;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Profiler;

import javax.management.MBeanServerConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hardware {

    /**
     * core count
     */
    public static int cpuCount = 1;

    /**
     * size in mb
     */
    public static long memSize = 1024;

    /**
     * filesystem the anystore resides on
     */
    public static String fsType = "unknown";

    public static void discover() throws Exception {
        // detect os
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            // mac
            discoverMac();
        } else if (os.contains("win")) {
            // windows
            throw new Exception("discovery for this operating system is not supported");
        } else if (os.contains("nux")) {
            // linux
            discoverLinux();
        }
        Profiler.INSTANCE.initAverage(new WeightedAverage(DataPoints.OS_CPU_USAGE));
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        final OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,OperatingSystemMXBean.class);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Profiler.INSTANCE.avg(DataPoints.OS_CPU_USAGE,osMBean.getSystemLoadAverage());
            }
        },1,5000);
    }

    public static String runInOS(String[] cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            br.close();
            isr.close();
            return result.toString().trim();
        } catch (Exception e) {
            Logger logger = Logger.getLogger(Hardware.class.getName());
            logger.log(Level.SEVERE,"error exists with the current command", e);
        }
        return "";
    }

    private static void discoverMac() {
        // get cpu count
        cpuCount = Runtime.getRuntime().availableProcessors();

        // get memory size
        String size = runInOS(new String[] { "/bin/sh", "-c", "sysctl -n hw.memsize" });
        try {
            long i = Long.parseLong(size);
            if (i > 0)
                memSize = i;
        } catch (Exception e) {
            Logger logger = Logger.getLogger(Hardware.class.getName());
            logger.log(Level.SEVERE, "error discovering the mac address", e);
        }
    }

    private static void discoverLinux() {
        // get cpu count
        cpuCount = Runtime.getRuntime().availableProcessors();

        // get memory size
        String size = runInOS(new String[] { "/bin/sh", "-c", "/usr/bin/awk '/MemTotal:/ { print $2 }' /proc/meminfo" });
        Logger logger = Logger.getLogger(Hardware.class.getName());
        
        try {
            try {
                long i = Long.parseLong(size);
                if (i > 0)
                    memSize = i * 1024L;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "cannot read memory size", e);
            }
            String basePath = Central.getProperty("basePath");
            // checking filesystem
            String cmd = "/bin/df -T '" + basePath + "' | /usr/bin/awk '{print $2}' | tail -n1";
            fsType = runInOS(new String[] { "/bin/sh", "-c", cmd });

            Central.setProperty("fsType", fsType);
            logger.log(Level.INFO, "system is running on linux.");
            logger.log(Level.INFO, "cpu count is {0}", cpuCount);
            logger.log(Level.INFO, "memory size is {0}", memSize);
            logger.log(Level.INFO, "anystore path is {0}", basePath);
            logger.log(Level.INFO, "anystore filesystem type is {0}", fsType);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "os discovery failed", e);
        }
    }
}
