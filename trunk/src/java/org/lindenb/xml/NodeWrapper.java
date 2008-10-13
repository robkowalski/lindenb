package org.lindenb.xml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * NodeWrapper
 * @author pierre
 *
 * @param <T extends org.w3c.dom.Node>
 */
public class NodeWrapper<T extends org.w3c.dom.Node>
	{
	/** delegate node */
	private T node;
	public NodeWrapper(T node)
		{
		this.node=node;
		}
	
	public String getNodeName()
		{
		return getNode().getNodeName();
		}
	
	public T getNode()
		{
		return node;
		}
	
	public String getAttribute(String name,String defaultValue)
		{
		if(getNode().hasAttributes()) return null;
		Attr att= Attr.class.cast(getNode().getAttributes().getNamedItem(name));
		return att==null?defaultValue:att.getValue();
		}
	
	public String getAttribute(String name)
		{
		return getAttribute(name,null);
		}
	
	public boolean hasElement()
		{
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(n.getNodeType()!=Node.ELEMENT_NODE) continue;
			return true;
			}
		return false;
		}
	
	public int countElements()
		{
		int count=0;
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(n.getNodeType()!=Node.ELEMENT_NODE) continue;
			++count;
			}
		return count;
		}
	
	public Collection<org.w3c.dom.Element> elements()
		{
		ArrayList<org.w3c.dom.Element> _v= new ArrayList<org.w3c.dom.Element>();
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(n.getNodeType()!=Node.ELEMENT_NODE) continue;
			_v.add(org.w3c.dom.Element.class.cast(n));
			}
		return _v;
		}

	public int countElements(String name)
		{
		int count=0;
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
				     n.getNodeName().equals(name))) continue;
			++count;
			}
		return count;
		}	
	
	public Collection<org.w3c.dom.Element> elements(String name)
		{
		ArrayList<org.w3c.dom.Element> _v= new ArrayList<org.w3c.dom.Element>();
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
			     n.getNodeName().equals(name))) continue;
			_v.add(org.w3c.dom.Element.class.cast(n));
			}
		return _v;
		}
	
	public boolean hasElement(String name)
		{
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
				     n.getNodeName().equals(name))) continue;
			return true;
			}
		return false;
		}
	
	public int countElements(String ns,String localName)
		{
		int count=0;
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
					 ns.equals(n.getNamespaceURI()) &&
				     n.getLocalName().equals(localName))) continue;
			++count;
			}
		return count;
		}	

	public boolean hasElement(String ns,String localName)
		{
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
					 ns.equals(n.getNamespaceURI()) &&
				     n.getLocalName().equals(localName))) continue;
			return true;
			}
		return false;
		}
	
	public Collection<org.w3c.dom.Element> elements(String ns,String localName)
		{
		ArrayList<org.w3c.dom.Element> _v= new ArrayList<org.w3c.dom.Element>();
		for(Node n=getNode().getFirstChild();
			n!=null;
			n=n.getNextSibling()
			)
			{
			if(!(n.getNodeType()==Node.ELEMENT_NODE &&
				 ns.equals(n.getNamespaceURI()) &&
			     n.getLocalName().equals(localName))) continue;
			_v.add(org.w3c.dom.Element.class.cast(n));
			}
		return _v;
		}
	
	@Override
	public int hashCode() {
		return getNode().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		return obj==this;
		}
	
	
	public void write(Writer out)
		{
		PrintWriter w= new PrintWriter(out);
		write(w,getNode());
		w.flush();
		}
	
	protected static void write(PrintWriter out,Node n)
		{
		switch(n.getNodeType())
			{
			case Node.ELEMENT_NODE:
				{
				Element e= Element.class.cast(n);
				out.print('<');
				out.print(e.getNodeName());
				if(e.hasAttributes())
					{
					NamedNodeMap atts=e.getAttributes();
					for(int i=0;i< atts.getLength();++i)
						{
						Attr att= (Attr)atts.item(i);
						out.print(' ');
						out.print(att.getNodeName());
						out.print("=\"");
						out.print(XMLUtilities.escape(att.getValue()));
						out.print("\"");
						}
					}
				if(e.hasChildNodes())
					{
					out.print('>');
					for(Node c= e.getFirstChild();c!=null;c=c.getNextSibling())
						{
						write(out,c);
						}
					out.print("</");
					out.print(e.getNodeName());
					out.print(">");
					}
				else
					{
					out.print("/>");
					}
				break;
				}
			case Node.TEXT_NODE:
				{
				out.print(XMLUtilities.escape(((Text)n).getData()));
				break;
				}
			default:
				{
				throw new IllegalArgumentException("TODO");
				}
			}
		}
	
	@Override
	public String toString() {
		StringWriter w= new StringWriter();
		write(w);
		return w.toString();
		}
	
	}
