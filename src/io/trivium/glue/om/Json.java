package io.trivium.glue.om;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import javolution.util.FastList;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class Json {

    //FIXME json array in array doesn't work { "a": [ [ "b" ] ] }
	public static Element JsonToInternal(String in) {
		Element root = new Element("dummy");
		try {
			JsonReader reader = new JsonReader(new StringReader(in));
			LinkedList<Element> stack = new LinkedList<Element>();
			stack.push(root);
			boolean running = true;
			boolean isArray = false;
			while (running) {
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					reader.beginObject();
//					Central.logger.error("begin object "							+ stack.peek().getName());
					break;
				case NAME:
					String name = reader.nextName();
					Element child = new Element(name);
					Element cur = stack.peek();
					cur.addChild(child);
					stack.push(child);
	//				Central.logger.error("name " + stack.peek().getName());
					break;
				case END_OBJECT:
					reader.endObject();
    				stack.pop();
//					Central.logger
//							.error("end object " + stack.peek().getName());
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
//					Central.logger.error("string " + stack.peek().getName());
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
//					Central.logger.error("number " + stack.peek().getName());
					break;
				case END_DOCUMENT:
					running = false;
//					Central.logger.error("end document "
//							+ stack.peek().getName());
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
					isArray = true;
//					Central.logger.error("begin array "
//							+ stack.peek().getName());
					break;
				case END_ARRAY:
					reader.endArray();
					stack.pop();
					isArray = false;
//					Central.logger.error("end array " + stack.peek().getName());
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
//					Central.logger.error("boolean " + stack.peek().getName());
					break;
				case NULL:
					reader.nextNull();
//					Central.logger.error("null " + stack.peek().getName());
					break;
				}
			}
			reader.close();
		} catch (Exception ex) {
			Central.logger.error("error in json serializer",ex);
		}
		return root;
	}

	public static String InternalToJson(Element el) {
		StringWriter sw = new StringWriter();
		JsonWriter jw = new JsonWriter(sw);
		try {
			jw.beginObject();
			ArrayList<Element> children = el.getChildren();
			for (Element child : children) {
				ElementToWriter(jw, child);
			}
			jw.endObject();
			jw.close();
		} catch (Exception ex) {
			Central.logger.error("error in json serializer",ex);
		}
		return sw.toString();
	}

	private static void ElementToWriter(JsonWriter jw, Element el)
			throws Exception {
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
				throw new Exception("should not happen");
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
			Central.logger.error(e);
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
			Central.logger.error(e);
		}
		return rslt;
	}
}
