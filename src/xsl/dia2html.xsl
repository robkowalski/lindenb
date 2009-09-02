<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:dia="http://www.lysator.liu.se/~alla/dia/"
	version='1.0'
	>
<!--
Motivation:
	transforms a DIA file to XHTML

Author:
	Pierre Lindenbaum PhD plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com

-->
<xsl:output
	method='xml' indent='no'
	omit-xml-declaration="yes"
	cdata-section-elements=""
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
	/>
<xsl:param name="pixelperinch" select="72.0"/>
<xsl:template match="/dia:diagram">
<html><body>
<xsl:element name="div">
<xsl:attribute name="style">
<xsl:text>color:</xsl:text><xsl:value-of select="dia:diagramdata/dia:attribute[@name='color']/dia:color/@val"/><xsl:text>;</xsl:text>
<xsl:text>background-color:</xsl:text><xsl:value-of select="dia:diagramdata/dia:attribute[@name='background']/dia:color/@val"/><xsl:text>;</xsl:text>
</xsl:attribute>
<xsl:apply-templates select="dia:layer"/>
</xsl:element></body></html>
</xsl:template>


<xsl:template match="dia:attribute[@name='background' or @name='fill_color']">
<xsl:text>background-color:</xsl:text>
<xsl:value-of select="dia:color/@val"/>
<xsl:text>;</xsl:text>
</xsl:template>

<xsl:template match="dia:attribute[@name='color']">
<xsl:text>color:</xsl:text>
<xsl:value-of select="dia:color/@val"/>
<xsl:text>;</xsl:text>
</xsl:template>


<xsl:template match="dia:attribute[@name='visibility']">
<xsl:choose>
<xsl:when test="dia:enum/@val='0'"><span title="public">+</span></xsl:when>
<xsl:when test="dia:enum/@val='1'"><span title="private">-</span></xsl:when>
<xsl:when test="dia:enum/@val='2'"><span title="protected">#</span></xsl:when>
<xsl:otherwise><xsl:value-of select="dia:enum/@val"/>????</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="dia:string">
	<xsl:value-of select="substring(.,2,string-length(.)-2)"/>
</xsl:template>

<xsl:template match="dia:font">
	<xsl:text>font-family:"</xsl:text>
	<xsl:value-of select="@name"/>
	<xsl:text>",</xsl:text>
	<xsl:value-of select="@family"/>
	<xsl:text>;</xsl:text>
</xsl:template>

<xsl:template match="dia:layer">
 <xsl:element name="div">
  <xsl:attribute name="style">
  <xsl:choose>
    <xsl:when test="@visible='true'"></xsl:when>
    <xsl:otherwise>display:none;</xsl:otherwise>
   </xsl:choose>
  </xsl:attribute>
  <xsl:apply-templates select="dia:object[@type='UML - Generalization']"/>
  <xsl:apply-templates select="dia:object[@type='UML - Class']"/>
  </xsl:element>
</xsl:template>

<xsl:template match="dia:object[@type='UML - Generalization']">
<xsl:variable name="canvasid" select="concat('canvas',generate-id(.))"/>
<xsl:variable name="obj_bb" select="dia:attribute[@name='obj_bb']/dia:rectangle/@val"/>
<xsl:variable name="left" select="substring-before($obj_bb,';')"/>
<xsl:variable name="right" select="substring-after($obj_bb,';')"/>
<xsl:variable name="x" select="number(substring-before($left,','))"/>
<xsl:variable name="y" select="number(substring-after($left,','))"/>
<xsl:variable name="maxx" select="number(substring-before($right,','))"/>
<xsl:variable name="maxy" select="number(substring-after($right,','))"/>

<xsl:element name="canvas">
<xsl:attribute name="style">
<xsl:text>position:relative; border:solid;</xsl:text>

  <xsl:text>top:</xsl:text><xsl:value-of select="$x"/><xsl:text>in;</xsl:text>
  <xsl:text>left:</xsl:text><xsl:value-of select="$y"/><xsl:text>in;</xsl:text>

