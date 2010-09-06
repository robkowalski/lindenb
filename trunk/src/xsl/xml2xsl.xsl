<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xs='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<!--

Author:
	Pierre Lindenbaum PhD
	plindenbaum@yahoo.fr

Motivation:
	This stylesheet transforms a xml to a draft XSL stylesheet transforming this XML document.
	Many element will be duplicated but the result can be a good start.

-->
<xsl:output method="xml" indent="yes" />

<xsl:template match="/">
<xsl:element name="xs:stylesheet">

<xsl:attribute name="version">1.0</xsl:attribute>
<xsl:element name="xs:template">
	<xsl:attribute name="match">
	<xsl:text>/</xsl:text>	
	</xsl:attribute>
	<xsl:element name="xs:apply-templates">
		<xsl:attribute name="select">
			<xsl:value-of select="name(*[1])"/>
		</xsl:attribute>
	</xsl:element>	
</xsl:element>
<xsl:apply-templates select="*" mode="matching"/>
</xsl:element>
</xsl:template>

<xsl:template match="*" mode="matching">
<xsl:comment>
===
=== Template match: <xsl:value-of select="name(.)"/>
=== 
</xsl:comment>
<xsl:element name="xs:template">
	<xsl:attribute name="match">
		<xsl:value-of select="name(.)"/>
	</xsl:attribute>
	<xsl:for-each select="@*">
		<xsl:element name="xs:value-of">
			<xsl:attribute name="select">
				<xsl:value-of select="concat('@',name(.))"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:for-each>

	<xsl:if test="count(text())=1">
		<xsl:element name="xs:value-of">
			<xsl:attribute name="select">
				<xsl:text>.</xsl:text>
			</xsl:attribute>
		</xsl:element>
	</xsl:if>

	<xsl:for-each select="*">
		<xsl:element name="xs:apply-templates">
			<xsl:attribute name="select">
				<xsl:value-of select="name(.)"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:for-each>
	
	
</xsl:element>
<xsl:apply-templates select="*" mode="matching"/>
</xsl:template>


</xsl:stylesheet>

