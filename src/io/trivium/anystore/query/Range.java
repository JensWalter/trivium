package io.trivium.anystore.query;

public class Range implements Criteria {
    private String name;
    private String value;
    private RangeType rangeOption;

    public Range(String name, String value, RangeType rangeOption){
        this.name=name;
        this.value=value;
        this.rangeOption=rangeOption;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public RangeType getRangeOption() {
        return rangeOption;
    }
}
