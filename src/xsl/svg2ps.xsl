<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY blank "&#032;">
]>
<xsl:stylesheet
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 >
<!--
Motivation:
	This stylesheet transforms a *SIMPLE* SVG file  to postscript
Author:
	Pierre Lindenbaum PhD plindenbaum@yahoo.fr
-->


<xsl:output method='text'/>

<xsl:template match="/"><xsl:text>%!
/Times-Roman findfont
12 scalefont
setfont
</xsl:text>
<xsl:apply-templates select="svg:svg"/>
&blank;showpage
</xsl:template>

<xsl:template match="svg:svg">
<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="svg:line">
<xsl:variable name="shape"><xsl:value-of select="@x1"/><xsl:text> </xsl:text><xsl:value-of select="@y1"/>&blank;moveto&blank;<xsl:value-of select="@x2"/><xsl:text> </xsl:text><xsl:value-of select="@y2"/>&blank;lineto</xsl:variable>
<xsl:value-of select="$shape"/><xsl:text> stroke
</xsl:text>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="svg:rect">
<xsl:variable name="shape">newpath
<xsl:value-of select="@x"/><xsl:text> </xsl:text><xsl:value-of select="@y"/>&blank;moveto
<xsl:value-of select="@x+@width"/><xsl:text> </xsl:text><xsl:value-of select="@y"/>&blank;lineto
<xsl:value-of select="@x+@width"/><xsl:text> </xsl:text><xsl:value-of select="@y+@height"/>&blank;lineto
<xsl:value-of select="@x"/><xsl:text> </xsl:text><xsl:value-of select="@y+@height"/>&blank;lineto
closepath</xsl:variable>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="svg:circle">
<xsl:variable name="shape">newpath
<xsl:value-of select="@cx"/><xsl:text> </xsl:text>
<xsl:value-of select="@cy"/><xsl:text> </xsl:text>
<xsl:value-of select="@r"/><xsl:text> 0 360 arc </xsl:text>
</xsl:variable>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>


<xsl:template match="svg:text">
<xsl:variable name="shape">newpath
<xsl:value-of select="@x"/><xsl:text> </xsl:text>
<xsl:value-of select="@y"/><xsl:text> moveto (</xsl:text>
<xsl:value-of select="text()"/>
<xsl:text>) show </xsl:text>
</xsl:variable>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>


<xsl:template match="svg:polyline[@points]">
<xsl:variable name="shape">
<xsl:call-template name="split-path">
  <xsl:with-param name="points" select="normalize-space(@points)"/>
  <xsl:with-param name="join">moveto</xsl:with-param>
</xsl:call-template>
</xsl:variable>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="svg:polygon[@points]">
<xsl:variable name="shape">
<xsl:call-template name="split-path">
  <xsl:with-param name="points" select="normalize-space(@points)"/>
  <xsl:with-param name="join">moveto</xsl:with-param>
</xsl:call-template>
<xsl:text> closepath</xsl:text>
</xsl:variable>
<xsl:call-template name="stroke-and-fill">
	<xsl:with-param  name="node" select="."/>
	<xsl:with-param  name="shape" select="$shape"/>
</xsl:call-template>
</xsl:template>



<xsl:template match="svg:title|svg:defs">
<!-- ignore -->
</xsl:template>

<xsl:template match="svg:g">
<xsl:apply-templates  select="*"/>
</xsl:template>

<xsl:template match="*">
<xsl:message terminate="no">Element <xsl:value-of select="name(.)"/> not handled by this stylesheet</xsl:message>
<xsl:apply-templates  select="*"/>
</xsl:template>




<xsl:template name="split-path">
<xsl:param name="points"/>
<xsl:param name="join"/>
<xsl:if test="string-length(normalize-space($points)) &gt; 0">
<xsl:variable name="pt0">
<xsl:choose>
  <xsl:when test="substring-before(normalize-space($points),',')">
    <xsl:value-of select="substring-before(normalize-space($points),',')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="normalize-space($points)"/>
  </xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:variable name="x0" select="normalize-space(substring-before($pt0,' '))"/>
