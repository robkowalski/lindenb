<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#"
	xmlns:doac="http://ramonantonio.net/doac/0.1/"
	version='1.0'
	>
<xsl:output method="xml" indent="yes" encoding="UTF-8" />	

<!--

Author:
	Pierre Lindenbaum PhD
	plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com

Motivation:
	transform a linkedin profile to a FOAF profile
	Warning it just works with the current linkedin html (Last updated: 2010-01-03)

Param:
	'geoloc'=false: don't use geonames.org to find position

Usage:
	xsltproc \-\-html linkedin2foaf.xsl http://www.linkedin.com/in/lindenbaum
	xsltproc \-\-html linkedin2foaf.xsl http://www.linkedin.com/in/dsingh
-->

<xsl:param name="geoloc">yes</xsl:param>

<xsl:template match="/">
<rdf:RDF>
<xsl:apply-templates select="html"/>
</rdf:RDF>
</xsl:template>

<xsl:template match="html">

<xsl:apply-templates select="body"/>

</xsl:template>

<xsl:template match="body">
<xsl:variable name="action">
<xsl:value-of select="//a[@class='action' and @rel='nofollow'][1]/@href"/>
</xsl:variable>
<xsl:variable name="lkid" select="concat('http://www.linkedin.com/ppl/webprofile?id=',substring-before(substring-after($action,'id='),'&amp;'))"/>
<xsl:element name="foaf:Person">
<xsl:attribute name="rdf:about"><xsl:value-of select="$lkid"/></xsl:attribute>
 <xsl:apply-templates/>
 
 <foaf:holdsAccount>
		<xsl:element name="foaf:OnlineAccount">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$lkid"/>
			</xsl:attribute>
			<foaf:accountServiceHomepage rdf:resource="http://www.linkedin.com"/>
		</xsl:element>
	</foaf:holdsAccount>
 
</xsl:element>
</xsl:template>

<xsl:template match="h1[@id='name']">
<foaf:name><xsl:value-of select="."/></foaf:name>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="span[@class='given-name']">
<foaf:givenname><xsl:value-of select="."/></foaf:givenname>
</xsl:template>

<xsl:template match="span[@class='family-name']">
<foaf:family_name><xsl:value-of select="."/></foaf:family_name>
</xsl:template>


<xsl:template match="p[@class='headline title summary']|p[@class='headline title']">
<doac:summary>
<xsl:value-of select="normalize-space(.)"/>
</doac:summary>
</xsl:template>

<xsl:template match="ul[@class='websites']">
<xsl:apply-templates select="li/a[@href]"/>
</xsl:template>

<xsl:template match="div[@id='experience']">
<xsl:for-each select="ul/li">
<doac:experience>
<doac:Experience>
<doac:title><xsl:value-of select="normalize-space(h3)"/></doac:title>
<doac:location><xsl:value-of select="normalize-space(h4[@class='org summary'])"/></doac:location>

<xsl:if test="p[@class='period']/abbr[@class='dtstart']/@title">
	<doac:date-starts>
		<xsl:value-of select="normalize-space(p[@class='period']/abbr[@class='dtstart']/@title)"/>
	</doac:date-starts>
</xsl:if>

<xsl:if test="p[@class='period']/abbr[@class='dtend']/@title">
	<doac:date-ends>
		<xsl:value-of select="normalize-space(p[@class='period']/abbr[@class='dtend']/@title)"/>
	</doac:date-ends>
</xsl:if>

<xsl:if test="p[@class='description']">
	<doac:activity>
		<xsl:value-of select="normalize-space(p[@class='description'])"/>
	</doac:activity>
</xsl:if>
</doac:Experience>
</doac:experience>
</xsl:for-each>
</xsl:template>


<xsl:template match="a[@href][@class='url']">
<xsl:choose>
<xsl:when test="starts-with(@href,'http://twitter.com/')">
	<foaf:holdsAccount>
		<xsl:element name="foaf:OnlineAccount">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="@href"/>
			</xsl:attribute>
			<foaf:accountName>
				<xsl:value-of select="substring-after(@href,'.com/')"/>
			</foaf:accountName>
			<foaf:accountServiceHomepage rdf:resource="http://twitter.com"/>
		</xsl:element>
	</foaf:holdsAccount>
