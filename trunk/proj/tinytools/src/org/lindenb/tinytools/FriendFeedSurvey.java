package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lindenb.util.Compilation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A tool to trach the new Member in a FF room (no feeds at this time)
 * @author pierre
 *
 */
public class FriendFeedSurvey
	{
	private static final String DEFAULT_ROOM_NAME="the-life-scientists";
	private static Logger log=Logger.getLogger(FriendFeedSurvey.class.getName());
	private FriendFeedSurvey()
		{
		
		}
	
	private void scan(String room,File fileIn) throws IOException,SAXException
		{
		DocumentBuilder builder = null;
		try
			{
			DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
			f.setNamespaceAware(true);
			f.setCoalescing(true);
			f.setExpandEntityReferences(true);
			f.setValidating(false);
			f.setIgnoringElementContentWhitespace(false);
			builder=f.newDocumentBuilder();
			}
		catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
			}
		Set<String> oldSet =new TreeSet<String>();
		Set<String> newSet =new TreeSet<String>();

		if(fileIn.exists())
			{
			BufferedReader r= new BufferedReader(new FileReader(fileIn));
			String line;
			while((line=r.readLine())!=null)
				{
				line=line.trim();
				if(line.startsWith("#") || line.length()==0) continue;
				oldSet.add(line);
				}
			r.close();
			}
		int start=0;
		final int profilesPerPage=60;//<80 I know
		boolean dirty=true;
		while(dirty)
			{
			dirty=false;
			String url="http://friendfeed.com/rooms/"+
						URLEncoder.encode(room, "UTF-8")+
						"/members?start="+start;
			log.info(url);
			Document doc =builder.parse(url);
			NodeList divs=doc.getElementsByTagName("a");
			for(int i=0;i< divs.getLength();++i)
				{
				Element e= Element.class.cast(divs.item(i));
				Attr att= e.getAttributeNode("class");
				if(att==null || !"l_person".equals(att.getValue())) continue;
				att= e.getAttributeNode("href");
				if(att==null) continue;
				String id= att.getValue();
				if(id.startsWith("/")) id=id.substring(1);
				
				
				if(newSet.add(id))
					{
					if(!oldSet.contains(id))
						{
						log.info("Found member: "+id);
						}
					dirty=true;
					}
				}
			start+=profilesPerPage;
			}
		
		
		newSet.removeAll(oldSet);
		if(newSet.isEmpty())
			{
			System.out.println("#Nothing new.");
			return;
			}
		
		log.info("Saving to "+fileIn);
		PrintWriter out= new PrintWriter(new FileWriter(fileIn,true));//append
		out.println("#"+new Date());
		for(String s:newSet)
			{
			System.out.println(s);
			out.println(s);
			}
		out.flush();
		out.close();
		}
	

	public static void main(String[] args)
		{
		try
			{
			File fileIn= new File(System.getProperty("user.home", "."),".friendfeed-survey.txt");
			String room=DEFAULT_ROOM_NAME;
			FriendFeedSurvey scanner= new FriendFeedSurvey();
			int optind=0;
			
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("A tool to trach the new Member in a FF room (no feeds at this time)");
					System.err.println(" -r <room-id> default:"+ room);
					System.err.println(" -f <save-file> default:"+ fileIn);
					}
				else if(args[optind].equals("-r"))
					{
					room = args[++optind].trim();
					}
				else if(args[optind].equals("-f"))
					{
					fileIn = new File(args[++optind].trim());
					}
				else if(args[optind].equals("-h"))
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
			if(optind!=args.length)
				{
				System.err.println("illegal number of arguments");
				return;
				}
			if(room.length()==0)
				{
				System.err.println("room is empty");
				return;
				}
			scanner.scan(room,fileIn);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
}
