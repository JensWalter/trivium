package io.trivium.glue.binding.http;

import io.trivium.Central;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.statics.ContentTypes;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebUIRequestHandler implements HttpAsyncRequestHandler<HttpRequest>{
    Logger log = LogManager.getLogger(getClass());

	@Override
	public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
		 HttpResponse response = httpexchange.getResponse();
         Session s = new Session(request, httpexchange, context, ObjectRef.getInstance());
		 //Header[] headers = request.getAllHeaders();
		 String origURI =request.getRequestLine().getUri();
		 String uri = origURI;
		 if(uri.equals("/ui/"))
			 uri= "io/trivium/webui/index.html";
		 else
			 uri= "io/trivium/webui"+uri.substring(3);
		 log.debug("receiving request for uri: {} => {}",origURI,uri);
		 ClassLoader cl = ClassLoader.getSystemClassLoader();
        try{
            Class<?> clazz = cl.loadClass(uri.replace('/','.'));
            Class<?>[] interfaces = clazz.getInterfaces();
            for(Class<?> iface : interfaces){
                if (iface.getCanonicalName().equals("org.apache.http.nio.protocol.HttpAsyncRequestHandler")) {
                    //is request handler
                    HttpAsyncRequestHandler handler = (HttpAsyncRequestHandler) clazz.newInstance();
                    handler.handle(request, httpexchange, context);
                    return;
                }
            }
        }catch(Exception ex){
            //ignore
        }
         InputStream is = cl.getResourceAsStream(uri);
         if(is!=null){
			InputStreamReader isr = new InputStreamReader(is);
			char[] buf = new char[1000000];
			int num = isr.read(buf);
            String ending = uri.substring(uri.lastIndexOf('.')+1);
            String contentType = ContentTypes.getMimeType(ending,"text/plain");

			s.ok(contentType,new String(buf,0,num));
            return;
		 }
        //if no response was send so far
        s.error(404,"resource not found");
	}

	@Override
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest arg0, HttpContext arg1) throws HttpException, IOException {
		return new BasicAsyncRequestConsumer();
	}

}
