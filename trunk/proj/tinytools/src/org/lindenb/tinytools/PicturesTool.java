package org.lindenb.tinytools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;


import javax.imageio.ImageIO;

import org.lindenb.awt.ColorUtils;
import org.lindenb.util.Compilation;

/**
 * PicturesTool
 *
 */
public class PicturesTool
	{
	/**
	 * 
	 * Picture
	 *
	 */
	private class Picture
		{
		URL url;
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
				double ratioW= (float)(PicturesTool.this.destWidth)/(float)this.img.getWidth();
				double ratioH= (float)(PicturesTool.this.destHeight)/(float)this.img.getHeight();
				double ratio=Math.min(ratioW, ratioH);
				double newWidth=((double)this.img.getWidth()*ratio);
				double newHeight=((double)this.img.getHeight()*ratio);
				int marginW= (int)((PicturesTool.this.destWidth-newWidth)/2.0);
				int marginH= (int)((PicturesTool.this.destHeight-newHeight)/2.0);
				
				
				BufferedImage copy = new BufferedImage(
						PicturesTool.this.destWidth,
						PicturesTool.this.destHeight,
						this.img.getType()
						);
				Graphics2D g= initGraphics(copy.createGraphics());
				g.setColor(PicturesTool.this.backgroundColor);
				g.fillRect(0, 0,
						copy.getWidth(),
						copy.getHeight()
						);
				g.drawImage(this.img,
						marginW,marginH,
						PicturesTool.this.destWidth-marginW,
						PicturesTool.this.destHeight-marginH,
						0,0,
						this.img.getWidth(),
						this.img.getHeight(),
						null);
				
				
				g.dispose();
				this.img=copy;
				}
			
			if(PicturesTool.this.frame && 1==2)
				{
				int frame=border();
				
				BufferedImage copy = new BufferedImage(
						PicturesTool.this.destWidth+2*frame,
						PicturesTool.this.destHeight+2*frame,
						this.img.getType()
						);
				Graphics2D g= initGraphics(copy.createGraphics());
				g.setColor(PicturesTool.this.backgroundColor);
				g.fillRect(0, 0,
						copy.getWidth(),
						copy.getHeight()
						);
				g.drawImage(this.img,
						frame,
						frame,
						null);
				g.dispose();
				this.img=copy;
				}
			
			return this.img;
			}
		}
	
	private Color backgroundColor=Color.WHITE;
	private String prefix="_";
	private File outputDir=null;
	private int pictPerImage=1;
	private Stack<Picture> pictures= new Stack<Picture>();
	private int destWidth=1024;
	private int destHeight=768;
	private boolean frame=false;
	private boolean ignore_errors=false;
	
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
	
	private int border()
		{
		return (int)(0.05f*Math.min(
				PicturesTool.this.destWidth,
				PicturesTool.this.destHeight
				));
		}
	
	private void run() throws IOException
		{
		int counter=0;
		int x=1;
		int y=1;
		int frame= (this.frame?border():0);
		
		if(this.pictPerImage==4)
			{
			x=2; y=2;
			}
		while(!this.pictures.isEmpty())
			{
			BufferedImage img=null;
			++counter;
			for(int x1=0; x1< x;++x1)
				{
				for(int y1=0; y1< y;++y1)
					{
					if(this.pictures.isEmpty()) break;
					Picture p= this.pictures.pop();
					try
						{
						p.prepareImage();
						}
					catch(IOException err)
						{
						if(this.ignore_errors)
							{
							System.err.println("Error with "+p.url+" "+err.getMessage());
							continue;
							}
						throw err;
						}
					if(img==null)
						{
						img = new BufferedImage(
							(x*(this.destWidth+2*frame)),
							(y*(this.destHeight+2*frame)),
							p.img.getType()	
							);
						}
					Graphics2D g= initGraphics(img.createGraphics());
					g.drawImage(
						p.img,
						frame+(x1*(this.destWidth+2*frame)),
						frame+(y1*(this.destHeight+2*frame)),
						null);
					g.dispose();
					}
				}
			
			if(img!=null)
				{
				File out= new File(this.outputDir,this.prefix+counter+".jpeg");
				System.err.println("Writing to "+out);
				ImageIO.write(img, "jpeg", out);
				img=null;
				}
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
					System.err.println("-W image destination height (default: "+app.destWidth +")");
					System.err.println("-H image destination height (default: "+app.destHeight +")");
					System.err.println("-p image prefix (default: \'"+app.prefix+"\')");
					System.err.println("-f add a frame");
					System.err.println("-4 4 images per picture");
					System.err.println("-d output directory");
					System.err.println("-i <file> use this file as a source of url");
					System.err.println("-e ignore errors");
					return;
					}
				else if(args[optind].equals("-c"))
					{
					app.backgroundColor= ColorUtils.parseColor(args[++optind]);
					}
				else if(args[optind].equals("-e"))
					{
					app.ignore_errors=true;
					}
				else if(args[optind].equals("-p"))
					{
					app.prefix= args[++optind];
					}
				else if(args[optind].equals("-f"))
					{
					app.frame=true;
					}
				 else if(args[optind].equals("-4"))
					{
					app.pictPerImage=4;
					}
				else if(args[optind].equals("-W"))
					{
					app.destWidth= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-H"))
					{
					app.destHeight= Integer.parseInt(args[++optind]);
					}
				else if(args[optind].equals("-d"))
					{
					app.outputDir= new File(args[++optind]);
					}
				else if(args[optind].equals("-i"))
					{
					BufferedReader r= new BufferedReader(new FileReader(args[++optind]));
					String line;
					while((line=r.readLine())!=null)
						{
						if(line.startsWith("#")) continue;
						line=line.trim();
						if(line.length()==0) continue;
						app.add(line);
						}
					r.close();
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
			
			if(  args.length==optind &&
				 app.pictures.isEmpty())
				{
				System.err.println("No Input");
				return;
				}
			
			if(app.destWidth<app.destHeight)
				{
				int t= app.destWidth;
				app.destWidth=app.destHeight;
				app.destHeight=t;
				}
			
			while(optind< args.length)
				{
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
