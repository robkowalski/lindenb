<?xml version='1.0' ?>
<xsl:stylesheet
	 xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	 xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:event="http://purl.org/NET/c4dm/event.owl#"
	xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#"
	xmlns:doap="http://usefulinc.com/ns/doap#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	version='1.0'
	>

<!--

This stylesheet transforms one or more Pubmed
Article in rdf format into a set of citations



-->
<xsl:output method='xml' indent="yes"/>


<xsl:template match="/">
<rdf:RDF
    >
<xsl:comment>
Generated with pubmed2rdf.xsl
Author: Pierre Lindenbaum PhD.
plindenbaum@yahoo.fr 
</xsl:comment>

<xsl:apply-templates/>
</rdf:RDF>
</xsl:template>

<xsl:template match="PubmedArticleSet">
	<xsl:apply-templates select="PubmedArticle"/>
</xsl:template>


<xsl:template match="PubmedArticle">

	<xsl:element name="bibo:Article">
	<xsl:variable name="pmid"><xsl:value-of select="MedlineCitation/PMID"/></xsl:variable>
	<xsl:attribute name="rdf:about">http://www.ncbi.nlm.nih.gov/pubmed/<xsl:value-of select="$pmid"/></xsl:attribute>
	<xsl:element name="bibo:pmid"><xsl:value-of select="$pmid"/></xsl:element>
	
	<xsl:element name="owl:sameAs"><xsl:attribute name="rdf:resource">http://bio2rdf.org/pubmed:<xsl:value-of select="$pmid"/></xsl:attribute></xsl:element>
	
	
	<xsl:apply-templates select="MedlineCitation"/>
	
	<xsl:apply-templates select="ArticleIdList"/>
	</xsl:element>
</xsl:template>
	
<xsl:template match="MedlineCitation">
	<xsl:apply-templates select="Article"/>
	<xsl:apply-templates select="MeshHeadingList"/>
	<xsl:apply-templates select="PersonalNameSubjectList"/>
</xsl:template>

<xsl:template match="Journal">
	<dcterms:isPartOf>
		<xsl:variable name="nlmId"><xsl:value-of select="../../MedlineJournalInfo/NlmUniqueID"/></xsl:variable>
		<xsl:element name="bibo:Journal">
			<xsl:attribute name="rdf:about">http://www.ncbi.nlm.nih.gov/sites/entrez?Db=nlmcatalog&amp;doptcmdl=Expanded&amp;cmd=search&amp;Term=<value-of select="$nlmId"/>[NlmId]<xsl:value-of select="$nlmId"/></xsl:attribute>
			<xsl:if test="ISSN[@IssnType='Electronic']"><bibo:eissn><xsl:value-of select="ISSN"/></bibo:eissn></xsl:if>
			<xsl:if test="ISSN[@IssnType='Print']"><bibo:issn><xsl:value-of select="ISSN"/></bibo:issn></xsl:if>
			
			<dc:title><xsl:value-of select="Title"/></dc:title>
			<bibo:shortTitle><xsl:value-of select="ISOAbbreviation"/></bibo:shortTitle>
		</xsl:element>
	</dcterms:isPartOf>
</xsl:template>



<xsl:template match="Article">
	<xsl:apply-templates select="ArticleTitle"/>
	
	<xsl:call-template name="articleDate">
		<xsl:with-param name="date" select="Journal/JournalIssue/PubDate"/>
	</xsl:call-template>
	
	<xsl:apply-templates select="Abstract/AbstractText"/>
	<xsl:apply-templates select="Journal"/>
	<xsl:apply-templates select="Journal/JournalIssue"/>
	<xsl:apply-templates select="Pagination/MedlinePgn"/>
	<xsl:apply-templates select="AuthorList"/>
	<xsl:apply-templates select="Language"/>
</xsl:template>

<xsl:template match="Abstract/AbstractText">
	<bibo:abstract><xsl:value-of select="."/></bibo:abstract>
</xsl:template>

<xsl:template match="ArticleTitle">
	<dc:title><xsl:value-of select="."/></dc:title>
