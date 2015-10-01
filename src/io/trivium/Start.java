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

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import io.trivium.anystore.ObjectRef;
import io.trivium.dep.org.apache.commons.io.IOUtils;
import io.trivium.glue.binding.http.channel.ChannelConfig;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Start {

	public static void main(String[] args) throws Exception {
        if("--build".equals(args[0])){
            uuencode();
            System.exit(0);
        }
		Central.setup(args);
		Central.start();
		
		//create dummy json channel for testing
		ChannelConfig c = new ChannelConfig();
		c.id= ObjectRef.getInstance("1d26e2f1-7161-4d5c-b2ac-17ffb4f0a97d");
		c.name="dummy json channel";
		c.className="io.trivium.glue.binding.http.channel.JsonChannel";
		c.retention =4320000000L;//50 days in ms
		c.setTypeId(ObjectRef.getInstance("8a7d067a-0feb-4a7f-9636-1df269999cbb"));
		ChannelConfig.addConfig(c);

        //create dummy xml channel for testing
        c = new ChannelConfig();
        c.id= ObjectRef.getInstance("f34781e4-aa32-4d9b-ac61-96a7ccb3791f");
        c.name="dummy xml channel";
        c.className="io.trivium.glue.binding.http.channel.XmlChannel";
        c.retention =4320000000L;//50 days in ms
        c.setTypeId(ObjectRef.getInstance("05ca9d63-ae71-4ca8-99bb-0387aef53556"));
        ChannelConfig.addConfig(c);
	}

    public static void uuencode(){
        try {
            FileInputStream fis = new FileInputStream(new File("trivium.jar"));
            FileOutputStream fos = new FileOutputStream(new File("trivium.sh"));

            String head="#!/bin/bash\n" +
                    "uudecode -o trivium.jar $0\n" +
                    "java -jar trivium.jar\n" +
                    "exit\n\n";
            fos.write(head.getBytes());

            OutputStream os = MimeUtility.encode(fos, "uuencode");
            IOUtils.copy(fis,os);
            os.flush();
            os.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
