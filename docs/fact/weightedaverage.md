# description

Calculates a weighted average by the following formula:
average = (average * count + newvalue ) / (count +1)

# fields

| type | name | description |
|------|------|-------------|
| String | datapoint | name of the datapoint |
| AtomicDouble | avg | current average value |
| AtomicLong | count | current value count |
| *Instant* | *timestamp* | *injected during serialization, not part or the object<br>contains the instant that the object was serialized* |

# sample

```json
{"io.trivium.extension.fact.WeightedAverage": {
         "datapoint": "dp1",
         "avg": 5.0,
         "count": 3,
         "timestamp": "2016-01-01T18:00:00Z"
            }
}
```

# github reference

**link**

[WeightedAverage.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/fact/WeightedAverage.java)

**last commits**

<div id='commits' data-path='src/io/trivium/extension/fact/WeightedAverage.java'></div>
<script src='../../js/commits.js' async></script>
