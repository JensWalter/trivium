# trivium
[![Build Status](https://travis-ci.org/trivium-io/trivium.svg)](https://travis-ci.org/trivium-io/trivium)

The core system without dependencies.

There are multiple ways to start the engine. 
###shell script

./trivium.sh.

###jar file

java -Djava.system.class.loader=io.trivium.TriviumLoader -Djava.protocol.handler.pkgs=io.trivium.urlhandler -jar trivium.jar