<xsl:variable name="xy" select="normalize-space(substring-after($pt0,' '))"/>

<xsl:value-of select="$x0"/>
<xsl:text> </xsl:text>
<xsl:value-of select="$xy"/>
<xsl:text> </xsl:text>
<xsl:value-of select="$join"/>
<xsl:text> </xsl:text>
<xsl:call-template name="split-path">
  <xsl:with-param name="points" select="substring-after(normalize-space($points),',')"/>
  <xsl:with-param name="join">lineto</xsl:with-param>
</xsl:call-template>
</xsl:if>
</xsl:template>


<xsl:template name="stroke-and-fill">
<xsl:param name="node"/>
<xsl:param name="shape"/>

<!-- fill -->
<xsl:if test="$node/@fill!=&apos;none&apos;">
  <xsl:call-template name="color-to-ps">
    <xsl:with-param name="css" select="normalize-space($node/@fill)"/>
  </xsl:call-template>
  <xsl:text> setrgbcolor </xsl:text>
  <xsl:value-of select="$shape"/>
  <xsl:text> fill </xsl:text>
</xsl:if>

<!-- stroke -->
<xsl:if test="$node/@stroke!=&apos;none&apos;">
<xsl:choose>
  <xsl:when test="$node/@stoke-width">
   	<xsl:text> </xsl:text><xsl:value-of select="$node/@stoke-width"/><xsl:text> setlinewidth</xsl:text>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text> 1 setlinewidth </xsl:text>
  </xsl:otherwise>
</xsl:choose>

<xsl:call-template name="color-to-ps">
  <xsl:with-param name="css" select="normalize-space($node/@stroke)"/>
</xsl:call-template>
<xsl:text> setrgbcolor </xsl:text>
<xsl:value-of select="$shape"/>
<xsl:text> stroke </xsl:text>
</xsl:if>

</xsl:template>


<!-- cat jeter.txt | gawk '{split($2,a,"[,]"); printf("<xsl:when test=\"$css=&apos;%s&apos;\">%f %f %f</xsl:when>\n",$1,a[1]/255.0,a[2]/255.0,a[3]/255.0);}' -->
<xsl:template name="color-to-ps">
<xsl:param name="css"/>

