package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.ResourceUtils;
import org.lindenb.util.Compilation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * ImpactFactorSorter
 * @author pierre
 *
 */
public class ImpactFactorSorter
{
private static class PubmedArticle
	{
	/** original index in xml */
	int indexInList=0;
	/** associated dom element */
	Element domElement=null;
	/** impact factor */
	float impact=-1f;
	}

/** map nlim-id to impact factor */
private Map<String, Float> nlmid2impact;

public ImpactFactorSorter(Map<String, Float> nlmid2impact)
	{
	if(nlmid2impact==null) throw new NullPointerException("nlmid2impact is null");
	this.nlmid2impact=nlmid2impact;
	}

/** @return impact factor from a Nlm Id or -1 if unknown */
public float getImpactFromNlmId(String nlmId)
	{
	if(nlmId==null) return -1f;
	Float impact= this.nlmid2impact.get(nlmId);
	return impact==null?-1f:impact;
	}

private static Element find(Element root,String childName)
	{
	if(root==null) return null;
	for(Node n1=root.getFirstChild();
	n1!=null;
	n1=n1.getNextSibling())
		{
		if(!(	n1.getNodeType()==Node.ELEMENT_NODE &&
				n1.getNodeName().equals(childName))) continue;
		return Element.class.cast(n1);
		}
	return null;
	}

public Document sort(Document doc)
	{
	Vector<PubmedArticle> articles= new Vector<PubmedArticle>();
	if(doc==null) throw new NullPointerException("Document is null");
	Element root= doc.getDocumentElement();
	if(!root.getNodeName().equals("PubmedArticleSet"))
		{
		throw new IllegalArgumentException("Expected <PubmedArticleSet> as root");
		}
	int indexInList=0;
	
	Node curr=root.getFirstChild();
	Node next=null;
	while(curr!=null)
		{
		next=curr.getNextSibling();	
		
		if(!(	curr.getNodeType()==Node.ELEMENT_NODE ||
				curr.getNodeName().equals("PubmedArticle")))
			{
			curr=next;
			continue;
			}
		PubmedArticle pubmedArticle= new PubmedArticle();
		pubmedArticle.indexInList=(indexInList++);
		pubmedArticle.domElement= Element.class.cast(curr);
		Element medlineCitation= find(pubmedArticle.domElement,"MedlineCitation");
		Element medlineJournalInfo= find(medlineCitation,"MedlineJournalInfo");
		Element nlmUniqueID = find(medlineJournalInfo,"NlmUniqueID");

		if(nlmUniqueID!=null)
			{
			pubmedArticle.impact= getImpactFromNlmId(nlmUniqueID.getTextContent().trim());
			}
	
		articles.addElement(pubmedArticle);
		root.removeChild(curr);
		curr=next;
		}
	
	Collections.sort(articles, new Comparator<PubmedArticle>()
		{
		@Override
		public int compare(PubmedArticle a1, PubmedArticle a2)
			{
			if(a1.impact < a2.impact) return  1;
			if(a1.impact > a2.impact) return -1;
			int i=a1.indexInList-a2.indexInList;
			if(i!=0) return i;
			return 0;
			}
		});
	for(PubmedArticle pubmedArticle : articles)
		{
		root.appendChild(pubmedArticle.domElement);
		}
	
	
	
	return doc;
		
	
	}

/* parse a tab delimited score file containing nlmid and score */
private static Map<String,Float> readScoreFile(BufferedReader in) throws IOException
	{
	Pattern TAB= Pattern.compile("[\t]");
	HashMap<String, Float> map= new HashMap<String, Float>();
	int nlmId_col=-1;
	int score_col=-1;
	String line=in.readLine();
	if(line==null) throw new IOException("Expected at least one line in score file");
	if(line.startsWith("#")) line=line.substring(1);
	String tokens[]=TAB.split(line);
	for(int i=0;i< tokens.length;++i)
		{
		if(tokens[i].toLowerCase().equals("nlmid"))
			{
			nlmId_col=i;
			}
		else if(tokens[i].toLowerCase().equals("eigenfactor"))
			{
			score_col=i;
			}
		}
	if(nlmId_col==-1)  throw new IOException("Cannot find nlmid in header of score file");
	if(score_col==-1)  throw new IOException("Cannot find score in header of score file");
	while((line=in.readLine())!=null)
		{
		tokens=TAB.split(line);
		if(tokens.length<= nlmId_col || tokens.length<= score_col
			|| tokens[nlmId_col].trim().length()==0
			|| tokens[score_col].trim().length()==0)
			{
			continue;
			}
		try {
			Float n= new Float( tokens[score_col]);
			if(n!=null && n.floatValue()>=0) map.put(tokens[nlmId_col].trim(),n);
			} 
		catch (NumberFormatException e) {
			throw new IOException("Error in score file in "+line,e);
			}
		}
	
	return map;
	}

public static void main(String[] args) {
	try {
		String scoreFile=null;
		int optind=0;
    	while(optind<args.length)
	        {
	        if(args[optind].equals("-h"))
	           {
	        	System.err.println(Compilation.getLabel());
	        	System.err.println("This program sort the articles in a pubmed-xml file on the impact factor of their journals."+
	        				"Default values from http://www.eigenfactor.org/ with permission.");
	        	System.err.println("\t-h this screen");
	        	System.err.println("\t-f score file (may be an url) , tab delimited, header contains \"nlmid\" and \"eigenfactor\" (<optional>)");
	        	System.err.println("xml-file");
	        	System.err.println();
				return;
	           	}
	        else if(args[optind].equals("-f"))
	        	{
	        	scoreFile= args[++optind];
	        	}
	        else if(args[optind].equals("--"))
	            {
	            ++optind;
	            break;
	            }
	        else if(args[optind].startsWith("-"))
	            {
	            throw new IllegalArgumentException("Unknown option "+args[optind]);
	            }
	        else
	            {
	            break;    
	            }
	        ++optind;
	        }
    	Map<String,Float> map=null;
    	if(scoreFile==null)
    		{
    		BufferedReader r= ResourceUtils.openReader(ImpactFactorSorter.class, "eigenfactors.xls");
    		map= readScoreFile(r);
    		r.close();
    		}
    	else
    		{
    		BufferedReader r= IOUtils.openReader(scoreFile);
    		map= readScoreFile(r);
    		r.close();
    		}
    	
    	
    	
    	//create a xml reader and parse the file
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		factory.setExpandEntityReferences(true);
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(false);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setValidating(false);
		factory.setXIncludeAware(false);
		DocumentBuilder builder= factory.newDocumentBuilder();
		Document doc=null;
		
		//parse stdin
		if(args.length==optind)
			{
			doc= builder.parse(System.in);
			}
		//parse file
		else if(optind+1==args.length)
			{
			doc=builder.parse(new File(args[optind]));
			}
		//too much files
		else
			{
			System.err.println("Illegal number of arguments");
			System.exit(-1);
			}
		
		
		//sort the articles
		ImpactFactorSorter sorter= new ImpactFactorSorter(map);
		doc=sorter.sort(doc);
		
		//echo the document using an empty xslt document
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(new DOMSource(doc), new StreamResult(System.out));
		} 
	catch (Exception e)
		{
		e.printStackTrace();
		System.exit(-1);
		}
	}
}
