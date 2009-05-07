/**
 * 
 */
package org.lindenb.sw;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;


import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.FOAF;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.RDFS;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author pierre
 *
 */
public class PrefixMapping
{
private HashMap<String, String> uri2prefix= new HashMap<String, String>();

public PrefixMapping(boolean initialize)
	{
	if(initialize)
		{
		setNsPrefix("rdf", RDF.NS);
		setNsPrefix("rdfs", RDFS.NS);
		setNsPrefix("dc", DC.NS);
		setNsPrefix("foaf", FOAF.NS);
		}
	}

public PrefixMapping()
	{
	this(true);
	}

public void setNsPrefix(String prefix, String namespaceuri)
	{
	uri2prefix.put(namespaceuri,prefix);
	}
/**  Answer the prefix for the given URI, or null if there isn't one. */
public String getNsURIPrefix(java.lang.String uri) 
	{
	return this.uri2prefix.get(uri);
	}
/** Get the URI bound to a specific prefix, null if there isn't one.*/
public String getNsPrefixURI(String prefix) 
	{
	for(String ns: this.uri2prefix.keySet())
		{
		if(getNsURIPrefix(ns).equals(prefix))
			{
			return ns;
			}
		}
	return null;
	}

/**  Answer a QName with the expansion of the given uri, or null if no such qname can be constructed using the mapping's prefixes. */
public QName getQName(java.lang.String uri)
	{
	if(uri==null) return null;
	int n= uri.lastIndexOf('#');
	if(n==-1) n= uri.lastIndexOf('/');
	if(n==-1) return null;
	String ns= uri.substring(0,n+1);
	String prefix= getNsURIPrefix(ns);
	if(prefix==null) return null;
	return new QName(ns,uri.substring(n+1),prefix);
	}

/**  Answer a qname with the expansion of the given uri, or null if no such qname can be constructed using the mapping's prefixes. */
public String qnameFor(java.lang.String uri)
	{
	if(uri==null) return null;
	int n= uri.lastIndexOf('#');
	if(n==-1) n= uri.lastIndexOf('/');
	if(n==-1) return null;
	String prefix= getNsURIPrefix(uri.substring(0,n+1));
	if(prefix==null) return null;
	return prefix+":"+uri.substring(n+1);
	}

/** Compress the URI using the prefix mappings if possible.*/
public String shortForm(java.lang.String uri)
	{
	String s= qnameFor(uri);
	return s==null?uri:s;
	}


/** list all prefixes */
public Set<String> getPrefixes()
	{
	Set<String> set= new HashSet<String>(this.uri2prefix.size());
	set.addAll(this.uri2prefix.values());
	return set;
	}

/** add all the prefixes namespaces declared in a dom element */
public void addNamespaces(Element root)
	{
	NamedNodeMap atts=root.getAttributes();
	for(int j=0;j< atts.getLength();++j)
		{
		Attr att= Attr.class.cast(atts.item(j));
		if(!att.getName().startsWith("xmlns:")) continue;
		String prefix= att.getName().substring(6);
		this.setNsPrefix(prefix, att.getValue());
		}
	}
}
