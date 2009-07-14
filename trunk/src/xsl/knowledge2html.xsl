<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY my "urn:ontology:lindenb.org/">
	<!ENTITY bibo "http://purl.org/ontology/bibo/" >
	<!ENTITY foaf "http://xmlns.com/foaf/0.1/" >
	<!ENTITY skos "http://www.w3.org/2004/02/skos/core#" >
	<!ENTITY dc "http://purl.org/dc/elements/1.1/" >
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >
	<!ENTITY event "http://purl.org/NET/c4dm/event.owl#" >
]>
<xsl:stylesheet
	xmlns:rdf="&rdf;"
	xmlns:my="&my;"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	xmlns:owl="&owl;"
	xmlns:rdfs="&rdfs;"
	xmlns:dc="&dc;"
	xmlns:foaf="&foaf;"
	xmlns:bibo="&bibo;"
	xmlns:xsd="&xsd;"
	xmlns:skos="&skos;"
	xmlns:event="&event;"
        xmlns:bio="http://vocab.org/bio/0.1/.html"
	>
<xsl:output method='xml' omit-xml-declaration="yes" encoding="ISO-8859-1" />

<xsl:template match="/">
<xsl:apply-templates/> 
</xsl:template>

<xsl:template match="rdf:RDF">
<div>
<div style="display:none;">
<rdf:RDF><xsl:apply-templates mode="copy-rdf"/></rdf:RDF>
</div>
<div><xsl:text>





</xsl:text><xsl:apply-templates select="*|text()"/><xsl:text>





</xsl:text></div>
</div>
</xsl:template>

<xsl:template match="my:Quote" >
<div>
<xsl:apply-templates select="my:quotation"/>
<xsl:apply-templates select="dc:source"/>
<xsl:apply-templates select="dc:author"/>
<xsl:call-template name="subjects"><xsl:with-param name="node" select="."/></xsl:call-template>
<xsl:call-template name="footer"/>
</div>
</xsl:template>

<xsl:template match="foaf:Person">
<xsl:element name="div">
<h3 style="font-size:200%; text-align:center;">
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="@rdf:about"/></xsl:attribute>
<xsl:attribute name="title"><xsl:value-of select="@rdf:about"/></xsl:attribute>
<xsl:attribute name="target"><xsl:value-of select="generate-id(@rdf:about)"/></xsl:attribute>
<xsl:choose>
<xsl:when test="foaf:name"><xsl:value-of select="foaf:name"/></xsl:when>
<xsl:when test="dc:title"><xsl:value-of select="dc:title"/></xsl:when>
<xsl:otherwise>
 <xsl:call-template name="shortName">
   <xsl:with-param name="uri" select="@rdf:about"/>
 </xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:element></h3>
