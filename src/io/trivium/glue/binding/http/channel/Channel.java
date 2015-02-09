package io.trivium.glue.binding.http.channel;

import io.trivium.Central;
import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.binding.http.Session;
import javolution.util.FastMap;

public abstract class Channel {

	protected static FastMap<ObjectRef, Channel> all = new FastMap<ObjectRef, Channel>().shared();
	final protected ChannelConfig config;
	
	public Channel(ObjectRef id){
		config = ChannelConfig.getConfig(id);
	}
	

	public static Channel getChannel(ObjectRef id){
		Channel c = all.get(id);
		if(c==null){
			ChannelConfig config = ChannelConfig.getConfig(id);
			try {
				Class<?> t = Class.forName(config.className);
				c = (Channel) t.getDeclaredConstructor(ObjectRef.class).newInstance(id);
				all.put(id, c);
			} catch (Exception e) {
				Central.logger.error(e);
			}
		}
		return c;
	}
	public abstract void process(Session session,ObjectRef sourceId) throws Exception;
}