</xsl:attribute>
<xsl:attribute name="id"><xsl:value-of select="$canvasid"/></xsl:attribute>
<xsl:attribute name="width"><xsl:value-of select="$pixelperinch * ($maxx - $x)"/></xsl:attribute>
<xsl:attribute name="height"><xsl:value-of select="$pixelperinch * ($maxy - $y)"/></xsl:attribute>
</xsl:element>
<xsl:element name="script">
<xsl:text>
function draw</xsl:text><xsl:value-of select="$canvasid"/><xsl:text>()
	{
	var canvas = document.getElementById('</xsl:text><xsl:value-of select="$canvasid"/><xsl:text>');
	var ctx = canvas.getContext('2d');
	
	ctx.strokeStyle="</xsl:text><xsl:value-of select="dia:attribute[@name='line_colour']/dia:color/@val"/><xsl:text>";
	
	ctx.beginPath();
	</xsl:text>
	<xsl:for-each select="dia:attribute[@name='orth_points']/dia:point">
	  <xsl:variable name="x1" select="$pixelperinch * (number(substring-before(@val,',')) - $x)"/>
          <xsl:variable name="y1" select="$pixelperinch * (number(substring-after(@val,',')) - $y)"/>
	  <xsl:choose>
	   <xsl:when test="position()=1">
	   	<xsl:text>ctx.moveTo(</xsl:text>
		<xsl:value-of select="$x1"/>
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$y1"/>
		<xsl:text>);</xsl:text>
	   </xsl:when>
	   <xsl:otherwise>
	   	<xsl:text>ctx.lineTo(</xsl:text>
		<xsl:value-of select="$x1"/>
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$y1"/>
		<xsl:text>);</xsl:text>
	   </xsl:otherwise>
	  </xsl:choose>
	</xsl:for-each>
	<xsl:text>
	ctx.stroke();
	}
draw</xsl:text><xsl:value-of select="$canvasid"/><xsl:text>();
</xsl:text>
</xsl:element>

</xsl:template>

