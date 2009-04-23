<?xml version='1.0' ?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	  <!ENTITY empty "">
	  ]>
<xsl:stylesheet
	 xmlns:rdf="&rdf;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>

<xsl:output method='xml' indent="yes"/>


<xsl:template match="/">
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>


<xsl:template match="rdf:RDF">
<xsl:element name="rdf:RDF">
<xsl:for-each select="*">

<xsl:call-template name="parseStatement">
<xsl:with-param name="node" select="."/>
</xsl:call-template>

</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name="parseStatement">
<xsl:param name="node"/>
<xsl:variable name="subject">
	<xsl:call-template name="subjectURI">
		<xsl:with-param name="node" select="$node"/>
	</xsl:call-template>
</xsl:variable>

<xsl:choose>
	<xsl:when test="namespace-uri($node)='&rdf;' and local-name($node)='Description'">
		
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="emit">
			<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
			<xsl:with-param name="predicate">&rdf;type</xsl:with-param>
			<xsl:with-param name="value-is-uri">true</xsl:with-param>
			<xsl:with-param name="value"><xsl:value-of select="namespace-uri($node)"/><xsl:value-of select="local-name($node)"/></xsl:with-param>
		</xsl:call-template>
	</xsl:otherwise>
</xsl:choose>

<xsl:for-each select="$node/@*">
		<xsl:if test="not(rdf:about or rdf:ID or rdf:nodeID)">
		<xsl:call-template name="emit">
			<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
			<xsl:with-param name="predicate"><xsl:value-of select="namespace-uri(.)"/><xsl:value-of select="local-name(.)"/></xsl:with-param>
			<xsl:with-param name="value-is-uri">false</xsl:with-param>
			<xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
		</xsl:call-template>
		</xsl:if>
</xsl:for-each>

<xsl:for-each select="$node/*">
	<xsl:call-template name="parsePredicate">
		<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
		<xsl:with-param name="node" select="$node"/>
	</xsl:call-template>
</xsl:for-each>


</xsl:template>

<xsl:template name="parsePredicate">
 <xsl:param name="subject"/>
 <xsl:param name="node"/>



<xsl:for-each select="$node/*">

<xsl:choose>
	<xsl:when test="@rdf:resource">
		<xsl:call-template name="emit">
			<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
			<xsl:with-param name="predicate"><xsl:value-of select="namespace-uri(.)"/><xsl:value-of select="local-name(.)"/></xsl:with-param>
			<xsl:with-param name="value-is-uri">true</xsl:with-param>
			<xsl:with-param name="value"><xsl:value-of select="@rdf:resource"/></xsl:with-param>
		</xsl:call-template>
	</xsl:when>
	<xsl:when test="count(*)=0">
		<xsl:call-template name="emit">
			<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
			<xsl:with-param name="predicate"><xsl:value-of select="namespace-uri(.)"/><xsl:value-of select="local-name(.)"/></xsl:with-param>
			<xsl:with-param name="value-is-uri">false</xsl:with-param>
			<xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:for-each select="*">
			<xsl:variable name="value-uri">
				<xsl:call-template name="subjectURI">
					<xsl:with-param name="node" select="."/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:call-template name="emit">
				<xsl:with-param name="subject"><xsl:value-of select="$subject"/></xsl:with-param>
				<xsl:with-param name="predicate"><xsl:value-of select="namespace-uri(.)"/><xsl:value-of select="local-name(.)"/></xsl:with-param>
				<xsl:with-param name="value-is-uri">true</xsl:with-param>
				<xsl:with-param name="value"><xsl:value-of select="$value-uri"/></xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="parseStatement">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:otherwise>
</xsl:choose>

</xsl:for-each>
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
			<xsl:element name="rdf:value"><xsl:attribute name="rdf:resource"><xsl:value-of select="$value"/></xsl:attribute></xsl:element>
		</xsl:when>
		<xsl:otherwise><xsl:element name="rdf:value"><xsl:choose><xsl:when test="string-length($value)=0"><xsl:text>&empty;</xsl:text></xsl:when><xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise></xsl:choose></xsl:element></xsl:otherwise>
	</xsl:choose>


</rdf:Statement>

</xsl:template>


<!-- get the ID of a subject node -->
<xsl:template name="subjectURI">
 <xsl:param name="node"/>
	<xsl:choose>
		<xsl:when test="$node/@rdf:about"><xsl:value-of select="$node/@rdf:about"/></xsl:when>
		<xsl:when test="$node/@rdf:ID"><xsl:value-of select="$node/@rdf:ID"/></xsl:when>
		<xsl:when test="$node/@rdf:nodeID"><xsl:value-of select="$node/@rdf:nodeID"/></xsl:when>
		<xsl:otherwise>_:<xsl:value-of select="generate-id($node)"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>


</xsl:stylesheet>
