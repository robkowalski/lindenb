package org.gnf.interactome;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.lindenb.berkeley.DocumentBinding;
import org.lindenb.io.IOUtils;
import org.lindenb.lang.InvalidXMLException;
import org.lindenb.util.Base64;
import org.lindenb.util.Cast;
import org.lindenb.util.StringUtils;

import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;

import org.lindenb.xml.NamespaceContextImpl;
import org.lindenb.xml.Sax2Dom;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class Interactome01
{
private static final String PSI_NS="net:sf:psidev:mi";
private static final Logger LOG=Logger.getLogger(Interactome01.class.getName());
private static long ID_GENERATOR=System.currentTimeMillis();
private File envHome;
private Environment berkeleyEnv;
private SimpleDB<BerkeleyDBKey,Document> biogridDB;
/** map interactor ID to interaction id */
private MultipleDB<BerkeleyDBKey, BerkeleyDBKey> interactor2interaction;
private SimpleDB<String, String> qName2wikipedia;
private SimpleDB<String, String> omim2qname;
private SimpleDB<String, String> entrezGene2qname;
/** map a QName in wikipedia to its BOX template name  */
private SimpleDB<String, String> qName2boxtemplate;
private SimpleDB<String, BerkeleyDBKey> shortName2interactor;
private SimpleDB<String, BerkeleyDBKey> omim2interactor;
/** map a pmid to its XML description */
private SimpleDB<Integer,Document> pmid2dom;
private DocumentBuilder documentBuilder;
private DocumentBinding documentBinding;
private Transformer transformer;
private Templates pubmed2wikiXslt;
private XPath xpath;
private XMLInputFactory xmlInputFactory;

private XPathExpression xpathFindOmimId=null;
private XPathExpression xpathInteractorShortName=null;
private XPathExpression findParticipantRef=null;
private XPathExpression xpathFindMethod=null;
private XPathExpression xpathFindExperimentRef=null;


/** httpClient for loggin/wikipedia */
private HttpClient httpClient=new HttpClient();

/** stores user id */
static private class MWAuthorization
	{
	String lguserid;
	String lgusername;
	String lgtoken;
	String cookieprefix;
	String sessionid;
	}

static private enum BioGridKeyType
	{
	proteinInteractor,
	interaction,
	experimentDescription,
	}

private static class ZipBinding
	extends TupleBinding<String>
	{
	@Override
	public String entryToObject(TupleInput input) {
		boolean zipped= input.readBoolean();
		if(!zipped)
			{
			return input.readString();
			}
		int len = input.readInt();
		try {
			byte array[]= new byte[len];
			input.read(array);
			GZIPInputStream in= new GZIPInputStream(new ByteArrayInputStream(array));
			ByteArrayOutputStream out= new ByteArrayOutputStream();
			IOUtils.copyTo(in, out);
			in.close();
			out.close();
			return new String(out.toByteArray());
			} 
		catch(IOException err) { throw new RuntimeException(err);}
		}
	
	@Override
	public void objectToEntry(String object, TupleOutput output)
		{
		byte array[]= object.getBytes();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream out= new GZIPOutputStream(baos);
			ByteArrayInputStream in= new ByteArrayInputStream(array);
			IOUtils.copyTo(in, out);
			in.close();
			out.close();
			byte array2[]=baos.toByteArray();
			if(array2.length +4 /** size of length */ < array.length)
				{
				output.writeBoolean(true);
				output.writeInt(array2.length);
				output.write(array2);
				}
			else
				{
				output.writeBoolean(false);
				output.writeString(object);
				}
			} 
		catch(IOException err) { throw new RuntimeException(err);}
		}
	}


abstract class  BerkeleyDB<K,V>
	{
	protected Database database;
	protected TupleBinding<K> keyBinding;
	protected TupleBinding<V> valueBinding;
	BerkeleyDB(Database database,
		TupleBinding<K> keyBinding,
		TupleBinding<V> valueBinding
		)
		{
		this.database=database;
		this.keyBinding=keyBinding;
		this.valueBinding=valueBinding;
		}
	
	public void close() throws DatabaseException
		{
		this.database.close();
		}
	public Set<K> getKeySet() throws DatabaseException
		{
		Set<K> set= new HashSet<K>(); 
		DatabaseEntry key= new DatabaseEntry();
		DatabaseEntry data= new DatabaseEntry();
		Cursor c= cursor();
		while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			set.add(entryToKey(key));
			}
		c.close();
		return set;
		}
	
	
	Cursor cursor() throws DatabaseException
		{
		return this.database.openCursor(null, null);
		}
	
	public void clear() throws DatabaseException
		{
		DatabaseEntry key= new DatabaseEntry();
		DatabaseEntry data= new DatabaseEntry();
		Cursor c= cursor();
		while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			c.delete();
			}
		c.close();
		}
	
	public  TupleBinding<K> getKeyBinding() { return keyBinding;}
	public  TupleBinding<V> getValueBinding() { return valueBinding;}
	public DatabaseEntry keyToEntry(K key)
		{
		if(key==null) throw new NullPointerException("key==null");
		DatabaseEntry e= new DatabaseEntry();
		getKeyBinding().objectToEntry(key, e);
		return e;
		}
	public DatabaseEntry valueToEntry(V val)
		{
		if(val==null) throw new NullPointerException("val==null");
		DatabaseEntry e= new DatabaseEntry();
		getValueBinding().objectToEntry(val, e);
		return e;
		}
	public K entryToKey(DatabaseEntry e)
		{
		return getKeyBinding().entryToObject(e);
		}
	public V entryToValue(DatabaseEntry e)
		{
		return getValueBinding().entryToObject(e);
		}
	
	public boolean put(K k, V v)  throws DatabaseException
		{
		DatabaseEntry key= keyToEntry(k);
		DatabaseEntry value= valueToEntry(v);
		return this.database.put(null, key, value)==OperationStatus.SUCCESS;
		}
	}


