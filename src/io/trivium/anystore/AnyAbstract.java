package io.trivium.anystore;

public abstract class AnyAbstract {
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
