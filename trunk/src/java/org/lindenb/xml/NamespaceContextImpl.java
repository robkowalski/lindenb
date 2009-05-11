/**
 * 
 */
package org.lindenb.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

/**
 * @author lindenb
 * NamespaceContext: a context used in XPATH.setNamespaceContext
 *
 */
public class NamespaceContextImpl implements javax.xml.namespace.NamespaceContext
	{
	private Map<String, String> prefix2uri=new HashMap<String, String>();
	private Map<String, Set<String>> uri2prefix=new HashMap<String, Set<String>>();
	
	public NamespaceContextImpl()
		{
		
		}
	
	public void setPrefixURI(String prefix,String uri)
		{
		if(prefix==null || uri==null) return;
		prefix2uri.put(prefix,uri);
		Set<String> set= uri2prefix.get(prefix);
		if(set==null)
			{
			set= new HashSet<String>();
			uri2prefix.put(uri,set);
			}
		set.add(prefix);
		}
	
	
	@Override
	public String getNamespaceURI(String prefix) {
		if(prefix==null) throw new IllegalArgumentException("prefix cannot be null");
		if(prefix.equals(XMLConstants.XML_NS_PREFIX)) return XMLConstants.XML_NS_URI;
		if(prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		if(prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) return XMLConstants.NULL_NS_URI;
		String bound= prefix2uri.get(prefix);
		if(bound!=null) return bound;
		return  XMLConstants.NULL_NS_URI;//see javadoc
		}
	
	@Override
	public String getPrefix(String namespaceURI) {
		if(namespaceURI==null) throw new IllegalArgumentException("prefix cannot be null");
		if(namespaceURI.equals(XMLConstants.XML_NS_URI)) return XMLConstants.XML_NS_PREFIX;
		if(namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) return XMLConstants.XMLNS_ATTRIBUTE;
		if(namespaceURI.equals(XMLConstants.NULL_NS_URI)) return XMLConstants.DEFAULT_NS_PREFIX;
		Set<String> set= uri2prefix.get(namespaceURI);
		if(set==null || set.isEmpty()) return null;
		return set.iterator().next();
		}
	
	private static Iterator<?> makeIterator(String s)
		{
		List<String> L= new ArrayList<String>(1);
		L.add(s);
		return L.iterator();
		}
	
	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		if(namespaceURI==null) throw new IllegalArgumentException("prefix cannot be null");
		if(namespaceURI.equals(XMLConstants.XML_NS_URI)) return makeIterator(XMLConstants.XML_NS_PREFIX);
		if(namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) return makeIterator(XMLConstants.XMLNS_ATTRIBUTE);
		if(namespaceURI.equals(XMLConstants.NULL_NS_URI)) return makeIterator(XMLConstants.DEFAULT_NS_PREFIX);
		Set<String> set= uri2prefix.get(namespaceURI);
		if(set==null) return new ArrayList<String>(1).iterator();
		return set.iterator();
		}

}
