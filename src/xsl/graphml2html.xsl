<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 xmlns="http://www.w3.org/1999/xhtml"
 xmlns:g="http://graphml.graphdrawing.org/xmlns"
 >
<!--
Motivation:
	transforms a GRAPHML xml result to XHTML+SVG

Author:
	Pierre Lindenbaum PhD plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com

-->
<!-- ========================================================================= -->
<xsl:output
	method='xml' indent='no'
	omit-xml-declaration="yes"
	cdata-section-elements="script"
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
	/>


<!-- ========================================================================= -->

<!-- matching the root node -->
<xsl:template match="/">
<html>
<head>
<script>
var SVG={ NS:"http://www.w3.org/2000/svg" };
var RADIUS=50;
/** increase speed */
var mathsqrt = Math.sqrt;
var mathpow = Math.pow;
<xsl:for-each select="g:graphml/g:graph">
<xsl:variable name="graphid"><xsl:value-of select="generate-id(.)"/></xsl:variable>
/** timeout variable */
var thread<xsl:value-of select="$graphid"/>=null;

/**
 * The nodes
 */
var nodes<xsl:value-of select="$graphid"/>=
	[
	<xsl:for-each select="g:node">
		{
		id:"<xsl:value-of select="@id"/>",
		x: 0,
		y: 0,
		element:null,
		edges: new Array()
		}<xsl:if test="position()!=last()">,</xsl:if>
	</xsl:for-each>
	];
/** the edges */
var edges<xsl:value-of select="$graphid"/>= new Array();

/** return the index of a node from its id or -1 if not found */
function findNodeIndexById<xsl:value-of select="$graphid"/>(id)
	{
	for(var i in nodes<xsl:value-of select="$graphid"/>)
		{
		if( nodes<xsl:value-of select="$graphid"/>[i].id == id ) return i;
		}
	return -1;
	}	

function createLink<xsl:value-of select="$graphid"/>(source,target)
	{
	if(source==target) return;
	var i1 = findNodeIndexById<xsl:value-of select="$graphid"/>(source);
	if(i1==-1) return;
	var i2 = findNodeIndexById<xsl:value-of select="$graphid"/>(target);
	if(i2==-1) return;
	for(var n1 in  edges<xsl:value-of select="$graphid"/> )
		{
		var e= edges<xsl:value-of select="$graphid"/>[n1];
		if( (e.source==i1 &amp;&amp; e.target==i2) ||
		    (e.source==i2 &amp;&amp; e.target==i1) ) return;
		}
	var n = edges<xsl:value-of select="$graphid"/>.length;
	edges<xsl:value-of select="$graphid"/>.push({i1:i1,i2:i2,node:null});
	nodes<xsl:value-of select="$graphid"/>[i1].edges.push(n);
	nodes<xsl:value-of select="$graphid"/>[i2].edges.push(n);
	}


function init<xsl:value-of select="$graphid"/>()
	{
	var svg= document.getElementById("svg<xsl:value-of select="$graphid"/>");
	for(var i in nodes<xsl:value-of select="$graphid"/>)
		{
		var node= nodes<xsl:value-of select="$graphid"/>[i];
		node.x= 100+Math.random();
		node.y= 100+Math.random();
		var g= document.createElementNS(SVG.NS,"svg:g");
		g.setAttribute("transform","translate("+node.x+" "+node.y+")"); 

		var circle= document.createElementNS(SVG.NS,"svg:circle");
		g.appendChild(circle);
		circle.setAttribute("cx","0");
		circle.setAttribute("cy","0");
		circle.setAttribute("r",""+RADIUS);
		circle.setAttribute("fill","red");
		
		node.element= g;
		svg.appendChild(g);
		}
	<xsl:for-each select="g:edge[@source][@target]">
	<xsl:if test="@source != @target ">
	createLink<xsl:value-of select="$graphid"/>(
		"<xsl:value-of select="@source"/>",
		"<xsl:value-of select="@target"/>"
		);
	</xsl:if>
	</xsl:for-each>
	var edges= edges<xsl:value-of select="$graphid"/>;
	for(var i in edges)
		{
		var edge= edges[i];
		var L= document.createElementNS(SVG.NS,"svg:line");
		L.setAttribute("x1",""+nodes<xsl:value-of select="$graphid"/>[edge.i1].x);
		L.setAttribute("y1",""+nodes<xsl:value-of select="$graphid"/>[edge.i1].y);
		L.setAttribute("x2",""+nodes<xsl:value-of select="$graphid"/>[edge.i2].x);
		L.setAttribute("y2",""+nodes<xsl:value-of select="$graphid"/>[edge.i2].y);
		L.setAttribute("stroke","black");
		svg.appendChild(L);
		}
	}

