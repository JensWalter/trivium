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
import io.trivium.extension._f70b024ca63f4b6b80427238bfff101f.TriviumObject;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.om.Element;

public class FileTypeFactory implements TypeFactory<FileType> {
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
    public TriviumObject getTriviumObject(FileType instance) {
            TriviumObject po = new TriviumObject();
            po.replaceMeta("name", instance.name);
            po.replaceMeta("size", instance.size);
            po.replaceMeta("contentType", instance.contentType);

            Element file = new Element("file");
            file.addChild(new Element("data", instance.data));
            file.addChild(new Element("name", instance.name));
            file.addChild(new Element("size", instance.size));
            file.addChild(new Element("contentType", instance.contentType));
            file.addChild(new Element("lastModified", instance.lastModified));
            po.setData(file);
            return po;
    }

    @Override
    public String getName(){
        return "trivium-file factory";
    }

}
