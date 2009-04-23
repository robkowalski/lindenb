<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:bio="http://ontology.lindenb.org/tagcloud/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	version='1.0'
	>
<!--

This stylesheet creates a TagCloud in XML

-->
<xsl:output method="html"
     	indent="yes"
     	omit-xml-declaration="yes"
	/>
<!-- min font percent size -->
<xsl:param name="minSize">100</xsl:param>
<!-- max font percent size -->
<xsl:param name="maxSize">500</xsl:param>
<!-- delimiter between the tags -->
<xsl:param name="delimiter"> </xsl:param>
<!-- language (xml:lang) used -->
<xsl:param name="lang">en</xsl:param>
<!-- default value for RDFa 'about' -->
<xsl:param name="me">mailto:nobody@nowhere.org</xsl:param>
<!-- default value for RDFa 'rel' -->
<xsl:param name="predicate">bio:skill</xsl:param>

<xsl:template match="/">
<html><body>
 <xsl:apply-templates select="rdf:RDF"/>
</body></html>
</xsl:template>

<xsl:template match="rdf:RDF">

<xsl:element name="div">
<xsl:attribute name="typeof">foaf:Person</xsl:attribute>
<xsl:attribute name="about"><xsl:value-of select="$me" /></xsl:attribute>
<xsl:attribute name="xml:lang"><xsl:value-of select="$lang" /></xsl:attribute>
<xsl:attribute name="style">font-family: verdana, sans-serif; text-align: justify;font-weight: bold;word-spacing: 5px; vertical-align: middle; padding:15px; text-align:justify; margin:0 10px; background:#fff;</xsl:attribute>

<xsl:comment>Tag Cloud generated with tagcloud2html.xsl Pierre Lindenbaum PhD. http://plindenbaum.blogspot.com</xsl:comment>

<!-- get the min value of the tag -->
<xsl:variable name="min">
  <xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:weight" data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="bio:weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<!-- get the max value of the tag -->
<xsl:variable name="max">
  <xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:weight" data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="bio:weight" />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<!-- loop over each tag -->
<xsl:for-each select="bio:Tag">
    <xsl:sort select="bio:label" order="ascending" />
    <xsl:choose>
	<xsl:when test="@rdf:about">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="@rdf:about"/></xsl:attribute>
			<xsl:attribute name="style">text-decoration:none;</xsl:attribute>
			<xsl:attribute name="rel"><xsl:value-of select="$predicate"/></xsl:attribute>
			<xsl:call-template name="makeTag">
				<xsl:with-param name="min"><xsl:value-of select="$min"/></xsl:with-param>
				<xsl:with-param name="max"><xsl:value-of select="$max"/></xsl:with-param>
				<xsl:with-param name="rel">false</xsl:with-param>
				<xsl:with-param name="tag" select="."/>
			</xsl:call-template>
		</xsl:element>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="makeTag">
			<xsl:with-param name="min"><xsl:value-of select="$min"/></xsl:with-param>
			<xsl:with-param name="max"><xsl:value-of select="$max"/></xsl:with-param>
			<xsl:with-param name="rel">true</xsl:with-param>
			<xsl:with-param name="tag" select="."/>
		</xsl:call-template>
	</xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position()!=last()"><xsl:value-of select="$delimiter"/></xsl:if>
</xsl:for-each>

</xsl:element>
</xsl:template>

<xsl:template name="makeTag">
<xsl:param name="tag"/>
<xsl:param name="min"/>
<xsl:param name="max"/>
<xsl:param name="rel"/>
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
	<xsl:if test="$rel='true'">
		<xsl:attribute name="property"><xsl:value-of select="$predicate"/></xsl:attribute>
	</xsl:if>
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
