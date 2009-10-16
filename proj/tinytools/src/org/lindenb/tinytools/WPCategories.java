package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
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
import org.lindenb.util.Compilation;

/**
 * WPUserStat
 * Author: Pierre Lindenbaum
 * retrieves the categories of a given article  in wikipedia
 */
public class WPCategories
	{
	/** logger */
	private static final Logger LOG= Logger.getLogger(WPCategories.class.getName());

	/** xml parser factory */
	private XMLInputFactory xmlInputFactory;
	/** WP base URP */
	private String base_api="http://en.wikipedia.org/w/api.php";


	

	
	/** private/empty cstor */
	private WPCategories()
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
	private void process(String entryName) throws IOException,XMLStreamException
		{
		final int cllimit=500;
		final QName att_clcontinue=new QName("clcontinue");
		
		final QName att_title=new QName("title");
		
		String clcontinue=null;
		
		
		while(true)
			{			
			String url=	this.base_api+"?action=query" +
					"&prop=categories" +
					"&format=xml" +
					(clcontinue!=null?"&clcontinue="+escape(clcontinue):"")+
					"&titles="+escape(entryName)+
					"&cllimit="+cllimit
					;
			clcontinue=null;
			
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
					Attribute att=null;
					if(name.equals("cl") &&
					  (att=e.getAttributeByName(att_title))!=null)
						{
						System.out.println(entryName+"\t"+att.getValue());
						}
					else if(name.equals("categories") &&
							(att=e.getAttributeByName(att_clcontinue))!=null)
						{
						clcontinue=att.getValue();
						}
					}
				}
			reader.close();
			if(clcontinue==null) break;
			}
		}
	
	public static void main(String[] args)
		{
		LOG.setLevel(Level.OFF);
		WPCategories app= new WPCategories();
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Return categories about a given set of articles in wikipedia.");
					System.err.println(Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
					System.err.println(" -log-level <java.util.logging.Level> default:"+LOG.getLevel());
					System.err.println(" -api <url> default:"+app.base_api);
					System.err.println(" (stdin|articles-names)");
					return;
					}
				else if(args[optind].equals("-log-level"))
					{
					LOG.setLevel(Level.parse(args[++optind]));
					}
				else if(args[optind].equals("-api"))
					{
					app.base_api=args[++optind];
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
