io.trivium.extension.task.ConsoleLogger

# 入 (input)

| type | query |
|------|-------|
| io.trivium.extension.fact.LogEntry | {<br>&nbsp;&nbsp;targetType = LogEntry.class;<br>} |

# 变 (transform)

Prints the given LogEntry message to the console.

# 出 (output)

(no output)

# code reference
<div id='code'></div>
<script>
var url = 'https://github.com/trivium-io/trivium/raw/master/src/io/trivium/extension/task/ConsoleLogger.java';
$.ajax({type:'GET',
        url:url,
        success: function(data){
  var root = $(data);
	var el = $('.file',root);
  var css = $('link[rel="stylesheet"]',root);
  $('#code').add(el);
  $('#code').add(css);
}});
</script>
