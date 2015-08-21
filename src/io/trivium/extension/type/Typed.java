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

package io.trivium.extension.type;

import io.trivium.anystore.ObjectRef;

import java.util.logging.Logger;

public interface Typed {
    default Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }

    default ObjectRef getTypeId(){
        String path = this.getClass().getCanonicalName();
        //eg: io.trivium.extension._e53042cbab0b4479958349320e397141.FileType
        String[] arr = path.split("\\.");
        String typeId = arr[arr.length-2];
        String uuid = typeId.substring(1,9)+"-"+typeId.substring(9,13)+"-"+typeId.substring(13,17)
                +"-"+typeId.substring(17,21)+"-"+typeId.substring(21,33);
        return ObjectRef.getInstance(uuid);
    }
}
