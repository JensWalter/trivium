# trivium [![Build Status](https://travis-ci.org/trivium-io/trivium.svg)](https://travis-ci.org/trivium-io/trivium)

Trivium provides a platform to approach software development from a more CEP/rule based approach than a classical procedure based approach.
So instead of building 

There are multiple ways to start the engine. 
###shell script

./trivium.sh.

###jar file

java -Djava.system.class.loader=io.trivium.TriviumLoader -Djava.protocol.handler.pkgs=io.trivium.urlhandler -jar trivium.jar
