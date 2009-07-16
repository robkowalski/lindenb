package java.org.lindenb.graffiti;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lindenb.awt.Cap;
import org.lindenb.awt.ColorUtils;
import org.lindenb.awt.Join;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.util.Pair;

/**
 * Figure
 * @author pierre
 *
 */
public class Figure
	{
	private Layer layer=null;
	private GeneralPath path;
	private float alpha=1.f;
	private Join strokeJoin= Join.ROUND;
	private Cap strokeCap= Cap.ROUND;
	private Color color=Color.BLACK;
	private float strokeWidth=1f;
	private boolean filled=false;
	
	public Figure()
		{
		}
	
	public void setLayer(Layer layer) {
		this.layer = layer;
		}
	
	public Layer getLayer() {
		return layer;
		}
	
	public float getOpacity()
		{
		return alpha;
		}
	
	public Composite getComposite()
		{
		float parent=(getLayer()==null?1f:getLayer().getOpacity());
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, parent*getOpacity());
		}
	
	public void setOpacity(float alpha) {
		this.alpha = alpha;
		}
	
	public Join getStrokeJoin() {
		return strokeJoin;
		}
	
	public void setStrokeJoin(Join strokeJoin) {
		this.strokeJoin = strokeJoin;
		}
	
	public Cap getStrokeCap() {
		return strokeCap;
		}
	public void setStrokeCap(Cap strokeCap) {
		this.strokeCap = strokeCap;
		}
	
	public float getStrokeWidth() {
		return strokeWidth;
		}
	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
		}
	
	public BasicStroke getStroke()
		{
		return new BasicStroke(
				getStrokeWidth(),
				getStrokeCap().stroke(),
				getStrokeJoin().stroke()
				);
		}
	
	public void paint(Graphics2D g)
		{
		g.setColor(getColor());
		g.setStroke(getStroke());
		g.setComposite(getComposite());
		g.draw(getPath());
		if(isFilled()) g.fill(getPath());
		}
	
	public Color getColor() {
		return color;
		}
	
	public void setColor(Color color) {
		this.color = color;
		}
	
	public GeneralPath getPath() {
		return path;
		}
	
	public Rectangle2D getBounds()
		{
		return getPath().getBounds2D();
		}
	
	public void setPath(GeneralPath path) {
		this.path = path;
		}
	
	public void setPath(Rectangle2D rect)
		{
		this.path = new GeneralPath();
		this.path.moveTo(rect.getX(), rect.getY());
		this.path.lineTo(rect.getX(), rect.getMaxY());
		this.path.lineTo(rect.getMaxX(), rect.getMaxY());
		this.path.lineTo(rect.getMaxX(), rect.getY());
		this.path.lineTo(rect.getX(), rect.getY());
		//this.path.closePath();
		}
	
	public void setFilled(boolean filled) {
		this.filled = filled;
		}
	
	public boolean isFilled() {
		return filled;
		}
	
	
	public Pair<Point2D.Double,Point2D.Double> getStartEndPoints()
		{
		Pair<Point2D.Double,Point2D.Double> p =new Pair<Point2D.Double,Point2D.Double>(
			new Point2D.Double(),
			new Point2D.Double()
			);
		boolean first=true;
		PathIterator iter= getPath().getPathIterator(null);
		double array[]=new double[6];

		 while(!iter.isDone())
			{
			switch(iter.currentSegment(array))
				{
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_MOVETO:
					if(first)
						{
						p.first().setLocation(array[0], array[1]);
						first=false;
						}
					p.second().setLocation(array[0], array[1]);
					break;
				default:break;
				}
			iter.next();
			}
		 return p;
		}
	
	
	void writeSVG(XMLStreamWriter out) throws IOException,XMLStreamException
		{
		out.writeEmptyElement(SVG.NS, "polyline");
		StringBuilder sb= new StringBuilder();
		sb.append("stroke-linecap:"+getStrokeCap().svg()+";");
		sb.append("stroke-linejoin:"+getStrokeJoin().svg()+";");
		sb.append("opacity:").append(String.valueOf(getOpacity())).append(";");
		sb.append("stroke:").append(ColorUtils.toRGB(getColor())).append(";");
		sb.append("stroke-width:").append(String.valueOf(getStrokeWidth())).append(";");
		sb.append("fill:"+(isFilled()?ColorUtils.toRGB(getColor()):"none")+";");
		out.writeAttribute("style", sb.toString());
		sb= new StringBuilder();
		PathIterator iter= getPath().getPathIterator(null);
		double array[]=new double[6];

		 while(!iter.isDone())
			{
			switch(iter.currentSegment(array))
				{
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_MOVETO:
					if(sb.length()!=0) sb.append(" ");
					sb.append(""+array[0]+","+array[1]);
					break;
				default:break;
				}
			iter.next();
			}
		
		out.writeAttribute("points", sb.toString());
		}
	

}
