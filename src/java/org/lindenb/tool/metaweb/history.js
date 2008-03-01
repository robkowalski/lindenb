var XUL={
NS:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"
};

var XHTML={
NS:"http://www.w3.org/1999/xhtml"
};

function StartDate(year,month,dayOfMonth)
	{
	this.year=year;
	this.month=month;
	this.dayOfMonth=dayOfMonth;
	}

StartDate.prototype.days=function()
	{
	var d= this.year*365.25;
	if(this.month!=null)
		{
		d+=(this.month*(365.25/12.0));
		if(this.dayOfMonth!=null)
			{
			d+=this.dayOfMonth;
			}
		}
	return d;
	}

	
	
function EndDate(year,month,dayOfMonth)
	{
	this.year=year;
	this.month=month;
	this.dayOfMonth=dayOfMonth;
	}

EndDate.prototype.days=function()
	{
	var v= 0;
	if(this.month!=null)
		{
		if(this.dayOfMonth!=null)
			{
			v+=(1+this.dayOfMonth);
			v+=this.month*(365.25/12.0);
			}
		else
			{
			v+=(this.month+1)*(365.25/12.0);
			}
		v+=this.year*365.25;
		}
	else
		{
		v+=(1+this.year)*365.25;
		}
	return v;
	}	
	

