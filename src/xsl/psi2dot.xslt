<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
 	xmlns:psi="net:sf:psidev:mi"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
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

This is a really basic implementation: e.g: all experiment must 
have one pmid, there must be one and only one 'names' for each entity,
I did't much escaped sql strings, etc...

Pierre

-->


<xsl:key name="expid" match="psi:experimentDescription" use="@id" />



<xsl:output method="text" encoding="UTF-8"/>

<xsl:template match="/">
graph G {

<xsl:apply-templates/>

}
</xsl:template>


<xsl:template match="psi:entrySet">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="psi:entry">
<xsl:apply-templates select="psi:interactorList"/>
<xsl:apply-templates select="psi:interactionList"/>
</xsl:template>




<xsl:template match="psi:interactorList">
<xsl:apply-templates select="psi:interactor"/>
</xsl:template>


<xsl:template match="psi:interactionList">
<xsl:apply-templates select="psi:interaction"/>
</xsl:template>




<xsl:template match="psi:interactor">
i<xsl:value-of select="@id"/>[label="<xsl:value-of select="translate(psi:names[1]/psi:shortLabel,&apos;&quot;&apos;,&quot;&apos;&quot;)"/>",URL="http://www.ensembl.org/Homo_sapiens/geneview?gene=<xsl:value-of select="psi:xref/psi:primaryRef/@id"/>"];
</xsl:template>


<xsl:template match="psi:interaction">
<xsl:if test="count(psi:participantList/psi:participant)!=2">
This stylesheet expect only binary interaction but found <xsl:value-of select="count(psi:participant)"/>
in interaction od=<xsl:value-of select="@id"/>;
</xsl:if>

i<xsl:value-of select="psi:participantList/psi:participant[1]/psi:interactorRef"/> -- i<xsl:value-of select="psi:participantList/psi:participant[2]/psi:interactorRef"/>[label="pmid:<xsl:value-of select="key('expid', psi:experimentList/psi:experimentRef)/psi:bibref/psi:xref[1]/psi:primaryRef[@db='pubmed']/@id"/>",URL="http://www.ncbi.nlm.nih.gov/pubmed/<xsl:value-of select="key('expid', psi:experimentList/psi:experimentRef)/psi:bibref/psi:xref[1]/psi:primaryRef[@db='pubmed']/@id"/>"];

</xsl:template>


</xsl:stylesheet>
