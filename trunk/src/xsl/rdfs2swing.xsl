<?xml version='1.0'  encoding="ISO-8859-1" ?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 	  <!ENTITY owl "http://www.w3.org/2002/07/owl#">
  	  <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	  <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >
	  ]>
<xsl:stylesheet
	xmlns:rdf="&rdf;"
 	xmlns:rdfs="&rdfs;"
	xmlns:owl="&owl;"
	xmlns:xsd="&xsd;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<xsl:key name="classes" match="/rdf:RDF/owl:Class" use="@rdf:about"/>
<xsl:key name="dataTypeProperties" match="/rdf:RDF/owl:DataTypeProperty " use="@rdf:about"/>
<xsl:key name="objectTypeProperties" match="/rdf:RDF/owl:ObjectType " use="@rdf:about"/>

<xsl:param name="package">org.lindenb.test</xsl:param>
<xsl:variable name="package-path" select="concat(translate($package,&apos;.&apos;,&apos;/&apos;),&apos;/&apos;)"/>
<xsl:output method="xml"/>
<xsl:preserve-space elements="file" />

<xsl:template match="/">
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>


<xsl:template match="rdf:RDF">
  <xsl:element name="archive">
 
  <xsl:apply-templates select="owl:Class[@rdf:about]" mode="interface"/>
  </xsl:element>
</xsl:template>


<xsl:template match="owl:Class"  mode="interface">
<xsl:variable name="className" select="rdfs:label"/>
<xsl:element name="file">

<xsl:attribute name="path"><xsl:value-of select="concat($package-path,rdfs:label)"/></xsl:attribute>
<xsl:if test="string-length($package)&gt;0">package <xsl:value-of select="$package"/>;</xsl:if>
/*****************************************
 * <xsl:value-of select="$className"/>
 * <xsl:value-of select="rdfs:comment"/>
 */
public interface <xsl:value-of select="$className"/>
<xsl:if test="count(rdfs:subClassOf[@rdf:resource])&gt;0">
extends <xsl:for-each select="rdfs:subClassOf[@rdf:resource]">
<xsl:if test="position()!=last()">,</xsl:if> X
</xsl:for-each>
</xsl:if>
	{
	<!-- Loop over ow:Restriction -->
	<xsl:for-each select="rdfs:subClassOf/owl:Restriction[owl:onProperty/@rdf:resource]">
	<xsl:variable name="datatype" select="key(&apos;dataTypeProperties&apos;,owl:onProperty/@rdf:resource)"/>
	<xsl:variable name="objecttype" select="key(&apos;objectTypeProperties&apos;,owl:onProperty/@rdf:resource)"/>
	<xsl:choose>
	  <xsl:when test="$objecttype">OBJECT
          </xsl:when>
	  <xsl:when test="$datatype">


	<xsl:variable name="rangetype"><xsl:call-template name="xsd2java">
		<xsl:with-param name="uri" select="$datatype/rdfs:range/@rdf:resource"/>
	</xsl:call-template></xsl:variable>


	<xsl:variable name="returntype"><xsl:choose>
	  <xsl:when test="owl:cardinality = &apos;0&apos; or owl:cardinality = &apos;1&apos;"><xsl:value-of select="$rangetype"/></xsl:when>
	  <xsl:otherwise>java.util.List&lt;<xsl:value-of select="$rangetype"/>&gt;</xsl:otherwise>
	</xsl:choose></xsl:variable>
	/** getter for <xsl:value-of select="$datatype/rdfs:label"/> */
	public <xsl:value-of select="$returntype"/> get<xsl:value-of select="$datatype/rdfs:label"/>();
	/** setter for <xsl:value-of select="$datatype/rdfs:label"/> */
	public void set<xsl:value-of select="$datatype/rdfs:label"/>( <xsl:value-of select="$returntype"/> value);

          </xsl:when>
	</xsl:choose>
	
	
	

	</xsl:for-each>
	}
</xsl:element>
</xsl:template>


<xsl:template name="xsd2java">
<xsl:param name="uri" select="java.lang.String"/>
<xsl:choose>
 <xsl:when test="$uri = &apos;&xsd;string&apos; or $uri = &apos;&xsd;java:java.lang.String&apos;">java.lang.String</xsl:when>
 <xsl:otherwise><xsl:value-of select="$uri"/></xsl:otherwise>
</xsl:choose>
</xsl:template>


</xsl:stylesheet>
