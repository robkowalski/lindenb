<?xml version="1.0"?>
<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  >
<!--

Motivation:
	transforms a mysql resultset XML to XSL-FO
Author:
	Pierre Lindenbaum PhD plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com
Usage:
        fop  -xml source-mysql.xml -xsl mysql2fo.xsl -pdf result.pdf
-->


<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

<xsl:template match="/">
<fo:root>
<xsl:comment>Created with mysql2fo Pierre Lindenbaum http://plindenbaum.blogspot.com</xsl:comment>
 <fo:layout-master-set>

   

  <fo:simple-page-master master-name="main"
	margin-top="36pt"
	margin-bottom="36pt"
	page-width="210mm"
	page-height="297mm"
	margin-left="1cm"
	margin-right="1cm"
	>
   <fo:region-body margin-bottom="50pt" margin-right="50pt"/>
  </fo:simple-page-master>
 </fo:layout-master-set>


 <fo:page-sequence master-reference="main">


 <fo:flow flow-name="xsl-region-body">
  <xsl:apply-templates select="resultset" />
 </fo:flow>
 </fo:page-sequence>

</fo:root>
</xsl:template>


<xsl:template match="resultset">
	<xsl:choose>
		<xsl:when test="count(row)&gt;0">
		 <xsl:variable name="cols" select="count(row[1]/field)"/>
		<fo:block><xsl:value-of select="@statement"/></fo:block>
		
			


		 <fo:table table-layout="fixed" border-collapse="collapse"   width="100%" font-size="12pt" font-family="Arial" >
		   <xsl:for-each select="row[1]/field">
                      <xsl:element name="fo:table-column">
                        <xsl:attribute name="column-width"><xsl:value-of select="100.0 div $cols"/><xsl:text>%</xsl:text></xsl:attribute>
		      </xsl:element>
                   </xsl:for-each>
                  <fo:table-header  color="white" background-color="blue" font-weight="bold">
                   	<fo:table-row>
			   <xsl:for-each select="row[1]/field">
		              <xsl:element name="fo:table-cell">
		                <xsl:attribute name="padding">2pt</xsl:attribute>
				<xsl:attribute name="background-color">brown</xsl:attribute>
				<xsl:attribute name="color">white</xsl:attribute>
				<xsl:attribute name="font-weight">bold</xsl:attribute>
				<xsl:attribute name="font-size">14pt</xsl:attribute>
				<xsl:attribute name="text-align">center</xsl:attribute>
				 <fo:block><xsl:value-of select="@name"/></fo:block>
			      </xsl:element>
		           </xsl:for-each>
                    	</fo:table-row>
		  </fo:table-header>
		  <fo:table-body>
		          <xsl:for-each select="row">
				  <xsl:element name="fo:table-row">
                                  <xsl:attribute name="background-color">
					<xsl:choose>
					   <xsl:when test="position() mod 2 = 1">#FCF6CF</xsl:when>
					   <xsl:otherwise>#FEFEF2</xsl:otherwise>
					</xsl:choose>
                                  </xsl:attribute>

				  	<xsl:for-each select="field">
						<fo:table-cell padding="2pt" border="1pt solid black">
								<xsl:choose>
									<xsl:when test="@xsi:nil=&apos;true&apos;">
										<fo:block><fo:inline font-style="italic" color="gray">NULL</fo:inline></fo:block>
									</xsl:when>
									<xsl:otherwise>
										<fo:block><xsl:call-template name="content"><xsl:with-param name="text" select="."/></xsl:call-template></fo:block>
									</xsl:otherwise>
								</xsl:choose>
            					</fo:table-cell>
					</xsl:for-each>
				  </xsl:element>
		          </xsl:for-each>
		  </fo:table-body>
		 </fo:table>
		</xsl:when>
		<xsl:otherwise>
			<fo:flow flow-name="xsl-region-body">
			<fo:block font-size="14pt" line-height="16pt" >
			No Result
			</fo:block>
			</fo:flow>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- analyse the content and tries to create an hyperlink -->
<xsl:template name="content">
<xsl:param name="text"/>
<xsl:choose>
<xsl:when test="string-length($text)&gt;2 and starts-with($text,&apos;rs&apos;) and string-length(translate(substring($text,3),&apos;0123456789&apos;,&apos;&apos;))=0">
<xsl:element name="fo:basic-link">
<xsl:attribute name="color">blue</xsl:attribute>
<xsl:attribute name="external-destination">http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=<xsl:value-of select="substring($text,3)"/></xsl:attribute>
<xsl:value-of select="$text"/>
</xsl:element>
</xsl:when>
<xsl:when test="starts-with($text,&apos;http://&apos;) or starts-with($text,&apos;https://&apos;)  or starts-with($text,&apos;mailto://&apos;)  or starts-with($text,&apos;ftp://&apos;)">
<xsl:element name="fo:basic-link">
<xsl:attribute name="color">blue</xsl:attribute>
<xsl:attribute name="external-destination"><xsl:value-of select="$text"/></xsl:attribute>
<xsl:value-of select="$text"/>
</xsl:element>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$text"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


</xsl:stylesheet>
