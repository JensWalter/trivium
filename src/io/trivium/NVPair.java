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

import java.util.ArrayList;

public class NVPair {

	private String name;
	private ArrayList<String> value = new ArrayList<String>();

	public NVPair() {

	}

	public NVPair(String name){
		this.name = name;
	}

	public NVPair(String name, String value) {
		this.name = name;
		this.value.add(value);
	}

	public NVPair(String name, ArrayList<String> value) {
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

	public ArrayList<String> getValues() {
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
