package org.lindenb.tinytools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.FOAF;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.Compilation;
import org.lindenb.util.StringUtils;
import org.lindenb.xml.XMLUtilities;

/**
 * ImageTool
 * @author pierre
 *
 */
public class ImageTool
	{
	private abstract class ImageHandler
		{
		void startDocument() throws IOException{}
		abstract void image(URL url,Dimension dim) throws IOException;
		void endDocument() throws IOException{}
		}
	
	private class TextHandler extends ImageHandler
		{
		void startDocument() throws IOException
			{
			System.out.println("#url\twidth\theight");
			}
		@Override
		void image(URL url, Dimension dim) throws IOException
			{
			System.out.println(url.toString()+"\t"+dim.width+"\t"+dim.height);
			}
		}
	
	private class RDFHandler extends ImageHandler
		{
		void startDocument() throws IOException
			{
			System.out.println("<rdf:RDF xmlns:rdf='"+RDF.NS+"' " +
					"xmlns:dc='"+DC.NS+"' " +
					"xmlns:foaf='"+FOAF.NS+"' " +
					"xmlns:svg='"+SVG.NS+"#'>"
					);
			}
		@Override
		void endDocument() throws IOException
			{
			System.out.println("</rdf:RDF>");
			}
		@Override
		void image(URL url, Dimension dim) throws IOException
			{
			System.out.println("  <foaf:Image rdf:about='"+url+"'>");
			System.out.println("    <dc:title>"+XMLUtilities.escape(url.getFile().toString())+"</dc:title>");
			System.out.println("    <svg:width>"+dim.width+"</svg:width>");
			System.out.println("    <svg:height>"+dim.height+"</svg:height>");
			System.out.println("  </foaf:Image>");
			}
		}
	
	
	private Dimension getImageSize(URL url) throws IOException
		{
		ImageInputStream in = ImageIO.createImageInputStream(url.openStream());
		try {
		    Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
	        if(!readers.hasNext()) return null;
            ImageReader reader=readers.next();
            reader.setInput(in);
            return new Dimension(
            		reader.getWidth(0),
            		reader.getHeight(0)
            		);
	        	
			}
		catch (IOException e) {
			throw e;
			}
		finally {
		        if (in != null) in.close();
				}
		}
	
	
	private void run_sizes(String[] args,int optind) throws IOException
		{
		ImageHandler handler=new TextHandler();
		handler.startDocument();
		
		if(optind==args.length)
			{
			String line=null;
			BufferedReader r= new BufferedReader(new InputStreamReader(System.in));
			while((line=r.readLine())!=null)
				{
				if(line.startsWith("#") || StringUtils.isBlank(line)) continue;
				URL url=new URL(line);
				Dimension dim=getImageSize(url);
				handler.image(url,dim);
				}
			r.close();
			}
		else
			{
			while(optind<args.length)
				{
				URL url=new URL(args[optind++]);
				Dimension dim=getImageSize(url);
				handler.image(url,dim);
				}
			}
		handler.endDocument();
		}
	
	public static void main(String[] args)
		{
		ImageTool app=new ImageTool();
		String program=null;
		try
			{
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("-p program");
					System.err.println(" 'size' dump the sizes of the images ");
					System.err.println(" <stdin|urls>");
					return;
					}
				else if(args[optind].equals("-p"))
					{
					program=args[++optind];
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					return;
					}
				else 
					{
					break;
					}
				++optind;
				}
			if(program==null)
				{
				System.err.println("Program undefined");
				return;
				}
			else if(program.equals("size"))
				{
				app.run_sizes(args,optind);
				}
			else
				{
				System.err.println("unknown program:"+program);
				return;
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
