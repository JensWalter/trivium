# description

Representation of a File within trivium.

# fields

| type | name | description |
|------|------|-------------|
| String | name | |
| long | size | |
| String | contentType | |
| Instant | lastModified | |
| String | data | |
| NVList | metadata | &nbsp;|

# sample

```json
{"io.trivium.extension.fact.File": {
         "data": "aGVsbG8=",
         "name": "hello.txt",
         "size": "5",
         "contentType": "text\/plain",
         "lastModified": "2014-11-11T15:33:14Z",
         "metadata": []
            }
}
```

# github reference

**link**

[File.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/fact/File.java)

**last commits**

<div id='commits' data-path='src/io/trivium/extension/fact/File.java'></div>
<script src='../../js/commits.js' async></script>
