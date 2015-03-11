package io.trivium;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.binding.http.channel.ChannelConfig;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Start {

	public static void main(String[] args) throws Exception {
		Central.setup(args);

		
//		Central.setProperty("webInterface", "true");
//		Central.setProperty("autoIncrementPort", "true");
		
		Central.start();
		
		//create dummy channel for testing
		ChannelConfig c = new ChannelConfig();
		c.id= ObjectRef.getInstance("1d26e2f1-7161-4d5c-b2ac-17ffb4f0a97d");
		c.name="dummy channel";
		c.className="JsonChannel";
		c.retention =4320000000L;//50 days in ms
		c.setTypeId(ObjectRef.getInstance("8a7d067a-0feb-4a7f-9636-1df269999cbb"));
		ChannelConfig.addConfig(c);

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();

//		ClassLoader cl = new ClassLoader();
//		Class<?> c = cl.loadClass("io.trivium.binding.http.Node");
//		Object o = c.newInstance();
//		Method m =c.getMethod("start");
//		m.invoke(o);
		
		
		
		//Node httpNode = new Node();
		//httpNode.start();
	}

}
