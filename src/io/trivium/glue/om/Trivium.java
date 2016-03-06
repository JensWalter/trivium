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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.trivium.dep.com.google.gson.stream.JsonReader;
import io.trivium.dep.com.google.gson.stream.JsonToken;
import io.trivium.dep.com.google.gson.stream.JsonWriter;
import io.trivium.NVList;
import io.trivium.NVPair;

public class Trivium {

	public static Element triviumJsonToElement(String in) {
		Element root = new Element("dummy");
		try {
			JsonReader reader = new JsonReader(new StringReader(in));

			tvm2Elem(root, reader);

			reader.close();
		} catch (Exception ex) {
            Logger logger = Logger.getLogger(Trivium.class.getName());
            logger.log(Level.SEVERE,"exception thrown while transforming object to external structure", ex);
		}
        //get first child after the dummy root
		return root.getChild(0);
	}

	public static String elementToTriviumJson(Element in) {
		String rslt = null;
		try {
			StringWriter sw = new StringWriter();
			JsonWriter writer = new JsonWriter(sw);
			writer.setIndent("  ");
			elem2Tvm(in, writer);

			writer.close();
			rslt = sw.toString();
		} catch (Exception ex) {
            Logger logger = Logger.getLogger(Trivium.class.getName());
            logger.log(Level.SEVERE,"exception thrown while transforming object to internal structure",ex);
		}
		return rslt;
	}

    private static void tvm2Elem(Element elem, JsonReader reader) throws IOException {
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
                    tvm2Elem(e, reader);
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

	private static void elem2Tvm(Element elem, JsonWriter writer) throws IOException {
		//TODO make this recursion free
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
				elem2Tvm(e, writer);
			}
			writer.endArray();
		}
		writer.endObject();

		// end elem
		writer.endObject();
	}
}
