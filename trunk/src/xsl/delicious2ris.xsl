<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:dia="http://www.lysator.liu.se/~alla/dia/"
	version='1.0'
	>

<!--

This stylesheet transforms the output of endnode xml to RIS 


-->
<xsl:output method="text" version="1.0" encoding="ASCII"/>

<xsl:template match="/">
<xsl:apply-templates select="posts"/>
</xsl:template>

<xsl:template match="posts">
<xsl:apply-templates select="post"/>
</xsl:template>

<xsl:template match="post[not(@shared) or @shared!='no']">
<xsl:choose>
<xsl:when test="starts-with(@href,'http://www.ncbi.nlm.nih.gov')">
<xsl:text>TY  - JOUR
</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>TY  - GEN
</xsl:text>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates select="@description"/>
<xsl:apply-templates select="@href"/>
<xsl:apply-templates select="@tag"/>
<xsl:apply-templates select="@extended"/>
<xsl:text>ER  - 

</xsl:text>
</xsl:template>

<xsl:template match="@href">
<xsl:text>UR  - </xsl:text><xsl:value-of select="."/><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="@description">
<xsl:text>T1  - </xsl:text><xsl:value-of select="."/><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="@extended">
<xsl:text>AB  - </xsl:text><xsl:value-of select="."/><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="@tag">
<xsl:call-template name="kw">
 <xsl:with-param name="s" select="normalize-space(.)"/>
</xsl:call-template>
</xsl:template>


<xsl:template name="kw">
<xsl:param name="s"/>
<xsl:choose>
<xsl:when test="contains($s,' ')">
	<xsl:call-template name="kw">
		<xsl:with-param name="s" select="normalize-space(substring-before($s,' '))"/>
	</xsl:call-template>
	<xsl:call-template name="kw">
		<xsl:with-param name="s" select="normalize-space(substring-after($s,' '))"/>
	</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:text>KW  - </xsl:text><xsl:value-of select="$s"/><xsl:text>
</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


</xsl:stylesheet>


