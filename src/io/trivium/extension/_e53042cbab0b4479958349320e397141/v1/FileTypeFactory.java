package io.trivium.extension._e53042cbab0b4479958349320e397141.v1;

import io.trivium.NVList;
import io.trivium.anystore.ObjectType;
import io.trivium.extension.type.TypeFactory;
import io.trivium.glue.InfiniObject;
import io.trivium.glue.om.Element;
import io.trivium.anystore.ObjectRef;

public class FileTypeFactory implements TypeFactory {
    @Override
    public ObjectType getTypeId() {
        return ObjectType.getInstance("e53042cb-ab0b-4479-9583-49320e397141","v1");
    }

    @Override
    public FileType getInstance(InfiniObject po) {
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
    public InfiniObject getPersistenceObject(Object instance) {
        if(instance instanceof FileType) {
            FileType inst = (FileType)instance;
            InfiniObject po = new InfiniObject();
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
        return "infiniup-file factory";
    }

}
