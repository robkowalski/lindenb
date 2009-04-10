<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	>
<!--

Author:
	Pierre Lindenbaum PhD
	plindenbaum@yahoo.fr

Motivation:
	This stylesheet transform xml/DOM to javascript statements

-->
<xsl:output method="text" />
<xsl:variable name="datadoc">true</xsl:variable>
<xsl:variable name="NodeType">var</xsl:variable>

<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>



<xsl:template match="text()">
<xsl:if test=".."><xsl:if test="$datadoc='false' or ($datadoc='true' and count(../child::*) = 0)">
<xsl:value-of select="translate(local-name(..),'-','_')"/>_<xsl:value-of select="generate-id(..)"/>.appendChild(document.createTextNode("<xsl:call-template name="escape"><xsl:with-param name="s"><xsl:value-of select="."/></xsl:with-param></xsl:call-template>"));<xsl:text>
</xsl:text>
</xsl:if></xsl:if>
</xsl:template>

<xsl:template match="node()">
<xsl:variable name="owner"><xsl:value-of select="translate(local-name(.),'-','_')"/>_<xsl:value-of select="generate-id(.)"/></xsl:variable>
<xsl:value-of select="$NodeType"/><xsl:text> </xsl:text><xsl:value-of select="$owner"/>= <xsl:choose>
<xsl:when test="namespace-uri(.)=''">document.createElement("<xsl:value-of select="name(.)"/>");</xsl:when>
<xsl:otherwise>document.createElementNS(<xsl:call-template name="namespace"><xsl:with-param name="uri"><xsl:value-of select="namespace-uri(.)"/></xsl:with-param></xsl:call-template>,"<xsl:value-of select="name(.)"/>");</xsl:otherwise>
</xsl:choose><xsl:text>
</xsl:text>
<xsl:if test="..">
<xsl:value-of select="translate(local-name(..),'-','_')"/>_<xsl:value-of select="generate-id(..)"/>.appendChild(<xsl:value-of select="$owner"/>);
</xsl:if>


<xsl:for-each select="@*">
<xsl:value-of select="$owner"/>.<xsl:choose>
<xsl:when test="namespace-uri(.)=''">setAttribute("<xsl:value-of select="name(.)"/>","<xsl:call-template name="escape"><xsl:with-param name="s"><xsl:value-of select="."/></xsl:with-param></xsl:call-template>");</xsl:when>
<xsl:otherwise>setAttributeNS(<xsl:call-template name="namespace"><xsl:with-param name="uri"><xsl:value-of select="namespace-uri(.)"/></xsl:with-param></xsl:call-template>,"<xsl:value-of select="name(.)"/>","<xsl:call-template name="escape"><xsl:with-param name="s"><xsl:value-of select="."/></xsl:with-param></xsl:call-template>");</xsl:otherwise>
</xsl:choose><xsl:text>
</xsl:text>
</xsl:for-each>

<xsl:apply-templates select="*|text()"/>
</xsl:template>

<xsl:template name="namespace">
<xsl:param name="uri"/>
<xsl:choose>
	<xsl:when test="$uri='http://www.w3.org/1999/XSL/Transform'">XSL.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/xhtml'">HTML.NS</xsl:when>
	<xsl:when test="$uri='http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul'">XUL.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/2000/svg'">SVG.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/xlink'">XLINK.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/02/22-rdf-syntax-ns#'">RDF.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/2000/01/rdf-schema#'">RDFS.NS</xsl:when>
	<xsl:otherwise>"<xsl:value-of select="$uri"/>"</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="escape">
<xsl:param name="s"/><xsl:variable name="c"><xsl:value-of select="substring($s,1,1)"/></xsl:variable>
<xsl:choose>
 <xsl:when test="$c='&#xA;'">\n</xsl:when>
 <xsl:when test='$c="&#39;"'>\'</xsl:when>
 <xsl:when test="$c='&#34;'">\"</xsl:when>
 <xsl:otherwise><xsl:value-of select="$c"/></xsl:otherwise>
</xsl:choose><xsl:if test="string-length($s) &gt;1"><xsl:call-template name="escape">
<xsl:with-param name="s"><xsl:value-of select="substring($s,2,string-length($s)-1)"/></xsl:with-param>
</xsl:call-template></xsl:if>
</xsl:template>


</xsl:stylesheet>

