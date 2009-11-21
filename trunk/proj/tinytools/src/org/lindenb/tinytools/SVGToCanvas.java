package org.lindenb.tinytools;


import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.lindenb.awt.Dimension2D;
import org.lindenb.io.IOUtils;
import org.lindenb.lang.InvalidXMLException;
//import org.lindenb.svg.SVGUtils;
import org.lindenb.svg.SVGUtils;
import org.lindenb.svg.path.ParseException;
import org.lindenb.svg.path.SVGPathParser;
import org.lindenb.svg.transform.SVGTransformParser;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.AbstractApplication;
import org.lindenb.util.C;
import org.lindenb.util.StringUtils;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Transforms a Scalable Vector Graphics SVG
 * to javascript/Canvas
 * @author lindenb
 *
 */
public class SVGToCanvas
	extends AbstractApplication
	{
	
	private static long ID_GENERATOR=System.currentTimeMillis();
	
	public SVGToCanvas()
		{
		}
	
	private enum Selector
		{
		STROKE,FILL,OPACITY,STROKE_WIDTH,
		STROKE_LINECAP,
		STROKE_LINEJOIN,
		STROKE_MITERLIMIT,
		FONT_FAMILY,
		FONT_SIZE,
		FONT_WEIGHT,
		FONT_STYLE,
		TEXT_ANCHOR
		}
	
	
	private static class Text
		{
		String text;
		double x;
		double y;
		Text(double x,double y,String text)
			{
			this.x=x;
			this.y=y;
			this.text=text;
			}
		}
	
	/**
	 * 
	 * State
	 *
	 */
	private static class State
		{
		State prev=null;
		Text text=null;
		Shape shape=null;
		AffineTransform tr=null;
		List<State> children=new ArrayList<State>();
		Map<Selector,String> selector= new HashMap<Selector,String>();
		
		
		AffineTransform getTransform()
			{
			AffineTransform t=this.tr;
			if(t==null) t=new AffineTransform();
			AffineTransform old=null;
			if(prev!=null)
				{
				old=prev.getTransform();
				old.concatenate(t);
				t=old;
				}
			return t;
			}
		
		public String get(Selector sel)
			{
			String o=selector.get(sel);
			if(o==null && prev!=null) return prev.get(sel);
			return o;
			}
		
		public double getOpacity()
			{
			String s=(String)selector.get(Selector.OPACITY);
			double curr=(s==null?1.0:Double.parseDouble(s));
			double prevValue=(prev==null?1.0:prev.getOpacity());
			return curr*prevValue;
			}
		}
	

	
	private PrintStream out= System.out;
	private File fileout=null;
	
	private void parse(
			State parent,
			Element e) throws InvalidXMLException
		{
		State state= new State();
		state.prev=parent;
		parent.children.add(state);
		
		if(e.hasAttributes())
			{
			NamedNodeMap atts=e.getAttributes();
			for(int i=0;i< atts.getLength();++i)
				{
				Attr att= Attr.class.cast(atts.item(i));
				if(att.getNamespaceURI()!=null) continue;
				String s=att.getName();
				applyStyle(state,s,att.getValue());
				}
			}
		
		String shapeName= e.getLocalName();
		
		if(!SVG.NS.equals(e.getNamespaceURI()))
			{
			for(Node c=e.getFirstChild();c!=null;c=c.getNextSibling())
				{
				if(c.getNodeType()!=Node.ELEMENT_NODE) continue;
				parse(state,Element.class.cast(c));
				}
			}
		else if(shapeName==null)
			{
			LOG.warning("shapeName is null");
			}
		else if(shapeName.equals("g"))
			{
			
			for(Node c=e.getFirstChild();c!=null;c=c.getNextSibling())
				{
				if(c.getNodeType()!=Node.ELEMENT_NODE) continue;
				parse(state,Element.class.cast(c));
				}
			}
		else if(shapeName.equals("path"))
			{
			Attr d= e.getAttributeNode("d");
			if(d!=null)
				{
				Shape shape;
				try
				{
				shape=new SVGPathParser(d.getValue()).path();
				} catch(ParseException err)
					{
					throw new InvalidXMLException(e,err.getMessage());
					}
				state.shape=shape;
				}
			}
		else if(shapeName.equals("polyline"))
			{
			Attr points= e.getAttributeNode("points");
			if(points!=null)
				{
				Shape shape  = SVGUtils.polylineToShape(points.getValue());
				state.shape=shape;
				}
			}
		else if(shapeName.equals("polygon"))
			{
			Attr points= e.getAttributeNode("points");
			if(points!=null)
				{
				Shape shape  = SVGUtils.polygonToShape(points.getValue());
				state.shape=shape;
				}
			}
		else if(shapeName.equals("rect"))
			{
			
			Attr x= e.getAttributeNode("x");
			Attr y= e.getAttributeNode("y");
			Attr w= e.getAttributeNode("width");
			Attr h= e.getAttributeNode("height");
			if(x!=null && y!=null && w!=null && h!=null)
				{
				Shape shape =new Rectangle2D.Double(
					Double.parseDouble(x.getValue()),
					Double.parseDouble(y.getValue()),	
					Double.parseDouble(w.getValue()),	
					Double.parseDouble(h.getValue())
					);
				state.shape=shape;
				}
			}
		else if(shapeName.equals("line"))
			{
			Attr x1= e.getAttributeNode("x1");
			Attr y1= e.getAttributeNode("y1");
			Attr x2= e.getAttributeNode("x2");
			Attr y2= e.getAttributeNode("y2");
			if(x1!=null && y1!=null && x2!=null && y2!=null)
				{
				Shape shape =new Line2D.Double(
					Double.parseDouble(x1.getValue()),
					Double.parseDouble(y1.getValue()),	
					Double.parseDouble(x2.getValue()),	
					Double.parseDouble(y2.getValue())
					);
				state.shape=shape;
				}
			}
		else if(shapeName.equals("circle"))
			{
			Attr cx= e.getAttributeNode("cx");
			Attr cy= e.getAttributeNode("cy");
			Attr r= e.getAttributeNode("r");
			if(cx!=null && cy!=null && r!=null)
				{
				double radius=Double.parseDouble(r.getValue());
				Shape shape =new Ellipse2D.Double(
					Double.parseDouble(cx.getValue())-radius,
					Double.parseDouble(cy.getValue())-radius,	
					radius*2,	
					radius*2
					);
				state.shape=shape;
				}
			}
		else if(shapeName.equals("ellipse"))
			{
			Attr cx= e.getAttributeNode("cx");
			Attr cy= e.getAttributeNode("cy");
			Attr rx= e.getAttributeNode("rx");
			Attr ry= e.getAttributeNode("ry");
			if(cx!=null && cy!=null && rx!=null && ry!=null)
				{
				double radiusx=Double.parseDouble(rx.getValue());
				double radiusy=Double.parseDouble(ry.getValue());
				Shape shape =new Ellipse2D.Double(
					Double.parseDouble(cx.getValue())-radiusx,
					Double.parseDouble(cy.getValue())-radiusy,	
					radiusx*2,	
					radiusy*2
					);
				state.shape=shape;
				}
			}
		else if(StringUtils.isIn(shapeName,"title","defs","desc","metadata"))
			{
			//ignore
			}
		else if(shapeName.equals("text"))
			{
			Attr x= e.getAttributeNode("x");
			Attr y= e.getAttributeNode("y");
			if(x!=null && y!=null)
				{
				state.text= new Text(
					Double.parseDouble(x.getValue()),
					Double.parseDouble(y.getValue()),
					e.getTextContent()
					);
				}
			}
		else if(shapeName.equals("svg"))
			{
			for(Node c=e.getFirstChild();c!=null;c=c.getNextSibling())
				{
				if(c.getNodeType()!=Node.ELEMENT_NODE) continue;
				parse(state, Element.class.cast(c));
				}
			}
		else
			{
			LOG.warning("cannot display <"+e.getLocalName()+">");
			}
		
		}
	
	/**
	 * applyStyle
	 */
	private void applyStyle(
		State state,
		String key,
		String value
		) throws InvalidXMLException
		{
		if(StringUtils.isBlank(key) || StringUtils.isBlank(value)) return;
		if(key.equals("style"))
			{
			for(String style:value.split("[;]+"))
				{
				int j=style.indexOf(':');
				if(j!=-1)
					{
					applyStyle(
						state,
						style.substring(0,j).trim(),
						style.substring(j+1).trim());
					}
				}
			return;
			}
		
		if(key.equals("transform"))
			{
			if(state.shape==null) state.tr= new AffineTransform();
			AffineTransform tr= SVGTransformParser.parse(value);
			state.tr.concatenate(tr);
			return;
			}
		
		for(Selector sel:Selector.values())
			{
			if(key.equals(sel.name().toLowerCase().replace('_', '-')))
				{
				state.selector.put(sel, value);
				return;
				}
			}
		if(StringUtils.isIn(key, "x","y","width","height","id")) return;
		System.err.println("Not handled :"+key+"="+value);
		
		
		}
	
	@Override
	protected int processArg(String[] args, int optind)
		{
		if(args[optind].equals("-o"))
			{
			this.fileout=new File(args[++optind]);
			return optind;
			}
		return super.processArg(args, optind);
		}
	
	private void startHTML()
		{
		out.println("<html><body>");
		}
	
	private void endHTML()
		{
		out.println("</body></html>");
		}
	
	private String unit(String s)
		{
		if(s.endsWith("px") ||
				s.endsWith("pt"))
				{
				return unit(s.substring(0,s.length()-2));
				}
		return s;
		}
	
	/**
	 * generateCode
	 */
	private void generateCode(State state, Map<Selector,String> current)
		{
		if(state.tr!=null)
			{
			double f[]=new double[6];
			state.getTransform().getMatrix(f);
			out.println("c.setTransform("+
					f[0]+","+f[1]+","+
					f[2]+","+f[3]+","+
					f[4]+","+f[5]+");"
					);
			}
		
		String fill= state.get(Selector.FILL);
		boolean do_fill=!fill.equals("none");
		String stroke= state.get(Selector.STROKE);
		boolean do_stroke=!stroke.equals("none");
		
		
		
		if(!fill.equals(current.get(Selector.FILL)))
			{
			if(!fill.equals("none")) out.println("c.fillStyle=\""+fill+"\";");
			current.put(Selector.FILL, fill);
			}
		
		if(!stroke.equals(current.get(Selector.STROKE)))
			{
			if(!stroke.equals("none")) out.println("c.strokeStyle=\""+stroke+"\";");
			current.put(Selector.STROKE, stroke);
			}
		
		String opacity= String.valueOf(state.getOpacity());
		
		if(!opacity.equals(current.get(Selector.OPACITY)))
			{
			out.println("c.globalAlpha=\""+opacity+"\";");
			current.put(Selector.OPACITY, opacity);
			}		
		
		String strokeWidth= state.get(Selector.STROKE_WIDTH);
		if(!strokeWidth.equals(current.get(Selector.STROKE_WIDTH)))
			{
			out.println("c.lineWidth=\""+unit(strokeWidth)+"\";");
			current.put(Selector.STROKE_WIDTH, strokeWidth);
			}
		
		String lineCap = state.get(Selector.STROKE_LINECAP);
		if(!lineCap.equals(current.get(Selector.STROKE_LINECAP)))
			{
			out.println("c.lineCap=\""+lineCap+"\";");
			current.put(Selector.STROKE_LINECAP, lineCap);
			}
		
		String lineJoin = state.get(Selector.STROKE_LINEJOIN);
		if(!lineJoin.equals(current.get(Selector.STROKE_LINEJOIN)))
			{
			out.println("c.lineJoin=\""+lineJoin+"\";");
			current.put(Selector.STROKE_LINEJOIN, lineJoin);
			}
		
		String mitterLimit = state.get(Selector.STROKE_MITERLIMIT);
		if(!mitterLimit.equals(current.get(Selector.STROKE_MITERLIMIT)))
			{
			out.println("c.mitterLimit=\""+mitterLimit+"\";");
			current.put(Selector.STROKE_MITERLIMIT, mitterLimit);
			}
		
		boolean font_changed=false;
		String fontSize = state.get(Selector.FONT_SIZE);
		if(!fontSize.equals(current.get(Selector.FONT_SIZE)))
			{
			font_changed=true;
			current.put(Selector.FONT_SIZE, fontSize);
			}
		
		String fontWeight = state.get(Selector.FONT_WEIGHT);
		if(!fontWeight.equals(current.get(Selector.FONT_WEIGHT)))
			{
			font_changed=true;
			current.put(Selector.FONT_WEIGHT, fontWeight);
			}
		
		String fontStyle = state.get(Selector.FONT_STYLE);
		if(!fontStyle.equals(current.get(Selector.FONT_STYLE)))
			{
			font_changed=true;
			current.put(Selector.FONT_STYLE, fontStyle);
			}
		
		String fontFamily = state.get(Selector.FONT_FAMILY);
		if(!fontFamily.equals(current.get(Selector.FONT_FAMILY)))
			{
			font_changed=true;
			current.put(Selector.FONT_FAMILY, fontFamily);
			}
		
		String textAnchor = state.get(Selector.TEXT_ANCHOR);
		if(!textAnchor.equals(current.get(Selector.TEXT_ANCHOR)))
			{
			out.println("c.textAnchor=\""+textAnchor+"\";");
			current.put(Selector.TEXT_ANCHOR, textAnchor);
			}
		
		//textalign
		if(font_changed)
			{
			out.print("c.font=\""+fontStyle+" "+fontWeight+" "+fontSize+" "+fontFamily+"\";");
			}
		
		
		if(state.text!=null && (do_fill || do_stroke))
			{
			if(do_stroke)
				{
				out.println("c.strokeText(\""+
					C.escape(state.text.text)+"\","+
					state.text.x+","+state.text.y+");");
				}
			
			if(do_fill)
				{
				out.println("c.fillText(\""+
					C.escape(state.text.text)+"\","+
					state.text.x+","+state.text.y+");");
				}
			}
			
		if(state.shape!=null)
			{
			if(!do_fill && !do_stroke)
				{
				//nothing
				}
			else if(state.shape instanceof Rectangle2D)
				{
				Rectangle2D r=Rectangle2D.class.cast(state.shape);
				String tmp=""+
						(int)r.getX()+","+
						(int)r.getY()+","+
						(int)r.getWidth()+","+
						(int)r.getHeight();
				if(do_fill) out.println("c.fillRect("+tmp+");");
				if(do_stroke) out.println("c.strokeRect("+tmp+");");
				}
			else
				{
				out.print("c.beginPath();");
				PathIterator iter= state.shape.getPathIterator(null);
				float coords[]=new float[6];
				while(!iter.isDone())
					{
					switch(iter.currentSegment(coords))
						{
						case PathIterator.SEG_MOVETO:
							{
							out.print("c.moveTo("+coords[0]+","+coords[1]+");");
							break;
							}
						case PathIterator.SEG_LINETO:
							{
							out.print("c.lineTo("+coords[0]+","+coords[1]+");");
							break;
							}
						case PathIterator.SEG_QUADTO:
							{
							out.print(
								"c.quadraticCurveTo("+
								coords[0]+","+coords[1]+","+
								coords[2]+","+coords[3]+");"
								);
							break;
							}
						case PathIterator.SEG_CUBICTO:
							{
							out.print(
								"c.bezierCurveTo("+
								coords[0]+","+coords[1]+","+
								coords[2]+","+coords[3]+","+
								coords[4]+","+coords[5]+
								");"
								);
							break;
							}
						case PathIterator.SEG_CLOSE:
							{
							out.print("c.closePath();");
							break;
							}
						}
					
					iter.next();
					}
				if(do_fill) out.println("c.fill();");
				if(do_stroke) out.println("c.stroke();");
				
				}
			/*
			
			for(Selector sel:Selector.values())
				{
				
					
					out.println("c.lineWidth="+unit(step.value)+";");
					}
				else if(step.key.equals("lineCap"))
					{
					out.println("c.lineCap=\""+step.value+"\";");
					}
				else if(step.key.equals("lineJoin"))
					{
					out.println("c.lineJoin=\""+step.value+"\";");
					}
				else if(step.key.equals("miterLimit"))
					{
					out.println("c.miterLimit=\""+step.value+"\";");
					}
				
				
				}*/
			
			
			
			}
		
		
		for(State c: state.children)
			{
			generateCode(c,current);
			}
		
		if(state.tr!=null && state.prev!=null)
			{
			double f[]=new double[6];
			AffineTransform tr=new AffineTransform(state.prev.getTransform());
			/*try
				{
				tr.invert();
				}
			catch (NoninvertibleTransformException e)
				{
				throw new RuntimeException(e);
				}*/
			tr.getMatrix(f);
			out.println("c.setTransform("+
					f[0]+","+f[1]+","+
					f[2]+","+f[3]+","+
					f[4]+","+f[5]+");"
					);
			}
		
		}
		
	
	private void paintDocument(Document dom)
		throws InvalidXMLException
		{

		
	
		Element root=dom.getDocumentElement();
		if(root==null) throw new InvalidXMLException(dom,"no root");
		if(!XMLUtilities.isA(root, SVG.NS, "svg")) throw new InvalidXMLException(root,"not a SVG root");
		
		State init= new State();
		init.selector.put(Selector.FILL, "white");
		init.selector.put(Selector.STROKE, "black");
		init.selector.put(Selector.OPACITY, "1.0");
		init.selector.put(Selector.STROKE_WIDTH, "1");
		init.selector.put(Selector.STROKE_LINEJOIN, "round");
		init.selector.put(Selector.STROKE_LINECAP, "butt");
		init.selector.put(Selector.STROKE_MITERLIMIT, "1");
		init.selector.put(Selector.FONT_FAMILY, "Courier");
		init.selector.put(Selector.FONT_SIZE, "12");
		init.selector.put(Selector.FONT_STYLE, "normal");
		init.selector.put(Selector.FONT_WEIGHT, "normal");
		init.selector.put(Selector.TEXT_ANCHOR, "start");
		
		
		
		parse(init,root);
		
		
		Dimension2D size=SVGUtils.getSize(root);
		long id=(++ID_GENERATOR);
		out.print("<div>");
		out.print(
			"<canvas id='ctx"+id+"' " +
				"width='"+size.getWidth()+"' " +
				"height='"+size.getHeight()+"'></canvas>");
		out.print("<script>");
		
		out.print("function paint"+id+"(){" +
			"var canvas=document.getElementById('ctx"+id+"');" +
			"if (!canvas.getContext) return;"+
			"var c=canvas.getContext('2d');");
				
		
		
		generateCode(init,new HashMap<Selector, String>());
		
		out.print("}paint"+id+"();</script>");
		
		out.println("</div>");
		}
	
	
	
	@Override
	protected int processArgs(String[] args)
		{
		int optind=super.processArgs(args);
		
		try {
			DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
			f.setCoalescing(true);
			f.setNamespaceAware(true);
			f.setValidating(false);
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(false);
			f.setIgnoringElementContentWhitespace(true);
			DocumentBuilder docBuilder= f.newDocumentBuilder();
			
			if(fileout!=null)
				{
				this.out=new PrintStream(this.fileout);
				}
			
			startHTML();
			
	            if(optind==args.length)
                        {
                        Document dom=docBuilder.parse(System.in);
                        paintDocument(dom);
                        }
                else
                        {
                        while(optind< args.length)
                            {
                            String fname=args[optind++];
                          	InputStream in=IOUtils.openInputStream(fname);
                          	Document dom=docBuilder.parse(in);
                          	paintDocument(dom);
                          	in.close();
                            }
                        }
			
			endHTML();
			this.out.flush();
			
			if(fileout!=null)
				{
				this.out.close();
				}
			this.out=System.out;
		} catch (Exception e)
			{
			e.printStackTrace();
			throw new RuntimeException(e);
			}

		
		
		return optind;
		}
	
	public static void main(String[] args)
		{
		try
			{
			new SVGToCanvas().processArgs(args);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	
}
