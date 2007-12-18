package org.lindenb.xml;

import java.util.Collection;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XMLUtilities
	{
	private XMLUtilities() {}
	
/** escape the XML of a given string */
public static String escape(CharSequence s)
        {
        if(s==null) throw new NullPointerException("XML.escape(null)");
        int needed=-1;
        for(int i=0;i< s.length();++i)
	        {
	        switch(s.charAt(i))
	            {
				case '\'': 
				case '\"': 
				case '&': 
				case '<':
				case '>':  needed=i; break;
	
	            default: break;
	            }
	        if(needed!=-1) break;
	        }
		if(needed==-1) return s.toString();
		StringBuilder buffer=new StringBuilder(s.subSequence(0,needed));
		for(int i=needed;i< s.length();++i)
		       {
		       switch(s.charAt(i))
		               {
		               case '\'': buffer.append("&apos;"); break;
		               case '\"': buffer.append("&quot;"); break;
		               case '&': buffer.append("&amp;"); break;
		               case '<': buffer.append("&lt;"); break;
		               case '>': buffer.append("&gt;"); break;
		               default:  buffer.append(s.charAt(i));break;
		               }
		       }
		return buffer.toString();
        }

/**
 * return wether the given node matches ns and localName
 * @param node
 * @param ns namespace use null as a wildcard for all namespaceuri
 * @param localName use null as a wildcard for all localNames
 * @return is node matching
 */
public static boolean isA(Node node,String ns,String localName)
	{
	if(node.getNodeType()!=Node.ELEMENT_NODE) return false;
	if(ns!=null && !ns.equals(node.getNamespaceURI()))return false;
	if(localName!=null && !localName.equals(node.getLocalName())) return false;
	return true;
	}


/**
 * count number of element under root matching ns and localName
 * @param root
 * @param ns namespace use null as a wildcard for all namespaceuri
 * @param localName use null as a wildcard for all localNames
 * @return number Found
 */
public static int count(Node root,String ns,String localName)
	{
	int n=0;
	for(Node c=root.getFirstChild();c!=null;c=c.getNextSibling())
		{
		if(!isA(c,ns,localName)) continue;
		++n;
		}
	return n;
	}

/**
 * return the first Element under root matching ns and localName
 * @param root
 * @param ns namespace use null as a wildcard for all namespaceuri
 * @param localName use null as a wildcard for all localNames
 * @return element Found or null
 */
public static Element firstElement(Node root,String ns,String localName)
	{
	for(Node c=root.getFirstChild();c!=null;c=c.getNextSibling())
		{
		if(!isA(c,ns,localName)) continue;
		return Element.class.cast(c);
		}
	return null;
	}

/** return Collection over child elements */
public static Collection<Element> elements(Node parent)
	{
	return elements(parent,null,null);
	}

/**
 * return a collection of all Element under parent matching ns and localName
 * @param parent
 * @param ns namespace use null as a wildcard for all namespaceuri
 * @param localName use null as a wildcard for all localNames
 * @return element Found or null
 */
public static Collection<Element> elements(Node parent,String namespace,String localName)
	{
	Vector<Element> v= new Vector<Element>();
	for(Node c=parent.getFirstChild();c!=null;c=c.getNextSibling())
		{
		if(isA(c,namespace,localName))
			{
			v.addElement( Element.class.cast(c));
			}
		}
	return v;
	}



/**
 * return a XPATH-like description of the node
 */
public static String node2path(Node n)
	{
	StringBuilder b=new StringBuilder();
	while(n!=null)
		{
		switch(n.getNodeType())
			{
			case Node.ATTRIBUTE_NODE:
				{
				b.insert(0,"/@"+n.getNodeName());
				n= Attr.class.cast(n).getOwnerElement();
				continue;
				}
			case Node.TEXT_NODE:
				{
				b.insert(0,"text()");
				break;
				}
			case Node.COMMENT_NODE:
				{
				b.insert(0,"comment()");
				break;
				}
			case Node.ELEMENT_NODE:
				{
				int L=getLevel(n);
				b.insert(0,"/"+n.getNodeName()+"["+(L+1)+"]");
				break;
				}
			}
		n=n.getParentNode();
		}
	return b.toString();
	}

/** return the level of a node */
public static int getLevel(Node n)
	{
	if(n==null) return -1;
	int L=0;
	while(n.getParentNode()!=null)
		{
		++L;
		n= n.getParentNode();
		}
	return L;
	}

}
