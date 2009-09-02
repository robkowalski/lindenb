<?xml version='1.0'  encoding="ISO-8859-1" ?>
<xsl:stylesheet
	xmlns="http://www.ncbi.nlm.nih.gov/SNP/docsum"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>



<xsl:output method="text"/>

<xsl:template match="/">
<xsl:apply-templates select="xsd:schema"/>
</xsl:template>

<xsl:template match="xsd:schema">
#include &lt;libxml/xmlreader.h&gt;



/** a simple container */
typedef struct State_t
	{
	xmlTextReaderPtr reader;
	} State,*StatePtr;
	

<xsl:for-each select="//xsd:element[xsd:complexType]">
 <xsl:sort select="@name"/>
 <xsl:variable name="tagName">
 <xsl:call-template name="tagName">
  <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:apply-templates select="xsd:annotation"/>
	static int process<xsl:value-of select="$tagName"/>(StatePtr state);

</xsl:for-each>




/** read the string content of a simple tag. return a xmlChar* that should be free with xmlFree */
static xmlChar* _readString(StatePtr state,const char* nodeName,int *errCode)
	{
	int success;
	int nodeType;
	xmlChar* value=NULL;
	*errCode=EXIT_SUCCESS;
	success= xmlTextReaderRead(state->reader);
	if(!success)
		{
		*errCode=EXIT_FAILURE;
		fprintf(stderr,"In %s  I/O Error.\n",nodeName);
		return NULL;
		}
	nodeType=xmlTextReaderNodeType(state->reader);
	switch(nodeType)
		{
		case XML_READER_TYPE_TEXT:
		case XML_READER_TYPE_WHITESPACE:
		case XML_READER_TYPE_CDATA:
		case XML_READER_TYPE_SIGNIFICANT_WHITESPACE:
			{
			value= xmlTextReaderValue(state->reader);
			if(value==NULL)
				{
				*errCode=EXIT_FAILURE;
				fprintf(stderr,"In %s  Cannot read text value.\n",nodeName);
				return NULL;
				}
			break;
			}
		default:
			*errCode=EXIT_FAILURE;
			fprintf(stderr,"In %s  Expected a text node but got %d.\n",nodeName,nodeType);
			return NULL;
		}
	
	success= xmlTextReaderRead(state->reader);
	if(!success)
		{
		*errCode=EXIT_FAILURE;
		xmlFree(value);
		fprintf(stderr,"In %s  I/O Error.\n",nodeName);
		return NULL;
		}
	nodeType=xmlTextReaderNodeType(state->reader);
	if(nodeType!=XML_READER_TYPE_END_ELEMENT)
		{
		*errCode=EXIT_FAILURE;
		xmlFree(value);
		fprintf(stderr,"In %s Expected a XML_READER_TYPE_END_ELEMENT but got %d.\n",nodeName,nodeType);
		return NULL;
		}
	#ifndef NDEBUG
	fprintf(stderr,"#value of %s is %s.\n",nodeName,value);
	#endif
	return value;
	}

<xsl:for-each select="//xsd:element[xsd:complexType]">
 <xsl:sort select="@name"/>
 <xsl:call-template name="element">
  <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:for-each>


int streamFile(char *filename)
   {
    xmlTextReaderPtr reader;
    int ret;

    reader = xmlReaderForFile(filename,NULL,XML_PARSE_NOBLANKS);
    if (reader != NULL) {
    	State state;
    	state.reader= reader;
        ret = xmlTextReaderRead(reader);
        
        while (ret == 1)
        	{
        	if(XML_READER_TYPE_ELEMENT==xmlTextReaderNodeType(reader) &amp;&amp;
        	xmlStrcmp(xmlTextReaderConstName(reader),BAD_CAST "ExchangeSet")==0
        	   )
        	   {
        	   State state;
        	   state.reader= reader;
        	   if(processExchangeSet(&amp;state)!=EXIT_SUCCESS)
        	   	{
        	   	fprintf(stderr,"Failure\n");
        	   	exit(EXIT_FAILURE);
        	   	}
        	   else
        	   	{
        	   	fprintf(stderr,"Success\n");
        	   	}
        	   }
            	ret = xmlTextReaderRead(reader);
        	}
        xmlFreeTextReader(reader);
        printf("Done.");
        if (ret != 0) {
            printf("%s : failed to parse\n", filename);
        }
    } else {
        printf("Unable to open %s\n", filename);
    }
}

int main(int argc,char** argv)
	{
	if(argc&lt;=1) return 0;
	streamFile(argv[1]);
	}

</xsl:template>

<xsl:template match="xsd:annotation">
<xsl:apply-templates select="xsd:documentation"/>
</xsl:template>

