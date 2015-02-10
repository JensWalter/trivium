# trivium-core
core system without dependencies.

start the engine:
vm arguments: -Xmx2G -Djava.system.class.loader=io.trivium.InfiniLoader -Djava.protocol.handler.pkgs=io.trivium.urlhandler -server
app arguments: -cq -cs -ll debug -p /Users/jens/tmp/store -t 1m