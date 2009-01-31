function apiurl()
{
  var url=CmdUtils.getDocument( ).location.toString() ;
  var i= url.indexOf("/wiki/");
  if(i!=-1)
  {
    url= url.substr(0,i);
    if(url=="http://openwetware.org") return url+"/api.php";
      
    url+="/w/api.php";
    return url;
  }
  return "http://en.wikipedia.org/w/api.php";
}

var noun_type_mwkw = {
_name: "wikipedia keyword",
  
suggest: function suggest( text, html, callback )
    {
    jQuery.ajax( {
      url: apiurl(),
      dataType: "json",
     data:
        {
          "action":"opensearch",
          "search": text,
          "format":"json",
	   "namespace":"0|14"
        },
     success: function suggestTopics( content ) {
       var i;
        
       if(content==null || content.length!=2) return;
     
       for ( i = 0; i < content[1].length; i++ ) {Utils.reportInfo("C"+i);
         var result = "[["+content[1][i]+"]]";
         callback( CmdUtils.makeSugg( result, result, result ) );
        }
      }
    } );
    return [];
  }
}


/* This is a template command */
CmdUtils.CreateCommand({
  name: "mw",
  icon: "http://upload.wikimedia.org/wikipedia/commons/3/3d/Mediawiki-logo.png",
  homepage: "http://plindenbaum.blogspot.com/",
  author: { name: "Pierre LIndenbaum", email: "plindenbaum@yahoo.fr"},
  license: "GPL",
  description: "Insert a link for mediawiki/wikipedia",
  takes: {"input": noun_type_mwkw},
  preview: function( pblock, input ) {
    var template = "[${name} ${name}]";
    pblock.innerHTML = CmdUtils.renderTemplate(template, {name: "link"});
  },
            
  
  execute: function(input) {
    if(input==null || !input.data) return;
         CmdUtils.setSelection(input.data);
    
  }
});
