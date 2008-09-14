package org.lindenb.tinytools;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lindenb.util.Compilation;
import org.lindenb.util.Couple;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * AboutIdentifiers
 *
 */
public class AboutIdentifiers
	{
	
	/**
	 * Database
	 *
	 */
	private enum Database
		{
		pubmed
			{
			@Override
			public String getURL(Identifier id) {
				return "http://www.ncbi.nlm.nih.gov/pubmed/"+id.getId();
				}
			@Override
			public String getTitle(Identifier id,Map<FieldInfo,String> map)
				{
				return map.get(FieldInfo.title);
				}
			@Override
			public String getDotShape() { return "ellipse"; }
			} ,
		snp{
			@Override
			public String getURL(Identifier id) {
				return "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs="+id.getId();
				}
			@Override
			public String getTitle(Identifier id,Map<FieldInfo,String> map)
				{
				return "rs"+id.getId();
				}
			@Override
			public String getDotShape() { return "circle"; }
			} ,
		omim
			{
			@Override
			public String getURL(Identifier id) {
				return "http://www.ncbi.nlm.nih.gov/entrez/dispomim.cgi?id="+id.getId();
			}
			@Override
			public String getTitle(Identifier id,Map<FieldInfo,String> map)
				{
				return map.get(FieldInfo.omimAlias);
				}
			@Override
			public String getDotShape() { return "rect"; }
			} ,
		gene
			{
			@Override
			public String getURL(Identifier id) {
				return "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch="+id.getId();
			}
			@Override
			public String getTitle(Identifier id,Map<FieldInfo,String> map)
				{
				return map.get(FieldInfo.geneDesc);
				}
			@Override
			public String getDotShape() { return "house"; }
			} ,
		unigene
			{
			@Override
			public String getURL(Identifier id) {
				return "http://www.ncbi.nlm.nih.gov/UniGene/clust.cgi?UGID="+id.getId();
				}
			@Override
			public String getTitle(Identifier id,Map<FieldInfo,String> map)
				{
				return "unigene:"+id.getId();
				}
			@Override
			public String getDotShape() { return "hexagon"; }
			} 
		;
		public abstract String getDotShape();
		public abstract String getURL(Identifier id);
		public abstract String getTitle(Identifier id,Map<FieldInfo,String> map);
		};
	
	private enum FieldInfo
		{
		title,journal,year,
		het_value,
		geneLocus,geneDesc,mapLoc,geneSummary,
		omimSymbol,omimLocus,omimAlias
		}
		

		
	/**
	 * 
	 * Identifier
	 *
	 */
	private class Identifier
		{	
		Database database;
		int id;
		Identifier(Database database,int id)
			{
			if(database==null) throw new NullPointerException();
			if(id<=0) throw new IllegalArgumentException("bad id");
			this.database=database;
			this.id=id;
			}
		
		public Database getDatabase() {
			return database;
			}
		
		public int getId() {
			return id;
			}
		
		public String getURL()
			{
			return getDatabase().getURL(this);
			}
		
		public String getTitle()
			{
			Hashtable<FieldInfo, String> map=AboutIdentifiers.this.seenIdentifiers.get(this);
			return getDatabase().getTitle(this,map);
			}
		
		public String dotName()
			{
			return getDatabase().name()+"_"+getId();
			}
		
		public String dotDeclaration()
			{
			return dotName()+
				"[" +
				"label=\""+getTitle().replace('\"', '\'')+"\", "+
				"URL=\""+ getURL()+"\","+
				"shape="+getDatabase().getDotShape()+
				"]";
			}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getDatabase().hashCode();
			result = prime * result + getId();
			return result;
			}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Identifier other = Identifier.class.cast(obj);
			return  this.getDatabase().equals(other.getDatabase()) &&
					this.getId()==(other.getId());
			}
		@Override
		public String toString() {
			return "("+getDatabase()+")"+getId();
			}
		}
	
	/**
	 * EFetchInfo
	 */
	private class EFetchInfo
		extends DefaultHandler
		{
		private Database databaseType;
		private StringBuilder content= new StringBuilder();
		private Hashtable<FieldInfo,String> current_Info=null;
		
		EFetchInfo(Database databaseType)
			{
			this.databaseType = databaseType;
			}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			this.content.setLength(0);
			}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException
			{
			
			switch(this.databaseType)
				{
				case pubmed:
					{
					if(name.equals("PMID"))
						{
						Identifier id = new Identifier(this.databaseType,Integer.parseInt(this.content.toString()));
						this.current_Info= AboutIdentifiers.this.seenIdentifiers.get(id);
						}
					else if(name.equals("MedlineTA"))
						{
						this.current_Info.put(FieldInfo.journal,this.content.toString());
						}
					else if(name.equals("ArticleTitle"))
						{
						this.current_Info.put(FieldInfo.title,this.content.toString());
						}
					else if(name.equals("Year"))
						{
						this.current_Info.put(FieldInfo.year, this.content.toString());
						}
					else if(name.equals("PubmedArticle"))
						{
						this.current_Info=null;
						}
					break;
					}
				case snp:
					{
					if(name.equals("Rs_rsId"))
						{
						Identifier id = new Identifier(this.databaseType,Integer.parseInt(this.content.toString()));
						this.current_Info= AboutIdentifiers.this.seenIdentifiers.get(id);
						}
					else if(name.equals("Rs_het_value"))
						{
						this.current_Info.put(FieldInfo.het_value, this.content.toString());
						}
					break;
					}
				case gene:
					{
					if(name.equals("Gene-track_geneid"))
						{
						Identifier id = new Identifier(this.databaseType,Integer.parseInt(this.content.toString()));
						this.current_Info= AboutIdentifiers.this.seenIdentifiers.get(id);
						}
					else if(name.equals("Gene-ref_locus"))
						{
						this.current_Info.put(FieldInfo.geneLocus, this.content.toString());
						}
					else if(name.equals("Gene-ref_desc"))
						{
						this.current_Info.put(FieldInfo.geneDesc, this.content.toString());
						}
					else if(name.equals("Gene-ref_maploc"))
						{	
						this.current_Info.put(FieldInfo.mapLoc, this.content.toString());
						}
					else if(name.equals("Entrezgene_summary"))
						{	
						this.current_Info.put(FieldInfo.geneSummary, this.content.toString());
						}
					break;
					}
				case omim:
					{
					if(name.equals("Mim-entry_mimNumber"))
						{
						Identifier id = new Identifier(this.databaseType,Integer.parseInt(this.content.toString()));
						this.current_Info= AboutIdentifiers.this.seenIdentifiers.get(id);
						}
					else if(name.equals("Mim-entry_symbol"))
						{
						this.current_Info.put(FieldInfo.omimSymbol, this.content.toString());
						}
					else if(name.equals("Mim-entry_locus"))
						{
						this.current_Info.put(FieldInfo.omimLocus, this.content.toString());
						}
					else if(name.equals("Mim-entry_aliases_E"))
						{	
						this.current_Info.put(FieldInfo.omimAlias, this.content.toString());
						}
					break;
					}
				default:
					{
					System.err.println(name);
					break;
					}
				}
			this.content.setLength(0);
			}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			this.content.append(ch,start,length);
			}
		
		}
	
	/**
	 * ELinkHandler
	 *
	 */
	private class ELinkHandler
		extends DefaultHandler
		{
		Identifier identifier;
		StringBuilder content= new StringBuilder();
		int LinkSetDb_flag=0;
		Database dbTo=null;
		
		ELinkHandler(Identifier identifier)
			{
			this.identifier=identifier;
			}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			this.content.setLength(0);
			if(name.equals("LinkSetDb"))
				{
				LinkSetDb_flag=1;
				}
			
			}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException
			{
			if(name.equals("LinkSetDb"))
				{
				LinkSetDb_flag=0;
				this.dbTo=null;
				}
			else if(LinkSetDb_flag==1 &&
					name.equals("DbTo"))
				{
				String dbstr= this.content.toString();
				this.dbTo=null;
				for(Database d: Database.values())
					{
					if(d.name().equals(dbstr))
						{
						dbTo=d;
						break;
						}
					}
				}
			else if(dbTo!=null &&
					name.equals("Id")
					)
				{
				Identifier id = new Identifier(
						dbTo,
						Integer.parseInt(this.content.toString())
						);
				Couple<Identifier> newLink=new Couple<Identifier>
					(
					this.identifier,
					id
					);
				
				if(!AboutIdentifiers.this.links.contains(newLink))
					{
					if(!id.equals(this.identifier) &&
						!(this.identifier.getDatabase().equals(Database.pubmed) &&
						  id.getDatabase().equals(Database.pubmed)))
						{
						AboutIdentifiers.this.seenIdentifiers.put(id,new Hashtable<FieldInfo,String>());
						AboutIdentifiers.this.links.add(newLink);
						}
					}
				
				}
			this.content.setLength(0);
			}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			this.content.append(ch,start,length);
			}
		}
	
	
	private SAXParser saxParser;
	private HashSet<Couple<Identifier>> links= new HashSet<Couple<Identifier>>();
	private Hashtable<Identifier, Hashtable<FieldInfo,String>> seenIdentifiers =  new Hashtable<Identifier, Hashtable<FieldInfo,String>>();
	
	AboutIdentifiers() throws ParserConfigurationException, SAXException
		{
		SAXParserFactory f= SAXParserFactory.newInstance();
		f.setNamespaceAware(false);
		f.setValidating(false);
		f.setXIncludeAware(false);
		this.saxParser= f.newSAXParser();
		}
	

	public void scan(Set<Identifier> identifiers) throws IOException,SAXException
		{
		for(Identifier identifier: identifiers)
			{
			this.seenIdentifiers.put(identifier,new Hashtable<FieldInfo,String>());
			ELinkHandler handler= new ELinkHandler(identifier);
			String uri = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?" +
					"dbfrom=" + identifier.getDatabase() +
					"&id=" + identifier.getId() +
					"&cmd=neighbor&db=all"
					; 
			this.saxParser.parse(uri,handler);
			}
			
		}
	
	private void fetchInfos() throws IOException,SAXException
		{
		for(Database db: Database.values())
			{
			boolean ok=false;
			URL url = new URL("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");		
	    	URLConnection connection = url.openConnection();
	    	connection.setDoOutput(true);
	    	PrintStream out= new PrintStream(connection.getOutputStream());
	    	out.print("db="+db+"&retmode=xml");
			
			for(Identifier identifier: this.seenIdentifiers.keySet())
				{
				if(identifier.getDatabase()!=db) continue;
				out.print("&id="+identifier.getId());
				ok=true;
				}
			out.close();
			if(!ok) continue;
			System.err.println(db);
	    	InputStream in = connection.getInputStream();
			
			EFetchInfo handler= new EFetchInfo(db);
			this.saxParser.parse(in,handler);
			
			in.close();
			}
		}
	
	private void dotCluster(Set<Identifier> cluster) throws IOException
		{
		File file= File.createTempFile("_jeter_", ".dot");
		PrintWriter out= new PrintWriter(file);
		
		out.println("graph G{");
		out.println("ranksep=3; ratio=auto;");
		for(Identifier id:cluster)
			{
			out.println(id.dotDeclaration());
			}
		for(Couple<Identifier> link:this.links)
			{
			if(!(cluster.contains(link.first()) ||
			     cluster.contains(link.second()))) continue;
			out.println(link.first().dotName()+"--"+link.second().dotName()+";");
			}
		out.println("}");
		out.flush();
		out.close();
		String args[]={"twopi","-Tsvg","-o/tmp/jeter.svg",file.toString()};
		Process proc=Runtime.getRuntime().exec(args);
		try {
			proc.waitFor();
			file.delete();
			} 
		catch (InterruptedException e) {
			e.printStackTrace();
			}
		}
	
	private List<Set<Identifier>> makeClusters()
		{
		Vector<Set<Identifier>> clusters= new Vector<Set<Identifier>>();
		
		HashSet<Identifier> remains= new HashSet<Identifier>(this.seenIdentifiers.keySet());
		/** loop while there is a cluster to create */
		while(!remains.isEmpty())
			{
			//the new cluster
			HashSet<Identifier> cluster= new HashSet<Identifier>();
			Identifier start=remains.iterator().next();
			//add the first item in the new cluster
			remains.remove(start);
			cluster.add(start);
			

			while(true)
				{
				HashSet<Identifier> tobeAdded= new HashSet<Identifier>();
				for(Identifier id1: cluster)
					{
					for(Couple<Identifier> link:this.links)
						{
						if( link.contains(id1))
							{
							Identifier id2 =link.getComplement(id1);
							remains.remove(id2);
							if(!cluster.contains(id2))
								{
								tobeAdded.add(id2);
								}
							}
						}

					}
				if(tobeAdded.isEmpty()) break;
				cluster.addAll(tobeAdded);
				}
			
			System.err.println("Found Cluster "+cluster);
			if(cluster.size()>1)
				{
				System.err.println("Adding "+cluster);
				clusters.add(cluster);
				}
			}
		
		return clusters;
		}
	
	private  void _parse(Set<Identifier> set,Database database,InputStream in) throws IOException
		{
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		while((line=r.readLine())!=null)
			{
			if(line.startsWith("#")) continue;
			line=line.trim().toLowerCase();
			if(line.length()==0) continue;
			if(database.equals(Database.snp) && line.startsWith("rs"))
				{
				line=line.substring(2);
				}
			set.add(new Identifier(database,Integer.parseInt(line)));
			}
		
		set.add(new Identifier(Database.snp,2056202));
		
		set.add(new Identifier(Database.snp,2056302));
		
		}
	
	public static void main(String[] args) {
		try
			{
			AboutIdentifiers app= new AboutIdentifiers();
			int optind=0;
			Database database=Database.snp;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
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
			HashSet<Identifier> identifiers= new HashSet<Identifier>();
			if(optind==args.length)
				{
				app._parse(identifiers,database,System.in);
				}
			else
				{
				while(optind< args.length)
					{
					FileInputStream r= new FileInputStream(args[optind++]);
					app._parse(identifiers,database,r);
					r.close();
					}
				}
			
			if(identifiers.isEmpty())
				{
				System.err.println("Empty Set.");
				return;
				}
			app.scan(identifiers);
			
			for(Couple<Identifier> link:app.links)
				{
				System.err.println(link);
				}
			app.fetchInfos();
			for(Set<Identifier> cluster:app.makeClusters())
				{
				System.out.println(cluster);
				app.dotCluster(cluster);
				}
			
			
			//app.printDOT(System.out);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
}