</xsl:element>
<xsl:if test="foaf:depiction/foaf:Image">
<div style="text-align:center;">
<xsl:call-template name="thumbImage">
<xsl:with-param name="node" select="foaf:depiction/foaf:Image"/>
<xsl:with-param name="size">200px</xsl:with-param>
<xsl:with-param name="float">center</xsl:with-param>
</xsl:call-template>
</div>
</xsl:if>
<div style="width:50%; margin-left:25%; border:thin solid lightgray; ">
<xsl:apply-templates select="my:olb"/>
<dl>
  <xsl:if test="my:gender">
  <dt><b>Gender:</b></dt>
  <dd><xsl:apply-templates select="my:gender"/></dd>
  </xsl:if>
  <xsl:if test="my:nationality">
  <dt><b>Naionality:</b></dt>
  <dd><xsl:apply-templates select="my:nationality"/></dd>
  </xsl:if>
  <xsl:if test="my:birthDate">
  <dt><b>Birth Date:</b></dt>
  <dd><xsl:apply-templates select="my:birthDate"/></dd>
  </xsl:if>
  <xsl:if test="my:birthPlace">
  <dt><b>Birth Place:</b></dt>
  <dd><xsl:apply-templates select="my:birthPlace"/></dd>
  </xsl:if>
  <xsl:if test="my:deathDate">
  <dt><b>Death Date:</b></dt>
  <dd><xsl:apply-templates select="my:deathDate"/></dd>
  </xsl:if>
  <xsl:if test="my:deathPlace">
  <dt><b>Death Place:</b></dt>
  <dd><xsl:apply-templates select="my:deathPlace"/></dd>
  </xsl:if>
  <xsl:if test="my:hasFather">
  <dt><b>Father:</b></dt>
  <dd><xsl:apply-templates select="my:hasFather"/></dd>
  </xsl:if>
  <xsl:if test="my:hasMother">
  <dt><b>Mother:</b></dt>
  <dd><xsl:apply-templates select="my:hasMother"/></dd>
  </xsl:if>
  <xsl:if test="my:hasPartner">
  <dt><b>Partner:</b></dt>
  <dd><xsl:for-each select="my:hasPartner|my:hasHusband|my:hasWife"><xsl:if test="position()!=1"><br/></xsl:if><xsl:apply-templates select="."/></xsl:for-each></dd>
  </xsl:if>
  <xsl:if test="my:hasChildren">
  <dt><b>Children:</b></dt>
  <dd><xsl:for-each select="my:hasChildren"><xsl:if test="position()!=1"><br/></xsl:if><xsl:apply-templates select="."/></xsl:for-each></dd>
  </xsl:if>
</dl>
<xsl:call-template name="subjects"><xsl:with-param name="node" select="."/></xsl:call-template>
<xsl:call-template name="footer"/>
</div>
</xsl:template>


<xsl:template match="foaf:Person" mode="quotation">
<xsl:element name="div">
<xsl:if test="foaf:depiction/foaf:Image">
<xsl:call-template name="thumbImage">
<xsl:with-param name="node" select="foaf:depiction/foaf:Image"/>
</xsl:call-template>
</xsl:if>

<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="@rdf:about"/></xsl:attribute>
<xsl:attribute name="title"><xsl:value-of select="@rdf:about"/></xsl:attribute>
<xsl:attribute name="target"><xsl:value-of select="generate-id(@rdf:about)"/></xsl:attribute>
<xsl:choose>
<xsl:when test="foaf:name"><xsl:value-of select="foaf:name"/></xsl:when>
<xsl:when test="dc:title"><xsl:value-of select="dc:title"/></xsl:when>
<xsl:otherwise>
 <xsl:call-template name="shortName">
   <xsl:with-param name="uri" select="@rdf:about"/>
 </xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:element>
<xsl:text> </xsl:text>
<xsl:call-template name="biodates">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>

<xsl:apply-templates select="my:olb"/>

</xsl:element>
</xsl:template>


<xsl:template match="skos:Concept">
<table border="1" width="50%">
<caption>
<xsl:if test="skos:prefSymbol[@rdf:resource]">
<xsl:element name="img">
<xsl:attribute name="style">margin: 0pt 10px 10px 0pt; float:left;</xsl:attribute>
<xsl:attribute name="src"><xsl:value-of select="skos:prefSymbol/@rdf:resource"/></xsl:attribute>
<xsl:attribute name="width">64</xsl:attribute>
<xsl:attribute name="alt"><xsl:value-of select="skos:prefSymbol/@rdf:resource"/></xsl:attribute>
</xsl:element>
</xsl:if>
<h3><xsl:value-of select="skos:prefLabel"/></h3>
</caption>
</table>
</xsl:template>



<xsl:template name="thumbImage">
<xsl:param name="node"/>
<xsl:param name="size">64px</xsl:param>
<xsl:param name="float">left</xsl:param>
<xsl:element name="a">
<xsl:attribute name="target"><xsl:value-of select="generate-id($node)"/></xsl:attribute>
<xsl:attribute name="href">
 <xsl:choose>
  <xsl:when test="$node/dc:source"><xsl:value-of select="$node/dc:source"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="$node/@rdf:about"/></xsl:otherwise>
