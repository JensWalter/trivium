## introduction

Trivium is a platform which follows the rules of SEDA ([staged event-driven architecture](https://en.wikipedia.org/wiki/Staged_event-driven_architecture)).
So the whole information processing can be broken down into little steps and the runtime determines the best way to invoke those peaces.

## features

* free from external dependencies
* unites in-flight and at-rest information processing
* configuration free object persistence
* java-based object store query syntax

## the rules

1. There exist only 3 types of objects
  * facts
  * bindings
  * tasks


1. There is no configuration - just code.

1. All persistent data must exist as fact.


## versioning

So lets start with version 0 - everything has to start somewhere.

Version 0 is set to timestamp 1444000000 (so Sun, 04 Oct 2015 23:06:40 GMT human time).

After that, the version increases constantly ever 2^21 seconds.
In the human world that would be about every 24 days.

So far there are no subversions. The criteria for defining an increment would be to cite the git commit hash (abbreviated version preferred).
