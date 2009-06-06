package org.lindenb.mwrdf;



import org.lindenb.sw.PrefixMapping;
import org.lindenb.xml.NodeWrapper;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Schema
	{
	public static final String NS="http://mwrdf.lindenb.org";
	private class Atom extends NodeWrapper<Element>
		{
		Atom(Element e)
			{
			super(e);
			}
		}
	
	
	private Document dom;
	private PrefixMapping prefixMapping= new PrefixMapping();
	
	public Schema(Document dom)
		{
		this.dom=dom;
		
		Element root=dom.getDocumentElement();
		for(Element e: XMLUtilities.elements(root, NS, "namespace"))
			{
			prefixMapping.setNsPrefix(
				e.getAttribute("prefix"),
				e.getAttribute("uri")
				);
			}
		}
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
		}
	
	public String createEmptyRDFDocument()
		{
		StringBuilder b= new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
		b.append(getPrefixMapping().createDocType());
		b.append("<rdf:RDF\n");
		for(String p:getPrefixMapping().getPrefixes())
			{
			b.append("  xmlns:"+p+"=\"&"+p+";\"\n");
			}
		b.append("  >\n");
		
		b.append("</rdf:RDF>");
		return b.toString();
		}
	
	}
