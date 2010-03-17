package org.lindenb.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.lindenb.io.IOUtils;

public class ResourceUtils {
protected ResourceUtils() {}

public static URL getResource(Class<?> clazz,String name) throws IOException
	{
	if(clazz==null) throw new NullPointerException("class is null");
	if(name==null) throw new NullPointerException("name is null");
	URL url= clazz.getResource(name);
	if(url==null) throw new IOException(clazz.toString()+" cannot getResource("+name+")");
	return url;
	}


public static InputStream getResourceAsStream(Class<?> clazz,String name) throws IOException
	{
	if(clazz==null) throw new NullPointerException("class is null");
	if(name==null) throw new NullPointerException("name is null");
	InputStream in= clazz.getResourceAsStream(name);
	if(in==null) throw new IOException(clazz.toString()+" cannot getResourceAsStream("+name+")");
	return in;
	}

public static String getContent(Class<?> clazz,String name) throws IOException
	{
	return IOUtils.getReaderContent(openReader(clazz, name));
	}

public static String getContent(Class<?> clazz,String name,String defaultContent)
	{
	try {
		return getContent(clazz, name);
		}
	catch (IOException e)
		{
		return defaultContent;
		}
	}

public static BufferedReader openReader(Class<?> clazz,String name) throws IOException
	{
	return new  BufferedReader(new InputStreamReader(getResourceAsStream(clazz, name)));
	}

public static Icon getIcon(Class<?> clazz,String name) throws IOException
	{
	return new ImageIcon(getResource(clazz, name));
	}

public static Icon getIcon(Class<?> clazz,String name,Icon defaultIcon)
	{
	try {
		return getIcon(clazz, name);
		}
	catch (IOException e)
		{
		return defaultIcon;
		}
	}
}
