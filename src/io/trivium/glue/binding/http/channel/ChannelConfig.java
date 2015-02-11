package io.trivium.glue.binding.http.channel;

import com.google.gson.Gson;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.ObjectType;
import io.trivium.extension.type.Typed;
import javolution.util.FastMap;

public class ChannelConfig implements Typed{
	
	private static FastMap<ObjectRef, ChannelConfig> knownConfigs = new FastMap<ObjectRef, ChannelConfig>().shared();

	public ObjectRef id;
	private ObjectType typeId;
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
	public ObjectType getTypeId(){
		return typeId;
	}

	public void setTypeId(ObjectType newType){
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
