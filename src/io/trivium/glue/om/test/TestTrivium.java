package io.trivium.glue.om.test;

import io.trivium.NVPair;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Trivium;

public class TestTrivium {

	public static void main(String[] args) {
		elem2Iup();
		
		iup2Elem();
		
		cycle();
	}

	private static void cycle() {
		Element e = new Element("root");
		e.addChild(new Element("sub1", "value1"));
		e.addChild(new Element("sub2", "value2"));
		
		Element complex = new Element("complex1");
		complex.setValue("value2");
		complex.addMetadata(new NVPair("meta1", "val1"));
		complex.addMetadata(new NVPair("meta1", "val2"));
		
		e.addChild(complex);
		String str  = Trivium.internalToTrivium(e);
		Element elem = Trivium.triviumToInternal(str);
		System.out.println(e.toString());
		System.out.println(elem.getChild(0).toString());
		/*
		Gson gs = new Gson();
		String output = gs.toJson(str);
		System.out.println(output);
		Element t = gs.fromJson(output, Element.class);
		System.out.println(t.toString());
		*/
	}

	public static void elem2Iup(){
		Element e = new Element("root");
		e.addChild(new Element("sub1", "value1"));
		e.addChild(new Element("sub2", "value2"));
		
		Element complex = new Element("complex1");
		complex.setValue("value2");
		complex.addMetadata(new NVPair("meta1", "val1"));
		complex.addMetadata(new NVPair("meta1", "val2"));
		
		e.addChild(complex);
		String str  = Trivium.internalToTrivium(e);
		System.out.println(str);
	}
	
	public static void iup2Elem(){
		String str ="{\"root\":{\"children\":[{\"sub1\":{\"value\":\"value1\"}},{\"sub2\":{\"value\":\"value2\"}},{\"complex1\":{\"metadata\":[{\"meta1\":\"val1\"},{\"meta1\":\"val2\"}],\"value\":\"value2\"}}]}}";
		Element elem = Trivium.triviumToInternal(str);
		System.out.println(elem.toString());
	}
}
