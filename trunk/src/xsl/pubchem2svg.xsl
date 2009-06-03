<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
 version="1.0"
 xmlns:c="http://www.ncbi.nlm.nih.gov"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 xmlns:h="http://www.w3.org/1999/xhtml"
 >
<!-- ========================================================================= -->
<xsl:output method='xml' indent='yes' omit-xml-declaration="no"/>

<!-- ========================================================================= -->
<!-- the width of the SVG -->
<xsl:variable name="svg-width">800</xsl:variable>
<!-- the height of the SVG -->
<xsl:variable name="svg-height">800</xsl:variable>
<!-- the height of the SVG -->
<xsl:variable name="scale">20</xsl:variable>
<!-- number of atoms-->
<xsl:variable name="count"><xsl:value-of select="count(/c:PC-Compound/c:PC-Compound_atoms/c:PC-Atoms/c:PC-Atoms_aid/c:PC-Atoms_aid_E)"/></xsl:variable>

<xsl:variable name="a_array" select="/c:PC-Compound/c:PC-Compound_atoms/c:PC-Atoms/c:PC-Atoms_element"/>
<xsl:variable name="conformers" select="/c:PC-Compound/c:PC-Compound_coords/c:PC-Coordinates/c:PC-Coordinates_conformers/c:PC-Conformer"/>

<xsl:variable name="x_array" select="$conformers/c:PC-Conformer_x"/>
<xsl:variable name="y_array" select="$conformers/c:PC-Conformer_y"/>
<xsl:variable name="z_array" select="$conformers/c:PC-Conformer_z"/>


<xsl:variable name="min-x">
  <xsl:for-each select="$x_array/c:PC-Conformer_x_E">
    <xsl:sort select="." data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="max-x">
  <xsl:for-each select="$x_array/c:PC-Conformer_x_E">
    <xsl:sort select="." data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="min-y">
  <xsl:for-each select="$x_array/c:PC-Conformer_y_E">
    <xsl:sort select="." data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="max-y">
  <xsl:for-each select="$x_array/c:PC-Conformer_y_E">
    <xsl:sort select="." data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="min-z">
  <xsl:for-each select="$x_array/c:PC-Conformer_z_E">
    <xsl:sort select="." data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="max-z">
  <xsl:for-each select="$x_array/c:PC-Conformer_z_E">
    <xsl:sort select="." data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>


<xsl:template match="/">
<xsl:apply-templates select="c:PC-Compound"/>
</xsl:template>


<xsl:template match="c:PC-Compound">
 <xsl:element name="svg:svg">
 <xsl:attribute name="version">1.0</xsl:attribute>
 <xsl:attribute name="width"><xsl:value-of select="$svg-width"/></xsl:attribute>
 <xsl:attribute name="height"><xsl:value-of select="$svg-height"/></xsl:attribute>
 <svg:title><xsl:value-of select="c:PC-Compound_id/c:PC-CompoundType/c:PC-CompoundType_id/c:PC-CompoundType_id_cid"/></svg:title>
 
 
  <!-- DEFINITIONS -->
 <xsl:element name="svg:defs">
 
 <xsl:call-template name="gradient">
   <xsl:with-param name="gid">o</xsl:with-param>
   <xsl:with-param name="start">white</xsl:with-param>
   <xsl:with-param name="end">blue</xsl:with-param>
   <xsl:with-param name="r">18</xsl:with-param>
 </xsl:call-template>
 
 <xsl:call-template name="gradient">
   <xsl:with-param name="gid">c</xsl:with-param>
   <xsl:with-param name="start">gray</xsl:with-param>
   <xsl:with-param name="end">black</xsl:with-param>
   <xsl:with-param name="r">16</xsl:with-param>
 </xsl:call-template> 
 
 <xsl:call-template name="gradient">
   <xsl:with-param name="gid">h</xsl:with-param>
   <xsl:with-param name="start">orange</xsl:with-param>
   <xsl:with-param name="end">yellow</xsl:with-param>
   <xsl:with-param name="r">10</xsl:with-param>
 </xsl:call-template>
 
  </xsl:element><!-- defs -->
 
 <xsl:comment>min x <xsl:value-of select="$min-x"/> , <xsl:value-of select="$max-x"/></xsl:comment>
 
  <xsl:element name="svg:g">
	<xsl:call-template name="xyz">
		<xsl:with-param name="index" select="1"/>
	</xsl:call-template>
  </xsl:element><!-- g -->


 </xsl:element><!-- svg -->
</xsl:template>

<xsl:template name="xyz">
<xsl:param name="index" select="1"/>
  <xsl:if test="$index &lt;= $count">
  	<xsl:variable name="s"><xsl:value-of select="$a_array/c:PC-Element[$index]/@value"/></xsl:variable>
  	<xsl:variable name="x"><xsl:value-of select="$x_array/c:PC-Conformer_x_E[$index]"/></xsl:variable>
  	<xsl:variable name="y"><xsl:value-of select="$y_array/c:PC-Conformer_y_E[$index]"/></xsl:variable>
  	<xsl:variable name="z"><xsl:value-of select="$z_array/c:PC-Conformer_z_E[$index]"/></xsl:variable>
  	
  	<xsl:element name="svg:use">
  	  <xsl:attribute name="xlink:href">#atom<xsl:value-of select="$s"/></xsl:attribute>
  	  <xsl:attribute name="x"><xsl:value-of select="$x * $scale +100"/></xsl:attribute>
  	  <xsl:attribute name="y"><xsl:value-of select="$z * $scale +100"/></xsl:attribute>
  	</xsl:element>
  	
   	<xsl:call-template name="xyz">
 		<xsl:with-param name="index" select="1 + $index"/>
 	</xsl:call-template>
  </xsl:if>
</xsl:template>


<xsl:template name="gradient">
<xsl:param name="gid" select="did"/>
<xsl:param name="start" select="rgb(200,200,200)"/>
<xsl:param name="end" select="rgb(0,0,255)"/>
<xsl:param name="r" select="0"/>
<xsl:element name="svg:radialGradient">
  <xsl:attribute name="id">radial<xsl:value-of select="$gid"/></xsl:attribute>
  <xsl:attribute name="cx">50%</xsl:attribute>
  <xsl:attribute name="cy">50%</xsl:attribute>
  <xsl:attribute name="r">50%</xsl:attribute>
  <xsl:attribute name="fx">50%</xsl:attribute>
  <xsl:attribute name="fy">50%</xsl:attribute>
  <xsl:element name="svg:stop">
  	<xsl:attribute name="offset">0%</xsl:attribute>
  	<xsl:attribute name="style">stop-color:<xsl:value-of select="$start"/>;stop-opacity:0;</xsl:attribute>
  </xsl:element>
  <xsl:element name="svg:stop">
  	<xsl:attribute name="offset">100%</xsl:attribute>
  	<xsl:attribute name="style">stop-color:<xsl:value-of select="$end"/>;stop-opacity:1;</xsl:attribute>
  </xsl:element>  
</xsl:element>


<xsl:element name="svg:circle">
	<xsl:attribute name="id">atom<xsl:value-of select="$gid"/></xsl:attribute>
	<xsl:attribute name="r"><xsl:value-of select="$r"/></xsl:attribute>
	<xsl:attribute name="style">stroke:black;fill:url(#radial<xsl:value-of select="$gid"/>);</xsl:attribute>
</xsl:element>

</xsl:template>



</xsl:stylesheet>