<?xml version='1.0'  encoding="ISO-8859-1" ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:g="http://www.ncbi.nlm.nih.gov/SNP/geno"
	xmlns:snp="http://www.ncbi.nlm.nih.gov/SNP/docsum"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns="http://ontology.lindenb.org/genotypes/"
	version='1.0'
	>
<!--
Author Pierre Lindenbaum PhD
http://plindenbaum.blogspot.com

xsltproc \-\-stringparam "with-sequence" yes  \-\-novalid genoexch2rdf.xsl SNPgenotype-100201-1244-3905.xml 

-->


<xsl:output method="xml" indent='yes'/>
<xsl:param name="with-sequence">no</xsl:param>

<xsl:template match="/">
<rdf:RDF>
<xsl:apply-templates/>
</rdf:RDF>
</xsl:template>

<xsl:template match="g:GenoExchange">
<xsl:apply-templates select="g:Population|g:Individual|g:SnpInfo"/>
</xsl:template>

<xsl:template match="g:Population">
<xsl:element name="Population">
<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_viewTable.cgi?type=pop&amp;pop_id=',@popId)"/>
</xsl:attribute>
	<handle><xsl:value-of select="@handle"/></handle>
	<locPopId><xsl:value-of select="@locPopId"/></locPopId>
</xsl:element>

</xsl:template>

<xsl:template match="g:Individual">
<xsl:element name="Individual">
<xsl:attribute name="rdf:about"><xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ind.cgi?ind_id=',@indId)"/>
</xsl:attribute>
	<xsl:element name="hasPop">
	<xsl:attribute name="rdf:resource">
	  <xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_viewTable.cgi?type=pop&amp;pop_id=',g:SubmitInfo/@popId)"/>
	</xsl:attribute>
	</xsl:element>
	<sex><xsl:value-of select="@sex"/></sex>
	<name><xsl:value-of select="g:SubmitInfo/@submittedIndId"/></name>
</xsl:element>
</xsl:template>

<xsl:template match="g:SnpInfo">
<xsl:variable name="rs_id" select="@rsId"/>
<xsl:element name="SNP">
	<xsl:attribute name="rdf:about">
	  <xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=',$rs_id)"/>
	</xsl:attribute>
	<name><xsl:value-of select="concat('rs',@rsId)"/></name>
	<observed><xsl:value-of select="@observed"/></observed>
	<xsl:if test="$with-sequence='yes'">
	 <xsl:variable name="url" select="concat('http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=snp&amp;id=',@rsId,'&amp;retmode=xml&amp;rettype=xml')"/>
	 <xsl:message terminate="no">Download <xsl:value-of select="$url"/></xsl:message>
	<xsl:variable name="dom" select="document($url,/)"/>
	<seq5><xsl:value-of select="$dom/snp:ExchangeSet/snp:Rs/snp:Sequence/snp:Seq5"/></seq5>
	<seq3><xsl:value-of select="$dom/snp:ExchangeSet/snp:Rs/snp:Sequence/snp:Seq3"/></seq3>
	</xsl:if>
</xsl:element>

<xsl:for-each select="g:SnpLoc">
	<MapLoc>
		<xsl:element name="hasSNP">
			<xsl:attribute name="rdf:resource">
			  <xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=',$rs_id)"/>
			</xsl:attribute>
		</xsl:element>
		<strand><xsl:choose>
		<xsl:when test="@rsOrientToChrom='fwd'">+</xsl:when>
		<xsl:when test="@rsOrientToChrom='rev'">-</xsl:when>
		<xsl:otherwise><xsl:value-of select="@rsOrientToChrom"/></xsl:otherwise>
		</xsl:choose></strand>
		<chrom><xsl:value-of select="@chrom"/></chrom>
		<allele><xsl:value-of select="@contigAllele"/></allele>
		<assembly rdf:resource="urn:assembly:{@genomicAssembly}"/>
	</MapLoc>
</xsl:for-each>

<xsl:for-each select="g:SsInfo/g:ByPop/g:GTypeByInd">
  <Genotype>
  	<xsl:element name="hasIndi">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ind.cgi?ind_id=',@indId)"/>
		</xsl:attribute>
	</xsl:element>
	<xsl:element name="hasSNP">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="concat('http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=',$rs_id)"/>
		</xsl:attribute>
	</xsl:element>
  	<allele1><xsl:value-of select="substring-before(@gtype,'/')"/></allele1>
  	<allele2><xsl:value-of select="substring-after(@gtype,'/')"/></allele2>
  </Genotype>
</xsl:for-each>

</xsl:template>

</xsl:stylesheet>
