package org.lindenb.tinytools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.lindenb.me.Me;
import org.lindenb.util.Compilation;


/**
 * Java2Dot
 * prints a java hierarchy for javaclasses to the dot format
 * @author lindenb
 *
 */
public class Java2Dot
{
/** unique id generator */
private static int ID_GENERATOR=0;

private static enum Relation
	{
	SUPER,
	IMPLEMENTS,
	DECLARES
	};

/** Wrapper around a java class */
private static class ClassWrapper
	{
	/** unique id */
	int id= (++ID_GENERATOR);
	/** the class observed */
	Class<?> clazz;
	/** did we already processed this class ? */
	private boolean seen=false;
	
	ClassWrapper(Class<?> clazz)
		{
		this.clazz=clazz;
		}
	
	@Override
	public int hashCode() {
		return this.clazz.getName().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj==null || getClass()!=obj.getClass()) return false;
		return ClassWrapper.class.cast(obj).clazz==this.clazz;
		}
	
	void dot(PrintStream out)
		{
		out.print("id"+this.id+"[shape=rectangle,style=filled,");
		if(this.clazz.isInterface())
			{
			out.println("fillcolor=khaki,");
			}
		else
			{
			out.println("fillcolor=gray77,");
			}
		out.print("label=\""+this.clazz.getName()+"\"");
		out.println("]");
		}
	
	@Override
	public String toString() {
		return this.clazz.getName();
		}
	} 

/**
 * Defines a Link between to classes
 * @author lindenb
 *
 */
private static class Link
	{
	private ClassWrapper from;
	private ClassWrapper to;
	private Relation label;
	Link(ClassWrapper from,ClassWrapper to,Relation label)
		{
		this.from=from;
		this.to=to;
		this.label=label;
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj==null || getClass()!=obj.getClass()) return false;
		return 	Link.class.cast(obj).from.equals(this.from) &&
				Link.class.cast(obj).to.equals(this.to)
				;
		}
	
	@Override
	public int hashCode() {
		return from.hashCode()*31+to.hashCode();
		}
	
	@Override
	public String toString() {
		return from.toString() +" -["+label+"]-> " +to.toString();
		}
	
	void dot(PrintStream out)
		{
		out.print("id"+from.id+"->id"+to.id+"[");
		switch(label)
			{
			case IMPLEMENTS: out.print("color=red,fontcolor=red,arrowType=onormal,"); break;
			case DECLARES: out.print("color=green,fontcolor=green,"); break;
			case SUPER:out.print("color=black,fontcolor=black,arrowType=normal,"); break;
			default:System.err.println("???? dot type not handled "+label);break;
			}
		out.print("label=\""+label.name().toLowerCase()+"\"");
		out.println("]");
		}
	
	}

/** all the files */
private ArrayList<File> files=new ArrayList<File>();
/** all the classes that may be observed */
private HashSet<ClassWrapper> classes= new HashSet<ClassWrapper>();
/** all the links between the classes */
private HashSet<Link> links= new HashSet<Link>();
/** ignore pattern */
private ArrayList<Pattern> ignorePattern= new ArrayList<Pattern>();

/** are we using any.any$any classes ? */ 
boolean usingDeclaredClasses=true;
/** are we using interfaces ? */
boolean usingInterfaces=true;
/** are we looking for classes implementing interfaces */
boolean usingClassesImplementingInterfaces=true;


/** empty private cstor */
private Java2Dot()
	{
	
	}

/** add a file in the list of jar files */
private void addFile(File jarFile) throws IOException
	{
	if(!jarFile.exists())
		{
		System.err.println(jarFile.toString()+" doesn't exists");
		return;
		}
	
	if(jarFile.isDirectory())
		{
		for(File fc: jarFile.listFiles(new FileFilter()
			{
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || (f.isFile() && f.getName().endsWith("ar"));
				}
			}))
			{
			this.addFile(fc);
			}
		return;
		}
	
	this.files.add(jarFile);
	}

