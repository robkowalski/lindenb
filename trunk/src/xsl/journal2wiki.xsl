<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>

<!--

This stylesheet transforms a description of a journal 
from the NCBI to a new article about this journal

Author: Pierre Lindenbaum PhD plindenbaum@yahoo.fr

see: http://en.wikipedia.org/wiki/Template:Infobox_Journal

-->
<xsl:output method='text'/>


<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="SerialSet">
<xsl:apply-templates select="Serial"/>
</xsl:template>

<xsl:template match="Serial">
<xsl:variable name="url">http://www.ncbi.nlm.nih.gov/sites/entrez?Db=nlmcatalog&amp;doptcmdl=Expanded&amp;cmd=search&amp;Term=<xsl:value-of select="NlmUniqueID"/>%5BNlmId%5D</xsl:variable>
<xsl:text>



</xsl:text>
{{Infobox Journal
| title        = <xsl:value-of select="Title"/>
| cover        = 
| editor       = 
| discipline   = <xsl:for-each select="BroadJournalHeadingList/BroadJournalHeading">
<xsl:if test="position()&gt;1"><br/></xsl:if>[[<xsl:value-of select="."/>]]</xsl:for-each>
| language     = <xsl:choose>
<xsl:when test="Language=&apos;eng&apos;">[[English language|English]]</xsl:when>
<xsl:when test="Language=&apos;fre&apos;">[[French language|French]]</xsl:when>
<xsl:when test="Language=&apos;sp&apos;">[[Spanish language|Spanish]]</xsl:when>
<xsl:when test="Language=&apos;de&apos;">[[German language|Germany]]</xsl:when>
<xsl:when test="Language=&apos;ru&apos;">[[Russian language|Russian]]</xsl:when>
<xsl:otherwise><xsl:value-of select="Language"/></xsl:otherwise>
</xsl:choose>
| abbreviation = <xsl:value-of select="ISOAbbreviation"/>
| publisher    = [[<xsl:value-of select="PublicationInfo/Publisher"/>]]
| country      = [[<xsl:value-of select="PublicationInfo/Country"/>]]
| frequency    = <xsl:value-of select="PublicationInfo/Frequency"/>
| history      = [[<xsl:value-of select="PublicationInfo/PublicationFirstYear"/>]]<xsl:choose>
<xsl:when test="PublicationInfo/PublicationEndYear"> to [[<xsl:value-of select="PublicationInfo/PublicationEndYear"/>]] </xsl:when>
<xsl:otherwise> to present</xsl:otherwise>
</xsl:choose>
| openaccess   = 
| impact       = 
| impact-year  = 
| website      = 
| link1        = 
| link1-name   = 
| link2        =<xsl:value-of select="$url"/>
| link2-name   = National Library Of Medicine
| RSS          = 
| atom         = 
| JSTOR        = 
| OCLC         = 
| LCCN         = 
| CODEN        = 
| ISSN         = <xsl:value-of select="ISSN[@IssnType=&apos;Print&apos;]"/>
| eISSN        = <xsl:value-of select="ISSN[@IssnType=&apos;Electronic&apos;]"/>
}}
'''<xsl:value-of select="Title"/>''' <xsl:if test="ISOAbbreviation">(also known as '''<xsl:value-of select="ISOAbbreviation"/>''')</xsl:if> is a<xsl:choose>
<xsl:when test="PublicationInfo/Country=&apos;England&apos;"> [[United Kingdom|British]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;United States&apos;">n [[United States|American]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;France&apos;"> [[France|French]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Spain&apos;"> [[Spain|Spanish]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Germany&apos;"> [[Germany|Germa]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Russian&apos;"> [[Russian|Russia]]</xsl:when>
</xsl:choose> [[Academic_journal|journal]] founded in  [[<xsl:value-of select="PublicationInfo/PublicationFirstYear"/>]]. It covers the field<xsl:if test="count(BroadJournalHeadingList/BroadJournalHeading)&gt;1">s</xsl:if> of <xsl:for-each select="BroadJournalHeadingList/BroadJournalHeading">
<xsl:choose>
<xsl:when test="position()=1"></xsl:when>
<xsl:when test="position()=last()"> and </xsl:when>
<xsl:otherwise>, </xsl:otherwise>
</xsl:choose>[[<xsl:value-of select="."/>]]</xsl:for-each>. <xsl:value-of select="ContinuationNotes"/>

==References==
{{Reflist}}

==External Links==
* [<xsl:value-of select="$url"/> National Library Of Medicine]

{{DEFAULTSORT:<xsl:value-of select="SortSerialName"/>}}

{{Sci-journal-stub}}

[[Category:Publications established in <xsl:value-of select="PublicationInfo/PublicationFirstYear"/>]]
[[Category:Academic journals]]
[[Category:Academic publishing]]
<xsl:choose>
<xsl:when test="PublicationInfo/Country=&apos;England&apos;">[[Category:British journals]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;United States&apos;">[[Category:American journals]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;France&apos;">[[Category:French journals]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Spain&apos;">[[Category:Spanish journals]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Germany&apos;">[[Category:German journals]]</xsl:when>
<xsl:when test="PublicationInfo/Country=&apos;Russian&apos;">[[Category:Russian journals]]</xsl:when>
<xsl:otherwise>[[Category:<xsl:value-of select="Language"/> journals]]</xsl:otherwise>
</xsl:choose>
<xsl:for-each select="BroadJournalHeadingList/BroadJournalHeading">
[[Category:<xsl:choose>
<xsl:when test=".=&apos;Science&apos;">Scientific</xsl:when>
<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise></xsl:choose></xsl:for-each> literature]]
<xsl:text>



</xsl:text>
</xsl:template>



</xsl:stylesheet>