</xsl:choose>
</xsl:attribute>
<xsl:attribute name="title">
 <xsl:choose>
  <xsl:when test="$node/foaf:name"><xsl:value-of select="$node/foaf:name"/></xsl:when>
  <xsl:when test="$node/dc:title"><xsl:value-of select="$node/dc:title"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="$node/@rdf:about"/></xsl:otherwise>
</xsl:choose>
</xsl:attribute>

 <xsl:element name="img">
 <xsl:attribute name="style">margin: 0pt 10px 10px 0pt; float: <xsl:value-of select="$float"/>; width: <xsl:value-of select="$size"/> </xsl:attribute>
 <xsl:attribute name="border">0</xsl:attribute>
 <xsl:attribute name="src"><xsl:value-of select="$node/@rdf:about"/></xsl:attribute>
<xsl:attribute name="alt">
 <xsl:choose>
  <xsl:when test="$node/foaf:name"><xsl:value-of select="$node/foaf:name"/></xsl:when>
  <xsl:when test="$node/dc:title"><xsl:value-of select="$node/dc:title"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="$node/@rdf:about"/></xsl:otherwise>
</xsl:choose>
</xsl:attribute>
 </xsl:element>

</xsl:element>
</xsl:template>


<xsl:template match="owl:DatatypeProperty" >
<h1><xsl:value-of select="local-name(.)"/></h1>
<table class="table1"><caption><xsl:value-of select="@rdf:about"/></caption>
<col width="30%"/>
<col width="70%"/> 
<thead><tr><th>Property</th><th>Value</th></tr></thead>
<tbody>
<xsl:for-each select="rdfs:label|rdfs:comment">
<xsl:if test="not(@xml:lang)"><xsl:message terminate="yes" >missing @xml:lang</xsl:message></xsl:if>
<tr>
  <td><xsl:value-of select="local-name(.)"/></td>
  <td><xsl:value-of select="."/> (<i><xsl:value-of select="@xml:lang"/></i>)</td>
</tr>
</xsl:for-each>
<xsl:for-each select="rdfs:range|rdfs:domain">
<tr>
  <td><xsl:value-of select="local-name(.)"/></td>
  <td><u><xsl:value-of select="@rdf:resource"/></u></td>
</tr>
</xsl:for-each>
</tbody>
</table>
</xsl:template>

<xsl:template match="my:quotation" >
<xsl:element name="div">
<xsl:attribute name="style">font-size:300%; line-height: 130%; color:gray; margin:30px; padding:30px; white-space:pre-wrap;</xsl:attribute>
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute></xsl:if>
<span style="color:black;"><xsl:text>&#8220;</xsl:text></span>
<xsl:value-of select="."/>
<span style="color:black;"><xsl:text>&#8221;</xsl:text></span>
</xsl:element>
</xsl:template>

<xsl:template match="dc:subject" >
<xsl:choose>
<xsl:when test="@rdf:resource">
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="@rdf:resource"/></xsl:attribute>
<xsl:attribute name="title"><xsl:value-of select="@rdf:resource"/></xsl:attribute>
<xsl:attribute name="target"><xsl:value-of select="generate-id(@rdf:resource)"/></xsl:attribute>
<xsl:call-template name="shortName">
   <xsl:with-param name="uri" select="@rdf:resource"/>
 </xsl:call-template>
</xsl:element>
</xsl:when>
<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="dc:author" >

<xsl:choose>

<xsl:when test="foaf:Person">
  <xsl:apply-templates select="foaf:Person" mode="quotation"/>
</xsl:when>
<xsl:when test="@rdf:resource">
   <xsl:variable name="uri" select="@rdf:resource"/>
   <xsl:apply-templates select="/rdf:RDF/foaf:Person[@rdf:about = $uri]" mode="quotation"/>
