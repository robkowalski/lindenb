package org.lindenb.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

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
				     n.getNodeName().equals(localName))) continue;
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
				     n.getNodeName().equals(localName))) continue;
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
			     n.getNodeName().equals(localName))) continue;
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
	
	}
