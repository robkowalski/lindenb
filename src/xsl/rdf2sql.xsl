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
<!-- temporary table -->
<xsl:param name="temporary"></xsl:param>
<!-- temporary table -->
<xsl:param name="column4uri">varchar(50)</xsl:param>


<xsl:template match="/">
<xsl:text>
create </xsl:text><xsl:value-of select="$temporary"/><xsl:text> table TRIPLE IF NOT EXISTS
(
subject </xsl:text><xsl:value-of select="$column4uri"/><xsl:text> not null,
predicate </xsl:text><xsl:value-of select="$column4uri"/><xsl:text> not null,
value_is_uri enum('true','false') not null,
value </xsl:text><xsl:value-of select="$column4uri"/><xsl:text> not null
);
</xsl:text>
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>

<!-- emit a RDF statement -->

<xsl:template name="emit">
<xsl:param name="subject"/>
<xsl:param name="predicate"/>
<xsl:param name="value-is-uri"/>
<xsl:param name="value"/>
	<xsl:text>insert into TRIPLE(subject,predicate,value_is_uri,value) values ("</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="s"><xsl:value-of  select="$subject"/></xsl:with-param>
		</xsl:call-template>
		<xsl:text>","</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="s"><xsl:value-of  select="$predicate"/></xsl:with-param>
		</xsl:call-template>
		<xsl:text>",</xsl:text>
	<xsl:choose>
		<xsl:when test="$value-is-uri='true'">
			<xsl:text>"true","</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>"false","</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:call-template name="escape">
		<xsl:with-param name="s"><xsl:value-of  select="$value"/></xsl:with-param>
	</xsl:call-template>
	<xsl:text>");
</xsl:text>
</xsl:template>

<xsl:template name="quote">

</xsl:template>

</xsl:stylesheet>
