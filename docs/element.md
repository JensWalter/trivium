# description

Element is a special type which carries all Fact information. It it used as data element of an TriviumObject and represents the content.

# basic layout

An element contains the following fields.

| field | type | description |
|-------|------|-------------|
| name | String | Name of the element. In special cases, this can be null (see special rules). |
| value | String | Value of the element. |
| metadata | NVList | additional metadata for this element. |
| children | ArrayList&lt;Element&gt; | contains all sub elements. |
| parent | Element | Back reference for tree traversal. |

# special rules

** How arrays are implemented? **

The name is set to the name of the array. The value is kept null. Instead of the value, children elements are build with the name set to null and the value set to the real value of the array.

** How types are implemented? **

Type implementation can vary with the actual serializer used to generate the element. Type information is only kept if the same deserializer is used. (see possible metadata entries)

** What are possible metadata entries? **

| name | value | description |
|------|-------|-------------|
| type | boolean | value is to read as boolean type |
| type | number | value is to read as numeric type |
| xml:attribute | {empty} | If this entry is present, the xml serializer uses an attribute instead of an element for this value |
| xml:namespace | {string} | Contains the xml namespace used for this element. This only applies to the xml serializer, the json serializer completely ignores namespaces. |