/** finds a class Wrapper by its name */
private ClassWrapper findByName(String s)
	{
	for(ClassWrapper cw: this.classes)
		{
		if(cw.clazz.getName().equals(s)) return cw;
		}
	try {
		Class<?> c=Class.forName(s);
		ClassWrapper cw= new ClassWrapper(c);
		this.classes.add(cw);
		return cw;
	} catch (Exception e) {
		System.err.println(s+" not found");
		return null;
		}
	
	
	}

/** finds a class Wrapper by its delegated class */
private ClassWrapper findByClass(Class<?> c)
	{
	if(c==null) return null;
	for(ClassWrapper cw: this.classes)
		{
		if(cw.clazz==c) return cw;
		}
	ClassWrapper cw= new ClassWrapper(c);
	this.classes.add(cw);
	return cw;
	}


/** workhorse. recursive call for this class wrapper */
private void run(ClassWrapper cw)
	{
	if(cw==null) return;
	if(cw.seen) return;
	HashSet<ClassWrapper> next= new HashSet<ClassWrapper>();
	//System.err.println("Found "+cw);
	cw.seen=true;
	Class<?> c= cw.clazz;

	Class<?> parent2= c.getSuperclass();
	if(parent2!=null && parent2!=Object.class)
		{
		ClassWrapper cw2= findByClass(parent2);
		if(cw2!=null)
			{
			this.links.add(new Link(cw,cw2,Relation.SUPER));
			next.add(cw2);
			}
		}

	if(usingInterfaces)
		{
		for(Class<?> i:c.getInterfaces())
			{
			ClassWrapper cw2= findByClass(i);
			if(cw2==null) continue;
				
			ClassWrapper cw3= findByClass(parent2);
			if(cw3==null) continue;
			for(Class<?> i2:cw3.clazz.getInterfaces())
				{
				if(i2==i)
					{
					i=null;
					break;
					}
				}
			if(i==null) continue;
			
			Link L=new Link(cw,cw2,
					(c.isInterface()?Relation.SUPER:Relation.IMPLEMENTS));
			this.links.add(L);
			next.add(cw2);
			}
		
		if(usingClassesImplementingInterfaces && c.isInterface())
			{
			for(ClassWrapper cw2:this.classes)
				{
				for(Class<?> i:cw2.clazz.getInterfaces())
					{
					if(i==c)
						{
						Link L=new Link(cw2,cw,Relation.IMPLEMENTS);
						this.links.add(L);
						next.add(cw2);
						}
					}
				}
			}
		
		}
	
	if(usingDeclaredClasses)
		{
		for(Class<?> d:c.getDeclaredClasses())
			{
			ClassWrapper cw2= findByClass(d);
			if(cw2!=null)
				{
				this.links.add(new Link(cw,cw2,Relation.DECLARES));
				next.add(cw2);
				}
			}
		}
	
	for(ClassWrapper child: this.classes)
		{
		Class<?> parent3= child.clazz.getSuperclass();
		if(parent3==null) continue;
		
		if(parent3!=c)
			{
			continue;
			}
		ClassWrapper cw3= findByClass(parent3);
		if(cw3!=null)
			{
			Link L=new Link(child,cw,Relation.SUPER);
			this.links.add(L);
			next.add(child);
			}
		}
	for(ClassWrapper cw2:next)
		{
		run(cw2);
		}
	}

