package io.trivium.anystore.query;

public class Value implements Criteria {
    private String name;
    private String value;

    public Value(String name, String value){
        this.name=name;
        this.value=value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