</xsl:template>

<xsl:template match="ArticleTitle">
<dc:title><xsl:choose>
<xsl:when test="substring(.,1,1)='[' and substring(.,string-length(.))=']'">
<xsl:value-of select="substring(.,2,string-length(.)-1)"/></xsl:when>
<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
</xsl:choose></dc:title></xsl:template>

<xsl:template match="JournalIssue">
<xsl:if test="Volume"><bibo:volume><xsl:value-of select="Volume"/></bibo:volume></xsl:if>
<xsl:if test="Issue"><bibo:issue><xsl:value-of select="Issue"/></bibo:issue></xsl:if>
</xsl:template>

<xsl:template match="MedlinePgn">
	<bibo:pages><xsl:value-of select="."/></bibo:pages>
</xsl:template>



<xsl:template match="AuthorList">
<bibo:authorList>

<xsl:call-template name="buildAuthorShip">
<xsl:with-param name="index">1</xsl:with-param>
<xsl:with-param name="authors" select="Author"></xsl:with-param>
</xsl:call-template>

</bibo:authorList>
</xsl:template>

<xsl:template name="buildAuthorShip">
<xsl:param name="index">1</xsl:param>
<xsl:param name="authors"/>
<rdf:List>
	<rdf:first>
		<xsl:call-template name="foafPerson">
			<xsl:with-param name="node" select="$authors[$index]"/>
		</xsl:call-template>
	</rdf:first>
<xsl:choose>
<xsl:when test="$index = count($authors)">
	<rdf:rest rdf:resource="http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"/>
</xsl:when>
<xsl:otherwise>
	<rdf:rest>
		<xsl:call-template name="buildAuthorShip">
			<xsl:with-param name="index" select="1+ $index"/>
			<xsl:with-param name="authors" select="$authors"/>
		</xsl:call-template>
	</rdf:rest>
</xsl:otherwise>
</xsl:choose>
</rdf:List>
</xsl:template>


<xsl:template name="foafPerson">
<xsl:param name="node"/>

<xsl:variable name="firstName"><xsl:choose>
<xsl:when test="$node/ForeName"><xsl:value-of select="$node/ForeName"/></xsl:when>
<xsl:when test="$node/FirstName"><xsl:value-of select="$node/FirstName"/></xsl:when>
<xsl:otherwise></xsl:otherwise>
</xsl:choose></xsl:variable>

<xsl:variable name="middleName"><xsl:choose>
<xsl:when test="$node/MiddleName"><xsl:value-of select="$node/MiddleName"/></xsl:when>
<xsl:otherwise></xsl:otherwise>
</xsl:choose></xsl:variable>

<xsl:variable name="lastName"><xsl:choose>
<xsl:when test="$node/LastName"><xsl:value-of select="$node/LastName"/></xsl:when>
<xsl:when test="$node/Initials"><xsl:value-of select="$node/Initials"/></xsl:when>
<xsl:when test="$node/CollectiveName">Collective Work</xsl:when>
<xsl:otherwise></xsl:otherwise>
</xsl:choose></xsl:variable>

<xsl:variable name="initial"><xsl:choose>
<xsl:when test="$node/Initials"><xsl:value-of select="$node/Initials"/></xsl:when>
<xsl:when test="$node/LastName"><xsl:value-of select="substring($node/LastName,1,1)"/></xsl:when>
<xsl:otherwise></xsl:otherwise>
</xsl:choose></xsl:variable>

