package io.trivium.extension.type;

import io.trivium.anystore.ObjectRef;

public interface Typed {
    default public ObjectRef getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        String uuid = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
        return ObjectRef.getInstance(uuid);
    }
}
