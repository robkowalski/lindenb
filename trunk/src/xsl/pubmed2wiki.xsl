<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<!--

This stylesheet transforms one or more Pubmed
Article in xml format into a set of citations for wikipedia

Author: Pierre Lindenbaum PhD plindenbaum@yahoo.fr

see: http://en.wikipedia.org/wiki/Template:Citation

-->
<xsl:output method='text'/>


<xsl:template match="/">
&lt;!-- 
Generated with pubmed2wiki.xsl
Author: Pierre Lindenbaum PhD.
plindenbaum@yahoo.fr 
http://en.wikipedia.org/wiki/User:Plindenbaum
--&gt;
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="PubmedArticleSet">
<xsl:apply-templates select="PubmedArticle"/>
</xsl:template>

<xsl:template match="PubmedArticle">&lt;ref&gt;{{Citation<xsl:apply-templates select=".//PMID"/>
<xsl:apply-templates select=".//AuthorList"/>
<xsl:apply-templates select=".//PubDate"/>
<xsl:apply-templates select=".//ArticleTitle"/>
<xsl:apply-templates select=".//JournalIssue"/>
<xsl:apply-templates select=".//ISOAbbreviation"/>
<xsl:if test="not(.//ISOAbbreviation)"><xsl:call-template name="periodical">
<xsl:with-param name="J" select=".//Journal/Title"/>
</xsl:call-template></xsl:if>
<xsl:apply-templates select=".//Pagination"/>
<xsl:apply-templates select=".//ArticleId[@IdType=&apos;doi&apos;]"/>
}}&lt;/ref&gt;</xsl:template>

<xsl:template match="PMID">
|id = [[PMID]]:<xsl:value-of select="."/>
|url= http://www.ncbi.nlm.nih.gov/pubmed/<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="ArticleId[@IdType=&apos;doi&apos;]">
|doi = <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="AuthorList">
<xsl:if  test="Author[1]">
|last=<xsl:value-of select="Author[1]/LastName"/>
|first=<xsl:value-of select="Author[1]/ForeName"/>
</xsl:if>
<xsl:if  test="Author[2]">
|last2=<xsl:value-of select="Author[2]/LastName"/>
|first2=<xsl:value-of select="Author[2]/ForeName"/>
</xsl:if>
<xsl:if  test="Author[3]">
|last3=<xsl:value-of select="Author[3]/LastName"/>
|first3=<xsl:value-of select="Author[3]/ForeName"/>
</xsl:if>
<xsl:if  test="Author[4]">
|last4=<xsl:value-of select="Author[4]/LastName"/>
|first4=<xsl:value-of select="Author[4]/ForeName"/>
</xsl:if>
</xsl:template>

<xsl:template match="PubDate">
|publication-date=<xsl:value-of select="Year"/><xsl:text> </xsl:text><xsl:value-of select="Month"/><xsl:text> </xsl:text><xsl:value-of select="Day"/>
|year=[[<xsl:value-of select="Year"/>]]</xsl:template>


<xsl:template match="ArticleTitle">
|title=<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="ISOAbbreviation"><xsl:call-template name="periodical">
<xsl:with-param name="J" select="."/>
</xsl:call-template>
</xsl:template>

<xsl:template name="periodical">
<xsl:param name="J"/>
|periodical=<xsl:choose>
<xsl:when test="$J='JAMA'">[[Journal of the American Medical Association|JAMA]]</xsl:when>
<xsl:when test="$J='Science'">[[Science Magazine|Science]]</xsl:when>
<xsl:when test="$J='Nature'">[[Nature (journal)|Nature]]</xsl:when>
<xsl:when test="$J='Endocrinology'">[[Endocrinology (journal)|Endocrinology]]</xsl:when>
<xsl:when test="$J='Genetics'">[[Genetics (journal)|Genetics]]</xsl:when>
<xsl:when test="$J='Proc. Natl. Acad. Sci. U.S.A.'">[[PNAS|Proc. Natl. Acad. Sci. U.S.A.]]</xsl:when>
<xsl:otherwise><xsl:value-of select="$J"/></xsl:otherwise>
</xsl:choose>
</xsl:template>



<xsl:template match="JournalIssue">
|volume=<xsl:value-of select="Volume"/>
|issue=<xsl:value-of select="Issue"/>
</xsl:template>


<xsl:template match="Pagination">
|pages=<xsl:value-of select="MedlinePgn"/>
</xsl:template>


</xsl:stylesheet>
