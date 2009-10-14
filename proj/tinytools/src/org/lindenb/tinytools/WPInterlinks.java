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
 * anwsers if a page has been copied in another project of wikipedia
 */
public class WPInterlinks
	{
	/** logger */
	private static final Logger LOG= Logger.getLogger(WPInterlinks.class.getName());

	/** xml parser factory */
	private XMLInputFactory xmlInputFactory;
	/** WP base URP */
	private String base_api="http://en.wikipedia.org/w/api.php";
	/** lang */
	private String lang="fr";
	/** inverse selection  */
	private boolean inverse_selection=false;
	
	
	
	/** private/empty cstor */
	private WPInterlinks()
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
	private void process(String page) throws IOException,XMLStreamException
		{
		final int lllimit=500;
		
		
		final QName att_lang=new QName("lang");
		final QName att_llcontinue=new QName("llcontinue");
		String llcontinue=null;
		String found=null;
		
		
		

		
		while(true)
			{			
			String url=	this.base_api+"?action=query" +
					"&prop=langlinks" +
					"&format=xml" +
					"&redirects"+
					(llcontinue!=null?"&llcontinue="+escape(llcontinue):"")+
					"&titles="+escape(page)+
					"&lllimit="+lllimit
					;
			llcontinue=null;
			
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
					
					Attribute langAtt=null;
					if(name.equals("ll") &&
						(langAtt=e.getAttributeByName(att_lang))!=null &&
						langAtt.getValue().equalsIgnoreCase(this.lang)
						)
						{
						found=reader.getElementText();
						llcontinue=null;
						break;
						}
					else if(name.equals("langlinks"))
						{
						Attribute llcont= e.getAttributeByName(att_llcontinue);
						if(llcont!=null)
							{
							llcontinue=llcont.getValue();
							}
						}
					}
				}
			reader.close();
			if(llcontinue==null) break;
			}
		if(found!=null && !inverse_selection)
			{
			System.out.println(found);
			}
		else if(found==null && inverse_selection)
			{
			System.out.println(page);
			}
		}
	
	public static void main(String[] args)
		{
		LOG.setLevel(Level.OFF);
		WPInterlinks app= new WPInterlinks();
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Find if a page was translated in wikipedia.");
					System.err.println(Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
					System.err.println(" -log-level <java.util.logging.Level> default:"+LOG.getLevel());
					System.err.println(" -api <url> default:"+app.base_api);
					System.err.println(" -v  inverse selection.");
					System.err.println(" -l <lang> language to observe default:"+app.lang);
					System.err.println(" (stdin|pages-names)");
					return;
					}
				else if(args[optind].equals("-v"))
					{
					app.inverse_selection=!app.inverse_selection;
					}
				else if(args[optind].equals("-log-level"))
					{
					LOG.setLevel(Level.parse(args[++optind]));
					}
				else if(args[optind].equals("-api"))
					{
					app.base_api=args[++optind];
					}
				else if(args[optind].equals("-l") ||
						args[optind].equals("-lang") ||
						args[optind].equals("-L"))
					{
					app.lang=args[++optind];
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
                    LOG.info("processing "+fname);
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