var MY={
now: null,
iconSize:64,
screenWidth:15000,
minDate:null,
maxDate:null,
debug:function(msg)
	{
	var message=document.getElementById("message");
	if(message==null) return;
	MY.removeAllChild(message);
	message.appendChild(document.createTextNode(msg==null?"null":msg));
	},
x1:function(person)
	{
	return MY.convertDate2Pixel(person.birthDate);
	},
x2:function(person)
	{
	var d= person.deathDate;
	if(d==null)
		{
		d= MY.now;
		}
	return MY.convertDate2Pixel(d);
	},
convertDate2Pixel:function(date)
        {
        return MY.screenWidth*((date.days()-MY.minDate.days())/(MY.maxDate.days()-MY.minDate.days()));
        },
removeAllChild:function(root)
	{
	if(root==null) return;
	while(root.hasChildNodes())
		{
		root.removeChild(root.firstChild);
		}
	},
loaded:function()
	{
	var d= new Date();
	MY.now = new EndDate(d.getFullYear(),1+d.getMonth(),d.getUTCDate());
	
	var set= new Array()
	for(var i=0;i< persons.length;++i)
		{
		for(var j=0;j< persons[i].profession.length;++j)
			{
			set[persons[i].profession[j] ]=1;
			}
		}
	MY.fillListBox("profession",set);
	
	//knownfor
	set= new Array()	
	for(var i=0;i< persons.length;++i)
		{
		for(var j=0;j< persons[i].knownFor.length;++j)
			{
			set[persons[i].knownFor[j] ]=1;
			}
		}
	MY.fillListBox("knownfor",set);
	
	//country
	set= new Array()	
	for(var i=0;i< persons.length;++i)
		{
		for(var j=0;j< persons[i].nationality.length;++j)
			{
			set[persons[i].nationality[j] ]=1;
			}
		}
	MY.fillListBox("country",set);
	
	//AWARDS
	set= new Array()	
	for(var i=0;i< persons.length;++i)
		{
		for(var j=0;j< persons[i].awards.length;++j)
			{
			set[persons[i].awards[j] ]=1;
			}
		}
	MY.fillListBox("awards",set);
	
	MY.pileup();
	},
fillListBox:function(id,set)
	{
	var root=document.getElementById(id);
	if(root==null) return;
	var array2= new Array(set.length);
	for(var p in set)
		{
		array2.push(p);
		}
	array2.sort();
	for(var j=0;j< array2.length;++j)
		{
		var item=document.createElementNS(XUL.NS,"listitem");
		item.setAttribute("label",array2[j]);
		item.setAttribute("value",array2[j]);
		root.appendChild(item);
		}
	},
selectedItems:function(id)
	{
	var set= new Array();
	var root=document.getElementById(id);
	if(root==null) return set;
	var selected= root.selectedItems;
	
	for(var i=0;i<selected.length;++i)
		{
		set.push(selected[i].value);
		}
	return set;
	},
date2text:function(date)
	{
	var s=""+date.year;
	if(date.month!=null)
		{
		s+=" ";
		switch(date.month)
			{
			case 1: s+=("Jan"); break;
			case 2: s+=("Feb"); break;
			case 3: s+=("Mar"); break;
			case 4: s+=("Apr"); break;
			case 5: s+=("May"); break;
			case 6: s+=("Jun"); break;
			case 7: s+=("Jul"); break;
			case 8: s+=("Aug"); break;
			case 9: s+=("Sep"); break;
			case 10: s+=("Oct"); break;
			case 11: s+=("Nov"); break;
			case 12: s+=("Dec"); break;
			default: s+=date.month; break;
			}
		if(date.dayOfMonth!=null)
			{
			s+=" "+date.dayOfMonth;
			}
		}
	return s;
	},
containsSet:function(set,subset)
	{
	if(subset.length==0) return true;
	for(var i=0;i< subset.length;++i)
		{
		if(set.indexOf(subset[i])!=-1) return true;
		}
	return false;
	},
update:function()
	{
	for(var i=0;i< persons.length;++i)
		{
		persons[i].selected=true;
		}
	var sel=MY.selectedItems("profession");
	
	for(var i=0;i< persons.length && sel.length>0;++i)
		{
		if(!MY.containsSet(persons[i].profession,sel))
			{
			persons[i].selected=false;
			}
		}
	sel=MY.selectedItems("knownfor");
	
	for(var i=0;i< persons.length && sel.length>0;++i)
		{
		if(!persons[i].selected) continue;
		if(!MY.containsSet(persons[i].knownFor,sel))
			{
			persons[i].selected=false;
			}
		}
	
	sel=MY.selectedItems("country");
	
	for(var i=0;i< persons.length && sel.length>0;++i)
		{
		if(!persons[i].selected) continue;
		if(!MY.containsSet(persons[i].nationality,sel))
			{
			persons[i].selected=false;
			}
		}	
	
	sel=MY.selectedItems("awards");
	
	for(var i=0;i< persons.length && sel.length>0;++i)
		{
		if(!persons[i].selected) continue;
		if(!MY.containsSet(persons[i].awards,sel))
			{
			persons[i].selected=false;
			}
		}
	
	sel=MY.selectedItems("gender");
	
	for(var i=0;i< persons.length && sel.length>0;++i)
		{
		if(!persons[i].selected ) continue;
		if(sel.indexOf(persons[i].gender)==-1)
			{
			persons[i].selected=false;
			}
		}	
		
	for(var i=0;i< persons.length ;++i)
		{
		persons[i].node.style.opacity=(persons[i].selected?1.0:0.3);
		}
	},
simpleTag:function(tag,text)
	{
	var e = document.createElementNS(XHTML.NS,tag);
	e.appendChild(document.createTextNode(text));
	return e;
	},
bold:function(text) { return MY.simpleTag("h:b",text);},
italic:function(text) { return MY.simpleTag("h:i",text);},
underline:function(text) { return MY.simpleTag("h:u",text);},
pileup:function()
	{
	var remains=new Array(persons.length);
	for(var i=0;i< persons.length;++i) remains[i]=persons[i];
	MY.minDate=null;
	MY.maxDate=null;
	for(var i=0;i< remains.length;++i)
		{
		var o=remains[i];
		if(MY.minDate==null || o.birthDate.days() < MY.minDate.days())
			{
			MY.minDate= o.birthDate;
			}
		if(o.deathDate!=null && (MY.maxDate==null || MY.maxDate.days()< o.deathDate.days()))
			{
			MY.maxDate= o.deathDate;
			}
		}
	if(MY.minDate==null || MY.maxDate==null) return;
	
	var nLine=-1;
	while(remains.length>0)
		{
		++nLine;
		var first=remains[0];
		remains=remains.slice(1);
		first.y=nLine;

		while(true)
			{
			var best=null;
			var bestIndex=-1;
			for(var i=0;i< remains.length;++i)
				{
				var next=remains[i];
				if(MY.x1(next)< MY.x2(first)+5) continue;
				if(best==null ||
				(MY.x1(next)-MY.x2(first) < MY.x1(best)-MY.x2(first)))
					{
					best=next;
					bestIndex=i;
					}
				}
			if(best==null) break;
			first=best;
			first.y=nLine;
			remains.splice(bestIndex,1);
			}
		}
	
	var timeline=document.getElementById("timeline");
	MY.removeAllChild(timeline);
	var MARGIN=2;
	var HEIGHT=MY.iconSize+MARGIN*2;
	for(var i=0;i< persons.length;++i)
		{
		var o= persons[i];
		var stack= document.createElementNS(XUL.NS,"stack");
		var style="top:"+Math.round(o.y*(HEIGHT+10))+"px;"+
			"left:"+Math.round(MY.x1(o))+"px;"+
			"width:"+Math.round(MY.x2(o)-MY.x1(o))+"px;"+
			"height:"+Math.round(HEIGHT)+"px;"+
			"background-color:black;"+
			"color:white;"+
			"border-width:2px;"+
			"border-color:red;"+
			"overflow:hidden;"+
			"font-size:11px;"+
			"opacity: 1;"
			;
		o.node=stack;
		stack.setAttribute("style",style);
		
		
		var hbox= document.createElementNS(XUL.NS,"hbox");
		hbox.setAttribute("flex","1");
		stack.appendChild(hbox);
		if(o.img!=null)
			{
			var img= document.createElementNS(XUL.NS,"image");
			hbox.appendChild(img);
			img.setAttribute("src",o.img);
			img.setAttribute("style","width:"+MY.iconSize+"px;height:"+MY.iconSize+"px;");
			}
		
		var div= document.createElementNS(XHTML.NS,"h:div");
		div.setAttribute("style","width:"+Math.round(MY.x2(o)-MY.x1(o)-(o.img==null?0:MY.iconSize))+"px;");
		//div= document.createElementNS(XUL.NS,"label");
		hbox.appendChild(div);
		
		var anchor= document.createElementNS(XHTML.NS,"h:a");
		anchor.appendChild(document.createTextNode(o.name));
		anchor.setAttribute("href","http://www.freebase.com/view/guid/"+o.guid);
		anchor.setAttribute("target",o.guid);
		anchor.setAttribute("title",o.name);
		div.appendChild(anchor);
		
		div.appendChild(document.createTextNode(" : "));
		if(o.birthDate!=null)
			{
			div.appendChild(document.createTextNode(MY.date2text(o.birthDate)));
			if(o.birthPlace!=null)
				{
				div.appendChild(document.createTextNode(" at "+o.birthPlace));
				}
			div.appendChild(document.createTextNode(" - "));
			}
		if(o.deathDate!=null)
			{
			div.appendChild(document.createTextNode(MY.date2text(o.deathDate)));
			if(o.deathPlace!=null)
				{
				div.appendChild(document.createTextNode(" at "+o.deathPlace));
				}
			}
		div.appendChild(document.createTextNode(":"+o.shortBio+" "));
		if(o.knownFor.length>0)
			{
			div.appendChild(MY.bold("Known For:"));
			for(var k in o.knownFor)
				{
				div.appendChild(document.createTextNode(o.knownFor[k]+" "));
				}
			}
		timeline.appendChild(stack);
		}
	
	
	}

};