</xsl:when>
<xsl:when test="starts-with(@href,'http://friendfeed.com/')">
	<foaf:holdsAccount>
		<xsl:element name="foaf:OnlineAccount">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="@href"/>
			</xsl:attribute>
			<foaf:accountName>
				<xsl:value-of select="substring-after(@href,'.com/')"/>
			</foaf:accountName>
			<foaf:accountServiceHomepage rdf:resource="http://friendfeed.com"/>
		</xsl:element>
	</foaf:holdsAccount>
</xsl:when>
<xsl:otherwise>
	<xsl:element name="foaf:homepage">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="@href"/>
		</xsl:attribute>
	</xsl:element>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="div[@class='image']">
	<xsl:apply-templates select="img[@class='photo']"/>
</xsl:template>

<xsl:template match="img[@class='photo']">
<foaf:depiction>
	<xsl:element name="foaf:Image">
		<xsl:attribute name="rdf:about">
			<xsl:value-of select="@src"/>
		</xsl:attribute>
		<dc:title><xsl:value-of select="@alt"/></dc:title>
	</xsl:element>
</foaf:depiction>
</xsl:template>

<xsl:template match="p[@class='skills']">
<xsl:call-template name="skills">
<xsl:with-param name="s" select="normalize-space(translate(.,',',' '))"/>
</xsl:call-template>
</xsl:template>

<xsl:template name="skills">
<xsl:param name="s"/>
<xsl:choose>
<xsl:when test="contains($s,' ')">
	<xsl:call-template name="skills">
		<xsl:with-param name="s" select="normalize-space(substring-after($s,' '))"/>
	</xsl:call-template>
	<doac:skill><xsl:value-of select="substring-before($s,' ')"/></doac:skill>
</xsl:when>
<xsl:otherwise>
	<doac:skill><xsl:value-of select="$s"/></doac:skill>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="div[@id='education']">
<xsl:for-each select="ul[@class='vcalendar']/li">
<doac:education>
	<doac:Education>
		<foaf:organization><xsl:value-of select="normalize-space(h3)"/></foaf:organization>
		<doac:title><xsl:value-of select="normalize-space(div/p/span[@class='degree'])"/></doac:title>
		
		<xsl:if test="div/p/abbr[@class='dtstart']/@title">
			<doac:date-starts>
				<xsl:value-of select="normalize-space(div/p/abbr[@class='dtstart']/@title)"/>
			</doac:date-starts>
		</xsl:if>
		
		<xsl:if test="div/p/abbr[@class='dtend']/@title">
			<doac:date-ends>
				<xsl:value-of select="normalize-space(div/p/abbr[@class='dtend']/@title)"/>
			</doac:date-ends>
		</xsl:if>
		
		<xsl:if test="div/p[@class='notes']">
			<doac:subject>
				<xsl:value-of select="normalize-space(div/p[@class='notes'])"/>
			</doac:subject>
		</xsl:if>
	</doac:Education>
</doac:education>
</xsl:for-each>
</xsl:template>


<xsl:template match="p[@class='locality']">
<xsl:if test="$geoloc='yes'">
<xsl:variable name="url" select="concat('http://ws.geonames.org/search?q=',translate(normalize-space(.),' ','+'),'&amp;maxRows=1')"/>
<xsl:message terminate="no">Downloading <xsl:value-of select="$url"/></xsl:message>
 <xsl:apply-templates select="document($url,/geonames)" mode="geo"/>
<xsl:message terminate="no">Done.</xsl:message>
</xsl:if>
</xsl:template>

<xsl:template match="script|head|meta|link">
</xsl:template>

<xsl:template match="div|span">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*|text()">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="/" mode="geo">
<xsl:apply-templates select="geonames" mode="geo"/>
</xsl:template>

<xsl:template match="geonames" mode="geo">
<xsl:apply-templates select="geoname[1]" mode="geo"/>
</xsl:template>

<xsl:template match="geoname" mode="geo">
<foaf:based_near> 
	<geo:Point>
		<dc:title>
			<xsl:value-of select="name"/>
			<xsl:text>, </xsl:text>
			<xsl:value-of select="countryCode"/>
		</dc:title>
		<geo:long><xsl:value-of select="lng"/></geo:long>
		<geo:lat><xsl:value-of select="lat"/></geo:lat>
	</geo:Point> 
</foaf:based_near> 
</xsl:template>


</xsl:stylesheet>
