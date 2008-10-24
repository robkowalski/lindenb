package org.lindenb.wikipedia.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class MWQuery
	{
	private static final QName AttTitle=new QName("title");
	private static final QName AttEicontinue=new QName("eicontinue");
	private static final QName AttRvstartid=new QName("rvstartid");
	
	private static final QName AttRevId=new QName("revid");
	private static final QName AttSize =new QName("size");
	private static final QName AttUser=new QName("user");
	private static final QName AttTimestamp=new QName("timestamp");
	private static final QName AttComment=new QName("comment");
	
	private static final SimpleDateFormat DATE_FORMAT= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private String base;
	private XMLInputFactory xmlInputFactory;
	
	public MWQuery()
		{
		this(Wikipedia.BASE);
		}
	
	public MWQuery(String base)
		{
		this.base=base;
		this.xmlInputFactory = XMLInputFactory.newInstance();
		this.xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
		this.xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		}
	
	protected String escape(Entry entry) throws IOException
		{
		return URLEncoder.encode(entry.getQName().replace(' ', '_'),"UTF-8");
		}
	
	protected XMLEventReader open(String url) throws IOException,XMLStreamException
		{
		URL net = new URL(url);
		InputStream in=net.openStream();
		return this.xmlInputFactory.createXMLEventReader(in);
		}
	
	public String getBase()
		{
		return this.base;
		}
	
	public String getBaseApi()
		{
		return getBase()+"/w/api.php";
		}
	
	protected int getLimit()
		{
		return 500;
		}
	
	public Collection<Revision> listRevisions(Entry entry) throws IOException
		{
		return listRevisions(entry,null);
		}
	
	public Collection<Revision> listRevisions(Entry entry, QueryFilter.Revision filter) throws IOException
		{
		Set<Revision> revisions= new TreeSet<Revision>(new Comparator<Revision>()
			{
			@Override
			public int compare(Revision o1, Revision o2) {
				return o1.getDate().compareTo(o2.getDate());
				}
			});
		String rvstartid=null;
		boolean dirty=true;
		try
			{
			while(dirty)
				{
				dirty=false;
				String url=getBaseApi()+"?action=query" +
						"&format=xml" +
						"&prop=revisions" +
						(rvstartid!=null?"&rvstartid="+rvstartid:"")+
						"&titles="+escape(entry)+
						"&rvlimit=15" +
						"&rvprop=ids|flags|timestamp|user|size|comment" 
						;
				
				XMLEventReader reader= open(url);
				/** loop until we find a rdf:RDF element */
				while(reader.hasNext())
					{
					XMLEvent event = reader.nextEvent();
					if(event.isStartElement())
						{
						StartElement e=event.asStartElement();
						String name=e.getName().getLocalPart();
						
						if(name.equals("rev"))
							{
							Attribute user =e.getAttributeByName(AttUser);
							Attribute timestamp =e.getAttributeByName(AttTimestamp);
							Attribute comment =e.getAttributeByName(AttComment);
							Attribute size =e.getAttributeByName(AttSize);
							Attribute revid =e.getAttributeByName(AttRevId);
							
							if(revid!=null &&
							   user!=null &&
							   timestamp!=null &&
							   comment!=null &&
							   size!=null)
								{
								Revision rev=new Revision(
									entry,
									DATE_FORMAT.parse(timestamp.getValue()),
									new User(user.getValue()),
									Integer.parseInt(size.getValue()),
									comment.getValue()
									);
								if(filter==null || filter.accept(rev))
									{
									if(revisions.add(rev))
										{
										dirty=true;
										}
									}
								}
							}
						else if(name.equals("revisions"))
							{
							Attribute attstartid = e.getAttributeByName(AttRvstartid);
							if(attstartid!=null)
								{
								rvstartid=attstartid.getValue();
								}
							}
						}
					}
				reader.close();
				if(rvstartid==null) break;
				}
			return revisions;
			}
		catch(ParseException err)
			{
			throw new IOException(err);
			}
		catch(XMLStreamException err)
			{
			throw new IOException(err);
			}
		}
	
	public Collection<Page> listPagesEmbedding(Entry entry) throws IOException
		{
		Set<Page> pages = new TreeSet<Page>();
		String eicontinue=null;
		boolean dirty=true;
		try
			{
			while(dirty)
				{
				dirty=false;
				String url= getBaseApi()+"?action=query" +
					"&format=xml" +
					"&list=embeddedin" +
					"&eititle="+escape(entry)+"" +
					"&einamespace=0"+
					(eicontinue==null?"":"&eicontinue="+eicontinue)+
					"&eilimit="+getLimit()
					;
				
				XMLEventReader reader = open(url);
				/** loop until we find a rdf:RDF element */
				while(reader.hasNext())
					{
					XMLEvent event = reader.nextEvent();
					if(event.isStartElement())
						{
						StartElement e=event.asStartElement();
						String name=e.getName().getLocalPart();
						if(name.equals("ei"))
							{
							Attribute att=e.getAttributeByName(AttTitle);
							if(att!=null)
								{
								Page page= new Page(att.getValue());
								
								if(pages.add(page))
									{
									dirty=true;
									}
								}
							}
						else if(name.equals("embeddedin"))
							{
							Attribute att=e.getAttributeByName(AttEicontinue);
							if(att!=null)
								{
								eicontinue=att.getValue();
								}
							}
						}
					}
				reader.close();
				if(eicontinue==null) break;
				}
			return pages;
			}
		catch(XMLStreamException err)
			{
			throw new IOException(err);
			}
		}
	
	
	public static void main(String[] args) {
		try {
			MWQuery app = new MWQuery();
			int i=0;
			for(Page r:app.listPagesEmbedding(new Template("PBB Controls")))
				{
				System.err.println(r+" "+(++i));
				}
			
			for(Revision r:app.listRevisions(new Page("Rotavirus")))
				{
				System.err.println(r);
				}
			
			} 
		catch (Exception e) {
			e.printStackTrace();
			}
		}
	
	}
