package io.trivium.anystore.query;

import io.trivium.anystore.AnyIndex;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.TriviumObject;
import javolution.util.FastList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class QueryExecutor implements Supplier<TriviumObject> {
    Logger log = LogManager.getLogger(getClass());
    
    ObjectRef id;
    Query query;
    FastList<ObjectRef> keys = new FastList<ObjectRef>();
    Iterator<ObjectRef> iterator;

    public QueryExecutor(Query query) {
        id = query.id;
        this.query = query;
    }

    /**
     * runs the query an return, whether there is a resultset
     */
    public boolean execute() {
        boolean mayExist = true;
        FastList<String> key = new FastList<String>();
        for (Criteria crit : query.criteria) {
            if (crit instanceof Value) {
                Value val = (Value) crit;
                //check for result probability
                boolean returnCode = AnyIndex.check(val.getName(), val.getValue());
                if (returnCode == false)
                    mayExist = false;
                else {
                    key.add(val.getName());
                }
            }
        }
        if (!mayExist) {
//            Central.logger.debug("index miss for query {}", query.id.toString());
            return false;
        } else {
            //sequential index scan
            String indexName = "typeId";
            for (int i = 0; i < key.size(); i++) {
                if (AnyIndex.getVariance(key.get(i)) > AnyIndex.getVariance(indexName)) {
                    indexName = key.get(i);
                }
            }
            String value = query.getValueForName(indexName);
            Stream<ObjectRef> stream = AnyIndex.lookup(indexName, value);
            stream.forEach(e -> keys.add(e));
            iterator = keys.iterator();
            return true;
        }
    }

    public int getSize() {
        return keys.size();
    }

    public TriviumObject get() {
        if (iterator.hasNext()) {
            ObjectRef ref = iterator.next();
            try {
                TriviumObject po = AnyServer.INSTANCE.getStore().loadObject(ref);
                //check for correct value
                boolean valid = true;
                for (Criteria crit : query.criteria) {
                    if (crit instanceof Value) {
                        Value val = (Value) crit;
                        if(! (po.hasMetaKey(val.getName()) &&
                                po.findMetaValue(val.getName()).equals(val.getValue()))){
                            valid=false;
                        }
                    }else
                    if(crit instanceof Range) {
                        Range range = (Range) crit;
                        if(range.getRangeOption() == RangeType.within){
                            if( po.hasMetaKey(range.getName())){
                                String value = po.findMetaValue(range.getName());
                                //check for int type
                                try {
                                    double d_value = Double.parseDouble(value);
                                }catch(NumberFormatException nfe){
                                    log.debug("looking for number, but value is not convertible",nfe);
                                }
                            }
                        }
                    }
                }
                if(valid) {
                    return po;
                } else {
                    return get();
                }
            } catch (Exception ex) {
                log.error("ignoring error while loading object", ex);
            }
        }
        return null;
    }
}