</xsl:when>
<xsl:when test="count(*) = 0"><xsl:value-of select="."/></xsl:when>
<xsl:otherwise><xsl:apply-templates select="."/></xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="dc:source" >
<div style="text-align:right;font-style:italic;"><xsl:value-of select="."/></div>
</xsl:template>


<xsl:template match="my:birthDate|my:deathDate">
<xsl:choose>
<xsl:when test="count(*) = 0"><xsl:value-of select="."/></xsl:when>
<xsl:otherwise><xsl:apply-templates select="my:Date"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="my:Date">
<xsl:if test="my:circa = &apos;true&apos;">~</xsl:if>
<xsl:if test="my:year">
<xsl:if test="my:month">
<xsl:if test="my:day">
<xsl:value-of select="my:day"/><xsl:text> </xsl:text>
</xsl:if>
<xsl:call-template name="month2ascii"><xsl:with-param name="s" select="my:month"/></xsl:call-template><xsl:text>, </xsl:text>
</xsl:if>
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="concat(&apos;http://en.wikipedia.org/wiki/&apos;,my:year)"/></xsl:attribute>
<xsl:attribute name="title"><xsl:value-of select="concat(&apos;http://en.wikipedia.org/wiki/&apos;,my:year)"/></xsl:attribute>
<xsl:attribute name="target"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
<xsl:value-of select="my:year"/>
</xsl:element>
</xsl:if>
</xsl:template>

<xsl:template match="my:Place">
<xsl:variable name="name">
 <xsl:choose>
  <xsl:when test="dc:title"><xsl:value-of select="dc:title"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="@rdf:about"/></xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:element name="a">
<xsl:attribute name="href"><xsl:value-of select="@rdf:about"/></xsl:attribute>
<xsl:attribute name="title"><xsl:value-of select="$name"/></xsl:attribute>
<xsl:attribute name="target"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
<xsl:value-of select="$name"/>
</xsl:element>
</xsl:template>

<xsl:template match="my:olb">
<div><xsl:element name="cite">
<xsl:if test="@xml:lang"><xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute></xsl:if>
<xsl:attribute name="style"></xsl:attribute>
<xsl:value-of select="."/>
</xsl:element></div>
</xsl:template>

<xsl:template match="my:gender">
<xsl:choose>
		<xsl:when test="@rdf:resource"><xsl:apply-templates select="@rdf:resource"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="my:hasFather|my:hasMother|my:hasChildren|my:hasWife|my:hasHusband|my:hasPartner">
<xsl:choose>
	<xsl:when test="@rdf:resource"><xsl:apply-templates select="@rdf:resource"/></xsl:when>
	<xsl:when test="count(*)=0"><xsl:value-of select="."/></xsl:when>
        <xsl:otherwise>TODO</xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="my:nationality">
<xsl:element name="a">
  <xsl:attribute name="href"><xsl:value-of select="@rdf:resource"/></xsl:attribute>
  <xsl:attribute name="title"><xsl:call-template name="shortName"><xsl:with-param name="uri" select="@rdf:about"/></xsl:call-template></xsl:attribute>
  <xsl:attribute name="target"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
  <xsl:call-template name="shortName"><xsl:with-param name="uri" select="@rdf:resource"/></xsl:call-template>
</xsl:element>
</xsl:template>


<xsl:template match="@rdf:resource">
<xsl:variable name="short"><xsl:call-template name="shortName"><xsl:with-param name="uri" select="."/></xsl:call-template></xsl:variable>
<xsl:element name="a">
  <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
  <xsl:attribute name="title"><xsl:value-of select="$short"/></xsl:attribute>
  <xsl:attribute name="target"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
  <xsl:value-of select="$short"/>
</xsl:element>
</xsl:template>


