package io.trivium.anystore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AnyAbstract {
    Logger log = LogManager.getLogger(getClass());
    
    public String fileName;
    public String path;
    public String id;
    public byte[] idAsBytes;
    /**
     * valid values are "meta" or "data"
     */
    public String type;

    public abstract void put(byte[] key, byte[] value);

    public abstract byte[] get(byte[] key);

    public abstract void generate();

    public abstract AnyAbstract cloneStore();
}
