package org.lindenb.tool.krobar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.awt.Cap;
import org.lindenb.awt.ColorUtils;
import org.lindenb.awt.Join;
import org.lindenb.awt.XMouseAdapter;
import org.lindenb.io.PreferredDirectory;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.swing.FileExtensionFilter;
import org.lindenb.util.Pair;

public class Model
	implements Iterable<Layer>
	{
	private Vector<Layer> layers_=new Vector<Layer>();
	private org.lindenb.awt.Dimension2D.Double dimension=null;
	private File saveAsFile=null;
	

	private static class RegionChooser extends JPanel
		{
		private static final long serialVersionUID = 1L;
		private Model model;
		private JPanel leftArea;
		private JPanel rightArea;
		private Rectangle2D modelRect;
		private JTextField xLabel,yLabel,widthLabel,heightLabel;
		RegionChooser(Model model)
			{
			super(new GridLayout(1,0,2,2));
			this.model=model;
			this.modelRect= null;
			setBorder(new EmptyBorder(2,2,2,2));
			Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
			setPreferredSize(new Dimension(screen.width-200,screen.height-200));
			this.leftArea= new JPanel()
				{
				private static final long serialVersionUID = 1L;
				@Override
				protected void paintComponent(Graphics g) {
					paintLeftArea(Graphics2D.class.cast(g));
					}
				};
			this.leftArea.setOpaque(true);
			this.leftArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			XMouseAdapter mouse= new XMouseAdapter()
				{
				@Override
				public void mousePressed(MouseEvent e) {
					this.mouseStart= new Point(e.getX(),e.getY());
					this.mousePrev=null;
					}
				
				@Override
				public void mouseDragged(MouseEvent e)
					{
					Graphics2D g= getGraphics(e);
					g.setXORMode(Color.RED);
					if(this.mousePrev!=null)
						{
						drawRect(g,this.mouseStart,this.mousePrev);
						}
					mousePrev= new Point(e.getX(),e.getY());
					drawRect(g,this.mouseStart,this.mousePrev);
					setSelectionViewRect(this.mouseStart.x,this.mouseStart.y,mousePrev.x,mousePrev.y);
					g.dispose();
					}
				
				@Override
				public void mouseReleased(MouseEvent e)
					{
					if(e.getX()==mouseStart.x && e.getY()==mouseStart.y)
						{
						setSelectionViewRect(0,0,leftArea.getWidth(),leftArea.getHeight());
						}
					else
						{
						setSelectionViewRect(this.mouseStart.x,this.mouseStart.y,mousePrev.x,mousePrev.y);
						}
					leftArea.repaint();
					}
				/*
				@Override
				public void mouseMoved(MouseEvent e) {
					AffineTransform tr= RegionChooser.this.model.createView2ModelTransform(null, leftArea.getSize());
					Point2D p=tr.transform(e.getPoint(), null);
					
					}*/
				
				};
			this.leftArea.setBorder(new LineBorder(Color.DARK_GRAY));
			this.leftArea.addMouseListener(mouse);
			this.leftArea.addMouseMotionListener(mouse);
			
			this.add(leftArea);
			
			//JPanel center= new JPanel();
			
			//center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
			Box center= Box.createVerticalBox();
			Pair<JLabel, JTextField> p = createLabel("X");
			center.add(p.first()); center.add(xLabel=p.second());
			p = createLabel("Y");
			center.add(p.first()); center.add(yLabel=p.second());
			p = createLabel("Width");
			center.add(p.first()); center.add(widthLabel=p.second());
			p = createLabel("Height");
			center.add(p.first()); center.add(heightLabel=p.second());
			
			
			center.add(Box.createVerticalStrut(2000));
			this.add(center);
			this.rightArea= new JPanel()
				{
				/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					paintRightArea(Graphics2D.class.cast(g));
					}
				};
			this.rightArea.setOpaque(true);
			this.rightArea.setBorder(new LineBorder(Color.DARK_GRAY));
			this.add(this.rightArea);
			
			}
		
		private Pair<JLabel, JTextField> createLabel(String label)
			{
			 Pair<JLabel, JTextField>  p = new Pair<JLabel, JTextField>(
				new JLabel(label,JLabel.LEADING),
				new JTextField(20)
			 	);
			 p.first().setFont(new Font("Helvetica",Font.BOLD,14));
			 p.first().setAlignmentX(0.5f);
			 p.second().setFont(new Font("Helvetica",Font.BOLD,14));
			 p.second().setAlignmentX(0.5f);
			 p.second().setEditable(false);
			 return p;
			}
		
		private void setSelectionViewRect(int x1,int y1,int x2,int y2)
			{
			AffineTransform tr= RegionChooser.this.model.createView2ModelTransform(null, leftArea.getSize());
			Point2D p1=tr.transform(new Point(Math.min(x1, x2),Math.min(y1, y2)), null);
			Point2D p2=tr.transform(new Point(Math.max(x1, x2),Math.max(y1, y2)), null);
			modelRect= new Rectangle2D.Double(p1.getX(),p1.getY(),p2.getX()-p1.getX(),p2.getY()-p1.getY());
			xLabel.setText(String.valueOf(modelRect.getX()));
			yLabel.setText(String.valueOf(modelRect.getY()));
			widthLabel.setText(String.valueOf(modelRect.getWidth()));
			heightLabel.setText(String.valueOf(modelRect.getHeight()));
			rightArea.repaint();
			}
		
		private void paintLeftArea(Graphics2D g)
			{
			paintMatrix(g,leftArea.getWidth(),leftArea.getHeight());
			BufferedImage img= model.createImage(
					null,
					leftArea.getSize(),
					null);
			g.drawImage(img, 0, 0, leftArea);
			
			if(modelRect!=null)
				{
				g.setXORMode(Color.RED);
				AffineTransform tr=model.createModel2ViewTransform(
					null,
					leftArea.getSize()	
					);
				AffineTransform old= g.getTransform();
				g.setTransform(tr);
				g.draw(modelRect);
				g.setTransform(old);
				}
			
			}
		private void paintRightArea(Graphics2D g)
			{
			paintMatrix(g,rightArea.getWidth(),rightArea.getHeight());
			if(modelRect!=null)
				{
				BufferedImage img= model.createImage(
						modelRect,
						leftArea.getSize(),
						null);
				g.drawImage(img, 0, 0, rightArea);
				}
			}
		
		private void paintMatrix(Graphics2D g,int width,int height)
			{
			int size=25;
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.WHITE);
			for(int i=0;i<width;i+=size)
				{
				for(int j=0;j< height;j+=size)
					{
					if((i/size)%2==(j/size)%2) continue;
					g.fillRect(i, j, size, size);
					}
				}
			}
		}
	
	
	
	public Model()
		{
		layers().addElement(new Layer(this));
		}
	
	public File getFile() {
		return saveAsFile;
		}
	
	public void setFile(File f) {
		this.saveAsFile = f;
		}
	
	/*
	public Vector<Figure> figures()
		{
		return this.figures_;
		}*/
	
	public Vector<Layer> layers()
		{
		return this.layers_;
		}
	
	@Override
	public Iterator<Layer> iterator() {
		return layers().iterator();
		}
	
	public static Model parse(File file)  throws IOException,XMLStreamException
		{
		Model m= new Model();
		m.parse_(file);
		return m;
		}
	
	public static File showDialogExportImage(Component owner,Model model)
		{
		RegionChooser zone= new RegionChooser(model);
		if(JOptionPane.showConfirmDialog(owner, zone,"Select the Region",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null)!=JOptionPane.OK_OPTION)
			{
			return null;
			}
		
		JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
		FileExtensionFilter filter= FileExtensionFilter.createImageFilter();
		chooser.setFileFilter(filter);
		if(chooser.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		File f= chooser.getSelectedFile();
		if(!filter.accept(f))
			{
			f= new File(f.getParentFile(),f.getName().concat(".png"));
			}
		if(f.exists() && JOptionPane.showConfirmDialog(owner,f.getName()+" exists. Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.OK_OPTION)
			{
			return null;
			}
		PreferredDirectory.setPreferredDirectory(f);
		try {
			BufferedImage img=model.createImage(zone.modelRect, null, Color.WHITE);
			ImageIO.write(img, "jpeg", f);
			img=null;
		} catch (Exception e) {
			ThrowablePane.show(owner, e);
			return null;
			}
		return f;
		}
	
	public static File showDialogExportSVG(Component owner,Model model)
		{
		RegionChooser zone= new RegionChooser(model);
		if(JOptionPane.showConfirmDialog(owner, zone,"Select the Region",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null)!=JOptionPane.OK_OPTION)
			{
			return null;
			}
		
		JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
		FileExtensionFilter filter=new FileExtensionFilter("SVG files",".svg",".svgz");
		chooser.setFileFilter(filter);
		if(chooser.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		File f= chooser.getSelectedFile();
		if(!filter.accept(f))
			{
			f= new File(f.getParentFile(),f.getName().concat(".svg"));
			}
		if(f.exists() && JOptionPane.showConfirmDialog(owner,f.getName()+" exists. Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.OK_OPTION)
			{
			return null;
			}
		PreferredDirectory.setPreferredDirectory(f);
		try {
			
			model.writeSVG(f);
			
		} catch (Exception e) {
			ThrowablePane.show(owner, e);
			return null;
			}
		return f;
		}
	
	private void parse_(File file) throws IOException,XMLStreamException
        {
        Reader r= null;
        if( file.getName().endsWith(".gz") ||
        	file.getName().endsWith(".svgz"))
        	{
        	r= new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
        	}
        else
        	{
        	r=new FileReader(file);
        	}
        parse_(r);
        r.close();
        }
	
	private void parse_(Reader in) throws IOException,XMLStreamException
    	{
		layers().clear();
		XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        XMLEventReader parser = factory.createXMLEventReader(in);
        XMLEvent evt;
        while(!(evt=parser.nextEvent()).isEndDocument())
            {
            if(evt.isStartElement())
                {
                StartElement e=evt.asStartElement();
                if(e.getName().getNamespaceURI().equals(SVG.NS))
                    {
                    if(  e.getName().getLocalPart().equals("polyline"))
                        {
                    	Figure f=readFigure(e);
                    	if(f!=null &&f.getPath()!=null && !layers().isEmpty())
	                    	{
                    		f.setLayer(this.layers().lastElement());
                    		this.layers().lastElement().figures().addElement(f);	          
	                        }
                        }
                    else if(  e.getName().getLocalPart().equals("g"))
                        {
                    	Layer layer=readLayer(e);
                    	if(layer!=null)
	                    	{
	                        this.layers().addElement(layer);
	                        }
                        }
                    }
                }
            }
    	}
	
	private Layer readLayer(StartElement e) throws IOException,XMLStreamException
		{
		Layer L= new Layer(this);
		Attribute att= e.getAttributeByName(new QName("style"));
	    if(att!=null)
            {
        	String ss[]=att.getValue().split(";");
        	for(String s:ss)
                {
        		s=s.trim();
        		if(s.length()==0) continue;
        		int i=s.indexOf(":");
        		if(i==-1) throw new IOException("; missing in "+s);
                String key=s.substring(0,i).trim().toLowerCase();
                String value =s.substring(i+1).trim();
                
               
               if(key.equals("opacity"))
                    {
                	L.setOpacity(  Float.parseFloat(value) );
                    }
                }
            }
	        return L;
			}
	
	
	private Figure readFigure(StartElement e) throws IOException,XMLStreamException
		{
		Figure figure= new Figure();
		
		
        Attribute att= e.getAttributeByName(new QName("style"));
        if(att!=null)
            {
        	String ss[]=att.getValue().split(";");
        	for(String s:ss)
                {
        		s=s.trim();
        		if(s.length()==0) continue;
        		int i=s.indexOf(":");
        		if(i==-1) throw new IOException("; missing in "+s);
                String key=s.substring(0,i).trim().toLowerCase();
                String value =s.substring(i+1).trim();
                
                if(key.equals("stroke-linecap"))
                    {
                	figure.setStrokeCap(Cap.parseSVG(value));
                    }
                else if(key.equals("stroke-linejoin"))
                    {
                	figure.setStrokeJoin(Join.parseSVG(value));
                    }
                else if(key.equals("stroke-width"))
                    {
                	figure.setStrokeWidth( Float.parseFloat(value) );
                    }
                else if(key.equals("stroke"))
                    {
                    figure.setColor(ColorUtils.parseColor(value));
                    }
                else if(key.equals("opacity"))
                    {
                	figure.setOpacity(  Float.parseFloat(value) );
                    }
                else if(key.equals("fill"))
	                {
                	if(value.equals("none"))
                		{
                		figure.setFilled(false);
                		}
                	else
	                	{
	                	figure.setColor(ColorUtils.parseColor(value));
	                	figure.setFilled(true);
		                }
	                }
                }
            }
       
        att= e.getAttributeByName(new QName("points"));
         if(att!=null)
             {
        	 GeneralPath path=null;
             StreamTokenizer st= new StreamTokenizer(new StringReader(att.getValue()));
            while(true)
                {
                if(st.nextToken()==StreamTokenizer.TT_EOF) break;
                if(st.ttype!=StreamTokenizer.TT_NUMBER) throw new IOException("error in points expected number");
                double x= st.nval;
                if(st.nextToken()!=',') throw new IOException("error in points expected a comma");
                if(st.nextToken()!=StreamTokenizer.TT_NUMBER) throw new IOException("error in points expected number");
                double y= st.nval;
               
                if(path==null)
                    {
                	path= new GeneralPath();
                	path.moveTo(x, y);
                    }
                else
                    {
                	path.lineTo(x, y);
                    }
                }
             figure.setPath(path);
             }
        if(figure.getPath()==null) return null;
        return figure;
		}
	
	
	public void updateDimension()
		{
		if(this.dimension==null) this.dimension= new org.lindenb.awt.Dimension2D.Double(1,1);
		this.dimension.setSize(1,1);
		if(layers().isEmpty())
			{	
			return;
			}
		for(Layer L:this.layers())
			{
			for(Figure f:L.figures())
				{
				Rectangle2D r= f.getBounds();
				this.dimension.max(
					r.getMaxX()+f.getStrokeWidth(),
					r.getMaxY()+f.getStrokeWidth()
					);
				}
			}
		}
	
	
	
	public Dimension2D getSize()
		{
		if(this.dimension==null) updateDimension();
		return new org.lindenb.awt.Dimension2D.Double(this.dimension);
		}
	
	public double getWidth()
		{
		if(this.dimension==null) updateDimension();
		return this.dimension.getWidth();
		}
	
	public double getHeight()
		{
		if(this.dimension==null) updateDimension();
		return this.dimension.getHeight();
		}
	
	/**
	 * @param modelRect model rectangle, if null, the whole dimension will be used.
	 */
	public AffineTransform createView2ModelTransform(
			Rectangle2D modelRect,
			Dimension2D viewSize
			)
		{
		AffineTransform tr= createModel2ViewTransform(modelRect, viewSize);
		try {
			return tr.createInverse();
		} catch (NoninvertibleTransformException e1) {
			throw new RuntimeException(e1);
		}
		}
	
	/**
	 * @param modelRect model rectangle, if null, the whole dimension will be used.
	 */
	public AffineTransform createModel2ViewTransform(
			Rectangle2D modelRect,
			Dimension2D viewSize
			)
		{
		if(modelRect==null) modelRect= new Rectangle2D.Double(0,0,getWidth(),getHeight());
		AffineTransform tr= AffineTransform.getScaleInstance(
				viewSize.getWidth()/modelRect.getWidth(),//ratio X
				viewSize.getHeight()/modelRect.getHeight()//ratio Y
				);
		tr.translate( -modelRect.getX(), -modelRect.getY());
		return tr;
		}
	/**
	 * @param modelRect model rectangle, if null, the whole dimension will be used.
	 * @param viewSize if null the dimension of the modelRect
	 * @param background background color, may be null
	 */
	public BufferedImage createImage(
		Rectangle2D modelRect,
		Dimension viewSize,
		Color background
		)
		{
		if(modelRect==null) modelRect= new Rectangle2D.Double(0,0,getWidth(),getHeight());
		if(viewSize==null) viewSize= new Dimension((int)modelRect.getWidth(),(int)modelRect.getHeight());
		BufferedImage img= new BufferedImage(viewSize.width,viewSize.height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g= img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(background!=null)
			{
			g.setColor(background);
			g.fillRect(0, 0, viewSize.width, viewSize.height);
			}
		g.setTransform(createModel2ViewTransform(modelRect,viewSize));
		paint(g);
		g.dispose();
		return img;
		}
	
	public void paint(Graphics2D g)
		{
		for(Layer L:layers())
			{
			for(Figure f: L.figures())
				{
				f.paint(g);
				}
			}
		}
	
	public void writeImage(File file) throws IOException
		{
		writeImage(file,null,null,null);
		}
	
	public void writeImage(
			File file,
			Rectangle2D viewBox,
			Dimension destSize,
			Color background
			) throws IOException
		{
		String suff= FileExtensionFilter.getFileExtension(file);
		if(suff==null) suff="png";
		BufferedImage img= createImage(viewBox, destSize, background);
		ImageIO.write(img, suff, file);
		}
	
	public void writeSVG(File file) throws IOException,XMLStreamException
		{
		writeSVG(file,null,null);
		}
	
	public void writeSVG(
			File file,
			Rectangle2D viewBox,
			Dimension destSize
			) throws IOException,XMLStreamException
		{
		if(viewBox==null) viewBox=new Rectangle2D.Double(0,0,getWidth(),getHeight());
		if(destSize==null) destSize= new Dimension((int)viewBox.getWidth(),(int)viewBox.getHeight());
		Writer fw= null;
		if(file.getName().toLowerCase().endsWith(".svgz"))
			{
			fw= new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)));
			}
		else
			{
			fw= new FileWriter(file);
			}
		XMLOutputFactory factory=XMLOutputFactory.newInstance();
		XMLStreamWriter w=factory.createXMLStreamWriter(fw);
		w.writeStartDocument();
		w.setPrefix("svg", SVG.NS);
		w.setPrefix("xlink", XLINK.NS);
		w.writeStartElement(SVG.NS, "svg");
		w.writeNamespace("svg", SVG.NS);
		w.writeNamespace("xlink", XLINK.NS);
		w.writeAttribute("width", String.valueOf(destSize.width));
		w.writeAttribute("height", String.valueOf(destSize.height));
		w.writeAttribute("viewBox",""+viewBox.getX()+" "+viewBox.getY()+" "+viewBox.getWidth()+" "+viewBox.getHeight());
		w.writeAttribute("preserveAspectRatio", "none");
		for(Layer L: this.layers())
			{
			w.writeStartElement(SVG.NS, "g");
			w.writeAttribute("style", "opacity:"+L.getOpacity()+";");
			for(Figure f:L.figures())
				{
				f.writeSVG(w);
				}
			w.writeEndElement();
			}
		w.writeEndElement();
		w.writeEndDocument();
		fw.flush();
		fw.close();
		}
	
	
	public static void main(String[] args)
		{
		try {
			Model model= new Model();
			Layer bot= model.layers().lastElement();
			bot.setOpacity(0.5f);
			model.layers().addElement(bot);
			for(int i=0;i< 100;i+=5)
				{
				Figure f= new Figure();
				f.setStrokeWidth(1.5f);
				f.setOpacity(0.6f);
				f.setPath(new Rectangle(i,i,18,18));
				f.setLayer(bot);
				f.setColor(Color.BLUE);
				model.layers().lastElement().figures().add(f);
				}
			
			System.err.println(model.getSize());
			model.writeSVG(new File("/home/pierre/jeter.svg"));
			model.writeImage(new File("/home/pierre/jeter.png"));
			JOptionPane.showMessageDialog(null,new RegionChooser(model));
		} catch (Exception e) {
			e.printStackTrace();
			}
		}
	
	}