class  SimpleDB<K,V>
extends BerkeleyDB<K, V>
	{
	SimpleDB(Database database,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding)
		{
		super(database,keyBinding,valueBinding);
		}
	
	public V get(K k) throws DatabaseException
		{
		DatabaseEntry key= keyToEntry(k);
		DatabaseEntry value= new DatabaseEntry();
		if(this.database.get(null, key, value, null)!=OperationStatus.SUCCESS) return null;
		return entryToValue(value);
		}
	}
 
class  MultipleDB<K,V>
extends BerkeleyDB<K, V>
	{
	MultipleDB(Database database,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding)
		{
		super(database,keyBinding,valueBinding);
		}
	
	public List<V> get(K k) throws DatabaseException
		{
		List<V> list= new ArrayList<V>();
		DatabaseEntry key= keyToEntry(k);
		DatabaseEntry value= new DatabaseEntry();
		Cursor c= cursor();
		if(c.getSearchKey(key, value, null)!=OperationStatus.SUCCESS) return list;
		list.add(entryToValue(value));
		while(c.getNextDup(key, value, null)==OperationStatus.SUCCESS)
			{
			list.add(entryToValue(value));
			}
		c.close();
		return list;
		}
	}


private class BerkeleyDBKey
	{
	BioGridKeyType type;
	String id;
	BerkeleyDBKey(BioGridKeyType type, String id)
		{
		this.type=type;
		this.id=id;
		}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOuterType().hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		BerkeleyDBKey other = (BerkeleyDBKey) obj;
	    return id.equals(other.id) && type.equals(other.type);
		}

	private Interactome01 getOuterType() {
		return Interactome01.this;
	}
	
	@Override
	public String toString() {
		return this.type.toString()+" "+id;
		}
	
	}

private class DBKeyBinding extends TupleBinding<BerkeleyDBKey>
	{
	public BerkeleyDBKey entryToObject(TupleInput input)
		{
		BioGridKeyType type= BioGridKeyType.values()[input.readInt()];
		 String id= input.readString();
		 return new BerkeleyDBKey(type,id); 
		}
	public void objectToEntry(BerkeleyDBKey object, TupleOutput output)
		{
		output.writeInt(object.type.ordinal());
		output.writeString(object.id);
		}
	
	}

/**
 * A SAX Handler parsing a PSI XML file
 *
 */
