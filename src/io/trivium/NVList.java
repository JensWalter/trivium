package io.trivium;

import javolution.util.FastList;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

public class NVList extends AbstractCollection<NVPair>{

	private FastList<NVPair> list;

	public NVList() {
list = new FastList<NVPair>();
		list.shared();
	}

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
		for(NVPair entry : list){
			if(entry.getName().equals(pair.getName())){
				list.remove(entry);
			}
		}
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
		neu.list = new FastList<NVPair>(this.list);
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
