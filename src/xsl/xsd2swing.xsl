<?xml version='1.0'  encoding="ISO-8859-1" ?>
<!DOCTYPE xsl:stylesheet [
	  <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 	  <!ENTITY owl "http://www.w3.org/2002/07/owl#">
  	  <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	  <!ENTITY xsd "http://www.w3.org/2001/XMLSchema" >
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
/************
 *
 *
 */
<xsl:if test="string-length($package)&gt;0">package <xsl:value-of select="$package"/>;</xsl:if>
<xsl:apply-templates select="xsd:schema"/>
</xsl:template>

<xsl:template match="xsd:schema">
 <xsl:apply-templates select="xsd:simpleType"/>
</xsl:template>


<xsl:template match="xsd:simpleType[@name][xsd:restriction/@base=&apos;xsd:string&apos;]">
<xsl:variable name="className"><xsl:value-of select="@name"/></xsl:variable>

</xsl:template>

<xsl:template match="xsd:simpleType[@name][xsd:restriction/@base=&apos;xsd:integer&apos;]">
<xsl:variable name="className"><xsl:value-of select="@name"/></xsl:variable>
<xsl:variable name="valueType">java.lang.Integer</xsl:variable>
class <xsl:value-of select="$className"/>
	{
	private <xsl:value-of select="$valueType"/> value;
	<xsl:value-of select="$className"/>(<xsl:value-of select="$valueType"/> value)
		{
		this.value=value;
		}
	
	}
</xsl:template>

</xsl:stylesheet>