<xsl:if test="$css!=&apos;none&apos; and $css!=&apos;&apos;">
<xsl:choose>
<xsl:when test="starts-with($css,&apos;rgb(&apos;)">
<xsl:variable name="red" select="substring-after(substring-before($css,&apos;,&apos;),&apos;(&apos;)"/>
<xsl:variable name="right1" select="substring-after($css,&apos;,&apos;)"/>
<xsl:variable name="green" select="substring-before($right1,&apos;,&apos;)"/>
<xsl:variable name="right2" select="substring-after($right1,&apos;,&apos;)"/>
<xsl:variable name="blue" select="substring-before($right2,&apos;)&apos;)"/>
<xsl:value-of select="number($red) div 255.0"/>
<xsl:text> </xsl:text>
<xsl:value-of select="number($green) div 255.0"/>
<xsl:text> </xsl:text>
<xsl:value-of select="number($blue) div 255.0"/>
</xsl:when>
<xsl:when test="$css=&apos;aliceblue&apos;"><xsl:text>0.941176 0.972549 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;antiquewhite&apos;"><xsl:text>0.980392 0.921569 0.843137</xsl:text></xsl:when>
<xsl:when test="$css=&apos;aqua&apos;"><xsl:text>0.000000 1.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;aquamarine&apos;"><xsl:text>0.498039 1.000000 0.831373</xsl:text></xsl:when>
<xsl:when test="$css=&apos;azure&apos;"><xsl:text>0.941176 1.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;beige&apos;"><xsl:text>0.960784 0.960784 0.862745</xsl:text></xsl:when>
<xsl:when test="$css=&apos;bisque&apos;"><xsl:text>1.000000 0.894118 0.768627</xsl:text></xsl:when>
<xsl:when test="$css=&apos;black&apos;"><xsl:text>0.000000 0.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;blanchedalmond&apos;"><xsl:text>1.000000 0.921569 0.803922</xsl:text></xsl:when>
<xsl:when test="$css=&apos;blue&apos;"><xsl:text>0.000000 0.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;blueviolet&apos;"><xsl:text>0.541176 0.168627 0.886275</xsl:text></xsl:when>
<xsl:when test="$css=&apos;brown&apos;"><xsl:text>0.647059 0.164706 0.164706</xsl:text></xsl:when>
<xsl:when test="$css=&apos;burlywood&apos;"><xsl:text>0.870588 0.721569 0.529412</xsl:text></xsl:when>
<xsl:when test="$css=&apos;cadetblue&apos;"><xsl:text>0.372549 0.619608 0.627451</xsl:text></xsl:when>
<xsl:when test="$css=&apos;chartreuse&apos;"><xsl:text>0.498039 1.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;chocolate&apos;"><xsl:text>0.823529 0.411765 0.117647</xsl:text></xsl:when>
<xsl:when test="$css=&apos;coral&apos;"><xsl:text>1.000000 0.498039 0.313725</xsl:text></xsl:when>
<xsl:when test="$css=&apos;cornflowerblue&apos;"><xsl:text>0.392157 0.584314 0.929412</xsl:text></xsl:when>
<xsl:when test="$css=&apos;cornsilk&apos;"><xsl:text>1.000000 0.972549 0.862745</xsl:text></xsl:when>
<xsl:when test="$css=&apos;crimson&apos;"><xsl:text>0.862745 0.078431 0.235294</xsl:text></xsl:when>
<xsl:when test="$css=&apos;cyan&apos;"><xsl:text>0.000000 1.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkblue&apos;"><xsl:text>0.000000 0.000000 0.545098</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkcyan&apos;"><xsl:text>0.000000 0.545098 0.545098</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkgoldenrod&apos;"><xsl:text>0.721569 0.525490 0.043137</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkgray&apos;"><xsl:text>0.662745 0.662745 0.662745</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkgreen&apos;"><xsl:text>0.000000 0.392157 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkgrey&apos;"><xsl:text>0.662745 0.662745 0.662745</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkkhaki&apos;"><xsl:text>0.741176 0.717647 0.419608</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkmagenta&apos;"><xsl:text>0.545098 0.000000 0.545098</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkolivegreen&apos;"><xsl:text>0.333333 0.419608 0.184314</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkorange&apos;"><xsl:text>1.000000 0.549020 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkorchid&apos;"><xsl:text>0.600000 0.196078 0.800000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkred&apos;"><xsl:text>0.545098 0.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darksalmon&apos;"><xsl:text>0.913725 0.588235 0.478431</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkseagreen&apos;"><xsl:text>0.560784 0.737255 0.560784</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkslateblue&apos;"><xsl:text>0.282353 0.239216 0.545098</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkslategray&apos;"><xsl:text>0.184314 0.309804 0.309804</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkslategrey&apos;"><xsl:text>0.184314 0.309804 0.309804</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkturquoise&apos;"><xsl:text>0.000000 0.807843 0.819608</xsl:text></xsl:when>
<xsl:when test="$css=&apos;darkviolet&apos;"><xsl:text>0.580392 0.000000 0.827451</xsl:text></xsl:when>
<xsl:when test="$css=&apos;deeppink&apos;"><xsl:text>1.000000 0.078431 0.576471</xsl:text></xsl:when>
<xsl:when test="$css=&apos;deepskyblue&apos;"><xsl:text>0.000000 0.749020 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;dimgray&apos;"><xsl:text>0.411765 0.411765 0.411765</xsl:text></xsl:when>
<xsl:when test="$css=&apos;dimgrey&apos;"><xsl:text>0.411765 0.411765 0.411765</xsl:text></xsl:when>
<xsl:when test="$css=&apos;dodgerblue&apos;"><xsl:text>0.117647 0.564706 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;firebrick&apos;"><xsl:text>0.698039 0.133333 0.133333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;floralwhite&apos;"><xsl:text>1.000000 0.980392 0.941176</xsl:text></xsl:when>
<xsl:when test="$css=&apos;forestgreen&apos;"><xsl:text>0.133333 0.545098 0.133333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;fuchsia&apos;"><xsl:text>1.000000 0.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;gainsboro&apos;"><xsl:text>0.862745 0.862745 0.862745</xsl:text></xsl:when>
<xsl:when test="$css=&apos;ghostwhite&apos;"><xsl:text>0.972549 0.972549 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;gold&apos;"><xsl:text>1.000000 0.843137 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;goldenrod&apos;"><xsl:text>0.854902 0.647059 0.125490</xsl:text></xsl:when>
<xsl:when test="$css=&apos;gray&apos;"><xsl:text>0.501961 0.501961 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;green&apos;"><xsl:text>0.000000 0.501961 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;greenyellow&apos;"><xsl:text>0.678431 1.000000 0.184314</xsl:text></xsl:when>
<xsl:when test="$css=&apos;grey&apos;"><xsl:text>0.501961 0.501961 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;honeydew&apos;"><xsl:text>0.941176 1.000000 0.941176</xsl:text></xsl:when>
<xsl:when test="$css=&apos;hotpink&apos;"><xsl:text>1.000000 0.411765 0.705882</xsl:text></xsl:when>
<xsl:when test="$css=&apos;indianred&apos;"><xsl:text>0.803922 0.360784 0.360784</xsl:text></xsl:when>
<xsl:when test="$css=&apos;indigo&apos;"><xsl:text>0.294118 0.000000 0.509804</xsl:text></xsl:when>
<xsl:when test="$css=&apos;ivory&apos;"><xsl:text>1.000000 1.000000 0.941176</xsl:text></xsl:when>
<xsl:when test="$css=&apos;khaki&apos;"><xsl:text>0.941176 0.901961 0.549020</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lavender&apos;"><xsl:text>0.901961 0.901961 0.980392</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lavenderblush&apos;"><xsl:text>1.000000 0.941176 0.960784</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lawngreen&apos;"><xsl:text>0.486275 0.988235 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lemonchiffon&apos;"><xsl:text>1.000000 0.980392 0.803922</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightblue&apos;"><xsl:text>0.678431 0.847059 0.901961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightcoral&apos;"><xsl:text>0.941176 0.501961 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightcyan&apos;"><xsl:text>0.878431 1.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightgoldenrodyellow&apos;"><xsl:text>0.980392 0.980392 0.823529</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightgray&apos;"><xsl:text>0.827451 0.827451 0.827451</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightgreen&apos;"><xsl:text>0.564706 0.933333 0.564706</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightgrey&apos;"><xsl:text>0.827451 0.827451 0.827451</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightpink&apos;"><xsl:text>1.000000 0.713725 0.756863</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightsalmon&apos;"><xsl:text>1.000000 0.627451 0.478431</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightseagreen&apos;"><xsl:text>0.125490 0.698039 0.666667</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightskyblue&apos;"><xsl:text>0.529412 0.807843 0.980392</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightslategray&apos;"><xsl:text>0.466667 0.533333 0.600000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightslategrey&apos;"><xsl:text>0.466667 0.533333 0.600000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightsteelblue&apos;"><xsl:text>0.690196 0.768627 0.870588</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lightyellow&apos;"><xsl:text>1.000000 1.000000 0.878431</xsl:text></xsl:when>
<xsl:when test="$css=&apos;lime&apos;"><xsl:text>0.000000 1.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;limegreen&apos;"><xsl:text>0.196078 0.803922 0.196078</xsl:text></xsl:when>
<xsl:when test="$css=&apos;linen&apos;"><xsl:text>0.980392 0.941176 0.901961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;magenta&apos;"><xsl:text>1.000000 0.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;maroon&apos;"><xsl:text>0.501961 0.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumaquamarine&apos;"><xsl:text>0.400000 0.803922 0.666667</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumblue&apos;"><xsl:text>0.000000 0.000000 0.803922</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumorchid&apos;"><xsl:text>0.729412 0.333333 0.827451</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumpurple&apos;"><xsl:text>0.576471 0.439216 0.858824</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumseagreen&apos;"><xsl:text>0.235294 0.701961 0.443137</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumslateblue&apos;"><xsl:text>0.482353 0.407843 0.933333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumspringgreen&apos;"><xsl:text>0.000000 0.980392 0.603922</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumturquoise&apos;"><xsl:text>0.282353 0.819608 0.800000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mediumvioletred&apos;"><xsl:text>0.780392 0.082353 0.521569</xsl:text></xsl:when>
<xsl:when test="$css=&apos;midnightblue&apos;"><xsl:text>0.098039 0.098039 0.439216</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mintcream&apos;"><xsl:text>0.960784 1.000000 0.980392</xsl:text></xsl:when>
<xsl:when test="$css=&apos;mistyrose&apos;"><xsl:text>1.000000 0.894118 0.882353</xsl:text></xsl:when>
<xsl:when test="$css=&apos;moccasin&apos;"><xsl:text>1.000000 0.894118 0.709804</xsl:text></xsl:when>
<xsl:when test="$css=&apos;navajowhite&apos;"><xsl:text>1.000000 0.870588 0.678431</xsl:text></xsl:when>
<xsl:when test="$css=&apos;navy&apos;"><xsl:text>0.000000 0.000000 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;oldlace&apos;"><xsl:text>0.992157 0.960784 0.901961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;olive&apos;"><xsl:text>0.501961 0.501961 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;olivedrab&apos;"><xsl:text>0.419608 0.556863 0.137255</xsl:text></xsl:when>
<xsl:when test="$css=&apos;orange&apos;"><xsl:text>1.000000 0.647059 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;orangered&apos;"><xsl:text>1.000000 0.270588 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;orchid&apos;"><xsl:text>0.854902 0.439216 0.839216</xsl:text></xsl:when>
<xsl:when test="$css=&apos;palegoldenrod&apos;"><xsl:text>0.933333 0.909804 0.666667</xsl:text></xsl:when>
<xsl:when test="$css=&apos;palegreen&apos;"><xsl:text>0.596078 0.984314 0.596078</xsl:text></xsl:when>
<xsl:when test="$css=&apos;paleturquoise&apos;"><xsl:text>0.686275 0.933333 0.933333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;palevioletred&apos;"><xsl:text>0.858824 0.439216 0.576471</xsl:text></xsl:when>
<xsl:when test="$css=&apos;papayawhip&apos;"><xsl:text>1.000000 0.937255 0.835294</xsl:text></xsl:when>
<xsl:when test="$css=&apos;peachpuff&apos;"><xsl:text>1.000000 0.854902 0.725490</xsl:text></xsl:when>
<xsl:when test="$css=&apos;peru&apos;"><xsl:text>0.803922 0.521569 0.247059</xsl:text></xsl:when>
<xsl:when test="$css=&apos;pink&apos;"><xsl:text>1.000000 0.752941 0.796078</xsl:text></xsl:when>
<xsl:when test="$css=&apos;plum&apos;"><xsl:text>0.866667 0.627451 0.866667</xsl:text></xsl:when>
<xsl:when test="$css=&apos;powderblue&apos;"><xsl:text>0.690196 0.878431 0.901961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;purple&apos;"><xsl:text>0.501961 0.000000 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;red&apos;"><xsl:text>1.000000 0.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;rosybrown&apos;"><xsl:text>0.737255 0.560784 0.560784</xsl:text></xsl:when>
<xsl:when test="$css=&apos;royalblue&apos;"><xsl:text>0.254902 0.411765 0.882353</xsl:text></xsl:when>
<xsl:when test="$css=&apos;saddlebrown&apos;"><xsl:text>0.545098 0.270588 0.074510</xsl:text></xsl:when>
<xsl:when test="$css=&apos;salmon&apos;"><xsl:text>0.980392 0.501961 0.447059</xsl:text></xsl:when>
<xsl:when test="$css=&apos;sandybrown&apos;"><xsl:text>0.956863 0.643137 0.376471</xsl:text></xsl:when>
<xsl:when test="$css=&apos;seagreen&apos;"><xsl:text>0.180392 0.545098 0.341176</xsl:text></xsl:when>
<xsl:when test="$css=&apos;seashell&apos;"><xsl:text>1.000000 0.960784 0.933333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;sienna&apos;"><xsl:text>0.627451 0.321569 0.176471</xsl:text></xsl:when>
<xsl:when test="$css=&apos;silver&apos;"><xsl:text>0.752941 0.752941 0.752941</xsl:text></xsl:when>
<xsl:when test="$css=&apos;skyblue&apos;"><xsl:text>0.529412 0.807843 0.921569</xsl:text></xsl:when>
<xsl:when test="$css=&apos;slateblue&apos;"><xsl:text>0.415686 0.352941 0.803922</xsl:text></xsl:when>
<xsl:when test="$css=&apos;slategray&apos;"><xsl:text>0.439216 0.501961 0.564706</xsl:text></xsl:when>
<xsl:when test="$css=&apos;slategrey&apos;"><xsl:text>0.439216 0.501961 0.564706</xsl:text></xsl:when>
<xsl:when test="$css=&apos;snow&apos;"><xsl:text>1.000000 0.980392 0.980392</xsl:text></xsl:when>
<xsl:when test="$css=&apos;springgreen&apos;"><xsl:text>0.000000 1.000000 0.498039</xsl:text></xsl:when>
<xsl:when test="$css=&apos;steelblue&apos;"><xsl:text>0.274510 0.509804 0.705882</xsl:text></xsl:when>
<xsl:when test="$css=&apos;tan&apos;"><xsl:text>0.823529 0.705882 0.549020</xsl:text></xsl:when>
<xsl:when test="$css=&apos;teal&apos;"><xsl:text>0.000000 0.501961 0.501961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;thistle&apos;"><xsl:text>0.847059 0.749020 0.847059</xsl:text></xsl:when>
<xsl:when test="$css=&apos;tomato&apos;"><xsl:text>1.000000 0.388235 0.278431</xsl:text></xsl:when>
<xsl:when test="$css=&apos;turquoise&apos;"><xsl:text>0.250980 0.878431 0.815686</xsl:text></xsl:when>
<xsl:when test="$css=&apos;violet&apos;"><xsl:text>0.933333 0.509804 0.933333</xsl:text></xsl:when>
<xsl:when test="$css=&apos;wheat&apos;"><xsl:text>0.960784 0.870588 0.701961</xsl:text></xsl:when>
<xsl:when test="$css=&apos;white&apos;"><xsl:text>1.000000 1.000000 1.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;whitesmoke&apos;"><xsl:text>0.960784 0.960784 0.960784</xsl:text></xsl:when>
<xsl:when test="$css=&apos;yellow&apos;"><xsl:text>1.000000 1.000000 0.000000</xsl:text></xsl:when>
<xsl:when test="$css=&apos;yellowgreen&apos;"><xsl:text>0.603922 0.803922 0.196078</xsl:text></xsl:when>
<xsl:otherwise><xsl:text>0.000000 0.000000 0.000000</xsl:text></xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:template>

</xsl:stylesheet>