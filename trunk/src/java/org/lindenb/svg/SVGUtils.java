package org.lindenb.svg;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.lindenb.awt.Dimension2D;
import org.lindenb.lang.InvalidXMLException;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.StringUtils;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;


/**
 * Utilities for Scalable Vector Graphics
 * @author lindenb
 *
 */
public class SVGUtils extends SVG
{
	protected static final Logger LOG=Logger.getLogger(SVGUtils.class.getName()); 
	
	public static double castUnit(String s)
		{
		s=s.trim();
		if(s.endsWith("px"))
			{
			return Double.parseDouble(s.substring(0,s.length()-2).trim());
			}
		else if(s.endsWith("cm"))
			{
			return Double.parseDouble(s.substring(0,s.length()-2).trim())*100;
			}
		else
			{
			return Double.parseDouble(s);
			}
		}
	
	/** return the dimension of a SVG document */
	public static Dimension2D getSize(Element svgRoot)throws InvalidXMLException
		{
		if(!XMLUtilities.isA(svgRoot, NS, "svg")) throw new InvalidXMLException(svgRoot,"not a svg:svg element");
		try
			{
			Dimension2D.Double srcSize=new Dimension2D.Double(0,0);
			Attr att= svgRoot.getAttributeNode("width");
			if(att==null) throw new InvalidXMLException(svgRoot,"@width missing");
			srcSize.width= castUnit(att.getValue());
			att= svgRoot.getAttributeNode("height");
			if(att==null) throw new InvalidXMLException(svgRoot,"@height missing");
			srcSize.height= castUnit(att.getValue());
			return srcSize;
			}
		catch(NumberFormatException err)
			{
			throw new InvalidXMLException(err);
			}
		}
	
	static public AffineTransform svgToaffineTransform(String transform)
		{
		if(StringUtils.isBlank(transform)) return null;
		String s=transform.trim();
		
		if(s.startsWith("matrix("))
			{
			int i=s.indexOf(")");
			if(i==-1) throw new IllegalArgumentException(s);
			if(!StringUtils.isBlank(s.substring(i+1))) throw new IllegalArgumentException(s);
			String tokens[]=s.substring(7, i).split("[,]");
			if(tokens.length!=6) throw new IllegalArgumentException(s);
			return new AffineTransform(new double[]{
				Double.parseDouble(tokens[0]),
				Double.parseDouble(tokens[1]),
				Double.parseDouble(tokens[2]),
				Double.parseDouble(tokens[3]),
				Double.parseDouble(tokens[4]),
				Double.parseDouble(tokens[5])
				});
			}
		AffineTransform tr= new AffineTransform();
		while(s.length()!=0)
			{
		
			
			if(s.startsWith("scale("))
				{
				int i=s.indexOf(")");
				if(i==-1) throw new IllegalArgumentException(s);
				
				String s2= s.substring(6,i).trim();
				s= s.substring(i+1).trim();
				i= s2.indexOf(',');
				if(i==-1)
					{
					double scale= Double.parseDouble(s2.trim());
					
					AffineTransform tr2= AffineTransform.getScaleInstance(
							scale,scale
						);
					tr2.concatenate(tr);
					tr=tr2;
					}
				else
					{
					double scalex= Double.parseDouble(s2.substring(0,i).trim());
					double scaley= Double.parseDouble(s2.substring(i+1).trim());
					
					AffineTransform tr2= AffineTransform.getScaleInstance(
							scalex,scaley
						);
					tr2.concatenate(tr);
					tr=tr2;
					}
				}
			else if(s.startsWith("translate("))
				{
				int i=s.indexOf(")");
				if(i==-1) throw new IllegalArgumentException(s);
				String s2= s.substring(10,i).trim();
				s= s.substring(i+1).trim();
				i= s2.indexOf(',');
				if(i==-1)
					{
					double translate= Double.parseDouble(s2.trim());
					
					AffineTransform tr2= AffineTransform.getTranslateInstance(
							translate,0
						);
					tr2.concatenate(tr);
					tr=tr2;
					}
				else
					{
					double translatex= Double.parseDouble(s2.substring(0,i).trim());
					double translatey= Double.parseDouble(s2.substring(i+1).trim());
					
					AffineTransform tr2= AffineTransform.getTranslateInstance(
							translatex,translatey
						);
					tr2.concatenate(tr);
					tr=tr2;
					}
				}
			else if(s.startsWith("rotate("))
				{
				int i=s.indexOf(")");
				if(i==-1) throw new IllegalArgumentException(s);
				String s2= s.substring(7,i).trim();
				s= s.substring(i+1).trim();
				i= s2.indexOf(',');
				if(i==-1)
					{
					double angle= Double.parseDouble(s2.trim());
					
					AffineTransform tr2= AffineTransform.getRotateInstance((angle/180.0)*Math.PI);
					tr2.concatenate(tr);
					tr=tr2;
					}
				else
					{
					double angle= Double.parseDouble(s2.substring(0,i).trim());
					s2=s2.substring(i+1);
					i= s2.indexOf(',');
					if(i==-1) throw new IllegalArgumentException("bad rotation "+s);
					
					double cx= Double.parseDouble(s2.substring(0,i).trim());
					double cy= Double.parseDouble(s2.substring(i+1).trim());
					
					AffineTransform tr2= AffineTransform.getRotateInstance(
							angle,cx,cy
						);
					tr2.concatenate(tr);
					tr=tr2;
					}
				}
			else if(s.startsWith("skewX("))
				{
				int i=s.indexOf(")");
				if(i==-1) throw new IllegalArgumentException(s);
				String s2= s.substring(6,i).trim();
				s= s.substring(i+1).trim();
				
				double shx= Double.parseDouble(s2.trim());
				
				AffineTransform tr2= AffineTransform.getShearInstance(shx, 1f);
				tr2.concatenate(tr);
				tr=tr2;
				}
			else if(s.startsWith("skewY("))
				{
				int i=s.indexOf(")");
				if(i==-1) throw new IllegalArgumentException(s);
				String s2= s.substring(6,i).trim();
				s= s.substring(i+1).trim();
				
				double shy= Double.parseDouble(s2.trim());
				
				AffineTransform tr2= AffineTransform.getShearInstance(1f,shy);
				tr2.concatenate(tr);
				tr=tr2;
				}
			
			}
		return tr;
		}
	