private class BioGridHandler
	extends Sax2Dom
	{
	BioGridHandler()
		{
		super(Interactome01.this.documentBuilder);
		}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		
		if(super.currentNode.getNodeType()==Node.ELEMENT_NODE &&
			XMLUtilities.getLevel(super.currentNode)==4 &&
			!StringUtils.isIn(super.currentNode.getParentNode().getLocalName(),
					"source","availabilityList")
			)
			{
			try
				{
				BerkeleyDBKey bdbKey=null;
				BioGridKeyType type=BioGridKeyType.valueOf(localName);
				if(localName.equals("interaction"))
					{
					String id="interaction"+(++ID_GENERATOR);
					Element.class.cast(super.currentNode).setAttribute("id", id);
					bdbKey = new BerkeleyDBKey(type,id);
					
					NodeList list=(NodeList)findParticipantRef.evaluate(currentNode,XPathConstants.NODESET);
					if(list.getLength()!=2) throw new SAXException("Boum");
					
					for(int i=0;i< list.getLength();++i)
						{
						BerkeleyDBKey partnerId =new BerkeleyDBKey(BioGridKeyType.proteinInteractor,Attr.class.cast(list.item(i)).getValue());
						interactor2interaction.put(
							partnerId,
							bdbKey)
							;
						//LOG.info(bdbKey+" "+partnerId);
						}
					}
				else if(localName.equals("proteinInteractor"))
					{
					Attr att=(Attr)this.currentNode.getAttributes().getNamedItem("id");
					if(att==null) throw new SAXException("Cannot get id in "+name);
					bdbKey = new BerkeleyDBKey(BioGridKeyType.proteinInteractor,att.getValue());
					String shortName=(String)xpathInteractorShortName.evaluate(currentNode, XPathConstants.STRING);
					if(!(name==null || name.trim().length()==0))
						{
						shortName2interactor.put(shortName, bdbKey);
						}
					else
						{
						LOG.info("Cannot get short Name for "+" "+bdbKey);
						}
					
					Attr omimAtt=(Attr)xpathFindOmimId.evaluate(currentNode, XPathConstants.NODE);
					if(omimAtt!=null)
						{
						//LOG.info("OK got OMIM for "+omimAtt+" "+bdbKey);
						omim2interactor.put(omimAtt.getValue(),bdbKey);
						}
					else
						{
						//LOG.info("Cannot get OMIM for "+att.getValue()+" "+bdbKey);
						}
					
					
					}
				else
					{
					Attr att=(Attr)this.currentNode.getAttributes().getNamedItem("id");
					if(att==null) throw new SAXException("Cannot get id in "+name);
					bdbKey = new BerkeleyDBKey(type,att.getValue());
					}
				
				
				
				DOMResult result= new DOMResult(documentBuilder.newDocument());
				transformer.transform(new DOMSource(this.currentNode),result);
				if(!biogridDB.put(bdbKey,(Document)result.getNode()))
					{
					throw new DatabaseException("Cannot insert new document");
					}
				if( biogridDB.get(bdbKey)==null)
					{
					throw new DatabaseException("Cannot retrieve "+bdbKey);
					}
				}
			catch(Exception err)
				{
				err.printStackTrace();
				throw new SAXException("Boum",err);
				}
			
			super.currentNode=super.currentNode.getParentNode();
			XMLUtilities.removeChildren(super.currentNode);
			}
		
		else
			{
			super.currentNode=super.currentNode.getParentNode();
			}
		}
	}

private Interactome01() throws Exception
	{
	
	//this.envHome= new File(System.getProperty("java.io.tmpdir"),"gnf01");
	this.envHome= new File("/home/lindenb/tmp/gnf01");//TODO FIX THIS
	
	DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
	domFactory.setCoalescing(true);
	domFactory.setExpandEntityReferences(true);
	domFactory.setIgnoringComments(true);
	domFactory.setNamespaceAware(true);
	domFactory.setValidating(false);
	domFactory.setIgnoringElementContentWhitespace(true);
	this.documentBuilder= domFactory.newDocumentBuilder();
	this.documentBinding= new DocumentBinding(this.documentBuilder);
	
	
	TransformerFactory tFactory=TransformerFactory.newInstance();
	//ARGH, full path, ugly ! FIX THIS
	this.pubmed2wikiXslt=tFactory.newTemplates(new StreamSource(
			new File("/home/lindenb/src/lindenb/src/xsl/pubmed2wiki.xsl")));
	
	this.transformer =tFactory.newTransformer();
	this.transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	
	XPathFactory xpathFactory=XPathFactory.newInstance();
	this.xpath=xpathFactory.newXPath();
	NamespaceContextImpl ctx=new NamespaceContextImpl();
	ctx.setPrefixURI("psi",PSI_NS);
	this.xpath.setNamespaceContext(ctx);
	this.findParticipantRef=xpath.compile("psi:participantList/psi:proteinParticipant/psi:proteinInteractorRef/@ref");
	this.xpathFindOmimId =xpath.compile("psi:xref[1]/psi:primaryRef[@db='MIM']/@id");
	this.xpathInteractorShortName= xpath.compile("psi:names[1]/psi:shortLabel[1]/text()");
	this.xpathFindMethod = xpath.compile("psi:interactionType[1]/psi:names[1]/psi:shortLabel[1]/text()");
	this.xpathFindExperimentRef = xpath.compile("psi:experimentList[1]/psi:experimentRef[1]/@ref");
	
	this.xmlInputFactory = XMLInputFactory.newInstance();
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
	}


/**
 * Open the BerkeleyDB environement
 * @throws DatabaseException
 * @throws IOException
 */
