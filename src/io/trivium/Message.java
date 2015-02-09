package io.trivium;

import io.trivium.glue.om.Element;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;

public class Message {

	public NVList header= new NVList();
	public Element body;
	public ObjectRef id;
	public String sourceId;
	
	public NVList getHeader() {
		return header;
	}

	public void setHeader(NVList header) {
		this.header = header;
	}

	public Element getBody() {
		return body;
	}

	public void setBody(Element body) {
		this.body = body;
	}

	public ObjectRef getId() {
		return id;
	}

	public void setId(ObjectRef id) {
		this.id = id;
	}
	
	public Message genId(){
		this.id = ObjectRef.getInstance();
		return this;
	}
	
	public void transferTo(Message m){
		m.id=this.id;
		m.header=this.header;
		m.body=this.body;
		m.sourceId=this.sourceId;
	}
}
