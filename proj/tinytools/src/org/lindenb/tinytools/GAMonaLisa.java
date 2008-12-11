/*
 * 
 * Pierre Lindenbaum PhD plindenbaum@yahoo.fr
 *  to create images inspired by Roger Alsing's blog:
 * http://rogeralsing.com/2008/12/07/genetic-programming-evolution-of-mona-lisa/
 */
package org.lindenb.tinytools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.lindenb.awt.ColorUtils;
import org.lindenb.me.Me;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;


public class GAMonaLisa
	{
	private BufferedImage imageSrc;
	private BufferedImage currentImage;
	private Random rand= new Random(System.currentTimeMillis());
	private int maxTrianglePerSolution=250;
	private int minTrianglePerSolution=50;
	private int generation=0;
	private int max_generation=-1;
	private File outDir=null;
	private boolean exportSVG=false;
	private int numberOfIndividualSurviving=10;
	private int initialPopulationSize=40;
	private long minDiffSaveMillisec=0L;
	private int maxThreadCount=4;
	
	
	
	private class FitnessThread
		extends Thread
		{
		int rowStart;
		int rowEnd;
		long fitness=0L;
		
		FitnessThread(int rowStart,int rowEnd)
			{
			this.rowStart=rowStart;
			this.rowEnd=rowEnd;
			}
		
		@Override
		public void run()
			{
			fitness=0L;
			for(int y=this.rowStart;y<this.rowEnd;++y)
				{
				for(int x=0;x<imageSrc.getWidth();++x)
					{
					Color c1= new Color(imageSrc.getRGB(x,y));
					Color c2= new Color(currentImage.getRGB(x,y));
					this.fitness+= Math.abs(c1.getRed()-c2.getRed());
					this.fitness+= Math.abs(c1.getGreen()-c2.getGreen());
					this.fitness+= Math.abs(c1.getBlue()-c2.getBlue());
					}
				}
			}
		
		}
	
	
	
	private class Triangle
		{
		private Color color; 
		private float alpha=1f;
		private int x[]=new int[3];
		private int y[]=new int[3];
		public Triangle()
			{
			this.color= randColor();
			this.alpha= 0.5f+rand.nextFloat()*0.5f;
			
			
			this.x[0]= rand.nextInt(imageSrc.getWidth());
			this.y[0]= rand.nextInt(imageSrc.getHeight());
			
			if(rand.nextInt(100)<40)//create small figure sometimes
				{
				int max=10+ rand.nextInt(Math.min(imageSrc.getWidth(), imageSrc.getHeight())/50);
				this.x[1]= this.x[0] + sign() * rand.nextInt(max);
				this.y[1]= this.x[0] + sign() * rand.nextInt(max);
				
				this.x[2]= this.x[0] + sign() * rand.nextInt(max);
				this.y[2]= this.x[0] + sign() * rand.nextInt(max);
				}
			else
				{
				int halfw= imageSrc.getWidth()/2;
				int halfh= imageSrc.getHeight()/2;
				
				this.x[1]= halfw + sign() * rand.nextInt(imageSrc.getWidth());
				this.y[1]= halfh + sign() * rand.nextInt(imageSrc.getHeight());
				
				this.x[2]= halfw + sign() * rand.nextInt(imageSrc.getWidth());
				this.y[2]= halfh + sign() * rand.nextInt(imageSrc.getHeight());
				}
			}
		
		public Triangle(String line)
			{
			String token[]=line.split("[ ]");
			for( int i=0;i< 3;++i)
				{
				int j= token[i].indexOf(',');
				x[i]= Integer.parseInt( token[i].substring(0, j));
				y[i]= Integer.parseInt( token[i].substring(j+1));
				}
			this.color= ColorUtils.parseColor(token[3]);
			this.alpha= Float.parseFloat(token[4])/10000f;
			}
		
		public Triangle(Triangle cp)
			{
			this.color=cp.color;
			this.alpha=cp.alpha;
			System.arraycopy(cp.x, 0, this.x, 0, 3);
			System.arraycopy(cp.y, 0, this.y, 0, 3);
			}
		
		private void toSVG(PrintStream out)
			{
			out.print("<svg:polygon style='fill:"+ColorUtils.toRGB(this.color)+";fill-opacity:"+ this.alpha+";' points='"+
					x[0]+","+y[0]+" "+
					x[1]+","+y[1]+" "+
					x[2]+","+y[2]+"'/>"
				);
			}
		
		private void toText(PrintStream out)
			{
			out.println(x[0]+","+y[0]+" "+
					x[1]+","+y[1]+" "+
					x[2]+","+y[2]+" "+
					ColorUtils.toRGB(this.color)+" "+
					(int)(this.alpha*10000f)
					);
			}
		
		public void mute()
			{
			int choice=rand.nextInt(5);
			switch(choice)
				{
				case 0: break;//no mutation
				case 1:
					{
					int i= rand.nextInt(3);
					int len= 1+rand.nextInt(20);
					this.x[i]+= rand.nextInt(len)*sign();
					this.y[i]+= rand.nextInt(len)*sign();
					break;
					}
				case 2:
					{
					int dc= sign()*(1+rand.nextInt(5));
					int r= this.color.getRed();
					int g= this.color.getGreen();
					int b= this.color.getBlue();
					switch(rand.nextInt(3))
						{
						case 0: if(r+dc>=0 && r+dc<=255) r+=dc; break;
						case 1: if(g+dc>=0 && g+dc<=255) g+=dc; break;
						case 2: if(b+dc>=0 && b+dc<=255) b+=dc; break;
						}
					this.color= new Color(r,g,b);
					break;
					}
				case 3:
					{
					float da= sign()*rand.nextFloat()*0.8f;
					if(this.alpha+da>=0f && this.alpha+da<=1f)
						{
						this.alpha+=da;
						}
					break;
					}
				case 4:
					{
					Rectangle2D p=getShape().getBounds2D();
					double cx=(int)p.getCenterX();
					double cy=(int)p.getCenterY();
					AffineTransform tr= AffineTransform.getTranslateInstance(-cx, -cy);
					tr.rotate(rand.nextDouble()*0.1);
					tr.translate(cx, cy);
					Point2D.Double ptDst= new Point2D.Double();
					for(int i=0;i< 3;++i)
						{
						tr.transform(new Point2D.Double(x[i],y[i]), ptDst);
						x[i]=(int)ptDst.getX();
						y[i]=(int)ptDst.getY();
						}
					break;
					}
				default:break;
				}
			}	
		
		public Polygon getShape()
			{
			return new Polygon(x,y,3);
			}
		}
	
	private class Solution
		implements Comparable<Solution>
		{
		private Long fitness=null;
		private List<Triangle> items= new ArrayList<Triangle>();
		Solution()
			{
			for(int i=0;i< minTrianglePerSolution;++i)
				{
				this.items.add(new Triangle());
				}
			
			if(minTrianglePerSolution< maxTrianglePerSolution)
				{
				int n=1+rand.nextInt((maxTrianglePerSolution-minTrianglePerSolution)-1);
				while(n>0)
					{
					--n;
					this.items.add(new Triangle());
					}
				}
			}
		
		Solution(Solution cp)
			{
			for(Triangle item:cp.items)
				{
				this.items.add(new Triangle(item));
				}
			}
		
		public Solution(List<Triangle> items)
			{
			this.items.addAll(items);
			}
		
		private void toSVG(PrintStream out)
			{
			out.println(SVG.DOCTYPE);
			out.println("<svg:svg xmlns:svg='"+ SVG.NS+ "' version='1.1' width='"+imageSrc.getWidth()+"' height='"+imageSrc.getHeight()+"' style='stroke:none;'>");
			out.print("<svg:title>Generation "+generation+" fitness:"+getFitness()+"</svg:title>");
			out.print("<svg:rect x='0' y='0' style='fill:white;' width='"+(imageSrc.getWidth()-1)+"' height='"+(imageSrc.getHeight()-1)+"' />");
			for(Triangle t:this.items) t.toSVG(out);
			out.print("<svg:rect x='0' y='0' style='fill:none;stroke:balck;' width='"+(imageSrc.getWidth()-1)+"' height='"+(imageSrc.getHeight()-1)+"' />");
			out.print("</svg:svg>");
			out.flush();
			}
		
		private void toText(PrintStream out)
			{
			out.println("#"+generation+","+getFitness());
			for(Triangle t:this.items) t.toText(out);
			out.flush();
			}
		
		private void mute()
			{
			int choice= rand.nextInt(7);
			switch(choice)
				{
				case 0:
					{
					Collections.shuffle(this.items);
					break;
					}
				case 1:
					{
					if(this.items.size()<2) break;	
					int n= rand.nextInt(this.items.size());
					this.items.remove(n);
					break;
					}
				case 2:
						{
						if(this.items.size()+1>= maxTrianglePerSolution) break;
						
						int n= rand.nextInt(this.items.size());
						Triangle t=null;
						if(rand.nextInt(100)<50)
							{
							t=new Triangle();
							}
						else
							{
							t= new Triangle(this.items.get(n));
							t.mute();
							}
						
						this.items.add(n,t);
						break;
						}
				case 3:
					{
					int n1= rand.nextInt(this.items.size());
					int n2= rand.nextInt(this.items.size());
					
					Triangle t1= this.items.get(n1);
					Triangle t2= this.items.get(n2);
					this.items.set(n1, t2);
					this.items.set(n2, t1);
					break;
					}
				case 4:case 5:case 6:
					{
					for(Triangle t:this.items)
						{
						if(rand.nextInt(100)<20)
							{
							t.mute();
							}
						}
					break;
					}
				}
			
			}
		
		public BufferedImage createImage()
			{
			if(currentImage==null)
				{
				currentImage= new BufferedImage(imageSrc.getWidth(),imageSrc.getHeight(),imageSrc.getType());
				}
			
			Graphics2D g=currentImage.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
			Composite comp= g.getComposite();
			for(Triangle t:this.items)
				{
				g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, t.alpha));
				g.setColor(t.color);
				g.fill(t.getShape());
				}
			g.setComposite(comp);
			g.dispose();
			return currentImage;
			}
		
		public Long getFitness()
			{
			if(fitness!=null) return fitness;
			createImage();
			this.fitness=0L;
			
			List<FitnessThread> fitnessThreads=new ArrayList<FitnessThread>(maxThreadCount);
			int y0=0;
			int dY=  imageSrc.getHeight()/maxThreadCount;
			if(dY==0) dY=1;
			
			while(true)
				{
				int rowEnd= Math.min(y0+dY,imageSrc.getHeight());
				fitnessThreads.add(new FitnessThread
					(
					y0,rowEnd
					));
				if( rowEnd >= imageSrc.getHeight() ) break;
				y0 = rowEnd;
				}
			
			
			for(FitnessThread thread: fitnessThreads)
				{
				thread.start();
				}
			
			for(FitnessThread thread: fitnessThreads)
				{
				
				try {
					thread.join();
					
					this.fitness+= thread.fitness;
					}
				catch (InterruptedException e)
					{
					throw new RuntimeException(e);
					}
				}
			
			return fitness;
			}
		
		@Override
		public int compareTo(Solution cp)
			{
			return getFitness().compareTo(cp.getFitness());
			}
		}
	
	private List<Solution> population=new ArrayList<Solution>();
	
	private GAMonaLisa()
		{
		}
	
	private int sign()
		{
		return rand.nextInt(100)<50?-1:1;
		}
	
	private Color randColor()
		{
		return new Color(
			rand.nextInt(255),
			rand.nextInt(255),
			rand.nextInt(255)
			);
		}
	
	private Solution[] makeLove(Solution father,Solution mother)
		{
		int min= Math.min(father.items.size(), mother.items.size());
		List<Triangle> items1=new ArrayList<Triangle>(father.items.size());
		List<Triangle> items2=new ArrayList<Triangle>(mother.items.size());
		//make crossing overs
		for(int i=0;i< min;++i)
			{
			if(rand.nextInt(100)<50)
				{
				items1.add(new Triangle(father.items.get(i)));
				items2.add(new Triangle(mother.items.get(i)));
				}
			else
				{
				items1.add(new Triangle(mother.items.get(i)));
				items2.add(new Triangle(father.items.get(i)));
				}
			}
		
		while(items1.size()< father.items.size())
			{
			items1.add(new Triangle(father.items.get(items1.size())));
			}
		
		while(items2.size()< mother.items.size())
			{
			items2.add(new Triangle(mother.items.get(items2.size())));
			}
		
		return new Solution[]{
			new Solution(items1),
			new Solution(items2),
			};
		}
	
	void run()
		{
		long lastSave=0L;
		
		
		
		
		while(true)
			{
			long now=System.currentTimeMillis();
			System.out.print("Generation: "+this.generation);
			List<Solution> children=new ArrayList<Solution>();
			while(this.population.size()<initialPopulationSize)
				{
				this.population.add(new Solution());
				}
			for(int x=0;x< this.population.size();++x)
				{
				for(int y=x;y< this.population.size();++y)
					{
					Solution sol[]= makeLove(
						this.population.get(x),
						this.population.get(y)
						);
					children.add(sol[0]);
					children.add(sol[1]);
					}
				}
			for(Solution sol:children)
				{
				sol.mute();
				}
			for(int i=0;i< 10;++i) children.add(new Solution());
			Collections.sort(children);
			
			while(children.size()>numberOfIndividualSurviving)
				{
				children.remove(children.size()-1);
				}
			
			if( this.population.get(0).getFitness() < children.get(0).getFitness())
				{
				children.add(0, this.population.get(0));
				}
			else 
				{
				System.out.print("\tFitness:"+children.get(0).getFitness());
				System.out.flush();
				if(System.currentTimeMillis() - lastSave > this.minDiffSaveMillisec)
					{
					Formatter formatter = new Formatter();
					formatter.format("%05d", this.generation);
					String prefix= formatter.out().toString();
					System.out.println("\tsaving to "+prefix);
					try
						{
						File file=new File(this.outDir,"img"+ prefix+".png");
						ImageIO.write(this.population.get(0).createImage(), "png", file);
						PrintStream out=null;
						if(exportSVG) 
							{
							file=new File(this.outDir,"img"+ prefix+".svgz");
							out= new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));
							children.get(0).toSVG(out);
							out.flush();
							out.close();
							}
						file=new File(this.outDir,"img"+ prefix+".txt");
						out= new PrintStream(new FileOutputStream(file));
						children.get(0).toText(out);
						out.flush();
						out.close();
						}
					catch(IOException err)
						{
						err.printStackTrace();
						}
					lastSave= System.currentTimeMillis();
					}
				}
			this.population=children;
			this.generation++;
			System.out.println(" "+(System.currentTimeMillis()-now)/1000+" seconds.");
			if(this.max_generation!=-1 && this.max_generation<=this.generation) break;
			}
		}
	
	private void preReadSolution(File f) throws IOException
		{
		List<Triangle> items= new ArrayList<Triangle>();
		BufferedReader r= new BufferedReader(new FileReader(f));
		String line;
		while((line=r.readLine())!=null)
			{
			if(line.startsWith("#"))
				{
				int j=line.indexOf(",");
				if(j>0) this.generation=Math.max(this.generation, 1+Integer.parseInt(line.substring(1,j)));
				continue;
				}
			if(line.trim().length()==0) continue;
			items.add(new Triangle(line));
			}
		r.close();
		this.population.add(new Solution(items));
		}
	
	private void loadImage(String uri) throws IOException
		{
		InputStream in=null;
		if(Cast.URL.isA(uri))
			{
			in= Cast.URL.cast(uri).openStream();
			}
		else
			{
			in= new FileInputStream(uri);
			}
		this.imageSrc= ImageIO.read(in);
		in.close();
		if(this.imageSrc==null) throw new IOException("Cannot read "+uri);
		}
	
	public static void main(String[] args)
		{
		try
			{
			int optind=0;
			GAMonaLisa app= new GAMonaLisa();
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Pierre Lindenbaum. "+Me.MAIL+" "+Me.WWW);
					System.err.println("Inspired by Roger Alsing's blog: http://rogeralsing.com/2008/12/07/genetic-programming-evolution-of-mona-lisa/ ");
					System.err.println(" -i input image (file|url) <required>");
					System.err.println(" -d output directory");
					System.err.println(" -r read previous chilren <*.txt>");
					System.err.println(" -s export SVG");
					System.err.println(" -n1 numberOfIndividualSurviving="+app.numberOfIndividualSurviving);
					System.err.println(" -n2 initialPopulationSize="+app.initialPopulationSize);
					System.err.println(" -n3 max_generation="+app.max_generation);
					System.err.println(" -n4 max Diff time Saving Millisec="+app.minDiffSaveMillisec);
					System.err.println(" -n5 max Triangle per solution ="+app.maxTrianglePerSolution);
					System.err.println(" -n6 min Triangle per solution ="+app.minTrianglePerSolution);
					System.err.println(" -n7 min Thread count ="+app.maxThreadCount);
					return;
					}
				else if(args[optind].equals("-i"))
					{
					app.loadImage(args[++optind]);
					}
				else if(args[optind].equals("-n1"))
					{
					app.numberOfIndividualSurviving = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-n2"))
					{
					app.initialPopulationSize = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-n3"))
					{
					app.max_generation = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-n4"))
					{
					app.minDiffSaveMillisec = Long.parseLong(args[++optind]);
					}
				else if(args[optind].equals("-n5"))
					{
					app.maxTrianglePerSolution = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-n6"))
					{
					app.minTrianglePerSolution = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-n7"))
					{
					app.maxThreadCount = Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-s"))
					{
					app.exportSVG=true;
					}
				else if(args[optind].equals("-d"))
					{
					app.outDir= new File(args[++optind]);
					}
				else if(args[optind].equals("-r"))
					{
					app.preReadSolution(new File(args[++optind]));
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
			if(optind+1==args.length && app.imageSrc==null)
				{
				app.loadImage(args[optind]);
				}
			else if(optind!=args.length)
				{
				System.err.println("Illegal number of arguments");
				return;
				}
			if(app.imageSrc==null)
				{
				System.err.println("Image source not defined");
				return;
				}
			app.run();
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
