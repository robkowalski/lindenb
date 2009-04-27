<?xml version='1.0'  encoding="ISO-8859-1" ?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	  ]>
<xsl:stylesheet
	 xmlns:rdf="&rdf;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<!--
Author Pierre Lindenbaum PhD
http://plindenbaum.blogspot.com
-->
<xsl:import href="rdf2.part.xsl"/>

<xsl:output method="text"/>


<xsl:template match="/">
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<!-- emit a RDF statement -->

<xsl:template name="emit">
<xsl:param name="subject"/>
<xsl:param name="predicate"/>
<xsl:param name="value-is-uri"/>
<xsl:param name="value"/>

	<xsl:element name="rdf:subject">
		<xsl:call-template name="printURI"><xsl:with-param name="uri"><xsl:value-of  select="$subject"/></xsl:with-param></xsl:call-template>
	</xsl:element>
	<xsl:text>&#09;</xsl:text>
	<xsl:element name="rdf:predicate">
		<xsl:call-template name="printURI"><xsl:with-param name="uri"><xsl:value-of  select="$predicate"/></xsl:with-param></xsl:call-template>
	</xsl:element>
	<xsl:text>&#09;</xsl:text>
	<xsl:choose>
		<xsl:when test="$value-is-uri='true'">
			<xsl:call-template name="printURI"><xsl:with-param name="uri"><xsl:value-of  select="$value"/></xsl:with-param></xsl:call-template>
		</xsl:when>
		<xsl:otherwise>"<xsl:call-template name="escape"><xsl:with-param name="s"><xsl:value-of  select="$value"/></xsl:with-param></xsl:call-template>"</xsl:otherwise>
	</xsl:choose>
<xsl:text> .
</xsl:text>

</xsl:template>

<xsl:template name="printURI">
<xsl:param name="uri"/>
<xsl:text>&lt;</xsl:text><xsl:value-of select="$uri"/><xsl:text>&gt;</xsl:text>
</xsl:template>

</xsl:stylesheet>
