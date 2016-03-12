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

After that, the version increases constantly ever 2^23 seconds.
In the human world that would be about every 90 days.

So far there are no subversions. The criteria for defining an increment would be to cite the git commit hash (abbreviated version preferred).

<script src="js/vis.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/vis.min.css">
<div>
time to the next release: <span id='timetorelease'></span>
</div>
<div id="visualization"></div>

<script type="text/javascript">
  // DOM element where the Timeline will be attached
  var container = document.getElementById('visualization');

  //timeframe is 2^23 seconds
  // so 8388608	seconds
  // or 97.09 days
  var timeframe = Math.pow(2,23);
  // offset Sun, 04 Oct 2015 23:06:40 GMT
  var offset = 1444000000000;

  var dataset=[];
  //generate first 20 versions
  for(var idx=0;idx<20;idx++){
	var item = {id: idx, content: 'version '+idx, start: new Date(offset+(timeframe*idx)*1000)};
	dataset.push(item);
  }
  var items = new vis.DataSet(dataset);
  var options = {start:'2016-01-01',end:'2016-12-31'};
  var timeline = new vis.Timeline(container, items, options);

  var ttr = document.getElementById('timetorelease');

  //select today
  var now = new Date();
  var diff = now-offset;
  var version = Math.round(diff/(timeframe*1000))-1;
  timeline.setSelection(version, {focus: true});
  //update countdown
  ttr.innerHTML=secondsToHumanReadable(Math.round((offset+(timeframe*(version+1)*1000)-now)/1000));

  window.setInterval(function(){
	var now = new Date();
	var diff = now-offset;
	var nextVersion = Math.round(diff/(timeframe*1000));
	ttr.innerHTML=secondsToHumanReadable(Math.round((offset+(timeframe*(nextVersion)*1000)-now)/1000));
  }, 1000);

  function secondsToHumanReadable(seconds){
	var numdays = Math.floor((seconds % 31536000) / 86400);
	var numhours = Math.floor(((seconds % 31536000) % 86400) / 3600);
	var numminutes = Math.floor((((seconds % 31536000) % 86400) % 3600) / 60);
	var numseconds = (((seconds % 31536000) % 86400) % 3600) % 60;
	return numdays + " days " + numhours + " hours " + numminutes + " minutes " + numseconds + " seconds";
  }
</script>
