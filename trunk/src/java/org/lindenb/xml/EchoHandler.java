package org.lindenb.xml;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Handler echoing a XML file to a stream 
 * @author lindenb
 *
 */
public class EchoHandler
	extends DefaultHandler
	{
	private PrintWriter out;
	private boolean needToCloseTag=false;
	private List<String> prefixMapping=new ArrayList<String>();
	protected int depth=0;
	public EchoHandler(PrintWriter out)
		{
		this.out=out;
		}
	
	protected PrintWriter getWriter()
		{
		return out;
		}
	
	public void characters(char[] ch, int start, int length)
			throws SAXException
		{
		if(needToCloseTag) getWriter().print(">");
		getWriter().write(ch, start, length);
		getWriter().write("\n");
		needToCloseTag=false;
		}

	public void endDocument() throws SAXException {
		getWriter().flush();
		}

	public void endElement(String uri, String localName, String name)
			throws SAXException
		{
		depth--;
		if(needToCloseTag)
			{
			getWriter().print("/>");
			}
		else
			{
			getWriter().print("</"+name+">");
			}
		needToCloseTag=false;
		}

	public int getDepth() {
		return depth;
		}
	
	

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		characters(ch, start, length);
		}

	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		getWriter().print("<!azdazd TODO ");
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		if(needToCloseTag)
			{
			getWriter().print(">");
			}
		getWriter().print("<?"+target+" ");
		getWriter().print(data);
		getWriter().print(" ?>");
		needToCloseTag=false;
	}


	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		prefixMapping.add(prefix);
		prefixMapping.add(uri);
		
		}

	public void startDocument() throws SAXException {
		getWriter().print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		needToCloseTag=false;
		depth=0;
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException
		{
		if(needToCloseTag)
			{
			getWriter().print(">");
			}
		getWriter().print("<"+name);
		
		for(int i=0;i+1< prefixMapping.size();i+=2)
			{
			getWriter().print(" xmlns"+(prefixMapping.get(i).length()>0?":"+prefixMapping.get(i):"")+"=\""+prefixMapping.get(i+1)+"\"");
			}
		
		if(attributes.getLength()>0)
			{
			for(int i=0;i< attributes.getLength();++i)
				{
				getWriter().print(" ");
				getWriter().print(attributes.getQName(i));
				getWriter().print("=\"");
				getWriter().print(XMLUtilities.escape(attributes.getValue(i)));
				getWriter().print("\"");
				}
			}
		needToCloseTag=true;
		prefixMapping.clear();
		depth++;
		}
	
	public static void main(String[] args)
		{
		try {
			SAXParserFactory f=SAXParserFactory.newInstance();
			f.setNamespaceAware(true);
			f.setValidating(false);
			SAXParser p=f.newSAXParser();
			p.parse(new FileInputStream("/home/lindenb/jeter.svg"),
					new EchoHandler(new PrintWriter(System.out)));
			}
		catch (Exception e) {
			e.printStackTrace();
			}
		}
	
	
	}
