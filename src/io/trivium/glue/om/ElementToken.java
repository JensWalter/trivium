package io.trivium.glue.om;

public class ElementToken {
    enum Type {
        BEGIN_ELEMENT,
        END_ELEMENT,
        NAME,
        CHILD,
        VALUE,
        BEGIN_ARRAY,
        END_ARRAY
    }

    private Element element;
    private Type type;

    public ElementToken(Element el, Type t) {
        this.element = el;
        this.type = t;
    }

    public Element getElement() {
        return element;
    }

    public Type getType() {
        return type;
    }
}
