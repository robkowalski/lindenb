package org.lindenb.tinytools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;


import javax.imageio.ImageIO;

import org.lindenb.awt.ColorUtils;
import org.lindenb.util.Compilation;

public class PicturesTool
	{
	private class Picture
		{
		URL url;
		Dimension dimension;
		BufferedImage img;
		Picture(String source) throws IOException
			{
			try {
				this.url= new URL(source);
				} 
			catch (MalformedURLException err)
				{
				this.url= new File(source).toURI().toURL();
				}
			}
		
		BufferedImage prepareImage() throws IOException
			{
			this.img=ImageIO.read(this.url);
			this.dimension= new Dimension(img.getWidth(),img.getHeight());
			//set good orientation
			if(this.img.getWidth()<this.img.getHeight())
				{
				int width = this.img.getWidth();
				int height = this.img.getHeight();

				BufferedImage copy = new BufferedImage(
						height,
						width,
						this.img.getType()
						);
				for(int i=0; i<width; i++)
					{
					for(int j=0; j<height; j++)
						{
						copy.setRGB(
								height-1-j,
								width-1-i,
								this.img.getRGB(i, j)
								);
						}
					}
				this.img=copy;
				}
			
			//set good size
			if(this.img.getWidth()!=PicturesTool.this.destWidth)
				{
				
				float ratio= (float)PicturesTool.this.destWidth/(float)this.img.getWidth();
				int newHeight= (int)((float)this.img.getHeight()*ratio);
				BufferedImage copy = new BufferedImage(
						PicturesTool.this.destWidth,
						newHeight,
						this.img.getType()
						);
				Graphics2D g= initGraphics(copy.createGraphics());
				
				g.drawImage(this.img,
						0,0,
						copy.getWidth(),copy.getHeight(),
						0,0,
						this.img.getWidth(),this.img.getHeight(),
						null);
				g.dispose();
				this.img=copy;
				}
			
			int expectedWidth= PicturesTool.this.destWidth;
			int expectedHeight= (int)((double)expectedWidth/PicturesTool.this.forceRatio);
			//set good ratio
			if(this.img.getHeight()<=expectedHeight)
				{
				int marginH=(expectedHeight-this.img.getHeight())/2;
				System.err.println(marginH);
				System.err.println(expectedHeight);
				BufferedImage copy = new BufferedImage(
						this.img.getWidth(),
						expectedHeight,
						this.img.getType()
						);
				Graphics2D g= initGraphics(copy.createGraphics());
				g.setColor(PicturesTool.this.backgroundColor);
				g.fillRect(0, 0, this.img.getWidth(), expectedHeight);
				g.drawImage(this.img, 0, marginH, null);
				g.dispose();
				this.img=copy;
				}
			else if(this.img.getHeight()>expectedHeight)
				{
				System.err.println("r2");
				float ratio= (float)expectedHeight/(float)this.img.getHeight();
				int newWidth=(int) (this.img.getWidth()*ratio);
				int margin=(this.img.getWidth()-newWidth)/2;
				BufferedImage copy = new BufferedImage(
						this.img.getWidth(),
						expectedHeight,
						this.img.getType()
						);
				Graphics2D g= initGraphics(copy.createGraphics());
				g.setColor(PicturesTool.this.backgroundColor);
				g.fillRect(0, 0, this.img.getWidth(), expectedHeight);
				g.drawImage(this.img,
						margin, 0,
						this.img.getWidth()-margin, this.img.getHeight(),
						0,0,this.img.getWidth(), this.img.getHeight(),
						null);
				g.dispose();
				this.img=copy;
				}
			
			
			return this.img;
			}
		}
	
	private Color backgroundColor=Color.WHITE;
	private double forceRatio=5.0/3.0;
	private String prefix="_";
	private File outputDir=null;
	private int pictPerImage=1;
	private Stack<Picture> pictures= new Stack<Picture>();
	private int destWidth=1024;
	
	private static Graphics2D initGraphics(Graphics2D g)
		{
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		return g;
		}
	
	private Picture add(String source) throws IOException
		{
		Picture p= new Picture(source);
		pictures.add(p);
		return p;
		}
	
	private void run() throws IOException
		{
		int counter=0;
		while(!this.pictures.isEmpty())
			{
			++counter;
			Picture p= this.pictures.pop();
			File out= new File(this.outputDir,this.prefix+counter+".jpeg");
			System.err.println("Writing to "+out);
			ImageIO.write(p.prepareImage(), "jpeg", out);
			
			}
		}
	
	public static void main(String[] args) {
		try
			{
			PicturesTool app= new PicturesTool();
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("-c background color (default: white)");
					System.err.println("-r h/w ratio (default: 1.3333)");
					System.err.println("-p image prefix (default: \'_\')");
					System.err.println("-f add a frame");
					System.err.println("-4 4 images per picture");
					System.err.println("-d output directory");
					System.err.println("-w width default:1024");
					}
				else if(args[optind].equals("-c"))
					{
					app.backgroundColor= ColorUtils.parseColor(args[++optind]);
					}
				else if(args[optind].equals("-p"))
					{
					app.prefix= args[++optind];
					}
				else if(args[optind].equals("-4"))
					{
					app.pictPerImage=4;
					}
				else if(args[optind].equals("-w"))
					{
					app.destWidth= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-d"))
					{
					app.outputDir= new File(args[++optind]);
					}
				else if(args[optind].equals("-r"))
					{
					app.forceRatio= Double.parseDouble(args[++optind]);
					if(app.forceRatio<1)
						{
						throw new IllegalArgumentException("Illegal Ratio <1");
						}
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
			
			if(args.length==optind)
				{
				System.err.println("No Input");
				}
			
			while(optind< args.length)
				{
				System.out.println("Adding "+args[optind]);
				app.add(args[optind]);
				++optind;
				}
			app.run();
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