private void open() throws DatabaseException,IOException
	{
	if(!this.envHome.exists())
		{
		if(!this.envHome.mkdir()) throw new IOException("Cannot create "+this.envHome );
		}
	if(!this.envHome.isDirectory()) throw new IOException("Not a directory "+this.envHome);
	/* create BDB environment */
	EnvironmentConfig envCfg= new EnvironmentConfig();
	envCfg.setAllowCreate(true);
	this.berkeleyEnv= new Environment(this.envHome,envCfg);
	DatabaseConfig cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	
	this.biogridDB= new SimpleDB<BerkeleyDBKey, Document>(
		this.berkeleyEnv.openDatabase(null, "biogrid", cfg),
		new DBKeyBinding(),
		this.documentBinding
		);
	
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	this.pmid2dom= new SimpleDB<Integer, Document>(
			this.berkeleyEnv.openDatabase(null, "pmid2doc", cfg),
			new IntegerBinding(),
			this.documentBinding
			);
	
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	this.omim2interactor= new SimpleDB<String, BerkeleyDBKey>(
			this.berkeleyEnv.openDatabase(null, "omim2interactor",cfg),
			new StringBinding(),
			new DBKeyBinding()
			);
	
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	this.shortName2interactor= new SimpleDB<String, BerkeleyDBKey>(
			this.berkeleyEnv.openDatabase(null, "shortName2interactor", cfg),
			new StringBinding(),
			new DBKeyBinding()
			);
	
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(true);
	interactor2interaction= new MultipleDB<BerkeleyDBKey, BerkeleyDBKey>(
			this.berkeleyEnv.openDatabase(null, "interactor2interaction", cfg),
			new DBKeyBinding(),
			new DBKeyBinding()
			);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(false);
	qName2wikipedia= new SimpleDB<String, String>(
			this.berkeleyEnv.openDatabase(null, "qName2wikipedia", cfg),
			new StringBinding(),
			new ZipBinding()
			);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(true);
	qName2boxtemplate=  new SimpleDB<String, String>(
			this.berkeleyEnv.openDatabase(null, "qName2templates", cfg),
			new StringBinding(),
			new StringBinding()
			);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(false);
	omim2qname= new SimpleDB<String, String>(
			this.berkeleyEnv.openDatabase(null, "omim2qname", cfg),
			new StringBinding(),
			new StringBinding()
			);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(false);
	entrezGene2qname= new SimpleDB<String, String>(
			this.berkeleyEnv.openDatabase(null, "entrezgene2qname", cfg),
			new StringBinding(),
			new StringBinding()
			);
	}

/**
 * Close the BerkeleyDB environement
 * @throws DatabaseException
 */
private void close()throws DatabaseException
	{
	qName2wikipedia.close();
	interactor2interaction.close();
	qName2boxtemplate.close();
	shortName2interactor.close();
	omim2interactor.close();
	biogridDB.close();
	omim2qname.close();
	entrezGene2qname.close();
	pmid2dom.close();
	if(this.berkeleyEnv!=null) { try { this.berkeleyEnv.close();this.berkeleyEnv=null;} catch(Exception err) { }}
	}

/** 
 * retrieve a PMID document
 * fetch it if needed
 */
private Document getPubmed(String s) throws IOException,DatabaseException,SAXException
	{
	if(!Cast.Integer.isA(s)) throw new IOException("Not a number :"+s);
	int pmid= Cast.Integer.cast(s);
	Document dom= this.pmid2dom.get(pmid);
	if(dom==null)
		{
		LOG.info("fetching pmid:"+pmid);
		dom=this.documentBuilder.parse("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id="+pmid);
		if(dom==null) throw new IOException("Cannot retrieve pmid "+s);
		this.pmid2dom.put(pmid,dom);
		}
	return dom;
	}


private String pmid2wiki(String pmid) throws Exception
	{
	Document dom= getPubmed(pmid);
	StringWriter str= new StringWriter();
	Transformer tr=this.pubmed2wikiXslt.newTransformer();
	tr.setParameter("layout", "\"no\"");
	tr.transform(new DOMSource(dom), new StreamResult(str));
	String s= str.toString();
	int i= s.indexOf("{{");
	if(i!=-1) s=s.substring(i);
	i= s.indexOf("}}");
	if(i!=-1) s=s.substring(0,i+2);
	s=s.replaceAll("[ \n\t]+", " ");
	return s;
	}

/**
 * delete the content of a BerkeleyDB database
 * @param db
 * @throws DatabaseException
 */
static void clear(Database db) throws  DatabaseException
	{
	DatabaseEntry key= new DatabaseEntry();
	DatabaseEntry data= new DatabaseEntry();
	Cursor c=db.openCursor(null, null);
	while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
		{
		c.delete();
		}
	c.close();
	}

/** parse the BIOGRID file and fill berkeleyDB */
private void parseBiogrid(File f) throws Exception
	{
	interactor2interaction.clear();
	biogridDB.clear();
	shortName2interactor.clear();
	omim2interactor.clear();
	SAXParserFactory saxFactory= SAXParserFactory.newInstance();
	saxFactory.setNamespaceAware(true);
	saxFactory.setValidating(false);

	SAXParser parser= saxFactory.newSAXParser();
	parser.parse(f, new BioGridHandler());
	}

private static String simpleFindField(String wikiText,String tag)
	{
	int i=wikiText.indexOf(tag);
	if(i==-1) return null;
	int k=i-1;
	while(k>=0 && Character.isWhitespace(wikiText.charAt(k)))
		{
		--k;
		}
	if(k==-1 || wikiText.charAt(k)!='|') return null;
	
	i+= tag.length();
	while(i< wikiText.length() && Character.isWhitespace(wikiText.charAt(i)))
		{
		i++;
		}
	if(i== wikiText.length() || wikiText.charAt(i)!='=') return null;
	i++;
	int j= wikiText.indexOf('|',i);
	if(j==-1) return null;
	String s= wikiText.substring(i,j).trim();
	return s.length()==0?null:s;
	}



