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
<xsl:key name="dataTypeProperties" match="/rdf:RDF/owl:DataTypeProperty" use="@rdf:about"/>
<xsl:key name="objectProperties" match="/rdf:RDF/owl:ObjectProperty" use="@rdf:about"/>
<xsl:key name="properties" match="/rdf:RDF/*[namespace-uri(.)=&apos;&owl;&apos; and ( local-name(.)=&apos;DataTypeProperty&apos; or local-name(.)=&apos;ObjectProperty&apos; )]" use="@rdf:about"/>

<xsl:param name="package"/>
<xsl:output method="text"/>


<xsl:template match="/">
<xsl:if test="string-length($package)&gt;0">package <xsl:value-of select="$package"/>;</xsl:if>
<xsl:apply-templates select="rdf:RDF"/>
</xsl:template>


<xsl:template match="rdf:RDF">
  <xsl:apply-templates select="owl:Class[@rdf:about]" mode="interface"/>
</xsl:template>


<xsl:template match="owl:Class"  mode="interface">
<xsl:variable name="label"><xsl:value-of select="rdfs:label[1]"/></xsl:variable>
<xsl:variable name="comment"><xsl:value-of select="rdfs:comment[1]"/></xsl:variable>
<xsl:variable name="className"><xsl:value-of select="$label"/></xsl:variable>
/**
 * <xsl:value-of select="$label"/>
 * <xsl:value-of select="$comment"/>
 */
interface <xsl:value-of select="className"/>
<xsl:if test="count(rdfs:subClassOf[@rdf:resource]) &gt; 0 ">
 extends <xsl:for-each select="rdfs:subClassOf/@rdf:resource">
	<xsl:variable name="sub-label"><xsl:value-of select="key('classes',.)/rdfs:label[1]"/></xsl:variable>
 	<xsl:variable name="sub-className"><xsl:value-of select="$sub-label"/></xsl:variable>
 </xsl:for-each >
	
</xsl:if>
	{
	<!-- loop over each property -->
	<xsl:for-each select="rdfs:subClassOf/owl:Restriction[owl:onProperty]">
                <xsl:variable name="property" select="key('properties',owl:onProperty/@rdf:resource)"/>
		<xsl:variable name="sub-label"><xsl:value-of select="$property/rdfs:label[1]"/></xsl:variable>
		<xsl:variable name="sub-comment"><xsl:value-of select="$property/rdfs:comment[1]"/></xsl:variable>
		<xsl:variable name="sub-range"><xsl:value-of select="$property/rdfs:range[1]/@rdf:resource"/></xsl:variable>
		/**
                 * <xsl:value-of select="$sub-comment"/>
                 */
		<xsl:choose>
                  <xsl:when test="local-name($property)=&apos;DataTypeProperty&apos;">
			<xsl:choose>
				<xsl:when test="$sub-range=&apos;&xsd;string&apos;">String</xsl:when>
				<xsl:otherwise>SubRange <xsl:value-of select="$sub-range"/></xsl:otherwise>
			</xsl:choose>
                  </xsl:when>
		  <xsl:when test="local-name($property)=&apos;ObjectProperty&apos;">
			<xsl:value-of select="$property/rdfs:range/@rdf:resource"/>
                  </xsl:when>
                </xsl:choose>
		!! <xsl:value-of select="$sub-label"/>()
	</xsl:for-each>
	}
</xsl:template>

</xsl:stylesheet>
