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
import java.io.StreamTokenizer;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class IOUtils
	{
	/** @return a representation of a StreamTokenizer  */
	public static String toString(StreamTokenizer st)
	    {
		if(st==null) return "null";
	    switch(st.ttype)
	        {
	        case StreamTokenizer.TT_EOF: return "<EOF>";
	        case StreamTokenizer.TT_EOL: return "<EOL>";
	        case StreamTokenizer.TT_NUMBER: return String.valueOf(st.nval)+"(number)";
	        case StreamTokenizer.TT_WORD: return st.sval+"(word)";
	        default: return  "'"+((char)st.ttype)+"'(char)";
	        }
	    }
	
	public static String getReaderContent(Reader r) throws IOException
		{
		StringWriter w= new StringWriter();
		copyTo(r,w);
		return w.toString();
		}
	
	
	public static String getFileContent(File file) throws IOException
		{
		FileReader r=new FileReader(file);
		String s=getReaderContent(r);
		r.close();
		return s;
		}
	
	public static String getURLContent(URL url) throws IOException
		{
		InputStreamReader r=new InputStreamReader(url.openStream());
		String s=getReaderContent(r);
		r.close();
		return s;
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
