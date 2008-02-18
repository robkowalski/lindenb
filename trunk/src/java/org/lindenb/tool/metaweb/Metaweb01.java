/** a test to display an biography of famous scientists using metaweb / www.freebase.com 
 * Author: Pierre Lindenbaum
 * plindenbaum@yahoo.fr
 * http://plindenbaum.blogspot.com
 * 
 * */
package org.lindenb.tool.metaweb;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.lindenb.json.JSONBuilder;
import org.lindenb.json.JSONItem;
import org.lindenb.json.JSONObject;
import org.lindenb.lang.ResourceUtils;
import org.lindenb.sw.vocabulary.KML;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.util.TimeUtils;
import org.lindenb.xml.XMLUtilities;




/** abstract class for Date YYYY-MM-DD */
abstract class Date
	{
	/** day of month */
	private Short dayOfMonth=null;
	/** month 1-12 */
	private Short month=null;
	/** year */
	private int year;
	
	/** constructor with current date */
	protected Date()
		{
		GregorianCalendar cal= new GregorianCalendar();
		this.year=cal.get(GregorianCalendar.YEAR);
		this.month=(short)(cal.get(GregorianCalendar.MONTH)+1);
		this.dayOfMonth=new Short((short)cal.get(GregorianCalendar.DAY_OF_MONTH));
		}
	
	/** constructor from string YYYY-MM-DD */
	protected Date(String s)
		{
		boolean neg=false;
		if(s.startsWith("-"))
			{
			s=s.substring(1);
			neg=true;
			}
		
		String toks[]=s.split("[\\-]");
		this.year= Integer.parseInt(toks[0])*(neg?-1:1);
		if(toks.length>1)
			{
			this.month=Short.parseShort(toks[1]);
			if(toks.length>2)
				{
				this.dayOfMonth=Short.parseShort(toks[2]);
				}
			}
		}


	/** @returns year */
	public int getYear() {
		return year;
	}
	/** @returns month (1-12) or null */
	public Short getMonth() {
		return month;
	}
	/** @returns day of month (1-31) or null */
	public Short getDayOfMonth() {
		return dayOfMonth;
	}
	
	
	/** base method to compare dates, used by derived classes */
	protected int compareDate(Date o,int side)
		{
		int i= getYear()-o.getYear();
		if(i!=0) return i;
		
		if(getMonth()==null && o.getMonth()==null)
			{
			return 0;
			}
		else if(getMonth()!=null && o.getMonth()==null)
			{
			return side;
			}
		else if(getMonth()==null && o.getMonth()!=null)
			{
			return -side;
			}
		i= getMonth()-o.getMonth();
		if(i!=0) return i;
		
		
		if(getDayOfMonth()==null && o.getDayOfMonth()==null)
			{
			return 0;
			}
		else if(getDayOfMonth()!=null && o.getDayOfMonth()==null)
			{
			return side;
			}
		else if(getDayOfMonth()==null && o.getDayOfMonth()!=null)
			{
			return -side;
			}
		return getDayOfMonth()-o.getDayOfMonth();
		}
	
		
	public String toISO()
		{
		StringBuilder b= new StringBuilder();
		b.append(getYear());
		if(getMonth()!=null)
			{
			b.append("-"+(getMonth()<10?"0":"")+getMonth());
			if(getDayOfMonth()!=null)
				{
				b.append("-"+(getDayOfMonth()<10?"0":"")+getDayOfMonth());
				}
			}
		return b.toString();
		}	
		
	@Override
	/** nice display of this date */
	public String toString()
		{
		StringBuilder b= new StringBuilder();
		if(getMonth()!=null)
			{
			switch(getMonth())
				{
				case 1: b.append("Jan"); break;
				case 2: b.append("Feb"); break;
				case 3: b.append("Mar"); break;
				case 4: b.append("Apr"); break;
				case 5: b.append("May"); break;
				case 6: b.append("Jun"); break;
				case 7: b.append("Jul"); break;
				case 8: b.append("Aug"); break;
				case 9: b.append("Sep"); break;
				case 10: b.append("Oct"); break;
				case 11: b.append("Nov"); break;
				case 12: b.append("Dec"); break;
				default: b.append("???"); break;
				}
			b.append(".");
			if(getDayOfMonth()!=null)
				{
				b.append(" ");
				b.append(getDayOfMonth()<10?"0":"").append(String.valueOf(getDayOfMonth()));
				}
			b.append(", ");
			}
		b.append(String.valueOf(getYear()));
		return b.toString();
		}
	}

