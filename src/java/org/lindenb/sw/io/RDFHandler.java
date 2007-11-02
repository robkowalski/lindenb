package org.lindenb.sw.io;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;
import org.lindenb.xml.XMLUtilities;
import static org.lindenb.util.Debug.debug;


/**
 * RDFHandler
 * @author Pierre Lindenbaum PhD plindenbaum@yahoo.fr
 *
 */
public class RDFHandler
	{
	private static long ID_GENERATOR = System.currentTimeMillis();
	private XMLInputFactory factory=null;
	private XMLEventReader parser=null;
	private URI base=null;

	
	private static class RDFEvent
		{
		URI subject=null;
		URI predicate=null;
		Object value=null;
		URI valueType=null;
		String lang=null;
		int listIndex=-1;
		
		

		public void write(PrintWriter out)
			{
			out.println("<rdf:Description>");
			out.println("\t<rdf:type rdf:resource=\""+ RDF.NS+"Statement\"/>");
			
			if(subject.isAbsolute() ||
					subject.toString().length()==0)
				{
				out.println("\t<rdf:subject rdf:resource=\""+
						escapeXML(subject.toString()) +
						"\"/>");
				}
			else
				{
				out.println("\t<rdf:subject rdf:nodeID=\""+
						escapeXML(subject.toString()) +
						"\"/>");
				}
			
			if(predicate.isAbsolute() ||
					predicate.toString().length()==0)
				{
				out.println("\t<rdf:predicate rdf:resource=\""+
						escapeXML(predicate.toString())
						+"\"/>");
				}
			else
				{
				out.println("\t<rdf:predicate rdf:nodeID=\""+
						escapeXML(predicate.toString()) +
						"\"/>");
				}
			
			out.print("\t<rdf:object ");
			
			
			
			if(value instanceof URI)
				{
				URI val=URI.class.cast(value);
				if(val.isAbsolute() ||
					val.toString().length()==0)
					{
					out.println("rdf:resource=\""+ escapeXML(val.toString()) +"\"/>");
					}
				else
					{
					out.println("rdf:nodeID=\""+ escapeXML(val.toString()) +"\"/>");
					}
				}
			else
				{
				if(this.valueType!=null)
					{
					out.print("rdf:datatype=\""+ 
							escapeXML(this.valueType.toString()) +
							"\" ");
					}
				if(this.lang!=null)
					{
					out.print("xml:lang=\""+ this.lang +"\" ");
					}
				out.println(">"+ escapeXML(value.toString()) +"</rdf:object>");
				}
			out.println("</rdf:Description>");
			}
		
		
		@Override
		public String toString()
			{
			StringWriter b= new StringWriter();
			write(new PrintWriter(b,true));
			return b.toString();
			}
		
		}
	
	
	/**
	 * constructor
	 * initialize the XMLInputFactory
	 */
	public RDFHandler()
		{
		this.factory = XMLInputFactory.newInstance();
		this.factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		this.factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		this.factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		}
	
	/**
	 * called when this handler finds a new RDF triple
	 * This method should be overiden
	 * @param event
	 */
	protected void found(
			RDFEvent event
			)
		{
		found(event.subject,event.predicate,event.value,event.valueType,event.lang,event.listIndex);
		}
	
	
	public void found(
			@SuppressWarnings("unused")URI subject,
			@SuppressWarnings("unused")URI predicate,
			@SuppressWarnings("unused")Object value,
			@SuppressWarnings("unused")URI dataType,
			@SuppressWarnings("unused")String lang,
			@SuppressWarnings("unused")int index)
		{
		
		}
	
	
	
	public void setBase(URI base) {
		this.base = base;
		}

	/** return XMLEventReader */
	protected XMLEventReader getReader()
		{
		return this.parser;
		}
	
	
	
	/** do the parsing */
	private void read(Reader in) throws XMLStreamException
		{
		try {
			/** create a XML Event parser */
			this.parser = this.factory.createXMLEventReader(in);
			/** loop until we find a rdf:RDF element */
			while(getReader().hasNext())
				{
				XMLEvent event = getReader().nextEvent();
				if(event.isStartElement())
					{
					StartElement start=(StartElement)event;
					debug("found start "+start);
					if(name2string(start).equals(RDF.NS+"RDF"))
						{
						debug("found RDF");
						parseRDF();
						}
					}
				}
			}
		catch (URISyntaxException e)
			{
			this.parser=null;
			throw new XMLStreamException(e);
			}
		this.parser=null;
		}

private void parseRDF() throws XMLStreamException,URISyntaxException
	{
	debug("In RDF ROOT: loop over each rdf:Description");
	while(getReader().hasNext())
		{
		XMLEvent event = getReader().nextEvent();
		
		if(event.isEndElement())
			{
			return;
			}
		else if(event.isStartElement())
			{
			parseDescription(event.asStartElement());
			}
		else if(event.isProcessingInstruction())
			{
			throw new XMLStreamException("Found Processing Instruction in RDF ???");
			}
		else if(event.isCharacters() &&
				event.asCharacters().getData().trim().length()>0)
			{
			throw new XMLStreamException("Found text in RDF ???");
			}
		}
	}

/**
 * Parse description of a Resource
 * @param description
 * @return
 * @throws URISyntaxException
 * @throws XMLStreamException
 */
private URI parseDescription(StartElement description) throws URISyntaxException,XMLStreamException
	{
	debug("Found a new  rdf:Description "+description.getName());
	URI descriptionURI=null;
	Attribute att= description.getAttributeByName(new QName(RDF.NS,"about"));
	if(att!=null) descriptionURI= createURI( att.getValue());
	if(descriptionURI==null)
		{
		att= description.getAttributeByName(new QName(RDF.NS,"nodeID"));
		if(att!=null) descriptionURI=  createURI( att.getValue());
		}
	if(descriptionURI==null)
		{
		att= description.getAttributeByName(new QName(RDF.NS,"ID"));
		if(att!=null) descriptionURI= resolveBase(att.getValue());
		}
	
	if(descriptionURI==null)
		{
		descriptionURI= createAnonymousURI();
		}
	
	debug("Description uri=\""+descriptionURI+"\"");
	
	QName qn=description.getName();
	if(!(qn.getNamespaceURI().equals(RDF.NS) &&
		 qn.getLocalPart().equals("Description")))
		{
		RDFEvent evt= new RDFEvent();
		evt.subject=descriptionURI;
		evt.predicate= createURI(RDF.NS+"type");
		evt.value=name2uri(qn);
		found(evt);
		}

	/** loop over attributes */
	for(Iterator<?> i=description.getAttributes();
		i.hasNext();)
		{
		att=(Attribute)i.next();
		qn= att.getName();
		String local= qn.getLocalPart();
		if(qn.getNamespaceURI().equals(RDF.NS) &&
			( local.equals("about") ||
				local.equals("ID") ||
				local.equals("nodeID")))
				{
				continue;
				}
		debug("found Attribute;"+att);
		RDFEvent evt= new RDFEvent();
		evt.subject=descriptionURI;
		evt.predicate= name2uri(qn);
		evt.value= att.getValue();
		found(evt);
		}
	
	
	while(getReader().hasNext())
		{
		XMLEvent event = getReader().nextEvent();
		
		if(event.isEndElement())
			{
			return descriptionURI;
			}
		else if(event.isStartElement())
			{
			parsePredicate(descriptionURI,event.asStartElement());
			}
		else if(event.isProcessingInstruction())
			{
			throw new XMLStreamException("Found Processing Instruction in RDF ???");
			}
		else if(event.isCharacters() &&
				event.asCharacters().getData().trim().length()>0)
			{
			throw new XMLStreamException("Found text in RDF ??? \""+
				event.asCharacters().getData()+"\""	
				);
			}
		}

	return descriptionURI;
	}

/**
 * parse predicate
 * @param descriptionURI
 * @param predicate 
 * @throws URISyntaxException
 * @throws XMLStreamException
 */
private void parsePredicate(URI descriptionURI,StartElement predicate) throws URISyntaxException,XMLStreamException
	{
	String parseType=null;
	String lang=null;
	URI datatype=null;
	Attribute att;
	QName qn=null;
	URI resource=null;

	URI predicateURI=name2uri(predicate.getName());
	debug("parse rdf:description=\""+descriptionURI+"\" predicate:"+predicateURI);
	
	/** collect attributes */
	for(int loop=0;loop<2;++loop)
		{
		for(Iterator<?> i=predicate.getAttributes();
			i.hasNext();)
			{
			att=(Attribute)i.next();
			qn= att.getName();
			String local= qn.getLocalPart();
			if(qn.getPrefix().equals("xml") &&
				local.equals("lang"))
				{
				if(loop==0) lang=att.getValue();
				continue;
				}
			else if(qn.getNamespaceURI().equals(RDF.NS))
				{
				if(local.equals("parseType"))
					{
					if(loop==0)  parseType=att.getValue();
					debug("parseType:"+parseType);
					continue;
					}
				else if(local.equals("datatype"))
					{
					if(loop==0) datatype= createURI(att.getValue());
					debug("dataType="+datatype);
					continue;
					}
				else if(local.equals("resource"))
					{
					if(loop==0) resource=  createURI(att.getValue());
					debug("rdf:resource="+resource);
					continue;
					}
				else if(local.equals("nodeID"))
					{
					if(loop==0) resource=  createURI(att.getValue());
					debug("rdf:nodeID="+resource);
					continue;
					}
				else if(local.equals("ID"))
					{
					if(loop==0) resource= resolveBase(att.getValue());
					debug("ID="+resource);
					continue;
					}
				}
			
			if(loop==1)
				{
				if(resource!=null)
					{
					debug(resource);
					RDFEvent evt= new RDFEvent();
					evt.subject= resource;
					evt.predicate= name2uri(att.getName());
					evt.value=att.getValue();
					found(evt);
					}
				else
					{
					throw new XMLStreamException("Cannot handle attribute "+att);
					}
				}
			
			}
		}
	
	
	
	
	if(resource!=null)
		{
		RDFEvent evt= new RDFEvent();
		evt.subject=descriptionURI;
		evt.predicate= predicateURI;
		evt.value=resource;
		found(evt);
		debug();
		XMLEvent event=getReader().peek();
		if(event!=null && event.isEndElement())
			{
			debug();
			getReader().nextEvent();
			return;
			}
		
		throw new XMLStreamException("Expected a EndElement for this element");
		}
	
	if(parseType==null) parseType="default";
	
	if(parseType.equals("Literal"))
		{
		StringBuilder b= parseLiteral();
		
		RDFEvent evt= new RDFEvent();
		evt.subject=descriptionURI;
		evt.predicate= predicateURI;
		evt.value= b.toString();
		evt.lang=lang;
		evt.valueType=datatype;
		found(evt);
		}
	else if(parseType.equals("Resource"))
		{
		URI blanck = createAnonymousURI();
		
		RDFEvent evt= new RDFEvent();
		evt.subject=descriptionURI;
		evt.predicate= predicateURI;
		evt.value=blanck;
		evt.lang=lang;
		evt.valueType=datatype;
		
		
		
		found(evt);
		while(getReader().hasNext())
			{
			XMLEvent event = getReader().nextEvent();
			if(event.isStartElement())
				{
				parsePredicate(blanck, event.asStartElement());
				}
			else if(event.isEndElement())
				{
				return;
				}
			}
		
		}
	else  if(parseType.equals("Collection"))
		{
		int index=0;
		while(getReader().hasNext())
			{
			XMLEvent event = getReader().nextEvent();
			if(event.isStartElement())
				{
				URI value= parseDescription(event.asStartElement());
				
				RDFEvent evt= new RDFEvent();
				evt.subject=descriptionURI;
				evt.predicate= predicateURI;
				evt.value=value;
				evt.lang=lang;
				evt.valueType=datatype;
				evt.listIndex=(++index);
				
				found(evt);
				}
			else if(event.isEndElement())
				{
				return;
				}
			}
		}
	else
		{
		boolean foundResourceAsChild=false;
		StringBuilder b= new StringBuilder();
		while(getReader().hasNext())
			{
			XMLEvent event = getReader().nextEvent();
			if(event.isStartElement())
				{
				if(b.toString().trim().length()!=0)
					{
					throw new XMLStreamException(
							"Bad text \""+b+"\" before "+
							event.asStartElement().getName()
							);
					}
				URI childURI=parseDescription(event.asStartElement());
				RDFEvent evt= new RDFEvent();
				evt.subject=descriptionURI;
				evt.predicate= predicateURI;
				evt.value= childURI;
				found(evt);
				b.setLength(0);
				foundResourceAsChild=true;
				}
			else if(event.isCharacters())
				{
				b.append(event.asCharacters().getData());
				}
			else if(event.isEndElement())
				{
				if(!foundResourceAsChild)
					{
					RDFEvent evt= new RDFEvent();
					evt.subject=descriptionURI;
					evt.predicate= predicateURI;
					evt.value= b.toString();
					evt.lang=lang;
					evt.valueType=datatype;
					found(evt);
					}
				else
					{
					if(b.toString().trim().length()!=0) throw new XMLStreamException("Found bad text "+b);
					}
				return;
				}
			}
		}
	
}

private URI resolveBase(String ID) throws URISyntaxException
	{
	if(this.base==null) return  createURI(ID);
	return this.base.resolve(ID);
	}

private StringBuilder parseLiteral() throws XMLStreamException
	{
	StringBuilder b=new StringBuilder();
	QName qn;
	int depth=0;
	while(getReader().hasNext())
		{
		XMLEvent event = getReader().nextEvent();
		if(event.isCharacters())
			{
			b.append(escapeXML(event.asCharacters().getData()));
			}
		else if(event.isProcessingInstruction())
			{
			b.append("<?"+event.asCharacters()+"?>");
			}
		else if(event.isEndElement())
			{
			if(depth==0) return b;
			qn= event.asEndElement().getName();
			b.append("</"+qn.getPrefix()+":"+qn.getLocalPart()+">");
			depth--;
			}
		else if(event.isStartElement())
			{
			qn= event.asEndElement().getName();
			b.append("<"+qn.getPrefix()+":"+qn.getLocalPart());
			
			for(Iterator<?> i=event.asStartElement().getAttributes();
				i.hasNext();)
				{
				Attribute att=(Attribute)i.next();
				qn= att.getName();
				b.append(" ").
					append(qn.getPrefix()+":"+qn.getLocalPart()).
					append("=\"").
					append(escapeXML(att.getValue())).
					append("\"");
				}
			event=getReader().peek();
			if(event!=null && event.isEndElement())
				{
				getReader().nextEvent();
				b.append("/>");
				}
			else
				{
				b.append(">");
				depth++;
				}
			}
		}
	
	return b;
	}

protected URI createAnonymousURI() throws URISyntaxException
	{
	return createURI("_"+(++ID_GENERATOR));
	}

public void parse(InputStream in) throws XMLStreamException
	{
	read(new InputStreamReader(in));
	}

public void parse(Reader in) throws XMLStreamException
	{
	read(in);
	}

public void parse(File in) throws XMLStreamException
	{
	try {
		debug("Parsing file "+in);
		FileReader fin= new FileReader(in);
		read(fin);
		fin.close();
		debug("End Parsing file "+in);
		}
	catch (IOException e)
		{
		throw new XMLStreamException(e);
		}
	}

public void parse(URL in) throws XMLStreamException
	{
	debug("parsing URL "+in);
	try {
		InputStream fin= in.openStream();
		read(new InputStreamReader(fin));
		fin.close();
		}
	catch (IOException e)
		{
		throw new XMLStreamException(e);
		}
	}

private String name2string(StartElement e)
	{
	return name2string(e.getName());
	}

private String name2string(QName name)
	{
	return name.getNamespaceURI()+name.getLocalPart();
	}


private  URI name2uri(QName name) throws URISyntaxException 
	{
	return createURI(name2string(name));
	}



private URI createURI(String uriAsString) throws URISyntaxException 
	{
	return  new URI(uriAsString);
	}


private static String escapeXML(String s)
	{
	return XMLUtilities.escape(s);
	}

/**
 * echo URLs
 * @param args
 */
public static void main(String[] args)
	{
	try {
		Debug.setDebugging(false);
    	int optind=0;
    	
    	while(optind<args.length)
		        {
		        if(args[optind].equals("-h"))
		           {
		        	System.err.println(Compilation.getLabel());
		        	System.err.println("\t-h this screen");
		        	System.err.println("\t-d turns debugging on");
					return;
		           	}
		        else if(args[optind].equals("-d"))
		        	{
		        	Debug.setDebugging(true);
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
    	
		System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		System.out.println("<rdf:RDF  xmlns:rdf=\""+RDF.NS+"\">");
		
		RDFHandler h= new RDFHandler()
			{
			@Override
			public void found(RDFEvent event) {
				System.out.println(event);
				}
			
			};
		
		while(optind< args.length)
			{
			h.parse(new URL(args[optind++]));
			}
		
		System.out.println("</rdf:RDF>");
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	}

}
