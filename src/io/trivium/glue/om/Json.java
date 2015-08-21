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

package io.trivium.glue.om;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.trivium.NVList;
import io.trivium.NVPair;
import javolution.util.FastList;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Json {

	public static Element jsonToElement(String in) {
		Element root = new Element("dummy");
		try {
			JsonReader reader = new JsonReader(new StringReader(in));
			LinkedList<Element> stack = new LinkedList<Element>();
			LinkedList<Element> arrayStack = new LinkedList<Element>();
			stack.push(root);
			boolean running = true;
			while (running) {
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					reader.beginObject();
//                    System.out.println("beginObject");
                    break;
				case NAME:
					String name = reader.nextName();
					Element child = new Element(name);
					Element cur = stack.peek();
                    if(cur == arrayStack.peek()){
                        //is array
                        Element inner = new Element(null,null);
                        inner.addChild(child);
                        cur.addChild(inner);
                        stack.push(inner);
                        stack.push(child);
                    }else {
                        cur.addChild(child);
                        stack.push(child);
                    }
//                    System.out.println("name");
                    break;
				case END_OBJECT:
					reader.endObject();
                    stack.pop();
//                    System.out.println("endObject");
                    break;
				case STRING:
					String val1 = reader.nextString();
					Element cur1 = stack.peek();
					if (cur1==arrayStack.peek())
						cur1.addChild(new Element(null, val1));
					else {
						cur1.setValue(val1);
						stack.pop();
					}
//                    System.out.println("string");
					break;
				case NUMBER:
					String val2 = reader.nextString();
					Element cur2 = stack.peek();
					if (cur2==arrayStack.peek()) {
						Element cur3 = new Element(null, val2);
						cur3.addMetadata(new NVPair("type", "number"));
						cur2.addChild(cur3);
					} else {
						cur2.setValue(val2);
						stack.pop();
					}
//                    System.out.println("number");
					break;
				case END_DOCUMENT:
					running = false;
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
                    if(stack.peek()==arrayStack.peek()){
                        Element cur3 = stack.peek();
                        Element el = new Element(null,null);
                        cur3.addChild(el);
                        stack.push(el);
                        arrayStack.push(el);
                    }else {
                        arrayStack.push(stack.peek());
                    }
//                    System.out.println("beginArray");
					break;
				case END_ARRAY:
					reader.endArray();
					stack.pop();
                    arrayStack.pop();
//                    System.out.println("endArray");
					break;
				case BOOLEAN:
					String val3 = reader.nextBoolean() ? "true" : "false";
					Element cur3 = stack.peek();
					if (stack.peek()==arrayStack.peek()) {
						Element cur4 = new Element(null, val3);
						cur4.addMetadata(new NVPair("type", "boolean"));
						cur3.addChild(cur4);
					} else {
						cur3.setValue(val3);
						stack.pop();
					}
//                    System.out.println("boolean");
					break;
				case NULL:
					reader.nextNull();
//                    System.out.println("null");
					break;
				}
			}
			reader.close();
		} catch (Exception ex) {
            Logger log = Logger.getLogger(Json.class.getName());
			log.log(Level.SEVERE,"error in json serializer", ex);
		}
		return root;
	}

	public static String elementToJson(Element el) {
		StringWriter sw = new StringWriter();
		JsonWriter jw = new JsonWriter(sw);
		try {
            ElementToWriter(jw, el);
			jw.close();
		} catch (Exception ex) {
            Logger log = Logger.getLogger(Json.class.getName());
			log.log(Level.SEVERE, "error in json serializer", ex);
		}
		return sw.toString();
	}

    private static void ElementToWriter(JsonWriter jw, Element el) throws Exception {
        Element currentElement = el;
        LinkedList<Element> stack = new LinkedList<Element>();
        LinkedList<Element> arrayStack = new LinkedList<Element>();
		currentElement.initReader();
        //skip root node, because it is a dummy
        currentElement.next();//begin_element
        stack.push(el);
        currentElement.next();//name
        ElementToken lastToken = null;
        while(currentElement.hasNext()) {
            ElementToken token = currentElement.next();
            switch (token.getType()) {
                case BEGIN_ELEMENT:
                    //new start
                    if(lastToken ==null){
//                        System.out.println("beginObject "+token.getElement().hashCode());
                        jw.beginObject();
                    }else
                    //is not array and is not sibling
                    if(stack.peek() != arrayStack.peek() && token.getElement().getName()!=null
                        && lastToken!=null && lastToken.getElement().getParent()!=token.getElement().getParent()){
//                        System.out.println("beginObject "+token.getElement().hashCode());
                        jw.beginObject();
                    }
                    stack.push(token.getElement());
                    lastToken = token;
                    break;
                case NAME:
                    if(stack.peek() != arrayStack.peek() && token.getElement().getName()!=null) {
//                        System.out.println("name "+token.getElement().hashCode());
                        jw.name(token.getElement().getName());
                    }
                    break;
                case VALUE:
                    Element cur = token.getElement();
                    if (cur.getMetadata().hasKey("type")) {
                        String type = cur.getMetadata().findValue("type");
                        if (type.equals("boolean")) {
                            jw.value(Boolean.valueOf(cur.getValue()));
                        } else if (type.equals("number")) {
                            Double val1 = Double.valueOf(cur.getValue());
                            if(val1.doubleValue() % 1 != 0){
                                jw.value(val1.doubleValue());
                            }else{
                                jw.value(val1.longValue());
                            }
                        } else {
                            jw.value(cur.getValue());
                        }
                    } else {
                        jw.value(cur.getValue());
//                        System.out.println("value "+token.getElement().hashCode());
                    }
                    break;
                case BEGIN_ARRAY:
//                    System.out.println("beginArray "+token.getElement().hashCode());
                    jw.beginArray();
                    stack.push(token.getElement());
                    arrayStack.push(token.getElement());
                    lastToken = token;
                    break;
                case CHILD:
                    //subnode
                    Element child = token.getElement();
                    child.initReader();
                    currentElement=child;
                    break;
                case END_ARRAY:
                    jw.endArray();
//                    System.out.println("endArray "+token.getElement().hashCode());
                    arrayStack.pop();
                    stack.pop();
                    lastToken = token;
                    break;
                case END_ELEMENT:
                    //ignore root element
                    lastToken = token;
//                    System.out.println("endObject "+token.getElement().hashCode());
                    if (stack.size() == 1) {
                        break;
                    } else {
                        if (stack.peek() != arrayStack.peek() && token.getElement().getName() != null
                                && token.getElement().getParent().isLast(token.getElement())) {
                            jw.endObject();
                        }
                        stack.pop();
                        currentElement = stack.peek();
                        break;
                    }
            }
        }
    }

	public static String NVPairsToJson(NVList in) {
		String rslt = "";
		try {
			StringWriter sw = new StringWriter();
			JsonWriter writer = new JsonWriter(sw);
			writer.setIndent("  ");
			writer.beginObject();
			for (NVPair pair : in) {
				writer.name(pair.getName());
				if (pair.isArray()) {
					// array
					writer.beginArray();
					FastList<String> vals = pair.getValues();
					for (String s : vals) {
						writer.value(s);
					}
					writer.endArray();
				} else {
					// single value
					writer.value(pair.getValue());

				}
			}
			writer.endObject();
			writer.close();
			rslt = sw.toString();
		} catch (Exception e) {
            Logger log = Logger.getLogger(Json.class.getName());
			log.log(Level.SEVERE, "exception thrown in json stringify", e);
		}
		return rslt;
	}

	public static NVList JsonToNVPairs(String in) {
		NVList rslt = new NVList();
		try {
			StringReader sr = new StringReader(in);
			JsonReader reader = new JsonReader(sr);
			reader.beginObject();
			NVPair current = null;
			boolean running = true;
			while (running) {
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case NAME:
					String name = reader.nextName();
					current = new NVPair(name, (String) null);
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case STRING:
				case NUMBER:
					String val1 = reader.nextString();
					current.setValue(val1);
					rslt.add(current);
					break;
				case END_DOCUMENT:
					running = false;
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
					break;
				case END_ARRAY:
					reader.endArray();
					break;
				case BOOLEAN:
					String val3 = reader.nextBoolean() ? "true" : "false";
					current.setValue(val3);
					rslt.add(current);
					break;
				case NULL:
					reader.nextNull();
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
            Logger log = Logger.getLogger(Json.class.getName());
			log.log(Level.SEVERE,"exception thrown in json parse",e);
		}
		return rslt;
	}
}
