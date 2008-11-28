/**
 * 
 */
package org.lindenb.tinytools;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.lindenb.util.Compilation;

/**
 * @author lindenb
 * simple and stupied utilities finding a class in a set of jar
 */
public class FindJar
	{
	private FindJar() {}
	
	private static void search(File file,String clazz)
		{
		if(file==null || clazz==null)
			{
			return;
			}
		else if(file.isDirectory())
			{
			File subfiles[]=file.listFiles();
			if(subfiles==null) return;
			for(File sub:subfiles)
				{
				search(sub,clazz);
				}
			}
		else if(file.isFile())
			{
			String name=file.getName().toLowerCase();
			if(name.endsWith(".war") || name.endsWith(".jar") || name.endsWith(".ear"))
				{
				try {
					JarFile jf= new JarFile(file);
					Enumeration<JarEntry> e= jf.entries();
					while(e.hasMoreElements())
						{
						JarEntry entry=e.nextElement();
						if(entry.getName().toLowerCase().contains(clazz))
							{
							System.out.println(file+"\t"+entry.getName());
							}
						}
					jf.close();
					} 
				catch (Exception e) {
					//
					}
				}
			else if((name.endsWith(".java") || name.endsWith(".class")) && name.contains(clazz))
				{
				System.out.println(file);
				}
			}
		
		}
	
	
	public static void main(String[] args) {
	try {
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println(Compilation.getLabel());
				System.err.println("Finds a class in a jar file");
				System.err.println("-h this screen");
				System.err.println("(path0 path1 path2.... pathN) class...");
				return;
				}
			 else if (args[optind].equals("--"))
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
	    Set<File> path= new HashSet<File>();
	    if(optind==args.length)
	    	{
	    	System.err.println("Illegal number of arguments");
	    	return;
	    	}
	    for(int i=optind;i+1<args.length;++i)
	    	{
	    	path.add(new File(args[i]));
	    	}
	    if(path.isEmpty())
	    	{
	    	path.add(new File(System.getProperty("user.dir")));
	    	}
	    String clazz= args[args.length-1].toLowerCase();
	    for(File dir: path)
	    	{
	    	search(dir,clazz);
	    	}
		} 
	catch (Exception e) {
		e.printStackTrace();
	}
}

}
