package org.lindenb.io;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Static class used to record the preferred folder when using
 * a JFileChooser
 * @author lindenb
 *
 */
public class PreferredDirectory
{

/**
 * call getPrefferedDirectory(key) using the Class caller in the StackTrace as a key
 * @return the preferred directory 
 */
public static File getPreferredDirectory()
	{	
	try {
		throw new Exception();
		}
	catch (Exception e)
		{
		StackTraceElement trace[]=e.getStackTrace();
		return getPreferredDirectory(trace[1].getClassName());
		}
	//return getPrefferedDirectory("default");
	}

/**
 * return the directory associated to a given key. 
 * @param key the key to use. may be null.
 * @return the preferred directory 
 */
public static File getPreferredDirectory(String key)
	{
	Preferences prefs = Preferences.userNodeForPackage(PreferredDirectory.class);
	String s=prefs.get("preferredDirectory:"+(key==null?"default":key),null);
	File choice=null;
	if(s!=null)
		{
		
		try {
			choice= new File(s);
			if(!choice.exists())
				{
				choice=null;
				}
			else if(choice.isFile())
				{
				choice=choice.getParentFile();
				}
			}
		catch (Throwable e)
			{
			choice=null;
			}
		}
	
	if(s==null || choice==null)
		{
		choice= new File("user.home","");
		}


	return choice;
	}

/**
 * set the directory associated to a given key using the Class caller in the StackTrace as a key
 * @param key the key to use. may be null.
 *
 */
public static  void setPreferredDirectory(File file)
{	
try {
	throw new Exception();
	}
catch (Exception e)
	{
	StackTraceElement trace[]=e.getStackTrace();
	setPreferredDirectory(trace[1].getClassName(),file);
	}
 //return getPrefferedDirectory("default");
 }


/**
 * set the directory associated to a given key. 
 * @param key the key to use. may be null.
 * @param the file. i ffile is a reguler file; getParentFile will be invoked
 */
public static void setPreferredDirectory(String key,File file)
	{
	if(file==null || !file.exists()) return ;
	try {
		Preferences prefs = Preferences.userNodeForPackage(PreferredDirectory.class);
		if(file.isFile()) file= file.getParentFile();
		if(file==null) return;
		prefs.put("preferredDirectory:"+(key==null?"default":key),file.toString());
		prefs.flush();
		}
	catch (Exception e)
		{
		//nothing
		}
	}

}
