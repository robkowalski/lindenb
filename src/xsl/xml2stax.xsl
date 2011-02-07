<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns="http://www.w3.org/1999/xhtml"
	version='1.0'
	>
<!--

Author:
	Pierre Lindenbaum PhD
	plindenbaum@yahoo.fr

Motivation:
	This stylesheet transform xml/dom  to StAX (Streaming Java API for XML) statements

-->
<xsl:output method="text" />
<xsl:variable name="datadoc">true</xsl:variable>
<xsl:variable name="NodeType">var</xsl:variable>

<xsl:template match="/">
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XML2StAX
{
public static void main(String args[])
	{
	try
		{
		XMLOutputFactory factory= XMLOutputFactory.newInstance();
		XMLStreamWriter w= factory.createXMLStreamWriter(out);
		w.writeStartDocument("UTF-8","1.0");
		<xsl:apply-templates/>
		w.writeEndDocument();
		w.flush();
		}
	catch(Exception err)
		{
		err.printStackTrace();
		}
	}
}</xsl:template>

<xsl:template match="node()">
<xsl:variable name="ns">
   <xsl:call-template name="escape">
      <xsl:with-param name="s">
         <xsl:value-of select="namespace-uri(.)"/>
      </xsl:with-param>
   </xsl:call-template>
</xsl:variable>


<xsl:choose>
 <xsl:when test="count(child::node())=0">
   <xsl:choose>
   	<xsl:when test="string-length($ns)&gt;0">
   	  <xsl:text>w.writeEmptyElement("</xsl:text>
   	  <xsl:value-of select="substring-before(name(.),':')"/>
   	  <xsl:text>","</xsl:text>
   	  <xsl:value-of select="$ns"/>
   	  <xsl:text>","</xsl:text>
   	  <xsl:value-of select="local-name(.)"/>
   	  <xsl:text>");
</xsl:text>
   	</xsl:when>
   	<xsl:otherwise>
   	  <xsl:text>w.writeEmptyElement("</xsl:text>
   	  <xsl:value-of select="name(.)"/>
   	  <xsl:text>");
</xsl:text>
   	</xsl:otherwise>
   </xsl:choose>
   <xsl:call-template name="atts"><xsl:with-param name="node" select="."/></xsl:call-template>
</xsl:when>
<xsl:otherwise>
   <xsl:choose>
   	<xsl:when test="string-length($ns)&gt;0">
   	 <xsl:text> w.writeStartElement("</xsl:text>
   	 <xsl:value-of select="substring-before(name(.),':')"/>
   	 <xsl:text>","</xsl:text>
   	 <xsl:value-of select="local-name(.)"/>
   	 <xsl:text>","</xsl:text>
   	 <xsl:value-of select="$ns"/>
   	 <xsl:text>");
</xsl:text>
   	</xsl:when>
   	<xsl:otherwise>
   	  <xsl:text>w.writeStartElement("</xsl:text>
   	  <xsl:value-of select="name(.)"/>
   	  <xsl:text>");
</xsl:text>
   	</xsl:otherwise>
   </xsl:choose>
   <xsl:call-template name="atts"><xsl:with-param name="node" select="."/></xsl:call-template>
   <xsl:apply-templates select="*|text()"/>
   <xsl:text> w.writeEndElement();//</xsl:text><xsl:value-of select="name(.)"/>
   <xsl:text>
   </xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template name="atts">
<xsl:param name="node"/>
<xsl:for-each select="$node/@*">
<xsl:text>w.</xsl:text>
<xsl:choose>
   <xsl:when test="namespace-uri(.)=''">
      <xsl:text>writeAttribute("</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:text>","</xsl:text>
      <xsl:call-template name="escape">
         <xsl:with-param name="s">
            <xsl:value-of select="."/>
         </xsl:with-param>
      </xsl:call-template>
      <xsl:text>");
</xsl:text>
   </xsl:when>
   <xsl:otherwise>
      <xsl:text>writeAttribute("</xsl:text>
      <xsl:value-of select="substring-before(name(.),':')"/>
      <xsl:text>",</xsl:text>
      <xsl:call-template name="namespace">
         <xsl:with-param name="uri">
            <xsl:value-of select="namespace-uri(.)"/>
               </xsl:with-param>
            </xsl:call-template>
            <xsl:text>,"</xsl:text>
            <xsl:value-of select="local-name(.)"/>
            <xsl:text>","</xsl:text>
            <xsl:call-template name="escape">
               <xsl:with-param name="s">
                  <xsl:value-of select="."/>
               </xsl:with-param>
            </xsl:call-template>
            <xsl:text>");
</xsl:text>
            </xsl:otherwise>
</xsl:choose><xsl:text>
</xsl:text>
</xsl:for-each>
</xsl:template>

<xsl:template match="text()">
<xsl:if test="string-length(normalize-space(.))&gt;0">
<xsl:text>w.writeCharacters("</xsl:text>
<xsl:call-template name="escape">
   <xsl:with-param name="s">
      <xsl:value-of select="."/>
   </xsl:with-param>
</xsl:call-template>
<xsl:text>");
</xsl:text>
</xsl:if>
</xsl:template>


<xsl:template name="namespace">
<xsl:param name="uri"/>
<xsl:choose>
	<xsl:when test="$uri='http://www.w3.org/1999/XSL/Transform'">XSL.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/xhtml'">HTML.NS</xsl:when>
	<xsl:when test="$uri='http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul'">XUL.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/2000/svg'">SVG.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/xlink'">XLINK.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/1999/02/22-rdf-syntax-ns#'">RDF.NS</xsl:when>
	<xsl:when test="$uri='http://www.w3.org/2000/01/rdf-schema#'">RDFS.NS</xsl:when>
	<xsl:otherwise>"<xsl:value-of select="$uri"/>"</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="escape">
<xsl:param name="s"/><xsl:variable name="c"><xsl:value-of select="substring($s,1,1)"/></xsl:variable>
<xsl:choose>
 <xsl:when test="$c='&#xA;'">\n</xsl:when>
 <xsl:when test='$c="&#39;"'>\'</xsl:when>
 <xsl:when test="$c='&#34;'">\"</xsl:when>
 <xsl:otherwise><xsl:value-of select="$c"/></xsl:otherwise>
</xsl:choose><xsl:if test="string-length($s) &gt;1"><xsl:call-template name="escape">
<xsl:with-param name="s"><xsl:value-of select="substring($s,2,string-length($s)-1)"/></xsl:with-param>
</xsl:call-template></xsl:if>
</xsl:template>


</xsl:stylesheet>

