<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version='1.0'
	>
<!--

Motivation:
	This stylesheet transforms the output of 'desc table *' in mysql+XML for the UCSC database
to a JSON file

Author:
	Pierre Lindenbaum
	http://plindenbaum.blogspot.com
	plindenbaum@yahoo.fr
Params:
	* "var" var @name={content}; will be added OPTIONAL
	* "ucsc"=true extension for ucsc tables OPTIONAL
	* "mongo"=name for mondodb collection OPTIONAL
-->
<xsl:output method="text" version="1.0" encoding="UTF-8"/>

<xsl:param name="var"></xsl:param>
<xsl:param name="ucsc">false</xsl:param>
<xsl:param name="mongo"></xsl:param>
<!-- ================ DOCUMENT ROOT ========================================================== -->
<xsl:template match="/">
<xsl:if test="string-length($var)&gt;0 and string-length($mongo)=0"><xsl:value-of select="concat('var ',$var,'=')"/></xsl:if>

<xsl:apply-templates/>
<xsl:if test="string-length($var)&gt;0 and string-length($mongo)=0"><xsl:text>;</xsl:text></xsl:if>
<xsl:text>
</xsl:text>
</xsl:template>
<!-- ================ RESULT SET ========================================================== -->
<xsl:template match="resultset">

<xsl:if test="string-length($mongo)=0">
	<xsl:text>[</xsl:text>
</xsl:if>


<xsl:for-each select="row">
<xsl:if test="string-length($mongo)&gt;0">
	<xsl:text>record=</xsl:text>
</xsl:if>

<xsl:if test="position()!=1 and string-length($mongo)=0"><xsl:text>,</xsl:text></xsl:if>
<xsl:text>{</xsl:text>
<xsl:for-each select="field">
<xsl:if test="position()!=1"><xsl:text>,</xsl:text></xsl:if>
<xsl:apply-templates select="."/>
</xsl:for-each>
<xsl:text>}</xsl:text>

<xsl:if test="string-length($mongo)&gt;0">
	<xsl:text>; db.</xsl:text>
	<xsl:value-of select="$mongo"/>
	<xsl:text>.save(record);
</xsl:text>
</xsl:if>
</xsl:for-each>

<xsl:if test="string-length($mongo)=0">
	<xsl:text>]</xsl:text>
</xsl:if>


</xsl:template>




<xsl:template match="field">
<xsl:variable name="s" select="."/>

<xsl:choose>
	<xsl:when test="@name='id' and string-length($mongo)&gt;0">
		<xsl:text>_id</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="quote">
			<xsl:with-param name="s" select="@name"/>
		</xsl:call-template>
	</xsl:otherwise>
</xsl:choose>


 <xsl:text>:</xsl:text>

<xsl:choose>
	<xsl:when test="$ucsc='true' and (@name='exonEnds' or @name='exonStarts' )">
		 <xsl:text>[</xsl:text>
		 <xsl:call-template name="exons">
		 	<xsl:with-param name="s" select="$s"/>
		 </xsl:call-template>
		 <xsl:text>]</xsl:text>
	</xsl:when>
	<xsl:when test="$s='-'">
		 <xsl:text>&quot;-&quot;</xsl:text>
	</xsl:when>
	<xsl:when test="$s='+'">
		 <xsl:text>&quot;+&quot;</xsl:text>
	</xsl:when>
	<xsl:when test="number($s)=number($s)"><!-- test will fail if NaN=Nan -->
		<xsl:value-of select="$s"/>
	</xsl:when>
	<xsl:when test="$s='FALSE' or $s='false'">
		<xsl:text>false</xsl:text>
	</xsl:when>
	<xsl:when test="$s='TRUE' or $s='true'">
		<xsl:text>true</xsl:text>
	</xsl:when>
	<xsl:when test="@xsi:nil='true'">
		<xsl:text>null</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="quote">
			<xsl:with-param name="s" select="$s"/>
		</xsl:call-template>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="quote">
 <xsl:param name="s"/>
 <xsl:text>&quot;</xsl:text>
 <xsl:call-template name="escape">
	<xsl:with-param name="s" select="$s"/>
  </xsl:call-template>
 <xsl:text>&quot;</xsl:text>
</xsl:template>

<xsl:template name="escape">
 <xsl:param name="s"/>
<xsl:choose>
	<xsl:when test="contains($s,'&quot;')">
		<xsl:value-of select="substring-before($s,'&quot;')"/>
		<xsl:text>\&quot;</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select='$s'/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="exons">
<xsl:param name="s"/>
<xsl:variable name="s2" select="normalize-space($s)"/>
<xsl:choose>
	<xsl:when test="$s2=',' or $s2=''">
	</xsl:when>
	<xsl:when test="contains($s2,',')">
		<xsl:value-of select="substring-before($s2,',')"/>
		<xsl:variable name="s3" select="substring-after($s2,',')"/>
		<xsl:if test="$s3!=',' and $s3!=''">
			<xsl:text>,</xsl:text>
			<xsl:call-template name="exons">
				<xsl:with-param name="s" select="$s3"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select='$s2'/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
