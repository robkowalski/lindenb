<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	>
<!--

This stylesheet creates a TagCloud in XML

-->
<xsl:output method="html"
     	indent="yes"
     	omit-xml-declaration="yes"
	/>
<xsl:variable name="minSize">100</xsl:variable>
<xsl:variable name="maxSize">500</xsl:variable>

<xsl:template match="/">
 <xsl:apply-templates select="TagCloud"/>
</xsl:template>

<xsl:template match="TagCloud">
<div> 
<xsl:variable name="min">
  <xsl:for-each select="Tag">
    <xsl:sort select="weight" data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="max">
  <xsl:for-each select="Tag">
    <xsl:sort select="weight" data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:for-each select="Tag">
    <xsl:sort select="label" order="ascending" />
    <xsl:choose>
	<xsl:when test="url">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
			<xsl:call-template name="makeTag">
				<xsl:with-param name="min"><xsl:value-of select="$min"/></xsl:with-param>
				<xsl:with-param name="max"><xsl:value-of select="$max"/></xsl:with-param>
				<xsl:with-param name="tag" select="."/>
			</xsl:call-template>
		</xsl:element>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="makeTag">
			<xsl:with-param name="min"><xsl:value-of select="$min"/></xsl:with-param>
			<xsl:with-param name="max"><xsl:value-of select="$max"/></xsl:with-param>
			<xsl:with-param name="tag" select="."/>
		</xsl:call-template>
	</xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
</xsl:for-each>

</div>
</xsl:template>

<xsl:template name="makeTag">
<xsl:param name="tag"/>
<xsl:param name="min"/>
<xsl:param name="max"/>
	<xsl:element name="span">
	<xsl:attribute name="style">
		<xsl:text>font-size:</xsl:text>
		<xsl:value-of select="$minSize + ((weight - $min) div ($max - $min))*($maxSize - $minSize)"/>
		<xsl:text>%;</xsl:text>
		<xsl:if test="color">
			<xsl:text>color:</xsl:text>
			<xsl:value-of select="color"/>
			<xsl:text>;</xsl:text>
		</xsl:if>
	</xsl:attribute>
	<xsl:if test="title">
		<xsl:attribute name="title">
			<xsl:value-of select="title"/>
		</xsl:attribute>
	</xsl:if>
	
	
		<xsl:value-of select="label"/>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>
