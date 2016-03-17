# description

The Ticker keep count of an event and aggregates within its given interval.

# fields

| type | name | description |
|------|------|-------------|
| String | datapoint | name of the datapoint |
| long | interval | interval in ms in between the ticker counts its values e.g. for every 60 seconds |
| ConcurrentHashMap<Long, AtomicLong> | values | |

# sample

```json
{"io.trivium.extension.fact.Ticker": {
         "datapoint": "dp1",
         "interval": 60000,
         "timeFrameStart": "2016-01-01T18:00:00Z",
         "timeFrameEnd": "2016-01-01T18:01:00Z",
         "value": 5
            }
}
```

# github reference

**link**

[Ticker.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/fact/Ticker.java)

**last commits**

<div id='commits' data-path='src/io/trivium/extension/fact/Ticker.java'></div>
<script src='../../js/commits.js' async></script>
