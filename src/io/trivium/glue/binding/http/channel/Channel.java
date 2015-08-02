package io.trivium.glue.binding.http.channel;

import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.ObjectRef;
import javolution.util.FastMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
                Logger log = LogManager.getLogger(Channel.class);
				log.error(e);
			}
		}
		return c;
	}
	public abstract void process(Session session,ObjectRef sourceId) throws Exception;
}
