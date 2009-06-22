package org.gnf.interactome;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.lindenb.berkeley.DocumentBinding;
import org.lindenb.io.IOUtils;
import org.lindenb.util.Cast;
import org.lindenb.util.StringUtils;

import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;

import org.lindenb.xml.NamespaceContextImpl;
import org.lindenb.xml.Sax2Dom;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
private DocumentBuilder documentBuilder;
private DocumentBinding documentBinding;
private Transformer transformer;
private XPath xpath;
private XMLInputFactory xmlInputFactory;

private XPathExpression xpathFindOmimId=null;
private XPathExpression xpathInteractorShortName=null;
private XPathExpression findParticipantRef=null;

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
		
		if(c.getNext(key, value, null)!=OperationStatus.SUCCESS) return list;
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
					bdbKey = new BerkeleyDBKey(type,id);
					
					NodeList list=(NodeList)findParticipantRef.evaluate(currentNode,XPathConstants.NODESET);
					if(list.getLength()!=2) throw new SAXException("Boum");
					
					for(int i=0;i< list.getLength();++i)
						{
						interactor2interaction.put(
							new BerkeleyDBKey(BioGridKeyType.proteinInteractor,Attr.class.cast(list.item(i)).getValue()),
							bdbKey)
							;
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
	this.envHome= new File(System.getProperty("java.io.tmpdir"),"gnf01");
	
	
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
	
	this.transformer =tFactory.newTransformer();
	this.transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	
	XPathFactory xpathFactory=XPathFactory.newInstance();
	this.xpath=xpathFactory.newXPath();
	NamespaceContextImpl ctx=new NamespaceContextImpl();
	ctx.setPrefixURI("psi","net:sf:psidev:mi");
	this.xpath.setNamespaceContext(ctx);
	this.findParticipantRef=xpath.compile("psi:participantList/psi:proteinParticipant/psi:proteinInteractorRef/@ref");
	this.xpathFindOmimId =xpath.compile("psi:xref[1]/psi:primaryRef[@db='MIM']/@id");
	this.xpathInteractorShortName= xpath.compile("psi:names[1]/psi:shortLabel[1]/text()");
	
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
	if(this.berkeleyEnv!=null) { try { this.berkeleyEnv.close();this.berkeleyEnv=null;} catch(Exception err) { }}
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
private void loadWikipedia() throws Exception
	{
	LOG.info("load wikipedia");
	qName2wikipedia.clear();
	qName2boxtemplate.clear();
	omim2qname.clear();
	entrezGene2qname.clear();
	
	MWQuery query= new MWQuery();
	//get all the pages aving a Template:PBB
	for(Page page:query.listPagesEmbedding(new Page("Template:PBB")))
		{
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
	Set<String> wikipediaPages= qName2wikipedia.getKeySet();
	LOG.info("Start loop");
	//loop over wikipedia
	PrintWriter out= new PrintWriter(new FileWriter(fileout));
	for(String qName: wikipediaPages)
		{
		if(qName.startsWith("Template:")) continue;
		
		
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
		
		
		out.println("<h1>"+qName+"</h1>");
		out.println("<table>");
		out.println("<tr><th>Partner</th><th>Method</th><th>Reference</th></tr>");
		for(BerkeleyDBKey interactionId : interactionList)
			{
			Document interaction= biogridDB.get(interactionId);
			//get the interactors in this interaction
			NodeList interactionItemList=(NodeList)findParticipantRef.evaluate(interaction.getDocumentElement(),XPathConstants.NODESET);
			//loop over all the intectors this interaction
			for(int i=0;i< interactionItemList.getLength();++i)
				{
				String partnerId=((Attr)interactionItemList.item(i)).getValue();
				if(partnerId.equals(interactorID)) continue;
				out.print("<tr>");
				out.print("<td>"+ proteineId2qName(partnerId)+"</td>");
				out.print("<td></td>");
				out.print("<td></td>");
				out.println("</tr>");
				}
			
			}
		out.println("</table>");
		out.flush();
		}
	LOG.info("End  loop");
	out.flush();
	out.close();
	}

private String proteineId2qName(String id) throws Exception
	{
	Document dom = biogridDB.get(new BerkeleyDBKey(BioGridKeyType.proteinInteractor,id));
	if(dom==null) return null;
	String shortName=(String)xpathInteractorShortName.evaluate(dom.getDocumentElement(), XPathConstants.STRING);
	Attr omimAtt=(Attr)xpathFindOmimId.evaluate(dom.getDocumentElement(), XPathConstants.NODE);
	String omim= (omimAtt==null?null:omimAtt.getValue());
	
	
	return id;
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
		    app.loadWikipedia();
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
