String.prototype.trim = function() {
    try {
        return this.replace(/^\s+|\s+$/g, "");
    } catch(e) {
        return this;
    }
};

String.prototype.escapeC = function()
	{
   	var s="";
   	for(var i=0;i < this.length;++i)
   		{
   		switch(this.charAt(i))
   			{
   			case '\n': s+="\\n"; break;
   			case '\t': s+="\\t"; break;
   			case '\r': s+="\\r"; break;
   			case '\"': s+="\\\""; break;
   			case '\'': s+="\\\'"; break;
   			case '\\': s+="\\\\"; break;
   			default: s+=this.charAt(i); break;
   			}
   		}
   	return s;
	};

String.prototype.escapeSqlite = function()
	{
   	var s="";
   	for(var i=0;i < this.length;++i)
   		{
   		switch(this.charAt(i))
   			{
   			/* http://www.sqlite.org/faq.html#q14 */
   			case '\'': s+="\'\'"; break;
   			default: s+=this.charAt(i); break;
   			}
   		}
   	return s;
	};

String.prototype.escapeXML = function()
	{
   	var s="";
   	for(var i=0;i < this.length;++i)
   		{
   		switch(this.charAt(i))
   			{
   			case '<': s+="&lt;"; break;
   			case '>': s+="&gt;"; break;
   			case '&': s+="&amp;"; break;
   			case '\"': s+="&quot;"; break;
   			case '\'': s+="&apos;"; break;
   			default: s+=this.charAt(i); break;
   			}
   		}
   	return s;
	};


var XUL={NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};
var HTML={NS:"http://www.w3.org/1999/xhtml"};
var RDF={NS:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"};
var RDFS={NS:"http://www.w3.org/2000/01/rdf-schema#"};
var XLINK={NS:"http://www.w3.org/1999/xlink"};
var SVG={NS:"http://www.w3.org/2000/svg"};
var DC={NS:"http://purl.org/dc/elements/1.1/"};

/** https://developer.mozilla.org/en/Debugging_a_XULRunner_Application */
function jsdump(str)
	{
	Components.classes['@mozilla.org/consoleservice;1']
            .getService(Components.interfaces.nsIConsoleService)
            .logStringMessage(str+"\n");
      	}


/**
 * IsA 
 */
var IsA={
	URI:function(uri)
		{
		try
			{
			var  nsURI = Components
			.classes["@mozilla.org/network/simple-uri;1"]
			.getService(Components.interfaces.nsIURI);
			nsURI.spec = uri;
			return true;
			}
		catch(err2)
			{
			return false;
			}
		},
	Integer:function(s)
		{
		s=s.trim();
		if(!s.match(/^[0-9]+$/ig) || parseInt(s)==NaN) return false;
		return true;
		}
	};

/** DOM */
var DOM={
	serialize:function(node)
		{
		switch(node.nodeType)
			{
			case 1:
				{
				var s= "<"+ node.nodeName;
				var atts= node.attributes;
				for(var i=0;i< atts.length;++i)
					{
					s+=" ";
					s+=atts[i].nodeName+"=\""+atts[i].nodeValue.escapeXML()+"\"";
					}
				if(!node.hasChildNodes())
					{
					s+= "/>";
					return s;
					}
				s+= ">";
				for(var c=node.firstChild;
					c!=null;
					c=c.nextSibling)
					{
					s+= DOM.serialize(c);
					}
				s+= "</"+ node.nodeName+">";
				return s;
				}
			case 3:	return nodeValue.escapeXML();
			default: return "DOM:???";
			}
		},
	removeAll:function(node)
		{
		if(node==null) return;
		while(node.hasChildNodes())
			{
			node.removeChild(node.firstChild);
			}
		},
	isA:function(root,ns,localName)
		{
		return	root!=null &&
			root.namespaceURI == ns &&
			root.localName == localName;
		},
	firstChildNS:function(root,ns,localName)
		{
		if(root==null) return null;
		for(var c=root.firstChild;
			c!=null;
			c=c.nextSibling)
			{
			if(DOM.isA(c,ns,localName)) return c;
			}
		return null;
		}
	};



function $(id)
	{
	var e = document.getElementById(id);
	return e;
	}



