package io.trivium.webui;

import io.trivium.NVList;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.ObjectRef;
import io.trivium.glue.TriviumObject;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Element;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploadRequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
    Logger log = LogManager.getLogger(getClass());
    
    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context) throws HttpException, IOException {
        log.debug("upload request handler");
        NVList upload = HttpUtils.getInputAsNVList(request);
        /**
         {
         "name":"vie.xml",
         "type":"text/xml",
         "size":1825,
         "lastModified":1397852272000,
         "data":"PGRvbWFpbiB...."
         }
         */
        Session s = new Session(request, httpexchange, context, ObjectRef.getInstance());
        try {
            String fileName = upload.findValue("name");
            String type = upload.findValue("type");
            long size = Long.parseLong(upload.findValue("size"));
            long lastModified = Long.parseLong(upload.findValue("lastModified"));
            String data = upload.findValue("data");

            //look for jar files
            if (type.equals("application/java-archive") || type.equals("application/zip")) {
                processContainer(data);
            } else {
                processFile(fileName,size,type,lastModified,data);
            }
        } catch (Exception ex) {
            log.error("error while processing file upload {}", ex);
        }
        s.ok();
    }

    private void processContainer(String data){
        try {
            byte[] input = Base64.getDecoder().decode(data);
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                Instant fileLastModifed = entry.getLastModifiedTime().toInstant();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                bos.close();
                byte[] content = bos.toByteArray();
                int fileSize = content.length;
                if (fileSize > 0) {
                    String fileData = Base64.getEncoder().encodeToString(content);
                    processFile(name,fileSize,"",fileLastModifed.toEpochMilli(),fileData);
                }
            }
            zis.close();
            bis.close();
        }catch(Exception ex){
            log.error("error while processing file upload {}", ex);
        }
    }

    private void processFile(String fileName,long dataSize,String type,long lastModified,String data){
        //single file insert
        //guess for java sources
        String canonicalName="";
        if (fileName.endsWith(".class")) {
            //class file found
            canonicalName = fileName.substring(0, fileName.indexOf(".class")).replace('/', '.');
        }
        if (fileName.endsWith(".java")) {
            //class file found
            canonicalName = fileName.substring(0, fileName.indexOf(".java")).replace('/', '.');
        }

        if(type.equals("")){
            //guess contentType from file ending
            String ending = fileName.substring(fileName.lastIndexOf('.')+1);
            type = ContentTypes.getMimeType(ending);
        }

        TriviumObject po = new TriviumObject();

        po.addMetadata("name", fileName);
        po.addMetadata("contentType", type);
        po.addMetadata("lastModified", Instant.ofEpochMilli(lastModified).toString());
        po.addMetadata("type", "file");
        if (canonicalName.length() > 0) {
            po.addMetadata("canonicalName", canonicalName);
        }

        Element file = new Element("file");
        file.addChild(new Element("data", data));
        file.addChild(new Element("name", fileName));
        file.addChild(new Element("size", String.valueOf(dataSize)));
        file.addChild(new Element("contentType", type));
        file.addChild(new Element("lastModified", Instant.ofEpochMilli(lastModified).toString()));

        log.debug("inserting file into anystore {}",fileName);


        po.setData(file);
        po.setTypeId(TypeIds.FILE);
        AnyClient.INSTANCE.storeObject(po);
    }
}
