<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:event="http://purl.org/NET/c4dm/event.owl#"
	xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#"
	xmlns:doap="http://usefulinc.com/ns/doap#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	
	xmlns:ex="http://simile.mit.edu/2006/11/exhibit#"
	version='1.0'
	>
<!--

Source:
	http://code.google.com/p/lindenb/source/browse/trunk/src/xsl/pubmed2exhibit.xsl

Motivation:
	This stylesheet transforms one or more Pubmed
	Article into a Exhibit ( http://simile.mit.edu/wiki/Exhibit )
	presentation

Author:
	Pierre Lindenbaum
	plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com

Usage:
	xsltproc   pubmed2exhibit.xsl ~/pubmed_result.xml > file.html

Optional Parameters:
	'title' overrides the title
	'analytics' code for google analytics
	
	example:
		xsltproc \-\-stringparam title "My Bibliography" \-\-stringparam analytics "UA-XXXXX-X" \-\-novalid pubmed2exhibit.xsl ~/pubmed_result.txt > ~/jeter.html
	
	
References:
 	http://plindenbaum.blogspot.com/2010/01/transforming-pubmed-to-simileexhibit.html
	http://simile.mit.edu/mail/ReadMsg?listId=10&msgId=22059
	http://www.dpawson.co.uk/xsl/sect2/N2696.html
	http://www.eggheadcafe.com/articles/20010508.asp

-->
<xsl:output method='html' />
<xsl:key name="distinct-authors" match="//Author" use="."/>
<xsl:param name="title"></xsl:param>
<xsl:param name="analytics"></xsl:param>

<xsl:template match="/">
<html>


   <xsl:comment>
	Generated with pubmed2exhibit.xsl
	Author: Pierre Lindenbaum PhD.
	http://plindenbaum.blogspot.com
	plindenbaum@yahoo.fr 
   </xsl:comment>
   <xsl:variable name="title2">
   	<xsl:choose>
   		<xsl:when test="string-length($title)=0">
	   		<xsl:value-of select="count(/PubmedArticleSet/PubmedArticle)"/><xsl:text> Articles.</xsl:text>
	   	</xsl:when>
	   	<xsl:otherwise>
	   		<xsl:value-of select="$title"/>
	   	</xsl:otherwise>
   	</xsl:choose>
   </xsl:variable>
    <head>
        <title><xsl:value-of select="$title2"/></title>

        <link type="inline" rel="exhibit/data" />
	

        <script src="http://static.simile.mit.edu/exhibit/api-2.0/exhibit-api.js"
        	type="text/javascript">
        </script>
    	<script src="http://static.simile.mit.edu/exhibit/extensions-2.0/time/time-extension.js"
    		type="text/javascript">
    	</script>


	 <script>
		Exhibit.InlineImporter = { };
		Exhibit.importers["inline"] = Exhibit.InlineImporter;
		Exhibit.InlineImporter.load = function(link, database, cont) {
		Exhibit.UI.showBusyIndicator();
		database.loadData(Exhibit.InlineImporter.userdata);
		Exhibit.UI.hideBusyIndicator();
		if (cont) cont();
		};
		
		Exhibit.InlineImporter.userdata=({
			<xsl:apply-templates select="PubmedArticleSet"/> 
			});
	</script>

        <style>
      
        h1.main-title {
        	padding:10px;
        	text-shadow: 3px 3px 4px gray;
        	 font-size:   250%;
        	}
        
        div.Article {
           border: 1px solid lightgray;
           margin:20px;
           padding:20px;
           -moz-border-radius: 20px;
           border-radius: 20px; 
           background-color:rgb(230,230,230);
           }
	
	div.me {
            font-style:  italic;
            font-size:   80%;
            }


	div.title {
           font-weight: bold;
           font-size:   120%;
           }

        .journal {
        	font-style:  italic;
        	}
        
        .abstract {
        	background-color:rgb(240,240,240);
        	border: 1px solid black;
        	width:80%;
        	max-height:200px;
        	overflow:auto;
        	margin:20px;
          	padding:20px;
        	}
        .affiliation { font-size: 80%; margin-left:40px; }
        .volume { font-weight: bold;  }
        .issue { }
        .pages { }
        .date { }
        .authors { margin-left:20px;}
        .pmid { text-align:right;margin-left:40px; }
        </style>
    </head> 
    <body>
    <h1 class="main-title"><xsl:value-of select="$title2"/></h1>
     <table width="100%">
        <tr valign="top">
            <td ex:role___="viewPanel">
    
   <!-- this is the view for each article -->
  
   <div ex:role="lens" class="Article">
   	<div class="citation">
   		<span ex:content=".journal" class="journal"></span>.
   		<span ex:content=".date" class="date"></span>;
   		
   		<span ex:if-exists=".volume">
   			<span ex:content=".volume" class="volume"></span>
   		</span>
   		
   		<span ex:if-exists=".issue">
   			(<span ex:content=".issue" class="issue"></span>)
   		</span>
   		
   		<span ex:if-exists=".pages">
   			:<span ex:content=".pages" class="pages"></span>
   		</span>
   	</div>
   	<div ex:content=".title" class="title"></div>
   	
   	<div ex:content=".authors" class="authors"></div>
	
	<div ex:if-exists=".affiliation" class="affiliation">
		<span ex:content=".affiliation" ></span>
	</div>
	<div ex:if-exists=".abstract">
		<div ex:content=".abstract" class="abstract"></div>
	</div>
	
	<div class="pubmed">PMID:
		<a ex:href-content=".url" ex:target-content=".pmid" title="link to pubmed">
			<span ex:content=".pmid"/>
		</a>
	</div>
   </div>
   <!-- end of view for article -->
   
		 <div ex:role="view" ex:orders=".pmid">
                </div>
   
		<!-- BEGIN VIEW for TIMELINE -->
		<div ex:role="view"
		ex:viewClass="Timeline"
		ex:start=".date"
		ex:colorKey=".year">
		</div>
		<!-- END VIEW for TIMELINE -->
            
               
                
                
                
                
            </td>
            <td width="25%">
                <!-- Search -->
                 <div ex:role="facet" ex:facetClass="TextSearch"></div>
		<!-- filters -->
                <div ex:role="facet" ex:expression=".year" ex:facetLabel="Year"></div>
                <div ex:role="facet" ex:expression=".journal" ex:facetLabel="Journal"></div>
                <div ex:role="facet" ex:expression=".authors" ex:facetLabel="Author"></div>
            </td>
        </tr>
    </table>

	<div class="me">
		<xsl:text>Made with </xsl:text><a href="http://code.google.com/p/lindenb/source/browse/trunk/src/xsl/pubmed2exhibit.xsl">pubmed2exhibit.xsl</a>
		
		<xsl:text> Author: Pierre Lindenbaum PhD. (</xsl:text>
		<a href="mailto:plindenbaum@yahoo.fr">plindenbaum@yahoo.fr</a>
		<xsl:text>)   </xsl:text>
		<a href="http://plindenbaum.blogspot.com">http://plindenbaum.blogspot.com</a>
	</div>
   	
    <xsl:if test="string-length($analytics)&gt;0">
    <xsl:comment>BEGIN GOOGLE ANALYTICS</xsl:comment>
<script type='text/javascript'>

 var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '<xsl:value-of select="$analytics"/>']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script');
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 
        'http://www') + '.google-analytics.com/ga.js';
    ga.setAttribute('async', 'true');
    document.documentElement.firstChild.appendChild(ga);
  })();

