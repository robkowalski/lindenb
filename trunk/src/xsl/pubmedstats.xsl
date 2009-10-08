<?xml version='1.0'  encoding="ISO-8859-1"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<!--

This stylesheet make stats for Pubmed. Asked by @tpoi

	http://twitter.com/tpoi/status/4441486284
	Quelqu'un aurait un outil pour récupérer uniquement les années de publi sur une requête pubmed? Ou je dois faire du PERL? 

Author: Pierre Lindenbaum PhD plindenbaum@yahoo.fr

-->
<xsl:output method='text' encoding="UTF-8"/>

<!-- minimum start Year -->
<xsl:variable name="startYear">
 <xsl:for-each select="/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Journal/JournalIssue/PubDate/Year">
    <xsl:sort select="." data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<!-- maximum start Year -->
<xsl:variable name="endYear">
 <xsl:for-each select="/PubmedArticleSet/PubmedArticle/MedlineCitation/DateCreated/Year">
    <xsl:sort select="." data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<!--main -->
<xsl:template match="/">
<xsl:text>#Year	Count
</xsl:text>
<xsl:call-template name="stat4year">
<xsl:with-param name="year" select="$startYear"/>
</xsl:call-template>
</xsl:template>

<!-- for a given $year, prints the number of articles -->
<xsl:template name="stat4year">
<xsl:param name="year"/>
<xsl:if test="number($year)&lt;=$endYear">
 <xsl:variable name="total" select="count(/PubmedArticleSet/PubmedArticle/MedlineCitation[DateCreated/Year = $year] )"/>
<xsl:value-of select="$year"/><xsl:text>	</xsl:text><xsl:value-of select="$total"/><xsl:text>
</xsl:text>
<xsl:call-template name="stat4year">
<xsl:with-param name="year" select="number($year)+1"/>
</xsl:call-template>
 </xsl:if>
</xsl:template>


</xsl:stylesheet>
