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

import com.google.gson.Gson;
import io.trivium.anystore.ObjectRef;
import io.trivium.extension.type.Typed;
import javolution.util.FastMap;

public class ChannelConfig implements Typed{
	
	private static FastMap<ObjectRef, ChannelConfig> knownConfigs = new FastMap<ObjectRef, ChannelConfig>().shared();

	public ObjectRef id;
	private ObjectRef typeId;
	public String name;
	public long retention =432000000;//5 days in ms
	public String className;
	
	public static ChannelConfig getConfig(ObjectRef id){
		if(knownConfigs.containsKey(id)){
			return knownConfigs.get(id);
		} else {
			//TODO retrieve config
			return null;
		}
	}
	
	public static void addConfig(ChannelConfig config){
		knownConfigs.put(config.id, config);
		//TODO publish object to all nodes
	}

	@Override
	public ObjectRef getTypeId(){
		return typeId;
	}

	public void setTypeId(ObjectRef newType){
		typeId=newType;
	}

	public String toJson(){
		Gson gson = new Gson();
		String config = gson.toJson(this);
		return config;
	}
	public static ChannelConfig fromJson(String json){
		Gson gson = new Gson();
		return gson.fromJson(json, ChannelConfig.class);
	}
}
