package org.gnf.interactome;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
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
import org.lindenb.util.StringUtils;
import org.lindenb.wikipedia.api.Entry;
import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.xml.NamespaceContextImpl;
import org.lindenb.xml.Sax2Dom;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sleepycat.bind.tuple.StringBinding;
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
private static long ID_GENERATOR=System.currentTimeMillis();
private File envHome;
private Environment berkeleyEnv;
private Database biogridDB;
private Database interactor2interaction;
private DocumentBuilder documentBuilder;
private DocumentBinding documentBinding;
private Transformer transformer;
private XPath xpath;
private XMLInputFactory xmlInputFactory;

static private enum BioGridKeyType
	{
	proteinInteractor,
	interaction,
	experimentDescription,
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
	
	BerkeleyDBKey(DatabaseEntry entry) throws IOException
		{
		TupleInput input= new TupleInput(entry.getData(),entry.getOffset(),entry.getSize());
		this.type= BioGridKeyType.values()[input.readInt()];
		this.id= input.readString();
		input.close();
		}
	
	public DatabaseEntry toEntry() throws IOException
		{
		TupleOutput out= new TupleOutput();
		out.writeInt(this.type.ordinal());
		out.writeString(this.id);
		out.close();
		return new DatabaseEntry(out.toByteArray());
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
	
	this.xmlInputFactory = XMLInputFactory.newInstance();
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
	}

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
	this.biogridDB= this.berkeleyEnv.openDatabase(null, "biogrid", cfg);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(true);
	interactor2interaction= this.berkeleyEnv.openDatabase(null, "interactor2interaction", cfg);
	}


private void close()throws DatabaseException
	{
	if(this.interactor2interaction!=null) { try { this.interactor2interaction.close();this.interactor2interaction=null;} catch(Exception err) { }}
	if(this.biogridDB!=null) { try { this.biogridDB.close();this.biogridDB=null;} catch(Exception err) { }}
	if(this.berkeleyEnv!=null) { try { this.berkeleyEnv.close();this.berkeleyEnv=null;} catch(Exception err) { }}
	}

private void parseBiogrid(File f) throws IOException,SAXException,DatabaseException,ParserConfigurationException
	{
	DatabaseEntry key= new DatabaseEntry();
	DatabaseEntry data= new DatabaseEntry();
	
	//clear
	Cursor c= this.biogridDB.openCursor(null, null);
	while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
		{
		c.delete();
		}
	c.close();
	SAXParserFactory saxFactory= SAXParserFactory.newInstance();
	saxFactory.setNamespaceAware(true);
	saxFactory.setValidating(false);

	SAXParser parser= saxFactory.newSAXParser();
	parser.parse(f, new Sax2Dom(this.documentBuilder)
			{
			@Override
			public void endElement(String uri, String localName, String name)
					throws SAXException {
				
				if(super.currentNode.getNodeType()==Node.ELEMENT_NODE &&
					XMLUtilities.getLevel(super.currentNode)==4 &&
					!StringUtils.isIn(super.currentNode.getParentNode().getLocalName(),"source","availabilityList")
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
							XPathExpression expr=xpath.compile("psi:participantList/psi:proteinParticipant/psi:proteinInteractorRef/@ref");
							NodeList list=(NodeList)expr.evaluate(currentNode,XPathConstants.NODESET);
							
							
							for(int i=0;i< list.getLength();++i)
								{
								DatabaseEntry key=new DatabaseEntry();
								DatabaseEntry value=new DatabaseEntry();
								StringBinding.stringToEntry(Attr.class.cast(list.item(i)).getValue(), key);
								StringBinding.stringToEntry(id, value);
								if(interactor2interaction.put(null, key, value)!=OperationStatus.SUCCESS)
									{
									throw new IOException("cannot insert i2i");
									}
								}
							}
						else
							{
							Attr att=(Attr)this.currentNode.getAttributes().getNamedItem("id");
							if(att==null) throw new SAXException("Cannot get id in "+name);
							bdbKey = new BerkeleyDBKey(type,att.getValue());
							}
						
						
						DatabaseEntry key= bdbKey.toEntry();
						DatabaseEntry value= new DatabaseEntry();
						DOMResult result= new DOMResult(documentBuilder.newDocument());
						transformer.transform(new DOMSource(this.currentNode),result);
						documentBinding.objectToEntry((Document)result.getNode(), value);
						
						if(biogridDB.put(null, key, value)!=OperationStatus.SUCCESS)
							{
							throw new IOException("cannot insert "+bdbKey);
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
			});
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
	return wikiText.substring(i,j).trim();
	}
Document findProteinByOmimId(String omimId) throws Exception
	{
	XPathExpression expr=this.xpath.compile("/psi:proteinInteractor/psi:xref/psi:primaryRef[@db=\"MIM\" and @id=\""+omimId+"\"]");
	Document doc=null;
	DatabaseEntry key= new DatabaseEntry();
	DatabaseEntry data= new DatabaseEntry();
	Cursor c= this.biogridDB.openCursor(null, null);
	while(c.getNext(key, data, null)==OperationStatus.SUCCESS)
		{
		doc= this.documentBinding.entryToObject(data);
		Element e= doc.getDocumentElement();
		if(e==null) continue;
		if(!e.getLocalName().equals("proteinInteractor")) continue;
		Node n=(Node)expr.evaluate(doc, XPathConstants.NODE);
		if(n!=null)
			{
			System.err.println("Ok found "+omimId);
			break;
			}
		doc=null;
		}
	c.close();
	return doc;
	}

private void loop() throws Exception
	{
	MWQuery query= new MWQuery();
	for(Page page:query.listPagesEmbedding(new Page("Template:PBB")))
		{
		System.err.println(page+"\n");
		for(Page template:query.listTemplatesIn(page))
			{
			if(!template.getQName().startsWith("Template:PBB/")) continue;
			String content= query.getContent(template);
			String omimId= simpleFindField(content,"OMIM");
			if(omimId==null)
				{
				System.err.println("No OmimId for "+page);
				continue;
				}
			System.err.println(findProteinByOmimId(omimId));
			}
		}
	}

public static void main(String[] args) {
	try {
		int optind=0;
		Interactome01 app= new Interactome01();
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println("-h this screen");
				return;
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
	    app.open();
	    app.loop();
	    //app.parseBiogrid(new File("/home/lindenb/BIOGRID-ALL-SINGLEFILE-2.0.53.psi.xml"));
	    app.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
