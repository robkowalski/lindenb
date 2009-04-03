<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	>
<!--

This stylesheet transform xml to html

-->
<xsl:output method="html"
     	indent="yes"
     	omit-xml-declaration="yes"
	/>



<xsl:template match="/">
<div style="font-family:monospace;white-space:pre; background-color:rgb(230,230,230);border: 1px solid black;width:100%;height:300px;overflow:auto;">
	<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="text()"><xsl:choose><xsl:when test="string-length(normalize-space(.))&gt;0"><span style="color:gray;"><xsl:value-of select="."/></span></xsl:when><xsl:otherwise><xsl:value-of select="."/></xsl:otherwise></xsl:choose></xsl:template>



<xsl:template match="@*"><xsl:text> </xsl:text><xsl:value-of select="name()"/>=&quot;<span style="color:gray;"><xsl:choose><xsl:when test="starts-with (.,&apos;http://&apos;)"><xsl:element name="a"><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute><xsl:value-of select="."/></xsl:element></xsl:when><xsl:otherwise><xsl:value-of select="."/></xsl:otherwise></xsl:choose></span>&quot;</xsl:template>

<xsl:template match="node()" name="element"><xsl:choose>
<xsl:when test="count(child::node()) != 0"><span>&lt;<xsl:value-of select="name()"/><xsl:apply-templates select="@*"/>&gt;</span><xsl:apply-templates/><span>&lt;/<xsl:value-of select="name()"/>&gt;</span></xsl:when>
<xsl:otherwise><span>&lt;<xsl:value-of select="name()"/><xsl:apply-templates select="@*"/>/&gt;</span></xsl:otherwise>
</xsl:choose>
</xsl:template>






</xsl:stylesheet>
