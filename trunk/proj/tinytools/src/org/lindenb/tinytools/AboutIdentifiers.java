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
 * load a list of rs## at ncbi
 * and draw the links as SVG using graphiz dot
 */
public class AboutIdentifiers
	{
	
	/**
	 * Database
	 * describe a database at the ncbi
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
		/** @return the DOT shape */
		public abstract String getDotShape();
		/** @return a URL about this identifier */
		public abstract String getURL(Identifier id);
		/** @return the title about an identifier */
		public abstract String getTitle(Identifier id,Map<FieldInfo,String> map);
		};
	
	/** various fields describing a XML record at the ncbi */
	private enum FieldInfo
		{
		title,journal,year,
		het_value,
		geneLocus,geneDesc,mapLoc,geneSummary,
		omimSymbol,omimLocus,omimAlias
		}
		

		
	/**
	 * 
	 * An Identifier at the NCBI
	 * defines a identification number
	 * associated to a database
	 *
	 */
	private class Identifier
		{
		/** the ncbi database */
		Database database;
		/** the id in this database */
		int id;
		
		Identifier(Database database,int id)
			{
			if(database==null) throw new NullPointerException();
			if(id<=0) throw new IllegalArgumentException("bad id");
			this.database=database;
			this.id=id;
			}
		/** @return the database */
		public Database getDatabase() {
			return database;
			}
		/** @return the id */
		public int getId() {
			return id;
			}
		/** @return the url associated to a database */
		public String getURL()
			{
			return getDatabase().getURL(this);
			}
		
		/** return a string describing this */
		public String getTitle()
			{
			Hashtable<FieldInfo, String> map=AboutIdentifiers.this.seenIdentifiers.get(this);
			return getDatabase().getTitle(this,map);
			}
		/** @return a string used as a node in DOT */
		public String dotName()
			{
			return getDatabase().name()+"_"+getId();
			}
		
		/** declare the attributes in DOT */
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
	 * SAX handler retrieving the data associated with an identifier
	 * at NCBI
	 */
	private class EFetchInfo
		extends DefaultHandler
		{
		private Database databaseType;
		private StringBuilder content= new StringBuilder();
		private Hashtable<FieldInfo,String> current_Info=null;
		private Identifier currentIdentifier=null;
		
		EFetchInfo(Database databaseType)
			{
			this.databaseType = databaseType;
			}
		
		void updateCurrentInfo()
			{
			this.currentIdentifier = new Identifier(this.databaseType,Integer.parseInt(this.content.toString()));
			this.current_Info= AboutIdentifiers.this.seenIdentifiers.get(this.currentIdentifier);
			if(this.current_Info==null)
				{
				throw new RuntimeException("Cannot find current-info for "+this.currentIdentifier);
				}
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
					if(name.equals("PMID") &&
						this.currentIdentifier==null)//<-- else bug when CommentsCorrections/ommentOn/PMID
						{
						updateCurrentInfo();
						}
					else if(name.equals("MedlineTA"))
						{
						this.current_Info.put(FieldInfo.journal,this.content.toString());
						}
					else if(name.equals("ArticleTitle"))
						{
						this.current_Info.put(FieldInfo.title,this.content.toString());
						}
					else if(name.equals("Year") )
						{
						if(this.current_Info==null)
							{
							throw new SAXException("Null ptr !?"+this.currentIdentifier);
							}
						this.current_Info.put(FieldInfo.year, this.content.toString());
						}
					else if(name.equals("PubmedArticle"))
						{
						this.currentIdentifier=null;
						this.current_Info=null;
						}
					break;
					}
				case snp:
					{
					if(name.equals("Rs_rsId"))
						{
						updateCurrentInfo();
						}
					else if(name.equals("Rs_het_value"))
						{
						this.current_Info.put(FieldInfo.het_value, this.content.toString());
						}
					else if(name.equals("Rs"))
						{
						this.current_Info=null;
						this.currentIdentifier=null;
						}
					break;
					}
				case gene:
					{
					if(name.equals("Gene-track_geneid"))
						{
						updateCurrentInfo();
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
					else if(name.equals("Entrezgene"))
						{
						this.current_Info=null;
						this.currentIdentifier=null;
						}
					break;
					}
				case omim:
					{
					if(name.equals("Mim-entry_mimNumber"))
						{
						updateCurrentInfo();
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
					else if(name.equals("Mim-entry"))
						{
						this.current_Info=null;
						this.currentIdentifier=null;
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
	 * find the links between two identifiers
	 */
	private class ELinkHandler
		extends DefaultHandler
		{
		//the identifier
		Identifier identifier;
		//the current string content
		StringBuilder content= new StringBuilder();
		//found  LinkSetDb
		int LinkSetDb_flag=0;
		//the database currently associated
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
	
	/** sax parser */
	private SAXParser saxParser;
	/** all the links found */
	private HashSet<Couple<Identifier>> links= new HashSet<Couple<Identifier>>();
	/** all the nodes and their info */
	private Hashtable<Identifier, Hashtable<FieldInfo,String>> seenIdentifiers =  new Hashtable<Identifier, Hashtable<FieldInfo,String>>();
	/** where the svg files should be saved */
	private File outputDir=null;
	
	private AboutIdentifiers() throws ParserConfigurationException, SAXException
		{
		SAXParserFactory f= SAXParserFactory.newInstance();
		f.setNamespaceAware(false);
		f.setValidating(false);
		f.setXIncludeAware(false);
		this.saxParser= f.newSAXParser();
		}
	
	/**
	 * Loop over each identifier and
	 * scan the associated id in each id
	 */
	private void scan(Set<Identifier> identifiers) throws IOException,SAXException
		{
		int loop=0;
		for(Identifier identifier: identifiers)
			{
			System.err.println("scan "+identifier+" ("+(++loop)+"/"+identifiers.size()+")");
			this.seenIdentifiers.put(identifier,new Hashtable<FieldInfo,String>());
			ELinkHandler handler= new ELinkHandler(identifier);
			String uri = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?" +
					"dbfrom=" + identifier.getDatabase() +
					"&id=" + identifier.getId() +
					"&cmd=neighbor&db=all"
					; 
			
			this.saxParser.parse(uri,handler);
			
			
			
			try {
				//don't be evil with the ncbi wait 1 sec
				Thread.sleep(1000L);
				}
			catch (InterruptedException e) {
				
				}
			}
			
		}
	
	/**
	 * retrieve the XML descriptions of each identifier
	 * 
	 */
	private void fetchInfos() throws IOException,SAXException
		{
		for(Database db: Database.values())
			{
			boolean ok=false;
			//build the POST query
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
			
			InputStream in = connection.getInputStream();
			EFetchInfo handler= new EFetchInfo(db);
			this.saxParser.parse(in,handler);
			in.close();
			
			try {
				//don't be evil with the ncbi wait 1 sec
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				
			}
			
			}
		}
	
	/**
	 * Save a cluster to SVG Using graphiz DOT
	 */
	private void dotCluster(Set<Identifier> cluster,int clusterId) throws IOException
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
		File svgFile = new File(this.outputDir,"cluster"+clusterId+".svg");
		System.err.println(svgFile.toString());
		String args[]={"twopi","-Tsvg","-o"+svgFile,file.toString()};
		Process proc=Runtime.getRuntime().exec(args);
		try {
			proc.waitFor();
			file.delete();
			} 
		catch (InterruptedException e) {
			e.printStackTrace();
			}
		}
	/**
	 * Build the cluster of identifiers
	 * Each cluster has not any link with another cluster
	 */
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
			
			//System.err.println("Found Cluster "+cluster);
			if(cluster.size()>1)
				{
				//System.err.println("Adding "+cluster);
				clusters.add(cluster);
				}
			}
		
		return clusters;
		}
	/**
	 * Parse a stream and get the identifiers
	 */
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
		
		//set.add(new Identifier(Database.snp,2056202));
		//set.add(new Identifier(Database.snp,2056302));
		
		}
	/**
	 * main
	 */
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
					System.err.println("-d output dir");
					}
				else if(args[optind].equals("-d"))
					{
					app.outputDir= new File(args[++optind]);
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
			System.err.println("Scanning "+identifiers.size());
			app.scan(identifiers);
			
			for(Couple<Identifier> link:app.links)
				{
				System.err.println(link);
				}
			app.fetchInfos();
			int loop=0;
			for(Set<Identifier> cluster:app.makeClusters())
				{
				++loop;
				System.out.println(cluster);
				app.dotCluster(cluster,loop);
				}
			
			
			//app.printDOT(System.out);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
}
