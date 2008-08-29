/**
 * 
 */
package org.lindenb.tinytools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lindenb.bio.ncbi.QueryKeyHandler;
import org.lindenb.util.Compilation;
import org.lindenb.xml.XMLUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author pierre
 * based on an idea from David Rothman's blog
 * http://davidrothman.net/2008/08/19/a-really-good-idea-for-a-3rd-party-pubmedmedline-tool/
 * 
 */
public class MeshFrequencies
	{
	/**
	 * EUTilsHandler
	 *
	 */
	private static class EUTilsHandler extends QueryKeyHandler
		{
		Set<Integer> pmids;
		EUTilsHandler(Set<Integer> pmids)
			{
			this.pmids=pmids;
			}
		@Override
		public void foundId(String id) {
			this.pmids.add(Integer.parseInt(id));
			}
		}
	/**
	 * EFetchHandler
	 *
	 */
	private class EFetchHandler
		extends DefaultHandler
		{
		private StringBuilder content=new StringBuilder();
		Article currentArticle=null;
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException
			{
			content.setLength(0);
			if(name.equals("PubmedArticle"))
				{
				this.currentArticle=new Article();
				MeshFrequencies.this.articles.addElement(this.currentArticle);
				}
			
			}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException
			{
			if(name.equals("PubmedArticle"))
				{
				this.currentArticle=null;
				}
			else if(name.equals("PMID")) 
				{
				this.currentArticle.pmid= Integer.parseInt(this.content.toString());
				}
			else if(name.equals("ArticleTitle")) 
				{
				this.currentArticle.title= this.content.toString();
				}
			else if(name.equals("MedlineTA")) 
				{
				this.currentArticle.journal= this.content.toString();
				}
			else if(name.equals("Year")) 
				{
				this.currentArticle.year= Integer.parseInt( this.content.toString());
				}
			else if(name.equals("DescriptorName")) 
				{
				String mesh= this.content.toString();
				Set<Article> set= MeshFrequencies.this.mesh2articles.get(mesh);
				if(set==null)
					{
					set=new HashSet<Article>();
					MeshFrequencies.this.mesh2articles.put(mesh,set);
					}
				set.add(this.currentArticle);
				}
			content.setLength(0);
			}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			content.append(ch, start, length);
			}
		
		}
	
	/** a class describing an pubmed Article */
	private static class Article
		implements Comparable<Article>
		{
		int pmid;
		String title="";
		String journal="";
		int year=0;
		@Override
		public int hashCode() {
			return  31 * 1 + pmid;
			}
		@Override
		public int compareTo(Article o) {
			return 0;
			}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			return (pmid != Article.class.cast(obj).pmid);
			}
		@Override
		public String toString() {
			return "{pmid:"+pmid+"}";
			}
		
		}
	
	/** all the articles */
	private Vector<Article> articles= new Vector<Article>();
	/** mesh term to articles */
	private Map<String, Set<Article> > mesh2articles=new TreeMap<String, Set<Article>>();
	
	/** constructor */
	private MeshFrequencies()
		{
		}
	
	public void fetch(Set<Integer> pmids) throws SAXException,IOException
		{
		URL url= new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream());
		out.write("db=pubmed"+
                "&rettype=full"+
                "&tool=meshfrequencies"+
                "&email=plindenbaum_at_yahoo.fr"+
                "&retmode=xml"
                );
		for(Integer id:pmids)  out.write("&id="+id);
		out.close();
		
		InputStream in=connection.getInputStream();
		SAXParser parser= newSAXParser();
		parser.parse(in, new EFetchHandler());
	    in.close();
		}
	
	public void run(PrintStream out) throws IOException
		{
		Vector<String> terms= new Vector<String>(this.mesh2articles.size());
		terms.addAll(this.mesh2articles.keySet());
		Collections.sort(terms,new Comparator<String>()
				{
				@Override
				public int compare(String s1, String s2)
					{
					return
						MeshFrequencies.this.mesh2articles.get(s2).size() -
						MeshFrequencies.this.mesh2articles.get(s1).size()
						;
					}
				});
		Collections.sort(this.articles);
		out.print("<table>");
		  out.print("<thead><tr>");
		  out.print("<th>Mesh</th>");
		  for(Article article:this.articles) out.print(
				  "<th><a href=\"http://www.ncbi.nlm.nih.gov/pubmed/"+article.pmid+"\">"+
				  article.pmid+
				  "</a></th>");
		  out.print("</tr></thead>\n");
		  out.println("<tbody>");
		  
		  for(String mesh:terms)
		  	{
			out.print("<tr>");
			out.print("<th><a href=\"http://www.nlm.nih.gov/cgi/mesh/2008/MB_cgi?mode=&term="+URLEncoder.encode(mesh, "UTF-8")+"\">"+
						XMLUtilities.escape(mesh)+"</a></th>");
			for(Article article:this.articles)
				{
				out.print("<td>");
				out.print(this.mesh2articles.get(mesh).contains(article)?"X":"");
				out.print("</td>");
				}
			out.println("</tr>");
		  	}
		  
		  out.print("</tbody>");
		out.print("</table>");
		}
	
	/** create a new sax parser */
	private static SAXParser newSAXParser() throws SAXException
		{
		try
		{
		SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        return factory.newSAXParser();
		} catch(ParserConfigurationException err)
			{
			throw new SAXException(err);
			}
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int max_return=100;
			String term=null;
			Set<Integer> pmids=new HashSet<Integer>();
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("-pmid a list of pmid separated by comma");
					System.err.println("-term <a pubmed query>");
					System.err.println("-n <max return for pubmed query> default:"+max_return);
					}
				else if(args[optind].equals("-pmid"))
					{
					++optind;
					for(String pmid: args[optind].split("[,]"))
						{
						pmids.add( Integer.parseInt(pmid) );
						}
					}
				else if(args[optind].equals("-term"))
					{
					term=args[++optind];
					}
				else if(args[optind].equals("-n"))
					{
					max_return=Integer.parseInt( args[++optind]);
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					}
				else 
					{
					break;
					}
				++optind;
				}
			
			if(term!=null)
				{
				EUTilsHandler handler= new EUTilsHandler(pmids);

				URL url=new URL(
                        "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term="+
                        URLEncoder.encode(term, "UTF-8")+
                        "&retstart=0&retmax="+max_return+
                        "&usehistory=y&retmode=xml&email=plindenbaum_at_yahoo.fr&tool=meshfreqs");
				
				InputStream in=url.openStream();
				newSAXParser().parse(in,handler);
				in.close();
				}
			if(pmids.isEmpty()) return;
			
			MeshFrequencies app= new MeshFrequencies();
			app.fetch(pmids);
			app.run(System.out);
			}
		catch (Throwable e)
			{
			e.printStackTrace();
			}

	}

}