/**
 * Load Pages from wikipedia
 * @throws Exception
 */
private void loadWikipedia(boolean cleanupFirst) throws Exception
	{
	LOG.info("load wikipedia");
	if(cleanupFirst)
		{
		qName2wikipedia.clear();
		qName2boxtemplate.clear();
		omim2qname.clear();
		entrezGene2qname.clear();
		}
	
	MWQuery query= new MWQuery();
	//get all the pages aving a Template:PBB
	for(Page page:query.listPagesEmbedding(new Page("Template:PBB")))
		{
		if(!cleanupFirst)
			{
			if(this.qName2wikipedia.get(page.getQName())!=null) continue;
			}
		
		
		LOG.info("current page is "+page);
		//save the content of this page
		String content= query.getContent(page);
		
		this.qName2wikipedia.put(page.getQName(),content);
		LOG.info("load templates in "+page);
		boolean found=false;
		//get al the templates in this page
		for(Page template:query.listTemplatesIn(page))
			{
			//get the PBB/xxxx template
			if(!template.getQName().startsWith("Template:PBB/")) continue;
			LOG.info("found PBB templates for  "+page+" "+template);
			
			//save the content of this template
			content= query.getContent(template);
			if(content==null)
				{
				LOG.warning("no template for "+page);
				continue;
				}
			this.qName2wikipedia.put(template.getQName(),content);
			this.qName2boxtemplate.put(page.getQName(), template.getQName());
			
			String omimId = simpleFindField(content, "OMIM");
			if(omimId!=null && Cast.Integer.isA(omimId))
				{
				omim2qname.put(omimId, page.getQName());
				}
			String Hs_EntrezGene= simpleFindField(content, "Hs_EntrezGene");
			if(Hs_EntrezGene!=null)
				{
				entrezGene2qname.put(Hs_EntrezGene, page.getQName());
				}
			
			found=true;
			break;
			}
		
		if(!found)
			{
			LOG.warning("Cannot find PBB for "+page);
			}
		}
	LOG.info("load wikipedia END");
	}

