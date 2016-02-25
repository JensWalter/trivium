/*
 * Copyright 2016 Jens Walter
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

package io.trivium.glue.om;

public class ElementToken {
    enum Type {
        BEGIN_ELEMENT,
        END_ELEMENT,
        NAME,
        CHILD,
        VALUE,
        BEGIN_ARRAY,
        END_ARRAY
    }

    private Element element;
    private Type type;

    public ElementToken(Element el, Type t) {
        this.element = el;
        this.type = t;
    }

    public Element getElement() {
        return element;
    }

    public Type getType() {
        return type;
    }
}
