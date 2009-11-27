package org.lindenb.tinytools;


import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.InvalidXMLException;
import org.lindenb.svg.path.ParseException;
import org.lindenb.svg.path.SVGPathParser;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class SketchySVG
{
private double NoiseAmp = .05;
private boolean wiggly = true;
	  
static private final int B  = 256;//0x100;
static private final int BM = 255;//0xff;

static private final int N  = 0x1000;
static private int p[] = new int[B + B +2];
static private double g1[] = new double[B + B +2];

static private void init()
	{

	Random rand= new Random();
	
	int i=0, j, k;
	
	for (i = 0 ; i < B ; i++)
		{
		p[i] = i;
		g1[i] = (float)((rand.nextInt(Short.MAX_VALUE) % (B + B)) - B) / B;
		}
	while (--i>=0) {
		k = p[i];
		p[i] = p[j = (rand.nextInt(Short.MAX_VALUE) % B )];
		p[j] = k;
	}
	
	for (i = 0 ; i < B + 2 ; i++) {
		p[B + i] = p[i];
		g1[B + i] = g1[i];
	}

}

static private double noise1(double arg)
	{
	   int bx0, bx1;
	   double rx0, rx1, sx, t, u, v;

	   t = arg + N;
	   bx0 = ((int)t)&BM;
	   bx1 = (bx0+1)&BM;
	   rx0 = t - (int) t;
	   rx1 = rx0 -1;

	   sx = rx0*rx0*(3.0 - 2.0 * rx0);
	   u = rx0 * g1[p[bx0]];
	   v = rx1 * g1[p[bx1]];

	   return (lerp(sx, u, v));
	}
	
 static private double lerp(double t, double a, double b)
 	{
	 return(a+t*(b-a));
	 }
  
	 
protected double getNoiseAmp()
	 {
		 return this.NoiseAmp;
	 }
	  
private void drawWiggle(GeneralPath path,double p, float ax, float ay, float bx, float by)
	{
    /** distance horizontal */
	float width = bx - ax;
	//distence vertical
	float height = by - ay;
	
	float distanceCube = width * width + height * height;
	double freq = .1;

	//int lastX = 0, lastY = 0;
    
	if (distanceCube > 0) {
        double len = Math.sqrt(distanceCube) * 2;
  	    int w = (int)(len*getNoiseAmp());
		if (w > 50) w = 50;
        double wx = (((-height * w) / len));
        double wy = (((width * w) / len));
		
		/*if (ax == bx || ay == by) {
			System.err.println("wx = " + wx + ", wy = " + wy);	
		}*/

		
		float x1 = ax, y1 = ay;
		path.lineTo(x1, y1);
		for (int i = 1; i < 10; i++)
			{
			double t = i/10.0;
		    double n = noise1(p);
		    p += freq;
			
		    int x2 = (int)(lerp(t, ax, bx) + n * wx);
			int y2 = (int)(lerp(t, ay, by) + n * wy);
			if (wiggly) {
				t = (i-1)/10.0;
				n = noise1(p-123.0); p += freq;
				if (n < 0) n = -n;
				t = t-n;
				if (t < -0.01) t=-0.01;
				x1 = (int)(lerp(t, ax, bx) + n * wx);
				y1 = (int)(lerp(t, ay, by) + n * wy);
			}
			//lineTo(path,x1, y1, x2, y2);
			path.lineTo(x2, y2);
			x1 = x2;
			y1 = y2;		
		}
		//lineTo(path,x1, y1, bx, by);
		path.lineTo(bx, by);
	}
  }


	
	public Shape createStrokedShape(Shape shape) {
		GeneralPath path=new GeneralPath(GeneralPath.WIND_NON_ZERO);
		double p=1;
		 float tab[] = new  float[6];
		 float currx=0f,curry=0f;
	    PathIterator pathiterator = shape.getPathIterator(null);
	       
	        while(!pathiterator.isDone()) {
	          switch(pathiterator.currentSegment(tab)) {
	          case PathIterator.SEG_MOVETO:
	          	{
	        	  path.moveTo(tab[0],tab[1]);
	        	  currx=tab[0];
	        	  curry=tab[1];
	              break;
	          	}
	          case PathIterator.SEG_LINETO: {
	        	  drawWiggle(path,p,currx,curry,tab[0],tab[1]);
	        	  currx=tab[0];
	        	  curry=tab[1];
	              break;
	          }
	          case PathIterator.SEG_CLOSE: {
	              path.closePath();
	              break;
	          }
	          case PathIterator.SEG_QUADTO: {
	             path.quadTo(tab[0],tab[1],tab[2],tab[3]);
	             currx=tab[2];
	        	 curry=tab[3];
	             break;
	          }
	          case PathIterator.SEG_CUBICTO: {
	              path.curveTo(tab[0],tab[1],tab[2],tab[3],tab[4],tab[5]);
	              currx=tab[4];
	              curry=tab[5];
	              break;
	          	}
	          }
	          pathiterator.next();
	        }
		
		return path;
	}

	
	
	
public SketchySVG() throws Exception
	{
	}

public Document transform(Document dom) throws InvalidXMLException
	{
	if(dom==null) throw new NullPointerException("dom is null");
	Element root= dom.getDocumentElement();
	if(root==null) return dom;
	if(!SVG.NS.equals(root.getNamespaceURI())) throw new InvalidXMLException("NS is not "+SVG.NS);
	if(!"svg".equals(root.getLocalName())) throw new InvalidXMLException("root is not svg:svg");
	_transform(root);
	return dom;
	}



public void _transform(Node root) throws InvalidXMLException
	{
	if(root.getNodeType()==Node.ELEMENT_NODE &&
		SVG.NS.equals(root.getNamespaceURI()))
		{
		Element e= Element.class.cast(root);
		String localName=e.getLocalName();
		if(localName.equals("rect") && hasAttributes(root,"width","height"))
			{
			double x= att2double(e,"x");
			double y= att2double(e,"y");
			double width=att2double(e,"width");
			double height=att2double(e,"height");
			Rectangle2D rect= new Rectangle2D.Double(x,y,width,height);
			Element path= createPath(e);
			removeAttributes(path,"x","y","width","height");
			path.setAttribute("d", shape2noise(rect));
			root.getParentNode().replaceChild(path, root);
			}
		else if(localName.equals("circle") && hasAttributes(root,"r"))
			{
			double cx= att2double(e,"cx");
			double cy= att2double(e,"cy");
			double r=att2double(e,"r");
			GeneralPath circle= new GeneralPath();
			for(int i=0;i< 360;i+=20)
				{
				double x= cx+ r*Math.cos(i/180.0*Math.PI);
				double y= cy+ r*Math.sin(i/180.0*Math.PI);
				if(i==0)
					{
					circle.moveTo(x, y);
					}
				else
					{
					circle.lineTo(x, y);
					}
				}
			circle.closePath();
			
			Element path= createPath(e);
			removeAttributes(path,"r");
			path.setAttribute("d", shape2noise(circle));
			root.getParentNode().replaceChild(path, root);
			}
		else if(localName.equals("line") && hasAttributes(root,"x1","x2","y1","y2"))
			{
			double x1= att2double(e,"x1");
			double y1= att2double(e,"y1");
			double x2= att2double(e,"x2");
			double y2= att2double(e,"y2");
			Line2D rect= new Line2D.Double(x1,y1,x2,y2);
			Element path= createPath(e);
			removeAttributes(path,"x1","x2","y1","y2");
			path.setAttribute("d", shape2noise(rect));
			root.getParentNode().replaceChild(path, root);
			}
		else if(localName.equals("path") && hasAttributes(root,"d"))
			{
			Shape thepath= Path2Shape(e.getAttribute("d"));
			Element path= createPath(e);
			path.setAttribute("d", shape2noise(thepath));
			root.getParentNode().replaceChild(path, root);
			}
		else if(localName.equals("polygon") && hasAttributes(root,"points"))
			{
			String points[]= e.getAttribute("points").trim().split("[ \t\n]+");
			GeneralPath p= new GeneralPath();
			for(int i=0;i< points.length;++i)
				{
				String s=points[i];
				int j=s.indexOf(',');
				if(j==-1) throw new InvalidXMLException(e,"bad points (missing comma)");
				double x= Double.parseDouble(s.substring(0,j));
				double y= Double.parseDouble(s.substring(j+1));
				if(i==0)
					{
					p.moveTo(x, y);
					}
				else
					{
					p.lineTo(x, y);
					}
				}
			p.closePath();
			Element path= createPath(e);
			removeAttributes(path,"points");
			path.setAttribute("d", shape2noise(p));
			root.getParentNode().replaceChild(path, root);
			}
		}
	if(root.hasChildNodes())
		{
		Node next=null;
		Node n1=root.getFirstChild();
		while(n1!=null)
			{
			next=n1.getNextSibling();
			_transform(n1);
			n1=next;
			}
		}
	}


private static Shape Path2Shape(String pathString )
	{
	try
		{
		return new SVGPathParser(pathString).path();
		}
	catch (ParseException e)
		{
		throw new RuntimeException(e);
		}
	}


private String shape2noise(Shape shape)
	{
	return Shape2path(createStrokedShape(shape));
	}

private String Shape2path(Shape shape)
	{
	StringWriter w= new StringWriter();
	PrintWriter path= new PrintWriter(w);

	
	 double tab[] = new double[6];
	    PathIterator pathiterator = shape.getPathIterator(null);
	       
	        while(!pathiterator.isDone()) {
	          switch(pathiterator.currentSegment(tab)) {
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
	          }
	          pathiterator.next();
	        }
	path.flush();
	
	
	return w.toString();
	}

private static Element createPath(Element n)
	{
	String qName=(n.getPrefix()==null?"":n.getPrefix()+":")+"path";
	Element path= n.getOwnerDocument().createElementNS(SVG.NS, qName);
	NamedNodeMap atts= n.getAttributes();
	for(int i=0;i< atts.getLength();++i)
		{
		path.setAttributeNode((Attr)atts.item(i).cloneNode(false));
		}
	return path;
	}

private static double att2double(Element n,String att)
	{
	return att2double(n.getAttributeNode(att));
	}

private static double att2double(Attr att)
	{
	return (att!=null && Cast.Double.isA(att.getValue())?Cast.Double.cast(att.getValue()):0.0);
	}

private static boolean hasAttributes(Node n,String...attNames)
	{
	if(!n.hasAttributes()) return false;
	NamedNodeMap atts= n.getAttributes();
	for(String att:attNames)
		{
		if(atts.getNamedItem(att)==null) return false;
		}
	return true;
	}

private static void removeAttributes(Element n,String...attNames)
	{
	for(String att:attNames)
		{
		n.removeAttribute(att);
		}
	}


private static void echo(Document dom,PrintStream out) throws Exception
	{
	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.transform(new DOMSource(dom), new StreamResult(out));
	}

public static void main(String[] args) {
	try {
		SketchySVG app= new SketchySVG();
		SketchySVG.init();
		PrintStream out= System.out;
		FileOutputStream fout=null;
		File filename=null;
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println(Compilation.getLabel());
				System.err.println("-h this screen");
				return;
				}
			else if (args[optind].equals("-o"))
			     {
			     filename=new File(args[++optind]);
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
	    
		DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
		domFactory.setCoalescing(true);
		domFactory.setExpandEntityReferences(true);
		domFactory.setIgnoringComments(true);
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		domFactory.setNamespaceAware(true);
		domFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder	domBuilder = domFactory.newDocumentBuilder();
		
		if(filename!=null)
			{
			fout= new FileOutputStream(filename);
			out= new PrintStream(fout);
			}
		
		if(optind==args.length)
		 	{
			Document dom=domBuilder.parse(System.in);
			dom=app.transform(dom);
			echo(dom,out);
		 	}
		 else 
		 	{
	    	while(optind< args.length)
	    		{
	    		InputStream in=null;
	    		try {
	    			in= IOUtils.openInputStream(args[optind++]);
	    			Document dom=domBuilder.parse(in);
	    			dom=app.transform(dom);
	    			echo(dom,out);
					}
	    		catch (java.io.IOException e) {
					throw e;
					}
				finally
					{
					if(in!=null) in.close();
					in=null;
					}
	    		}
		 	}
		out.flush();
		if(fout!=null)
			{
			fout.flush();
			fout.close();
			}
		
		}
	catch (Exception e) {
		e.printStackTrace();
	}
}
}