</script>
<xsl:comment>END GOOGLE ANALYTICS</xsl:comment>
    </xsl:if>
    </body>
    </html>
</xsl:template>

<xsl:template match="PubmedArticleSet">
      types: {
             "Article":{
                     pluralLabel: "Articles"
                     }
       	     },
     "items" : [
     	<xsl:apply-templates select="PubmedArticle"/>
     	
     	<!--
	<xsl:for-each select="//Author">
	<xsl:if test="generate-id(.)=generate-id(key('distinct-authors', .)[1])">
	,
	<xsl:apply-templates select="." mode="json"/>
	</xsl:if>
	</xsl:for-each> -->
     	]
</xsl:template>


<xsl:template match="PubmedArticle">
	<xsl:variable name="pmid"><xsl:value-of select="MedlineCitation/PMID"/></xsl:variable>
	<xsl:if test="position()&gt;1"><xsl:text>,</xsl:text></xsl:if>
	{
	"type":"Article",
	"id":"pmid:<xsl:value-of select="$pmid"/>",
	"label":"pmid:<xsl:value-of select="$pmid"/>",
	"pmid":<xsl:value-of select="$pmid"/>,
	"url":"http://www.ncbi.nlm.nih.gov/pubmed/<xsl:value-of select="$pmid"/>"
	<xsl:apply-templates select="MedlineCitation/Article/ArticleTitle" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/Article/Journal/JournalIssue" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/MedlineJournalInfo/MedlineTA" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/Article/Pagination/MedlinePgn" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/Article/AuthorList" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/Article/Abstract/AbstractText" mode="json"/>
	<xsl:apply-templates select="MedlineCitation/Article/Affiliation" mode="json"/>
	}
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
                <xsl:text>\"</xsl:text>
                <xsl:call-template name="escape">
                        <xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
                </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
                <xsl:value-of select='$s'/>
        </xsl:otherwise>
        </xsl:choose>
