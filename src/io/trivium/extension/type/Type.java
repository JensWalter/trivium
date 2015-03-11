package io.trivium.extension.type;

import io.trivium.anystore.ObjectRef;

public interface Type extends Typed{
    default public ObjectRef getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        return ObjectRef.getInstance(typeId);
    }
}
