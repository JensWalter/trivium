# description

A TriviumObject is an object that is used to serialize and transport any fact within the trivium platform.

# fields

| type | name | description |
|------|------|-------------|
| byte | typeByte | Represents the encoding type for the binary serialization.<br>0 uncompressed json<br>1 snappy compressed json<br>*default is 1* |
| Logger | logger | Trivium internal logger |
| ObjectRef | id | Id of the object. This Id is also persisted as metadata within the header. |
| TypeRef | typeRef | Actual type of the content to specify the Fact to cast to. |
| NVList | metadata | all metadata |
| byte[] | b_metadata | all metadata in binary form |
| Element | data | content of the fact |
| byte[] | b_data | content of the fact in binary form |

These fields are not serialized by the default serializer. A TriviumObject has a different kind of Layout to support various applications.

# sample

** metadata **
```json
{
  id: "1db571be-dfa2-4fab-929c-7e036295a3a8",
  typeRef: "io.trivium.extension.fact.File",
  size: 12345
}
```

** data **
```json
{"io.trivium.extension.fact.File": {
         "data": "cGFja2F...",
         "name": "io\/trivium\/test\/NjamsTestData.java",
         "size": "814",
         "contentType": "text\/x-java-source",
         "lastModified": "2014-11-11T15:33:14Z",
         "metadata": []
            }
}
```

# github reference

**link**

[TriviumObject.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/fact/TriviumObject.java)

**last commits**

<div id='commits' data-path='src/io/trivium/extension/fact/TriviumObject.java'></div>
<script src='../js/commits.js' async></script>