/**
 * Start Date
 * @author pierre
 *
 */
class StartDate extends Date
implements Comparable<StartDate>
	{
	public StartDate(String s) {
		super(s);
		}
	@Override
	public int compareTo(StartDate o)
		{
		return compareDate(o, 1);
		}
	
	/** convert this date to the number of days since year 0
	 *  using the first day of the year/month */
	public double dayValue()
		{
		double v= getYear()*365.25;
		if(getMonth()!=null)
			{
			v+= (365.25/12.0)*getMonth();
			if(getDayOfMonth()!=null)
				{
				v+=getDayOfMonth();
				}
			}
		return v;
		}
	}

/**
 * End Date
 * @author pierre
 *
 */
class EndDate extends Date
implements Comparable<EndDate>
	{
	public EndDate()
		{
		super();
		}
	
	public EndDate(String s) {
		super(s);
		}
	
	@Override
	public int compareTo(EndDate o)
		{
		return compareDate(o, -1);
		}
	
	/** convert this date to the number of days since year 0 
	 * using the last day of the year/month
	 */
	public double dayValue()
		{
		double v= 0;
		if(getMonth()!=null)
			{
			if(getDayOfMonth()!=null)
				{
				v+=(1+getDayOfMonth());
				v+=getMonth()*(365.25/12.0);
				}
			else
				{
				v+=(getMonth()+1)*(365.25/12.0);
				}
			v+=getYear()*365.25;
			}
		else
			{
			v+=(1+getYear())*365.25;
			}
		return v;
		}
	
	}

/** Metaweb01 */
public class Metaweb01 {
    static private final String COOKIE="metaweb-user";
    static private final String BASE_URL="http://www.freebase.com";
    static private final String MQLREADURL=BASE_URL+"/api/service/mqlread";
	/** metaweb-user cookie */
    private String metawebCookie=null;
    /** google anaytics */
    private String urchinID =null;
	/** where we save our data */
	private File tmpFolder=null;
	/** base URL */
	private String baseURL=null;
	/** default icon size */
	public static final int DEFAULT_ICON_SIZE=64;
	/** icon size */
	private int iconSize=DEFAULT_ICON_SIZE;
	/** the smallest birth date we found */
	private StartDate minDate=null;
	/** the biggest death date we found */
	private EndDate maxDate=null;
	/** all the person we found */
	private Vector<Person> persons= new Vector<Person>();
	
	/** for debugging: a simple class which echos the bytes read by the inputstream */
    private  class EchoReader extends InputStream
    	{
    	private InputStream in;
    	public EchoReader(InputStream in)
    		{
			this.in=in;
    		}
    	@Override
    	public int read() throws IOException
    		{
    		byte array[]=new byte[1];
    		return read(array,0,1);
    		}
    	@Override
    	public int read(byte[] b, int off, int len) throws IOException {
    		int n= in.read(b, off, len);
    		if(n!=-1 && isDebugging())
    			{
    			System.err.write(b,off, n);
    			}
    		return n;
    		}
    	@Override
    	public void close() throws IOException {
    		in.close();
    		}
    	}
   
    
    
    /**
     * Metaweb01
     */
    private Metaweb01()
    	{
    	
    	}
    
    private boolean isDebugging()
    	{
    	return true;
    	}
    
    
    private int getIconSize() {
    	return iconSize;
    	}
    
