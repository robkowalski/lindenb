<?xml version='1.0'  encoding="ISO-8859-1" ?>
<!DOCTYPE xsl:stylesheet [
          <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
          <!ENTITY xsd "http://www.w3.org/2001/XMLSchema">
          ]>
<xsl:stylesheet
	xmlns:s="http://www.ncbi.nlm.nih.gov/SNP/docsum"
	xmlns:rdf='&rdf;'
	xmlns:xsd='&xsd;'
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:o="urn:void:dbsnp"
	version='1.0'
	>
<!--
Author:
	Pierre Lindenbaum PhD
	http://plindenbaum.blogspot.com
	plindenbaum@yahoo.fr
Motivation:
	transforms dbsnp to rdf

Parameters:
	$taxonid (default: 9606) the taxon id
	
Example:
	
 	xsltproc -\-novalid dbsnp2rdf.xsl \
 		 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=snp&id=25,26&retmode=xml'

or
	#download xsltstream.ja from http://lindenb.googlecode.com/files/xsltstream.jar
	
	wget 'http://lindenb.googlecode.com/files/xsltstream.jar'
	java -jar  xsltstream.jar -x  dbsnp2rdf.xsl  -d 1 \
	 'ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/XML/ds_chMT.xml.gz

-->


<xsl:output method="xml" indent="yes" encoding="UTF-8" />


<xsl:param name="taxonid" select="9606"/>

<xsl:template match="/">
<rdf:RDF>
<xsl:comment>
Generated with dbsnp2rdf.xsl
Author: Pierre Lindenbaum PhD.
plindenbaum@yahoo.fr
http://plindenbaum.blogspot.com
</xsl:comment>
<xsl:apply-templates/>
</rdf:RDF>
</xsl:template>

<xsl:template match="s:ExchangeSet">
  <xsl:apply-templates select="s:Rs" />
</xsl:template>

<xsl:template match="s:Rs">
<xsl:element name="o:SNP">
<xsl:attribute name="rdf:about">
 <xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/snp/',@rsId)"/>
</xsl:attribute>
<dc:title><xsl:value-of select="concat('rs',@rsId)"/></dc:title>
<o:taxon rdf:resource="{concat('http://www.ncbi.nlm.nih.gov/taxonomy/',$taxonid)}"/>
<xsl:apply-templates select="s:Het"/>
<xsl:apply-templates select="s:Ss"/>
<xsl:apply-templates select="s:Assembly"/>
</xsl:element>
</xsl:template>

<xsl:template match="s:Ss">
  <xsl:if test="string-length(@handle)&gt;0">
    <o:hasHandle rdf:resource="{concat('urn:void:ncbi:snp:handle:' ,@handle)}"/>
   </xsl:if>
</xsl:template>



<xsl:template match="s:Assembly">
 <xsl:variable name="genomeBuild" select="@genomeBuild"/>
 <xsl:variable name="groupLabel" select="@groupLabel"/>
 <xsl:for-each select="s:Component[@chromosome]">
  <xsl:variable name="chromosome" select="@chromosome"/>
  <xsl:for-each select="s:MapLoc[@physMapInt and @leftContigNeighborPos and @rightContigNeighborPos and  @orient]">
    <xsl:variable name="physMapInt" select="@physMapInt"/>
    <xsl:variable name="len" select="number(@rightContigNeighborPos)  - number(@leftContigNeighborPos) -1"/>
    
    <xsl:variable name="chromStart">
    	<xsl:choose>
    		<xsl:when test="$len=0">
    			<xsl:value-of select="number(@physMapInt)+1"/>
    		</xsl:when>
    		<xsl:otherwise>
    			<xsl:value-of select="number(@physMapInt)"/>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="chromEnd" select="$chromStart + $len"/>
    
    
    <o:hasMapping>
      <o:Mapping>
        <o:build rdf:resource="{concat('urn:void:ncbi:build:',$groupLabel,'/',$genomeBuild)}"/>
        <o:chrom rdf:resource="{concat('urn:void:ncbi:chromosome:',$taxonid,'/chr',$chromosome)}"/>
        <o:start rdf:datatype="&xsd;#int"><xsl:value-of select="$chromStart"/></o:start>
        <o:end rdf:datatype="&xsd;#int"><xsl:value-of select="$chromEnd"/></o:end>
        <o:orient>
        	<xsl:choose>
        		<xsl:when test="@orient='forward'">
        			<xsl:text>+</xsl:text>
        		</xsl:when>
        		<xsl:when test="@orient='reverse'">
        			<xsl:text>-</xsl:text>
        		</xsl:when>
        		<xsl:otherwise>
        			<xsl:value-of select="@orient"/>
        		</xsl:otherwise>
        	</xsl:choose>
        </o:orient>
      </o:Mapping>
    </o:hasMapping>
  </xsl:for-each>
 </xsl:for-each>
</xsl:template>




<!--
"snpClass":"<xsl:value-of select="@snpClass"/>",
"mapping":[<xsl:for-each select="s:Assembly">
<xsl:if test="position()!=1">,</xsl:if>
	{
	"dbSnpBuild":"<xsl:value-of select="@dbSnpBuild"/>",
	"genomeBuild":"<xsl:value-of select="@genomeBuild"/>",
	"groupLabel":"<xsl:value-of select="@groupLabel"/>",
	"map":[
		<xsl:for-each select="s:Component/s:MapLoc">
			<xsl:if test="position()!=1">,</xsl:if>
			{
			"chromosome":"<xsl:value-of select="../@chromosome"/>",
			"position":<xsl:value-of select="@physMapInt"/>
			}
		</xsl:for-each>
		]
	}
</xsl:for-each>]
-->


<xsl:template match="s:Het">
 <xsl:if test="number(@value)&gt;0">
 	<o:het rdf:datatype="&xsd;#float">
 	  <xsl:value-of select="@value"/>
 	</o:het>
 </xsl:if>
</xsl:template>

<xsl:template match="s:Sequence">
	<o:seq5><xsl:value-of select="s:Seq5"/></o:seq5>
	<o:observed><xsl:value-of select="s:Observed"/></o:observed>
	<o:seq3><xsl:value-of select="s:Seq3"/></o:seq3>
</xsl:template>




</xsl:stylesheet>
