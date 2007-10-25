/**
 * 
 */
package org.lindenb.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.GregorianCalendar;

import org.apache.tools.ant.BuildException;


/**
 * @author pierre
 * &lt;taskdef name="mytask" classname="org.lindenb.ant.CompileInfoTask"/&gt;
 */
public class CompileInfoTask extends  org.apache.tools.ant.Task
{
private String pakage="";
private File dir=null;

public CompileInfoTask()
	{
	}


public File getDir()
	{
	return dir;
	}

public void setDir(File dir)
	{
	this.dir=dir;
	}

public String getPackage() {
	return pakage;
	}

public void setPackage(String pakageName) {
	this.pakage = pakageName;
	}

private static String LPAD(int v)
	{
	return v<10?"0"+v:String.valueOf(v);
	}

@Override
public void execute() throws BuildException {
	try {
		String path[]=getPackage().split("[\\.\\/]");
		File currentDir= getDir();
		if(currentDir!=null )
			{
			if(!currentDir.exists()) throw new BuildException("Directory "+ currentDir+ " does not exist.");
			if(!currentDir.isDirectory()) throw new BuildException("Not A Directory "+ currentDir+ ".");
			}
		else
			{
			currentDir= new File(System.getProperty("user.dir"));
			}
		
		for(String sub:path)
			{
			File subDir= new File(currentDir,sub);
			if(!subDir.exists())
				{
				System.out.println("Creating "+subDir);
				if(!subDir.mkdir())
					{
					throw new BuildException("Cannot create subdirectory "+ subDir+".");
					}
				}
			currentDir=subDir;
			}
		
		GregorianCalendar now= new GregorianCalendar();
		
		now= new GregorianCalendar();
		
		File file= new File(currentDir,"Compilation.java");
		PrintWriter out= new PrintWriter(new FileWriter(file));
		if(getPackage().length()>0)
			{
			out.println(
				"package "+ getPackage().replaceAll("[/]+", ".")+";\n")
				;
			}
		out.println(
			"//this file was GENERATED using "+this.getClass()+" DO NOT EDIT !\n"+
			"import java.util.GregorianCalendar;\n"+
			"/**\n"+
			" *  @author Pierre Lindenbaum\n"+
			" */\n"+
			"public class Compilation\n"+
			" {\n"+
			" private Compilation() {}\n"+
			" public static String getPath() { return \""+ System.getProperty("user.dir")+"\";}\n"+
			" public static String getUser() { return \""+ System.getProperty("user.name")+"\";}\n"+
			" public static GregorianCalendar getCalendar() { return new GregorianCalendar("+
					now.get(GregorianCalendar.YEAR)+","+
					now.get(GregorianCalendar.MONTH)+","+
					now.get(GregorianCalendar.DAY_OF_MONTH)+","+
					now.get(GregorianCalendar.HOUR_OF_DAY)+","+
					now.get(GregorianCalendar.MINUTE)+","+
					now.get(GregorianCalendar.SECOND)+")"+
					";}\n"+
			" public static String getDate() {return \""+
					now.get(GregorianCalendar.YEAR)+"-"+
					LPAD( 1+now.get(GregorianCalendar.MONTH))+"-"+
					LPAD( now.get(GregorianCalendar.DAY_OF_MONTH))+" at "+
					LPAD( now.get(GregorianCalendar.HOUR_OF_DAY))+":"+
					LPAD( now.get(GregorianCalendar.MINUTE))+":"+
					LPAD( now.get(GregorianCalendar.SECOND))+
					"\"; }\n"+
			" public static String getLabel() { return \"Compiled by \"+getUser()+\" on \"+getDate()+\" in \"+getPath();}\n"+ 
			" public static void main(String args[]) { System.out.println(getLabel());}\n"+
			" }\n"
			);
		out.flush();
		out.close();
		System.out.println(getTaskName()+" Wrote "+file+".");
		}
	catch (Exception e)
		{
		throw new BuildException(e);
		}
	}
}