    /** takes as input a freebase image id and save it into this.tmpFolder */
    private File makeIcon(String id)
		{
    	if(id.startsWith("#")) id=id.substring(1);
		File dest= new File(this.tmpFolder,id+".png");
		
		//ignore if exists
		if(dest.exists()) return dest;
		
		try {
			//load image
			BufferedImage src= ImageIO.read(new URL("http://www.freebase.com/api/trans/raw/guid/"+id));
			//create icon
			BufferedImage img=new BufferedImage(this.iconSize,this.iconSize,BufferedImage.TYPE_INT_RGB);
			Graphics2D g= img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getIconSize(), getIconSize());
	
			//center the icon
			if(src.getWidth()< src.getHeight())
				{
				double ratio= src.getWidth()/(double)src.getHeight();//<0
				int len=  (int)(getIconSize()*ratio);
				int x= (getIconSize()-len)/2;
				int y=0;
				g.drawImage(src,x,y,len,getIconSize(),null);
				}
			else
				{
				double ratio= src.getHeight()/(double)src.getWidth();//<0
				int len=  (int)(getIconSize()*ratio);
				int y= (getIconSize()-len)/2;
				int x=0;
				g.drawImage(src,x,y,getIconSize(),len,null);
				}
			
			g.dispose();
			//save the icon
			ImageIO.write(img, "png", dest);
			//return the newly created icon
			return dest;
			} 
		catch (Exception e) {
			System.err.println("Cannot create icon for "+id+" "+e.getMessage());
			return null;
			}
		
		}
    
    /** performs a query over www.rebase.com */
    private JSONItem query(String json) throws IOException
	    {
    	
	    String envelope = "{\"qname1\":{\"query\":" +json+ "}}";
	    
	    String urlStr =MQLREADURL+"?queries="+URLEncoder.encode(envelope, "UTF-8");
	    if(isDebugging())
		{
		System.err.println("Sending:"+envelope);
		//System.err.println("Sending : "+urlStr);
		}
	    // Now place the query in JSON envelope objects, and URL encodethe envelopes
	    URL url= new URL(urlStr);
	    URLConnection con=url.openConnection();
	    //set the cookie using the metaweb-user cookie
	    con.setRequestProperty("Cookie",COOKIE+"="+"\""+getMetawebCookie()+"\"");
	    con.connect();
	    InputStream in=con.getInputStream();
	    //parse JSON
	    JSONItem item=JSONBuilder.newInstance().parse(new EchoReader(in));
	    in.close();
	    //check for error
	    String code=getString(item, "code");
	    if(!"/api/status/ok".equals(code))
	    	{
	    	throw new IOException("Bad code "+item);
	    	}
	    code=getString(item, "qname1.code");
	    if(!"/api/status/ok".equals(code))
	    	{
	    	throw new IOException("Bad code "+item);
	    	}
	    return item;
	    }

    //run the program
    private void run() throws IOException
		{
    	//get all object on freebase having type=/user/lindenb/default_domain/scientist
	    JSONItem item=query("[{\"guid\":null,\"type\":\"/user/lindenb/default_domain/scientist\"}]");
	    if(item==null) return;
	    //get the result
	    JSONItem json1=item.find("qname1.result");
		if(!json1.isArray()) return;
		//loop over all the results
	    for(JSONItem i: json1.asArray())
	    	{
	    	if(!i.isObject()) continue;
	    	handlePerson(i.asObject());
	    	}
	    
	    //sort persons on birth-date/death-date
	    Collections.sort(persons);

	    //get min/max dates
		for(Person o: persons)
			{
			if(this.minDate==null || o.startDate.compareTo(this.minDate)<0)
				{
				this.minDate= o.startDate;
				}
			if(o.endDate!=null && (this.maxDate==null || this.maxDate.compareTo(o.endDate)<0))
				{
				this.maxDate= o.endDate;
				}
			}
		
		Vector<Person> remains=new Vector<Person>(this.persons);
		int nLine=-1;
		while(!remains.isEmpty())
			{
			++nLine;
			
			Person first=remains.firstElement();
			remains.removeElementAt(0);
			first.y=nLine;
			
			while(true)
				{
				Person best=null;
				int bestIndex=-1;
				for(int i=0;i< remains.size();++i)
					{
					Person next=remains.elementAt(i);
					if(next.x1()< first.x2()+5) continue;
					if(best==null ||
					  (next.x1()-first.x2() < best.x1()-first.x2()))
						{
						best=next;
						bestIndex=i;
						}
					
					}
				if(best==null) break;
				first=best;
				first.y=nLine;
				remains.removeElementAt(bestIndex);
				}
			}
		/*
		boolean done=false;
		while(!done)
			{
			done=true;
			for(int i=0;i+1< this.persons.size();++i)
				{
				Person t1=this.persons.elementAt(i);
				for(int j=i+1;j< this.persons.size();++j)
					{
					Person t2=this.persons.elementAt(j);
					
					if(!t1.intersect(t2))
						{
						continue;
						}
					done=false;
					t2.y++;
					}
				}
			}
		*/
		remains=null;
		final int MARGIN=2;
		//final int HEIGHT=getIconSize()+(MARGIN*2);
		//print the result

	    
		//save as KML
		PrintWriter out= new PrintWriter(new FileWriter(new File(this.tmpFolder,"history.kml")));
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<kml xmlns=\""+KML.NS+"\">");
		out.println("<Document>");
		out.println("<name>History of Science</name>");
		
		//define icons
		for(Person o:persons)
			{
			if(o.iconFile==null) continue;
			out.println("<Style id=\""+ o.iconFile.getName()+"\">");
			out.println(" <href>"+this.baseURL+o.iconFile.getName()+"</href>");
			out.println(" <hotSpot x='"+getIconSize()/2 +"' y='"+getIconSize()/2 +"' xunits=\"pixels\" yunits=\"pixels\"/>");
			out.println("</Style>");
			}
		
		
		for(Person o:persons)
			{
			for(int side=0;side<2;++side)
				{
				Date date=(side==0?o.startDate:o.endDate);
				Place place=(side==0?o.startPlace:o.endPlace);
				if(date==null || place==null) continue;
					
				out.println("<Placemark>");
				out.println("<TimeStamp><when>"+date.toISO()+"</when></TimeStamp>");
				out.println("<name>"+(side==0?"Birth of ":"Death of ")+XMLUtilities.escape(o.get("name"))+" at "+ XMLUtilities.escape(o.get(side==0?"place_of_birth":"place_of_death"))+"</name>");
				out.print("<description>");
				out.print(XMLUtilities.escape("<div style='padding: "+MARGIN +"px; background-color:black; color:white;'>"));
				if(o.iconFile!=null)
					{
					out.print(XMLUtilities.escape("<img  align ='left' width='"+getIconSize() +"' height='"+ getIconSize()+"'  src=\'"+ this.baseURL+o.iconFile.getName() +"\'/>"));
					}
				
				out.print((side==0?"Birth of ":"Death of ")+XMLUtilities.escape(o.getHTML()));
				out.print(XMLUtilities.escape("</div>"));
				out.println("</description>");
				if(o.iconFile!=null)
					{
					out.println("<styleUrl>"+this.baseURL+o.iconFile.getName()+"</styleUrl>");
					}
				out.print("<Point><coordinates>");
				out.print(place.longitude+","+place.latitude);
				out.println("</coordinates></Point>");
				out.println("</Placemark>");
					
				}
			}
		out.println("</Document>");
		out.println("</kml>");
		out.flush();
		out.close();
		
		//save as XUL
		out= new PrintWriter(new FileWriter(new File(this.tmpFolder,"person.js")));
		out.println("var persons=[");
		for(int i=0;i< persons.size();++i)
			{
			if(i>0) out.println(",");
			Person p= persons.elementAt(i);
			out.println("{");
			out.println("name:"+quote(p.getName())+",");
			out.println("guid:"+quote(p.getID())+",");
			out.println("gender:"+quote(p.get("gender"))+",");
			out.println("y:0,");
			out.println("node:null,");
			out.println("selected:true,");
			out.println("nationality:"+quote(p.getArray("nationality"))+",");
			out.println("shortBio:"+quote(p.get("shortBio"))+",");
			out.println("profession:"+quote(p.getArray("profession"))+",");
			out.print("birthDate:");
			if(p.startDate==null)
				{
				out.println("null,");
				}
			else
				{
				out.print("new StartDate("+
					p.startDate.getYear()+","+
					(p.startDate.getMonth()==null?"null,null":
					p.startDate.getMonth()+","+p.startDate.getDayOfMonth())+
					"),");
				}
			out.print("birthPlace:"+quote(p.get("place_of_birth"))+",");
			
			out.print("deathDate:");
			if(p.endDate==null)
				{
				out.println("null,");
				}
			else
				{
				out.print("new EndDate("+
					p.endDate.getYear()+","+
					(p.endDate.getMonth()==null?"null,null":
					p.endDate.getMonth()+","+p.endDate.getDayOfMonth())+
					"),");
				}
			out.print("deathPlace:"+quote(p.get("place_of_death"))+",");
			
			out.print("knownFor:"+quote(p.getArray("knownFor"))+",");
			out.print("img:"+quote(p.iconFile==null?null:p.iconFile.getName()));
			out.println("}");
			}
		out.println("];");
		out.flush();
		out.close();
		
		String xul=ResourceUtils.getContent(getClass(), "history.xul");
		out= new PrintWriter(new FileWriter(new File(this.tmpFolder,"history.xul")));
		xul=xul.replaceAll("__ABOUT__", "Updated: "+TimeUtils.toYYYYMMDD()).
				replaceAll(Pattern.quote("<!-- __URCHIN__ -->"),
				(this.urchinID==null?"":
				"<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\">"+
				"</script>"+
				"<script type=\"text/javascript\">"+
				"_uacct = \""+urchinID+"\";"+
				"urchinTracker();"+
				"</script>")).replace("__KML__",
				"http://maps.google.com/maps?f=q&amp;hl=en&amp;geocode=&amp;q="+
				XMLUtilities.escape(URLEncoder.encode(this.baseURL+"history.kml","UTF-8"))+
				"&amp;ie=UTF8&amp;ll=53.956086,-13.007812&amp;spn=99.233765,284.0625&amp;t=h&amp;z=2"
				);
		out.print(xul);
		out.flush();
		out.close();
		
		out= new PrintWriter(new FileWriter(new File(this.tmpFolder,"history.js")));
		out.print(ResourceUtils.getContent(getClass(), "history.js"));
		out.flush();
		out.close();
		}
    
    private static String quote(String s)
    	{
    	if(s==null) return "null";
    	return "\""+C.escape(s)+"\"";
    	}
    
    private static String quote(String s[])
		{
		if(s==null) return "[]";
		StringBuilder b= new StringBuilder("[");
		for(int i=0;i< s.length;++i)
			{
			if(i>0) b.append(",");
			b.append(quote(s[i]));
			}
		b.append("]");
		return b.toString();
		}
    
    private static class MetawebObject
    	{
    	private HashMap<String, String> properties= new HashMap<String,  String>();
    	private HashMap<String, HashSet<String>> name2values= new HashMap<String,  HashSet<String>>();
    	public MetawebObject()
    		{
    		}
    	String set(String key,String value)
    		{
    		if(value==null)
    			{
    			properties.remove(key);
    			}
    		else
    			{
    			properties.put(key, value);
    			}
    		return value;
    		}
    	String get(String key)
			{
			return this.properties.get(key);
			}
    	
    	String[] getArray(String key)
			{
    		HashSet<String> set=name2values.get(key);
    		if(set==null || set.isEmpty()) return new String[0];
    		String array[]=new String[set.size()];
    		return set.toArray(array);
			}
    	
    	
    	Collection<String> set(String key,Collection<String> values)
			{
			if(values==null || values.isEmpty()) return values;
			HashSet<String> set= new HashSet<String>();
			this.name2values.put(key, set);
			set.addAll(values);
			return values;
			}

    	@Override
    	public String toString() {
    		StringBuilder b= new StringBuilder("{\n");
    		for(String key:properties.keySet())
    			{
    			b.append(key).append(":").append(properties.get(key)).append("\n");
    			}
    		for(String key:name2values.keySet())
				{
    			HashSet<String> set= this.name2values.get(key);
				b.append(key).append(":[");
				boolean found=false;
				for(String s:set)
					{
					if(found) b.append(",");
					found=true;
					b.append(s);
					}
				b.append("]\n");
				}
    		b.append("}");
    		return b.toString();
    		}
    	
    	
    	}
    
    private static class Place
    	{
    	double longitude;
    	double latitude;
    	@Override
    	public String toString() {
    		return "("+longitude+" "+latitude+")";
    		}
    	}
    
    private class Person extends MetawebObject
    implements Comparable<Person>
    	{
    	Place startPlace=null;
    	Place endPlace=null;
    	StartDate startDate=null;
    	EndDate endDate=null;
    	File iconFile=null;
    	int y=0;
    	
    	@Override
    	public int compareTo(Person o)
    		{
    		int i= startDate.compareTo(o.startDate);
    		if(i!=0) return i;
    		return endDate.compareTo(o.endDate);
    		}
    	
    	public double x1()
    		{
    		return convertDate2Pixel(startDate);
    		}
    	
    	public double x2()
    		{
    		return convertDate2Pixel(endDate==null?new EndDate():endDate);
    		}

    	boolean intersect(Person o)
    		{
    		final int margin=5;
    		if(o==this || this.y!=o.y) return false;
    		return !(this.x2()+margin< o.x1() || o.x2()+margin< this.x1());
    		}
    	
    	@Override
    	public String toString() {
    		return super.toString()+" birth "+startDate+"\ndeath "+endDate+"\n"+
    			startPlace+"\n"+endPlace+"\n";
    		}
    	
    	public String getID()
    		{
    		String gui= get("guid");
    		if(gui.startsWith("#")) gui= gui.substring(1);
    		return gui;
    		}
    	
    	public String getName()
    		{
    		return get("name");
    		}
    	
    	public String getHTML()
    		{
    		StringWriter sw=new StringWriter();
    		PrintWriter out= new PrintWriter(sw);
    		String gui= getID();
    		if(gui!=null)
	    		{
	    		out.print("<a href=\"http://www.freebase.com/view/guid/"+gui+"\" title=\"");
	    		out.print(XMLUtilities.escape(getName()));
	    		out.print("\">");
	    		}
    		out.print(XMLUtilities.escape(getName()));
    		if(gui!=null)
    			{
    			out.print("</a>");
    			}
    		out.print(" ( ");
    		if(startDate!=null)
    			{
    			out.print(startDate);
    			String place= get("place_of_birth");
    			if(place!=null)
    				{
    				out.print(" at "+XMLUtilities.escape(place));
    				}
    			}
    		if(endDate!=null)
    			{
    			out.print(", ");
    			out.print(endDate);
    			String place= get("place_of_death");
    			if(place!=null)
    				{
    				out.print(" at "+XMLUtilities.escape(place));
    				}
    			}
    		out.print(")");
    		
    		String s= get("shortBio");
    		if(s!=null) out.print("<cite>"+XMLUtilities.escape(s)+"</cite>. ");
    		HashSet<String> set= super.name2values.get("knownFor");
    		if(set!=null && !set.isEmpty())
    			{
    			out.print(" <b>Known for</b> : ");
    			boolean found=false;
    			for(String i:set)
    				{
    				if(found) out.print(", ");
    				found=true;
    				out.print("&apos;"+XMLUtilities.escape(i)+"&apos;");
    				}
    			}
    		
    		return sw.toString();
    		}
    	
    	
    	
    	}
    
   
    
    
