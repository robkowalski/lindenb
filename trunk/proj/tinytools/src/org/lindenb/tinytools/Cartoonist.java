package org.lindenb.tinytools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.awt.Cap;
import org.lindenb.awt.ColorUtils;
import org.lindenb.awt.Dimension2D;
import org.lindenb.awt.Join;
import org.lindenb.lang.InvalidXMLException;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;
import org.lindenb.util.StringUtils;
import org.lindenb.xml.NodeWrapper;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Cartoonist: a great tool making great things
 *
 */
public class Cartoonist
	{
	private static final String NS="urn:cartoonist";
	private static final Logger LOG= Logger.getLogger(Cartoonist.class.getName());
	private Document dom;
	private Map<Object,Object> renderingHints= new HashMap<Object,Object>();
	//private Map<String, AbstractNode<?>> localName2node= new HashMap<String, AbstractNode<?>>();
	
	/**
	 * AbstractNode
	 */
	public static abstract class AbstractNode<T>
		{
		public abstract String getLocalName();
		public abstract T render(Element root) throws IOException,InvalidXMLException;
		}
	


	
	/*
	public static class ColorNode

			{
			public class ColorFiller 
				{
				Color color=null;
				ColorFiller(Color color)
					{
					this.color=color;
					}
			
				public void fill(Graphics2D g, Shape area) throws InvalidXMLException
					{
					if(this.color==null) return;
					g.setColor(this.color);
					g.fill(area);
					}
				}
	
		
		
			public String getLocalName()
				{
				return "color";
				}
			
			
			public Object render(Element root) throws IOException, InvalidXMLException
				{
				Attr att= root.getAttributeNode("rgb");
				if(att==null) throw new InvalidXMLException(root,"Cannot get att @rgb");
				return new ColorFiller(ColorUtils.parseColor(att.getValue()));
				}
			}
	
	
	public static abstract class GradientNode
	extends PaintNode
		{
		class Stop
			extends NodeWrapper<Element>
			{
			Stop(Element e) { super(e);}
			float offset() throws InvalidXMLException
				{
				String att= getAttribute("offset");
				if(att==null) throw new InvalidXMLException(getNode(),"no @offset");
				if(!Cast.Float.isA(att)) throw new InvalidXMLException(getNode(),"not a Float");
				return Cast.Float.cast(att);
				}
			
			Color color() throws InvalidXMLException
				{
				String att= getAttribute("stop-color");
				if(att==null) throw new InvalidXMLException(getNode(),"no @stop-color");
				return ColorUtils.parseColor(att);
				}
			}
		protected float unit(Element e,String attName,Shape shape,String defaultValue,double length)
			{
			Attr att=e.getAttributeNode(attName);
			String pixel= (att!=null?att.getValue():defaultValue);
			pixel=pixel.trim();
			if(pixel.endsWith("%"))
				{
				return (float)length*(Float.parseFloat(pixel.substring(0,pixel.length()-1))/100f);
				}
			return Float.parseFloat(pixel);
			}
		List<Stop> listStop(Element root)
			throws InvalidXMLException
			{
			List<Stop> list= new ArrayList<Stop>();
			for(Node n=root.getFirstChild();n!=null;n=n.getNextSibling())
				{
				if(!XMLUtilities.isA(n, NS, "stop")) continue;
				list.add(new Stop(Element.class.cast(n)));
				}
			return list;
			}
		
		
		}
	
	public class LinearGradientNode
	extends GradientNode
		{
		class LinearFiller extends 
			NodeWrapper<Element>
			implements SurfaceFiller
			{
			LinearFiller(Element root) { super(root);}
			
			@Override
			public void fill(Graphics2D g, Shape shape) throws InvalidXMLException
				{
				List<Stop> stops= listStop(getNode());
				if(stops.size()<2) throw new InvalidXMLException(getNode(),"count(stops)<2");
				Rectangle2D dim=shape.getBounds2D();
				float startX= unit(getNode(),"x1",shape,"0%",dim.getWidth());
				float startY= unit(getNode(),"y1",shape,"0%",dim.getHeight());
				float endX= unit(getNode(),"x2",shape,"100%",dim.getWidth());
				float endY= unit(getNode(),"y2",shape,"0%",dim.getHeight());
				float fractions[]=new float[stops.size()];
				Color colors[]=new Color[stops.size()];
				for(int i=0;i< stops.size();++i)
					{
					fractions[i]= stops.get(i).offset();
					colors[i]= stops.get(i).color();
					}
				LinearGradientPaint grad= new LinearGradientPaint(
						startX,startY,endX,endY,fractions,colors
					);
				g.setPaint(grad);
				g.fill(shape);
				}
			}
		
		@Override
		public String getLocalName()
			{
			return "linearGradient";
			}
		
		
		@Override
		public SurfaceFiller render(Element root) throws IOException,
				InvalidXMLException
			{
			return new LinearFiller(root);
			}
		
		}
	
	public abstract class RadialGradientNode
	extends GradientNode
		{
		class RadialFiller extends 
		NodeWrapper<Element>
		implements SurfaceFiller
			{
			 RadialFiller(Element root) { super(root);}
			
			@Override
			public void fill(Graphics2D g, Shape shape) throws InvalidXMLException
				{
				List<Stop> stops= listStop(getNode());
				if(stops.size()<2) throw new InvalidXMLException(getNode(),"count(stops)<2");
				Rectangle2D dim=shape.getBounds2D();
				float cx= unit(getNode(),"cx",shape,"50%",dim.getWidth());
				float cy= unit(getNode(),"cy",shape,"50%",dim.getHeight());
				float radius= unit(getNode(),"r",shape,"50%",Math.max(dim.getWidth(),dim.getHeight()));
				
				float fractions[]=new float[stops.size()];
				Color colors[]=new Color[stops.size()];
				for(int i=0;i< stops.size();++i)
					{
					fractions[i]= stops.get(i).offset();
					colors[i]= stops.get(i).color();
					}
				RadialGradientPaint grad= new RadialGradientPaint(
						cx,cy,radius,fractions,colors
					);
				g.setPaint(grad);
				g.fill(shape);
				}
			}
		
		@Override
		public String getLocalName()
			{
			return "radialGradient";
			}
		}*/
	
	

	
	/**
	 * Cartoonist
	 */
	private Cartoonist()
		{
		this.renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		this.renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		//this.renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	
		}
	
	/** get a required attribute */
	private static String getAtt(Element element, String attName) throws InvalidXMLException
		{
		Attr att= element.getAttributeNode(attName);
		if(att==null) throw new InvalidXMLException(element,"@"+attName+" missing");
		return att.getValue();
		}
	/** get a required attribute as double */
	private static double getAttDouble(Element element, String attName) throws InvalidXMLException
		{
		String x= getAtt(element,attName);
		if(!Cast.Double.isA(x)) throw new InvalidXMLException(element,"@"+attName+" : not a double");
		return Cast.Double.cast(x);
		}
	

	
	/** Scan the document and returns the document with the given id
	 * throws an exception if the element was note found */
	private Element getElementById(String id)
		throws  InvalidXMLException
		{
		Element e= getElementById(this.dom, id);
		if(e==null) throw new InvalidXMLException(this.dom,"Canoot find @id:"+id);
		return e;
		}
	
	/** Scan the node and its children. returns the element with the given id */
	private Element getElementById(Node root,String id)
		{
		if(root.getNodeType()==Node.ELEMENT_NODE && root.hasAttributes())
			{
			Element e= Element.class.cast(root);
			Attr att=e.getAttributeNode("id");
			if(att!=null && att.getValue().equals(id)) return e;
			}
		if(root.hasChildNodes())
			{
			for(Node n=root.getFirstChild();n!=null;n=n.getNextSibling())
				{
				Element e=getElementById(n, id);
				if(e!=null) return e;
				}
			}
		return null;
		}
	
	
	
	private Shape renderShape(Element root) throws IOException,InvalidXMLException
		{
		LOG.info("rendering shape "+XMLUtilities.node2path(root));
		if(!NS.equals(root.getNamespaceURI()))  throw new InvalidXMLException(root,"illegal namespace-uri");
		String local= root.getLocalName();
		if(local.equals("shape-ref"))
			{
			return renderShape(getElementById(getAtt(root, "shape-id")));
			}
		else if(local.equals("rect") || local.equals("rectangle"))
			{
			return new Rectangle2D.Double(
					getAttDouble(root,"x"),
					getAttDouble(root,"y"),
					getAttDouble(root,"width"),
					getAttDouble(root,"height")
					);
			}
		else if(local.equals("circle"))
			{
			double r= getAttDouble(root,"r");
			return new Ellipse2D.Double(
					getAttDouble(root,"cx")-r,
					getAttDouble(root,"cy")-r,
					r*2.0,r*2.0
				);
			}
		else if(local.equals("polygon"))
			{
			String points[]= getAtt(root, "points").trim().split("[ \t\n\t]+");
			GeneralPath path= new GeneralPath();
			for(int i=0;i< points.length;++i)
				{
				String point= points[i];
				int loc= point.indexOf(',');
				if(loc==-1) throw new InvalidXMLException(root,"bad point "+point);
				Double x= Cast.Double.cast(point.substring(0,loc));
				Double y= Cast.Double.cast(point.substring(loc+1));
				if(x==null || y==null) throw new InvalidXMLException("bad point "+point);
				if(i==0)
					{
					path.moveTo(x, y);
					}
				else
					{
					path.lineTo(x, y);
					}
				}
			path.closePath();
			return path;
			}
		else if(local.equals("stroke-path"))
			{
			BasicStroke stroke=null;
			Shape strokedShape=null;
			for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!NS.equals(n1.getNamespaceURI())) continue;
				Shape shape= renderShape(Element.class.cast(n1));
				if(shape==null) continue;
				if(strokedShape!=null) throw new InvalidXMLException(root,"Excpected only one shape. ");
				strokedShape=shape;
				}
			if(strokedShape==null)
				{
				Attr att= root.getAttributeNode("shape-id");
				if(att==null) throw new InvalidXMLException(root,"clip is missing an shape");
				strokedShape = renderShape(getElementById(att.getValue()));
				if(strokedShape==null) throw new InvalidXMLException(att,"Cannot get shape");
				}
			if(strokedShape==null) throw new InvalidXMLException(root,"Cannot get shape");
			
			float width=1.0f;
			Attr att= root.getAttributeNode("stroke-width");
			if(att!=null) width= new Float(att.getValue());
			Cap cap=Cap.ROUND;
			att= root.getAttributeNode("cap");
			if(att!=null) cap=  Cap.parseSVG(att.getValue());
			
			Join join= Join.ROUND;
			att= root.getAttributeNode("join");
			if(att!=null) join=  Join.parseSVG(att.getValue());
			stroke= new BasicStroke(width,cap.stroke(),join.stroke());
			
			return stroke.createStrokedShape(strokedShape);
			}
		else if(StringUtils.isIn(local,"add","substract","intersect","exclusiveOr"))
			{
			Shape first=null;
			Shape second=null;
			for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!NS.equals(n1.getNamespaceURI())) continue;
				Shape shape= renderShape(Element.class.cast(n1));
				if(shape==null) continue;
				if(first==null)
					{
					first=shape;
					}
				else if(second==null)
					{
					second=shape;
					}
				else
					{
					throw new InvalidXMLException(root,"Excpected only one shape. ");
					}
				}
			if(first==null) throw new InvalidXMLException(root,"No shape found under this node.");
			if(second==null) throw new InvalidXMLException(root,"Expected two shapes found under this node.");
			Area area= new Area(first);
			if(local.equals("add"))
				{
				area.add(new Area(second));
				}
			else if(local.equals("subtract"))
				{
				area.subtract(new Area(second));
				}
			else if(local.equals("intersect"))
				{
				area.intersect(new Area(second));
				}
			else if(local.equals("exclusiveOr"))
				{
				area.exclusiveOr(new Area(second));
				}
			else 
				{
				throw new IllegalStateException(local);
				}
			return area;
			}
		return null;
		}
	
	private BufferedImage renderImage(Element root) throws IOException,InvalidXMLException
		{
		LOG.info("rendering image "+XMLUtilities.node2path(root));
		if(!NS.equals(root.getNamespaceURI()))  throw new InvalidXMLException(root,"illegal namespace-uri");
		String local= root.getLocalName();
		if(local.equals("image-ref"))
			{
			return renderImage(getElementById(getAtt(root, "image-id")));
			}
		else if(local.equals("image"))
			{
			return ImageIO.read(new URL(getAtt(root, "src")));
			}
		else if(local.equals("clip"))
			{
			BufferedImage clippedImage=null;
			
			//find image
			for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!NS.equals(n1.getNamespaceURI())) continue;
				BufferedImage img= renderImage(Element.class.cast(n1));
				if(img==null) continue;
				if(clippedImage!=null) throw new InvalidXMLException(root,"Excpected only one images.");
				clippedImage=img;
				}
			if(clippedImage==null)
				{
				Attr att= root.getAttributeNode("image-id");
				if(att==null) throw new InvalidXMLException(root,"clip is missing an image");
				clippedImage = renderImage(getElementById(att.getValue()));
				if(clippedImage==null) throw new InvalidXMLException(att,"Cannot get image");
				}
			
			if(clippedImage==null) throw new InvalidXMLException(root,"Cannot get image");
			
			Shape clippedShape=null;
			//find shape for clipping
			for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!NS.equals(n1.getNamespaceURI())) continue;
				Shape shape= renderShape(Element.class.cast(n1));
				if(shape==null) continue;
				if(clippedShape!=null) throw new InvalidXMLException(root,"Excpected only one shape. "+clippedShape);
				clippedShape=shape;
				}
			if(clippedShape==null)
				{
				Attr att= root.getAttributeNode("shape-id");
				if(att==null) throw new InvalidXMLException(root,"clip is missing an shape");
				clippedShape = renderShape(getElementById(att.getValue()));
				if(clippedShape==null) throw new InvalidXMLException(att,"Cannot get shape");
				}
			
			if(clippedShape==null) throw new InvalidXMLException(root,"Cannot get shape");
			Rectangle2D bounds = clippedShape.getBounds2D();
			BufferedImage result= new BufferedImage(
				(int)bounds.getWidth(),
				(int)bounds.getHeight(),
				BufferedImage.TYPE_INT_ARGB
				);
			//translate to top-x,top-y
			clippedShape = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()).createTransformedShape(clippedShape);
			Graphics2D g= result.createGraphics();
			g.setRenderingHints(this.renderingHints);
			g.setClip(clippedShape);
			g.translate(-bounds.getX(), -bounds.getY());
			g.drawImage(clippedImage,0,0,null);
			g.translate(bounds.getX(),bounds.getY());
			g.dispose();
			return result;
			}
		else if(local.equals("g"))
			{
			return null;
			}
		else
			{
			return null;
			}
		}
	
	private BufferedImage renderImage() throws IOException,InvalidXMLException
		{
		LOG.info("rendering image");
		Element root= dom.getDocumentElement();
		if(root==null) throw new InvalidXMLException(dom,"Document has no root");
		if(!XMLUtilities.isA(root, NS, "cartoon")) throw new InvalidXMLException(dom,"Document is not a "+NS+"cartoon");
		Attr id=(Attr)root.getAttributeNode("default");
		if(id==null) throw new InvalidXMLException(root,"no @default");
		LOG.info("rendering image "+id);
		BufferedImage img= renderImage(getElementById(id.getValue()));
		if(img==null) throw new InvalidXMLException(id,"Not an image");
		return img;
		}
	
	public static void main(String[] args) {
		try {
			int optind=0;
			String documentURL="/home/lindenb/cartoon.xml";
			String imageId=null;
		    while(optind<args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("Pierre Lindenbaum PhD.");
					System.err.println(Compilation.getLabel());
					System.err.println("-h this screen");
					System.err.println("-f <document-url>");
					System.err.println("-i <image-id>");
					System.err.println("<image-id>");
					return;
					}
				 else if (args[optind].equals("-f"))
				     {
				     documentURL= args[++optind];
				     }
				 else if (args[optind].equals("-i"))
				     {
					 imageId= args[++optind];
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
		    if(documentURL==null)
		    	{
		    	System.err.println("document URL was not specified");
		    	return;
		    	}
		 
			DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
			domFactory.setCoalescing(true);
			domFactory.setExpandEntityReferences(true);
			domFactory.setIgnoringComments(true);
			domFactory.setNamespaceAware(true);
			domFactory.setValidating(false);
			domFactory.setNamespaceAware(true);
			DocumentBuilder domBuilder= domFactory.newDocumentBuilder();
			Document dom=domBuilder.parse(documentURL);
			Cartoonist app= new Cartoonist();
			app.dom=dom;
			BufferedImage img=null;
			if(imageId!=null)
				{
				img=app.renderImage(app.getElementById(imageId));
				}
			else
				{
				img= app.renderImage();
				}
			JLabel label=new JLabel(new ImageIcon(img));
			JOptionPane.showMessageDialog(null, label);
			System.err.println("OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