<xsl:template match="dia:object[@type='UML - Class']">
  <xsl:variable name="obj_pos" select="dia:attribute[@name='obj_pos']/dia:point/@val"/>
  <xsl:variable name="x" select="number(substring-before($obj_pos,','))"/>
  <xsl:variable name="y" select="number(substring-after($obj_pos,','))"/>
  
  
  <xsl:element name="div">
  <xsl:attribute name="style">
  <xsl:text>white-space:nowrap;position:relative;border-style:solid;overflow:auto;</xsl:text>
  <xsl:text>top:</xsl:text><xsl:value-of select="$x"/><xsl:text>in;</xsl:text>
  <xsl:text>left:</xsl:text><xsl:value-of select="$y"/><xsl:text>in;</xsl:text>
  <xsl:text>width:</xsl:text><xsl:value-of select="dia:attribute[@name='elem_width']/dia:real/@val"/><xsl:text>in;</xsl:text>
  <xsl:text>height:</xsl:text><xsl:value-of select="dia:attribute[@name='elem_height']/dia:real/@val"/><xsl:text>in;</xsl:text>
  <xsl:text>border-color:</xsl:text><xsl:value-of select="dia:attribute[@name='line_color']/dia:color/@val"/><xsl:text>;</xsl:text>
  <xsl:text>color:</xsl:text><xsl:value-of select="dia:attribute[@name='text_color']/dia:color/@val"/><xsl:text>;</xsl:text>
  <xsl:text>background-color:</xsl:text><xsl:value-of select="dia:attribute[@name='fill_color']/dia:color/@val"/><xsl:text>;</xsl:text>
  <xsl:apply-templates select="dia:attribute[@name='normal_font']/dia:font"/>
  <xsl:text>font-size:</xsl:text><xsl:value-of select="dia:attribute[@name='normal_font_height']/dia:real/@val"/><xsl:text>in;</xsl:text>
  <xsl:text>line-height:</xsl:text><xsl:value-of select="dia:attribute[@name='normal_font_height']/dia:real/@val"/><xsl:text>in;</xsl:text>
 </xsl:attribute>
 
  
   <xsl:element name="div">
    
    <xsl:attribute name="style">
     <xsl:text>text-align:center;</xsl:text>
     <xsl:apply-templates select="dia:attribute[@name='classname_font']/dia:font"/>
     <xsl:text>font-size:</xsl:text><xsl:value-of select="dia:attribute[@name='classname_font_height']/dia:real/@val"/><xsl:text>in;</xsl:text>
    </xsl:attribute>
     <xsl:apply-templates select="dia:attribute[@name='name']/dia:string"/>
   </xsl:element>
  
  <xsl:if  test="dia:attribute[@name='visible_attributes']/dia:boolean/@val='true'">
  <xsl:element name="div">
     <xsl:attribute name="style">
     <xsl:text>border-top-style:solid;</xsl:text>
     </xsl:attribute>
     <xsl:for-each select="dia:attribute[@name='attributes']/dia:composite">
     <xsl:element name="span">
      <xsl:attribute name="style">
       <xsl:if test="dia:attribute[@name='class_scope']/dia:boolean/@val='true'">
         <xsl:text>text-decoration: underline;</xsl:text>
       </xsl:if>
      </xsl:attribute>
     
      <xsl:apply-templates select="dia:attribute[@name='visibility']"/>
      <xsl:apply-templates select="dia:attribute[@name='name']/dia:string"/>
      <xsl:text> : </xsl:text>
      <xsl:apply-templates select="dia:attribute[@name='type']/dia:string"/>
      <xsl:if test="dia:attribute[@name='value']/dia:string != '##' ">
       <xsl:text> = </xsl:text>
        <xsl:apply-templates select="dia:attribute[@name='value']/dia:string"/>
       </xsl:if>
     </xsl:element>
     <xsl:if test="position()!=last()">
         <xsl:element name="br"/>
       </xsl:if>
     </xsl:for-each>
  </xsl:element>
  </xsl:if>
  
 <xsl:if  test="dia:attribute[@name='visible_operations']/dia:boolean/@val='true'">
  <xsl:element name="div">
     <xsl:attribute name="style">
     <xsl:text>border-top-style:solid;</xsl:text>
    
     </xsl:attribute>
     <xsl:for-each select="dia:attribute[@name='operations']/dia:composite">
      <xsl:element name="span">
      <xsl:attribute name="style">
       <xsl:if test="dia:attribute[@name='class_scope']/dia:boolean/@val='true'">
         <xsl:text>text-decoration: underline;</xsl:text>
       </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="dia:attribute[@name='visibility']"/>
      <xsl:apply-templates select="dia:attribute[@name='name']/dia:string"/>
      
       <xsl:text>(</xsl:text>
       <xsl:for-each select="dia:attribute[@name='parameters']/dia:composite">
		<xsl:if test="position()!=1">
			<xsl:text>,</xsl:text>
		</xsl:if>
       		<xsl:apply-templates select="dia:attribute[@name='name']/dia:string"/>
		<xsl:text> : </xsl:text>
		<xsl:apply-templates select="dia:attribute[@name='type']/dia:string"/>
		<xsl:if test="dia:attribute[@name='value']/dia:string != '##' ">
			<xsl:text> = </xsl:text>
			<xsl:apply-templates select="dia:attribute[@name='value']/dia:string"/>
		</xsl:if>
       </xsl:for-each>
       <xsl:text>)</xsl:text>
       
       <xsl:if test="dia:attribute[@name='type']/dia:string != '##' ">
			<xsl:text> : </xsl:text>
			<xsl:apply-templates select="dia:attribute[@name='type']/dia:string"/>
	</xsl:if>
       </xsl:element>
       <xsl:if test="position()!=last()">
         <xsl:element name="br"/>
       </xsl:if>
       
     </xsl:for-each>
  </xsl:element>
  </xsl:if>
   
  </xsl:element>

</xsl:template>

</xsl:stylesheet>