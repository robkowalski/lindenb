package org.lindenb.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class IOUtils
	{
	public static String getFileContent(File file) throws IOException
		{
		StringWriter w= new StringWriter();
		FileReader r=new FileReader(file);
		copyTo(r,w);
		r.close();
		return w.toString();
		}
	
	public static String getURLContent(URL url) throws IOException
		{
		StringWriter w= new StringWriter();
		InputStreamReader r=new InputStreamReader(url.openStream());
		copyTo(r,w);
		r.close();
		return w.toString();
		}
	
	
	public static void copyTo(InputStream in, OutputStream out) throws IOException
		{
		byte buffer[]=new byte[2048];
		int n=0;
		while((n=in.read(buffer))!=-1)
			{
			out.write(buffer, 0, n);
			}
		out.flush();
		}
	
	public static void copyTo(Reader in, Writer out) throws IOException
		{
		char buffer[]=new char[2048];
		int n=0;
		while((n=in.read(buffer))!=-1)
			{
			out.write(buffer, 0, n);
			}
		out.flush();
		}
	
	
	public static BufferedReader openReader(String uri) throws IOException
		{
		if(	uri.startsWith("http://") ||
				uri.startsWith("https://") ||
				uri.startsWith("file://") ||
				uri.startsWith("ftp://")
	    		)
	    		{
	    		URL url= new URL(uri);
	    		if(uri.endsWith(".gz"))
	        		{
	        		return new BufferedReader(new InputStreamReader(new GZIPInputStream(url.openStream())));
	        		}
	        	else
	        		{
	        		return new BufferedReader(new InputStreamReader(url.openStream()));
	        		}
	    		}
	    	else
	        	{
	        	return openFile( new File(uri));
	        	}
		}
	
	/** open a file and return a BufferedReader, gunzip the file if it ends with *.gz*/
	public static BufferedReader openFile(File file) throws IOException
		{
		if(	file.getName().endsWith(".gz"))
			{
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			}
		return new BufferedReader(new FileReader(file));
		}

	
}
