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
<xsl:param name="R_DefaultSerializeVersion" select="number(2)"/>
<xsl:param name="R_VERSION">133633</xsl:param>
<xsl:param name="R_Version">131840</xsl:param>

<!--  R_Serialize -->
<xsl:template match="/">
  <!-- InitConnOutPStream -->
  
  <!--  OutFormat -->
  <xsl:value-of select="concat('RDA',$R_DefaultSerializeVersion,'&CRLF;')"/>
  <xsl:text>A&CRLF;</xsl:text>
  <xsl:value-of select="concat($R_DefaultSerializeVersion,'&CRLF;')"/>
  <xsl:value-of select="concat($R_VERSION,'&CRLF;')"/>
  <xsl:value-of select="concat($R_Version,'&CRLF;')"/>
  
<xsl:apply-templates/>
</xsl:template>


<xsl:template match="array|vector">
<xsl:variable name="count" select="count(*)"/>
<xsl:variable name="count_integers" select="count(int|integer|long)"/>
<xsl:variable name="count_strings" select="count(int|integer|long)"/>
</xsl:template>

<xsl:template match="string|characters|char|text">
</xsl:template>


<xsl:template match="map|properties">
</xsl:template>

<!-- OutInteger -->
<xsl:template match="int|integer|long">
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

