// https://developer.mozilla.org/en/DOM/window.getComputedStyle

/** when the xul page is loaded, register for events from the contextual popupmenu */
function onloadCssPopup()
	{
	document.addEventListener("popupshowing",function(evt){preparePopup4CssPopup(evt);},true);
	}

/* prepare the contextual menu just before it is showing on screen: hide or show our menu */
function preparePopup4CssPopup(evt)
	{
	var menu_show = document.getElementById("csspopup-show");
	if(menu_show==null) return;
	var clicked= document.popupNode;
	if(clicked==null)
		{
		menu_show.hidden=true;
		return;
		}
	menu_show.hidden=false;
	}

function showcss()
	{
	showcssFromElement(document.popupNode);
	}

function showcssFromElement(clicked)
	{
	try
	{
	if( clicked==null)
		{
		return;
		}
	
	var msg=recurse(clicked,"");
	var w=window.openDialog("chrome://csspopup/content/csspopupwin.xul",
		"[WindowId]"+new Date(),
		"dialog,modal",
		msg+"\n\n/** CSS popup by Pierre Lindenbaum PhD. http://plindenbaum.blogspot.com */\n");
	if(w==null) { alert("cannot get window!"); return;}
	} catch(err)
	{
	alert("Error1: "+err.message);
	}
	}

function escapeXML(s)
	{
	var x="";
	for(var i=0;i< s.length;++i)
		{
		switch(s.charAt(i))
			{
			case '<': x+="&lt;"; break;
			case '>': x+="&gt;"; break;
			default: x+=s.charAt(i);break;
			}
		}
	return x;
	}

function recurse(node,content)
	{
	try
		{
		var selectors=[
		"auto", "background", "background-attachment", "background-color", "background-image",
		"background-position", "background-repeat", "border", "border-bottom", "border-collapse",
		"border-color", "border-left", "border-right", "border-spacing", "border-style",
		"border-top", "border-width", "bottom", "box-sizing", "caption-side", "clear", "clip",
		"color", "content", "counter-increment", "counter-reset", "cursor", "direction",
		"display", "empty-cells", "float", "font", "font-family", "font-size",
		"font-size-adjust", "font-stretch", "font-style", "font-variant", "font-weight",
		"height", "image-rendering", "ime-mode", "inherit", "initial", "left",
		"letter-spacing", "line-height", "list-style", "list-style-image",
		"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left",
		"margin-right", "margin-top", "marker-offset", "marks", "max-height", "max-width",
		"min-height", "min-width", "none", "normal", "opacity", "orphans", "outline",
		"outline-color", "outline-offset", "outline-style", "outline-width", "overflow",
		"overflow-x", "overflow-y", "padding", "padding-bottom", "padding-left",
		"padding-right", "padding-top", "page-break-after", "page-break-before",
		"page-break-inside", "pointer-events", "position", "quotes", "right",
		"table-layout", "text-align", "text-decoration", "text-indent",
		"text-overflow", "text-rendering", "text-shadow", "text-transform", "top",
		"unicode-bidi", "vertical-align", "visibility", "white-space", "widows",
		"width", "word-spacing", "word-wrap", "z-index"
		];
		if(node==null ) return content;
		if(node.nodeType==1)
			{
			var computedStyles = window.getComputedStyle(node,null);
			
			content+=node.nodeName.toLowerCase()+" {\n";
			for(var i in selectors)
				{
				var css=selectors[i];
				var theCSSprop = computedStyles.getPropertyValue(css);
				if(theCSSprop==null || (""+theCSSprop).replace("[\n \t]","").length==0) continue;
				content+="    "+escapeXML(""+css)+":"+ escapeXML(""+theCSSprop)+";\n";
				}
			content+="    }\n\n";
			}
		return recurse(node.parentNode,content);
		}
	catch(err)
		{
		return content+"\nError in recurse :" +err.message;
		}
	}

	
	
window.addEventListener("load",onloadCssPopup, false);
