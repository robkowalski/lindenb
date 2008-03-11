var MY={
/** when the xul page is loaded, register for events from the contextual popupmenu */
onload:function()
	{
	var element = document.getElementById("contentAreaContextMenu");
	element.addEventListener("popupshowing",function(evt){MY.preparePopup(evt);},true);
	},
/* prepare the contextual menu just before it is showing on screen: hide or show our menu */
preparePopup:function(evt)
	{
	var element = document.getElementById("menuWikipedia");
	if(document.popupNode.id!="wpTextbox1")
		{
		element.hidden=true;
		return;
		}
	element.hidden=false;
	},
/** insert a text at the caret position in the textarea of wikipedia */
insertTemplate:function(text)
	{
	var area= content.document.getElementById("wpTextbox1");
	if(area==null) return;
	//alert(area.value.substring(0,20)+" "+area.tagName);
	var selstart=area.selectionStart;
	var x= area.scrollLeft;
	var y= area.scrollTop;
	area.value= 	area.value.substring(0,selstart)+
			text+
			area.value.substring(area.selectionEnd)
			;
	area.scrollLeft=x;
	area.scrollTop=y;
	selstart+=text.length;
	area.setSelectionRange(selstart,selstart);
	},
/* insert a wikipedia category */
category:function(text)
	{
	MY.insertTemplate("[[Category:"+text+"]]");
	},
/** get current article name */
article:function()
	{
	var url=""+content.document.location;
	var i=url.indexOf("title=",0);
	if(i==-1) return "";
	i+=6;
	var j=url.indexOf("&action",i);
	if(j==-1) return "";
	return unescape(url.substr(i,j-i).replace("_"," "));
	}
/*INSERT_CMD_HERE*/
};

window.addEventListener("load",MY.onload, false);
