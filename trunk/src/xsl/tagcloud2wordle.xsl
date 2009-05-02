<?xml version='1.0' encoding="ISO-8859-1" ?>
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
<xsl:output method="text"/>

<!-- language (xml:lang) used -->
<xsl:param name="lang">en</xsl:param>

<xsl:template match="/">
 <xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<xsl:template match="rdf:RDF">

<!-- loop over each tag -->
<xsl:for-each select="bio:Tag">
<xsl:sort select="bio:label" data-type="text" order="ascending" />
	<xsl:call-template name="makeTag">
		<xsl:with-param name="label">
			<xsl:choose>
				<xsl:when test="bio:label[@xml:lang= $lang]"><xsl:value-of select="bio:label[@xml:lang=$lang]"/></xsl:when>
				<xsl:when test="bio:label[@xml:lang='en']"><xsl:value-of select="bio:label[@xml:lang='en']"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="bio:label[1]"/></xsl:otherwise>
			</xsl:choose>
		</xsl:with-param>
		<xsl:with-param name="count"><xsl:value-of select="bio:weight"/></xsl:with-param>
	</xsl:call-template>
	<xsl:text>
	</xsl:text>
</xsl:for-each>

</xsl:template>

<xsl:template name="makeTag">
<xsl:param name="label"/>
<xsl:param name="count"/>
	<xsl:value-of select="$label"/><xsl:text> </xsl:text>
	<xsl:if test="$count &gt; 0">
		<xsl:call-template name="makeTag">
		<xsl:with-param name="label"><xsl:value-of select="$label"/></xsl:with-param>
		<xsl:with-param name="count"><xsl:value-of select="$count - 1"/></xsl:with-param>
	</xsl:call-template>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
