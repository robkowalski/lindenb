<?xml version='1.0' encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY CRLF "&#10;">
	  <!ENTITY CHARSXP "9">
	  ]>
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
	This stylesheet transform xml to R
See also:
	https://svn.r-project.org/R/trunk/src/main/serialize.c
	https://svn.r-project.org/R/trunk/src/include/Rinternals.h
-->
<xsl:output method="text" />

<!-- WriteItem -->
<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>


<xsl:template match="array|vector">
</xsl:template>

<xsl:template match="string|characters|char|text">
</xsl:template>


<xsl:template match="map|properties">
</xsl:template>

<!-- OutInteger -->
<xsl:template match="int|integer|long|short">
<xsl:choose>
  <xsl:when test="$content='NA' or $content='N/A'">
	<xsl:text>NA&CRLF;</xsl:text>
  </xsl:when>
  <xsl:otherwise>
	<xsl:value-of select="$content"/>
  </xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- OutReal -->
<xsl:template match="double|float|floating">
<xsl:variable name="content" select="."/>
<xsl:choose>
  <xsl:when test="$content='NA' or $content='N/A'">
	<xsl:text>NA&CRLF;</xsl:text>
  </xsl:when>
  <xsl:when test="$content='-INF' or $content='-Inf'">
	<xsl:text>-Inf&CRLF;</xsl:text>
  </xsl:when>
  <xsl:when test="$content='INF' or $content='Inf'">
	<xsl:text>Inf&CRLF;</xsl:text>
  </xsl:when>
  <xsl:otherwise>
	<xsl:value-of select="$content"/>
  </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="entry">
</xsl:template>

<!-- OutComplex -->
<xsl:template match="complex">
</xsl:template>

<!-- OutByte -->
<xsl:template match="byte">
</xsl:template>


<xsl:template name="escape">
 <xsl:param name="s"/>
 	<xsl:choose>
 	<xsl:when test='contains($s,"&apos;")'>
		<xsl:value-of select='substring-before($s,"&apos;")'/>
		<xsl:text>\"</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="s" select='substring-after($s,"&apos;")'/>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select='$s'/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>



</xsl:stylesheet>

