String.prototype.trim = function() {
    try {
        return this.replace(/^\s+|\s+$/g, "");
    } catch(e) {
        return this;
    }
};

String.prototype.startsWith = function(s)
	{
	if(s==null || this.length < s.length) return false;
	return this.substr(0,s.length) == s;
	};


function $(id)
	{
	var e = document.getElementById(id);
	return e;
	}


var XUL={NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};
var HTML={NS:"http://www.w3.org/1999/xhtml"};
var RDF={NS:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"};
var RDFS={NS:"http://www.w3.org/2000/01/rdf-schema#"};
var XLINK={NS:"http://www.w3.org/1999/xlink"};
var SVG={NS:"http://www.w3.org/2000/svg"};
var DC={NS:"http://purl.org/dc/elements/1.1/"};
var XML={NS:"http://www.w3.org/XML/1998/namespace"};
var XMLNS={NS:"http://www.w3.org/2000/xmlns/"};
