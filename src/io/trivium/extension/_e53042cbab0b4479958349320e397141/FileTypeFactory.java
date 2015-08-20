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

package io.trivium.extension._e53042cbab0b4479958349320e397141;

import io.trivium.NVList;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.anystore.ObjectRef;

public class FileTypeFactory implements TypeFactory {
    @Override
    public FileType getInstance(TriviumObject po) {
        FileType file = new FileType();
        NVList metadata = po.getMetadata();
        Element root = po.getData();
        Element fileElement = root.getFirstChild("file");
        file.contentType = fileElement.getFirstChild("contentType").getValue();
        file.data = fileElement.getFirstChild("data").getValue();
        file.lastModified = fileElement.getFirstChild("lastModified").getValue();
        file.name = fileElement.getFirstChild("name").getValue();
        file.size = fileElement.getFirstChild("size").getValue();
        file.metadata = metadata;

        return file;
    }

    @Override
    public TriviumObject getTriviumObject(Object instance) {
        if(instance instanceof FileType) {
            FileType inst = (FileType)instance;
            TriviumObject po = new TriviumObject();
            po.replaceMeta("name", inst.name);
            po.replaceMeta("size", inst.size);
            po.replaceMeta("contentType", inst.contentType);

            Element file = new Element("file");
            file.addChild(new Element("data", inst.data));
            file.addChild(new Element("name", inst.name));
            file.addChild(new Element("size", inst.size));
            file.addChild(new Element("contentType", inst.contentType));
            file.addChild(new Element("lastModified", inst.lastModified));

            po.setData(file);

            return po;
        }else{
            return null;
        }
    }

    @Override
    public String getName(){
        return "trivium-file factory";
    }

}
