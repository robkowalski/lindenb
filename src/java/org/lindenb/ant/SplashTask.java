/**
 *  Create a quick Splash Screen for java executable jar
 *  See http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javase6/splashscreen/
 *  <taskdef name="makeIcon" classname="org.lindenb.ant.SplashTask" classpath="build/ant"/>
 *  <makeSplash title="Hello World !" file="task.jpeg"/>
 */
package org.lindenb.ant;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;

import org.apache.tools.ant.BuildException;


/**
 * @author Pierre Lindenbaum
 * Create a quick Splash Screen for java executable jar
 * 
 * Manifest-Version: 1.0
 * SplashScreen-Image: filename.gif
 * 
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javase6/splashscreen/
 */
public class SplashTask extends  org.apache.tools.ant.Task
{
private String title="Untitled";
private File toFile=null;
private String face="Helvetica";
private int size=48;
public SplashTask()
	{
	}

public void setTitle(String title) {
	this.title=title;
	}

public void setFace(String face) {
	this.face=face;
	}

public void setSize(int size) {
	this.size=size;
	}

public void setFile(File toFile)
	{
	this.toFile=toFile;
	}


@Override
public void execute() throws BuildException {

	if(this.toFile==null)  throw new BuildException("Missing Destination File");
	if(this.toFile.exists()) return;
	if(this.title==null) throw new BuildException("Missing Title");
	

	
	try {
		BufferedImage img= new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
		Graphics2D g= img.createGraphics();
		Font font= new Font(this.face,Font.BOLD,this.size);
		FontMetrics fm=g.getFontMetrics(font);
		
		
		Rectangle2D bounds =fm.getStringBounds(this.title, g);
		int margin= (int)(bounds.getWidth()/20.0);
		Rectangle r= new Rectangle(margin,margin,
				2*margin+(int)bounds.getWidth(),
				2*margin+(int)bounds.getHeight());
		g.dispose();
		
		
		img= new BufferedImage(r.width,r.height,BufferedImage.TYPE_INT_RGB);
		g= img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		Paint old= g.getPaint();
		g.setPaint(new GradientPaint(
				r.width/2-margin,-margin,Color.GRAY,
				r.width/2+margin,r.height+margin,Color.WHITE
				));

		g.fillRect(0, 0, r.width,r.height);
		g.setPaint(old);
		
		for(int h=2;h<r.width;h+=10)
			{
			for(int v=2;v<r.height;v+=10)
				{
				g.setColor(Color.WHITE);
				g.fillRect(h, v, 2, 2);
				g.setColor(Color.GRAY);
				g.fillRect(h, v, 1, 1);
				}
			}
		
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, r.width-1,r.height-1);
		g.setFont(font);
		
		g.drawString(this.title, margin, r.height-(margin+fm.getDescent()));
		g.dispose();
		
		String formatName="jpg";
		int i= this.toFile.getName().lastIndexOf(".");
		if(i!=-1)
			{
			String ext= this.toFile.getName().substring(i).toLowerCase();
			if( ext.equals(".jpeg") ||
				ext.equals(".jpg") ||
				ext.equals(".png") ||
				ext.equals(".gif"))
				{
				formatName=ext.substring(1);
				}
			}
		
		ImageIO.write(img, formatName, this.toFile);
		System.out.println("Saved SplashScreen \""+this.title+"\" to "+
				this.toFile+"["+r.width+" x "+r.height+"]");
		}
	catch (IOException e)
		{
		throw new BuildException(e);
		}
	}
}
