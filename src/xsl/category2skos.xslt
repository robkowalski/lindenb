<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:html='http://www.w3.org/1999/xhtml'
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	version='1.0'
	>
<!--

psi2sql.xslt
Pierre Lindenbaum PhD
plindenbaum@yahoo.fr

transform a psi/xml description of protein/protein interactions to
grapviz dot

I tested this input with http://string.embl.de/api/psi-mi/interactions?identifier=ROXAN

Usage:

	xsltproc psi2sql.xslt interactions.xml | dot 


Pierre

-->



<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
<xsl:param name="mwhost">http://en.wikipedia.org/wiki/</xsl:param>

<xsl:template match="/">
<rdf:RDF>
<xsl:apply-templates select="html:html"/>
</rdf:RDF>
</xsl:template>

<xsl:template match="html:html">
<xsl:apply-templates select="html:body"/>
</xsl:template>

<xsl:template match="html:body">
<xsl:element name="skos:Concept">
<xsl:attribute name="rdf:about"><xsl:value-of select="$mwhost"/><xsl:value-of select=".//html:h1[@id=&apos;firstHeading&apos;]"/></xsl:attribute>
<xsl:for-each select=".//html:div[@id=&apos;p-lang&apos;]//html:li[@class][html:a/@href]">
<xsl:element name="skos:prefLabel">
  <xsl:attribute name="xml:lang"><xsl:value-of select="substring-after(@class,&apos;-&apos;)"/></xsl:attribute>
  <xsl:value-of select="substring-after(substring-after(html:a/@href,&apos;/wiki/&apos;),&apos;:&apos;)"/>
</xsl:element>
</xsl:for-each>


<xsl:for-each select=".//html:div[@id=&apos;mw-subcategories&apos;]//html:a[@href]">
<xsl:if test="starts-with(@href,&apos;/wiki/&apos;)">
<xsl:element name="skos:narrower">
  <xsl:attribute name="rdf:resource"><xsl:value-of select="$mwhost"/><xsl:value-of select="substring-after(@href,&apos;/wiki/&apos;)"/></xsl:attribute>

</xsl:element>
</xsl:if>
</xsl:for-each>

<xsl:for-each select=".//html:div[@id=&apos;catlinks&apos;]//html:span/html:a[@href]">
<xsl:if test="starts-with(@href,&apos;/wiki/&apos;)">
<xsl:element name="skos:broader">
  <xsl:attribute name="rdf:resource"><xsl:value-of select="$mwhost"/><xsl:value-of select="substring-after(@href,&apos;/wiki/&apos;)"/></xsl:attribute>

</xsl:element>
</xsl:if>
</xsl:for-each>

</xsl:element>
</xsl:template>





</xsl:stylesheet>
