# TimerSample

## description

This sample shows a basic implementation of a Timer which triggers every ten seconds. Every triggered Event contains a timestamp which is then printed out to the log file.

## sample content

* implementation of a binding
* implementation of a fact
* implementation of a task

### fact

#### Timertick

The Timertick carries the timestamp through the system.

```java
public class TimerTick implements Fact {
    /**
     * provide a timestamp in utc milliseconds
     */
    public long timestamp = System.currentTimeMillis();
}
```

#### LogEntry (core system)

see documentation of the core system.

### binding

Uses a java timer, which is packaged as trivium binding.

```java
public class MyTimer extends Binding {
    Timer t = null;
    int interval_ms = 10000;

    @Override
    protected void start() {
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerTick tick = new TimerTick();
                tick.timestamp=System.currentTimeMillis();
                getLogger().log(Level.FINE,getName() + "=>tick " + tick.timestamp);
                emit(tick);
            }
        },0,interval_ms);
        setState(BindingState.running);
    }

    @Override
    protected void stop() {
        if(t!=null){
            t.cancel();
            t=null;
        }
        setState(BindingState.stopped);
    }
}
```
Every execution of the TimerTask emits an TimeTick fact.

### task

#### TimerTick2LogEntryMapper

This mapper transforms the timestamp into a LogEntry fact, so the console print task can pick it up.

```java
public class TimerTick2LogEntryMapper extends Task {
    TimerTick tick = new Query<TimerTick>(){
        {
            targetType = TimerTick.class;
        }
    }.getObject();

    LogEntry entry;

    @Override
    public boolean eval() throws Exception {
        entry = new LogEntry();
        getLogger().log(Level.FINE,getName() + "=>transforming " + tick.timestamp);
        entry.message = String.valueOf(tick.timestamp);
        return true;
    }
}
```

#### ConsoleLogger

see documentation of the core system.

## location

[https://github.com/trivium-io/TimerSample](https://github.com/trivium-io/TimerSample)