<xsl:template name="biodates">
<xsl:param name="node"/>
<xsl:if test="$node/my:birthDate|$node/my:deathDate">
<xsl:text>(</xsl:text>
<xsl:choose>
<xsl:when test="$node/my:birthDate">
<xsl:apply-templates select="$node/my:birthDate"/>
</xsl:when>
<xsl:otherwise>?</xsl:otherwise>
</xsl:choose>
<xsl:if test="$node/my:deathDate">
<xsl:text> &#8211; </xsl:text>
<xsl:apply-templates select="$node/my:deathDate"/>
</xsl:if>
<xsl:text>)</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template name="subjects">
<xsl:param name="node"/>
<xsl:if test="count($node/dc:subject) != 0">
<div style="border:thin solid lightgray; margin:10px; clear: left; ">
<b style="color:gray;">Categories:</b><xsl:text> </xsl:text>
<xsl:for-each select="$node/dc:subject">
<xsl:if test="position()!=1"><xsl:text> | </xsl:text></xsl:if>
<xsl:apply-templates select="."/>
</xsl:for-each>
</div>
</xsl:if>
</xsl:template>

<xsl:template name="month2ascii">
<xsl:param name="s"/>
<xsl:variable name="m" select="number($s)"/>
<xsl:choose>
<xsl:when test="$m = 1">January</xsl:when>
<xsl:when test="$m = 2">February</xsl:when>
<xsl:when test="$m = 3">March</xsl:when>
<xsl:when test="$m = 4">April</xsl:when>
<xsl:when test="$m = 5">May</xsl:when>
<xsl:when test="$m = 6">June</xsl:when>
<xsl:when test="$m = 7">July</xsl:when>
<xsl:when test="$m = 8">August</xsl:when>
<xsl:when test="$m = 9">September</xsl:when>
<xsl:when test="$m = 10">October</xsl:when>
<xsl:when test="$m = 11">November</xsl:when>
<xsl:when test="$m = 12">December</xsl:when>
<xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="shortName">
<xsl:param name="uri"/>
<xsl:choose>
<xsl:when test="contains($uri,&apos;#&apos;)">
 <xsl:call-template name="shortName">
   <xsl:with-param name="uri" select="substring-after($uri,&apos;#&apos;)"/>
 </xsl:call-template>
</xsl:when>
<xsl:when test="contains($uri,&apos;/&apos;)">
 <xsl:call-template name="shortName">
   <xsl:with-param name="uri" select="substring-after($uri,&apos;/&apos;)"/>
 </xsl:call-template>
</xsl:when>
<xsl:when test="starts-with($uri,&apos;Category:&apos;)">
 <xsl:value-of select="translate(substring-after($uri,&apos;Category:&apos;),&apos;_&apos;,&apos; &apos;)"/>
</xsl:when>
<xsl:otherwise><xsl:value-of select="translate($uri,&apos;_&apos;,&apos; &apos;)"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="footer">
<div style="text-align:right;font-style:italic; color:gray; font-size:50%;"> <button title="Show Source" onclick="document.location='view-source:' + window.location.href ;"> <img border="0" src="http://www.w3.org/RDF/icons/rdf_metadata_button.32"
alt="RDF Resource Description Framework Metadata Icon"/></button><br/>Data transformed with <a href="http://code.google.com/p/lindenb/source/browse/trunk/src/xsl/knowledge2html.xsl">knowledge2html.xsl</a></div>
</xsl:template>

<xsl:template match="text()">
<xsl:if test="name(..)=&apos;rdf:RDF&apos; and count(preceding-sibling::*)!=0 and count(following-sibling::*)!=0"><xsl:text>





</xsl:text><hr/><xsl:text>





</xsl:text></xsl:if>
</xsl:template>

<xsl:template match="*" mode="copy-rdf">
<xsl:copy-of select=".">
<xsl:copy-of select="@*"/>
<xsl:apply-templates mode="copy-rdf"/> 
</xsl:copy-of>
</xsl:template>



</xsl:stylesheet>