private void loop(File fileout) throws Exception
	{
	MWAuthorization authorization=login();
	
	Set<String> wikipediaPages= qName2wikipedia.getKeySet();
	LOG.info("Start loop");
	int countPageProcessed=0;
	
	//loop over wikipedia
	/*PrintWriter out= new PrintWriter(new FileWriter(fileout));
	out.print("<html><body>"); */
	for(String qName: wikipediaPages)
		{
		if(qName.startsWith("Template:")) continue;
		if(qName.equals("GFER")) continue;//this is my manual test
		
		String templateName= this.qName2boxtemplate.get(qName);
		if(templateName==null)
			{
			LOG.info("No box template for "+qName);
			continue;
			}
		
		String templateContent=  qName2wikipedia.get(templateName);
		
		Document interactor= null;
		
		
		String Hs_EntrezGene = simpleFindField(templateContent, "Hs_EntrezGene");
		if(Cast.Integer.isA(Hs_EntrezGene))
			{			
			BerkeleyDBKey id=shortName2interactor.get("EG"+Hs_EntrezGene);
			
			if(id!=null)
				{
				interactor=biogridDB.get(id);
				if(interactor==null) throw new DatabaseException("Boum "+id);
				}
			}
		
			{
			String omimId = simpleFindField(templateContent, "OMIM");
			if(Cast.Integer.isA(omimId))
				{
				BerkeleyDBKey id = omim2interactor.get(omimId);
				if(id!=null)
					{
					interactor=biogridDB.get(id);
					if(interactor==null) throw new DatabaseException("Boum "+id);
					}
				}
			}
		
		if(interactor==null)
			{
			//System.err.println("Cannot find info for "+templateContent);
			continue;
			}
		
		Attr interactorIdAtt = interactor.getDocumentElement().getAttributeNode("id");
		if(interactorIdAtt==null) throw new RuntimeException("Boumm");
		BerkeleyDBKey interactorID= new BerkeleyDBKey(BioGridKeyType.proteinInteractor,interactorIdAtt.getValue());
		//loop over all the interaction Ids
		List<BerkeleyDBKey> interactionList=interactor2interaction.get(interactorID);
		
		/*
		out.println("<h1><a target='"+ qName+
				"' href='http://en.wikipedia.org/w/index.php?action=edit&title="+
				URLEncoder.encode(qName.replace(' ','_'),"UTf-8")+"'>"+
				qName+"</a></h1>");
		
		
		  StringWriter table= new StringWriter();
		 PrintWriter w= new PrintWriter(table);
		 w.println("==Interactions==");
		 w.println("{| class=\"wikitable\" border=\"1\"");
		 w.println("! Partner !! Method !! References");
		 */
		
	
	
		StringWriter flow= new StringWriter();
		PrintWriter flowriter= new PrintWriter(flow);
		flowriter.append(qName+" has been shown to [[Protein-protein_interaction|interact]] with ");
		
		HashMap<String, Interactor>  partnerId2intractor= new HashMap<String, Interactor>();
		for(BerkeleyDBKey interactionId : interactionList)
			{
			Document interaction= biogridDB.get(interactionId);
			NodeList interactionItemList=(NodeList)findParticipantRef.evaluate(interaction.getDocumentElement(),XPathConstants.NODESET);
			for(int i=0;i< interactionItemList.getLength();++i)
				{
				String partnerId=((Attr)interactionItemList.item(i)).getValue();
				if(partnerId.equals(interactorID.id)) continue;
				Interactor actor= partnerId2intractor.get(partnerId);
				if(actor==null)
					{
					actor= new Interactor();
					partnerId2intractor.put(partnerId,actor);
					}
				actor.interactions.add(interaction);
				}
			}
		boolean foundOne=false;
		Set<String> seenReferences= new HashSet<String>();
		for(String partnerId: partnerId2intractor.keySet())
			{
			Interactor actor= partnerId2intractor.get(partnerId);
			if(actor.interactions.size()<2) continue;
			
			if(foundOne) flowriter.append(", ");
			String interactorQName=proteineId2qName(partnerId);
			if(interactorQName.toLowerCase().startsWith("biogrid-")) continue;
			foundOne=true;
			flowriter.append(interactorQName);
			
			for(Document interaction:actor.interactions)
				{
				/*
				w.println("|-");
				w.print("| "+ proteineId2qName(partnerId));
				String method= (String)xpathFindMethod.evaluate(interaction.getDocumentElement(),XPathConstants.STRING);
				w.print(" || [["+method+"]]");
				Attr expRef= (Attr)xpathFindExperimentRef.evaluate(interaction.getDocumentElement(),XPathConstants.NODE);
				w.print(" || ");
				if(expRef!=null)
					{
					Document experiment= this.biogridDB.get(new BerkeleyDBKey(BioGridKeyType.experimentDescription,expRef.getValue()));
					if(experiment!=null)
						{
						w.print(experiment2anchor(experiment,seenReferences));
						}
					}
				w.println();*/
				
				Attr expRef= (Attr)xpathFindExperimentRef.evaluate(interaction.getDocumentElement(),XPathConstants.NODE);
				if(expRef!=null)
					{
					Document experiment= this.biogridDB.get(new BerkeleyDBKey(BioGridKeyType.experimentDescription,expRef.getValue()));
					if(experiment!=null)
						{
						flowriter.print(experiment2anchor(experiment,seenReferences));
						}
					}
				}
			}
		/*
		w.println("|}");
		w.flush();
		out.println("<pre style='background-color:lightgray;'>"+ XMLUtilities.escape(table.toString())+"</pre>");
		out.flush();
		*/
		flowriter.append(".\n");
		flowriter.flush();
		
		if(foundOne)
			{
			edit(qName, authorization, flow.toString());
			++countPageProcessed;
			if(countPageProcessed==10) break;
			}
		//TODO
		
		
		}
	LOG.info("End  loop");
	/* out.print("</body></html>");
	out.flush();
	out.close();*/
	}

private class Interactor
	{
	List<Document> interactions=new ArrayList<Document>();
	
	}



private String proteineId2qName(String id) throws Exception
	{
	Document dom = biogridDB.get(new BerkeleyDBKey(BioGridKeyType.proteinInteractor,id));
	if(dom==null) return null;
	
	Attr omimAtt=(Attr)xpathFindOmimId.evaluate(dom.getDocumentElement(), XPathConstants.NODE);
	String omim= (omimAtt==null?null:omimAtt.getValue());
	if(omim!=null && Cast.Integer.isA(omim))
		{
		String qName= omim2qname.get(omim);
		if(qName!=null) return "[["+qName+"]]";
		}
	
	
	String shortName=(String)xpathInteractorShortName.evaluate(dom.getDocumentElement(), XPathConstants.STRING);
	if(shortName!=null && shortName.startsWith("EG"))
		{
		shortName = shortName.substring(2);
		String qName= entrezGene2qname.get(shortName);
		if(qName!=null) return "[["+qName+"]]";
		}

	return id;
	}

private String experiment2anchor(Document exp,Set<String> seenRefs)  throws Exception
	{
	Element names= XMLUtilities.firstChild(exp.getDocumentElement(), PSI_NS, "names");
	if(names==null) return "?";
	Element shortLabel=  XMLUtilities.firstChild(names,PSI_NS,"shortLabel");
	Element fullName=  XMLUtilities.firstChild(names,PSI_NS,"fullName");
	if(fullName==null) fullName=shortLabel;
	Element bibRef= XMLUtilities.firstChild(exp.getDocumentElement(), PSI_NS, "bibref");
	Element xref= XMLUtilities.firstChild(bibRef, PSI_NS, "xref");
	Element primaryRef= XMLUtilities.firstChild(xref, PSI_NS, "primaryRef");
	if(!"pubmed".equals(primaryRef.getAttribute("db"))) return fullName.getTextContent();
	String pmid= primaryRef.getAttribute("id");
	if(seenRefs.contains(pmid))
		{
		return "";
		//return XMLUtilities.escape(shortLabel.getTextContent())+"{{ref|pmid"+pmid+"}}";
		}
	seenRefs.add(pmid);
	
	return  /*XMLUtilities.escape(shortLabel.getTextContent())+*/
			"<ref name=pmid"+pmid+">"+
			XMLUtilities.escape(pmid2wiki(pmid))+
			"</ref>"
			;
	/*
	String url="http://www.ncbi.nlm.nih.gov/pubmed/"+primaryRef.getAttribute("id");
	return "<a href=\""+url+"\">"+XMLUtilities.escape(shortLabel.getTextContent()) +" : "+ 
		XMLUtilities.escape(fullName.getTextContent())+
		"</a>";*/
	}

