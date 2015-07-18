package io.trivium.glue.om;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import javolution.util.FastList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class Json {

	public static Element JsonToInternal(String in) {
		Element root = new Element("dummy");
		try {
			JsonReader reader = new JsonReader(new StringReader(in));
			LinkedList<Element> stack = new LinkedList<Element>();
			LinkedList<Element> arrayStack = new LinkedList<Element>();
			stack.push(root);
			boolean running = true;
			boolean isArray = false;
			while (running) {
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case NAME:
					String name = reader.nextName();
					Element child = new Element(name);
					Element cur = stack.peek();
					cur.addChild(child);
					stack.push(child);
					break;
				case END_OBJECT:
					reader.endObject();
    				stack.pop();
					break;
				case STRING:
					String val1 = reader.nextString();
					Element cur1 = stack.peek();
					if (isArray)
						cur1.addChild(new Element(null, val1));
					else {
						cur1.setValue(val1);
						stack.pop();
					}
					break;
				case NUMBER:
					String val2 = reader.nextString();
					Element cur2 = stack.peek();
					if (isArray) {
						Element cur3 = new Element(null, val2);
						cur3.addMetadata(new NVPair("type", "number"));
						cur2.addChild(cur3);
					} else {
						cur2.setValue(val2);
						stack.pop();
					}
					break;
				case END_DOCUMENT:
					running = false;
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
                    if(isArray){
                        isArray = true;
                        Element cur3 = stack.peek();
                        Element el = new Element(null,null);
                        cur3.addChild(el);
                        stack.push(el);
                        arrayStack.push(el);
                    }else {
                        isArray = true;
                        arrayStack.push(stack.peekFirst());
                    }
					break;
				case END_ARRAY:
					reader.endArray();
					stack.pop();
                    arrayStack.pop();
                    if(stack.peekFirst() == arrayStack.peekFirst()){
                        isArray=true;
                    }else {
                        isArray = false;
                    }
					break;
				case BOOLEAN:
					String val3 = reader.nextBoolean() ? "true" : "false";
					Element cur3 = stack.peek();
					if (isArray) {
						Element cur4 = new Element(null, val3);
						cur4.addMetadata(new NVPair("type", "boolean"));
						cur3.addChild(cur4);
					} else {
						cur3.setValue(val3);
						stack.pop();
					}
					break;
				case NULL:
					reader.nextNull();
					break;
				}
			}
			reader.close();
		} catch (Exception ex) {
            Logger log = LogManager.getLogger(Json.class);
			log.error("error in json serializer", ex);
		}
		return root;
	}

	public static String InternalToJson(Element el) {
		StringWriter sw = new StringWriter();
		JsonWriter jw = new JsonWriter(sw);
		try {
            ElementToWriter2(jw, el);
			jw.close();
		} catch (Exception ex) {
            Logger log = LogManager.getLogger(Json.class);
			log.error("error in json serializer",ex);
		}
		return sw.toString();
	}

	private static void ElementToWriter(JsonWriter jw, Element el) throws Exception {
		String name = el.getName();
		if(name!=null){
			//normal element
			jw.name(name);
			if (el.getValue() != null) {
				jw.value(el.getValue());
			} else {
				if(el.isArray()){
					jw.beginArray();
					for (Element e : el.getAllChildren()) {
						if (e.getMetadata().findValue("type") != null) {
							if (e.getMetadata().findValue("type")
									.equals("boolean"))
								jw.value(Boolean.parseBoolean(e.getValue()));
							else if (e.getMetadata().findValue("type")
									.equals("number"))
								jw.value(Double.parseDouble(e.getValue()));
							else
								jw.value(e.getValue());

						} else {
							//if no type use string
							jw.value(e.getValue());
						}
					}
					jw.endArray();
				}else{
				jw.beginObject();
				for (Element e : el.getAllChildren()) {
					ElementToWriter(jw, e);
				}
				jw.endObject();
				}
			}
		} else {
			//array entry
			if(el.isArray()){
				jw.value(el.getValue());
			}else{
				throw new Exception("element with no name found");
			}
		}
	}

    private static void ElementToWriter2(JsonWriter jw, Element el) throws Exception {
        Element currentElement = el;
        LinkedList<Element> stack = new LinkedList<Element>();
        LinkedList<Element> arrayStack = new LinkedList<Element>();
		currentElement.initReader();
        //skip root node, because it is a dummy
        currentElement.next();//begin_element
        stack.push(el);
        currentElement.next();//name
        boolean isArray=false;
        ElementToken.Type lastEvent = ElementToken.Type.NAME;
        while(currentElement.hasNext()) {
            ElementToken token = currentElement.next();
            switch (token.getType()) {
                case BEGIN_ELEMENT:
                    if(!isArray && token.getElement().getName()!=null && lastEvent!= ElementToken.Type.END_ELEMENT) {
                        jw.beginObject();
                    }
                    stack.push(token.getElement());
                    lastEvent = ElementToken.Type.BEGIN_ELEMENT;
                    break;
                case NAME:
                    if(!isArray && token.getElement().getName()!=null) {
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
                            jw.value(Double.valueOf(cur.getValue()));
                        } else {
                            jw.value(cur.getValue());
                        }
                    } else {
                        jw.value(cur.getValue());
                    }
                    break;
                case BEGIN_ARRAY:
                    jw.beginArray();
                    stack.push(token.getElement());
                    arrayStack.push(token.getElement());
                    isArray = true;
                    break;
                case CHILD:
                    //subnode
                    Element child = token.getElement();
                    child.initReader();
                    currentElement=child;
                    break;
                case END_ARRAY:
                    jw.endArray();
                    arrayStack.pop();
                    stack.pop();
                    if (stack.peekFirst() == arrayStack.peekFirst()) {
                        isArray = true;
                    } else {
                        isArray = false;
                    }
                    break;
                case END_ELEMENT:
                    //ignore root element
                    lastEvent = ElementToken.Type.END_ELEMENT;
                    if (stack.size() == 1) {
                        break;
                    } else {
                        if (!isArray && token.getElement().getName() != null) {
                            jw.endObject();
                        }
                        stack.pop();
                        currentElement = stack.peekFirst();
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
            Logger log = LogManager.getLogger(Json.class);
			log.error("exception thrown in json stringify",e);
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
            Logger log = LogManager.getLogger(Json.class);
			log.error("exception thrown in json parse",e);
		}
		return rslt;
	}
}
