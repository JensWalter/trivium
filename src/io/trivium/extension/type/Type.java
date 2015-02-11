package io.trivium.extension.type;

import io.trivium.anystore.ObjectType;

public interface Type extends Typed{
    default public ObjectType getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.v1.FileType
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-3];
        String version = arr[arr.length-2];
        return ObjectType.getInstance(typeId,version);
    }
}
