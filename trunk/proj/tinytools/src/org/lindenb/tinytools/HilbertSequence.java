package org.lindenb.tinytools;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.lindenb.awt.ColorUtils;
import org.lindenb.io.IOUtils;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;
import org.lindenb.xml.XMLUtilities;



/**
 * Program Drawin a HilbertSequence for a set of Gene
 * Mostly inspired by Gareth Palidwor's post at 
 * http://www.palidwor.com/blog/?p=123
 * 
 * and the code was copied from the Wikpedia page
 * http://en.wikipedia.org/wiki/Hilbert_curve
 *
 */
public class HilbertSequence
	{
	private static class Gene
		{
		String name;
		int start;
		int end;
		}
	
	private List<Gene>  genes= new ArrayList<Gene>();
	/** with/height of the final picture */
	private int imageSize=512;
	/** size of an edge */
	private int dist=imageSize;
	/** size (in pb) of an edge */
	private double d_base=0;
	/** last index of the position in the genome */
	private double prev_base=0;
	/** ouput stream */
	private PrintStream out;
	/** last time we plot X */
	private int prev_x=0;
	/** last time we plot Y */
	private int prev_y=0;
	/** genome size */
	private int genomeSize=-1;
	/** level of recursion */
	private int recursionLevel=5;
	/** current segment index (for coloring )*/
	private int segmentIndex=0;
	
	/** ctor */
	private HilbertSequence()
		{
		}
	
	/** do our stuff , algorithm mostly copied from wikipedia */
	void paint(PrintStream writer) throws IOException
		{
		this.out=writer;
		this.dist=imageSize;
		this.prev_base=0;
		Collections.sort(this.genes, new Comparator<Gene>()
				{
				@Override
				public int compare(Gene o1, Gene o2) {
					return o1.start-o2.start;
					}
				});
		if(this.genomeSize<2)
			{
			this.genomeSize=2;
			for(Gene g:this.genes) this.genomeSize=Math.max(this.genomeSize, g.end);
			}
		
		
		out.println("<?xml version=\"1.0\"?>");
		out.println(SVG.DOCTYPE);
		out.println("<svg xmlns=\""+SVG.NS+"\" " +
				   " xmlns:xlink=\""+XLINK.NS+"\" version=\"1.1\" "+
				   " width=\""+imageSize+"\" "+
				   " height=\""+imageSize+"\" "+
				   " style='stroke:black;' "+
				   ">");
		
	    for (int i=recursionLevel; i>0; i--)
	    	{
	    	dist /= 2;
	    	}
	    //4 16 64 256 1023
	    d_base= ((double)genomeSize/Math.pow(4,recursionLevel));
	    
	    prev_x= dist/2;
	    prev_y=dist/2;
	    segmentIndex=0;
	    HilbertU(recursionLevel); // start recursion

		out.println("</svg>");
		out.flush();
		out=null;
		}
	/**
	 * Draw a line on the picture
	 * @param deltaX
	 * @param deltaY
	 */
	private void lineRel(int deltaX, int deltaY)
	   {
	   double stepRatio= (double)segmentIndex/Math.pow(4,recursionLevel);
	   int r= 255-(int)(255.0*stepRatio);
	   int g= (int)(155.0*stepRatio);
	   int b= 60+(int)(155.0*(1.0-stepRatio));
	   
	   
	   int x2= prev_x + deltaX;
	   int y2= prev_y + deltaY;
	   out.print(
		   "<line x1='"+prev_x+"'  y1='"+prev_y+"' style='stroke:"+ColorUtils.toRGB(new Color(r,g,b))+"' "+
		   " x2='"+x2+"'  y2='"+y2+"'/>"
		   );
	  int chromStart=(int)prev_base;
	  int chromEnd=chromStart+(int)d_base;
	  
	  //look for a gene in this segment
	  for(Gene gene:this.genes)
	   	{
		if(gene.end< chromStart || gene.start >chromEnd) continue;   
	   	int geneStart = Math.max(chromStart, gene.start);
		int geneEnd = Math.min(chromEnd, gene.end);
		double r1= ((geneStart-chromStart)/d_base);
		double r2= ((geneEnd-chromStart)/d_base);
		double gx = prev_x+(x2-prev_x)*r1;
		double gy = prev_y+(y2-prev_y)*r1;
		out.print("<line x1='"+gx+"'  y1='"+gy+"' ");
		
		gx = prev_x+(x2-prev_x)*r2;
		gy = prev_y+(y2-prev_y)*r2;
		out.print(" x2='"+gx+"'  y2='"+gy+"' style='stroke-width:4;stroke:red; fill:yellow;'" +
				" title='"+ XMLUtilities.escape(gene.name+":"+gene.start+"-"+gene.end)+"'/>"
				);
	   	}
	   
	   prev_x =x2;
	   prev_y =y2;
	   prev_base+= d_base;
	   ++segmentIndex;
	   }
    // Make U-shaped curve at this scale:
    private void HilbertU(int level)
    	{
        if (level > 0) {
            HilbertD(level-1);    this.lineRel(0, dist);
            HilbertU(level-1);    this.lineRel(dist, 0);
            HilbertU(level-1);    this.lineRel(0, -dist);
            HilbertC(level-1);
        }
    	}
 
    // Make D-shaped (really "]" shaped) curve at this scale:
    private void HilbertD(int level) {
        if (level > 0) {
            HilbertU(level-1);    this.lineRel(dist, 0);
            HilbertD(level-1);    this.lineRel(0, dist);
            HilbertD(level-1);    this.lineRel(-dist, 0);
            HilbertA(level-1);
        }
    }
 
    // Make C-shaped (really "[" shaped) curve at this scale:
    private void HilbertC(int level) {
        if (level > 0) {
            HilbertA(level-1);    this.lineRel(-dist, 0);
            HilbertC(level-1);    this.lineRel(0, -dist);
            HilbertC(level-1);    this.lineRel(dist, 0);
            HilbertU(level-1);
        }
    }
 
    // Make A-shaped (really "⊓" shaped) curve at this scale:
    private void HilbertA(int level) {
        if (level > 0) {
            HilbertC(level-1);    this.lineRel(0, -dist);
            HilbertA(level-1);    this.lineRel(-dist, 0);
            HilbertA(level-1);    this.lineRel(0, dist);
            HilbertD(level-1);
        }
    }
	
    private void readGeneList(BufferedReader in) throws IOException
    	{
    	final Pattern tab=Pattern.compile("[\t]");
    	String line=null;
    	while((line=in.readLine())!=null)
    		{
    		if(line.trim().length()==0 || line.startsWith("#")) continue;
    		String token[]=tab.split(line);
    		if(token.length<3 || token[0].trim().length()==0 ||
    			!Cast.Integer.isA(token[1]) ||
    			!Cast.Integer.isA(token[2])
    			)
    			{
    			System.err.println("Bad input in "+line);
    			continue;
    			}
    		Gene g= new Gene();
    		g.name=token[0];
    		g.start=Cast.Integer.cast(token[1]) ;
    		g.end=Cast.Integer.cast(token[2]);
    		this.genes.add(g);
    		}
    	}
	
	public static void main(String[] args)
		{
		try
			{
			HilbertSequence app=new HilbertSequence();
			File fileout=null;
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Displays a SVG Hilbert Curve for a DNA sequence, displaying the genes.");
					System.err.println("Idea from Gareth Palidwor.");
					System.err.println("Algorithm from wikipedia.");
					System.err.println(" -o <file> output file");
					System.err.println(" -s <int> image size");
					System.err.println(" -L <int> genome size (default=gene.max-start");
					System.err.println(" -X <int> level of recursion");
					System.err.println("<input|stdin> (= tab delim file: name,start,end");
					}
				else if(args[optind].equals("-s"))
					{
					app.imageSize= Integer.parseInt(args[++optind]);
					
					}
				else if(args[optind].equals("-L"))
					{
					app.genomeSize= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-X"))
					{
					app.recursionLevel= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-o"))
					{
					fileout=new File(args[++optind]);
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					}
				else 
					{
					break;
					}
				++optind;
				}
			if(app.recursionLevel<1 || app.recursionLevel>20)
				{
				System.err.println("Invalid recursion Level");
				return;
				}
			if(app.imageSize<1)
			{
			System.err.println("Invalid image size");
			return;
			}
			
			if(optind==args.length)
				{
				app.readGeneList(new BufferedReader(new InputStreamReader(System.in)));
				}
			else
				{
				while(optind< args.length)
					{
					BufferedReader r= IOUtils.openFile(new File(args[optind++]));
					app.readGeneList(r);
					r.close();
					}
				}
			
			PrintStream out= System.out;
			if(fileout!=null)
				{
				out=new PrintStream(fileout);
				}
			app.paint(out);
			if(fileout!=null)
				{
				out.flush();
				out.close();
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	
	}