private void run(HashSet<String> setOfClasses) throws IOException
	{
		 ArrayList<URL> urls=new ArrayList<URL>();
		 for(File f:this.files)
		 	{
			urls.add(f.toURI().toURL()); 
		 	}
		    
		 
	    URLClassLoader cl= new URLClassLoader(urls.toArray(new URL[urls.size()]),ClassLoader.getSystemClassLoader());
	    
	    //loop over each file
	    for(File f:this.files)
	    	{
	    	System.err.println(f);
	    	JarFile jf= new JarFile(f);
	    	Enumeration<JarEntry> e=jf.entries();
	    	//loop over each entry of this jar file
	    	while(e.hasMoreElements())
	    		{
	    		JarEntry je=e.nextElement();
	    		if(!je.getName().endsWith(".class")) continue;
	    	
	    		String className=je.getName();
	    		className=className.substring(0,className.length()-6);
	    		className=className.replace('/','.');
	    		int sub= className.indexOf('$');
	    		if(sub!=-1 && usingDeclaredClasses==false) continue;
	    		//ignore anonymous classes
	    		if(sub!=-1 && Character.isDigit(className.charAt(sub+1))) continue;
	    		
	    		for(Pattern pat:this.ignorePattern)
		    		{
		    		if(pat.matcher(className).matches())
		    			{
		    			System.err.println("Ignoring "+className);
		    			jf=null;
		    			break;
		    			}
		    		}
	    		
	    		if(jf==null) continue;
	    		
	    		try
		    		{
		    		Class<?> c=cl.loadClass(className);
		    		//System.err.println(c.getName());
		    		classes.add(new ClassWrapper(c));
		    		}
	    		catch(IllegalAccessError err)
	    			{
	    			System.err.println("#cannot access "+className+" "+err.getMessage());
	    			}
	    		catch(NoClassDefFoundError err)
	    			{
	    			System.err.println("#class not defined "+className+" "+err.getMessage());
	    			}
	    		catch(ClassNotFoundException err)
	    			{
	    			System.err.println("#class not found "+className+" "+err.getMessage());
	    			}
	    		}
	    	}
	    
	    for(String x: setOfClasses)
		    {
		    ClassWrapper cw=findByName(
		    		x
		    	);
		    if(x==null)
		    	{
		    	System.err.println("Cannot find class "+x);
		    	continue;
		    	}
		    run(cw);
		    }
		}

/** echo the result to a graphiz dot file */
private void dot(PrintStream out) throws IOException
	{
	out.println("digraph G{");

	for(ClassWrapper c: this.classes)
		{
		if(!c.seen) continue;
		c.dot(out);
		}
	for(Link L: this.links)
		{
		L.dot(out);
		}
	out.println("}");
	out.flush();
	}

public static void main(String[] args) {
	try {
		Java2Dot app= new Java2Dot();
		int optind=0;
		File output=null;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD. "+Me.MAIL);
				System.err.println(Compilation.getLabel());
				System.err.println(" -h this screen");
				System.err.println(" -jar <dir0:jar1:jar2:dir1:...> add a jar in the jar list. If directory, will add all the *ar files");
				System.err.println(" -r <regex> add a pattern of classes to be ignored.");
				System.err.println(" -i ignore interfaces");
				System.err.println(" -m ignore classes iMplementing interfaces");
				System.err.println(" -d ignore declared-classes (classes with $ in the name)");
				System.err.println(" -o <file> output file");
				System.err.println("\n class-1 class-2 ... class-n");
				return;
				}
			else if (args[optind].equals("-jar"))
				{
				String tokens[]=args[++optind].split("[:]");
				for(String s:tokens)
					{
					s=s.trim();
					if(s.length()==0) continue;
					File file= new File(s);
					
					app.addFile(file);	
					}
				}
			else if (args[optind].equals("-r"))
				{
				app.ignorePattern.add(Pattern.compile(args[++optind]));
				}
			else if (args[optind].equals("-o"))
				{
				output=new File(args[++optind]);
				}
			else if (args[optind].equals("-i"))
				{
				app.usingInterfaces=false;
				}
			else if (args[optind].equals("-d"))
				{
				app.usingDeclaredClasses=false;
				}
			else if (args[optind].equals("-m"))
				{
				app.usingClassesImplementingInterfaces=false;
				}
			else if (args[optind].equals("-d"))
				{
				app.usingDeclaredClasses=false;
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
	    if(optind==args.length)
	    	{
	    	System.err.println("classes missing");
	    	return;
	    	}
	    HashSet<String> setOfClasses=new HashSet<String>();
	    while(optind< args.length)
	    	{
	    	setOfClasses.add(args[optind++]);
	    	}
	    app.run(setOfClasses);
	    PrintStream out= System.out;
	    if(output!=null)
	    	{
	    	out= new PrintStream(output);
	    	}
	    app.dot(out);
	    out.flush();
	    if(output!=null) out.close();
	  
	   
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