	/**
	 * transform a shape into a SVG path as String
	 * @param shape the shape
	 * @return the SVG points for &lt;path&gt;
	 */
	static public String shapeToPath(Shape shape)
	{
		StringWriter out= new StringWriter();
		shapeToPath(out,shape);
		return out.toString();
	}

	
	
	
	/**
	 * transform a shape into a SVG path
	 * @param shape
	 * @return
	 */
	static public void shapeToPath(Writer out,Shape shape)
	{
		PrintWriter path= new PrintWriter(out);

		double tab[] = new double[6];
		PathIterator pathiterator = shape.getPathIterator(null);

		while(!pathiterator.isDone())
		{
			int currSegmentType= pathiterator.currentSegment(tab);
			switch(currSegmentType) {
			case PathIterator.SEG_MOVETO: {
				path.print( "M " + (tab[0]) + " " + (tab[1]) + " ");
				break;
			}
			case PathIterator.SEG_LINETO: {
				path.print( "L " + (tab[0]) + " " + (tab[1]) + " ");
				break;
			}
			case PathIterator.SEG_CLOSE: {
				path.print( "Z ");
				break;
			}
			case PathIterator.SEG_QUADTO: {
				path.print( "Q " + (tab[0]) + " " + (tab[1]));
				path.print( " "  + (tab[2]) + " " + (tab[3]));
				path.print( " ");
				break;
			}
			case PathIterator.SEG_CUBICTO: {
				path.print( "C " + (tab[0]) + " " + (tab[1]));
				path.print( " "  + (tab[2]) + " " + (tab[3]));
				path.print( " "  + (tab[4]) + " " + (tab[5]));
				path.print( " ");
				break;
			}
			default:
			{
				LOG.info("Cannot handled "+currSegmentType);
				break;
			}
			}
			pathiterator.next();
		}
		path.flush();
	}

	private static float getPathFloat(StringTokenizer t)
	{
		float pathFloat;
		String tempBuffer = t.nextToken();
		while (tempBuffer.equals(",")|| tempBuffer.equals(" ")){
			tempBuffer = t.nextToken();
		}
		if (tempBuffer.equals("-")){
			pathFloat =(float) -1.0 * new Float(t.nextToken()).floatValue();
		}
		else{
			pathFloat = new Float(tempBuffer).floatValue();
		}
		return(pathFloat);
	}
	
	public static GeneralPath polygonToShape(String lineString )
		{
		GeneralPath p = polylineToShape(lineString);
		p.closePath();
		return p;
		}
	
