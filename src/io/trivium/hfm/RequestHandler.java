package io.trivium.hfm;

import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.InfiniObject;
import io.trivium.anystore.AnyServer;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.glue.InfiniObject;
import javolution.util.FastList;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class RequestHandler implements HttpAsyncRequestHandler<HttpRequest>{

	@Override
	public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
		HttpResponse response = httpexchange.getResponse();

		//find hfm related headers
		Header[] headers = request.getAllHeaders();
		String contentType = "";
		NVList list = new NVList();
		for(Header header : headers){
			String name = header.getName();
			if(name.startsWith("hfm:")){
				list.add(new NVPair(name.substring(3), header.getValue()));
			}
			if(name.equalsIgnoreCase("Content-Type")){
				contentType=header.getValue();
			}
		}
		//switch request method
		String method = request.getRequestLine().getMethod();
		if(method.equals("GET")){
			//read from store
            Query q = new Query();
            for(NVPair pair:list){
                q.criteria.add(new Value(pair.getName(), pair.getValue()));
            }
			FastList<InfiniObject> all = AnyServer.INSTANCE.getStore().loadObjects(q);
			if(all.size()>0){
				InfiniObject po = all.get(0);
				String type = po.findMetaValue("contentType");
			}
		}
		if(method.equals("POST") || method.equals("PUT")){
			//write to store
			String uri =request.getRequestLine().getUri();
		}
		
		 
			
			StringEntity entity = new StringEntity(new String("TEST"),ContentType.create(contentType,"UTF-8"));
//                   ContentType.create("text/html", "UTF-8"));
           response.setEntity(entity);
			response.setStatusCode(HttpStatus.SC_OK);
			
		 
		 //handleInternal(request, response, context);
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
	}

	@Override
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest arg0, HttpContext arg1) throws HttpException, IOException {
		return new BasicAsyncRequestConsumer();
	}

}
