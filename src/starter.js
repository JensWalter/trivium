/*
jjs -Djava.system.class.loader=io.trivium.TriviumLoader -Djava.protocol.handler.pkgs=io.trivium.urlhandler -server -cp bnd-tibco-ems-receiver:tpe-timer:tsk-consoleLogger:bnd-timer:tpe-timerconfig:tsk-excel2table:tpe-error:tpe-timertick:tsk-java-compiler:tpe-logentry:trivium-core:tsk-js-runner:tpe-table:tsk-TimerTick2LogEntryMapper:tsk-log
*/

var args =["-cq","-cs","-ll","fine","-p","/Users/jens/tmp/store","-t","1m"];

var mainType =  Java.type("io.trivium.Start");
mainType.main(args);

var Central = Java.type("io.trivium.Central");
var Registry = Java.type('io.trivium.Registry');
var ObjectRef = Java.type('io.trivium.anystore.ObjectRef');
//print("central is running => "+Central.isRunning);