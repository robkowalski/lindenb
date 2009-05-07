package org.lindenb.sw.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.InvalidXMLException;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.util.StringUtils;
import org.lindenb.util.TimeUtils;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOM4RDF: A generic RDF parser for the DOM document.
 * @author pierre
 *
 */
public class DOM4RDF
	{
	static private long ID_GENERATOR=System.currentTimeMillis();
	static private Logger _log= Logger.getLogger(DOM4RDF.class.getName());
	private Random rand= new Random(ID_GENERATOR);
	private Transformer transformer;
	
	/**
	 * A Resource (URI) in the document
	 * @author lindenb
	 *
	 */
	public static class Resource
		{
		private String uri;
		private boolean anonymous;
		private Resource(String s) throws URISyntaxException
			{
			this(s,false);
			}
		private Resource(String s,boolean anonymous) throws URISyntaxException
			{
			this.uri=s;
			if(s.startsWith("_:")) anonymous=true;
			this.anonymous=anonymous;
			if(!anonymous) new java.net.URI(s);
			}
		@Override
		public int hashCode()
			{
			return uri.hashCode();
			}
		/** is it an anonymous resource */
		public boolean isAnonymous()
			{
			return anonymous;
			}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			Resource other = (Resource) obj;
			return uri.equals(other.uri);
			}
		
		@Override
		public String toString() {
			return uri;
			}
		}
	
	/**
	 * 
	 * A Statement (subject/predicate/value)
	 *
	 */
	public static class Statement
		{
		private Resource subject;
		private Resource predicate;
		private Object value;
		private String dataType;
		private String lang;
		
		
		
		private Statement(Resource subject, Resource predicate, Object value,
				String dataType, String lang)
			{
			this.subject = subject;
			this.predicate = predicate;
			this.value = value;
			this.dataType = dataType;
			this.lang = lang;
			}
		
		public Resource getSubject() {
			return subject;
			}
		public Resource getPredicate() {
			return predicate;
			}
		public Object getValue() {
			return value;
			}
		public String getDataType() {
			return dataType;
			}
		public String getLang() {
			return lang;
			}
		
		@Override
		public int hashCode()
			{
			final int prime = 31;
			int result = 1;
			result = prime * result + predicate.hashCode();
			result = prime * result + subject.hashCode();
			result = prime * result +  value.hashCode();
			return result;
			}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Statement))
				return false;
			Statement other = (Statement) obj;
			
			if (!subject.equals(other.subject)) return false;
			if (!predicate.equals(other.predicate)) return false;
			if (!value.equals(other.value)) return false;
			
			if (dataType == null) {
				if (other.dataType != null)
					return false;
			} else if (!dataType.equals(other.dataType))
				return false;
			if (lang == null) {
				if (other.lang != null)
					return false;
			} else if (!lang.equals(other.lang))
				return false;
			
			return true;
			}
		
		public boolean isLiteral()
			{
			return !(value instanceof Resource);
			}
		
		public String asN3()
			{
			StringBuilder b=new StringBuilder();
			if(this.subject.isAnonymous())
				{
				b.append(this.subject).append(" ");;
				}
			else
				{
				b.append("<").append(this.subject).append("> ");
				}
			
			if(this.predicate.isAnonymous())
				{
				b.append(this.predicate).append(" ");;
				}
			else
				{
				b.append("<").append(this.predicate).append("> ");
				}
			
			if(!isLiteral())
				{
				if(Resource.class.cast(this.value).isAnonymous())
					{
					b.append(this.value).append(" ");
					}
				else
					{
					b.append("<").append(this.value).append("> ");
					}
				}
			else
				{
				b.append("\"").append(C.escape(this.value.toString())).append("\"");
				if(dataType!=null)
					{
					b.append("^^<").append(this.dataType).append("> ");
					}
				else if(lang!=null)
					{
					b.append("@").append(this.lang);
					}
				}
			
			b.append(" .");
			return b.toString();
			}
		
		/**
		 * return wether this statement match a given rule
		 * @param subject used as a filter. if null, select wathever 
		 * @param predicate used as a filter. if null, select wathever 
		 * @param value used as a filter. Can be a String or a Resource. If null, select wathever
		 * @returna true if this Stmt match the rules
		 */
		public boolean match( Resource subject, Resource predicate, Object value)
			{
			if(subject!=null && !this.subject.equals(subject)) return false;
			if(predicate!=null && !this.predicate.equals(predicate)) return false;
			if(value!=null && !this.value.equals(value)) return false;
			return true;
			}
		
		@Override
		public String toString() {
			return asN3();
			}
		}
	
	
	
	
	
	private static class PileUpStmt
		extends DOM4RDF
		{
		private Set<Statement> stmts= new HashSet<Statement>();
		private Resource subject;
		private Resource predicate;
		private Object value;
		
		
		public PileUpStmt()
			{
			this(null,null,null,null,null);
			}
		
		public PileUpStmt(
				Resource subject, Resource predicate, Object value,
				String dataType, String lang)
			{
			this.subject=subject;
			this.predicate=predicate;
			this.value=value;
			
			}
		
		@Override
		public void foundStatement(
				Resource subject, Resource predicate, Object value,
				String dataType, String lang)
			{
			Statement stmt=new Statement(subject,predicate,value,dataType,lang);
			if(!stmt.match(this.subject, this.predicate, this.value)) return;
			this.stmts.add(stmt);
			}
		
		public Set<Statement> getStatements()
			{
			return this.stmts;
			}
		}
	
	
	
	/**
	 * DOM4RDF
	 */
	public DOM4RDF()
		{
		TransformerFactory tFactory=TransformerFactory.newInstance();
		try
			{
			this.transformer =tFactory.newTransformer();
			this.transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}
		catch(TransformerConfigurationException err)
			{
			throw new RuntimeException(err);
			}
		}
	
	private static DocumentBuilder newBuilder() throws IOException
		{
		try {
			DocumentBuilderFactory f= DocumentBuilderFactory.newInstance();
			f.setNamespaceAware(true);
			f.setValidating(false);
			f.setCoalescing(true);
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(true);
			f.setIgnoringElementContentWhitespace(true);
			return f.newDocumentBuilder();
			} 
		catch (Exception e)
			{
			throw new IOException(e);
			}
		}
		
	public void parse(String uri) throws IOException,SAXException,InvalidXMLException
		{
		parse(newBuilder().parse(uri));
		}
	
	public void parse(InputStream in) throws IOException,SAXException,InvalidXMLException
		{
		parse(newBuilder().parse(in));
		}
	
	public void parse(Reader in) throws IOException,SAXException,InvalidXMLException
		{
		parse(newBuilder().parse(new InputSource(in)));
		}
	
	/** parse a DOM Document */
	public void parse(Document dom) throws InvalidXMLException
		{
		if(dom==null) throw new NullPointerException("dom==null");
		
		Element root= dom.getDocumentElement();
		if(root==null)
			{
			throw new InvalidXMLException(root,"Document has no root");
			}
		//check rdf definition
		NamedNodeMap atts=root.getAttributes();
		for(int i=0;i< atts.getLength();++i)
			{
			Attr att= (Attr)atts.item(i);
			
			if( XMLConstants.XML_NS_PREFIX.equals(att.getPrefix()) &&
				XMLConstants.XML_NS_URI.equals(att.getNamespaceURI()) &&
				"rdf".equals(att.getLocalName()) &&
				!RDF.NS.equals(att.getValue()))
				{
				 throw new InvalidXMLException("rdf should be defined as "+RDF.NS);
				}
			}
		parseRDF(root);
		}
	

	
	/** creates an anonymous ID */
	protected Resource createAnonId() throws InvalidXMLException
		{
		try
			{
			return new Resource("_:"+TimeUtils.toYYYYMMDDHHMMSS()+"-"+(++ID_GENERATOR)+"-"+Math.abs(rand.nextInt() ) ,true);
			}
		catch(URISyntaxException err)
			{
			throw new InvalidXMLException(err);
			}
		}
	
	/** parse a rdf:RDF element */
	public void parseRDF(Element root) throws InvalidXMLException
		{
		if(root==null) throw new IllegalArgumentException("null root");
		if(!XMLUtilities.isA(root, RDF.NS, "RDF")) throw new IllegalArgumentException("Root is not rdf:RDF");
		
		//loop over children of rdf:RDF
		for(Node n1= root.getFirstChild();
			n1!=null;n1=n1.getNextSibling())
			{
			switch(n1.getNodeType())
				{
				case Node.ELEMENT_NODE:
					{
					parseResource(Element.class.cast(n1));
					break;
					}
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					{
					checkNodeIsEmpty(n1);
					break;
					}
				case Node.PROCESSING_INSTRUCTION_NODE:
					{
					warning(n1, "Found Processing instruction under "+root.getNodeName());
					break;
					}
				default: throw new UnsupportedOperationException("Node type not handled : "+n1.getNodeType());
				}
			}
		}
	
	/** create a new Resource */
	public static Resource createResource(String uri)  throws InvalidXMLException
		{
		try {
			return new Resource(uri);
		} catch (URISyntaxException e)
			{
			throw new InvalidXMLException(e);
			}
		}
	
	/**
	 * return a Set of filtered RDF Statements
	 * @param an original collection of rdf:RDF or a Statement
	 * @param subject used as a filter. if null, select all the subjects
	 * @param predicate used as a filter. if null, select all the predicates
	 * @param value used as a filter. Can be a String or a Resource. If null, select all the values
	 * @returna Set of filtered RDF Statements
	 */
	public static Set<Statement> filter(
			final Collection<Statement> stmts,
			final Resource subject,
			final Resource predicate,
			final Object value)
		{
		Set<Statement> set= new HashSet<Statement>();
		for(Statement stmt: stmts)
			{
			if(stmt.match(subject, predicate, value))
				{
				set.add(stmt);
				}
			}
		return set;
		}
	
	/** return wether this node a a rdf:(abou|ID|nodeId) */
	public static boolean isAnonymousResource(Element rsrc)
		{
		Attr att= rsrc.getAttributeNodeNS(RDF.NS, "about");
		if(att!=null) return false;
		att= rsrc.getAttributeNodeNS(RDF.NS, "ID");
		if(att!=null) return false;
		att= rsrc.getAttributeNodeNS(RDF.NS, "nodeID");
		if(att!=null) return false;
		return true;
		}
	
	/** returns a URI for an Resource element */
	public Resource getResourceURI(Element root) throws InvalidXMLException
		{
		Resource subject=null;
		if(root.hasAttributes())
			{
			NamedNodeMap atts=root.getAttributes();
			for(int i=0;i< atts.getLength();++i)
				{
				Attr att=(Attr)atts.item(i);
				if(!RDF.NS.equals(att.getNamespaceURI())) continue;
				if(att.getLocalName().equals("about"))
					{
					if(subject!=null) throw new InvalidXMLException(root,"subject id defined twice");
					subject= createResource(att.getValue());
					}
				else if(att.getLocalName().equals("ID"))
					{
					if(subject!=null) throw new InvalidXMLException(root,"subject id defined twice");
					subject= createResource(getBase(root)+att.getValue());
					}
				else if(att.getLocalName().equals("nodeID"))
					{
					if(subject!=null) throw new InvalidXMLException(root,"subject id defined twice");
					subject= createResource("_:"+att.getValue());
					//uri= URI.create(getBase(root)+att.getValue());
					}
				}
			}
		if(subject==null) subject= createAnonId();
		return subject;
		}
	
	
	/** parse everything under rdf:RDF
	 * @return the URI of the resource
	 */
	public Resource parseResource(Element root) throws InvalidXMLException
		{
		Resource subject=getResourceURI(root);
		
		if(root.hasAttributes())
			{
			NamedNodeMap atts=root.getAttributes();
			for(int i=0;i< atts.getLength();++i)
				{
				Attr att=(Attr)atts.item(i);
				if(RDF.NS.equals(att.getNamespaceURI()))
					{
					if(att.getLocalName().equals("about") ||
					   att.getLocalName().equals("nodeID") ||
					   att.getLocalName().equals("ID")
						)
						{
						continue;
						}
					else  if(att.getLocalName().equals("resource"))
						{
						throw new InvalidXMLException(root,"should not contains rdf:resource");
						}
					else
						{
						throw new UnsupportedOperationException("rdf:* node supported");
						}
					}
				else if(XMLConstants.XML_NS_URI.equals(att.getNamespaceURI()))
					{
					//ignore
					}
				else
					{
					foundStatement(subject,
						createResource(att.getNamespaceURI()+att.getLocalName()),
						att.getValue(),
						null,null
						);
					}
				}
			}
		parseResourceChildren(root, subject);
		return subject;
		}
	
	
	/** check a node contains only a blank stuff */
	protected boolean checkNodeIsEmpty(Node n1) throws InvalidXMLException
		{
		if(!StringUtils.isBlank(n1.getNodeValue()))
			{
			throw new InvalidXMLException(n1, "Found not whitespace content  under "+n1.getParentNode().getNodeName());
			}
		return true;
		}
	
	
	
	/** parse everything under a resource element */
	protected void parseResourceChildren(
		Element root,
		Resource subjectURI
		) throws InvalidXMLException
		{
		for(Node n1= root.getFirstChild();
			n1!=null;
			n1=n1.getNextSibling())
			{
			switch(n1.getNodeType())
				{
				case Node.ELEMENT_NODE:
					{
					parseProperty(Element.class.cast(n1),subjectURI);
					break;
					}
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					{
					checkNodeIsEmpty(n1);
					break;
					}
				case Node.PROCESSING_INSTRUCTION_NODE:
					{
					warning(n1, "Found Processing instruction under "+root.getNodeName());
					break;
					}
				default:throw new UnsupportedOperationException("invalid node type");
				}
			}
		}
	
	/**  return the xml:lang of the node or null */
	private String getLang(Node root)
		{
		if(root==null) return null;
		if( root.getNodeType()==Node.ELEMENT_NODE && root.hasAttributes())
			{
			Attr att= Element.class.cast(root).getAttributeNodeNS(XMLConstants.XML_NS_URI, "lang");
			if(att!=null) return att.getValue();
			}
		return getLang(root.getParentNode());
		}
	
	/** parse everything under rdf:RDF */
	protected void parseProperty(Element property,Resource subject)
		throws 	InvalidXMLException
		{
		Attr parseTypeNode= property.getAttributeNodeNS(RDF.NS, "parseType");
		Attr dataTypeNode =  property.getAttributeNodeNS(RDF.NS, "dataType");
		String dataType= (dataTypeNode==null?null:dataTypeNode.getValue());
		String parseType=parseTypeNode!=null?parseTypeNode.getValue():null;
		
		Resource predicate= createResource(property.getNamespaceURI()+property.getLocalName());
		
		if(predicate==null)
			{
			throw new InvalidXMLException(property,"Cannot parse URI of this predicate");
			}
		/** default parse type */
		if(parseType==null)
			{
			Attr rsrc= property.getAttributeNodeNS(RDF.NS, "resource");
			if(!property.hasChildNodes())
				{
				if(rsrc==null) throw new InvalidXMLException(property,"missing rdf:resource");
				foundStatement(subject, predicate, createResource(rsrc.getValue()), null, null);
				}
			else
				{
				if(rsrc!=null) throw new InvalidXMLException(property,"rdf:resource is present and element has children");
				if(predicate.toString().equals(RDF.NS+"type")) throw new InvalidXMLException(property,"rdf:type expected in an empty element"); 
				int count = XMLUtilities.count(property);
				switch(count)
					{
					case 0: foundStatement(subject, predicate, property.getTextContent(), dataType, getLang(property)); break;
					case 1: Resource value= parseResource(XMLUtilities.firstChild(property));
							foundStatement(subject, predicate, value, null, null);
							break;
					default: throw new InvalidXMLException(property,"illegal number of element under.");
					}
				}
			}
		else if(parseType.equals("Literal"))
			{
			StringBuilder buff= null;
			for(Node n1=property.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				switch(n1.getNodeType())
					{
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						{
						buff.append(n1.getTextContent());
						break;	
						}
					case Node.ELEMENT_NODE:
						{
						try {
							StringWriter w= new StringWriter();
							this.transformer.transform(new DOMSource(n1),new  StreamResult(w));
							if(buff==null) buff=new StringBuilder();
							buff.append(w.toString());
							}
						catch (Exception e) {
							throw new InvalidXMLException(n1,e.getMessage());
							}
						break;
						}
					default: throw new UnsupportedOperationException("node type unsupported");
					}
				}
			
			if(buff==null)
				{
				throw new InvalidXMLException(property,"Found no content under node");
				}
			
			if(subject!=null && predicate!=null && buff!=null)
				{
				foundStatement(subject,predicate,buff.toString(),RDF.NS+"XMLLiteral", getLang(property));
				}
			}
		else if(parseType.equals("Resource"))
			{
			Resource rsrc= createAnonId();
			if(subject!=null && predicate!=null)
				{
				foundStatement(subject,predicate,rsrc,null,null);
				}
			
			for(Node n1=property.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				switch(n1.getNodeType())
					{
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						{
						checkNodeIsEmpty(n1);
						break;	
						}
					case Node.ELEMENT_NODE:
						{
						parseProperty(Element.class.cast(n1), rsrc);
						break;
						}
					default: warning(n1, "unsupported node type");break;
					}
				}
			
			}
		else if(parseType.equals("Collection"))
			{
			List<Resource> list= new ArrayList<Resource>();
			for(Node n1=property.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				switch(n1.getNodeType())
					{
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						{
						checkNodeIsEmpty(n1);
						break;	
						}
					case Node.ELEMENT_NODE:
						{
						Resource r=	 parseResource(Element.class.cast(n1));
						list.add(r);
						break;
						}
					default: warning(n1, "unsupported node type");break;
					}
				}
			
			
			if(list.isEmpty())
				{
				warning(property,"Empty list");
				}
			else
				{
				Resource prevURI=createAnonId();
				
				if(subject!=null && predicate!=null)
					{
					foundStatement(subject, predicate,prevURI, null, null);
					}
				
				for(int i=0;i< list.size();++i)
					{
					if(i+1==list.size())
						{
						foundStatement(prevURI,createResource(RDF.NS+"first"), list.get(i), null, null);
						foundStatement(prevURI,createResource(RDF.NS+"rest"), createResource(RDF.NS+"nil"), null, null);
						}
					else
						{
						Resource newURI= createAnonId();
						foundStatement(prevURI,createResource(RDF.NS+"first"), list.get(i), null,null);
						foundStatement(prevURI,createResource(RDF.NS+"rest"), newURI, null, null);
						prevURI=newURI;
						}
					}
				}
			
			}
		else
			{
			throw new InvalidXMLException(property,"illegal rdf:parseType:"+parseType);
			}
		}
	
	
	/** get the BASE url of the document */
	protected Resource getBase(Node n) throws InvalidXMLException
		{
		String s=n.getOwnerDocument().getBaseURI();
		if(s==null) throw new InvalidXMLException(n.getOwnerDocument(),"document has not xml:base");
		return createResource(s);
		}
	
	/**
	 * echo a warning
	 * @param node
	 * @param message
	 */
	protected void warning(Node node,String message)
		{
		_log.warning(message);
		}
	
	
	/**
	 * Called when a Statement was found. User can override this method.
	 * Default: does nothing
	 * @param subject
	 * @param property
	 * @param value
	 * @param dataType
	 * @param lang
	 */
	public void foundStatement(
		Resource subject,
		Resource property,
		Object value,
		String dataType,
		String lang
		)
		{
		
		}
	
	/**
	 * return a Set of filtered RDF Statements
	 * @param node the root node, can be a Document, a rdf:RDF or a Statement
	 * @param subject used as a filter. if null, select all the subjects
	 * @param predicate used as a filter. if null, select all the predicates
	 * @param value used as a filter. Can be a String or a Resource. If null, select all the values
	 * @returna Set of filtered RDF Statements
	 * @throws InvalidXMLException
	 */
	public static Set<Statement> getStatements(Node node,
			Resource subject,
			Resource predicate,
			Object value
			) throws InvalidXMLException
		{
		if(node==null) throw new NullPointerException("node==null");
		PileUpStmt app= new PileUpStmt(subject,predicate,value,null,null);
		switch(node.getNodeType())
			{
			case Node.DOCUMENT_NODE: app.parse(Document.class.cast(node));break;
			case Node.ELEMENT_NODE:
				{
				if(XMLUtilities.isA(node, RDF.NS, "RDF"))
					{
					app.parseRDF(Element.class.cast(node));
					}
				else
					{
					app.parseResource(Element.class.cast(node));
					}
				break;
				}
			default: throw new IllegalArgumentException("bad Node, should be Document or Element.");
			}
		return app.getStatements();
		}
	
	/**
	 * get all the Statements in the model
	 * @param node the root node, can be a Document, a rdf:RDF or a Statement
	 * @return a set of Statements
	 * @throws InvalidXMLException
	 */
	public static Set<Statement> getStatements(Node node) throws InvalidXMLException
		{
		return getStatements(node, null, null, null);
		}
	
	
		public static void main(String[] args) {
		try
			{
			int optind=0;
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
			DOM4RDF app= new DOM4RDF()
				{
				@Override
				public void foundStatement(
						Resource subject, Resource predicate, Object value,
						String dataType, String lang)
					{
					Statement stmt=new Statement(subject,predicate,value,dataType,lang);
					System.out.println(stmt.asN3());
					}
				};
			if(optind==args.length)
				{
				app.parse(System.in);
				}
			else
				{
				while(optind< args.length)
					{
					Reader in = IOUtils.openReader(args[optind++]);
					app.parse(in);
					in.close();
					}
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
