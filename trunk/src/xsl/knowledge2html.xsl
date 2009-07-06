<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE rdf:RDF [
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY my "urn:ontology:lindenb.org/">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#" >
	<!ENTITY bibo "http://purl.org/ontology/bibo/" >
	<!ENTITY skos "http://www.w3.org/2008/05/skos#" >
	<!ENTITY dc "http://purl.org/dc/elements/1.1/" >
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >
	<!ENTITY event "http://purl.org/NET/c4dm/event.owl#" >
]>
<xsl:stylesheet
	xmlns:rdf="&rdf;"
	xmlns:my="&my;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	xmlns:owl="&owl;"
	xmlns:rdfs="&rdfs;"
	xmlns:bibo="&bibo;"
	xmlns:xsd="&xsd;"
	xmlns:event="&event;"
	>
<xsl:output method='xml'/>

<xsl:template match="/">
<xsl:apply-templates/> 
</xsl:template>

<xsl:template match="rdf:RDF">
<div>
<div class="rdf-content">
<rdf:RDF><xsl:apply-templates mode="copy-rdf"/></rdf:RDF>
</div>
<div><xsl:apply-templates/></div>
</div>
</xsl:template>

<xsl:template match="bibo:Quote" >
</xsl:template>

<xsl:template match="owl:DatatypeProperty" >
<h1><xsl:value-of select="local-name(.):</h1>
<table class="table1"><caption><xsl:value-of select="@rdf:about"/></caption>
<col width="30%"/>
<col width="70%"/> 
<thead><tr><th>Property</th><th>Value</th></tr></thead>
<tbody>
<xsl:for-each select="rdfs:label|rdfs:comment">
<xsl:if test="not(@xml:lang)"><xsl:message terminate="yes" >missing @xml:lang</xsl:message></xsl:if>
<tr>
  <td><xsl:value-of select="local-name(.)"/></td>
  <td><xsl:value-of select="."/> (<i><xsl:value-of select="@xml:lang"/></i>)</td>
</tr>
</xsl:for-each>
<xsl:for-each select="rdfs:range|rdfs:domain">
<tr>
  <td><xsl:value-of select="local-name(.)"/></td>
  <td><u><xsl:value-of select="@rdf:resource"/></u></td>
</tr>
</xsl:for-each>
</tbody>
</table>
</xsl:template>

<xsl:template match="*" mode="copy-rdf">
<xsl:copy-of select=".">
<xsl:copy-of select="@*"/>
<xsl:apply-templates mode="copy-rdf"/> 
</xsl:copy-of>
</xsl:template>


</xsl:stylesheet>
