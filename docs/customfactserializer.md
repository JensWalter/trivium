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
It it recommended to extend the already existing Code the following way.

*toTriviumObject()*

```java
@Override
public TriviumObject toTriviumObject(){
  //generate a TriviumObject from the default mechanism
    TriviumObject tvm = Fact.super.toTriviumObject();
    //get the data element
    Element el = tvm.getData();
    //extend with additional information
    el.addChild(new Element("timestamp", Instant.now().toString()));
    //set data again to trigger correct serialization
    tvm.setData(el);
    //return the new element
    return tvm;
}
```

*populate(TriviumObject tvm)*

```java
//TODO
```