private MWAuthorization login() throws IOException,SAXException
	{
	MWAuthorization authorization= null;
	PostMethod postMethod=null;
	try
		{
		
		File wikipediaCfg= new File(System.getProperty("user.home"),".en.wikipedia.properties");
		if(!wikipediaCfg.exists())
			{
		    throw new IOException("Default params doesn't exists: "+wikipediaCfg);
			}

		Properties properties= new Properties();  
		InputStream in=null;
		in	= new FileInputStream(wikipediaCfg);
		properties.loadFromXML(in);
		in.close();
		if(!properties.containsKey("lgname")) throw new org.lindenb.lang.IllegalInputException("lgname missing");
		if(!properties.containsKey("lgpassword.base64")) throw new org.lindenb.lang.IllegalInputException("lgpassword.base64 missing");

		
		postMethod = new PostMethod(
				"http://en.wikipedia.org/w/api.php"
				);
		
		postMethod.addParameter("action", "login");
		postMethod.addParameter("format", "xml");
		postMethod.addParameter("lgname", properties.getProperty("lgname"));
		postMethod.addParameter("lgpassword",new String(Base64.decode(properties.getProperty("lgpassword.base64"))));
		postMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		
		
		int status = this.httpClient.executeMethod(postMethod);
		if(status==200)
			{
			Document dom= this.documentBuilder.parse(  postMethod.getResponseBodyAsStream());
			Element e= dom.getDocumentElement();
			if(e==null || !e.getTagName().equals("api")) throw new InvalidXMLException(e,"not api");
			e= XMLUtilities.firstChild(e);
			if(e==null || !e.getTagName().equals("login")) throw new InvalidXMLException(e,"not login");
			if(!"Success".equals(e.getAttribute("result"))) throw new InvalidXMLException(e,"not login@result");
			authorization= new MWAuthorization();
			authorization.cookieprefix= e.getAttribute("cookieprefix");
			authorization.lgusername= e.getAttribute("lgusername");
			authorization.lgtoken= e.getAttribute("lgtoken");
			authorization.sessionid= e.getAttribute("sessionid");
			authorization.lguserid= e.getAttribute("lguserid");
			
			}
		else
			{
			throw new IOException("bad http status:"+status);
			}
		
		
		return authorization;
		}
	catch(HttpException  err)
		{
		err.printStackTrace();
		throw err;
		}
	catch(IOException err)
		{
		err.printStackTrace();
		throw err;
		}
	catch(Throwable err)
		{
		err.printStackTrace();
		throw new RuntimeException(err);
		}
	finally
		{
		if(postMethod!=null) postMethod.releaseConnection();
		}
	}

