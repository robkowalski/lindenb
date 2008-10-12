<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version='1.0'
	>
<xsl:param name="package">test</xsl:param>
<!--

This stylesheet transforms the output of 'desc table *' in mysql+XML
into an hibernate mapping xml file

-->
<xsl:output method="xml" version="1.0" encoding="UTF-8"
 	doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd" 
     	doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"
     	indent="yes"
	/>



<xsl:template match="/">
<hibernate-mapping>
<xsl:comment>
Generated with sql2hbm.xsl
Author: Pierre Lindenbaum PhD.
plindenbaum@yahoo.fr 
</xsl:comment>
<xsl:apply-templates/>
</hibernate-mapping>
</xsl:template>

<xsl:template match="resultset">

<xsl:variable name="tableName" select="normalize-space(substring(@statement,5))"/>
<xsl:element name="class">
<xsl:attribute name="name"><xsl:value-of select="$package"/>.<xsl:call-template name="java-name"><xsl:with-param name="name"><xsl:value-of select="$tableName"/></xsl:with-param></xsl:call-template></xsl:attribute>
<xsl:attribute name="table"><xsl:value-of select="$tableName"/></xsl:attribute>

<xsl:for-each select="row">
<xsl:choose>
<xsl:when test="field[@name='Key']='PRI'">
<xsl:element name="id">
<xsl:attribute name="name"><xsl:value-of select="field[@name='Field']"/></xsl:attribute>
<generator class="native"/>
</xsl:element>

</xsl:when>
<xsl:otherwise>
<xsl:element name="property">
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:attribute name="access">public</xsl:attribute>
<xsl:attribute name="name"><xsl:value-of select="field[@name='Field']"/></xsl:attribute>
<xsl:attribute name="type"><xsl:choose>
<xsl:when test="starts-with($type,'varchar(')">java.lang.String</xsl:when>
<xsl:when test="starts-with($type,'char(')">java.lang.String</xsl:when> 
<xsl:when test="$type='text'">java.lang.String</xsl:when>
<xsl:when test="starts-with($type,'bigint(')">java.math.BigInteger</xsl:when>
<xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise> 
</xsl:choose></xsl:attribute>


	<xsl:element name="column">
	  <xsl:attribute name="sql-type"><xsl:value-of select="$type"/></xsl:attribute>
	
	  <xsl:if test="contains($type,'(')">
	    <xsl:variable name="x1" select="substring-after($type,'(')"/>
	     <xsl:variable name="x2"><xsl:choose>
	      <xsl:when test="contains($x1,',')"><xsl:value-of select="substring-before($x1,',')"/></xsl:when>
	      <xsl:otherwise><xsl:value-of select="$x1"/></xsl:otherwise>
	    </xsl:choose></xsl:variable>
	    <xsl:attribute name="length"><xsl:value-of select="substring-before($x2,')')"/></xsl:attribute>
	  </xsl:if>
	
	  <xsl:if test="contains($type,',')">
	    <xsl:variable name="t" select="field[@name='Type']"/>
	    <xsl:variable name="x1" select="substring-after($type,',')"/>
	    <xsl:attribute name="precision"><xsl:value-of select="substring-before($x1,')')"/></xsl:attribute>
	  </xsl:if>
	  
	<!-- not-null -->
	<xsl:attribute name="not-null"><xsl:choose>
	  <xsl:when test="field[@name='Null']='YES'">false</xsl:when>
	  <xsl:otherwise>true</xsl:otherwise>
	</xsl:choose></xsl:attribute>	
	
	<!-- unique -->
	<xsl:attribute name="unique"><xsl:choose>
	  <xsl:when test="field[@name='Key']='UNI'">true</xsl:when>
	  <xsl:otherwise>false</xsl:otherwise>
	</xsl:choose></xsl:attribute>
	
	</xsl:element>
 
 </xsl:element>
 
</xsl:otherwise>
</xsl:choose>
</xsl:for-each> 

</xsl:element>
</xsl:template>

<xsl:template name="java-name">
<xsl:param name="name"/><xsl:value-of select="translate(substring($name,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:value-of select="substring($name,2)"/></xsl:template>

</xsl:stylesheet>