private int getScreenWidthInPixel()
	{
	return 3000;
	}

private double convertDate2Pixel(StartDate d)
	{
	return getScreenWidthInPixel()*((d.dayValue()-minDate.dayValue())/(this.maxDate.dayValue()-this.minDate.dayValue()));
	}
/** metaweb doesn't like escaping simple &apos; juste &quote; */
private String escape(String s)
	{
	StringBuilder buffer=new StringBuilder(s.length());
  
     for(int i=0;i< s.length();++i)
         {
         switch(s.charAt(i))
             {
             case('\"'): buffer.append("\\\"");break;
             //case('\''): buffer.append("\\\'");break;
             case('\n'): buffer.append("\\n");break;
             case('\t'): buffer.append("\\t");break;
             case('\\'): buffer.append("\\\\");break;
             default: buffer.append(s.charAt(i)); break;
             }
         }
     return buffer.toString();
	}

	private double convertDate2Pixel(EndDate d)
		{
		return getScreenWidthInPixel()*((d.dayValue()-minDate.dayValue())/(this.maxDate.dayValue()-this.minDate.dayValue()));
		}
    
    private void handlePerson(JSONObject object) throws IOException
    	{
    	if(!object.containsKey("guid")) return;
    	Person person= new Person();
    	person.set("guid",object.get("guid").asString());

    	/** find types about this person */
    	JSONItem q =query("{\"guid\":\""+person.get("guid")+"\",\"type\":[]}");
    	JSONItem json=q.find("qname1.result.type");
    	for(JSONItem i2: json.asArray())
			{
    		String type=i2.asString();
			//System.out.println("types  = "+type);
			/** find information about this type */
			JSONItem q3 =query("{\"guid\":\""+person.get("guid")+"\",\"*\":null,\"type\":\""+type+"\"}");
			//System.err.println(q3);
			if(type.equals("/user/lindenb/default_domain/scientist"))
				{
				person.set("shortBio",getString(q3,"qname1.result.short_bio"));
				person.set("knownFor",getArray(q3,"qname1.result.known_for"));
				}
			else if(type.equals("/people/deceased_person"))
				{
				String date=person.set("date_of_death",getString(q3,"qname1.result.date_of_death"));
				if(date!=null) person.endDate= new EndDate(date);
				String place=person.set("place_of_death",getString(q3,"qname1.result.place_of_death"));
				person.set("cause_of_death",getArray(q3,"qname1.result.cause_of_death"));
				if(place!=null)
					{
					Place coord=handlePlace(place);
					if(coord!=null) person.endPlace=coord;
					}
				}
			else if(type.equals("/people/person"))
				{
				
				person.set("profession",getArray(q3,"qname1.result.profession"));	
				String date=person.set("date_of_birth", getString(q3,"qname1.result.date_of_birth"));
				if(date!=null) person.startDate= new StartDate(date);
				String place=person.set("place_of_birth", getString(q3,"qname1.result.place_of_birth"));
				person.set("gender",getString(q3,"qname1.result.gender"));
				person.set("nationality",getArray(q3,"qname1.result.nationality"));
				if(place!=null)
					{
					Place coord=handlePlace(place);
					if(coord!=null) person.startPlace=coord;
					}
				}
			else if(type.equals("/common/topic"))
				{
				String name=person.set("name",getString(q3,"qname1.result.name"));
				Collection<String> imgs=person.set("image",getArray(q3,"qname1.result.image"));
				if(imgs!=null)
					{
					for(String img: imgs)
						{
						person.iconFile=handleImage(img,name);
						if(person.iconFile!=null) break;
						}
					}
				}
			else if(type.equals("/user/mikelove/default_domain/influence_node"))
				{
				
				}
			else
				{
				System.err.println("Type not handled "+type);
				}
			}
	if(person.startDate==null) return;
    	persons.addElement(person);

    	//System.out.println(person.toString());
    	//System.out.println("##");
    	}
    
    private Place handlePlace(String place) throws IOException
    	{
    	
    	JSONItem q =query("[{\"name\":\""+escape(place)+"\",\"type\":\"/location/location\",\"geolocation\": {\"*\" : null },\"*\":null}]");
    	JSONItem i=q.find("qname1.result[0].geolocation");
    	if(i==null || !i.isObject())
			{
			return null;
			}
    	JSONObject geoloc= i.asObject();
    	if(!(geoloc.containsKey("longitude") && geoloc.containsKey("latitude")))
			{
			System.err.println("Cannot get lon/lat");
			return null;
			}
    	Place coord= new Place();
    	coord.longitude= geoloc.get("longitude").asDouble();
    	coord.latitude= geoloc.get("latitude").asDouble();
    	System.err.println("ok found "+coord);
    	return coord;
    	}
    
    private File handleImage(String imageName,String person) throws IOException
		{
    	JSONItem q =query("[{\"name\":\""+escape(imageName)+"\",\"type\":\"/common/image\",\"appears_in_topic_gallery\":\""+ escape(person) +"\",\"*\":null}]");
    	JSONItem result= q.find("qname1.result");
    	if(result==null || !result.isArray()) return null;
    	for(JSONItem item: result.asArray())
    		{
    		if(item==null || !item.isObject()) continue;
    		JSONItem id= item.asObject().get("guid");
    		if(id==null) continue;
    		String uid= id.asString();
    		if(uid.startsWith("#")) uid=uid.substring(1);
    		File iconFile=makeIcon(uid);
    		if(iconFile!=null) return iconFile;
    		}
	    return null;
		}
    
    private String getString(JSONItem root,String path)
    	{
    	JSONItem item=root.find(path);
    	if(item==null ||  !item.isString())
    		{
    		System.err.println("Cannot get "+path +" in "+root);
    		return null;
    		}
    	return item.toString();
    	}
    
    private Collection<String> getArray(JSONItem root,String path)
    	{
    	JSONItem item=root.find(path);
    	if(item==null ||  !item.isArray())
    		{
    		System.err.println("Cannot get "+path +" in "+root);
    		return null;
    		}
    	Vector<String> array=new Vector<String>(item.asArray().size());
    	for(int i=0;i<  item.asArray().size();++i)
    		{
    		JSONItem x= item.asArray().elementAt(i);
    		if(x==null || !x.isConstant()) continue;
    		array.addElement(x.asString());
    		}
    	return array;
    	}
    
    private String getMetawebCookie()
    	{
    	return this.metawebCookie;
    	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try {
			Metaweb01 app= new Metaweb01();
			Properties preferences=new Properties();
			File prefFile=new File(System.getProperty("user.home"),".metaweb.xml");
			if(!prefFile.exists())
				{
				System.err.println("Warning: Cannot find "+prefFile+" containing freebase cookie");
				}
			else
				{
				InputStream in= new FileInputStream(prefFile);
				preferences.loadFromXML(in);
				in.close();
				}
			
			
			
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println(" -h this screen");
					System.err.println(" -d <dir> save to directory");
					System.err.println(" -c <string> metaweb cookie");
					System.err.println(" -u <string> base url");
					System.err.println(" -g <string> google analytics id");
					return;
					}
				else if(args[optind].equals("-d"))
					{
					app.tmpFolder= new File(args[++optind]);
					}
				else if(args[optind].equals("-c"))
					{
					app.metawebCookie= args[++optind];
					}
				else if(args[optind].equals("-u"))
					{
					app.baseURL= args[++optind];
					}
				else if(args[optind].equals("-g"))
					{
					app.urchinID= args[++optind];
					}
				else if(args[optind].equals("--"))
					{
					++optind;
					break;
					}
				else if (args[optind].startsWith("-"))
                    {
                    System.err.println("bad argument " + args[optind]);
                    System.exit(-1);
                    } 
				else
					{
					break;
					}
				++optind;
				}
			
			if(optind!=args.length)
				{
				System.err.println("Bad number of arguments.");
				return;
				}
			
			if(app.tmpFolder==null)
				{
				if(preferences.containsKey("tmp"))
					{
					app.tmpFolder= new File(preferences.getProperty("tmp"));
					}
				if(app.tmpFolder==null)
					{
					System.err.println("folder was not specified.");
					return;
					}
				}
			
			if(app.metawebCookie==null)
				{
				if(preferences.containsKey(COOKIE))
					{
					app.metawebCookie=preferences.getProperty(COOKIE);
					}
				if(app.metawebCookie==null)
					{
					System.err.println("Cookie"+COOKIE+" was not specified");
					return;
					}
				}
			
			if(app.baseURL==null)
				{
				if(preferences.containsKey("url"))
					{
					app.baseURL=preferences.getProperty("url");
					}
				if(app.baseURL==null)
					{
					app.baseURL="";
					}
				}
			
			if(app.urchinID==null)
				{
				app.urchinID=preferences.getProperty("urchin");
				}
			
			app.run();
			System.out.println("Done.");
			}
		catch (Exception e) {
			e.printStackTrace();
		}
	
		}

}