<xsl:template match="xsd:documentation">
<xsl:text>
	/** </xsl:text>
<xsl:value-of select="."/>
<xsl:text> */
</xsl:text>
</xsl:template>

<xsl:template name="element">
<xsl:param name="node"/>
<xsl:variable name="tagName">
 <xsl:call-template name="tagName">
  <xsl:with-param name="node" select="$node"/>
 </xsl:call-template>
</xsl:variable>
<xsl:apply-templates select="$node/xsd:annotation"/>
<!-- <xsl:if test="not($node/xsd:complexType/xsd:sequence)">
 <xsl:message terminate="no">unknown type <xsl:value-of select="$tagName"/></xsl:message>
</xsl:if> -->
static int process<xsl:value-of select="$tagName"/>(StatePtr state)
	{
	int returnValue=EXIT_SUCCESS;
	int success;
	int nodeType;
	//Attributes
	<xsl:for-each select="$node/xsd:complexType/xsd:attribute">
	<xsl:sort select="@name"/>
	<xsl:apply-templates select="xsd:annotation"/>
	xmlChar* <xsl:value-of select="@name"/>Attr=NULL;
	</xsl:for-each>
	//Count/Values Elements 
	<xsl:for-each select="$node/xsd:complexType/xsd:sequence/xsd:element">
	<xsl:sort select="@name"/>
	<xsl:variable name="childName">
	<xsl:call-template name="tagName">
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	</xsl:variable>
	int count<xsl:value-of select="$childName"/>=0;
	<xsl:if test="not(@ref or (@name and xsd:complexType))">
	xmlChar* value<xsl:value-of select="$childName"/>=NULL;
	</xsl:if>
	</xsl:for-each>
	
	#ifndef NDEBUG
	fprintf(stderr,"#Entering <xsl:value-of select="$tagName"/>\n");
	
	if(xmlTextReaderNodeType(state -> reader)!=XML_READER_TYPE_ELEMENT)
		{
		fprintf(stderr,"#[%d]Not a XML_READER_TYPE_ELEMENT but %d\n",__LINE__,xmlTextReaderNodeType(state -> reader));
		returnValue=EXIT_FAILURE;
		goto cleanup;
		}
	
	
	#endif
	
	//fill Attributes
	<xsl:for-each select="$node/xsd:complexType/xsd:attribute">
	<xsl:sort select="@name"/>
	<xsl:variable name="attName"><xsl:value-of select="@name"/></xsl:variable>
	<xsl:apply-templates select="xsd:annotation"/>
	<xsl:value-of select="$attName"/>Attr= xmlTextReaderGetAttribute(state->reader,BAD_CAST "<xsl:value-of select="@name"/>");
	if(<xsl:value-of select="$attName"/>Attr!=NULL)
		{
		<xsl:choose>
		  <xsl:when test="@type='xsd:int' or @type='xsd:integer'">
		  //check it is an integer
		  </xsl:when>
		   <xsl:when test="@type='xsd:boolean'">
		  //check it is a boolean
		  </xsl:when>
		   <xsl:when test="@type='xsd:float'">
		  //check it is a float
		  </xsl:when>
		  <xsl:when test="@type='xsd:double'">
		  //check it is a double
		  </xsl:when>
		  <xsl:when test="@type='xsd:string' or @type='' or not(@type)">
		  <xsl:if test="not(xsd:simpleType)">/* just a string, nothing special */</xsl:if>
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:message terminate="yes">unknown flag "<xsl:value-of select="@type"/>" </xsl:message>
		  </xsl:otherwise>
		</xsl:choose>
		
		<!-- attribute is an enumeration -->
		<xsl:for-each select="xsd:simpleType/xsd:restriction[@base='xsd:string']/xsd:enumeration">
		<xsl:if test="position()=1">
		/* attribute @<xsl:value-of select="$attName"/> is an enumeration, checking the value */
		</xsl:if>
		<xsl:if test="position()!=1">
		else </xsl:if> if( xmlStrcmp(
			<xsl:value-of select="$attName"/>Attr,
			BAD_CAST "<xsl:value-of select="@value"/>"
			)==0
			)
			{
			//process "<xsl:value-of select="@value"/>" enumeration
			}
		<xsl:if test="position()=last()"> else
			{
			fprintf(stderr,"Unknown enum value for @<xsl:value-of select="$attName"/> %s\n",<xsl:value-of select="$attName"/>Attr);
		  	returnValue =  EXIT_FAILURE;
		 	 goto cleanup;
			}</xsl:if>
				
		</xsl:for-each>
		
		}
	else
		{
		<xsl:choose>
		  <xsl:when test="@use='required'">
		  fprintf(stderr,"Error in <xsl:value-of select="$tagName"/> attribute @<xsl:value-of select="@name"/> missing");
		  returnValue =  EXIT_FAILURE;
		  goto cleanup;
		  </xsl:when>
		  <xsl:when test="@use='optional' or not(@use)">
		  /* optional ignore */
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:message terminate="yes">unknown flag <xsl:value-of select="@use"/></xsl:message>
		  </xsl:otherwise>
		</xsl:choose>
		}
	</xsl:for-each>
	
	
	<xsl:if test="count($node/xsd:complexType/xsd:sequence/xsd:element)=0">
	if(!xmlTextReaderIsEmptyElement(state -> reader))
		{
		fprintf(stderr,"Expected no element under <xsl:value-of select="$tagName"/>\n");
                returnValue =  EXIT_FAILURE;
		goto cleanup;
		}
	else
		{
		goto cleanup;
		}
	</xsl:if>
	
	success = xmlTextReaderRead( state -> reader );
        if(!success)
                {
                fprintf(stderr,"In <xsl:value-of select="$tagName"/>  I/O Error. xmlTextReaderRead returned \n");
                returnValue =  EXIT_FAILURE;
		goto cleanup;
                }
	nodeType = xmlTextReaderNodeType( state -> reader );
	#ifndef NDEBUG
	fprintf(stderr,"#[%d]Invoking <xsl:value-of select="$tagName"/> type=%d\n",__LINE__,nodeType);
	#endif
	
	
	
	
	<xsl:for-each select="$node/xsd:complexType/xsd:sequence/xsd:element">
	<!-- NO !! <xsl:sort select="@name"/> -->
	<xsl:variable name="childName">
	<xsl:call-template name="tagName">
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	</xsl:variable>
	/* process childNode &lt;<xsl:value-of select="$childName"/>/&gt; */
	<xsl:apply-templates select="xsd:annotation"/>
	while(nodeType == XML_READER_TYPE_ELEMENT)
		{
		#ifndef NDEBUG
		fprintf(stderr,"#[%d] Current Child is <xsl:value-of select="$tagName"/>/%s\n",
			__LINE__,
			xmlTextReaderConstName(state -> reader)
			);
		#endif
		
		if(xmlStrcmp(
			xmlTextReaderConstName(state -> reader),
			BAD_CAST <xsl:choose>
			<xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
			<xsl:when test="@ref">"<xsl:value-of select="@ref"/>"</xsl:when>
			<xsl:otherwise> BOOOM </xsl:otherwise>
			</xsl:choose>
			)!=0)
			{
			break;
			}
		
		++count<xsl:value-of select="$childName"/>;
		<xsl:if test="not(@maxOccurs) or @maxOccurs!='unbounded'">
		<xsl:variable name="max">
		  <xsl:choose>
		    <xsl:when test="@maxOccurs"><xsl:value-of select="@maxOccurs"/></xsl:when>
		    <xsl:otherwise>1</xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>
		if( count<xsl:value-of select="$childName"/> &gt; <xsl:value-of select="$max"/> )
			{
			fprintf(stderr,"Expected at most <xsl:value-of select="$max"/> &lt;<xsl:value-of select="$childName"/>&gt; under  &lt;<xsl:value-of select="$tagName"/>&gt; but found %d\n",count<xsl:value-of select="$childName"/>);
			returnValue =  EXIT_FAILURE;
			goto cleanup;
			}
		</xsl:if>
		
		<xsl:choose>
		  <!-- this element is 'callable by another method -->
		  <xsl:when test="@ref or (@name and xsd:complexType)">
		  <xsl:variable name="refId">
		  <xsl:choose>
		  	<xsl:when test="@ref"><xsl:value-of select="@ref"/></xsl:when>
		  	<xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
		  </xsl:choose>
		  </xsl:variable>
		  <xsl:variable name="refName">
			<xsl:call-template name="tagName">
			<xsl:with-param name="node" select="//xsd:element[@name=$refId]"/>
		   </xsl:call-template>
		   </xsl:variable>
		  if(process<xsl:value-of select="$refName"/>(state)!=EXIT_SUCCESS)
		  	{
		  	returnValue =  EXIT_FAILURE;
			goto cleanup;
		  	}
		  
		  </xsl:when>
		  
		  <xsl:when test="@name">
		  	/* read content of &lt;<xsl:value-of select="$childName"/>/&gt; */
		  	value<xsl:value-of select="$childName"/>= _readString(state,"<xsl:value-of select="$tagName"/>/<xsl:value-of select="$childName"/>",&amp;returnValue);
		  </xsl:when>
		  
		  
		</xsl:choose>
		
		/* read next event */
		success= xmlTextReaderRead(state->reader);
		if(!success)
			{
			returnValue =  EXIT_FAILURE;
			fprintf(stderr,"In  <xsl:value-of select="$tagName"/>/<xsl:value-of select="$childName"/> I/O Error.\n");
			goto cleanup;
			}
		nodeType=xmlTextReaderNodeType(state->reader);
		#ifndef NDEBUG
		fprintf(stderr,"#[%d]Invoking <xsl:value-of select="$tagName"/>/<xsl:value-of select="$childName"/> type=%d\n",__LINE__,nodeType);
		#endif
		}
	
	<xsl:if test="@minOccurs and @minOccurs!='0'">
	//check number of <xsl:value-of select="$childName"/> read
	  <xsl:variable name="min">
		<xsl:choose>
		<xsl:when test="@minOccurs"><xsl:value-of select="@minOccurs"/></xsl:when>
		<xsl:otherwise>1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	  
	  if( count<xsl:value-of select="$childName"/> &lt; <xsl:value-of select="$min"/> )
	  	{
	  	fprintf(stderr,"Expected at least <xsl:value-of select="$min"/> &lt;<xsl:value-of select="$childName"/>&gt; under  &lt;<xsl:value-of select="$tagName"/>&gt; bout found %d.\n",count<xsl:value-of select="$childName"/>);
                returnValue =  EXIT_FAILURE;
		goto cleanup;
	  	}
	</xsl:if>
	
	
	
	</xsl:for-each>
	
	//end of element
	
        if(nodeType != XML_READER_TYPE_END_ELEMENT)
                {
                fprintf(stderr,"Expected closing &lt;/<xsl:value-of select="@name"/>&gt; but found nodeType: %d \n",nodeType);
                if(nodeType==XML_READER_TYPE_ELEMENT)
                	{
                	fprintf(stderr,"Element is :%s",xmlTextReaderConstName(state -> reader));
                	}
                returnValue =  EXIT_FAILURE;
		goto cleanup;
                }
	if(xmlStrcmp(
			xmlTextReaderConstName(state -> reader),
			BAD_CAST "<xsl:value-of select="@name"/>"
			)!=0)
		{
		fprintf(stderr,"Expected closing &lt;/<xsl:value-of select="$tagName"/>&gt; but found %s\n",
			xmlTextReaderConstName(state -> reader));
                returnValue =  EXIT_FAILURE;
		goto cleanup;
		}
	
	cleanup:
	
	//free attributes
	<xsl:for-each select="$node/xsd:complexType/xsd:attribute">
	<xsl:sort select="@name"/>
	if(<xsl:value-of select="@name"/>Attr!=NULL)
		{
		xmlFree(<xsl:value-of select="@name"/>Attr);
		}
	</xsl:for-each>
	
	//Free Elements Values 
	<xsl:for-each select="$node/xsd:complexType/xsd:sequence/xsd:element">
	<xsl:sort select="@name"/>
	<xsl:variable name="childName">
	<xsl:call-template name="tagName">
	  <xsl:with-param name="node" select="."/>
	</xsl:call-template>
	</xsl:variable>
	<xsl:if test="not(@ref or (@name and xsd:complexType))">
	if( value<xsl:value-of select="$childName"/>!=NULL)
		{
		xmlFree(value<xsl:value-of select="$childName"/>);
		}
	</xsl:if>
	</xsl:for-each>
	#ifndef NDEBUG
	fprintf(stderr,"#Exiting <xsl:value-of select="$tagName"/>\n");
	#endif
	return returnValue;
	}

</xsl:template>



<xsl:template name="tagName">
<xsl:param name="node"/>
<xsl:choose>
<xsl:when test="$node/@name">
  <xsl:choose>
    <xsl:when test="name($node/..)='xsd:schema' or count(//xsd:element[@name=$node/@name])=1"><xsl:value-of select="$node/@name"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="concat($node/@name,'_',generate-id(.))"/></xsl:otherwise>
  </xsl:choose>
</xsl:when>
<xsl:when test="$node/@ref">
  <xsl:choose>
    <xsl:when test="name($node/..)='xsd:schema' or count(//xsd:element[@name=$node/@ref])=1"><xsl:value-of select="$node/@ref"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="concat($node/@ref,'_',generate-id(.))"/></xsl:otherwise>
  </xsl:choose>
</xsl:when>
  <xsl:otherwise>
     <xsl:message terminate="yes">
     	?? Tag name for <xsl:value-of select="name($node)"/> ??
     </xsl:message>
  </xsl:otherwise>

</xsl:choose>
</xsl:template>




</xsl:stylesheet>