function <xsl:value-of select="$graphid"/>distance( i1, i2 )
	{
	var n1=nodes<xsl:value-of select="$graphid"/>[i1];
	var n2=nodes<xsl:value-of select="$graphid"/>[i2];
	var d = mathsqrt(
		mathpow(n1.x-n2.x,2),
		mathpow(n1.y-n2.y,2)
		);
	return d;
	}


var current<xsl:value-of select="$graphid"/>=-1;
function run<xsl:value-of select="$graphid"/>()
	{
	current<xsl:value-of select="$graphid"/>++;
	
	if(current<xsl:value-of select="$graphid"/>==nodes<xsl:value-of select="$graphid"/>.length)
		{
		current<xsl:value-of select="$graphid"/> = 0;
		}
	var currentnode= nodes<xsl:value-of select="$graphid"/>[current<xsl:value-of select="$graphid"/>];
	var net_force=0;
	var dx=0;
	var dy=0;
	for(var i in nodes<xsl:value-of select="$graphid"/>)
		{
		break;
		if(i==current<xsl:value-of select="$graphid"/>) continue;
		var node= nodes<xsl:value-of select="$graphid"/>[i];
		var d= <xsl:value-of select="$graphid"/>distance(i,current<xsl:value-of select="$graphid"/>);
		if(d &gt; RADIUS) continue;
		
		}
	dx=10.0*Math.random();
	dy=10.0*Math.random();
	
	currentnode.x+=dx;
	currentnode.y+=dy;
	currentnode.element.setAttribute("transform","translate("+currentnode.x+" "+currentnode.y+")"); 
	}

</xsl:for-each>

/** init all graphs */
function init()
	{
	<xsl:for-each select="g:graphml/g:graph">
	init<xsl:value-of select="generate-id(.)"/>();
	</xsl:for-each>
	<xsl:for-each select="g:graphml/g:graph">
	thread<xsl:value-of select="generate-id(.)"/>=setInterval(run<xsl:value-of select="generate-id(.)"/>,100);;
	</xsl:for-each>
	}
</script>
<title>GraphML2SVG</title>
</head>
<body onload="init();">
<xsl:for-each select="g:graphml/g:graph">
<div>
<xsl:element name="svg:svg">
<xsl:attribute name="version">1.0</xsl:attribute>
<xsl:attribute name="width">600</xsl:attribute>
<xsl:attribute name="height">600</xsl:attribute>
<xsl:attribute name="id">svg<xsl:value-of select="generate-id(.)"/></xsl:attribute>

<xsl:element name="svg:rect">
<xsl:attribute name="x">0</xsl:attribute>
<xsl:attribute name="y">0</xsl:attribute>
<xsl:attribute name="width"><xsl:value-of select="number(600) - 1"/></xsl:attribute>
<xsl:attribute name="height"><xsl:value-of select="number(600) - 1"/></xsl:attribute>
<xsl:attribute name="stroke">black</xsl:attribute>
<xsl:attribute name="fill">none</xsl:attribute>
</xsl:element>

</xsl:element>
</div>
</xsl:for-each>
</body>
</html>
</xsl:template>


</xsl:stylesheet>