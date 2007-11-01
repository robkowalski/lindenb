/**
 * 
 */
package org.lindenb.ant;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.tools.ant.BuildException;


/**
 * @author pierre
 * &lt;taskdef name="mytask" classname="org.lindenb.ant.CompileInfoTask"/&gt;
 */
public class MakeIconTask extends  org.apache.tools.ant.Task
{

private String urlStr=null;
private File toFile=null;
private int size = 48;
private Rectangle viewRect=null;
private String formatName=null;

public MakeIconTask()
	{
	}




public void setUrl(String url)
	{
	this.urlStr=url;
	}

public void setFile(File toFile)
	{
	this.toFile=toFile;
	}

public void setSize(int size)
	{
	this.size=size;
	}

public void setFormat(String formatName)
	{
	this.formatName=formatName;
	}

public void setViewrect(String viewRect)
	{
	
	}

@Override
public void execute() throws BuildException
	{
	if(this.toFile==null)  throw new BuildException("Missing Destination File");
	if(this.toFile.exists()) return;
	if(this.urlStr==null) throw new BuildException("Missing URL");
	
	if(this.size==0) throw new BuildException("Icon size equals 0");
	
	URL url=null;
	try {
		url= new URL(this.urlStr);
		}
	catch(MalformedURLException e)
		{
		throw new BuildException(e);
		}
	
	try {
		BufferedImage img= ImageIO.read(url);
		if(img==null) throw new IOException("Cannot download "+url);
		Rectangle rect= this.viewRect;
		if(rect==null)
			{
			rect= new Rectangle(0,0,img.getWidth(),img.getHeight());
			}
		
		BufferedImage subImage = img.getSubimage(rect.x, rect.y, rect.width, rect.height);
		BufferedImage dest= new BufferedImage(this.size,this.size,img.getType());
		
		Graphics2D g= dest.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		
		int x0=0;
		int y0=0;
		int len=0;
		if(subImage.getWidth()< subImage.getHeight())
			{
			x0=0;
			len= subImage.getWidth();
			y0= (subImage.getHeight()-len)/2;
			}
		else
			{
			y0=0;
			len= subImage.getHeight();
			x0= (subImage.getWidth()-len)/2;
			}
		
		
		g.setColor(Color.WHITE);
		g.fillRect(0,0,this.size,this.size);
		g.drawImage(
				subImage,
				0,0,this.size,this.size,
				x0,y0,
				len,
				len,null
				);
		
		g.dispose();
		
		String fmt= this.formatName;
		if(fmt==null)
			{
			int i= this.toFile.getName().lastIndexOf(".");
			if(i!=-1)
				{
				String ext= this.toFile.getName().substring(i).toLowerCase();
				if( ext.equals(".jpeg") ||
					ext.equals(".jpg") ||
					ext.equals(".png") ||
					ext.equals(".gif"))
					{
					fmt=ext.substring(1);
					}
				}
			}
		if(fmt==null) fmt="png";
		
		ImageIO.write(dest, fmt, toFile);
		System.out.println(getTaskName()+" downloaded "+this.urlStr+" to "+this.toFile);
		}
	catch (Exception e)
		{
		throw new BuildException(e);
		}
	}
}
