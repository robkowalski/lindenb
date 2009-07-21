package org.lindenb.svg;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.awt.Dimension2D;
import org.lindenb.lang.InvalidXMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class SVGIcon implements Icon
	{
	private Node dom;
	private SVGRenderer renderer=new SVGRenderer();
	private Dimension2D svgSize=null;
	public SVGIcon(Node dom)
		{
		if(dom==null) throw new NullPointerException("dom is null");
		this.dom=dom;
		
		try
			{
			if(dom.getNodeType()==Node.ELEMENT_NODE)
				{
				this.svgSize= SVGUtils.getSize(Element.class.cast(dom));
				}
			else if(dom.getNodeType()==Node.DOCUMENT_NODE)
				{
				this.svgSize= SVGUtils.getSize(Document.class.cast(dom).getDocumentElement());
				}
			else
				{
				throw new IllegalArgumentException("bad node type");
				}
			} 
		catch(InvalidXMLException err)
			{	
			throw new IllegalArgumentException(err);
			}
		}
	
	
	
	@Override
	public int getIconHeight() {
		return (int)this.svgSize.getHeight();
	}

	@Override
	public int getIconWidth() {
		return (int)this.svgSize.getWidth();
	}

	@Override
	public void paintIcon(Component c, Graphics g1d, int x, int y) {
		Graphics2D g=(Graphics2D)g1d;
		g.translate(x,y);
		try
			{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			this.renderer.paint(Graphics2D.class.cast(g1d),this.dom);
			} 
		catch(InvalidXMLException err)
			{	
			err.printStackTrace();
			}
		g.translate(-x,-y);
		}
	
public static void main(String[] args) {
	try {
	
	 
		DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
		domFactory.setCoalescing(true);
		domFactory.setExpandEntityReferences(true);
		domFactory.setIgnoringComments(true);
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		domFactory.setNamespaceAware(true);
		DocumentBuilder domBuilder= domFactory.newDocumentBuilder();
		
		Document dom=domBuilder.parse("http://upload.wikimedia.org/wikipedia/commons/f/fd/Ghostscript_Tiger.svg");
		//Document dom=domBuilder.parse(new java.io.File("/home/lindenb/jeter.svg"));
		//Document dom=domBuilder.parse("http://www.w3.org/TR/SVG/images/text/text01.svg");
		SVGIcon icon= new SVGIcon(dom);
    	JOptionPane.showMessageDialog(null, new JScrollPane(new JLabel(icon)));
	    
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
