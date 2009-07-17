package org.lindenb.tinytools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lindenb.io.IOUtils;
import org.lindenb.util.Compilation;
import org.lindenb.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A test for Apache Lucene
 *
 */
public class Lucene4Genes
	{
	private static Logger LOG= Logger.getLogger(Lucene4Genes.class.getName());
	private File luceneDir;
	
	
	/**
	 * A SAXHandler parsing Entrez Gene and indexing the textual data
	 * @author pierre
	 *
	 */
	private static class GeneHandler
		extends DefaultHandler
		{
		//current value of the tag
		private StringBuilder content=null;
		//entrez gene id 
		private String id=null;
		//entrez gene title 
		private String title=null;
		//entrez gene concatenated textual data
		private StringBuilder text= new StringBuilder();
		//lucene indexer
		private IndexWriter indexWriter;
		
		GeneHandler(IndexWriter indexWriter)
			{
			this.indexWriter=indexWriter;
			}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException
			{
			this.content=null;
			if(StringUtils.isIn(
					name,
					"Gene-track_geneid",
					"Gene-ref_locus",
					"Gene-ref_desc",
					"Prot-ref_name_E",
					"Gene-ref_desc",
					"Entrezgene_summary",
					"Gene-commentary_text"
					))
				{
				this.content=new StringBuilder();
				}
			
			}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException
			{
			if(name.equals("Entrezgene"))
				{
				try {
					LOG.info("adding document \""+title+"\"");
					Document document=new Document();
					document.add(
						new Field(
							"id",
							this.id,
							Field.Store.YES,//Store the original field value in the index.
							Field.Index.NOT_ANALYZED //Index the field's value without using an Analyzer, so it can be searched.
							)
						);
					document.add(
						new Field(
							"title",
							(this.title==null?this.id:this.title),
							Field.Store.YES,//Store the original field value in the index.
							Field.Index.NOT_ANALYZED //Index the field's value without using an Analyzer, so it can be searched.
							)
						);
					document.add(
						new Field(
							"content",
							this.text.toString(),
							Field.Store.YES,//Store the original field value in the index.
							Field.Index.ANALYZED//Index the tokens produced by running the field's value through an Analyzer.
							)
						);
					//Sets a boost factor for hits on any field of this document. This value will be multiplied into the score of all hits on this document.
					if(this.text.toString().toLowerCase().contains("rotavirus"))
						{
						document.setBoost(100f);
						}
					//Adds a document to this index.
					this.indexWriter.addDocument(document);
					
				} catch (CorruptIndexException e) {
					throw new SAXException(e);
				} catch (IOException e) {
					throw new SAXException(e);
				}
				this.id=null;
				this.title=null;
				this.text= new StringBuilder();
				}
			else if(this.content!=null)
				{
				if(name.equals("Gene-track_geneid"))
					{
					this.id= this.content.toString();
					}
				else if(this.title==null && StringUtils.isIn(name,"Gene-ref_desc","Prot-ref_desc"))
					{
					this.title= this.content.toString();
					}
				
				this.text.append(this.content.toString()).append(" ");
				}
			this.content=null;
			}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(content!=null)
					{
					content.append(ch, start, length);
					}
			}
		
		}
	
	/** Constructor, create the directory if it does not exist */
	private Lucene4Genes(File luceneDir)
		throws IOException
		{
		if(!luceneDir.exists())
			{
			if(!luceneDir.mkdir())
				{
				throw new IOException("Cannot create "+luceneDir);
				}
			System.err.println("Created "+luceneDir);
			}
		if(!luceneDir.isDirectory())
			{
			throw new IOException("Not a directory "+luceneDir);
			}
		this.luceneDir=luceneDir;		
		}
	
	/**
	 * Index the XML stream containing the entrez genes
	 * @param in xml stream
	 * @throws IOException
	 * @throws SAXException
	 */
	private void indexGenes(InputStream in) throws IOException,SAXException
		{
		LOG.info("indexing genes in "+this.luceneDir);
		SAXParserFactory f= SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        SAXParser parser= null;
        try {
        	parser=f.newSAXParser();
			}
        catch (ParserConfigurationException err)
			{
			throw new SAXException(err);
			}

		/* An Analyzer builds TokenStreams, which analyze text.
		 * It thus represents a policy for extracting index terms from text. 
		 */
		Analyzer analyzer=new StandardAnalyzer();
		
		/* An IndexWriter creates and maintains an index.  */
		IndexWriter indexWriter=new IndexWriter(
			this.luceneDir,//data dir
			analyzer,
			true,//create
			IndexWriter.MaxFieldLength.UNLIMITED //no limit
			);
		
		parser.parse(in, new GeneHandler(indexWriter));
		
		/* multiple files for each segment are merged into a single file when a new segment is flushed. */
		indexWriter.setUseCompoundFile(true);
		/* Requests an "optimize" operation on an index, priming the index for the fastest available search. */
		indexWriter.optimize();
		indexWriter.close();
		}
	
	/**
	 * Search our database with the user query, print the result to stdout
	 * @param terms
	 * @throws IOException
	 */
	private void search(String terms) throws IOException
		{
		Directory directory= FSDirectory.getDirectory(this.luceneDir);
		IndexSearcher searcher=new IndexSearcher(directory);
		/* QueryParser translates query expressions into one of Lucene’s built-in query types */
		QueryParser q=new QueryParser("content", new StandardAnalyzer());
		try
			{
			TopDocCollector hitCollector = new TopDocCollector(5); 
			Query query =q.parse(terms);
			searcher.search(
				query,
				null,//if non-null, used to permit documents to be collected.
				hitCollector
				);
			TopDocs topDocs = hitCollector.topDocs(); 
			
			 if (topDocs!=null && topDocs.totalHits>0)
			   {
				for(ScoreDoc scoredoc:topDocs.scoreDocs)
					{
					Document document = searcher.doc(scoredoc.doc); 
					System.out.println(
						document.get("id")+"\t"+
						document.get("title")+"\t"+
						scoredoc.score
						);
					
					}
			 	}
			}
		catch(ParseException err)
			{
			throw new IOException(err);
			}
		}
	
	
	public static void main(String[] args)
		{
		Lucene4Genes app=null;
		try
			{
			File dir= new File(System.getProperty("java.io.tmpdir"),"lucene4genes");
			String program=null;
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("Lucene for genes. Pierre Lindenbaum PhD (2009).");
					System.err.println(Compilation.getLabel());
					System.err.println("options:");
					System.err.println(" -d <lucene-directory> default:"+dir);
					System.err.println(" -p <program>");
					System.err.println("     'index' <stdin|files> index the EntrezGenes input");
					System.err.println("     'query' '<the query>'");
					}
				else if(args[optind].equals("-d"))
					{
					dir=new File(args[++optind]);
					}
				else if(args[optind].equals("-p"))
					{
					program=args[++optind];
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
			if(program==null)
				{
				System.err.println("Undefined program");
				return;
				}
			app= new Lucene4Genes(dir);
			if(program.equals("query"))
				{
				if(optind+1!=args.length)
					{
					System.err.println("Illegal number of arguments.");
					return;
					}
				String query= args[optind++];
				app.search(query);
				}
			else if(program.equals("index"))
				{
				
				if(optind==args.length)
	                    {
						LOG.info("reading stdin");
						app.indexGenes(System.in);
	                    }
	            else
	                    {
	                    while(optind< args.length)
	                            {
	                    		String filename=args[optind++];
	                    		LOG.info("reading file "+filename);
	                          	java.io.InputStream r= IOUtils.openInputStream(filename);
	                          	app.indexGenes(r);
	                            r.close();
	                            }
	                    }
				}
			else
				{
				System.err.println("Unknown program "+program);
				return;
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
