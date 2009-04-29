package org.lindenb.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import org.lindenb.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Sax2Dom
	extends DefaultHandler
	{
	protected Document dom;
	protected Node currentNode=null;
	protected List<String> nsMapping=new ArrayList<String>();
	
	
	public Sax2Dom(DocumentBuilder domBuilder)
		{
		this.dom= domBuilder.newDocument();
		}
	@Override
	public void startDocument() throws SAXException {
		this.currentNode=this.dom;
		}
	
	@Override
	public void endDocument() throws SAXException {
		this.currentNode=null;
		while(this.dom.hasChildNodes())
			{
			this.dom.removeChild(this.dom.getFirstChild());
			}
		}
	
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		this.currentNode.appendChild(this.dom.createProcessingInstruction(target, data));
		}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		final String text = new String(ch, start, length);
		this.currentNode.appendChild(this.dom.createTextNode(text));
		}
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		this.nsMapping.add(prefix);
		this.nsMapping.add(uri);
		}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException
		{
		
		org.w3c.dom.Element e= null;
		
		if(uri!=null)
			{
			e=this.dom.createElementNS(uri, name);
			}
		else
			{
			e=this.dom.createElement(name);
			}
		currentNode.appendChild(e);
		currentNode=e;
		
		for(int i=0; i+1< nsMapping.size();i+=2)
			{
			String prefix= nsMapping.get(i);
			String ns = nsMapping.get(i+1);
			 if (StringUtils.isEmpty(prefix))
			 	{
				e.setAttributeNS(XMLConstants.XML_NS_URI, "xmlns",ns);
				}
			else
				{
				e.setAttributeNS(XMLConstants.XML_NS_URI, "xmlns:"+prefix,ns);
				}
			}
		
		final int nAtt= attributes.getLength();
		for(int i=0; i< nAtt;++i)
			{
			 if (attributes.getLocalName(i) == null)
			 	{
				e.setAttribute(attributes.getQName(i), attributes.getValue(i));
				}
			else
				{
				e.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
				}
			}
		
		nsMapping.clear();
		}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException
		{
		this.currentNode=this.currentNode.getParentNode();
		}
	}
