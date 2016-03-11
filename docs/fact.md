# Fact

## description

A fact is abstract definition of aa data structure. Facts can be serialized to different representation. By default, every fact is available as JSON, XML and JavaType.

At first Facts can only be defined in Java.
Later lazy facts will be build incrementally from existing data. So the need to define a fact will lessen and the system can learn new elements on its own.

So how does it look.
Facts are extensions to the trivium system, so they must be packaged in the appropriate java package.
All extensions must also implement SPI for dynamic loading and unloading. SPI is implemented by package a service metafile into the jar file, so the JVM knows, what service will be extended by this implementation.
Although it is called service, trivium uses this system only to load class definitions. It makes no assumptions on the type of implementation.
