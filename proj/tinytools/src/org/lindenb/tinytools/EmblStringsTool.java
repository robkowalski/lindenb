package org.lindenb.tinytools;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.berkeley.DocumentBinding;
import org.lindenb.lang.IllegalInputException;
import org.lindenb.util.Compilation;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;



public class EmblStringsTool
{
private static final String NS="net:sf:psidev:mi";
private Environment environment=null;
private Database protein2psixml=null;
private Database protein2protein=null;
private DocumentBuilder domBuilder=null;
private static Logger _log = Logger.getLogger(EmblStringsTool.class.getName());
private DocumentBinding documentBinding;



private void init(File directory) throws IOException, DatabaseException
	{
	try {
		DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
		domFactory.setCoalescing(true);
		domFactory.setExpandEntityReferences(true);
		domFactory.setIgnoringComments(true);
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		domFactory.setNamespaceAware(true);
		this.domBuilder= domFactory.newDocumentBuilder();
	
		
		}	

	catch (Exception e)
		{
		throw new RuntimeException(e);
		}
	
	EnvironmentConfig envcfg= new EnvironmentConfig();
	envcfg.setAllowCreate(true);
	envcfg.setReadOnly(false);
	this.environment= new Environment(directory,envcfg);
	DatabaseConfig cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	
	this.protein2psixml= this.environment.openDatabase(null, "protein2psixml", cfg);
	cfg= new DatabaseConfig();
	cfg.setAllowCreate(true);
	cfg.setReadOnly(false);
	cfg.setSortedDuplicates(true);
	this.protein2protein= this.environment.openDatabase(null, "protein2protein", cfg);
	}

private Logger log()
	{
	return _log;
	}

private void close() 
	{
	if(this.protein2protein!=null)
		{
		try { this.protein2protein.close(); } catch(Exception err) {}
		this.protein2protein=null;
		}
	if(this.protein2psixml!=null)
		{
		try { this.protein2psixml.close(); } catch(Exception err) {}
		this.protein2psixml=null;
		}
	if(this.environment!=null)
		{
		try { this.environment.close(); } catch(Exception err) {}
		this.environment=null;
		}
	}

private int getMaxDepth()
	{
	return 2;
	}

private Document getPsiXml(String identifier)
throws DatabaseException
	{
	DatabaseEntry key=new DatabaseEntry();
	DatabaseEntry data=new DatabaseEntry();
	StringBinding.stringToEntry(identifier, key);
	if(this.protein2psixml.get(null, key, data, LockMode.DEFAULT)!=OperationStatus.SUCCESS) return null;
	return this.documentBinding.entryToObject(data);
	}

private boolean contains(String identifier)
	throws DatabaseException
	{
	DatabaseEntry key=new DatabaseEntry();
	DatabaseEntry data=new DatabaseEntry();
	StringBinding.stringToEntry(identifier, key);
	return(this.protein2psixml.get(null, key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS);
	}

private void scan(String identifier,int depth)
	throws IOException,DatabaseException
	{
	log().info("Scanning "+identifier);

	if(contains(identifier))
		{
		log().info("Already processed: "+identifier);
		return;
		}
	String url="http://string.embl.de/api/psi-mi/interactions?identifier="+URLEncoder.encode(identifier, "UTF-8");
	log().info(url);
	Document dom=null;
	try
		{
		dom= this.domBuilder.parse(url);
		}
	catch(Exception err)
		{
		log().severe("Cannot get "+url+" "+err.getMessage());
		return;
		}
	
	log().info("Inserting: "+identifier);
	DatabaseEntry key=new DatabaseEntry();
	DatabaseEntry data=new DatabaseEntry();
	documentBinding.objectToEntry(dom, data);
	if(this.protein2psixml.put(null, key, data)!=OperationStatus.SUCCESS)
		{
		throw new DatabaseException("Cannot insert psi/xml for "+identifier);
		}
	
	if(depth> getMaxDepth())
		{
		//skip children
		log().info("Max depth reached");
		return;
		}
	log().info("Searching interactions");
	
	Element root= dom.getDocumentElement();
	verify(root!=null);
	if(XMLUtilities.isA(root, NS, "entrySet")) throw new IllegalInputException("root not entry Set");
	Element entry=XMLUtilities.firstChild(root,NS, "entry");
	verify(entry!=null);
	Element interactorList=XMLUtilities.firstChild(entry,NS, "interactorList");
	verify(interactorList!=null);
	Map<String,Element> id2interactor= new HashMap<String, Element>();
	Set<String> remaining= new HashSet<String>();
	
	for(Element interactor: XMLUtilities.elements(interactorList, NS, "interactor"))
		{
		Attr att= interactor.getAttributeNode("id");
		verify(att!=null);
		String id=att.getValue();
		id2interactor.put(id, interactor);
		}
	Element interactionList=XMLUtilities.firstChild(entry,NS, "interactionList");
	verify(interactionList!=null);
	for(Element interaction: XMLUtilities.elements(interactionList, NS, "interaction"))
		{
		Element participantList= XMLUtilities.firstChild(interaction,NS, "participantList");
		verify(participantList!=null);
		List<Element> participants= XMLUtilities.elements(participantList,NS, "participant");
		verify(participants.size()==2);
		String identifiers[]=new String[2];
		for(int i=0;i< 2;++i)
			{
			Element interactorRef = XMLUtilities.firstChild(participants.get(i),NS, "interactorRef");
			verify(interactorRef!=null);
			String content= interactorRef.getTextContent();
			Element interactor=id2interactor.get(content);
			verify(interactor!=null);
			identifiers[i]=getEnsemblIdentifier(interactor);
			
			
			}
		if(identifiers[0].equalsIgnoreCase(identifier) ||
		   identifiers[1].equalsIgnoreCase(identifier)
				)
			{
			putInteraction(identifiers[0],identifiers[1]);
			
			for(String ensembl:identifiers)
				{
				if(ensembl.equalsIgnoreCase(identifier)) continue;
				if(contains(ensembl)) continue;
				remaining.add(ensembl);
				}
			}
		}
	for(String ensembl:remaining)
		{
		scan(ensembl, depth+1);
		}
	}

private void walkNetwork(String identifier,int depth)throws DatabaseException
	{
	Cursor c=null;
	try
		{
		DatabaseEntry key= new DatabaseEntry();
		DatabaseEntry data= new DatabaseEntry();
		StringBinding.stringToEntry(identifier, key);
		c=protein2protein.openCursor(null, null);
		boolean first=true;
		while(true)
			{
			OperationStatus status= (first?
				c.getSearchKey(key, data, LockMode.DEFAULT):
				c.getSearchKey(key, data, LockMode.DEFAULT)
				);
			if(status!=OperationStatus.SUCCESS) break;
			
			}
		}
	catch(DatabaseException err)
		{
		throw err;
		}
	finally
		{
		if(c!=null) try{c.close();} catch(Exception err) {} 
		c=null;
		}
	}


private void putInteraction(String p1,String  p2)
	throws DatabaseException
	{
	DatabaseEntry key=new DatabaseEntry();
	DatabaseEntry data=new DatabaseEntry();
	StringBinding.stringToEntry(p1, key);
	StringBinding.stringToEntry(p2, data);

	protein2protein.putNoDupData(null, key, data);
	protein2protein.putNoDupData(null, data, key);
	}

private static String getEnsemblIdentifier(Element interactor)
	{
	Element xref=XMLUtilities.firstChild(interactor,NS,"xref");
	verify(xref!=null);
	Element primaryRef=XMLUtilities.firstChild(xref,NS,"primaryRef");
	verify(primaryRef!=null);
	Attr att= primaryRef.getAttributeNode("id");
	verify(att!=null);
	return att.getValue().toUpperCase();
	}

private static void verify(boolean b)
	{
	if(!b) throw new AssertionError("Assertion failed");
	}
	
public static void main(String[] args)
	{
	try {
		File directory= new File(System.getProperty("java.io.tmpdir", "/tmp"));
		EmblStringsTool app= new EmblStringsTool();
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD."+Compilation.getLabel());
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
    try {
		Set<String> identifiers= new HashSet<String>();
		
		while(optind< args.length)
			{
			identifiers.add(args[optind++].toUpperCase());
			}
		if(identifiers.isEmpty())
			{
			System.err.println("Identifiers are missing");
			}
		for(String id: identifiers)
			{
			app.scan(id,0);
			}
		app.init(directory);
	} catch (Exception e) {
		e.printStackTrace();
		}
	finally
		{
		if(app!=null) app.close();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
