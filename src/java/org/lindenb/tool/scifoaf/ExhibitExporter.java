package org.lindenb.tool.scifoaf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lindenb.sw.model.DerbyModel;
import org.lindenb.sw.model.RDFException;
import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.FOAF;
import org.lindenb.sw.vocabulary.Geo;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;

public class ExhibitExporter
{
private DerbyModel derby=null;
private File outputDirectory=null;
private int iconSize=64;
private HashMap<DerbyModel.Resource, Integer> rsrc2id=new HashMap<DerbyModel.Resource, Integer>();
ExhibitExporter(DerbyModel derby)
	{
	this.derby=derby;
	}
	
public void setOutputDirectory(File outputDirectory)
	{
	this.outputDirectory=outputDirectory;
	}

public DerbyModel getRDFModel()
	{
	return this.derby;
	}

private DerbyModel.Resource _rsrc(String ns,String local)
	{
	return getRDFModel().createResource(ns, local);
	}

String getTitle(DerbyModel.Resource subject) throws RDFException
	{
	String title =subject.getString(_rsrc(FOAF.NS,"name"));
	if(title==null || title.trim().length()==0)
		{
		title= subject.getString(_rsrc(DC.NS,"title"));
		}
	if(title==null) title= subject.getURI();
	return title;
	}

private BufferedImage loadDepiction(DerbyModel.Resource foafAgent) throws IOException,RDFException
	{
	BufferedImage img=null;
	DerbyModel.CloseableIterator<DerbyModel.Statement> iter1= getRDFModel().listStatements(
		foafAgent,
		_rsrc(FOAF.NS,"depiction")
		, null);
	
	while(iter1.hasNext())
		{
		DerbyModel.Statement stmt1= iter1.next();
		if(!stmt1.getValue().isResource()) continue;
		DerbyModel.Resource imgrsrc= stmt1.getValue().asResource();
		if(!imgrsrc.hasProperty(
			_rsrc(RDF.NS, "type"),
			_rsrc(FOAF.NS, "Image")	
			)) continue;			
		
		if(!imgrsrc.isURL()) continue;
		URL url= imgrsrc.asURL();
		if(url==null) continue;
		try
			{
			img = ImageIO.read(url);
			break;
			}
		catch (IOException e)
			{
			Debug.debug(e);
			}
		}
	
	iter1.close();
	return img;
	}

private String getId(DerbyModel.Resource r)
	{
	Integer n=rsrc2id.get(r);
	if(n==null)
		{
		n=rsrc2id.size()+1;
		rsrc2id.put(r,n);
		}
	return "id"+n;
	}

private boolean oneLiteralProperty(PrintWriter out,boolean needComma,DerbyModel.Resource subject,String label,DerbyModel.Resource predicate) throws RDFException
	{
	String v= subject.getString(predicate);
	if(v==null) return false;
	if(needComma) out.print(",");
	out.print("\""+C.escape(label)+"\":\""+C.escape(v)+"\"");
	return true;
	}
	
public void run() throws IOException,RDFException
	{
	rsrc2id.clear();
	if(this.outputDirectory==null) return;
	if(this.outputDirectory.exists() && !this.outputDirectory.isDirectory())
		{
		fatalError(this.outputDirectory.toString()+" exists and is not a directory");
		return;
		}
	
	this.outputDirectory.mkdir();
	
	try
		{
		PrintWriter out= new PrintWriter(new FileWriter(new File(this.outputDirectory,"scifoaf.js")));
		out.println("{\"types\":{");
		out.println("'Person':{pluralLabel:'People'},");
		out.println("'Article':{pluralLabel:'Articles'},");
		out.println("'Group':{pluralLabel:'Groups'},");
		out.println("'Place':{pluralLabel:'Places'}");
		out.println("},");
		
		out.println("'properties':{");
		out.println("'papers':{'valueType':'item'}");
		out.println("},");
		
		out.println("\"items\":[");
		boolean found=false;
		DerbyModel.CloseableIterator<DerbyModel.Resource> iter1= getRDFModel().listSubjects();
		while(iter1.hasNext())
			{
			DerbyModel.Resource subject= iter1.next();
			if(subject.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Person")))
				{
				if(found) out.println(",");
				found=true;
				out.println("{");
				out.print("\"type\":\"Person\"");
				out.print("\n,\"label\":\""+getId(subject)+"\"");
				//String s= subject.getString(_rsrc(FOAF.NS, "name"));
				out.print(",\n\"name\":\""+C.escape(getTitle(subject))+"\"");
				out.print(",\n\"papers\":[");
				boolean found2=false;
				DerbyModel.CloseableIterator<DerbyModel.Statement> iter2=getRDFModel().listStatements(null,_rsrc(FOAF.NS,"maker"),subject);
				while(iter2.hasNext())
					{
					DerbyModel.Resource paper= iter2.next().getSubject();
					if(!paper.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Document"))) continue;
					if(found2) out.print(",");
					found2=true;
					out.print("'"+getId(paper)+"'");
					}
				iter2.close();
				out.print("]");
				out.println("}");
				}
			else if(subject.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Document")))
				{
				if(found) out.println(",");
				found=true;
				out.println("{");
				out.print("\"type\":\"Article\"");
				out.print("\n,\"label\":\""+getId(subject)+"\"");
				//String s= subject.getString(_rsrc(DC.NS, "title"));
				out.print(",\n\"title\":\""+C.escape(getTitle(subject))+"\"");
				
				
				out.print(",\n\"authors\":[");
				boolean found2=false;
				DerbyModel.CloseableIterator<DerbyModel.Statement> iter2=getRDFModel().listStatements(subject,_rsrc(FOAF.NS,"maker"),null);
				while(iter2.hasNext())
					{
					DerbyModel.Statement stmt2= iter2.next();
					if(!stmt2.getValue().isResource()) continue;
					DerbyModel.Resource author= stmt2.getValue().asResource();
					if(!author.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Person"))) continue;
					if(found2) out.print(",");
					found2=true;
					out.print("'"+getId(author)+"'");
					}
				iter2.close();
				out.print("]");
				
				
				out.println("}");
				}
			else if(
					subject.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Group")) ||
					subject.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(FOAF.NS, "Organization"))
					)
				{
				if(found) out.println(",");
				found=true;
				out.println("{");
				out.print("\"type\":\"Group\"");
				out.print("\n,\"label\":\""+getId(subject)+"\"");
				//String s= subject.getString(_rsrc(FOAF.NS, "name"));
				out.print(",\n\"name\":\""+C.escape(getTitle(subject))+"\"");
				out.println("}");
				}
			else if(subject.hasProperty(_rsrc(RDF.NS, "type"),_rsrc(Geo.NS, "SpatialThing")))
				{
				if(found) out.println(",");
				found=true;
				out.println("{");
				out.print("\"type\":\"Place\"");
				out.print("\n,\"label\":\""+getId(subject)+"\"");
				//String s= subject.getString(_rsrc(FOAF.NS, "name"));
				out.print(",\n\"title\":\""+C.escape(getTitle(subject))+"\"");
				out.println("}");
				}
			}
		iter1.close();
		out.println("]}");
		out.flush();
		out.close();
		
		
		
		out= new PrintWriter(new FileWriter(new File(this.outputDirectory,"scifoaf.html")));
		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.print("<html>");
		out.print("<head><title>SciFOAF</title>");
		out.print("<link href='scifoaf.js' type='application/json' rel='exhibit/data' />");
		out.print("<script src='http://static.simile.mit.edu/exhibit/api-2.0/exhibit-api.js' type='text/javascript' />");
		out.print("<style></style>");
		out.print("</head><body>");
		out.print("<h1>SciFOAF</h1>");
		out.print("<hr/>");
		out.print("<a href='scifoaf.js'>Exhibit JSON data file</a>.<br/>");
		out.print("<table width='100%'>");
		out.print("<tr valign='top'>");
		out.print("<td ex:role='viewPanel'>");
		
		out.print("<div ex:role='view' class='Person'>Hello World</div>");
		out.print("<div ex:role='view' class='Article'>Hello 1</div>");
		out.print("<div ex:role='view' class='Group'>Hello 2</div>");
		
		out.print("</td>");
		
		out.print("<td width='25%'>");
		out.print("  <div ex:role='facet' ex:facetClass='TextSearch'></div>");
		out.print("  <div ex:role='facet' ex:expression='.subject' ex:facetLabel='Subject'></div>");
		out.print("</td>");
		
		out.print("</tr>");
		out.print("</table>");
		out.print("</body></html>");
		out.flush();
		out.close();
		
		}
	catch(Exception err)
		{
		fatalError(err);
		}
	rsrc2id.clear();
	}

public void fatalError(Object o)
	{
	System.err.println(o);
	}

public static void main(String[] args) {
	try {
		Debug.setDebugging(false);
    	int optind=0;
    	int iconsize=-1;
    	String output="EXHIBIT";
    	while(optind<args.length)
		        {
		        if(args[optind].equals("-h"))
		           {
		        	System.err.println(Compilation.getLabel());
		        	System.err.println("\t-h this screen");
		        	System.err.println("\t-o <file> output directory");
		        	System.err.println("\t-icn <integer> icon size");
		        	System.err.println("\t-d turns debugging on");
					return;
		           	}
		        else if(args[optind].equals("-d"))
		        	{
		        	Debug.setDebugging(true);
		        	}
		        else if(args[optind].equals("-o"))
		        	{
		        	output= args[++optind];
		        	}
		        else if(args[optind].equals("-icn"))
		        	{
		        	iconsize= Integer.parseInt(args[++optind]);
		        	}
		       else if(args[optind].equals("--"))
		            {
		            ++optind;
		            break;
		            }
		        else if(args[optind].startsWith("-"))
		            {
		            throw new IllegalArgumentException("Unknown option "+args[optind]);
		            }
		        else
		            {
		            break;    
		            }
		        ++optind;
		        }
    	
    	
    	if(optind+1!=args.length)
    		{	
    		throw new IllegalArgumentException("Bad number of argments.");
		}
    	File fileIn= new File(args[optind++]);
    	if(!fileIn.exists())
    		{
    		throw new FileNotFoundException("Cannot find "+fileIn);
    		}
    	if(!fileIn.isDirectory())
    		{
    		throw new IOException("Not a directory : "+fileIn);
    		}
    	
    	if(output==null)
    		{
    		System.err.println("option -o undefined (output directory name)");
    		}
    	
    	try {
    		Class.forName(DerbyModel.JDBC_DRIVER_NAME);
    		} 
    	catch (ClassNotFoundException e)
    		{
    		System.err.println("Cannot find the SQL Driver "+DerbyModel.JDBC_DRIVER_NAME+"\n"+
    				" in the java $CLASSPATH\n"+
    				"See http://developers.sun.com/javadb"
    				);
    		return;
    		}
    	ExhibitExporter app= new ExhibitExporter(new DerbyModel(fileIn));
    	if(iconsize>1) app.iconSize=iconsize;
    	app.setOutputDirectory(new File(output));
    	app.run();
		} 
	catch (Exception e)
		{
		e.printStackTrace();
		}
	}
}
