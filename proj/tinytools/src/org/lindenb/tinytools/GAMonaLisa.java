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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lindenb.util.Compilation;


public class GAMonaLisa
	{
	private BufferedImage imageSrc;
	private Random rand= new Random(System.currentTimeMillis());
	private int maxTrianglePerSolution=50;
	
	private class Triangle
		{
		private Color color; 
		private float alpha=1f;
		private int x[]=new int[3];
		private int y[]=new int[3];
		public Triangle()
			{
			this.color= randColor();
			this.alpha= rand.nextFloat();
			int halfw= imageSrc.getWidth()/2;
			int halfh= imageSrc.getHeight()/2;
			
			this.x[0]= rand.nextInt(imageSrc.getWidth());
			this.y[0]= rand.nextInt(imageSrc.getHeight());
			
			this.x[1]= halfw + sign() * rand.nextInt(imageSrc.getWidth());
			this.y[1]= halfh + sign() * rand.nextInt(imageSrc.getHeight());
			
			this.x[2]= halfw + sign() * rand.nextInt(imageSrc.getWidth());
			this.y[2]= halfh + sign() * rand.nextInt(imageSrc.getHeight());
			}
		
		public Triangle(Triangle cp)
			{
			this.color=cp.color;
			this.alpha=cp.alpha;
			System.arraycopy(cp.x, 0, this.x, 0, 3);
			System.arraycopy(cp.y, 0, this.y, 0, 3);
			}
		
		
		
		
		public void mute()
			{
			int i= rand.nextInt(3);
			if(sign()==1)
				{
				this.x[i]+= 10*sign();
				}
			else
				{
				this.y[i]+= 10*sign();
				}
			if(rand.nextInt(100)<33)
				{
				int dc= sign()*5;
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
				}
			if(rand.nextInt(100)<33)
				{
				float da= sign()*rand.nextFloat()*0.05f;
				if(this.alpha+da>=0f && this.alpha+da<=1f)
					{
					this.alpha+=da;
					}
				
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
			int n=1+rand.nextInt(maxTrianglePerSolution-1);
			while(n>0)
				{
				--n;
				this.items.add(new Triangle());
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
		
		
		private void mute()
			{
			if(this.items.size()>1 &&
				rand.nextInt(100)<20)
				{
				int n= rand.nextInt(this.items.size());
				this.items.remove(n);
				}
			if(this.items.size()+1< maxTrianglePerSolution &&
				rand.nextInt(100)<20)
				{
				int n= rand.nextInt(this.items.size());
				this.items.add(n, new Triangle());
				}
			for(Triangle t:this.items)
				{
				if(rand.nextInt(100)<20)
					{
					t.mute();
					}
				}
			}
		
		public BufferedImage createImage()
			{
			BufferedImage img= new BufferedImage(imageSrc.getWidth(),imageSrc.getHeight(),imageSrc.getType());
			Graphics2D g=img.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			Composite comp= g.getComposite();
			for(Triangle t:this.items)
				{
				g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, t.alpha));
				g.setColor(t.color);
				g.fill(t.getShape());
				}
			g.setComposite(comp);
			g.dispose();
			return img;
			}
		
		public Long getFitness()
			{
			if(fitness!=null) return fitness;
			BufferedImage img = createImage();
			this.fitness=0L;
			for(int x=0;x<img.getWidth();++x)
				{
				for(int y=0;y<img.getHeight();++y)
					{
					Color c1= new Color(imageSrc.getRGB(x,y));
					Color c2= new Color(img.getRGB(x,y));
					this.fitness+= Math.abs(c1.getRed()-c2.getRed());
					this.fitness+= Math.abs(c1.getGreen()-c2.getGreen());
					this.fitness+= Math.abs(c1.getBlue()-c2.getBlue());
					}
				}
			img=null;
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
	
	private Solution[] cross(Solution father,Solution mother)
		{
		int min= Math.min(father.items.size(), mother.items.size());
		int index= rand.nextInt(min);
		List<Triangle> items1=new ArrayList<Triangle>();
		List<Triangle> items2=new ArrayList<Triangle>();
		
		for(int i=0;i< min;++i)
			{
			if(i<index)
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
		
		for(int generation=0;generation<10000;generation++)
			{
			System.err.println("g="+generation);
			List<Solution> children=new ArrayList<Solution>();
			while(this.population.size()<30)
				{
				this.population.add(new Solution());
				}
			for(int x=0;x+1< this.population.size();++x)
				{
				for(int y=x+1;y< this.population.size();++y)
					{
					Solution sol[]= cross(
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
			
			while(children.size()>15)
				{
				children.remove(children.size()-1);
				}
			this.population=children;
			if(generation%100==0)
				{
				try
					{
					ImageIO.write(this.population.get(0).createImage(), "png", new File("img"+generation+".png"));
					}
				catch(IOException err)
					{
					err.printStackTrace();
					}
				}
			
			}
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
					}
				else if(args[optind].equals("-i"))
					{
					InputStream in=null;
					String uri=args[++optind];
					if(uri.startsWith("http://") || uri.startsWith("https://"))
						{
						in= new URL(uri).openStream();
						}
					else
						{
						in= new FileInputStream(uri);
						}
					app.imageSrc= ImageIO.read(in);
					in.close();
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
			if(optind!=args.length)
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
