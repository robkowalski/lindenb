package org.lindenb.tool.oneshot;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.util.Base64;


public class Connotea2Delicious
{
private Properties prefs=new Properties();

private static class EchoInput extends InputStream
    {
    private InputStream in;
    EchoInput(InputStream in) { this.in=in;}
    @Override
    public int read() throws IOException {
        int c= in.read();
        if(c==-1) return -1;
        //System.err.write(c);
        return c;
        }
    @Override
    public void close() throws IOException {
        in.close();
        }
    }


private void run() throws IOException,XMLStreamException,InterruptedException
    {
    URL url= new URL("http://www.connotea.org/data/user/"+prefs.getProperty("connotea-user")+"?num="+prefs.getProperty("num", "100"));
    URLConnection con=url.openConnection();
    String encoding =Base64.encode((prefs.getProperty("connotea-user")+":"+prefs.getProperty("connotea-password")).getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);
    con.connect();
    InputStream in=con.getInputStream();
    
    XMLInputFactory factory= XMLInputFactory.newInstance();
    XMLEventReader reader= factory.createXMLEventReader(new EchoInput(in));
    XMLEvent evt;
    HashSet<String> subject= new HashSet<String>();
    String title=null;
    String link=null;
    boolean isPrivate=false;;
    while(!(evt=reader.nextEvent()).isEndDocument())
        {
        if(evt.isStartElement())
            {
            StartElement e= evt.asStartElement();
            String local= e.getName().getLocalPart();
           
            if(local.equals("subject"))
                {
                subject.add(reader.getElementText());
                }
            else if(local.equals("title"))
                {
                title=reader.getElementText();
                }
            else if(local.equals("link"))
                {
                link=reader.getElementText();
                }
            else if(local.equals("private"))
                {
                isPrivate=reader.getElementText().equals("1");
                }
            }
        else if(evt.isEndElement())
            {
            EndElement e= evt.asEndElement();
            String local= e.getName().getLocalPart();
           
            if(local.equals("Post"))
                {
                if(link!=null)
                    {
                    if(title==null) title=link;
                    if(subject.isEmpty()) subject.add("post");
                   
                    StringBuilder sb= new StringBuilder("https://api.del.icio.us/v1/posts/add?");
                    sb.append("url="+URLEncoder.encode(link,"UTF-8"));
                    sb.append("&description="+URLEncoder.encode(title,"UTF-8"));
                    sb.append("&tags=");
                    for(String tag:subject)
                        {
                        sb.append(URLEncoder.encode(tag,"UTF-8")+"+");
                        }
                    sb.append("&replace=yes");
                    sb.append(isPrivate?"&shared=no":"");
                   
                    System.out.print(title+" "+sb);
                   
                    URL url2= new URL(sb.toString());
                    URLConnection con2=url2.openConnection();
                    encoding = Base64.encode((prefs.getProperty("delicious-user")+":"+prefs.getProperty("delicious-password")).getBytes());
                    con2.setRequestProperty("Authorization", "Basic " + encoding);
                    con2.connect();
                    InputStream in2= con2.getInputStream();
                    XMLEventReader reader2= factory.createXMLEventReader(in2);
                    XMLEvent evt2;
                    while(!(evt2=reader2.nextEvent()).isEndDocument())
                    	{
                    	if(!evt2.isStartElement()) continue;
                    	if(!evt2.asStartElement().getName().getLocalPart().equals("result")) continue;
                    	Attribute att=evt2.asStartElement().getAttributeByName(new QName("code"));
                    	if(att==null) continue;
                    	System.out.print("\t"+att.getValue());
                    	break;
                    	}
                    System.out.println();
                    in2.close();
                    Thread.sleep(2000);
                    }
                subject.clear();
                link=null;
                title=null;
                isPrivate=false;
                }
           
            }
        }
   
    in.close();
    System.err.print("Done.");
    }

public static void main(String[] args)
    {
    try
    {
    File prefFile= new File(System.getProperty("user.home"),".connotea.xml");
    if(!prefFile.exists())
    	{
    	System.err.println("Cannot get "+prefFile);
    	return;
    	}
    Connotea2Delicious app= new Connotea2Delicious();
    InputStream in= new FileInputStream(prefFile);
    app.prefs.loadFromXML(in);
    in.close();
    app.run();
    }
catch(Exception err)
    {
    err.printStackTrace();   
    }
}
}
 