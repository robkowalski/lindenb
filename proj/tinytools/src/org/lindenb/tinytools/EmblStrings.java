package org.lindenb.tinytools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Compilation;
import org.lindenb.util.SHA1;
import org.lindenb.xml.NodeWrapper;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EmblStrings
	{
	private static final String PSI="net:sf:psidev:mi";
	private XMLInputFactory inputFactory;

	
	private EmblStrings()
		{
		this.inputFactory= XMLInputFactory.newInstance();
		this.inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		this.inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		this.inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		}
	
	private boolean useCache=false;
	private InputStream open(String url) throws IOException
		{
		debug(url);
		if(!useCache)
			{
			return new URL(url).openStream();
			}
		File f= new File(System.getProperty("java.io.tmpdir","."),SHA1.encrypt(url)+".xml");
		if(f.exists())
			{
			return new FileInputStream(f);
			}
		InputStream in= new URL(url).openStream();
		OutputStream out= new FileOutputStream(f);
		IOUtils.copyTo(in, out);
		out.flush();out.close();
		in.close();
		
		return new FileInputStream(f);
		}
	
	
	private static class ExperimentDescription
		{
		private Set<String> pmids=new HashSet<String>();
		ExperimentDescription(XMLEventReader r)throws XMLStreamException
			{
			while((r.hasNext()))
				{
				XMLEvent evt= r.nextEvent();
				if(evt.isStartElement())
					{
					StartElement e=evt.asStartElement();
					String localName=e.getName().getLocalPart();
					if(localName.equals("primaryRef") || localName.equals("secondaryRef"))
						{
						Attribute att =e.getAttributeByName(new QName("db"));
						if(!"pubmed".equals(att.getValue())) continue;
						this.pmids.add( e.getAttributeByName(new QName("id")).getValue() );
						}
					}
				else if(evt.isEndElement())
					{
					EndElement e=evt.asEndElement();
					if(e.getName().getLocalPart().equals("experimentDescription"))
						{
						return;
						}
					}
				}
			}
		}
	
	
	private static class Interactor
		{
		private String shortLabel;
		private String fullName;
		private String primaryRef;
		Interactor(XMLEventReader r)throws XMLStreamException
			{
			while((r.hasNext()))
				{
				XMLEvent evt= r.nextEvent();
				if(evt.isStartElement())
					{
					StartElement e=evt.asStartElement();
					String localName=e.getName().getLocalPart();
					if(localName.equals("shortLabel"))
						{
						this.shortLabel= r.getElementText();
						}
					else if(localName.equals("fullName"))
						{
						this.fullName= r.getElementText();
						}
					else if(localName.equals("primaryRef"))
						{
						this.primaryRef= e.getAttributeByName(new QName("id")).getValue();
						}
					}
				else if(evt.isEndElement())
					{
					EndElement e=evt.asEndElement();
					if(e.getName().getLocalPart().equals("interactor"))
						{
						return;
						}
					}
				}
			}
		
		public String getPrimaryRef() {
			return primaryRef;
		}
		public String getFullName() {
			return fullName;
		}
		public String getShortLabel() {
			return shortLabel;
		}
		
		@Override
		public boolean equals(Object obj)
			{
			if(this==obj) return true;
			if(obj==null || this.getClass()!=obj.getClass()) return false;
			return getPrimaryRef().equals(Interactor.class.cast(obj).getPrimaryRef());
			}
		
		
		@Override
		public int hashCode() {
			return getPrimaryRef().hashCode();
			}
		
		@Override
		public String toString() {
			return getShortLabel()+" : "+getFullName();
			}

		}
	
	private static class Interaction
		{
		private Interactor interactors[]=new Interactor[]{null,null};
		private ExperimentDescription experiment;
		Interaction(EntrySet entrySet, XMLEventReader r)throws XMLStreamException
			{
			while((r.hasNext()))
				{
				XMLEvent evt= r.nextEvent();
				if(evt.isStartElement())
					{
					StartElement e=evt.asStartElement();
					String localName=e.getName().getLocalPart();
					if(localName.equals("interactorRef"))
						{
						Interactor i= entrySet.id2interactor.get(r.getElementText());
						if(i==null) throw new XMLStreamException("Cannot find interactor");
						this.interactors[this.interactors[0]==null?0:1]=i;
						}
					else if(localName.equals("experimentRef"))
						{
						this.experiment = entrySet.id2experiment.get(r.getElementText());
						if(this.experiment==null) throw new XMLStreamException("Cannot find experimentRef");
						}
					}
				else if(evt.isEndElement())
					{
					EndElement e=evt.asEndElement();
					if(e.getName().getLocalPart().equals("interaction"))
						{
						return;
						}
					}
				}
			}
	
		@Override
		public int hashCode()
			{
			return (this.interactors[0].hashCode()+this.interactors[1].hashCode())%3571;
			}
		
		@Override
		public boolean equals(Object obj)
			{
			if(this==obj) return true;
			if(obj==null || this.getClass()!=obj.getClass()) return false;
			Interaction cp=Interaction.class.cast(obj);
			
			return	(this.interactors[0].equals(cp.interactors[0]) &&  this.interactors[1].equals(cp.interactors[1]) ) ||
				 	(this.interactors[0].equals(cp.interactors[1]) &&  this.interactors[1].equals(cp.interactors[0]) )
					;
			}
		@Override
		public String toString() {
			return "("+this.interactors[0].toString()+" / "+this.interactors[1]+")";
			}
		
		}
	
	private static class EntrySet
		{
		private Map<String, Interactor> id2interactor= new HashMap<String, Interactor>();
		private Map<String, Interaction> id2interaction= new HashMap<String, Interaction>();
		private Map<String,ExperimentDescription> id2experiment= new HashMap<String, ExperimentDescription>();
		
		EntrySet(XMLEventReader r)throws XMLStreamException
			{
			final QName idAtt = new QName("id");
			while((r.hasNext()))
				{
				XMLEvent evt= r.nextEvent();
				if(evt.isStartElement())
					{
					String id=null;
					StartElement e=evt.asStartElement();
					String localName=e.getName().getLocalPart();
					if(localName.equals("interactor"))
						{
						id=e.getAttributeByName(idAtt).getValue();
						id2interactor.put(id, new Interactor(r));
						}
					else if(localName.equals("interaction"))
						{
						id=e.getAttributeByName(idAtt).getValue();
						id2interaction.put(id, new Interaction(this,r));
						}
					else if(localName.equals("experimentDescription"))
						{
						id=e.getAttributeByName(idAtt).getValue();
						id2interaction.put(id, new Interaction(this,r));
						}
					else if(localName.equals("experimentDescription"))
						{
						id=e.getAttributeByName(idAtt).getValue();
						id2experiment.put(id, new ExperimentDescription(r));
						}
					
					}
				else if(evt.isEndElement())
					{
					EndElement e=evt.asEndElement();
					if(e.getName().getLocalPart().equals("entry"))
						{
						return;
						}
					}
				}
			}
		}
	
	private EntrySet parse(String identifier) throws XMLStreamException,IOException
		{
		EntrySet set=null;
		String url="http://string.embl.de/api/psi-mi/interactions?identifier="+URLEncoder.encode(identifier, "UTF-8");
		InputStream in=open(url);
		XMLEventReader r=this.inputFactory.createXMLEventReader(in);
		while((r.hasNext()))
			{
			XMLEvent evt= r.nextEvent();
			if(evt.isStartElement())
				{
				StartElement e=evt.asStartElement();
				if(e.getName().getLocalPart().equals("entry"))
					{
					set= new EntrySet(r);
					}
				}
			}
		in.close();
		return set;
		}
	
	
	private void build(String identifier) throws XMLStreamException,IOException
		{
		EntrySet set =  parse(identifier);
		}
	
	private void debug(Object o)
		{
		System.err.println(String.valueOf(o));
		}
	
	public static void main(String[] args)
		{
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					}
				else 
					{
					break;
					}
				++optind;
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}

}
