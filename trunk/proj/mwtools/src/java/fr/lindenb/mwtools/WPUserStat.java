package fr.lindenb.mwtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.me.Me;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;

/**
 * WPUserStat
 * Author: Pierre Lindenbaum
 * retrieves informations about a given user's edits in wikipedia
 */
public class WPUserStat
	{
	/** logger */
	private static final Logger LOG= Logger.getLogger(WPUserStat.class.getName());

	/** xml parser factory */
	private XMLInputFactory xmlInputFactory;
	/** WP base URP */
	private String base_api="http://en.wikipedia.org/w/api.php";
	/** namespaces */
	private Set<Integer> ucnamespaces= new HashSet<Integer>();
	/** use prefix */
	private boolean use_prefix=false;
	
	
	private static class Revision
		{
		Long id;
		String user;
		Long pageid;
		Integer ns;
		String title;
		String timestamp;
		String comment;
		boolean is_new;
		boolean is_minor;
		boolean is_top;
		
		
		public String toJSon()
			{
			return "{revid:"+id+",user:\""+user+"\",pageid:"+pageid+",ns:"+ns+",title:\""+
			C.escape(title)+"\",date:\"+"+timestamp+"\",comment:\""+C.escape(comment)+"\",new:"+is_new+
			",minor:"+is_minor+",top:"+is_top+"}";
			}
		
		@Override
		public String toString()
			{
			return toJSon();
			}
		}
	
	/** private/empty cstor */
	private WPUserStat()
		{
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		}
	
	
	
	/** escapes WP title */
	private String escape(String entry) throws IOException
		{
		return URLEncoder.encode(entry.replace(' ', '_'),"UTF-8");
		}
	
	/**
	 * Open a URL to the given stream, retry 10 times if it fails
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private InputStream openStream(String url) throws IOException
		{
		final int tryNumber=10;
		IOException lastError=null;
		URL net = new URL(url);
		for(int i=0;i< tryNumber;++i)
			{
			try
				{
				InputStream in=net.openStream();
				return in;
				}
			catch(IOException err)
				{
				lastError=err;
				LOG.info("Trying "+i+" "+err.getMessage());
				try {
					Thread.sleep(10000);//sleep 10secs
				} catch (Exception e) {
					
				}
				continue;
				}
			}
		throw lastError;
		}
	
	
	/**
	 * process user
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private void process(String userName) throws IOException,XMLStreamException
		{
		final int uclimit=500;
		final QName Attucstart=new QName("ucstart");
		final QName att_revid=new QName("revid");
		final QName att_pageid=new QName("pageid");
		final QName att_ns=new QName("ns");
		final QName att_title=new QName("title");
		final QName att_timestamp=new QName("timestamp");
		final QName att_comment=new QName("comment");
		final QName att_new=new QName("new");
		final QName att_top=new QName("top");
		final QName att_minor=new QName("minor");
		String ucstart=null;
		String ucnamespace=null;//default is ALL

		
		if(!this.ucnamespaces.isEmpty())
			{
			StringBuilder sb= new StringBuilder();
			for(Integer i:this.ucnamespaces)
				{
				if(sb.length()>0) sb.append("|");
				sb.append(String.valueOf(i));
				}
			ucnamespace=sb.toString();
			}
		
		List<Revision> revisions= new ArrayList<Revision>();
		
		while(true)
			{			
			String url=	this.base_api+"?action=query" +
					"&list=usercontribs" +
					"&format=xml" +
					(ucnamespace==null?"":"&ucnamespace="+ucnamespace)+
					(ucstart!=null?"&ucstart="+escape(ucstart):"")+
					(this.use_prefix?"&ucuserprefix=":"&ucuser=")+escape(userName)+
					"&uclimit="+uclimit
					;
			ucstart=null;
			
			LOG.info(url);
			XMLEventReader reader= this.xmlInputFactory.createXMLEventReader(
					openStream(url));
			
			while(reader.hasNext())
				{
				XMLEvent event = reader.nextEvent();
				if(event.isStartElement())
					{
					StartElement e=event.asStartElement();
					String name=e.getName().getLocalPart();
					
					if(name.equals("item"))
						{
						Revision r=new Revision();
						
						
						Attribute att=e.getAttributeByName(att_revid);
						if(att!=null) r.id=Long.parseLong(att.getValue());
						att=e.getAttributeByName(att_pageid);
						if(att!=null) r.pageid=Long.parseLong(att.getValue());
						att=e.getAttributeByName(att_ns);
						if(att!=null) r.ns=Integer.parseInt(att.getValue());
						att=e.getAttributeByName(att_title);
						if(att!=null) r.title=att.getValue();
						att=e.getAttributeByName(att_timestamp);
						if(att!=null) r.timestamp=att.getValue();
						att=e.getAttributeByName(att_comment);
						if(att!=null) r.comment=att.getValue();
						r.is_new=e.getAttributeByName(att_new)!=null;
						r.is_top=e.getAttributeByName(att_top)!=null;
						r.is_minor=e.getAttributeByName(att_minor)!=null;
						LOG.info(r.toString());
						revisions.add(r);
						}
					else if(name.equals("usercontribs"))
						{
						Attribute clcont= e.getAttributeByName(Attucstart);
						if(clcont!=null)
							{
							ucstart=clcont.getValue();
							}
						}
					}
				}
			reader.close();
			if(ucstart==null) break;
			}
		LOG.info("count("+userName+")="+revisions.size());
		}
	
	public static void main(String[] args)
		{
		LOG.setLevel(Level.OFF);
		WPUserStat app= new WPUserStat();
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Return informations about a given user in wikipedia.");
					System.err.println(Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
					System.err.println(" -log-level <java.util.logging.Level> default:"+LOG.getLevel());
					System.err.println(" -api <url> default:"+app.base_api);
					System.err.println(" -p  Retrieve contibutions for all users whose names begin with this value.");
					System.err.println(" -ns <int> restrict to given namespace default:all");
					System.err.println(" (stdin|user-names)");
					return;
					}
				else if(args[optind].equals("-ns"))
					{
					app.ucnamespaces.add(Integer.parseInt(args[++optind]));
					}
				else if(args[optind].equals("-log-level"))
					{
					LOG.setLevel(Level.parse(args[++optind]));
					}
				else if(args[optind].equals("-api"))
					{
					app.base_api=args[++optind];
					}
				else if(args[optind].equals("-p"))
					{
					app.use_prefix=true;
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
			
			
			
			
			if(optind==args.length)
                {
                String line;
                LOG.info("read from stdin");
                java.io.BufferedReader r= new BufferedReader(new InputStreamReader(System.in));
                while(( line=r.readLine())!=null)
                	{
                	app.process(line);
                	}
                r.close();
                }
            else
                {
                while(optind< args.length)
                        {
                        String fname=args[optind++];
                        LOG.info("opening "+fname);
                      	app.process(fname);
                        }
                }

			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		
		}
	}
