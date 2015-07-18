package io.trivium.glue.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import io.trivium.NVList;
import io.trivium.NVPair;

public class Element implements Iterator<ElementToken> {

    private String name;
    private String value;
    private NVList metadata = new NVList();
    private ArrayList<Element> children = new ArrayList<Element>();
    private LinkedList<ElementToken> structure = new LinkedList<>();

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

    public void initReader(){
        structure.clear();
        structure.push(new ElementToken(this, ElementToken.Type.END_ELEMENT));
        if(this.isArray()){
            structure.push(new ElementToken(this, ElementToken.Type.END_ARRAY));
            int size = this.children.size();
            for(int idx=size-1;idx>=0;idx--){
                Element child = this.children.get(idx);
                structure.push(new ElementToken(child, ElementToken.Type.CHILD));
            }
            structure.push(new ElementToken(this, ElementToken.Type.BEGIN_ARRAY));
        }else if(this.children.size()>0) {
            int size = this.children.size();
            for(int idx=size-1;idx>=0;idx--){
                Element child = this.children.get(idx);
                structure.push(new ElementToken(child, ElementToken.Type.CHILD));
            }
        }else{
            structure.push(new ElementToken(this, ElementToken.Type.VALUE));
        }
        structure.push(new ElementToken(this, ElementToken.Type.NAME));
        structure.push(new ElementToken(this, ElementToken.Type.BEGIN_ELEMENT));
    }

    public boolean hasNext(){
        return structure.size()>0;
    }

    public ElementToken next(){
        if(structure.size()>0)
            return structure.pop();
        else
            return null;
    }
}
