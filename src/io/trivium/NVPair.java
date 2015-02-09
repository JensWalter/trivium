package io.trivium;

import javolution.util.FastList;

import java.io.Serializable;
import java.util.ArrayList;

public class NVPair {

	private String name;
	private FastList<String> value = new FastList<String>();

	public NVPair() {

	}

	public NVPair(String name){
		this.name = name;
	}

	public NVPair(String name, String value) {
		this.name = name;
		this.value.add(value);
	}

	public NVPair(String name, FastList<String> value) {
		this.name = name;
		this.value = value;
	}

	public boolean isArray() {
		return value.size() > 1;
	}

	public String getValue() {
		if (value.size() > 0)
			return value.get(0);
		else
			return null;
	}

	public FastList<String> getValues() {
		return value;
	}

	public void setValue(String val) {
		this.value.clear();
		this.value.add(val);
	}

	public void addValue(String val) {
		this.value.add(val);
	}

	public String getName() {
		return name;
	}
}
