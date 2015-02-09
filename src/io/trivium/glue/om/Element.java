package io.trivium.glue.om;

import java.util.ArrayList;

import io.trivium.NVList;
import io.trivium.NVPair;

public class Element {

    private String name;
    private String value;
    private NVList metadata = new NVList();
    private ArrayList<Element> children = new ArrayList<Element>();

    public static Element EMPTY = new Element("");

    public Element(String name) {
        this.name = name;
    }

    public Element(String name, String val) {
        this.name = name;
        this.value = val;
    }

    public ArrayList<Element> getChildren() {
        return children;
    }

    public Element getChild(int i) {
        Element child = children.get(i);
        if(child!=null){
            return child;
        }else{
            return EMPTY;
        }
    }

    public Element getFirstChild(String name) {
        for(Element el : children){
            if(el.getName().equals(name)){
                return el;
            }
        }
        return EMPTY;
    }

    public Element[] getAllChildren() {
        return children.toArray(new Element[children.size()]);
    }

    public boolean hasChild(String name){
        for(Element el : children){
            if(el.getName().equals(name))
                return true;
        }
        return false;
    }

    public void addChild(Element child) {
        this.children.add(child);
    }

    public void addMetadata(NVPair pair) {
        metadata.add(pair);
    }

    public void setMetadataList(NVList list) {
        metadata = list;
    }

    public NVList getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        this.value = val;
    }

    public boolean isArray() {
        //check children for array
        if (children.size() > 0 && children.get(0).name == null)
            return true;
        else
            return false;
    }

    public String toString() {

        String str = name;
        if (metadata.size() > 0) {
            str += " [";
            for (NVPair pair : metadata) {
                str += pair.getName() + "::" + pair.getValue() + ", ";
            }
            str = str.substring(0, str.length() - 2);
            str += "]";
        }
        str += " => ";
        if (value != null && value.length() > 0) {
            str += "{";
            str += "\"" + value + "\"";

            str += "}, ";
        }
        if (!children.isEmpty()) {
            str += "{";
            for (Element el : children) {
                str += el.toString() + ", ";
            }
            str = str.substring(0, str.length() - 2);

            str += "}, ";
        }
        str = str.substring(0, str.length() - 2);
        return str;
    }
}
