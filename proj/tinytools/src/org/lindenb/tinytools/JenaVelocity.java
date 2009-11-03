package org.lindenb.tinytools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.lindenb.io.IOUtils;
import org.lindenb.json.JSONParser;
import org.lindenb.util.AbstractApplication;
import org.lindenb.util.StringUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class JenaVelocity extends AbstractApplication
	{
	private PrintWriter out;
	private Vector<String> templatePath=new Vector<String>();
	private String primaryTemplate=null;
	private List<RDFStore> stores= new ArrayList<RDFStore>();
	
	/** RDFStore, wraps a JenaModel */
	public static class RDFStore
		{
		private Model model;
		private String name;
		public long getSize()
			{
			return this.model.size();
			}
		}
	
	private JenaVelocity()
		{
        this.out= new PrintWriter(System.out);
		}
	
	private int run() throws Exception
		{
		VelocityEngine engine=null;
		try
			{
			if(this.primaryTemplate==null)
				{
				System.err.println("Undefined primary template.");
				return -1;
				}
			if(this.stores.isEmpty())
				{
				System.err.println("No RDF Store defined.");
				return -1;
				}
			
			if(this.templatePath.isEmpty())
				{
				templatePath.add(System.getProperty("user.dir", "."));
				for(File root:File.listRoots())
			        	{
			        	LOG.info("add "+root.getAbsolutePath());
			        	templatePath.add(root.getAbsolutePath());
			        	}
				}
			engine = new VelocityEngine();
			engine.setProperty(Velocity.RESOURCE_LOADER,"file"); 
			engine.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, this.templatePath);
			engine.init();
			
			VelocityContext ctx=new VelocityContext();
			ctx.put("rdfstores", this.stores);
			Template template= engine.getTemplate(this.primaryTemplate);
			template.merge(ctx, this.out);
			this.out.flush();
			return 0;
			}
		catch (ResourceNotFoundException e)
			{
			System.err.println("Error:"+e.getMessage());
			return -1;
			}
		catch (Throwable e)
			{
			e.printStackTrace();
			return -1;
			}
		finally
			{
			this.out.flush();
			}
		}
	
	
	private void parseModelDefinition(Map<String,Object> map)
		{
		RDFStore store= new RDFStore();
		if(!map.containsKey("name")) throw new IllegalArgumentException(map.toString()+" is missing 'name'");
		Object name=map.get("name");
		if(name==null || !(name instanceof String)) throw new IllegalArgumentException("'name' is not a string "+map);
		store.name=name.toString();
		
		Object base=map.get("base");
		if(base!=null && !(base instanceof String )) throw new IllegalArgumentException("'base' is not a string "+map);	
		Object lang=map.get("lang");
		if(lang!=null && !(lang instanceof String )) throw new IllegalArgumentException("'lang' is not a string "+map);	
	
		if(!map.containsKey("url")) throw new IllegalArgumentException(map.toString()+" is missing 'url'");
		Object url=map.get("url");
		if(url==null || !(url instanceof String )) throw new IllegalArgumentException("'url' is not a string "+map);	
		
		
		for(RDFStore other:this.stores)
			{
			if(other.name.equals(store.name))
				{
				throw new IllegalArgumentException("duplicate model name:"+store.name);
				}
			}
		LOG.info("reading RDF Model "+url);
		store.model= ModelFactory.createDefaultModel();
		store.model.read(
			String.class.cast(url),
			base==null?null:base.toString(),
			lang==null?null:lang.toString()
			);
		this.stores.add(store);
		}
	
	@SuppressWarnings("unchecked")
	private void parseModelDefinition(Reader r)
		throws IllegalArgumentException
		{
		Object o=null;
		try
			{
			JSONParser parser= new JSONParser(r);
			o=parser.any();
			parser.eof();
			}
		catch (Exception err)
			{
			throw new IllegalArgumentException(err);
			}
		if(o==null) throw new IllegalArgumentException("bad RDFStore definition:null.");
		else if(o instanceof Map<?,?>)
			{
			parseModelDefinition(Map.class.cast(o));
			}
		else if(o instanceof List<?>)
			{
			List array= List.class.cast(o);
			for(Object item:array)
				{
				if(item==null) throw new IllegalArgumentException("bad RDFStore array item is null.");
				if(!(item instanceof Map<?,?>)) throw new IllegalArgumentException("bad RDFStore array item is not a JSON Object but a "+item.getClass());
				parseModelDefinition(Map.class.cast(item));
				}
			}
		else
			{
			throw new IllegalArgumentException("bad RDFStore object. Not a JSON Object or Array: class is "+o.getClass());
			}
		}
	
	@Override
	protected void usage(PrintStream out)
		{
		super.usage(out);
		out.println(" -path <path1:path2:path3...> add the path for velocity");
		out.println(" -t <path> primary template path <REQUIRED>");
		}
	
	@Override
	protected int processArg(String[] args, int optind)
		{
		if(args[optind].equals("-path"))
			{
			String paths[]=args[++optind].split(File.pathSeparator);
			for(String s:paths)
				{
				if(StringUtils.isBlank(s)) continue;
				LOG.info("adding template path "+s);
				this.templatePath.insertElementAt(s.trim(),0);
				}
			return optind;
			}
		else if(args[optind].equals("-t"))
			{
			this.primaryTemplate=args[++optind];
			return optind;
			}
		else if(args[optind].equals("-m"))
			{
			StringReader r=new StringReader(args[++optind]);
			parseModelDefinition(r);
			return optind;
			}
		else if(args[optind].equals("-f"))
			{
			Reader r=null;
			try{
				r=new FileReader(args[++optind]);
				parseModelDefinition(r);
				}
			catch(FileNotFoundException err)
				{
				throw new IllegalArgumentException(err);
				}
			finally
				{
				IOUtils.safeClose(r);
				}
			return optind;
			}
		return super.processArg(args, optind);
		}
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			JenaVelocity app= new JenaVelocity();
			app.primaryTemplate="/home/pierre/src/cephb/src/mw4ceph/src/META-INF/runs2wiki.vm";
			app.run();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		}

	}
