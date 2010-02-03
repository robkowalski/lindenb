<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:atom="http://www.w3.org/2005/Atom"
	xmlns:openSearch="http://a9.com/-/spec/opensearchrss/1.0/"
	xmlns:georss="http://www.georss.org/georss"
	version='1.0'>
<!--

Author:
	Pierre Lindenbaum
	http://plindenbaum.blogspot.com

Motivation:
	creates an input for http://www.wordle.net/ (tag clouds)
	from the tags used in a blogger.com account.

Usage :
	xsltproc blogger2wordle.xsl 'http://www.blogger.com/feeds/<BLOG-ID>/posts/default'

Example:
	xsltproc blogger2wordle.xsl 'http://www.blogger.com/feeds/14688252/posts/default'
	
Parameters:
	none

-->
<xsl:output method="text" ident="yes"/>


<xsl:template match="/">
	<xsl:apply-templates select="atom:feed"/>
</xsl:template>

<xsl:template match="atom:feed">
<xsl:apply-templates select="atom:entry"/>
<xsl:if test="atom:link[@rel='next' and @href]">
	<xsl:variable name="url"><xsl:value-of select="atom:link[@rel='next']/@href"/></xsl:variable>
	<xsl:message terminate="no">Downloading <xsl:value-of select="$url"/></xsl:message>
	<xsl:apply-templates select="document($url,/atom:feed)"/>
</xsl:if>
</xsl:template>


<xsl:template match="atom:entry">
  <xsl:apply-templates select="atom:category[@term]"/>
</xsl:template>

<xsl:template match="atom:category">
  <xsl:value-of select="@term"/>
  <xsl:text> </xsl:text>
</xsl:template>

</xsl:stylesheet>