</xsl:template>


<xsl:template match="AuthorList" mode="json">
<xsl:text>,"authors":[</xsl:text>
<xsl:for-each select="Author">
	<xsl:if test="position()&gt;1"><xsl:text>,</xsl:text></xsl:if>
	<xsl:call-template name="quote">
		<xsl:with-param name="s">
			<xsl:call-template name="authorName">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:with-param>
	</xsl:call-template>
</xsl:for-each>
<xsl:text>]</xsl:text>
</xsl:template>


<xsl:template match="Author" mode="json">
<xsl:variable name="name">
<xsl:call-template name="quote">
		<xsl:with-param name="s">
			<xsl:call-template name="authorName">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:with-param>
</xsl:call-template>
</xsl:variable>
	{
	"type":"Author",
  	"id":<xsl:value-of select="$name"/>,
  	"label":<xsl:value-of select="$name"/>,
  	"firstName":<xsl:call-template name="quote">
		<xsl:with-param name="s">
			<xsl:call-template name="firstName">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:with-param>
		</xsl:call-template>,
	"lastName":<xsl:call-template name="quote">
		<xsl:with-param name="s">
			<xsl:call-template name="lastName">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:with-param>
		</xsl:call-template>
  	}
</xsl:template>

<xsl:template match="Affiliation" mode="json">
	<xsl:text>,"affiliation":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="AbstractText" mode="json">
	<xsl:text>,"abstract":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="ArticleTitle" mode="json">
	<xsl:text>,"title":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="Year" mode="json">
	<xsl:text>,"year":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>


<xsl:template match="MedlineTA" mode="json">
	<xsl:text>,"journal":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="JournalIssue" mode="json">
	<xsl:apply-templates select="Volume" mode="json"/>
	<xsl:apply-templates select="Issue" mode="json"/>
	<xsl:choose>
		<xsl:when test="PubDate/MedlineDate">
			
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select="PubDate" mode="json"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="Volume" mode="json">
	<xsl:text>,"volume":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="Issue" mode="json">
	<xsl:text>,"issue":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="MedlinePgn" mode="json">
	<xsl:text>,"pages":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s" select="."/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="PubDate" mode="json">
	<xsl:apply-templates select="Year" mode="json"/>
	<xsl:text>,"date":</xsl:text>
	<xsl:call-template name="quote">
		<xsl:with-param name="s">
			<xsl:call-template name="articleDate">
				<xsl:with-param name="date" select="."/>
			</xsl:call-template>
		</xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template name="authorName">
<xsl:param name="node"/>

