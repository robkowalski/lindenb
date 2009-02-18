String.prototype.trim = function() {
    try {
        return this.replace(/^\s+|\s+$/g, "");
    } catch(e) {
        return this;
    }
}


var XUL={NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};
var HTML={NS:"http://www.w3.org/1999/xhtml"};
var RDF={NS:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"};
var RDFS={NS:"http://www.w3.org/2000/01/rdf-schema#"};
var XLINK={NS:"http://www.w3.org/1999/xlink"};
var SVG={NS:"http://www.w3.org/2000/svg"};
var DC={NS:"http://purl.org/dc/elements/1.1/"};
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



