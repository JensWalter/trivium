package io.trivium.glue.binding.http.channel;

import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.glue.om.Xml;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.Date;

public class XmlChannel extends Channel {

	public XmlChannel(ObjectRef id) {
		super(id);
	}

	@Override
	public void process(Session session, ObjectRef sourceId) throws Exception {
		// read payload
		if (session.getRequest() instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest) session.getRequest();

            String requestData = HttpUtils.getInputAsString(r);

			//construct persistence object
			TriviumObject po = new TriviumObject();

			po.addMetadata("contentType", "application/trivium.io");

			// if header starts with trivium - copy value
			Header[] headers = session.getRequest().getAllHeaders();
			for (Header h : headers) {
				if (h.getName().startsWith("trivium-")) {
					po.addMetadata(h.getName().substring(8), h
							.getValue());
				}
			}

			//setting channel data
			//ttl -> stale after retention
			po.addMetadata("stale", String.valueOf(new Date().getTime() + config.retention));
			//type = object
			po.addMetadata("type","object");
			
			// parse the payload
			Element el = Xml.xmlToElement(requestData);
			

			po.setData(el);
			po.setTypeId(config.getTypeId());

			AnyClient.INSTANCE.storeObject(po);
			
			session.ok();
		}
	}

}
