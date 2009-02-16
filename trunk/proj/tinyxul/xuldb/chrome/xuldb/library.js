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

var XUL={NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};
var HTML={NS:"http://www.w3.org/1999/xhtml"};
var RDF={NS:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"};
var RDFS={NS:"http://www.w3.org/2000/01/rdf-schema#"};
var XLINK={NS:"http://www.w3.org/1999/xlink"};
var SVG={NS:"http://www.w3.org/2000/svg"};
var DC={NS:"http://purl.org/dc/elements/1.1/"};

function $(id)
	{
	dump("Searching for "+id);
	var e = document.getElementById(id);
	return e;
	}
