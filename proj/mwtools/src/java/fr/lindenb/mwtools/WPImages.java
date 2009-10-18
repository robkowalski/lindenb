package fr.lindenb.mwtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
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
import org.lindenb.sw.vocabulary.XHTML;
import org.lindenb.util.Compilation;
import org.lindenb.xml.XMLUtilities;

/**
 * WPUserStat
 * Author: Pierre Lindenbaum
 * retrieves the images of a given article  in wikipedia
 */
public class WPImages
	{
	private static enum Mode
		{
		text,xhtml,wiki
		}
	/** logger */
	private static final Logger LOG= Logger.getLogger(WPImages.class.getName());

	/** xml parser factory */
	private XMLInputFactory xmlInputFactory;
	/** WP base URP */
	private String base_api="http://en.wikipedia.org/w/api.php";
	/** html output */
	private Mode output_type=Mode.text;
	/** image width for html */
	private int iiurlwidth=64;

	
	/** private/empty cstor */
	private WPImages()
		{
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		}
	
	
	
	/** escapes WP title */
	private String escape(String entry) throws IOException
		{
		entry=entry.replace(' ', '_');
		if(1!=2) return entry;
		int i=entry.indexOf(":");
		if(i!=-1)
			{
			return entry.substring(0,i+1)+ URLEncoder.encode(entry.substring(i+1),"UTF-8");
			}
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
	
	static private String attr(StartElement e, String attName)
		{
		Attribute att=e.getAttributeByName(new QName(attName));
		return att==null?"":att.getValue();
		}
	
	/**
	 * processImage
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private void processImage(String entryName,String imageTitle) throws IOException,XMLStreamException
		{
		
		LOG.info(entryName+" "+imageTitle);
		boolean found=false;
		String url=	this.base_api+"?action=query" +
				"&prop=imageinfo" +
				(output_type==Mode.xhtml?"&iiurlwidth="+iiurlwidth:"")+
				"&format=xml" +
				"&titles="+escape(imageTitle)+
				"&iiprop=timestamp|user|comment|url|size|dimensions|mime|archivename|bitdepth"
				;
		
		LOG.info(url);
		XMLEventReader reader= this.xmlInputFactory.createXMLEventReader(
				openStream(url));
		
		while(reader.hasNext())
			{
			XMLEvent event = reader.nextEvent();
			if(event.isStartElement())
				{
				StartElement e=event.asStartElement();
				if(e.getName().getLocalPart().equals("ii"))
					{
					found=true;
					switch(this.output_type)
						{
						case text:
							{
							System.out.print(entryName+"\t");
							System.out.print(imageTitle+"\t");
							System.out.print(attr(e,"timestamp")+"\t");
							System.out.print(attr(e,"user")+"\t");
							System.out.print(attr(e,"size")+"\t");
							System.out.print(attr(e,"width")+"\t");
							System.out.print(attr(e,"height")+"\t");
							System.out.print(attr(e,"url")+"\t");
							System.out.print(attr(e,"descriptionurl"));
							System.out.println();
							break;
							}
						case xhtml:
							{
							System.out.println("<span>");
							System.out.println("<a href=\""+
									attr(e,"descriptionurl")+
								"\">");
							System.out.println(
								"<img src=\""+attr(e,"thumburl")+"\" " +
								" width=\""+ 	attr(e,"thumbwidth") +"\" "+
								" height=\""+ 	attr(e,"thumbheight") +"\" "+
								" alt=\""+ 	XMLUtilities.escape(imageTitle) +"\" "+
								 "/>");
							System.out.println("</a>");
							System.out.println("</span>");
							break;
							}
						case wiki:
							{
							System.out.println("|"+imageTitle.replace(' ', '_')+"|");
							break;
							}
						}
					}
				}
			}
		reader.close();
		if(!found)
			{
			LOG.info("ImageInfo not found for "+imageTitle+" "+url);
			}
		}
	
	/**
	 * processArticle
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private void processArticle(String entryName) throws IOException,XMLStreamException
		{
		if(entryName.startsWith("File:") ||
		  entryName.startsWith("Image:"))
			{
			processImage(entryName, entryName);
			return;
			}
		final int cllimit=500;
		final QName att_imcontinue=new QName("imcontinue");
		
		final QName att_title=new QName("title");
		
		String imcontinue=null;
		
		
		while(true)
			{
			Set<String> images= new HashSet<String>();
			String url=	this.base_api+"?action=query" +
					"&prop=images" +
					"&format=xml" +
					(imcontinue!=null?"&imcontinue="+escape(imcontinue):"")+
					"&titles="+escape(entryName)+
					"&imlimit="+cllimit
					;
			imcontinue=null;
			
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
					if(name.equals("im") &&
					  (att=e.getAttributeByName(att_title))!=null)
						{
						images.add(att.getValue());
						
						}
					else if(name.equals("images") &&
							(att=e.getAttributeByName(att_imcontinue))!=null)
						{
						imcontinue=att.getValue();
						}
					}
				}
			reader.close();
			
			if(images.isEmpty())
				{
				LOG.info("No images found for "+entryName);
				}
			
			for(String s:images)
				{
				processImage(entryName,s);
				}
			if(imcontinue==null) break;
			}
		}
	
	public static void main(String[] args)
		{
		LOG.setLevel(Level.OFF);
		WPImages app= new WPImages();
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Return images about a given set of articles in wikipedia.");
					System.err.println(Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
					System.err.println(" -html HTML output");
					System.err.println(" -wiki mediawiki gallery");
					System.err.println(" -w <int> image width for html output");
					System.err.println(" -log-level <java.util.logging.Level> default:"+LOG.getLevel());
					System.err.println(" -api <url> default:"+app.base_api);
					System.err.println(" (stdin|articles-names)");
					return;
					}
				else if(args[optind].equals("-log-level"))
					{
					LOG.setLevel(Level.parse(args[++optind]));
					}
				else if(args[optind].equals("-w"))
					{
					app.iiurlwidth= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-html"))
					{
					app.output_type=Mode.xhtml;
					}
				else if(args[optind].equals("-wiki"))
					{
					app.output_type=Mode.wiki;
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
			
			
			
			if(app.output_type==Mode.xhtml)
				{
				System.out.println(
					XHTML.DOCTYPE+	
					"\n<html xmlns=\""+ XHTML.NS+"\"><body><div>");
				}
			else if(app.output_type==Mode.wiki)
				{
				System.out.println("{{Gallery\n" +
					"|title=\n"+
					"|width= "+ app.iiurlwidth +"\n"+
					"|height= "+ app.iiurlwidth +"\n"+
					"|lines=\n"
				);

				}
			if(optind==args.length)
                {
                String line;
                LOG.info("read from stdin");
                java.io.BufferedReader r= new BufferedReader(new InputStreamReader(System.in));
                while(( line=r.readLine())!=null)
                	{
                	app.processArticle(line);
                	}
                r.close();
                }
            else
                {
                while(optind< args.length)
                        {
                        String fname=args[optind++];
                        LOG.info("opening "+fname);
                      	app.processArticle(fname);
                        }
                }
			if(app.output_type==Mode.xhtml)
				{
				System.out.println("</div></body></html>");
				}
			else  if(app.output_type==Mode.wiki)
				{
				System.out.println("}}");
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		
		}
	}
