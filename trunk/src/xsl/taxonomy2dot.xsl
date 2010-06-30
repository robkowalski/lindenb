<?xml version='1.0'  encoding="ISO-8859-1" ?>
<xsl:stylesheet
        xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
        xmlns:gp='gp'
        version='1.0'
        >
<xsl:output method='text' encoding="ISO-8859-1"/>

<xsl:key name="taxids" match="//Taxon" use="TaxId"/>
<!--
Author:
        Pierre Lindenbaum PHD
WWW:
        http://plindenbaum.blogspot.com
mail:
        plindenbaum@yahoo.fr
Motivation:
        This stylesheet transforms the NCBI taxonomy as XML to a graphiz dot file
Reference:
	http://plindenbaum.blogspot.com/2010/06/xsltncbi-taxonomygraphviz-dot.html
Example:
         xsltproc  \-\-novalid taxonomy2dot.xsl \
           "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?id=7070,32351,9605,9606&db=taxonomy&retmode=xml" |\
           dot  -o/home/pierre/jeter.svg -Tsvg
-->


<xsl:template match="/">
<xsl:text>digraph G {
</xsl:text>
<xsl:apply-templates select="//Taxon" mode="node"/>
<xsl:for-each select="TaxaSet/Taxon">
<xsl:variable name="curr" select="."/>
<xsl:for-each select="$curr/LineageEx/Taxon[position()&gt;1]">
<xsl:variable name="id1" select="TaxId"/>
<xsl:variable name="pos1" select="position()"/>
<xsl:variable name="num" select="count($curr/preceding-sibling::Taxon/LineageEx/Taxon[TaxId=$id1])"/>
<xsl:if test="$num=0">
<xsl:value-of select="concat('tax',$id1)"/>
<xsl:text> -&gt; </xsl:text>
<!-- in the next line there was a bug when I used [$pos1 -1 ] because we select only those
node having position()&gt; 1 -->
<xsl:value-of select="concat('tax',$curr/LineageEx/Taxon[$pos1]/TaxId)"/>
<xsl:text>;
</xsl:text>
</xsl:if>
</xsl:for-each>

<xsl:if test="count(/TaxaSet/Taxon/LineageEx/Taxon[TaxId=$curr/TaxId]) = 0">
<xsl:value-of select="concat('tax',$curr/TaxId)"/>
<xsl:text> -&gt; </xsl:text>
<xsl:value-of select="concat('tax',$curr/LineageEx/Taxon[position()=last()]/TaxId)"/>
<xsl:text> ;
</xsl:text>
</xsl:if>

</xsl:for-each>

<xsl:text>}
</xsl:text>
</xsl:template>

<xsl:template match="Taxon" mode="node">
<xsl:if test="generate-id(.)=generate-id(key('taxids',TaxId))">
<xsl:value-of select="concat('tax',./TaxId)"/>[label=&quot;<xsl:value-of select="./ScientificName"/>&quot;,URL=&quot;http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=<xsl:value-of select="TaxId"/>&quot;];
</xsl:if>
</xsl:template>

</xsl:stylesheet>
