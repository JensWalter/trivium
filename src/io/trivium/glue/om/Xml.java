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

package io.trivium.glue.om;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.trivium.NVPair;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class Xml {

	public static Element xmlToElement(String in){
		Element root=new Element("dummy");
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.push(root);
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
		try {
            XMLStreamReader xsr = xmlif.createXMLStreamReader(new StringReader(in));

            while(xsr.hasNext()){
				int i = xsr.next();
				if(i== XMLStreamConstants.START_ELEMENT){
					//start
					String name = xsr.getLocalName().toString();
					Element child = new Element(name);
					String ca = xsr.getNamespaceURI();
					if(ca!=null && ca.length()>0){
						String ns = ca.toString();
						NVPair p = new NVPair("xml:namespace",ns);
						child.addMetadata(p);
					}
					int ac = xsr.getAttributeCount();
					if(ac>0){
						for(int idx=0;idx<ac;idx++){
							Element e =new Element(xsr.getAttributeLocalName(idx));
                            e.addMetadata(new NVPair("xml:attribute","true"));
							e.setValue(xsr.getAttributeValue(idx));
							child.addChild(e);
						}
					}
					Element cur = stack.peek();
					cur.addChild(child);
					stack.push(child);
				}
				if(i==XMLStreamConstants.CHARACTERS){
					String val = xsr.getText().toString();
					Element cur = stack.peek();
					cur.setValue(val);
				}
				if(i==XMLStreamConstants.END_ELEMENT){
					//end
					stack.pop();
				}
			}
		} catch (XMLStreamException e) {
            Logger logger = Logger.getLogger(Xml.class.getName());
            logger.log(Level.SEVERE,"error while converting xml to internal structure",e);
		}
		return root;
	}

    public static String elementToString(Element el){
        StringWriter sw = new StringWriter();
        Element root = el.getChild(0);
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter xtw = xof.createXMLStreamWriter(sw);
            xtw.writeStartDocument();
            xtw.writeStartElement(root.getName());
            //check for attribute
            if(root.getChildren().size()>0){
                for(Element child : root.getChildren()){
                    if(child.getMetadata().hasKey("xml:attribute")){
                        xtw.writeAttribute(child.getName(),child.getValue());
                    }
                }
            }
            xtw.writeEndElement();
            xtw.writeEndDocument();
        } catch (XMLStreamException e) {
            Logger logger = Logger.getLogger(Xml.class.getName());
            logger.log(Level.SEVERE, "error while converting internal structure to xml", e);
        }
        return sw.toString();
    }
}
