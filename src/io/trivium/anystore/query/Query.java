package io.trivium.anystore.query;

import io.trivium.anystore.ObjectRef;
import javolution.util.FastList;

public class Query {
    public ObjectRef id = ObjectRef.getInstance();
    public FastList<Criteria> criteria = new FastList<Criteria>();

    public String getValueForName(String name) {
        for (Criteria c : criteria) {
            if (c instanceof Value) {
                Value v = (Value) c;
                if (v.getName().equals(name)) {
                    return v.getValue();
                }
            }
            if(c instanceof Range) {
                Range r = (Range) c;
                if(r.getName().equals(name)) {
                    return r.getValue();
                }
            }
        }
        return null;
    }
}
