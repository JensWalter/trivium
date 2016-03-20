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

# toString layout

The toString method return the element structure as custom string. Here a short intruduction to the layout.

** constructs **

| operator | meaning |
|----------|---------|
| [] | enclosing for metadata attached to the element |
| =&gt; | separator between name and value |
| {} | enclosing for value or children |
| :: | metadata name value separator |

** samples **

> io.trivium.extension.fact.File => {name => {"testFile1.txt"}, size => {"6"}, contentType => {"text/plain"}, lastModified => {"2016-03-20T14:25:15.097Z"}, data => {"abc123"}}

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
