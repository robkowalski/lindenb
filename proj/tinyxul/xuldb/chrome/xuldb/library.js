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
					s+=atts[i].nodeName+"=\""+atts[i].nodeValue+"\"";
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
			case 3:	return nodeValue;
			default: return "DOM:???";
			}
		}
	};

var XUL={NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};
var HTML={NS:"http://www.w3.org/1999/xhtml"};
var RDF={NS:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"};
var RDFS={NS:"http://www.w3.org/2000/01/rdf-schema#"};
var XLINK={NS:"http://www.w3.org/1999/xlink"};
var SVG={NS:"http://www.w3.org/2000/svg"};
var DC={NS:"http://purl.org/dc/elements/1.1/"};

function $(id)
	{
	var e = document.getElementById(id);
	return e;
	}



