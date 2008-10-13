<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:dia="http://www.lysator.liu.se/~alla/dia/"
	version='1.0'
	>

<!--

This stylesheet transforms the output of 'desc table *' in mysql+XML
into an di un-gzipped xml dia file

-->
<xsl:output method="xml" version="1.0" encoding="UTF-8"
     	indent="yes"
	/>

<!-- ================ DOCUMENT ROOT ========================================================== -->
<xsl:template match="/">
<dia:diagram>
<xsl:comment>
Generated with sql2dia.xsl
Author: Pierre Lindenbaum PhD.
plindenbaum@yahoo.fr 
</xsl:comment>
  <dia:diagramdata>
    <dia:attribute name="background">
      <dia:color val="#ffffff"/>
    </dia:attribute>
    <dia:attribute name="pagebreak">
      <dia:color val="#000099"/>
    </dia:attribute>
    <dia:attribute name="paper">
      <dia:composite type="paper">
        <dia:attribute name="name">
          <dia:string>#A4#</dia:string>
        </dia:attribute>
        <dia:attribute name="tmargin">
          <dia:real val="2.8222000598907471"/>
        </dia:attribute>
        <dia:attribute name="bmargin">
          <dia:real val="2.8222000598907471"/>
        </dia:attribute>
        <dia:attribute name="lmargin">
          <dia:real val="2.8222000598907471"/>
        </dia:attribute>
        <dia:attribute name="rmargin">
          <dia:real val="2.8222000598907471"/>
        </dia:attribute>
        <dia:attribute name="is_portrait">
          <dia:boolean val="true"/>
        </dia:attribute>
        <dia:attribute name="scaling">
          <dia:real val="1"/>
        </dia:attribute>
        <dia:attribute name="fitto">
          <dia:boolean val="false"/>
        </dia:attribute>
      </dia:composite>
    </dia:attribute>
    <dia:attribute name="grid">
      <dia:composite type="grid">
        <dia:attribute name="width_x">
          <dia:real val="1"/>
        </dia:attribute>
        <dia:attribute name="width_y">
          <dia:real val="1"/>
        </dia:attribute>
        <dia:attribute name="visible_x">
          <dia:int val="1"/>
        </dia:attribute>
        <dia:attribute name="visible_y">
          <dia:int val="1"/>
        </dia:attribute>
        <dia:composite type="color"/>
      </dia:composite>
    </dia:attribute>
    <dia:attribute name="color">
      <dia:color val="#d8e5e5"/>
    </dia:attribute>
    <dia:attribute name="guides">
      <dia:composite type="guides">
        <dia:attribute name="hguides"/>
        <dia:attribute name="vguides"/>
      </dia:composite>
    </dia:attribute>
  </dia:diagramdata>
  <dia:layer name="Background" visible="true">
   <xsl:apply-templates select="//resultset"/>
  </dia:layer>
</dia:diagram>
</xsl:template>
<!-- ================ RESULT SET ========================================================== -->

