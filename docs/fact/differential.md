# description

Contains a object which implements increment and decrement functionality to keep track of a differential in a thread safe manner.

# fields

| type | name | description |
|------|------|-------------|
| String | datapoint | name of the datapoint |
| AtomicLong | value | actual value |
| *Instant* | *timestamp* | *injected during serialization, not part or the object*<br>
*contains the instant that the object was serialized* |

# github reference

**link**

[Differential.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/fact/Differential.java)

**last commits**

<div id='commits' data-path='src/io/trivium/extension/fact/Differential.java'></div>
<script src='../../js/commits.js' async></script>
