<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:h="http://www.w3.org/1999/xhtml"
 >

 <!--

Motivation:
	transforms a Pubmed xml result to Java FX.
	Creates two charts:
		number of publications/year
		number of publication/journal
Author
	Pierre Lindenbaum PhD plindenbaum@yahoo.fr
	http://plindenbaum.blogspot.com

-->

 <xsl:key name="articlesIds" match="/PubmedArticleSet/PubmedArticle/MedlineCitation/MedlineJournalInfo/NlmUniqueID" use="."/>

<!-- ========================================================================= -->
<xsl:output method='text' indent='yes' omit-xml-declaration="no"/>

<xsl:variable name="minMedlineTA">
  <xsl:for-each select="/PubmedArticleSet/PubmedArticle/MedlineCitation/MedlineJournalInfo/MedlineTA">
    <xsl:sort select="."  order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="minYear">
  <xsl:for-each select="/PubmedArticleSet/PubmedArticle/MedlineCitation/DateCreated/Year">
    <xsl:sort select="." data-type="number" order="ascending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="maxYear">
  <xsl:for-each select="/PubmedArticleSet/PubmedArticle/MedlineCitation/DateCreated/Year">
    <xsl:sort select="." data-type="number" order="descending" />
    <xsl:if test="position() = 1">
      <xsl:value-of select="." />
    </xsl:if>
  </xsl:for-each>
</xsl:variable>


<xsl:template match="/">
import javafx.stage.Stage; 
import javafx.scene.layout.LayoutInfo;
import javafx.stage.Alert;  
import javafx.scene.Scene;  
import javafx.scene.chart.PieChart; 
import javafx.scene.chart.BarChart;  
import javafx.scene.chart.PieChart3D;  
import javafx.scene.text.Font;
import javafx.scene.chart.part.CategoryAxis;
import javafx.scene.chart.part.NumberAxis;


<xsl:apply-templates select="PubmedArticleSet"/>
</xsl:template>




<xsl:template match="PubmedArticleSet">

def valuesPerYear = [
  <xsl:call-template name="barPerYear"><xsl:with-param name="year" select="$minYear"/></xsl:call-template>

];


Stage {
       title: "By Year"
 	scene: Scene {  
         	height: 800  
        	width: 1000 
		
		content: BarChart {
				title : "Per Year"
				layoutInfo: LayoutInfo {width: 580}
		 		height: 800  
				width: 1000
				categoryAxis: CategoryAxis { categories: for (value in valuesPerYear) value.category}
 				valueAxis: NumberAxis { lowerBound: 0; upperBound: <xsl:call-template name="maxPerYear"><xsl:with-param name="year" select="$minYear"/><xsl:with-param name="max" select="0"/></xsl:call-template> ; tickUnit: 10 }

				 data: [ BarChart.Series {
				  name: "Year"
				  data:valuesPerYear
  				

				}
				]
				}
		}
       }  

Stage {
       title: "By Journal"  
     scene: Scene {  
         height: 800  
         width: 1000  
         content: PieChart {  
             title: "By Journal"  
	     height: 800  
             width: 1000
	     pieToLabelLineCurved: true
	     pieLabelFont: Font{ size:9 }
             data: [  
<xsl:for-each select="PubmedArticle">
    <xsl:sort select="MedlineCitation/MedlineJournalInfo/MedlineTA"  order="ascending" />
	
    <xsl:if test="generate-id(MedlineCitation/MedlineJournalInfo/NlmUniqueID) = generate-id(key('articlesIds', MedlineCitation/MedlineJournalInfo/NlmUniqueID))">
      
      <xsl:variable name="journalId" ><xsl:value-of select="MedlineCitation/MedlineJournalInfo/NlmUniqueID"/></xsl:variable>
      <xsl:variable name="journalName" ><xsl:value-of select="MedlineCitation/MedlineJournalInfo/MedlineTA"/></xsl:variable>
	<xsl:if test="$journalName != $minMedlineTA">,</xsl:if>
	
	PieChart.Data {
		value: <xsl:value-of select="count(/PubmedArticleSet/PubmedArticle[MedlineCitation/MedlineJournalInfo/NlmUniqueID = $journalId])"/>;
		label: "<xsl:value-of select="$journalName"/>";
		}

    </xsl:if>
</xsl:for-each>
		]
  	}  
     }  
 }  
</xsl:template>

<xsl:template name="barPerYear">
<xsl:param name="year"/>
<xsl:if test="$year &lt;= $maxYear">
<xsl:if test="$year != $minYear">,</xsl:if>
BarChart.Data { 
category: "<xsl:value-of select="$year"/>"
value: <xsl:value-of select="count(/PubmedArticleSet/PubmedArticle[number(MedlineCitation/DateCreated/Year) = $year] )"/>
}

<xsl:call-template name="barPerYear"><xsl:with-param name="year" select="1 + $year"/></xsl:call-template>
</xsl:if>
</xsl:template>

<xsl:template name="maxPerYear">
<xsl:param name="year"/>
<xsl:param name="max"/>
<xsl:choose>
<xsl:when test="$year &lt;= $maxYear">
	<xsl:variable name="total" select="count(/PubmedArticleSet/PubmedArticle[number(MedlineCitation/DateCreated/Year) = $year] )"/>
	<xsl:call-template name="maxPerYear">
		<xsl:with-param name="year" select="1 + $year"/>
		<xsl:with-param name="max">
			<xsl:choose>
				<xsl:when test="$total &lt; number($max)"><xsl:value-of select="$max"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$total"/></xsl:otherwise>
			</xsl:choose>
		</xsl:with-param>
	</xsl:call-template>
</xsl:when>
<xsl:otherwise>
	<xsl:value-of select="number($max)"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>
