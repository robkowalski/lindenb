<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:bio="http://ontology.lindenb.org/resume/"
	version='1.0'
	>
<!--

This stylesheet creates a TagCloud in XML

-->
<xsl:output method="html"
     	indent="yes"
     	omit-xml-declaration="yes"
	/>
<xsl:param name="minSize">100</xsl:param>
<xsl:param name="maxSize">500</xsl:param>
<xsl:param name="delimiter"> </xsl:param>
<xsl:param name="lang">en</xsl:param>

<xsl:template match="/">
 <xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<xsl:template match="rdf:RDF">
<div style=" font-family: verdana, sans-serif; text-align: justify;font-weight: bold;word-spacing: 5px; vertical-align: middle; padding:15px; text-align:justify; margin:0 10px; background:#fff;"> 
<xsl:variable name="min">
  <xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:weight" data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="bio:weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="max">
  <xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:weight" data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="bio:weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:label" order="ascending" />
    <xsl:choose>
	<xsl:when test="@rdf:about">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="@rdf:about"/></xsl:attribute>
			<xsl:attribute name="style">text-decoration:none;</xsl:attribute>
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
    <xsl:if test="position()!=last()"><xsl:value-of select="$delimiter"/></xsl:if>
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
		<xsl:value-of select="$minSize + ((bio:weight - $min) div ($max - $min))*($maxSize - $minSize)"/>
		<xsl:text>%;</xsl:text>
		<xsl:if test="bio:color">
			<xsl:text>color:</xsl:text>
			<xsl:value-of select="bio:color"/>
			<xsl:text>;</xsl:text>
		</xsl:if>
	</xsl:attribute>
	<xsl:if test="bio:title">
		<xsl:attribute name="title">
			<xsl:choose>
				<xsl:when test="bio:title[@xml:lang= $lang]"><xsl:value-of select="bio:title[@xml:lang=$lang]"/></xsl:when>
				<xsl:when test="bio:title[@xml:lang='en']"><xsl:value-of select="bio:title[@xml:lang='en']"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="bio:title[1]"/></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:if>
	
		<xsl:choose>
			<xsl:when test="bio:label[@xml:lang= $lang]"><xsl:value-of select="bio:label[@xml:lang=$lang]"/></xsl:when>
			<xsl:when test="bio:label[@xml:lang='en']"><xsl:value-of select="bio:label[@xml:lang='en']"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="bio:label[1]"/></xsl:otherwise>
		</xsl:choose>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>