<xsl:template match="resultset">
<xsl:variable name="tableName" select="normalize-space(substring(@statement,5))"/>

    <dia:object type="UML - Class" version="0" id="O0">
	<xsl:variable name="width">5.5</xsl:variable>
	<xsl:variable name="height">3.0</xsl:variable>
	<xsl:variable name="x"><xsl:value-of select="number($width) * number( position() mod 5 )"/></xsl:variable>
	<xsl:variable name="y"><xsl:value-of select="number($height) * round( position() div 5 )"/></xsl:variable>
	


      <dia:attribute name="obj_pos">
	<xsl:element name="dia:point">
		<xsl:attribute name="val"><xsl:value-of select="$x"/>,<xsl:value-of select="$y"/></xsl:attribute>
	</xsl:element>        
      </dia:attribute>
      <dia:attribute name="obj_bb">
	<xsl:element name="dia:rectangle">
         <xsl:attribute name="val"><xsl:value-of select="$x"/>,<xsl:value-of select="$y"/>;<xsl:value-of select="$x + $width"/>,<xsl:value-of select="$y + $height"/></xsl:attribute>
        </xsl:element>
      </dia:attribute>
      <dia:attribute name="elem_corner">
       <xsl:element name="dia:point">
		<xsl:attribute name="val"><xsl:value-of select="$x"/>,<xsl:value-of select="$y"/></xsl:attribute>
	</xsl:element>   
      </dia:attribute>
      <dia:attribute name="elem_width">
	<xsl:element name="dia:real">
		<xsl:attribute name="val"><xsl:value-of select="$width"/></xsl:attribute>
	</xsl:element>  
      </dia:attribute>
      <dia:attribute name="elem_height">
	<xsl:element name="dia:real">
		<xsl:attribute name="val"><xsl:value-of select="$height"/></xsl:attribute>
	</xsl:element>  
      </dia:attribute>
      <dia:attribute name="name">
        <dia:string>#<xsl:value-of select="$tableName"/>#</dia:string>
      </dia:attribute>
      <dia:attribute name="stereotype">
        <dia:string>##</dia:string>
      </dia:attribute>
      <dia:attribute name="comment">
        <dia:string>#Table <xsl:value-of select="$tableName"/>#</dia:string>
      </dia:attribute>
      <dia:attribute name="abstract">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="suppress_attributes">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="suppress_operations">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="visible_attributes">
        <dia:boolean val="true"/>
      </dia:attribute>
      <dia:attribute name="visible_operations">
        <dia:boolean val="true"/>
      </dia:attribute>
      <dia:attribute name="visible_comments">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="wrap_operations">
        <dia:boolean val="true"/>
      </dia:attribute>
      <dia:attribute name="wrap_after_char">
        <dia:int val="40"/>
      </dia:attribute>
      <dia:attribute name="comment_line_length">
        <dia:int val="17"/>
      </dia:attribute>
      <dia:attribute name="comment_tagging">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="line_color">
        <dia:color val="#000000"/>
      </dia:attribute>
      <dia:attribute name="fill_color">
        <dia:color val="#ffffff"/>
      </dia:attribute>
      <dia:attribute name="text_color">
        <dia:color val="#000000"/>
      </dia:attribute>
      <dia:attribute name="normal_font">
        <dia:font family="monospace" style="0" name="Courier"/>
      </dia:attribute>
      <dia:attribute name="abstract_font">
        <dia:font family="monospace" style="88" name="Courier-BoldOblique"/>
      </dia:attribute>
      <dia:attribute name="polymorphic_font">
        <dia:font family="monospace" style="8" name="Courier-Oblique"/>
      </dia:attribute>
      <dia:attribute name="classname_font">
        <dia:font family="sans" style="80" name="Helvetica-Bold"/>
      </dia:attribute>
      <dia:attribute name="abstract_classname_font">
        <dia:font family="sans" style="88" name="Helvetica-BoldOblique"/>
      </dia:attribute>
      <dia:attribute name="comment_font">
        <dia:font family="sans" style="8" name="Helvetica-Oblique"/>
      </dia:attribute>
      <dia:attribute name="normal_font_height">
        <dia:real val="0.80000000000000004"/>
      </dia:attribute>
      <dia:attribute name="polymorphic_font_height">
        <dia:real val="0.80000000000000004"/>
      </dia:attribute>
      <dia:attribute name="abstract_font_height">
        <dia:real val="0.80000000000000004"/>
      </dia:attribute>
      <dia:attribute name="classname_font_height">
        <dia:real val="1"/>
      </dia:attribute>
      <dia:attribute name="abstract_classname_font_height">
        <dia:real val="1"/>
      </dia:attribute>
      <dia:attribute name="comment_font_height">
        <dia:real val="0.69999999999999996"/>
      </dia:attribute>
      <dia:attribute name="attributes">

      <xsl:for-each select="row">

        <dia:composite type="umlattribute">
          <dia:attribute name="name">
            <dia:string>#<xsl:value-of select="field[@name='Field']"/>#</dia:string>
          </dia:attribute>
          <dia:attribute name="type">
            <dia:string>#<xsl:value-of select="field[@name='Type']"/>#</dia:string>
          </dia:attribute>
          <dia:attribute name="value">
            <dia:string>#<xsl:value-of select="field[@name='Default']"/>#</dia:string>
          </dia:attribute>
          <dia:attribute name="comment">
            <dia:string>#<xsl:value-of select="field[@name='Field']"/>#</dia:string>
          </dia:attribute>
          <dia:attribute name="visibility">
            <dia:enum val="0"/>
          </dia:attribute>
          <dia:attribute name="abstract">
            <dia:boolean val="false"/>
          </dia:attribute>
          <dia:attribute name="class_scope">
            <dia:boolean val="false"/>
          </dia:attribute>
        </dia:composite>

      </xsl:for-each> 

      </dia:attribute>


      <dia:attribute name="operations"/>
      <dia:attribute name="template">
        <dia:boolean val="false"/>
      </dia:attribute>
      <dia:attribute name="templates"/>
    </dia:object>
   
</xsl:template>


</xsl:stylesheet>
