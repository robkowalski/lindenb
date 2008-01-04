package org.lindenb.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.lindenb.util.Pair;



/**
 * CGI parses the good old Common Gateway Interface
 * @author pierre
 *
 */
public class CGI
	{
	/** Interface for a Parameter */
	static public interface Param
		{
		public String getKey();
		public boolean isFile();
		}
		
	static public class Parameter extends Pair<String,String>
		implements Param
		{
		public Parameter(String key,String value)
			{
			super(key,value);
			}
		@Override
		public String getKey() {
			return first();
			}
		public String getValue() { return second();}
		@Override
		public boolean isFile() {
			return false;
			}
		}
	
	//private File directory;
	private Vector<Param> parameters=new Vector<Param>();
	private int contentMaxLength=Integer.MAX_VALUE;
	
	public CGI()
		{
		//this.directory= null;
		}
	
	
	
	
	
	public void setContentMaxLength(int contentMaxLength) {
		this.contentMaxLength = contentMaxLength;
		}
	
	public int getContentMaxLength() {
		return contentMaxLength;
		}
	
	public String getenv(String key)
		{
		return System.getenv(key);
		}
	
	public Set<String> getParameterNames()
		{
		TreeSet<String> keys= new TreeSet<String>();
		for(Param p:this.parameters)
			{
			keys.add(p.getKey());
			}
		return keys;
		}
	
	

	public Param[] getParameters(String key)
		{
		Vector<Param> params= new Vector<Param>();
		for(Param p:this.parameters)
			{
			if(p.getKey().equals(key)) params.add(p);
			}
		return params.toArray(new Param[params.size()]);
		}
	
	public Param getParameter(String key)
		{
		for(Param p:this.parameters)
			{
			if(p.getKey().equals(key))return p;
			}
		return null;
		}
	
	
	
	public boolean hasParameter(String key)
		{
		return getParameter(key)!=null;
		}
	
	
	
	public void parse() throws IOException
		{
		String requestMethod=null;
	
	    if((requestMethod=getenv("REQUEST_METHOD"))==null) throw new IOException("Cannot find REQUEST_METHOD");
	    if(requestMethod.equals("POST"))
	    	{
	    	parsePOST();
	    	}
	    else if(requestMethod.equals("GET"))
	    	{
	    	parseGET();
	    	}
	    else
		    {
		    throw new IOException("Unknown REQUEST_METHOD \""+requestMethod+"\"");
			}
		}
	
	
	
	private void parseGET()  throws IOException
		{
		String queryString = getenv("QUERY_STRING");
		if(queryString==null) throw new IOException("Cannot find QUERY_STRING");
		if(queryString.length()>getContentMaxLength()) throw new IOException("Content too large");
		parse(new StringReader(queryString),queryString.length());
		}
	
	private  boolean isMultipart()
		{
		String contentType= getenv("CONTENT_TYPE");
		if(contentType==null) return false;
		return contentType.indexOf("multipart/form-data")!=-1;
		}
	
	private void parsePOST()  throws IOException
		{
		String contentLengthStr= getenv("CONTENT_LENGTH");
		if(contentLengthStr==null) throw new IOException("CONTENT_LENGTH missing");
		int contentLength=0;
		try {
			contentLength= Integer.parseInt(contentLengthStr);
		} catch (Exception e) {
			throw new IOException("Bad content Length "+contentLength);
			}
		if(contentLength>getContentMaxLength()) throw new IOException("Content too large");
	 	
		if(!isMultipart())
			{
			parse(new BufferedReader(
					new InputStreamReader(System.in)),
					contentLength);
			}
		else
			{
			throw new IOException("Cannot parse multipart actions");
			}
		}
	
	

	
	private void parse(Reader in,int maxCharRead) throws IOException
		{
		int c;
		StringBuilder key=new StringBuilder();
		StringBuilder value=null;
		int count=0;
		while((c=in.read())!=-1 && count < maxCharRead)
			{
			++count;
			if(c=='+') c=' ';
			if(c=='&')
				{
				if(key.length()!=0)
					{
					addParameter(key,value);
					}	
				key=new StringBuilder();
				value=null;
				}
			else if(c=='=' && key.length()>0 && value==null)
				{
				value = new StringBuilder();
				}
			else
				{
				if(c=='%' && count+2<=maxCharRead)
					{
					int c2= in.read();
					if(c2==-1) throw new IOException("Bad Input");
					int c3= in.read();
					if(c3==-1) throw new IOException("Bad Input");	
					c=x2c(c2,c3);
					count+=2;
					}
				if(value!=null)
					{
					value.append((char)c);
					}
				else
					{
					key.append((char)c);
					}
				}
			}
		if(key.length()!=0)
			{
			addParameter(key,value);
			}
		}
	
	private static int x2c(int c1,int c2) throws IOException
		{
		try {
			return Integer.parseInt(String.valueOf((char)c1)+(char)c2, 16);
			}
		catch (NumberFormatException e)
			{
			throw new IOException(e);
			}
		}
	
	private void addParameter(CharSequence key,CharSequence value)
		{
		if(key==null) return;
		this.parameters.addElement(new Parameter(key.toString(),value==null?"":value.toString()));
		}
	
	
	
	
	
}
