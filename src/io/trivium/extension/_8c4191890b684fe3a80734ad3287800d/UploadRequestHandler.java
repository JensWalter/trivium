/*
 * Copyright 2015 Jens Walter
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

package io.trivium.extension._8c4191890b684fe3a80734ad3287800d;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.trivium.NVList;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.statics.MimeTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.glue.binding.http.HttpUtils;
import io.trivium.glue.binding.http.Session;
import io.trivium.glue.om.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploadRequestHandler implements HttpHandler {
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void handle(HttpExchange httpexchange) {
        log.log(Level.FINE,"upload request handler");
        NVList upload = HttpUtils.getInputAsNVList(httpexchange);
        /**
         {
         "name":"vie.xml",
         "type":"text/xml",
         "size":1825,
         "lastModified":1397852272000,
         "data":"PGRvbWFpbiB...."
         }
         */
        Session s = new Session(httpexchange);
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
            log.log(Level.SEVERE,"error while processing file upload", ex);
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
            log.log(Level.SEVERE,"error while processing file upload", ex);
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
            type = MimeTypes.getMimeType(ending);
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

        log.log(Level.FINE,"inserting file into anystore {}",fileName);


        po.setData(file);
        po.setTypeId(TypeIds.FILE);
        AnyClient.INSTANCE.storeObject(po);
    }
}