<xsl:variable name="the-name">
	<xsl:call-template name="lastName">
		<xsl:with-param name="node" select="$node"/>
	</xsl:call-template>
	
	<xsl:if test="$node/MiddleName">
		<xsl:text> </xsl:text>
		<xsl:value-of select="$node/MiddleName"/>
	</xsl:if>
	
	<xsl:text> </xsl:text>
	
	<xsl:call-template name="firstName">
		<xsl:with-param name="node" select="$node"/>
	</xsl:call-template>
</xsl:variable>

<xsl:value-of select="normalize-space($the-name)"/>
</xsl:template>


<xsl:template name="firstName">
<xsl:param name="node"/>
<xsl:choose>
	<xsl:when test="$node/ForeName"><xsl:value-of select="$node/ForeName"/></xsl:when>
	<xsl:when test="$node/FirstName"><xsl:value-of select="$node/FirstName"/></xsl:when>
	<xsl:otherwise></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="lastName">
<xsl:param name="node"/>
<xsl:choose>
	<xsl:when test="$node/LastName"><xsl:value-of select="$node/LastName"/></xsl:when>
	<xsl:when test="$node/Initials"><xsl:value-of select="$node/Initials"/></xsl:when>
	<xsl:when test="$node/CollectiveName">Collective Work</xsl:when>
	<xsl:otherwise></xsl:otherwise>
</xsl:choose>
</xsl:template>



<xsl:template match="ArticleIdList">
	<bibo:doi><xsl:value-of select="ArticleId"/></bibo:doi>
</xsl:template>


<xsl:template match="ArticleId">
	<xsl:choose>
		<xsl:when test="@IdType=&apos;doi&apos;">
		<bibo:doi><xsl:value-of select="."/></bibo:doi>
		</xsl:when>
	</xsl:choose>
</xsl:template>





<xsl:template match="MeshHeadingList">
	<xsl:apply-templates select="MeshHeading"/>
</xsl:template>

<xsl:template match="MeshHeading">
	<xsl:for-each select="DescriptorName">
		<xsl:element name="dc:subject"><xsl:value-of select="."/></xsl:element>
	</xsl:for-each>
</xsl:template>


<xsl:template match="PersonalNameSubjectList">
	<xsl:apply-templates select="PersonalNameSubject"/>
</xsl:template>




<xsl:template name="articleDate">
<xsl:param name="date"/>
<xsl:if test="$date/Year">
<xsl:value-of select="$date/Year"/><xsl:if test="$date/Month">-<xsl:call-template name="month2int"><xsl:with-param name="m"><xsl:value-of select="$date/Month"/></xsl:with-param></xsl:call-template><xsl:if test="$date/Day">-<xsl:value-of select="$date/Day"/></xsl:if></xsl:if>
</xsl:if>
</xsl:template>

<xsl:template name="month2int">
<xsl:param name="m"/>
<xsl:variable name="month"><xsl:value-of select="translate($m,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/></xsl:variable>
<xsl:choose>
<xsl:when test="$month='january' or $month='jan' or $month='jan.'">01</xsl:when>
<xsl:when test="$month='february' or $month='feb' or $month='feb.'">02</xsl:when>
<xsl:when test="$month='march' or $month='mar' or $month='mar.'">03</xsl:when>
<xsl:when test="$month='april' or $month='apr' or $month='apr.'">04</xsl:when>
<xsl:when test="$month='may'">05</xsl:when>
<xsl:when test="$month='june'  or $month='jun' or $month='jun.'">06</xsl:when>
<xsl:when test="$month='july'  or $month='jul' or $month='jul.'">07</xsl:when>
<xsl:when test="$month='august' or $month='aug' or $month='aug.' ">08</xsl:when>
<xsl:when test="$month='september' or $month='sep' or $month='sep.' ">09</xsl:when>
<xsl:when test="$month='october' or $month='oct' or $month='oct.' ">10</xsl:when>
<xsl:when test="$month='november' or $month='nov' or $month='nov.'  ">11</xsl:when>
<xsl:when test="$month='december' or $month='dec' or $month='dec.' ">12</xsl:when>
<xsl:otherwise><xsl:value-of select="$month"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
