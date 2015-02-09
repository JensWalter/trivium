package io.trivium.glue.om;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;

public class Infinup {

	public static Element infiniupToInternal(String in) {
		Element root = new Element("dummy");
		try {
			JsonReader reader = new JsonReader(new StringReader(in));

			iup2Elem(root, reader);

			reader.close();
		} catch (Exception ex) {
			Central.logger.error(ex);
		}
		return root;
	}

	public static String internalToInfiniup(Element in) {
		String rslt = null;
		try {
			StringWriter sw = new StringWriter();
			JsonWriter writer = new JsonWriter(sw);
			writer.setIndent("  ");
			elem2Iup(in, writer);

			writer.close();
			rslt = sw.toString();
		} catch (Exception ex) {
			Central.logger.error(ex);
		}
		return rslt;
	}

	private static void iup2Elem(Element elem, JsonReader reader)
			throws IOException {
		// get root name
		reader.beginObject();
		String name = reader.nextName();

		Element e = new Element(name);
		elem.addChild(e);
		reader.beginObject();
		while (reader.peek() == JsonToken.NAME) {
			
			String obj = reader.nextName();
			// can either be 'value','children' or 'metadata'
			if (obj.equals("value")) {
				// TODO other types then string
				e.setValue(reader.nextString());
			}
			if (obj.equals("children")) {
				// process children
				reader.beginArray();
				while (reader.peek() != JsonToken.END_ARRAY) {
					iup2Elem(e, reader);
				}
				reader.endArray();
			}
			if (obj.equals("metadata")) {
				reader.beginArray();
				NVList list = new NVList();
				while (reader.peek() != JsonToken.END_ARRAY) {
					reader.beginObject();
					String n = reader.nextName();
					String v = reader.nextString();
					list.add(new NVPair(n, v));
					reader.endObject();
				}
				e.setMetadataList(list);

				reader.endArray();	
			}
		}
			reader.endObject();
		
		reader.endObject();
	}

	private static void elem2Iup(Element elem, JsonWriter writer)
			throws IOException {
		// begin elem
		writer.beginObject();
		writer.name(elem.getName());
		writer.beginObject();
		// begin metadata
		if (elem.getMetadata().size() > 0) {
			writer.name("metadata");

			writer.beginArray();
			for (NVPair pair : elem.getMetadata()) {
				writer.beginObject();
				writer.name(pair.getName());
				writer.value(pair.getValue());
				writer.endObject();
			}
			writer.endArray();
		}

		// writing value
		if (elem.getValue() != null) {
			writer.name("value");
			writer.value(elem.getValue());
		}

		// writing children
		if (elem.getAllChildren().length > 0) {
			writer.name("children");
			writer.beginArray();
			for (Element e : elem.getAllChildren()) {
				elem2Iup(e, writer);
			}
			writer.endArray();
		}
		writer.endObject();

		// end elem
		writer.endObject();
	}
}
