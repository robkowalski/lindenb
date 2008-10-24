package org.gnf.genewiki;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import org.lindenb.me.Me;
import org.lindenb.sql.SQLUtilities;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.Template;
import org.lindenb.wikipedia.api.Wikipedia;
import org.lindenb.wikipedia.tool.Statistics;
import org.lindenb.xml.XMLUtilities;

public class GeneWikiAnalysis
	extends Statistics
	{
	
	private class SVGOutput
		{
		PrintStream out;
		int width=1000;
		int height=1000;
		int pixWindow=200;
		String quote(Object o)
			{
			return "\""+o+"\"";
			}
		
		void print() throws SQLException,IOException
			{

			final String pallette[]=new String[]{
				"antiquewhite","blanchedalmond","darksalmon",
					"lightyellow","navajowhite","palegreen",
					"peachpuff","burlywood","cadetblue",
					"gainsboro","lightgreen"
				};
			Connection con= getConnection();
			Statement stmt= con.createStatement();
			Timestamp minDate= SQLUtilities.selectOneValue(stmt.executeQuery("select min(when) from MW.revision"), Timestamp.class);
			Timestamp maxDate= SQLUtilities.selectOneValue(stmt.executeQuery("select max(when) from MW.revision"), Timestamp.class);
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
	
	private GeneWikiAnalysis(File dbFile) throws SQLException
		{
		super(dbFile);
		}
	
	
	private SVGOutput newSVGOutput()
		{
		return new SVGOutput();
		}
	
	
	public static void main(String[] args)
		{
		try {
			Template template=new Template("PBB Controls");
			File dbFile= null;
			File outFile= null;
			int optind=0;
		    while(optind<args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("Pierre Lindenbaum PhD. "+Me.MAIL);
					System.err.println(Compilation.getLabel());
					System.err.println("-h this screen");
					System.err.println("-f derby database folder");
					System.err.println("-o <file> output (default: stdout)");
					System.err.println("(command) can be:");
					System.err.println("	clear :clear the database");
					System.err.println("	build :fill the database with the revisions of all pages containing "+template);
					System.err.println("	dump  :dump the database to stdout");
					return;
					}
				 else if (args[optind].equals("-f"))
				     {
					 dbFile= new File(args[++optind]);
				     }
				 else if (args[optind].equals("-o"))
				     {
					 outFile= new File(args[++optind]);
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
		    
		    if(optind==args.length)	
		    	{
		    	System.err.println("argument missing");
		    	return;
		    	}
		    
		    if(optind+1==args.length && args[optind].equals("clear"))	
		    	{
		    	GeneWikiAnalysis app = new GeneWikiAnalysis(dbFile);
		    	app.clear();
		    	app.close();
		    	System.out.println("Done.");
		    	}
		    else if(optind+1==args.length && args[optind].equals("build"))	
		    	{
		    	GeneWikiAnalysis app = new GeneWikiAnalysis(dbFile);
		    	MWQuery query= new MWQuery();
	
		    	for(Page page:query.listPagesEmbedding(template))
		    		{
		    		System.out.println(""+page+ " contains "+template);
		    		for(Revision rev:query.listRevisions(page))
		    			{
		    			app.insertRevision(rev);
		    			}
		    		
		    		}
		    	app.close();
		    	System.out.println("Done.");
		    	}
		    else if(optind+1==args.length && args[optind].equals("dump"))
		    	{
		    	final String TAB="\t";
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	GeneWikiAnalysis app = new GeneWikiAnalysis(dbFile);
		    	for(Revision r: app.listRevisions(null, null, null, null))
		    		{
		    		System.out.println(
		    			r.getEntry()+TAB+
		    			r.getUser()+TAB+
		    			r.getDate()+TAB+
		    			r.getSize()+TAB+
		    			"\""+C.escape(r.getComment())+"\""
		    			);
		    		}
		    	app.close();
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else if(optind+1==args.length && args[optind].equals("svg"))
		    	{
		    	PrintStream out= System.out;
		    	if(outFile!=null) out= new PrintStream(outFile);
		    	GeneWikiAnalysis app = new GeneWikiAnalysis(dbFile);
		    	SVGOutput svgOutput= app.newSVGOutput();
		    	svgOutput.out=out;
		    	svgOutput.print();
		    	app.close();
		    	if(outFile!=null) { out.flush(); out.close();}
		    	}
		    else
		    	{
		    	System.err.println("Illegal arguments");
		    	}
		} catch (Exception e) {
		e.printStackTrace();
	}
}
	}
