package org.lindenb.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.lang.IllegalInputException;
import org.lindenb.util.Compilation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Serialize document to/from InputStream/OutputStream
 * @author pierre
 *
 */
public class DocumentSerializer
	{
	private static Logger LOG=Logger.getLogger(DocumentSerializer.class.getName()); 
	/** should we gzip */
	private boolean compressed=false;
	/** blank text */
	
	/** set the use a GZIPOutputStream for output */
	public void setCompressed(boolean compressed)
		{
		this.compressed=compressed;
		}
	
	/** shall we use a GZIPOutputStream for output ? */
	public boolean isCompressed() {
		return compressed;
		}
	
	private static void writeString(DataOutputStream out,String s)  throws IOException
		{
		out.writeUTF(s);
		}
	private static String readString(DataInputStream in)  throws IOException
		{
		return in.readUTF();
		}
	
	@SuppressWarnings("unused")
	private Logger log()
		{
		return LOG;
		}
	
	/** write this document to output stream */
	public void writeDocument(
			Document dom,
			OutputStream out) throws IOException
		{
		GZIPOutputStream zout=null;
		DataOutputStream output=null;
		out.write(isCompressed()?1:0);
		if(isCompressed())
			{
			zout= new GZIPOutputStream(out);
			output=new DataOutputStream(zout);
			}
		else
			{
			output= new DataOutputStream(out);
			}
		
		Map<String,Integer> ns2idx=new TreeMap<String,Integer>();
		Map<String,Integer> qName2idx=new TreeMap<String,Integer>();
		collectNS(dom,ns2idx,qName2idx);
		output.writeInt(ns2idx.size());
		for(String ns:ns2idx.keySet())
			{
			output.writeInt(ns2idx.get(ns));
			writeString(output,ns);
			}
		
		output.writeInt(qName2idx.size());
		for(String tag:qName2idx.keySet())
			{
			output.writeInt(qName2idx.get(tag));
			writeString(output,tag);
			}
		
		node2entry(dom,ns2idx,qName2idx,output);
		output.flush();
		if(zout!=null)
			{
			zout.flush();//see http://forums.sun.com/thread.jspa?threadID=190326&tstart=56040
			zout.finish();
			}
		out.flush();
		}
	/** collect the namespaces and the QName */
	private void collectNS(
		Node object,
		Map<String,Integer> nsidx,
		Map<String,Integer> qName2idx
		)
		{
		String ns=object.getNamespaceURI();
		if(ns!=null && !nsidx.containsKey(ns))
			{
			nsidx.put(ns, nsidx.size());
			}
		if(object.getNodeType()==Node.ATTRIBUTE_NODE ||
			object.getNodeType()==Node.ELEMENT_NODE)
			{
			String tag= object.getNodeName();
			if(!qName2idx.containsKey(tag))
				{
				qName2idx.put(tag, qName2idx.size());
				}
			}
		
		if(object.hasAttributes())
			{
			NamedNodeMap atts=object.getAttributes();
			for(int i=0;i< atts.getLength();++i)
				{
				collectNS(atts.item(i),nsidx,qName2idx);
				}
			}
		if(object.hasChildNodes())
			{
			for(Node n=object.getFirstChild();
				n!=null;
				n=n.getNextSibling())
				{
				collectNS(n,nsidx,qName2idx);
				}
			}
		}
	
	/** serialize node to outputstream */
	private void node2entry(
		Node n,
		Map<String,Integer> ns2idx,
		Map<String,Integer> qName2idx,
		DataOutputStream output
		) throws IOException
		{
		output.writeInt(n.getNodeType());
		switch(n.getNodeType())
			{
			case Node.ATTRIBUTE_NODE:
				{
				Attr att= Attr.class.cast(n); 
				String ns= att.getNamespaceURI();
				if(ns==null)
					{
					output.writeInt(-1);
					}
				else
					{
					output.writeInt(ns2idx.get(ns));
					}
				output.writeInt(qName2idx.get( att.getName()));
				writeString(output, att.getValue());
				break;
				}
			case Node.DOCUMENT_NODE:
				{
				Document doc= Document.class.cast(n);
				
				writeString(output,doc.getXmlVersion());
				//not setter writeString(output,doc.getXmlEncoding());
				output.writeBoolean(doc.getXmlStandalone());
				
				int i= XMLUtilities.countAllNodes(doc);
				output.writeInt(i);
				for(Node c=doc.getFirstChild();
					c!=null;
					c=c.getNextSibling())
					{
					node2entry(c,ns2idx,qName2idx,output);
					}
				break;
				}
			case Node.ELEMENT_NODE:
				{
				Element e=Element.class.cast(n);
				String ns= e.getNamespaceURI();
				if(ns==null)
					{
					output.writeInt(-1);
					}
				else
					{
					output.writeInt(ns2idx.get(ns));
					}
				output.writeInt(qName2idx.get( e.getTagName()));
				//save attributes
				NamedNodeMap atts=e.getAttributes();
				output.writeInt(atts.getLength());
				for(int i=0;i< atts.getLength();++i)
					{
					node2entry(atts.item(i),ns2idx,qName2idx,output);
					}
				//save child nodes
				int i= XMLUtilities.countAllNodes(e);
				output.writeInt(i);
				for(Node c=e.getFirstChild();
					c!=null;
					c=c.getNextSibling())
					{
					node2entry(c,ns2idx,qName2idx,output);
					}
				break;
				}
			case Node.COMMENT_NODE:
			case Node.TEXT_NODE:
				{
				writeString(output, n.getNodeValue());
				break;
				}
			case Node.PROCESSING_INSTRUCTION_NODE:
				{
				ProcessingInstruction pi=ProcessingInstruction.class.cast(n);
				writeString(output, pi.getTarget());
				writeString(output, pi.getTextContent());
				break;
				}
			case Node.ENTITY_REFERENCE_NODE:
				{
				EntityReference entity=EntityReference.class.cast(n);
				writeString(output,entity.getNodeName());
				break;
				}
			default: throw new IllegalArgumentException("Node Type not handled");
			}
		}
	
	/** read a serialized document */
	public Document readDocument(DocumentBuilder builder,InputStream in) throws IOException
		{
	    int compress= in.read();
	    if(compress==-1) throw new IllegalInputException("compression byte missing");
	    if(compress==1) in= new GZIPInputStream(in);
	    DataInputStream input=new DataInputStream(in);
	    
	    int n= input.readInt();
		Map<Integer,String> idx2ns=new TreeMap<Integer,String>();
		for(int i=0;i< n;++i)
			{
			idx2ns.put(input.readInt(), readString(input));
			}
		
		n= input.readInt();
		Map<Integer,String> idx2qname=new TreeMap<Integer,String>();
		for(int i=0;i< n;++i)
			{
			idx2qname.put(input.readInt(), readString(input));
			}
		
		Document dom= builder.newDocument();
		return (Document)entry2node(dom,idx2ns,idx2qname,input);
		}
	
	/** convert a DataInputStream to dom */
	private Node entry2node(
		Document dom,
		Map<Integer,String> idx2ns,
		Map<Integer,String> idx2qname,
		DataInputStream in)
		throws IOException
		{
		int nodeType=in.readInt();
		switch(nodeType)
			{
			case Node.ATTRIBUTE_NODE:
				{
				int nsid= in.readInt();
				String tagName=idx2qname.get(in.readInt());
				Attr att=null;
				if(nsid==-1)
					{
					att= dom.createAttribute(tagName);
					}
				else
					{
					att= dom.createAttributeNS(idx2ns.get(nsid),tagName);
					}
				att.setValue(readString(in));
				return att;
				}
			case Node.DOCUMENT_NODE:
				{
				dom.setXmlVersion(readString(in));
				dom.setXmlStandalone(in.readBoolean());
				int count= in.readInt();
				for(int i=0;i< count;++i)
					{
					dom.appendChild(entry2node(dom, idx2ns,idx2qname,in));
					}
				return dom;
				}
			case Node.ELEMENT_NODE:
				{
				int nsid= in.readInt();
				
				String tagName=idx2qname.get(in.readInt());
				Element e= null;
				if(nsid==-1)
					{
					e= dom.createElement(tagName);
					}
				else
					{
					String ns=idx2ns.get(nsid);
					
					e= dom.createElementNS(
						ns,
						tagName
						);
					}
				
				int count= in.readInt();
				for(int i=0;i< count;++i)
					{
					e.setAttributeNode((Attr)entry2node(dom,idx2ns,idx2qname, in));
					}
				count= in.readInt();
				for(int i=0;i< count;++i)
					{
					e.appendChild(entry2node(dom,idx2ns,idx2qname,in));
					}
				return e;
				}
			case Node.COMMENT_NODE:
				{
				return dom.createComment(readString(in));
				}
			case Node.TEXT_NODE:
				{
				return dom.createTextNode(readString(in));
				}
			case Node.PROCESSING_INSTRUCTION_NODE:
				{
				return dom.createProcessingInstruction(readString(in),readString(in));
				}
			case Node.ENTITY_REFERENCE_NODE:
				{
				return dom.createEntityReference(readString(in));
				}
			
			default: throw new IllegalArgumentException("Node not handled");
			}
		}
	

	/** read a serialized document using a SAX Handler. Not Tested*/
	public void parseDocument(ContentHandler handler,InputStream in) throws IOException,SAXException
		{
	    int compress= in.read();
	    if(compress==-1) throw new IllegalInputException("compression byte missing");
	    if(compress==1) in= new GZIPInputStream(in);
	    DataInputStream input=new DataInputStream(in);
	    
	    int n= input.readInt();
		Map<Integer,String> idx2ns=new TreeMap<Integer,String>();
		for(int i=0;i< n;++i)
			{
			idx2ns.put(input.readInt(), readString(input));
			}
		
		n= input.readInt();
		Map<Integer,String> idx2qname=new TreeMap<Integer,String>();
		for(int i=0;i< n;++i)
			{
			idx2qname.put(input.readInt(), readString(input));
			}
	
		parsenode(handler,idx2ns,idx2qname,input);
		}
	
	/** parse a DataInputStream with a SAX Handler */
	private void parsenode(
			ContentHandler handler,
		Map<Integer,String> idx2ns,
		Map<Integer,String> idx2qname,
		DataInputStream in)
		throws IOException,SAXException
		{
		int nodeType=in.readInt();
		switch(nodeType)
			{
			case Node.DOCUMENT_NODE:
				{
				handler.startDocument();
				readString(in);// XmlVersion
				in.readBoolean();//XmlStandalone
				
				int count= in.readInt();
				for(int i=0;i< count;++i)
					{
					parsenode(handler, idx2ns,idx2qname,in);
					}
				handler.endDocument();
				break;
				}
			case Node.ELEMENT_NODE:
				{
				AttributesImpl atts= new AttributesImpl();

				int nsid= in.readInt();
				String ns=null;
				String qName=idx2qname.get(in.readInt());
				String localName=qName;
				
				if(nsid!=-1)
					{
					int i= qName.indexOf(':');
					if(i!=-1) localName=qName.substring(i+1);
					ns=idx2ns.get(nsid);
					}
				//loop over attributes
				int count= in.readInt();
				for(int i=0;i< count;++i)
					{
					in.readInt();//ignore read Node.ATTRIBUTE_NODE
					nsid= in.readInt();
					String attns=null;
					String attQName= idx2qname.get(in.readInt());
					
					String attLocalName= attQName;
					
					if(nsid!=-1)
						{
						int j= attQName.indexOf(':');
						if(j!=-1)
							{
							attLocalName=attQName.substring(j+1);
							}
						attns=idx2ns.get(nsid);
						}
					
						
					if(attns!=null && attns.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
						{
						handler.startPrefixMapping(attLocalName, readString(in));
						}
					else
						{
						atts.addAttribute(attns, attLocalName, attQName, "TODO", readString(in));
						}
					}
				handler.startElement(ns, localName, qName, atts);
				count= in.readInt();
				for(int i=0;i< count;++i)
					{
					parsenode(handler,idx2ns,idx2qname,in);
					}
				handler.endElement(ns, localName, qName);
				break;
				}
			case Node.COMMENT_NODE:
				{
				//ignore
				break;
				}
			case Node.TEXT_NODE:
				{
				char s[]=readString(in).toCharArray();
				handler.characters(s, 0, s.length);
				break;
				}
			case Node.PROCESSING_INSTRUCTION_NODE:
				{
				handler.processingInstruction(readString(in),readString(in));
				break;
				}
			default: throw new IllegalArgumentException("Node not handled "+nodeType);
			}
		}
	
	/**
	 * 
	 * main
	 * 
	 */
	public static void main(String[] args)
		{
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
				
				
				
				DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
				f.setCoalescing(true);
				f.setNamespaceAware(true);
				f.setValidating(false);
				f.setExpandEntityReferences(false);
				f.setIgnoringComments(false);
				f.setIgnoringElementContentWhitespace(true);
				DocumentBuilder docBuilder= f.newDocumentBuilder();
				DocumentSerializer app= new DocumentSerializer();
				app.setCompressed(true);
				
				while(optind < args.length)
					{
					File fin=new File(args[optind++]);
					Document dom=docBuilder.parse(fin);
					for(int i=0;i< 10;++i)
						{
						ByteArrayOutputStream out= new ByteArrayOutputStream();
						app.writeDocument(dom, out);
						byte array[]=out.toByteArray();
						dom=app.readDocument(docBuilder, new ByteArrayInputStream(array));
						}
					}
				} 
			catch(Throwable err)
				{
				err.printStackTrace();
				}
			}
	
	}
