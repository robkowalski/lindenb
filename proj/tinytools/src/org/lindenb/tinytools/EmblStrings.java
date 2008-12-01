package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.io.IOUtils;
import org.lindenb.me.Me;
import org.lindenb.util.Compilation;
import org.lindenb.util.SHA1;



public class EmblStrings
	{
	//private static final String PSI="net:sf:psidev:mi";
	private XMLInputFactory inputFactory;
	
	private static class Candidate
		{
		String acn=null;
		String protName=null;
		double foldChange=0.0;
		}
	
	private List<Candidate> candidates= new ArrayList<Candidate>();
	
	private static class Base
		{
		protected String getAttribute(StartElement e,String name)
			{
			//BUG in StAX API ??? implemented this function due to exception at 
			// at com.ctc.wstx.sr.ElemAttrs.findMapIndex(ElemAttrs.java:190)
			for(Iterator<?> iter=e.getAttributes(); iter.hasNext();)
				{
				Attribute att= (Attribute)iter.next();
				if(name.equals(att.getName().getLocalPart()))
					{
					return att.getValue();
					}
				}
			return null;
			}
		
		protected void skip(XMLEventReader r,String localName)throws XMLStreamException
			{
			while((r.hasNext()))
				{
				XMLEvent evt= r.nextEvent();
				if(evt.isEndElement() && evt.asEndElement().getName().getLocalPart().equals(localName))
					{
					return;
					}
				}
			}
		}
	
	private static class ExperimentDescription
	extends Base
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
	extends Base
	{
	private String shortLabel;
	private String fullName;
	private String primaryRef;
	private Set<String> names= new HashSet<String>();
	
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
				else if(localName.equals("primaryRef") )
					{
					this.primaryRef=getAttribute(e,"id");
					if(this.primaryRef==null) throw new XMLStreamException("Cannot find @id in "+localName+" "+this.shortLabel);
					this.names.add(primaryRef.toUpperCase());
					}
				else if(localName.equals("secondaryRef")
					  )
					{
					String att=getAttribute(e,"refType");
					if(att!=null && "identity".equals(att))
						{
						this.names.add(getAttribute(e,"id").toUpperCase());
						}
					}
				else if(localName.equals("interactorType")
				  )
					{
					skip(r,localName);
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
	
	public Set<String> getNames() {
		return names;
		}
	
	public boolean hasName(String s)
		{
		return getNames().contains(s.toUpperCase());
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(this==obj) return true;
		if(obj==null || this.getClass()!=obj.getClass()) return false;
		boolean b= getPrimaryRef().equals(Interactor.class.cast(obj).getPrimaryRef());
		return b;
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
extends Base
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
					String iRef=r.getElementText();
					Interactor i= entrySet.id2interactor.get(iRef);
					if(i==null) throw new XMLStreamException("Cannot find interactor id"+iRef+" in "+entrySet.id2interactor.keySet());
					this.interactors[this.interactors[0]==null?0:1]=i;
					}
				else if(localName.equals("experimentRef"))
					{
					String exp= r.getElementText();
					if(!(exp.equals("0") || exp.length()==0))
						{
						this.experiment = entrySet.id2experiment.get(exp);
						if(this.experiment==null) throw new XMLStreamException("Cannot find experimentRef:"+exp+" in "+entrySet.id2experiment.keySet());
						}
					}
				else if(localName.equals("confidenceList"))
					{
					skip(r, localName);
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
	
	public Interactor first()
		{
		return this.interactors[0].getPrimaryRef().compareTo(this.interactors[1].getPrimaryRef()) < 0 ?
			this.interactors[0]:
			this.interactors[1]
			;
		}
	
	public Interactor second()
		{
		return this.interactors[0].getPrimaryRef().compareTo(this.interactors[1].getPrimaryRef()) < 0 ?
			this.interactors[1]:
			this.interactors[0]
			;
		}
	
	
	public boolean contains(Interactor i)
		{
		return first().equals(i) || second().equals(i);
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(this==obj) return true;
		if(obj==null || this.getClass()!=obj.getClass()) return false;
		Interaction cp=Interaction.class.cast(obj);
		
		return	(first().equals(cp.first()) &&  second().equals(cp.second()) )
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
	
	private Interactor partner1=null;
	private Set<Interaction> interactions= new HashSet<Interaction>();
	private Set<Interactor> interactors= new HashSet<Interactor>();
	
	private void build(String query) throws IOException,XMLStreamException
		{
		this.partner1=null;
		EntrySet entry= parse(query);
		
		this.interactions.addAll(entry.id2interaction.values());
		this.interactors.addAll(entry.id2interactor.values());
		if(this.interactions.isEmpty()) return;
		
		
		for(Interactor i:this.interactors)
			{
			if(i.hasName(query))
				{
				this.partner1=i;
				break;
				}
			}
		
		if(this.partner1==null)
			{
			throw new IOException("Cannot find root "+query+" in "+this.interactors);
			}
		
		for(Interactor i:new HashSet<Interactor>(this.interactors))//work on a copy
			{			
			entry= parse(i.getPrimaryRef());
			Set<Interaction> newinteractions= new HashSet<Interaction>(entry.id2interaction.values());
			newinteractions.removeAll(this.interactions);
			this.interactions.addAll(newinteractions);
			for(Interaction bind: newinteractions)
				{
				this.interactors.add(bind.first());
				this.interactors.add(bind.second());
				}
			}
		}
	
	
	
	
	
	public void toDot(PrintStream out)
		{
		out.println("Graph G {");
		Map<Interactor,Integer> prot2id= new HashMap<Interactor, Integer>();
		int dotId=0;
		
		for(Interactor interactor:this.interactors)
			{
			prot2id.put(interactor,++dotId);
			
			String morelabel="";
			for(Candidate candidate:this.candidates)
				{
				if(interactor.hasName(candidate.protName))
					{
					morelabel=" "+candidate.acn+" "+candidate.foldChange;
					break;
					}
				}
			
			out.print("p"+prot2id.get(interactor)+"[label=\""+interactor.getShortLabel()+morelabel+"\" ");
			if(this.partner1.equals(interactor))
				{
				out.println("style=filled  shape=box  fillcolor=blue  ");
				}
			else if(morelabel.length()>0)
				{
				out.println("style=filled  shape=pentagon  fillcolor=orange  ");
				}
			out.println("];");
			}
		
		for(Interaction link:this.interactions)
			{
			out.println("p"+prot2id.get(link.first())+" -- p"+ +prot2id.get(link.second()) +"[URL=\"http://www.ebi.ac.uk/intact/search/do/search?searchString="+ "A" +"&filter=ac\", label=\""+"B"+"\"];");
			}
		
		out.println("}");
		}

	
	private void debug(Object o)
		{
		System.err.println(String.valueOf(o));
		}
	
	private void readAltNames(File f) throws IOException
		{
		BufferedReader r= new BufferedReader(new FileReader(f));
		String line;
		while((line=r.readLine())!=null)
			{
			if(line.startsWith("#") && line.trim().length()==0) continue;
			String tokens[]=line.split("[\t]");
			Candidate c= new Candidate();
			c.acn=tokens[0];
			c.protName=tokens[1];
			c.foldChange=Double.parseDouble(tokens[2]);
			
			for(int i=0;i< candidates.size();++i)
				{
				Candidate c2= this.candidates.get(i);
				if(c2.acn.equalsIgnoreCase(c.acn)  )
					{
					if(Math.abs(c2.foldChange) < Math.abs(c.foldChange))
						{
						this.candidates.set(i, c);
						}
					c=null;
					break;
					}
				}
			if(c!=null) this.candidates.add(c);
			}
		r.close();
		}
	
	
	public static void main(String[] args)
		{
		try
			{
			EmblStrings app= new EmblStrings();
			String root=null;
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("EmblString "+Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
					System.err.println("Finds protein-protein interaction up to 2 degrees of freedom on Embl-Strings ");
					System.err.println("  -c use cache (default : false)");
					System.err.println("  -f <file> read alternate names (name <tab> alt-name <tab> weight )");
					System.err.println("  <root identifier>");
					System.err.println(Compilation.getLabel());
					}
				else if(args[optind].equals("-c"))
					{
					app.useCache=true;
					}
				else if(args[optind].equals("-f"))
					{
					app.readAltNames(new File(args[++optind]));
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
			if(optind+1==args.length)
		    	{
		    	root=args[optind++];
		    	}
		    else
		    	{
		    	System.err.println("illegal number of arguments");
		    	return;
		    	}
		    if(root==null)
		    	{
		    	System.err.println("root protein missing");
		    	return;
		    	}
			 app.build(root);
			 
			 PrintStream dot= new PrintStream(new FileOutputStream("/home/lindenb/jeter.dot"));
				app.toDot(dot);
				dot.flush();
				dot.close();
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}

}
