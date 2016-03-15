** How to write a custom fact serializer? **

After implementing a Fact, you have to overwrite 2 Functions derived from the Fact interface:
* populate
* toTriviumObject

```java
@Override
public void populate(TriviumObject tvm){
}

@Override
public TriviumObject toTriviumObject(){
   return null;
}
```
