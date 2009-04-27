<?xml version='1.0'  encoding="ISO-8859-1" ?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	  <!ENTITY empty "">
	  ]>
<xsl:stylesheet
	 xmlns:rdf="&rdf;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<xsl:import href="rdf2.part.xsl"/>

<xsl:output method='xml' indent="yes"/>

<xsl:template match="/">
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<!-- emit a RDF statement -->

<xsl:template name="emit">
<xsl:param name="subject"/>
<xsl:param name="predicate"/>
<xsl:param name="value-is-uri"/>
<xsl:param name="value"/>
<rdf:Statement>
	<xsl:element name="rdf:subject">
		<xsl:attribute name="rdf:resource"><xsl:value-of select="$subject"/></xsl:attribute>
	</xsl:element>
	<xsl:element name="rdf:predicate">
		<xsl:attribute name="rdf:resource"><xsl:value-of select="$predicate"/></xsl:attribute>
	</xsl:element>

	<xsl:choose>
		<xsl:when test="$value-is-uri='true'">
			<xsl:element name="rdf:object"><xsl:attribute name="rdf:resource"><xsl:value-of select="$value"/></xsl:attribute></xsl:element>
		</xsl:when>
		<xsl:otherwise><xsl:element name="rdf:object"><xsl:choose><xsl:when test="string-length($value)=0"><xsl:text>&empty;</xsl:text></xsl:when><xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise></xsl:choose></xsl:element></xsl:otherwise>
	</xsl:choose>


</rdf:Statement>

</xsl:template>



</xsl:stylesheet>
