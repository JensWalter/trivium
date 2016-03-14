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

[ConsoleLogger.java](https://github.com/trivium-io/trivium/blob/master/src/io/trivium/extension/task/ConsoleLogger.java)

# last changes

<div id='changes'></div>
<script>
var url = 'https://api.github.com/repos/trivium-io/trivium/commits?path=src/io/trivium/extension/task/ConsoleLogger.java';
$.ajax({type:'GET',
        url:url,
        success: function(data){
    var str="<table class='docutils'><tr><th>message</th><th>date</th><th>author</th><th>link</th></tr>";
    for(var idx=0;idx<data.length;idx++){
      var one = data[idx];
      var d = one.commit.author.date.substr(0,10);
      var t = one.commit.author.date.substr(11,10);
      str+="<tr><td>"+one.commit.message+"</td><td>"
          +d+" "+t+"</td><td>"
          +one.commit.author.name+"</td><td>"
          +"<a href='"+one.commit.url+"'>link</a></td></tr>";
    }
    str+="</table>";
    $('#changes').html(str);
}});
</script>
