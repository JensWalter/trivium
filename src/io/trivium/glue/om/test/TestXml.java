package io.trivium.glue.om.test;

import io.trivium.glue.om.Element;
import io.trivium.glue.om.Xml;

public class TestXml {

	public static void main(String[] args) {
		simpleXml();
		xmlWithNameSpace();
		xmlWithAttribute();
	}

	public static void simpleXml(){
		String str= "<a><b>blah</b><c>blah2</c></a>";
		Element root = Xml.xmlToElement(str);
		
		System.out.println(root.toString());
	}
	
	public static void xmlWithNameSpace(){
		String str= "<a xmlns=\"urn://ns1\" xmlns:b=\"urn://urn2\"><b>blah</b><b:c>blah2</b:c></a>";
		Element root = Xml.xmlToElement(str);
		
		System.out.println(root.toString());
	}
	public static void xmlWithAttribute(){
		String str= "<a xmlns=\"urn://ns1\" xmlns:b=\"urn://urn2\"><b>blah</b><b:c a='b'>blah2</b:c></a>";
		Element root = Xml.xmlToElement(str);
		
		System.out.println(root.toString());
	}
}
