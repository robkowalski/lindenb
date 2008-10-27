package org.gnf.genewiki;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.lindenb.me.Me;
import org.lindenb.sql.SQLUtilities;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.DerbyStorageModel;
import org.lindenb.wikipedia.api.Entry;
import org.lindenb.wikipedia.api.MWException;
import org.lindenb.wikipedia.api.MWNamespace;
import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.Template;
import org.lindenb.wikipedia.api.User;
import org.lindenb.wikipedia.api.Wikipedia;
import org.lindenb.xml.XMLUtilities;

public class GeneWikiAnalysis
	extends DerbyStorageModel
	{
	/**
	 * Outout as SVG
	 * @author pierre
	 *
	 */
	private class SVGOutput
		{
		PrintStream out;
		int width=1000;
		int height=1000;
		int pixWindow=200;
		
		void print() throws MWException,IOException,SQLException
			{

			final String pallette[]=new String[]{
				"antiquewhite","blanchedalmond","darksalmon",
					"lightyellow","navajowhite","palegreen",
					"peachpuff","burlywood","cadetblue",
					"gainsboro","lightgreen"
				};
			Connection con= getConnection();
			Statement stmt= con.createStatement();
			Timestamp minDate= getMinDate();
			Timestamp maxDate= getMaxDate();
			int totalRev=  SQLUtilities.selectOneValue(stmt.executeQuery("select count(*) from MW.revision"), Number.class).intValue();
			//maxDate= new Timestamp(maxDate.getTime()+1);
			long steptime=((maxDate.getTime()-minDate.getTime())/(width/pixWindow));
			
			float y0[]= new float[1+this.width/pixWindow];
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println(SVG.DOCTYPE);
			out.print("<svg xmlns='"+SVG.NS+"'  xmlns:xlink='"+XLINK.NS+"'");
			out.print(" width='"+this.width+"' height='"+this.height+"' style='stroke-width:1'");
			out.print(">");
			out.print("<title>"+XMLUtilities.escape(Compilation.getName())+"</title>");
			out.print("<desc>"+XMLUtilities.escape(Compilation.getName())+" : Pierre Lindenbaum "+Me.MAIL+"</desc>");
			out.print("<rect x='0' y='0' width='"+this.width+"' height='"+this.height+"' style='stroke:blue; fill:white;' />");
			out.print("<g>");
			int colorIndex=0;
			for(Page page: listPages())
				{
				++colorIndex;
				float y1[]= new float[y0.length];
				System.arraycopy(y0, 0, y1, 0, y0.length);
				out.print("<g>");
				int prevCount=0;
				for(int i=0;i< y0.length;++i)
					{
					
					int revisionCount=0;
					Timestamp start= new Timestamp(minDate.getTime()+i*steptime);
					Timestamp end= new Timestamp(start.getTime()+steptime);
					for(@SuppressWarnings("unused") Revision r: listRevisions(page, null, start, end))
						{
						revisionCount++;
						}
					double dy= ((double)(revisionCount+prevCount)/(double)totalRev)*this.height;
					prevCount+=revisionCount;
					y1[i]+= dy;
					}
				
				
				out.print("<a xlink:href='"+Wikipedia.BASE+"/wiki/"+
						URLEncoder.encode( XMLUtilities.escape(page.getQName()),"UTF-8")+
						"' title=\'"+
						XMLUtilities.escape(page.getQName())+
						"'>");
				out.print("<polygon points=\"");
				for(int i=0;i< y0.length;++i)
					{
					if(i>0) out.print(" ");
					out.print(""+(i*pixWindow)+","+(width-y0[i]));
					}
				
				for(int i=y1.length-1;i>0;--i)
					{
					out.print(" "+(i*pixWindow)+","+(width-y1[i]));
					}
				out.print("\" style=\"fill:"+ pallette[colorIndex%pallette.length]+"\"/>");
				out.print("</a>");
				out.print("</g>");
				y0=y1;
				}
			out.print("</g>");
			out.println("</svg>"); 
			recycleConnection(con);
			}
		
		}
	
	private GeneWikiAnalysis()
		{
		}
	
	private Timestamp getMinDate() throws SQLException
		{
		Connection con= getConnection();
		Statement stmt= con.createStatement();
		Timestamp minDate= SQLUtilities.selectOneValue(stmt.executeQuery(
				"select min(when) from MW.revision"+
				(use_size_instead_of_rev?" where size>0":"")
				), Timestamp.class);
		recycleConnection(con);
		return minDate;
		}
	
	private Timestamp getMaxDate() throws SQLException
		{
		Connection con= getConnection();
		Statement stmt= con.createStatement();
		Timestamp maxDate= SQLUtilities.selectOneValue(stmt.executeQuery(
				"select max(when) from MW.revision"+
				(use_size_instead_of_rev?" where size>0":"")
		), Timestamp.class);
		recycleConnection(con);
		return maxDate;
		}

	
	
	private SVGOutput newSVGOutput() throws SQLException
		{
		return new SVGOutput();
		}
	
	private boolean use_size_instead_of_rev=true;
	private int user_window=5;
	private int revisions_window=10;
	
	private static String range(int value, int window)
		{
		int step=value/window;
		return "["+step*window+"-"+(step+1)*window+"[";
		}
	
	private void export2(PrintStream out) throws SQLException,MWException
		{
		final String TAB="\t";
		final int step=20;
		Timestamp minDate= getMinDate();
		Timestamp maxDate= getMaxDate();
		maxDate= new Timestamp(maxDate.getTime()+1L);
		double timeunit=(maxDate.getTime()-minDate.getTime())/(double)step;
		
		
		out.print("#Page"+TAB);
		out.print("Category"+TAB);
		out.print("Users"+TAB);
		out.print("Revisions");
		for(int i=0;i< step;++i)
			{
			out.print(TAB);
			out.print(new Timestamp(
					(long)(minDate.getTime()+ i*timeunit )
				));
			}
		out.println();
		
		for(Page page: this.listPages())
    		{
			out.print(page.getLocalName());
			out.print(TAB);
			boolean found=false;
			for(Category c: listCategories(page))
				{
				if(found)out.print('|');
				out.print(c.getLocalName());
				found=true;
				}

			out.print(TAB);
			Set<User> users= new HashSet<User>();
			Collection<Revision> pageRevisions=listRevisions(page, null, null,null);
			for(Revision r: pageRevisions)
				{
				users.add(r.getUser());
				}

			out.print(users.size());
			out.print(TAB);
			
			
			out.print(pageRevisions.size());
			int prev_size=0;
			int prev_revisions=0;
			for(int i=0;i< step;++i)
				{
				int size_in_this_range=Revision.NO_SIZE;
				
				out.print(TAB);
				Timestamp start=new Timestamp(
						(long)(minDate.getTime()+ i*timeunit )
						);
				Timestamp end=new Timestamp(
						(long)(minDate.getTime()+ (i+1)*timeunit )
						);
				
				
				for(Revision r:pageRevisions)
					{
					if(	start.getTime()<= r.getDate().getTime() &&
							r.getDate().getTime()<end.getTime() )
						{
						prev_revisions++;
						if(r.getSize()!=Revision.NO_SIZE)
							{
							size_in_this_range=Math.max(r.getSize(),size_in_this_range);
							}
						}
					}
				if(size_in_this_range==Revision.NO_SIZE)
					{
					size_in_this_range=prev_size;
					}
				out.print(""+size_in_this_range+";"+prev_revisions);
				
				
				prev_size=size_in_this_range;
				}
			out.println();
    		}
		}
	
	private void manyEyes(PrintStream out) throws SQLException,MWException
		{
		final String TAB="\t";
		final int step=20;
		Timestamp minDate= getMinDate();
		Timestamp maxDate= getMaxDate();
		maxDate= new Timestamp(maxDate.getTime()+1L);
		double timeunit=(maxDate.getTime()-minDate.getTime())/(double)step;
		
		
		
		out.print("Page"+TAB);
		out.print("Chromosome"+TAB);
		out.print("Users"+TAB);
		out.print("Revisions"+TAB);
		for(int i=0;i+1< step;++i)
			{
			out.print(TAB);
			out.print(new Timestamp(
					(long)(minDate.getTime()+ i*timeunit )
				));
			}
		out.println();
		
		for(Page page: this.listPages())
    		{
			out.print(page.getLocalName());
			out.print(TAB);
			Category chrom=null;
			for(Category c: listCategories(page))
				{
				if(c.getLocalName().startsWith("Genes on chromosome "))
					{
					if(chrom!=null) { chrom=null; break;}
					chrom=c;
					}
				}
			out.print(chrom==null?"N/A":chrom.getLocalName());
			int countRevisions=0;
			out.print(TAB);
			Set<User> users= new HashSet<User>();
			Collection<Revision> pageRevisions=listRevisions(page, null, null,null);
			for(Revision r:pageRevisions)
				{
				users.add(r.getUser());
				}

			out.print("users:"+range(users.size(), user_window));
			out.print(TAB);
			
			
			out.print("revisions:"+range(pageRevisions.size(), revisions_window));
			int prev_size=0;
			for(int i=0;i< step;++i)
				{
				int size_in_this_range=0;
				int revisions_in_this_range=0;
				out.print(TAB);
				Timestamp start=new Timestamp(
						(long)(minDate.getTime()+ i*timeunit )
						);
				Timestamp end=new Timestamp(
						(long)(minDate.getTime()+ (i+1)*timeunit )
						);
				
				
				for(Revision r:pageRevisions)
					{
					if(	start.getTime()<= r.getDate().getTime() &&
							r.getDate().getTime()<end.getTime() )
						{
						revisions_in_this_range++;
						size_in_this_range+= Math.max(r.getSize(),0);//size undefined before 2007 in wikipedia
						}
					}
				
				
				if(revisions_in_this_range==0)
					{
					size_in_this_range=prev_size;
					}
				else
					{
					size_in_this_range/=revisions_in_this_range;
					}
				
				
				if(use_size_instead_of_rev)
					{
					out.print(size_in_this_range);
					}
				else
					{
					out.print(countRevisions);
					}
				prev_size=size_in_this_range;
				}
			out.println();
    		}
		}
	
	private void dump(PrintStream out) throws SQLException,MWException
		{
		final String TAB="\t";
		for(Page page: this.listPages())
	    	{
			Set<Category> cats= this.listCategories(page);
	    	for(Revision r: this.listRevisions(page, null, null, null))
	    		{
	    		out.print(
	    			r.getEntry()+TAB
	    			);
	    		
	    		int n=0;
	    		out.print("(");
	    		for(Category cat:cats)
	    			{
	    			out.print(n!=0?"|":"");
	    			out.print(cat);
	    			n++;
	    			}
	    		out.print(")"+TAB);
	    		
	    		out.println(
	    			r.getUser()+TAB+
	    			r.getDate()+TAB+
	    			r.getSize()+TAB+
	    			"\""+C.escape(r.getComment())+"\""
	    			);
	    		}
	    	}
		}
	
	public static void main(String[] args)
		{
		try {
			Template template=new Template("PBB Controls");
			GeneWikiAnalysis app= new GeneWikiAnalysis();
			File dbFile= null;
			File outFile= null;
			int limit=Integer.MAX_VALUE;
			int optind=0;
			String command=null;
		    while(optind<args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("Pierre Lindenbaum PhD. "+Me.MAIL);
					System.err.println(Compilation.getLabel());
					System.err.println("-h this screen");
					System.err.println("-f <directory> derby database folder");
					System.err.println("-L <integer> limit input for build default:"+limit);
					System.err.println("-o <file> output (default: stdout)");
					System.err.println("-t <template> qualified templateused as seed default:"+template);
					System.err.println("-p program can be:");
					System.err.println("	clear :clear the database");
					System.err.println("	build :fill the database with the revisions of all pages containing "+template);
					System.err.println("	dump  :dump text file the database");
					System.err.println("	svg  :dump diagram of the database");
					System.err.println("	ibm  :dump diagram of the IBM/ManyEyes");
					System.err.println("	rdf  :dump diagram to RDF");
					System.err.println("	export2  :dump diagram to export2 fmt");
					return;
					}
				 else if (args[optind].equals("-f"))
				     {
					 dbFile= new File(args[++optind]);
				     }
				 else if (args[optind].equals("-L"))
				     {
					 limit= Integer.parseInt(args[++optind]);
				     }
				 else if (args[optind].equals("-t"))
				     {
					 Entry e= Entry.create(args[++optind]);
				     if(e.getNamespace()!=MWNamespace.Template)
				     	{
				    	System.err.println(e.toString()+" not a template");
				    	return;
				     	}
				     template =Template.class.cast(e);
				     }
				 else if (args[optind].equals("-o"))
				     {
					 outFile= new File(args[++optind]);
				     }
				 else if (args[optind].equals("-p"))
				     {
					 command=args[++optind];
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
	    
		    if(dbFile==null)
		    	{
		    	System.err.println("derby database file not declared");
		    	return;
		    	}
		    
		    if(optind!=args.length)	
		    	{
		    	System.err.println("Illegal number of arguments");
		    	return;
		    	}
		    
		    if(command==null)
		    	{
		    	System.err.println("argument for program missing");
		    	return;
		    	}
		    
		    app.open(dbFile);
		    
		    if(command.equals("clear"))	
		    	{
		    	app.clear();
		    	System.out.println("Done.");
		    	}
		    else if(command.equals("build"))	
		    	{
		    	MWQuery query= new MWQuery();
		    	int count=0;
		    	for(Page page:query.listPagesEmbedding(template))
		    		{
		    		if(count >= limit) break;
		    	
		    		System.out.println(""+page+ " contains "+template);
		    		for(Revision rev:query.listRevisions(page))
		    			{
		    			app.insertRevision(rev);
		    			}

		    		app.insertLinks(page,query.listCategories(page));
		    				    		
		    		count++;
		    		}
		    	System.out.println("Done.");
		    	}
		    else if(command.equals("dump"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	app.dump(out);
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else if(command.equals("svg"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	SVGOutput svgOutput= app.newSVGOutput();
		    	svgOutput.out=out;
		    	svgOutput.print();
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else if(command.equals("ibm"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	app.manyEyes(out);
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else if(command.equals("rdf"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	app.toRDF(out,Wikipedia.BASE+"/wiki");
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else if(command.equals("export2"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	app.export2(out);
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else
		    	{
		    	System.err.println("Illegal arguments");
		    	}
		    
		    app.close();
		    
		} catch (Exception e) {
		e.printStackTrace();
	}
}
	}
