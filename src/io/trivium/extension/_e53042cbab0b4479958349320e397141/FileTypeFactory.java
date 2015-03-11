package io.trivium.extension._e53042cbab0b4479958349320e397141;

import io.trivium.NVList;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.TriviumObject;
import io.trivium.glue.om.Element;
import io.trivium.anystore.ObjectRef;

public class FileTypeFactory implements TypeFactory {
    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("e53042cb-ab0b-4479-9583-49320e397141");
    }

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