	public static GeneralPath polylineToShape(String lineString )
		{
		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		Scanner scanner= new Scanner(new StringReader(lineString));
		scanner.useDelimiter("[ \n,\t]+");
		
		boolean found=false;
		Double prev=null;
		while(scanner.hasNext())
			{
			String s=scanner.next();
			if(s.length()==0) continue;
			double v= Double.parseDouble(s);
			if(prev==null)
				{
				prev=v;
				}
			else
				{
				if(!found)
					{
					p.moveTo(prev, v);
					found=true;
					}
				else
					{
					p.lineTo(prev, v);
					}
				prev=null;
				}
			}
		if(prev!=null) throw new IllegalArgumentException("bad polyline "+lineString);
		return p;
		}
	
	
	public static GeneralPath pathToShape(String pathString )
	{
		float fx=0,fy=0,fx1=0,fx2=0,fy1=0,fy2=0,oldfx=0,oldfy=0;
		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		StringTokenizer t = new StringTokenizer(pathString.trim().replaceAll("[ ]+"," ")," ,MmLlCczArSsHhVvDdEeFfGgJjQqTtz-",true);
		while(t.hasMoreElements()){
			String tempBuffer = t.nextToken();
			//boolean negate=false;
			switch (tempBuffer.charAt(0)){
			case 'M': //Move To
				fx = getPathFloat(t);
				fy = getPathFloat(t);
				oldfx=fx;
				oldfy=fy;
				p.moveTo(fx, fy);
				break;
			case 'm':
				fx = getPathFloat(t);
				fy = getPathFloat(t);
				fx+=oldfx;
				fy+=oldfy;
				oldfx=fx;
				oldfy=fy;
				p.moveTo(fx, fy);
				break;

			case 'L': // Line to
				fx = getPathFloat(t);
				fy = getPathFloat(t);
				oldfx=fx;
				oldfy=fy;
				p.lineTo(fx, fy);
				break;
			case 'l':
				fx = getPathFloat(t);
				fy = getPathFloat(t);
				fx+=oldfx;
				fy+=oldfy;
				oldfx=fx;
				oldfy=fy;
				p.lineTo(fx, fy);
				break;
			case 'C':

				fx = getPathFloat(t);
				fy = getPathFloat(t);
				fx1 = getPathFloat(t);
				fy1 = getPathFloat(t);
				fx2 = getPathFloat(t);
				fy2 = getPathFloat(t);
				p.curveTo(fx,fy,fx1,fy1,fx2,fy2);
				oldfx= fx2;
				oldfy= fy2;



				break;
			case 'c':

				fx = getPathFloat(t);
				fy = getPathFloat(t);
				fx1 = getPathFloat(t);
				fy1 = getPathFloat(t);
				fx2 = getPathFloat(t);
				fy2 = getPathFloat(t);
				fx+=oldfx;
				fy+=oldfy;
				fx1+=oldfx;
				fy1=oldfy;
				fx2+=oldfx;
				fy2+=oldfy;
				p.curveTo(fx,fy,fx1,fy1,fx2,fy2);
				oldfx= fx2;
				oldfy= fy2;

				break;

			case 'z':
				//                   System.out.println("closepath");
				break;

			case 'A':
				LOG.warning("Cannot handle Absolute");
				break;
			case 'r':
				LOG.warning("Cannot handle relative");
				break;

			case 'S':
				LOG.warning("Cannot handle Smooth curve");
				break;

			case 's':
				LOG.warning("Cannot handle relative smooth curve");
				break;


			case 'H':
				fy = getPathFloat(t);
				oldfy=fy;
				p.lineTo(oldfx, fy);


				break;

			case 'h':
				fy = getPathFloat(t);
				fy+=oldfy;
				oldfy=fy;
				p.lineTo(oldfx, fy);

				break;

			case 'V':
				fx = getPathFloat(t);
				oldfx=fx;
				p.lineTo(fx, oldfy);

				break;

			case 'v':
				fx = getPathFloat(t);
				fx+=oldfx;
				oldfx=fx;
				p.lineTo(fx, oldfy);

				break;

			case 'D':
				LOG.warning("Cannot handle arc 1 - see spec");
				break;

			case 'd':
				LOG.warning("Cannot handle relative arc 1");
				break;
			case 'E':
				LOG.warning("Cannot handle arc 2 - with line");
				break;

			case 'e':
				LOG.warning("Cannot handle relative arc 2");
				break;
			case 'F':
				LOG.warning("Cannot handle arc 3");
				break;

			case 'f':
				LOG.warning("Cannot handle relative arc 3");
				break;
			case 'G':
				LOG.warning("Cannot handle arc 4");
				break;

			case 'g':
				LOG.warning("Cannot handle relative arc 4");
				break;


			case 'J':
				LOG.warning("Cannot handle elliptical quadrant");
				break;

			case 'j':
				LOG.warning("Cannot handle relative elliptical quadrant");
				break;
			case 'Q':
				fx = getPathFloat(t);
				fy = getPathFloat(t);
				fx1 = getPathFloat(t);
				fy1 = getPathFloat(t);
				p.quadTo(fx,fy,fx1,fy1);
				oldfx= fx2;
				oldfy= fy2;


				/*

    quadTo(float x1, float y1, float x2, float y2) 
              Adds a curved segment, defined by two new points, to the path by drawing a Quadratic curve that
    intersects both the current coordinates and the coordinates (x2, y2), using the specified point (x1, y1) as a
    quadratic parametric control point.

				 */

				break;

			case 'q':
				LOG.warning("Cannot handle relative quadratic bezier curve to");
				break;
			case 'T':
				LOG.warning("Cannot handle True Type quadratic bezier curve ");
				break;

			case 't':
				LOG.warning("Cannot handle relative True Type quadratic bezier curve");
				break;

			case '-':
				LOG.warning("Cannot handle Negative value");
				break;

			}


		}

		return p;
	}

}




