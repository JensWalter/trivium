/*
 * Copyright 2015 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
