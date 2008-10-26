package org.lindenb.wikipedia.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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
	private static final QName AttClcontinue=new QName("clcontinue");
	private static final QName AttPlcontinue=new QName("plcontinue");

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
		final int tryNumber=10;
		IOException lastError=null;
		URL net = new URL(url);
		for(int i=0;i< tryNumber;++i)
			{
			try
				{
				InputStream in=net.openStream();
				return this.xmlInputFactory.createXMLEventReader(in);
				}
			catch(IOException err)
				{
				lastError=err;
				System.err.println("Trying "+i+" "+err.getMessage());
				try {
					Thread.sleep(10000);//sleep 10secs
				} catch (Exception e) {
					
				}
				continue;
				}
			}
		throw lastError;
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
	
	public CategoryTree getParentalCategoryTree(Category category)  throws IOException
		{
		CategoryTree tree= new CategoryTree(category);
		Set<Category> seen=new HashSet<Category>();
		_getParentalCategoryTree(tree,seen);
		return tree;
		}
	
	private void _getParentalCategoryTree(CategoryTree root,Set<Category> seen)  throws IOException
		{
		if(!seen.add(root.getCategory())) return;
		System.err.println(root);
		Set<CategoryTree> remains= new HashSet<CategoryTree>();
		
		for(Category parent:listCategories(root.getCategory()))
			{
			CategoryTree node=new CategoryTree(parent);
			node.getChildren().add(root);
			
			if(root.getParents().add(node))
				{
				System.err.println(" add "+root+" -> "+node.getCategory());
				remains.add(node);
				}
			}
		for(CategoryTree parent:remains)
			{
			_getParentalCategoryTree(parent,seen);
			}
		}
	/*
	public Set<Category> listParentCategories(Category category)  throws IOException
		{
		boolean dirty=true;
		Set<Category> categories= new TreeSet<Category>();
		try
			{
			String plcontinue=null;
			while(dirty)
				{
				dirty=false;
				
				String url=getBaseApi()+"?action=query" +
						"&format=xml" +
						"&prop=links" +
						(plcontinue!=null?"&plcontinue="+plcontinue:"")+
						"&titles="+escape(category)+
						"&pllimit=" +getLimit()+
						"&plnamespace="+MWNamespace.Category.getId()
						;
				System.err.println(url);
				XMLEventReader reader= open(url);
				
				while(reader.hasNext())
					{
					XMLEvent event = reader.nextEvent();
					if(event.isStartElement())
						{
						StartElement e=event.asStartElement();
						String name=e.getName().getLocalPart();
						
						if(name.equals("pl"))
							{
							Attribute cat =e.getAttributeByName(AttTitle);
							
							if(cat!=null)
								{
								Category parent=new Category(cat.getValue());
								
								if(categories.add(parent))
									{
									dirty=true;
									}
								}
							}
						else if(name.equals("links"))
							{
							Attribute clcont= e.getAttributeByName(AttPlcontinue);
							if(clcont!=null)
								{
								plcontinue=clcont.getValue();
								}
							}
						}
					}
				reader.close();
				if(plcontinue==null) break;
				}
			return categories;
			}
		catch(XMLStreamException err)
			{
			throw new IOException(err);
			}
		}*/
	
	public Collection<Category> listCategories(Entry entry)  throws IOException
		{
		boolean dirty=true;
		Set<Category> categories= new TreeSet<Category>();
		try
			{
			String clcontinue=null;
			while(dirty)
				{
				dirty=false;
				
				String url=getBaseApi()+"?action=query" +
						"&format=xml" +
						"&prop=categories" +
						(clcontinue!=null?"&clcontinue="+clcontinue:"")+
						"&titles="+escape(entry)+
						"&cllimit="+getLimit()
						;
				
				XMLEventReader reader= open(url);
				
				while(reader.hasNext())
					{
					XMLEvent event = reader.nextEvent();
					if(event.isStartElement())
						{
						StartElement e=event.asStartElement();
						String name=e.getName().getLocalPart();
						
						if(name.equals("cl"))
							{
							Attribute cat =e.getAttributeByName(AttTitle);
							
							if(cat!=null)
								{
								Category rev=new Category(
									cat.getValue()
									);
								
								if(categories.add(rev))
									{
									dirty=true;
									}
									
								}
							}
						else if(name.equals("categories"))
							{
							Attribute clcont= e.getAttributeByName(AttClcontinue);
							if(clcont!=null)
								{
								clcontinue=clcont.getValue();
								}
							}
						}
					}
				reader.close();
				if(clcontinue==null) break;
				}
			return categories;
			}
			
			catch(XMLStreamException err)
				{
				throw new IOException(err);
				}
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
						"&rvlimit=" + getLimit()+
						"&rvprop=ids|flags|timestamp|user|size|comment" 
						;
				XMLEventReader reader= open(url);
				
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
							   comment!=null
							   )//!size can be null
								{
								Revision rev=new Revision(
									Integer.parseInt(revid.getValue()),
									entry,
									DATE_FORMAT.parse(timestamp.getValue()),
									new User(user.getValue()),
									(size==null?-1:Integer.parseInt(size.getValue())),
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
			
			//CategoryTree tree=app.getParentalCategoryTree(new Category("Proteins"));
			//tree.toRDF(System.out,Wikipedia.BASE+"/wiki/");
			
			
			
			System.err.println("XX");
			
			for(Category r:app.listCategories(new Category("French biologists")))
				{
				System.err.println(r);
				}
			
			if(1==1) return;
			for(Category r:app.listCategories(new Page("Albert Einstein")))
				{
				System.err.println(r);
				}
			
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
