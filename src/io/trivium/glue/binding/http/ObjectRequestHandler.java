package io.trivium.glue.binding.http;

import com.google.common.base.Joiner;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Json;
import io.trivium.Central;
import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Json;
import javolution.util.FastList;
import org.apache.http.*;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectRequestHandler implements
        HttpAsyncRequestHandler<HttpRequest> {
    private final static Pattern uuidpattern = Pattern
            .compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange,
                       HttpContext context) {
        ObjectRef sourceId = ObjectRef.getInstance();
        Session s = new Session(request, httpexchange, context, sourceId);

        try {
            Header[] headers = request.getAllHeaders();
            String uri = request.getRequestLine().getUri();

            String[] parts = uri.split("/");
            // get method
            String method = parts[2];
            if (method.equals("upsert")) {
                boolean processed = upsert(request, sourceId, headers, uri);
                if (!processed) {
                    s.error(HttpStatus.SC_BAD_REQUEST,
                            "request could not be processed");
                    return;
                } else {
                    s.ok();
                }
            } else if (method.equals("query")) {
                // read payload
                NVList criteria = HttpUtils.getInputAsNVList(request);
                Query q = new Query();
                for (NVPair pair : criteria) {
                    q.criteria.add(new Value(pair.getName(), pair.getValue()));
                }
                FastList<InfiniObject> objects = AnyClient.INSTANCE.loadObjects(q);
                FastList<String> sb = new FastList<String>();
                for (InfiniObject po : objects) {
                    if (po != null) {
                        sb.add(Json.InternalToJson(po.getData()));
                    }
                }
                String str = Joiner.on(",").join(sb);
                String responseText = "[" + str + "]";
                s.ok(ContentTypes.getMimeType("json"), responseText);
                return;
            } else {
                // unknown method
                s.error(HttpStatus.SC_BAD_REQUEST,
                        "method unknown\nplease use the following pattern\nhttp://{server}:{port}/object/{upsert|search|get|update}/{id}\n");
                return;
            }
        } catch (Exception ex) {
            Central.logger.error("error processing object request", ex);
            s.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ex.toString());
            return;
        }
    }

    private boolean upsert(HttpRequest request, ObjectRef sourceId,
                           Header[] headers, String uri) throws IOException {
        boolean processed = false;
        // look for id in uri
        ObjectRef id;
        Matcher matcher = uuidpattern.matcher(uri);
        if (matcher.find()) {
            id = ObjectRef.getInstance(matcher.group());
        } else {
            id = ObjectRef.getInstance();
        }
        // read payload
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest) request;
            DataInputStream dis = new DataInputStream(r.getEntity()
                    .getContent());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[100000];
            while (dis.available() > 0) {
                int i = dis.read(buf);
                bos.write(buf, 0, i);
            }
            dis.close();
            bos.close();

            // send object to backend
            InfiniObject po = new InfiniObject();
            po.setId(id);

            String contentType = "application/octet-stream";
            if (request.containsHeader("Content-Type")) {
                Header type = request.getFirstHeader("Content-Type");
                contentType = type.getValue();
            }
            po.addMetadata("contentType", contentType);

            // if header starts with infinup - copy value
            for (Header h : headers) {
                if (h.getName().startsWith("infiniup-")) {
                    po.addMetadata(h.getName().substring(9), h.getValue());
                }
            }

            //TODO is this still right
            po.setDataBinary(bos.toByteArray());

            AnyClient.INSTANCE.storeObject(po);
            processed = true;
        }
        return processed;
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(
            HttpRequest arg0, HttpContext arg1) throws HttpException,
            IOException {
        return new BasicAsyncRequestConsumer();
    }

}
