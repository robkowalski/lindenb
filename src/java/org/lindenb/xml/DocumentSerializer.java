package org.lindenb.xml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;

import org.lindenb.lang.IllegalInputException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

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
	
	private Logger log()
		{
		return LOG;
		}
	
	/** write this document to output stream */
	public void writeDocument(
			Document dom,
			OutputStream out) throws IOException
		{
		out.write(isCompressed()?1:0);
		if(isCompressed())
			{
			out= new GZIPOutputStream(out);
			}
		DataOutputStream output= new DataOutputStream(out);
		
		Map<String,Integer> ns2idx=new HashMap<String,Integer>();
		Map<String,Integer> qName2idx=new HashMap<String,Integer>();
		collectNS(dom,ns2idx,qName2idx);
		output.writeInt(ns2idx.size());
		for(String ns:ns2idx.keySet())
			{
			writeString(output,ns);
			}
		output.writeInt(qName2idx.size());
		for(String ns:qName2idx.keySet())
			{
			writeString(output,ns);
			}
		node2entry(dom,ns2idx,qName2idx,output);
		output.flush();
		}
	/** collect the namespaces and the QName */
	private static void collectNS(
		Node object,
		Map<String,Integer> nsidx,
		Map<String,Integer> qName2idx)
		{
		String ns=object.getNamespaceURI();
		if(ns!=null && !nsidx.containsKey(ns))
			{
			nsidx.put(ns, nsidx.size()+1);
			}
		if(object.getNodeType()==Node.ATTRIBUTE_NODE ||
			object.getNodeType()==Node.ELEMENT_NODE)
			{
			String tag= object.getNodeName();
			if(!qName2idx.containsKey(tag))
				{
				qName2idx.put(tag, qName2idx.size()+1);
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
			for(Node n=object.getFirstChild();n!=null;n=n.getNextSibling())
				{
				collectNS(n,nsidx,qName2idx);
				}
			}
		}

	private static void node2entry(
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
				for(Node c=doc.getFirstChild();c!=null;c=c.getNextSibling())
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
				NamedNodeMap atts=e.getAttributes();
				output.writeInt(atts.getLength());
				for(int i=0;i< atts.getLength();++i)
					{
					node2entry(atts.item(i),ns2idx,qName2idx,output);
					}
				int i= XMLUtilities.countAllNodes(e);
				output.writeInt(i);
				for(Node c=e.getFirstChild();c!=null;c=c.getNextSibling())
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
				}
			default: throw new IllegalArgumentException("Node not handled");
			}
		}
	
	
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
					e= dom.createElementNS(
						idx2ns.get(nsid),
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
	
	
	public Document readDocument(DocumentBuilder builder,InputStream in) throws IOException
		{
	    int compress= in.read();
	    if(compress==-1) throw new IllegalInputException("compression byte missing");
	    if(compress==1) in= new GZIPInputStream(in);
	    DataInputStream input=new DataInputStream(in);
	    
	    int n= input.readInt();
		Map<Integer,String> idx2ns=new HashMap<Integer,String>(n);
		for(int i=0;i< n;++i)
			{
			idx2ns.put(i+1, readString(input));
			}
		
		n= input.readInt();
		Map<Integer,String> idx2qname=new HashMap<Integer,String>(n);
		for(int i=0;i< n;++i)
			{
			idx2qname.put(i+1, readString(input));
			}
		
		return (Document)entry2node(null,idx2ns,idx2qname,input);
		}
	
	
	
	}
