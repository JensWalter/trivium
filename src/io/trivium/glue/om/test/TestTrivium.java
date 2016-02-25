/*
 * Copyright 2016 Jens Walter
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
		String str  = Trivium.elementToTriviumJson(e);
		Element elem = Trivium.triviumJsonToElement(str);
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
		String str  = Trivium.elementToTriviumJson(e);
		System.out.println(str);
	}
	
	public static void iup2Elem(){
		String str ="{\"root\":{\"children\":[{\"sub1\":{\"value\":\"value1\"}},{\"sub2\":{\"value\":\"value2\"}},{\"complex1\":{\"metadata\":[{\"meta1\":\"val1\"},{\"meta1\":\"val2\"}],\"value\":\"value2\"}}]}}";
		Element elem = Trivium.triviumJsonToElement(str);
		System.out.println(elem.toString());
	}
}
