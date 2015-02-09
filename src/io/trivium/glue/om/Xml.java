package io.trivium.glue.om;

import java.io.StringReader;
import java.util.LinkedList;

import io.trivium.Central;
import io.trivium.NVPair;

import javolution.text.CharArray;
import javolution.xml.XMLObjectReader;
import javolution.xml.sax.Attributes;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;

public class Xml {

	public static Element transformXml(String in){
		Element root=new Element("dummy");
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.push(root);
		XMLObjectReader xor = new XMLObjectReader();
		try {
			xor.setInput(new StringReader(in));
			XMLStreamReaderImpl xsr = (XMLStreamReaderImpl) xor.getStreamReader();
			
			while(xsr.hasNext()){
				int i = xsr.next();
				if(i==XMLStreamConstants.START_ELEMENT){
					//start
					String name = xsr.getLocalName().toString();
					Element child = new Element(name);
					CharArray ca = xsr.getNamespaceURI();
					if(ca!=null && ca.length()>0){
						String ns = ca.toString();
						NVPair p = new NVPair("xml:ns",ns);
						child.addMetadata(p);
					}
					int ac = xsr.getAttributeCount();
					if(ac>0){
						Attributes atts = xsr.getAttributes();
						for(int idx=0;idx<ac;idx++){
							Element e =new Element(atts.getLocalName(idx).toString());
							e.setValue(atts.getValue(idx).toString());
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
			Central.logger.error(e);
		}
		return root;
	}
}
