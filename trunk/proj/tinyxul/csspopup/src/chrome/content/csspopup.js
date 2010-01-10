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
		var pseudoclasses=["link","visited","active","over","focus","lang","not","first","left","right","root","nth-child","nth-last-child","nth-of-type","nth-last-of-type","first-child","first-of-type","only-of-type","empty","target","enabled","disabled","checked","indeterminate","default"];
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
		"width", "word-spacing", "word-wrap", "z-index",
		
		
		"-moz-appearance", "-moz-background-clip", "-moz-background-inline-policy",
		"-moz-background-origin", "-moz-background-size", "-moz-binding",
		"-moz-border-bottom-colors", "-moz-border-left-colors", "-moz-border-right-colors",
		"-moz-border-top-colors", "-moz-border-end", "-moz-border-end-color",
		 "-moz-border-end-style", "-moz-border-end-width", "-moz-border-image", "-moz-border-radius",
		 "-moz-border-radius-bottomleft", "-moz-border-radius-bottomright", "-moz-border-radius-topleft",
		"-moz-border-radius-topright", "-moz-border-start", "-moz-border-start-color",
		"-moz-border-start-style", "-moz-border-start-width", "-moz-box-align",
		"-moz-box-direction", "-moz-box-flex", "-moz-box-flexgroup", "-moz-box-ordinal-group",
		"-moz-box-orient", "-moz-box-pack", "-moz-box-shadow", "-moz-box-sizing",
		"-moz-column-count", "-moz-column-gap", "-moz-column-width", "-moz-column-rule", "-moz-column-rule-width",
		"-moz-column-rule-style", "-moz-column-rule-color", "-moz-float-edge", "-moz-force-broken-image-icon",
		"-moz-image-region", "-moz-margin-end", "-moz-margin-start", "-moz-opacity", "-moz-outline", "-moz-outline-color", "-moz-outline-offset", "-moz-outline-radius",
		"-moz-outline-radius-bottomleft", "-moz-outline-radius-bottomright", "-moz-outline-radius-topleft", "-moz-outline-radius-topright",  "-moz-outline-style", "-moz-outline-width", "-moz-padding-end",
		"-moz-padding-start", "-moz-stack-sizing", "-moz-transform", "-moz-transform-origin", "-moz-user-focus",
		 "-moz-user-input", "-moz-user-modify", "-moz-user-select", "-moz-window-shadow", "-moz-initial",
		"-moz-appearance", "-moz-win-browsertabbar-toolbox", "-moz-win-communications-toolbox",
		"-moz-win-media-toolbox", "-moz-mac-unified-toolbar", "-moz-linear-gradient", "-moz-radial-gradient",
		"-moz-use-text-color", "-moz-bg-inset", "-moz-activehyperlinktext", "-moz-hyperlinktext",
		"-moz-visitedhyperlinktext", "-moz-buttondefault", "-moz-buttonhoverface", "-moz-buttonhovertext",
		"-moz-cellhighlight", "-moz-cellhighlighttext", "-moz-field", "-moz-fieldtext", "-moz-dialog",
		"-moz-dialogtext", "-moz-dragtargetzone", "-moz-mac-accentdarkestshadow", "-moz-mac-accentdarkshadow", "-moz-mac-accentface", "-moz-mac-accentlightesthighlight", "-moz-mac-accentlightshadow",
		"-moz-mac-accentregularhighlight", "-moz-mac-accentregularshadow", "-moz-mac-chrome-active",
		"-moz-mac-chrome-inactive", "-moz-mac-focusring", "-moz-mac-menuselect", "-moz-mac-menushadow",
		"-moz-mac-menutextselect", "-moz-menuhover", "-moz-menuhovertext", "-moz-win-communicationstext",
		"-moz-win-mediatext", "-moz-nativehyperlinktext", "-moz-alias", "-moz-cell", "-moz-context-menu",
		"-moz-copy", "-moz-grab", "-moz-grabbing", "-moz-spinning", "-moz-zoom-in", "-moz-zoom-out",
		"-moz-box", "-moz-inline-block", "-moz-inline-box", "-moz-inline-grid", "-moz-inline-stack", "-moz-inline-table", "-moz-grid", "-moz-grid-group", "-moz-grid-line", "-moz-groupbox",
		"-moz-deck", "-moz-popup", "-moz-stack", "-moz-marker", "-moz-show-background", "-moz-button",
		"-moz-info", "-moz-desktop", "-moz-dialog", "-moz-document", "-moz-workspace", "-moz-window", "-moz-list", "-moz-pull-down-menu", "-moz-field", "-moz-fixed", "-moz-crisp-edges",
		"-moz-arabic-indic", "-moz-bengali", "-moz-cjk-earthly-branch", "-moz-cjk-heavenly-stem",
		"-moz-devanagari", "-moz-ethiopic-halehame", "-moz-ethiopic-halehame-am",
		"-moz-ethiopic-halehame-ti-er", "-moz-ethiopic-halehame-ti-et", "-moz-ethiopic-numeric",
		"-moz-gujarati", "-moz-gurmukhi", "-moz-hangul", "-moz-hangul-consonant",
		"-moz-japanese-formal", "-moz-japanese-informal", "-moz-kannada", "-moz-khmer", "-moz-lao", "-moz-malayalam", "-moz-myanmar", "-moz-oriya",
		"-moz-persian", "-moz-simp-chinese-formal", "-moz-simp-chinese-informal", "-moz-tamil",
		"-moz-telugu", "-moz-thai", "-moz-trad-chinese-formal", "-moz-trad-chinese-informal",
		"-moz-urdu", "-moz-scrollbars-none", "-moz-scrollbars-horizontal", 
		"-moz-scrollbars-vertical", "-moz-hidden-unscrollable", "-moz-center", "-moz-left",
		"-moz-right", "-moz-anchor-decoration", "-moz-user-select", "-moz-all", "-moz-none", "-moz-min-content", "-moz-fit-content", "-moz-max-content", "-moz-available", "-moz-math-columnline", 
		"-moz-math-firstcolumn", "-moz-math-firstrow", "-moz-math-font-size",
		"-moz-math-font-style", "-moz-math-lastcolumn", "-moz-math-lastrow", "-moz-math-rowline"
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
				if(theCSSprop=="default") continue;
				if(sameAsParent(node.parentNode,css,""+theCSSprop)) continue;
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

function sameAsParent(node,selector,value)
	{
	if(node==null) return false;
	if(node.nodeType==1)
		{
		var theCSSprop =  window.getComputedStyle(node,null).getPropertyValue(selector);
		if(theCSSprop!=null)
			{
			return (""+theCSSprop)==value;
			}
		}
	return sameAsParent(node.parentNode,selector,value);
	}
	
window.addEventListener("load",onloadCssPopup, false);
