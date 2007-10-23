package org.lindenb.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class IOUtils
	{
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
	        	File fin= new File(uri);
	        	if(fin.getName().endsWith(".gz"))
	        		{
	        		return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fin))));
	        		}
	        	else
	        		{
	        		return new BufferedReader(new FileReader(fin));
	        		}
	        	}
		}

}
