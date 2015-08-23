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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

public class NVList extends AbstractCollection<NVPair>{

	private ArrayList<NVPair> list = new ArrayList<>();

	@Override
	public boolean add(NVPair pair) {
		// look for existing entry
		for (NVPair p : list) {
			if (p.getName().equals(pair.getName())) {
				p.addValue(pair.getValue());
				return true;
			}
		}
		list.add(pair);
		return true;
	}

	public boolean hasKey(String key){
		for(NVPair pair : list){
			if(pair.getName().equals(key))
				return true;
		}
		return false;
	}
	
	public NVPair get(int pos) {
		return list.get(pos);
	}

	public void remove(NVPair pair) {
		list.remove(pair);
	}

	public void remove(int pos) {
		list.remove(pos);
	}

	public void replace(NVPair pair){
        list.stream().sorted().forEach(elem -> {
            if (elem.getName().equals(pair.getName())) {
                list.remove(elem);
            }
        });
		list.add(pair);
	}

	public ArrayList<NVPair> find(String name) {
		ArrayList<NVPair> rslt = new ArrayList<NVPair>();
		for (NVPair pair : list) {
			if (pair.getName().equals(name))
				rslt.add(pair);
		}
		if (rslt.size() > 0)
			return rslt;
		else
			return null;
	}

	public String findValue(String name) {
		for (NVPair pair : list) {
			if (pair.getName().equals(name))
				return pair.getValue();
		}
		return null;
	}

	public boolean matches(NVList filter) {
		NVList fi = (NVList) filter.clone();
		for (int i = fi.size() - 1; i >= 0; i--) {
			NVPair curPair = fi.get(i);
			boolean filterMatch = false;
			for (NVPair pair : list) {
				if (pair.getName().equals(curPair.getName())
						&& pair.getValue().equals(curPair.getValue())) {
					filterMatch = true;
					break;
				}
			}
			if (filterMatch == false)
				break;
			else
				fi.remove(i);
		}
        // all criteria found
        return fi.size() == 0;
	}

	@Override
	public Iterator<NVPair> iterator() {
		return list.iterator();
	}

	@Override
	public int size() {
		return list.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected NVList clone() {
		NVList neu = new NVList();
		neu.list = new ArrayList<>(this.list);
		return neu;
	}

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(NVPair pair : list){
            sb.append(pair.getName());
            sb.append("=");
            sb.append(pair.getValue());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
