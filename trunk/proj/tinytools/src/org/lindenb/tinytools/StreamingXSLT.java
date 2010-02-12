package org.lindenb.tinytools;


import java.io.InputStream;


import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Compilation;

import org.lindenb.xml.Sax2Dom;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * StreamingXSLT
 * @author pierre
 *
 */
public class StreamingXSLT
{
private Transformer transformer=null;
private int depth=-1;
private String qNameTarget=null;

private StreamingXSLT() throws Exception
	{
	}

/**
 * Builds the DOM for each fragment
 * 
 */
private class Sax2dom
	extends Sax2Dom
	{
	private int indexInStream=0;
	
	Sax2dom(DocumentBuilder builder) throws Exception
		{
		super(builder);
		}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		this.indexInStream=0;
		}
	
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException
		{
		Node current = super.getCurrentNode();
		boolean b=process(this.dom,Element.class.cast(current),this.indexInStream);
		super.endElement(uri, localName, name);
		current = super.getCurrentNode();
		if(b)
			{
			++this.indexInStream;
			while(current.hasAttributes())
				{
				Attr att = (Attr)current.getAttributes().item(0);
				Element.class.cast(current).removeAttributeNode(att);
				}
			while(current.hasChildNodes())
				{
				current.removeChild(current.getFirstChild());
				}
			}
		}
	
	}

/**
 * process this Node return true if it should removed 
 * @param dom
 * @param node
 * @param indexInStream
 * @return true if node has been processed and should be removed from the fragment
 * @throws SAXException
 */
private boolean process(
		Document dom,
		Element node,
		int indexInStream) throws SAXException
	{
	if(this.depth!=-1 && depth!=XMLUtilities.getLevel(node)) return false;
	if(this.qNameTarget!=null && node.getTagName().equals(this.qNameTarget)==false) return false;
	
	Source src= new DOMSource(dom);
	Result result= new StreamResult(System.out);
	try {
		this.transformer.setParameter("nodeIndex", indexInStream);
		this.transformer.transform(src, result);
		} 
	catch (TransformerException e)
		{
		throw new SAXException(e);
		}
	return true;
	}


private void parse(InputStream in) throws Exception
	{
	DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
	domFactory.setNamespaceAware(true);
	domFactory.setCoalescing(true);
	domFactory.setExpandEntityReferences(true);
	domFactory.setValidating(false);
	DocumentBuilder builder= domFactory.newDocumentBuilder();
	
	SAXParserFactory saxFactory= SAXParserFactory.newInstance();
	saxFactory.setValidating(false);
	saxFactory.setNamespaceAware(true);
	SAXParser parser= saxFactory.newSAXParser();
	parser.parse(new InputSource(in), new Sax2dom(builder));
	}

public static void main(String[] args) {
	try {
		StreamingXSLT app= new StreamingXSLT();
		int optind=0;
	    String stylesheet=null;
	    List<String> parameters= new ArrayList<String>();
		while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println(Compilation.getLabel());
				System.err.println("  -h this screen");
				System.err.println("  -x <xslt-stylesheet file/url> required");
				System.err.println("  -p <param-name> <param-value> (add parameter to the xslt engine)");
				System.err.println("  -d depth (0 based) default:-1");
				System.err.println("  -q qName target default:null" );
				System.err.println("   <file>|stdin");
				return;
				}
			 else if (args[optind].equals("-x"))
			     {
				 stylesheet=args[++optind];
			     }
			 else if (args[optind].equals("-d"))
			     {
				 app.depth= Integer.parseInt(args[++optind]);
			     }
			 else if (args[optind].equals("-p"))
			     {
				 parameters.add(args[++optind]);
				 parameters.add(args[++optind]);
			     }
			
			 else if (args[optind].equals("-q"))
			     {
				 app.qNameTarget= args[++optind];
			     }
			 else if (args[optind].equals("--"))
			     {
			     ++optind;
			     break;
			     }
			else if (args[optind].startsWith("-"))
			     {
			     System.err.println("bad argument " + args[optind]);
			     System.exit(-1);
			     }
			else
			     {
			     break;
			     }
			++optind;
			}
		if(stylesheet==null)
			{
			System.err.println("Undefined Stylesheet");
			return ;
			}
		TransformerFactory transformerFactory=TransformerFactory.newInstance();
		Templates templates = transformerFactory.newTemplates(new StreamSource(
				IOUtils.openReader(stylesheet)
			));
		app.transformer= templates.newTransformer();
		for(int i=0;i+1< parameters.size();i+=2)
			{
			app.transformer.setParameter(parameters.get(i), parameters.get(i+1));
			}
		if(optind==args.length)
			{
			app.parse(System.in);
			}
		else
			{
			while(optind< args.length)
				{
				InputStream r = IOUtils.openInputStream(args[optind++]);
				app.parse(r);
				r.close();
				}
			}
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
