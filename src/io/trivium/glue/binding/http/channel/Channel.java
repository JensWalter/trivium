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

package io.trivium.glue.binding.http.channel;

import io.trivium.glue.binding.http.Session;
import io.trivium.anystore.ObjectRef;
import javolution.util.FastMap;

import java.util.logging.Level;
import java.util.logging.Logger;

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
                Logger log = Logger.getLogger(Channel.class.getName());
				log.log(Level.SEVERE,"error while retrieving chanel definition",e);
			}
		}
		return c;
	}
	public abstract void process(Session session,ObjectRef sourceId) throws Exception;
}
