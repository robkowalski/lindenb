package org.lindenb.tinytools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;

/**
 * JenaVelocity
 * http://plindenbaum.blogspot.com
 * @author pierre
 *
 */
public class JenaVelocity extends AbstractApplication
	{
	private PrintWriter out;
	private Vector<String> templatePath=new Vector<String>();
	private String primaryTemplate=null;
	private List<RDFStore> stores= new ArrayList<RDFStore>();
	private Map<String,Object> mapOfInstances= new HashMap<String,Object>();
	
	/** SPARQL utility */
	public class Sparql
		{
		public List<Object> select(String serviceURL,String query)
			{
			LOG.info("invoke "+query);
	        QueryExecution qe = QueryExecutionFactory.sparqlService(
	            		serviceURL, query
	                    ) ;
	        // Model m= qe.execDescribe();
	         List<Object> o=new ArrayList<Object>();
	         ResultSet row=qe.execSelect();
	         while(row.hasNext())
	         	{
	        	
	        	o.add(row.next()); 
	         	}
	         return o;   
			 }
		}
	

	/** RDFStore, wraps a JenaModel */
	public static class RDFStore
		{
		private Model model;
		private String name;
		private IDBConnection conn = null;
		public Model getModel()
			{	
			return this.model;
			}	
		public void close()
			{
			if(conn!=null)
				{
				try { conn.close(); } catch(Exception err){}
				}
			conn=null;
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
			ctx.put("sparql", new Sparql());
			for(RDFStore key:this.stores)
				{
				ctx.put(key.name, key.model);
				}
			
			for(String key:this.mapOfInstances.keySet())
				{
				ctx.put(key, this.mapOfInstances.get(key));
				}
		
			
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
	
	
	
	private String nonNullString(Map<String,Object> map,String key)
		{
		if(!map.containsKey(key)) throw new IllegalArgumentException(map.toString()+" is missing 'name'");
		Object o=map.get(key);
		if(o==null || !(o instanceof String)) throw new IllegalArgumentException("'"+key+"' is not a string "+map);
		return String.class.cast(o);
		}
	
	private String string(Map<String,Object> map,String key)
		{
		if(!map.containsKey(key)) return null;
		Object o=map.get(key);
		if(o!=null && !(o instanceof String)) throw new IllegalArgumentException("'"+key+"' is not a string "+map);
		return String.class.cast(o);
		}
	
	private void parseModelDefinition(Map<String,Object> map)
		{
		RDFStore store= new RDFStore();
		store.name=nonNullString(map,"name");
		
		for(RDFStore other:this.stores)
			{
			if(other.name.equals(store.name))
				{
				throw new IllegalArgumentException("duplicate model name:"+store.name);
				}
			}
		
		if(map.containsKey("url"))
			{
			String base = string(map,"base");
			String lang = string(map,"lang");
			Object rdfa= map.get("rdfa");
			String url= nonNullString(map, "url");
			LOG.info("reading RDF Model "+url);
			
			store.model= ModelFactory.createDefaultModel();
			store.model.read(
				String.class.cast(url),
				base,lang
				);
			if(map.containsKey("prefix-mapping"))
				{
				Map mapping=(Map)map.get("prefix-mapping");
				for(Object p: mapping.keySet())
					{
					store.model.setNsPrefix(p.toString(),mapping.get(p).toString());
					}
				}
			}
		else if(map.containsKey("jdbc-url"))
			{
			String driver=nonNullString(map, "jdbc-driver");
			try
				{
				Class.forName(driver);
				}
			catch(Exception err)
				{
				throw new IllegalArgumentException("driver not found:"+err.getMessage());
				}
			// Create database connection
			store.conn = new DBConnection(
					nonNullString(map, "jdbc-url"),
					nonNullString(map, "user"),
					nonNullString(map,"password"),
					nonNullString(map, "type")
					);
			ModelMaker maker = ModelFactory.createModelRDBMaker(store.conn) ;

			// create or open the default model
			store.model = maker.createDefaultModel();
			}
		else
			{
			throw new IllegalArgumentException("Don't know how to create this model "+map);
			}
		
		
		
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
		out.println(" -c <name> <full-class-name> put an new instance of the given class in the velocity context");
		out.println(" -m <JSON definition> model def");
		out.println(" -f <JSON definition file> model def");
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
		else if(args[optind].equals("-c"))
			{
			try {
				String name= args[++optind];
				Class<?> clazz= Class.forName(args[++optind]);
				mapOfInstances.put(name, clazz.newInstance());
				return optind;
				} 
			catch (Exception e)
				{
				throw new IllegalArgumentException("Error:"+e.getMessage());
				}
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
			app.processArgs(args);
			app.run();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		}

	}