private static final String LEFT_COMMENT="<!-- BOT-BEGIN-INTERACTION-BOX. (please do not remove that flag) -->\n";
private static final String RIGHT_COMMENT="\n<!-- BOT-END-INTERACTION-BOX. (please do not remove that flag) -->";
private void edit(String page,MWAuthorization authorization,String text) throws IOException,SAXException
	{
	Pattern referencesPattern = Pattern.compile("[=]+[ ]*reference[s]?[ ]*[=]+", Pattern.CASE_INSENSITIVE);
	PostMethod postMethod=null;
	GetMethod getMethod= null;

	try
		{
		
		getMethod= new GetMethod(
				"http://en.wikipedia.org/w/api.php?action=query" +
				"&format=xml&intoken=edit" +
				"&prop="+URLEncoder.encode("info|revisions","UTF-8") +
				"&titles="+URLEncoder.encode(page.replace(' ', '_'),"UTF-8")+
				"&rvprop="+URLEncoder.encode("timestamp|content|revisions","UTF-8")
				);
		
		
		int status = this.httpClient.executeMethod(getMethod);
		if(status!=200)
			{
			System.err.println("Cannot send get method ");
			}
		InputStream in= getMethod.getResponseBodyAsStream();
		Document dom= documentBuilder.parse(in);
		in.close();
		Element api=dom.getDocumentElement();
		if(api==null) throw new IOException("no root");
		Element queryTag = XMLUtilities.firstChild(api, "query");
		if(queryTag==null) throw new IOException("no query");
		Element pages = XMLUtilities.firstChild(queryTag, "pages");
		if(pages==null) throw new IOException("no pages");
		Element pageTag = XMLUtilities.firstChild(pages, "page");
		if(pageTag==null) throw new IOException("no page");
		String token= pageTag.getAttribute("edittoken");
		
		String starttimestamp=pageTag.getAttribute("starttimestamp");
		Element revisions=  XMLUtilities.firstChild(pageTag, "revisions");
		if(revisions==null) throw new IOException("no revisions");
		Element rev=  XMLUtilities.firstChild(revisions, "rev");
		if(rev==null) throw new IOException("no rev");
		String basetimestamp=rev.getAttribute("timestamp");
		String content=rev.getTextContent();
		
		postMethod = new PostMethod(
				"http://en.wikipedia.org/w/api.php"
				);
		
		Matcher matcher= referencesPattern.matcher(content);
		
		int leftIndex= content.indexOf(LEFT_COMMENT);
		int rightIndex=-1;
		if(leftIndex!=-1)
			{
			rightIndex = content.indexOf(RIGHT_COMMENT,leftIndex+1);
			}
		
		if(rightIndex!=-1)
			{
			String old = content.substring(leftIndex+LEFT_COMMENT.length(),rightIndex);
			if(old.equals(text))
				{
				System.err.println("text didn't changed for "+page);
				return;
				}
			content= content.substring(0,leftIndex)+
					LEFT_COMMENT+ text+RIGHT_COMMENT+
					content.substring(rightIndex+RIGHT_COMMENT.length());
			}
		else if(matcher.find())
			{
			int n= matcher.start();
			content = content.substring(0,n)+
					"\n==Interactions==\n"+
					LEFT_COMMENT+ text+RIGHT_COMMENT+
					content.substring(n);
			}
		else
			{	
			System.err.println("Cannot process "+page+" "+content);
			return;
			}
		
		//postMethod.addParameter("bot", "true");
		postMethod.addParameter("action","edit");
		postMethod.addParameter("title",page.replace(' ', '_'));
		postMethod.addParameter("summary","updating interactions");
		postMethod.addParameter("text",content);
		postMethod.addParameter("basetimestamp",basetimestamp);
		postMethod.addParameter("starttimestamp",starttimestamp);
		postMethod.addParameter("token",token);
		postMethod.addParameter("notminor","");
		
		
		
		status = this.httpClient.executeMethod(postMethod);
		if(status==200)
			{
			
			System.out.println("Done: "+page+"\n");
			}
		else
			{
			throw new IOException("bad http status:"+status);
			}
		}
	catch(HttpException  err)
		{
		throw err;
		}
	catch(IOException err)
		{
		throw err;
		}
	finally
		{
		if(getMethod!=null) getMethod.releaseConnection();
		if(postMethod!=null) postMethod.releaseConnection();
		}
	}


public static void main(String[] args) {
	try {
		int optind=0;
		String program=null;
		File biogridFile=null;
		File fileout=null;
		Interactome01 app= new Interactome01();
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println("-h this screen");
				System.err.println("-biogrid <biogrid-file>");
				System.err.println("-o <out-file>");
				System.err.println("-p <program>");
				System.err.println("   'wikipedia' load the wikipedia page for GeneWiki");
				System.err.println("   'biogrid' load the biogrid file");
				return;
				}
			else if (args[optind].equals("-p"))
			     {
				 program=args[++optind];
			     }
				else if (args[optind].equals("-o"))
			     {
				fileout=new File(args[++optind]);
			     }
			else if (args[optind].equals("-biogrid"))
			     {
				biogridFile=new File(args[++optind]);
			     }
			 else if (args[optind].equals("--"))
			     {
			     ++optind;
			     break;
			     }
			else if (args[optind].startsWith("-"))
			     {
			     System.err.println("bad argument " + args[optind]);
			     System.exit(-1);
			     }
			else
			     {
			     break;
			     }
			++optind;
			}
	    if(program==null)
	    	{
	    	System.err.println("program missing");
	    	return;
	    	}
	    if(program.equals("wikipedia"))
		    {
		    app.open();
		    app.loadWikipedia(false);
		    app.close();
		    }
	    //-p biogrid -biogrid /home/lindenb/BIOGRID-ALL-SINGLEFILE-2.0.53.psi.xml
	    else if(program.equals("biogrid"))
		    {
	    	 if(biogridFile==null)
		    	{
		    	System.err.println("biogrid File missing");
		    	return;
		    	}
	    	LOG.info("start parsing "+biogridFile);
		    app.open();
		    app.parseBiogrid(biogridFile);
		    app.close();
		    LOG.info("end parsing "+biogridFile);
		    }
	    else if(program.equals("loop"))
	    	{
	    	 if(fileout==null)
		    	{
		    	System.err.println("fileout File missing");
		    	return;
		    	}
	    	app.open();
			app.loop(fileout);
			app.close();
	    	}
	    else
	    	{
	    	System.err.println("unknown program "+program);
	    	return;
	    	}
	  
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
