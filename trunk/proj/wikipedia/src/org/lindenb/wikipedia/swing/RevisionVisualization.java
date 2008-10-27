package org.lindenb.wikipedia.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lindenb.awt.ColorUtils;
import org.lindenb.io.PreferredDirectory;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.me.Me;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.swing.ObjectAction;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Compilation;
import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Wikipedia;
import org.lindenb.xml.XMLUtilities;


public class RevisionVisualization extends JFrame
	{
	
	private static final long serialVersionUID = 1L;

	private JPanel drawingArea;
	private JList pageList;
	private JList catList;
	private boolean dirty=true;
	private double max_of_all_y;
	private BufferedImage offscreen;
	private Figure highlitedFigure=null;
	
	private static class Figure
		{
		Page page;
		Set<Category> categories=new HashSet<Category>();
		int userCount=0;
		int revisionCount=0;
		int sizes[];
		int revisions[];
		boolean displayed=true;
		GeneralPath shape=null;
		Color fill=null;
		Color pen=null;
		@Override
		public int hashCode() {
			return page.hashCode();
			}
		
		@Override
		public boolean equals(Object obj) {
			return obj==this;
			}
		}
	
	private Vector<Figure> figures=new Vector<Figure>(1000,500);
	private String header[];
	private JCheckBox useRevisionInsteadOfSize;
	
	RevisionVisualization(BufferedReader r) throws IOException
		{
		super(Compilation.getName());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		TreeSet<Category> allCategories= new TreeSet<Category>();
		Pattern TAB= Pattern.compile("[\t]");
		String line= r.readLine();
		if(line==null) throw new IOException("Header line missing");
		String tokens[]=TAB.split(line);
		this.header=new String[tokens.length-4];
		System.arraycopy(tokens, 4, this.header, 0, this.header.length);
		if(tokens.length<6) throw new IOException("bad number of columns");
		while((line=r.readLine())!=null)
			{
			tokens=TAB.split(line);
			if(tokens.length!=this.header.length+4) throw new IOException("illegale number of columns in "+line);
			Figure f= new Figure();
			f.page= new Page(tokens[0]);
			String cats[]= tokens[1].split("[|]");
			for(String cat:cats) {f.categories.add(new Category(cat)); }
			allCategories.addAll(f.categories);
			f.userCount = Integer.parseInt(tokens[2]);
			f.revisionCount = Integer.parseInt(tokens[3]);
			f.sizes=new int[header.length];
			f.revisions=new int[header.length];
			for(int i=4;i< tokens.length;++i)
				{
				int j=tokens[i].indexOf(";");
				f.sizes[i-4]=Integer.parseInt(tokens[i].substring(0,j));
				f.revisions[i-4]=Integer.parseInt(tokens[i].substring(j+1));
				}
			
			
			this.figures.add(f);
			}

		
		JPanel mainPane= new JPanel(new BorderLayout());
		setContentPane(mainPane);
		JPanel left= new JPanel(new GridLayout(0,1,2,2));
		mainPane.add(left,BorderLayout.WEST);
		
		
		
		
		JPanel pane1= new JPanel(new BorderLayout());
		pane1.setPreferredSize(new Dimension(200,200));
		left.add(pane1);
		pane1.setBorder(new TitledBorder("Pages"));
		
		this.pageList=new JList(new Vector<Figure>(this.figures));
		this.pageList.setCellRenderer(new DefaultListCellRenderer()
			{
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c= super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				this.setText(Figure.class.cast(value).page.getLocalName());
				return c;
				}
			});
		this.pageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroll=new JScrollPane(this.pageList);
		scroll.setPreferredSize(new Dimension(200,200));
		pane1.add(scroll,BorderLayout.CENTER);
		JPanel pane2= new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pane1.add(pane2,BorderLayout.SOUTH);
		pane2.add(new JButton(new AbstractAction("Clear")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pageList.getSelectionModel().clearSelection();
				}
			}));
		
		pane1= new JPanel(new BorderLayout());
		left.add(pane1);
		pane1.setBorder(new TitledBorder("Categories ("+allCategories.size()+")"));
		this.catList=new JList(new Vector<Category>(allCategories));
		this.catList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scroll=new JScrollPane(this.catList);
		scroll.setPreferredSize(new Dimension(200,200));
		pane1.add(scroll,BorderLayout.CENTER);
		pane2= new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pane1.add(pane2,BorderLayout.SOUTH);
		pane2.add(new JButton(new AbstractAction("Clear")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				catList.getSelectionModel().clearSelection();
				}
			}));
		
		this.drawingArea= new JPanel(null)
			{
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintDrawingArea(Graphics2D.class.cast(g));
				}
			@Override
			public String getToolTipText(MouseEvent event)
				{
				Figure f= getFigureAt(event.getX(),event.getY());
				if(f==null) return null;
				
				int i=(int)((header.length/(double)drawingArea.getWidth())*event.getX());
				if(i>=header.length) return f.page.getLocalName();
				StringBuilder b=new StringBuilder("<html><body>");
				b.append("<b>").append(XMLUtilities.escape(f.page.getLocalName())).append("</b>");
				b.append("<ul>");
				b.append("<li>").append(XMLUtilities.escape(header[i])).append("</li>");
				b.append("<li>Revisions: ").append(f.revisions[i]).append("</li>");
				b.append("<li>Sizes: ").append(f.sizes[i]).append("</li>");
				b.append("</ul>");
				b.append("</body></html>");
				return b.toString();
				}
			};
	MouseAdapter mouse=		new MouseAdapter()
			{
			@Override
			public void mouseEntered(MouseEvent e)
				{
				highlitedFigure=null;
				}
			@Override
			public void mouseExited(MouseEvent e) {
				drawHigLightedFigure();
				highlitedFigure=null;
				}
			
			@Override
			public void mouseMoved(MouseEvent e)
				{
				Figure f= getFigureAt(e.getX(),e.getY());
				if(f==highlitedFigure) return;
				if(highlitedFigure!=null) drawHigLightedFigure();
				highlitedFigure= f;
				drawHigLightedFigure();
				}
			
			@Override
			public void mousePressed(MouseEvent e) {
				Figure f= getFigureAt(e.getX(),e.getY());
				if(f==null) return;
				if(!(e.isPopupTrigger() || e.isControlDown())) return;
				JPopupMenu popup= new JPopupMenu();
				JMenuItem menu= new JMenuItem(new ObjectAction<Page>(f.page,"Open "+f.page)
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try
							{
							Desktop d=Desktop.getDesktop();
							d.browse(new URI(Wikipedia.BASE+"/wiki/"+getObject().getQNameEncoded()));
							}
						catch(Exception err)
							{
							ThrowablePane.show(RevisionVisualization.this,err);
							}
						}
					});
				menu.setEnabled(Desktop.isDesktopSupported());
				popup.add(menu);
				popup.show(drawingArea, e.getX(), e.getY());
				}
			};
			
		this.drawingArea.addMouseListener(mouse);
		this.drawingArea.addMouseMotionListener(mouse);
		
		
		this.drawingArea.setToolTipText("");
		this.drawingArea.setOpaque(true);
		this.drawingArea.setBackground(Color.WHITE);
		mainPane.add(this.drawingArea,BorderLayout.CENTER);
		
		JPanel bottom= new JPanel(new FlowLayout(FlowLayout.LEADING));
		mainPane.add(bottom,BorderLayout.SOUTH);
		useRevisionInsteadOfSize= new JCheckBox("Revisions");
		bottom.add(useRevisionInsteadOfSize);
		useRevisionInsteadOfSize.addActionListener(new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				dirty=true;
				drawingArea.repaint();
				}
			});
		
		this.pageList.addListSelectionListener(new ListSelectionListener()
			{
			@Override
			public void valueChanged(ListSelectionEvent e) {
				dirty=true;
				drawingArea.repaint();
				}
			});
		this.catList.addListSelectionListener(new ListSelectionListener()
			{
			@Override
			public void valueChanged(ListSelectionEvent e) {
				dirty=true;
				drawingArea.repaint();
				}
			});
		this.drawingArea.addComponentListener(new ComponentAdapter()
			{
			@Override
			public void componentResized(ComponentEvent e) {
				dirty=true;
				drawingArea.repaint();
				}
			});
		JMenuBar bar= new JMenuBar();
		setJMenuBar(bar);
		JMenu menu= new JMenu("File");
		bar.add(menu);
		menu.add(new AbstractAction("About")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(RevisionVisualization.this, Compilation.getLabel());
				}
			});
		menu.add(new AbstractAction("About me")
		{
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(RevisionVisualization.this,"Pierre Lindenbaum PhD. "+Me.MAIL+" "+Me.WWW);
			}
		});
		menu.add(new JSeparator());
		menu.add(new AbstractAction("Save as SVG")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
				if(chooser.showSaveDialog(RevisionVisualization.this)!=JFileChooser.APPROVE_OPTION)return;
				File f= chooser.getSelectedFile();
				if(f==null ||
				   (f.exists() && JOptionPane.showConfirmDialog(
						   RevisionVisualization.this,
						   f.toString()+"exists. Overwrite ?",
						   "Overwrite ?",
						   JOptionPane.OK_CANCEL_OPTION,
						   JOptionPane.WARNING_MESSAGE,
						   null)!=JOptionPane.OK_OPTION))
					{
					return;
					}
				PreferredDirectory.setPreferredDirectory(f);
				try {
					PrintWriter out= new PrintWriter(f);
					saveAsSVG(out);
					out.flush();
					out.close();
				} catch (Exception e2) {
					ThrowablePane.show(RevisionVisualization.this, e2);
					}
				}
			});
		menu.add(new AbstractAction("Quit")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				RevisionVisualization.this.setVisible(false);
				RevisionVisualization.this.dispose();
				}
			});
		
		Collections.sort(this.figures,new Comparator<Figure>()
			{
			@Override
			public int compare(Figure o1, Figure o2) {
				return o1.revisions[header.length-1]-o2.revisions[header.length-1];
				}	
			});
		
		}
	
	private Color gradient(int index,int countElements)
		{
		return ColorUtils.between(Color.BLUE,Color.PINK,index/(double)countElements);
		}
	
	private void drawHigLightedFigure()
		{
		if(this.highlitedFigure==null || this.highlitedFigure.shape==null) return;
		Graphics2D g= Graphics2D.class.cast(this.drawingArea.getGraphics());
		Stroke old= g.getStroke();
		g.setXORMode(Color.WHITE);
		g.setStroke(new BasicStroke(3,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_BEVEL));
		g.draw(this.highlitedFigure.shape);
		g.setPaintMode();
		g.setStroke(old);
		g.dispose();
		
		}
	
	private Figure getFigureAt(int x,int y)
		{
		for(Figure f: this.figures)
			{
			if(!f.displayed || f.shape==null) continue;
			if(f.shape.contains(x,y)) return f;
			}
		return null;
		}
	
	private void saveAsSVG(PrintWriter out)
		{
		Dimension dim= this.drawingArea.getSize();
		out.println(XMLUtilities.DECLARATION_UTF8);
		out.println(SVG.DOCTYPE);
		out.print("<svg xmlns='"+SVG.NS+"'  xmlns:xlink='"+XLINK.NS+"'");
		out.print(" width='"+dim.width+"' height='"+dim.height+"' style='stroke-width:1'");
		out.print(">");
		out.print("<title>"+XMLUtilities.escape(Compilation.getName())+"</title>");
		out.print("<desc>"+XMLUtilities.escape(Compilation.getName())+" : Pierre Lindenbaum "+Me.MAIL+"</desc>");
		out.print("<rect x='0' y='0' width='"+dim.width+"' height='"+dim.height+"' style='stroke:blue; fill:white;' />");
		out.print("<g>");
		for(Figure f:this.figures)
			{
			if(!f.displayed) continue;
			out.print("<a xlink:href='"+Wikipedia.BASE+"/wiki/"+
					f.page.getQNameEncoded()+
					"' title=\'"+
					XMLUtilities.escape(f.page.getQName())+
					"'>");
			out.print("<polygon points=\"");
			boolean first=true;
			PathIterator iter= f.shape.getPathIterator(null);
			float array[]=new float[6];

			 while(!iter.isDone())
				{
			 int type=iter.currentSegment(array);
				switch(type)
					{
					case PathIterator.SEG_CLOSE:break;
					case PathIterator.SEG_LINETO:
					case PathIterator.SEG_MOVETO:
					
						
						if(!first) out.print(" ");
						out.print(array[0]+","+array[1]);
						first=false;
							
						break;
					default:System.err.println("BOUM"+type);break;
					}
				iter.next();
				}
			 out.print("\" style=\"fill:"+ ColorUtils.toRGB(f.fill) +";\"/>");
			out.print("</a>");
			}
		out.print("</g>");
		out.print("</svg>");
		out.flush();
		}
	
	private void computeShapes()
		{
		this.highlitedFigure=null;
		int drawingHeight= this.drawingArea.getHeight()-1;
		boolean useRevision= useRevisionInsteadOfSize.isSelected();
		Object array[]=this.pageList.getSelectedValues();
		HashSet<Figure> selPages= new HashSet<Figure>();
		for(Object o:array)
			{
			selPages.add(Figure.class.cast(o));
			}
		
		array=this.catList.getSelectedValues();
		HashSet<Category> selCat= new HashSet<Category>();
		for(Object o:array)
			{
			selCat.add(Category.class.cast(o));
			}
		
		int all_sizes[]=new int[header.length];
		int all_revs[]=new int[header.length];
		
		int countVisible=0;
		for(Figure f:this.figures)
			{
			f.displayed=true;
			f.shape=null;
	
			if(!selPages.isEmpty() && !selPages.contains(f))
				{
				f.displayed=false;
				continue;
				}
			
			if(!selCat.isEmpty() )
				{
				boolean ok=false;
				for(Category cat: selCat)
					{
					if(f.categories.contains(cat))
						{
						ok=true;
						break;
						}
					}
				if(!ok)
					{
					f.displayed=false;
					continue;
					}
				}
			countVisible++;

			for(int i=0;i< header.length;++i)
				{
				all_sizes[i] += f.sizes[i];
				all_revs[i] += f.revisions[i];
				}
			}
		
		this.max_of_all_y=0;
		for(int i=0;i< header.length;++i)
			{
			this.max_of_all_y=Math.max(
					(useRevision?all_revs[i]:all_sizes[i]),
					this.max_of_all_y);
			}
		
		
		int prev_y[]=new int[header.length];
		for(int i=0;i< prev_y.length;++i) prev_y[i]=0;
		double pixwin= (this.drawingArea.getWidth()/(double)header.length);
		
		int index=0;
		for(Figure f:this.figures)
			{
			f.shape=null;
			f.fill=null;
			f.pen=null;
			if(!f.displayed) continue;			
			f.fill= gradient(index,countVisible);
			f.pen=Color.YELLOW;
			++index;
			
			f.shape=new GeneralPath();
			int curr_y[]=new int[header.length];
			
			
			f.shape.moveTo(0,
					drawingHeight- (prev_y[0]/this.max_of_all_y)*drawingHeight
					);
			for(int i=1;i< header.length;++i)
				{ 
				f.shape.lineTo(i*pixwin,
						drawingHeight- (prev_y[i]/this.max_of_all_y)*drawingHeight
					);
				}
			
			for(int i= header.length-1;i>=0;--i)
				{
				curr_y[i] = prev_y[i]+(useRevision?f.revisions[i]:f.sizes[i]);
				
				f.shape.lineTo(i*pixwin,
						drawingHeight- (curr_y[i]/max_of_all_y)*drawingHeight
						);
				}
			f.shape.closePath();
			prev_y=curr_y;
			}

		dirty=false;
		}
	
	private void paintDrawingArea(Graphics2D g)
		{
		if(dirty)
			{
			offscreen=null;
			computeShapes();
			}
		if(offscreen==null || offscreen.getWidth()!=this.drawingArea.getWidth() ||
				offscreen.getHeight()!=this.drawingArea.getHeight())
			{
			this.highlitedFigure=null;
			offscreen= new BufferedImage(this.drawingArea.getWidth(),this.drawingArea.getHeight(),BufferedImage.TYPE_INT_RGB);
			Graphics2D g2= offscreen.createGraphics();
			//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(this.drawingArea.getBackground());
			g2.fillRect(0, 0, this.drawingArea.getWidth(), this.drawingArea.getHeight());
			int font_size=9;
			g2.setColor(Color.PINK);
			g2.setFont(new Font("Courier",Font.PLAIN,font_size));
			for(int i=0;i < header.length;++i)
				{
				int x=(int)( (this.drawingArea.getWidth()/(double)header.length)*i);
				g2.drawLine(x, 0, x, this.drawingArea.getHeight());
				AffineTransform old= g2.getTransform();
				AffineTransform tr= new AffineTransform(old);
				
				tr.translate(x, font_size);
				tr.rotate(Math.PI/2);
				g2.setTransform(tr);
				
				g2.drawString(header[i],0,0);
				
				g2.setTransform(old);
				}
			
			g2.setColor(Color.LIGHT_GRAY);
			for(int i=1;i<6;++i)
				{
				int H=(this.drawingArea.getHeight()-1);
				int y=(int) (H- (H/6.0)*i);
				float v=(float)( (max_of_all_y/6.0)*i);
				g2.drawString( String.valueOf(v),font_size,y-font_size);
				g2.drawLine(0, y, this.drawingArea.getWidth(), y);
				}
			
			for(Figure f: this.figures)
				{
				if(!f.displayed) continue;
				g2.setColor(f.fill);
				g2.fill(f.shape);
				g2.setXORMode(f.fill);
				g2.draw(f.shape);
				g2.setPaintMode();
				}
			g2.dispose();
			}
		
		g.drawImage(this.offscreen,0,0,this.drawingArea);
		}
	
		
	public static void main(String[] args) {
	try {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD. "+Me.MAIL);
				System.err.println(Compilation.getLabel());
				System.err.println("-h this screen");
				return;
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
	    
	    RevisionVisualization frame=null;
	    if(optind==args.length)
	    	{
	    	JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
	    	if(chooser.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION) return;
	    	File f= chooser.getSelectedFile();
	    	BufferedReader r= new BufferedReader(new FileReader(f));
	    	frame=new RevisionVisualization(r);
	    	r.close();
	    	PreferredDirectory.setPreferredDirectory(f);
	    	}
	    else if(optind+1==args.length)
	    	{
	    	BufferedReader r= new BufferedReader(new FileReader(args[optind++]));
	    	frame=new RevisionVisualization(r);
	    	r.close();
	    	}
	    else
	    	{
	    	System.err.println("Illegal number of arguments");
	    	return;
	    	}
	    SwingUtils.center(frame, 100,100);
		SwingUtils.show(frame);
	} catch (Exception e) {
		ThrowablePane.show(null, e);
	}
}
		
		

}
