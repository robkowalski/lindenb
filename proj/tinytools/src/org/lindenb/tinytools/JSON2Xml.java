package org.lindenb.tinytools;

import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Compilation;

/**
 * ransform Json to XML, so I can transform it with xslt
 * @author lindenb
 *
 */
public class JSON2Xml
{
public void echo(Object o) throws XMLStreamException
	{
	XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
	XMLStreamWriter w=xmlfactory.createXMLStreamWriter(System.out);
	w.writeStartDocument();
	write(w,o);
	w.writeEndDocument();
	w.flush();
	w.close();
	}

private void write(XMLStreamWriter w,Object o) throws XMLStreamException
	{
	if(o==null)
		{
		w.writeEmptyElement("null");
		}
	else if(o instanceof List)
		{
		w.writeStartElement("array");
		List<?> L= List.class.cast(o);
		for(int i=0;i< L.size();++i)
			{
			w.writeStartElement("item");
			attribute(w,L.get(i));
			if(L.get(i)!=null) write(w,L.get(i));
			w.writeEndElement();
			}
		w.writeEndElement();
		}
	else if(o.getClass().isArray())
		{
		w.writeStartElement("array");
		Object L[]= (Object[])(o);
		for(int i=0;i< L.length;++i)
			{
			w.writeStartElement("item");
			attribute(w,L[i]);
			if(L[i]!=null) write(w,L[i]);
			w.writeEndElement();
			}
		w.writeEndElement();
		}
	else if(o instanceof Map)
		{
		w.writeStartElement("object");
		Map<?,?> M= Map.class.cast(o);
		for(Object key: M.keySet())
			{
			w.writeStartElement("property");
			w.writeAttribute("name", key.toString());
			attribute(w,M.get(key));
			if(M.get(key)!=null) write(w,M.get(key));
			w.writeEndElement();
			}
		w.writeEndElement();
		}
	else
		{
		w.writeCharacters(String.valueOf(o));
		}
	}


private void attribute(XMLStreamWriter w,Object o) throws XMLStreamException
	{
	if(o==null)
		{
		w.writeAttribute("type", "null");
		}
	else if(o instanceof Boolean)
		{
		w.writeAttribute("type", "boolean");
		}
	else if(o instanceof Number)
		{
		w.writeAttribute("type", "number");
		}
	}

public static void main(String[] args) {
	try {
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println("Transform Json to XML, so I can transform it with xslt.");
				System.err.println(Compilation.getLabel());
				System.err.println("-h this screen");
				System.err.println("(stdin|file)");
				return;
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
	    org.lindenb.json.Parser parser=new org.lindenb.json.Parser();
	    Object o=null;
	    if(args.length==0)
	    	{
	    	o=parser.parse(System.in);
	    	}
	    else if(optind+1==args.length)
	    	{
	    	o=parser.parse(IOUtils.openInputStream(args[optind++]));
	    	}
	    else
	    	{
	    	System.err.println("Illegal number of arguments");
	    	}
	    JSON2Xml app= new JSON2Xml();
	    app.echo(o);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