<xsl:variable name="uri">http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&amp;Cmd=Search&amp;Term=%22<xsl:value-of select="$lastName"/>%20<xsl:value-of select="$initial"/>%22[Author]&amp;itool=EntrezSystem2.PEntrez.Pubmed.Pubmed_ResultsPanel.Pubmed_DiscoveryPanel.Pubmed_RVAbstractPlus</xsl:variable>

	<xsl:element name="foaf:Person">
		<xsl:attribute name="rdf:about"><xsl:value-of select="$uri"/></xsl:attribute>
		<foaf:name><xsl:value-of select="$firstName"/><xsl:text> </xsl:text><xsl:value-of select="$middleName"/><xsl:text> </xsl:text><xsl:value-of select="$lastName"/></foaf:name>
		<foaf:title>Dr</foaf:title>
		<foaf:firstName><xsl:value-of select="$firstName"/></foaf:firstName>
		<foaf:family_name><xsl:value-of select="$lastName"/></foaf:family_name>
		<xsl:element name="foaf:made">
		<xsl:attribute name="rdf:resource">http://www.ncbi.nlm.nih.gov/pubmed/<xsl:value-of select="../../PMID"/></xsl:attribute>
		</xsl:element>
	</xsl:element>
</xsl:template>



<xsl:template match="ArticleIdList">
	<bibo:doi><xsl:value-of select="ArticleId"/></bibo:doi>
</xsl:template>


<xsl:template match="ArticleId">
	<xsl:choose>
		<xsl:when test="@IdType=&apos;doi&apos;">
		<bibo:doi><xsl:value-of select="."/></bibo:doi>
		</xsl:when>
	</xsl:choose>
</xsl:template>





<xsl:template match="MeshHeadingList">
	<xsl:apply-templates select="MeshHeading"/>
</xsl:template>

<xsl:template match="MeshHeading">
	<xsl:for-each select="DescriptorName">
		<xsl:element name="dc:subject"><xsl:value-of select="."/></xsl:element>
	</xsl:for-each>
</xsl:template>


<xsl:template match="PersonalNameSubjectList">
	<xsl:apply-templates select="PersonalNameSubject"/>
</xsl:template>


<xsl:template match="PersonalNameSubject">
	<xsl:element name="rdfs:seeAlso">
	<xsl:attribute name="rdf:resource">http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&amp;term=<xsl:value-of select="LastName"/>%20<xsl:value-of select="Initials"/>[PS]</xsl:attribute>
	</xsl:element>
</xsl:template>


<xsl:template match="Language">
	<dcterms:language><xsl:value-of select="."/></dcterms:language>
</xsl:template>



<xsl:template name="articleDate">
<xsl:param name="date"/>
<xsl:if test="$date/Year"><xsl:element name="dc:date">
<xsl:value-of select="$date/Year"/><xsl:if test="$date/Month">-<xsl:call-template name="month2int"><xsl:with-param name="m"><xsl:value-of select="$date/Month"/></xsl:with-param></xsl:call-template><xsl:if test="$date/Day">-<xsl:value-of select="$date/Day"/></xsl:if></xsl:if>
</xsl:element></xsl:if>
</xsl:template>

<xsl:template name="month2int">
<xsl:param name="m"/>
<xsl:variable name="month"><xsl:value-of select="translate($m,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/></xsl:variable>
<xsl:choose>
<xsl:when test="$month='january' or $month='jan' or $month='jan.'">01</xsl:when>
<xsl:when test="$month='february' or $month='feb' or $month='feb.'">02</xsl:when>
<xsl:when test="$month='march' or $month='mar' or $month='mar.'">03</xsl:when>
<xsl:when test="$month='april' or $month='apr' or $month='apr.'">04</xsl:when>
<xsl:when test="$month='may'">05</xsl:when>
<xsl:when test="$month='june'  or $month='jun' or $month='jun.'">06</xsl:when>
<xsl:when test="$month='july'  or $month='jul' or $month='jul.'">07</xsl:when>
<xsl:when test="$month='august' or $month='aug' or $month='aug.' ">08</xsl:when>
<xsl:when test="$month='september' or $month='sep' or $month='sep.' ">09</xsl:when>
<xsl:when test="$month='october' or $month='oct' or $month='oct.' ">10</xsl:when>
<xsl:when test="$month='november' or $month='nov' or $month='nov.'  ">11</xsl:when>
<xsl:when test="$month='december' or $month='dec' or $month='dec.' ">12</xsl:when>
<xsl:otherwise><xsl:value-of select="$month"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
