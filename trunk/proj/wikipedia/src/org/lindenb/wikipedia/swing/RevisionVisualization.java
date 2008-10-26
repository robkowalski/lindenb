package org.lindenb.wikipedia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lindenb.awt.ColorUtils;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;
import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.Page;


public class RevisionVisualization extends JFrame
	{
	private static Color GRADIENT[]=new Color[]
        {
		Color.CYAN,
		Color.ORANGE,
		Color.GREEN,
		Color.YELLOW,
		Color.BLUE,
		Color.PINK
        };
	private static final long serialVersionUID = 1L;

	private JPanel drawingArea;
	private JList pageList;
	private JList catList;
	private boolean dirty=true;
	
	static private Color gradient(int index,int count)
		{
		float colorIndex= (((float)count)/index)*GRADIENT.length;
		Color c1= GRADIENT[(int)colorIndex];
		Color c2= GRADIENT[1+(int)colorIndex];
		float ratio= colorIndex-(int)colorIndex;
		return new Color(
				c1.getRed()+(int)((c2.getRed()-c1.getRed())*ratio),
				c1.getGreen()+(int)((c2.getGreen()-c1.getGreen())*ratio),
				c1.getBlue()+(int)((c2.getBlue()-c1.getBlue())*ratio)
			);
		}
	
	private static class Figure
		{
		Page page;
		Set<Category> categories=new HashSet<Category>();
		int userCount=0;
		int revisionCount=0;
		int sizes[];
		int revisions[];
		boolean displayed=true;
		private GeneralPath shape=null;
		private Color fill=null;
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
	private Set<Figure> visible= new HashSet<Figure>();
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
		
		this.pageList=new JList(figures);
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
			public String getToolTipText(MouseEvent event) {
				Figure f= getFigureAt(event.getX(),event.getY());
				if(f==null) return null;
				return f.page.toString();
				}
			};
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
				Debug.debug();
				dirty=true;
				drawingArea.repaint();
				}
			});
		
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
	
	private void computeShapes()
		{
		Debug.debug();
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
		double max_xxx=0;
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
				countVisible++;
				}
			
			for(int i=0;i< header.length;++i)
				{
				all_sizes[i] += f.sizes[i];
				all_revs[i] += f.revisions[i];
				if(i>0) all_revs[i]+=all_revs[i-1];
				if(!useRevision)
					{
					max_xxx=Math.max(all_sizes[i],max_xxx);
					}
				else
					{
					max_xxx=Math.max(all_revs[i],max_xxx);
					}
				}
			}
		
		int prev_y[]=new int[header.length];
		for(int i=0;i< prev_y.length;++i) prev_y[i]=0;
		double pixwin= (this.drawingArea.getWidth()/(double)header.length);
		
		int index=0;
		for(Figure f:this.figures)
			{
			f.shape=null;
			if(!f.displayed) continue;			
			f.fill= GRADIENT[index%GRADIENT.length];
			++index;
			
			f.shape=new GeneralPath();
			int curr_y[]=new int[header.length];
			
			
			f.shape.moveTo(0,
					drawingHeight- (prev_y[0]/max_xxx)*drawingHeight
					);
			for(int i=1;i< header.length;++i)
				{ 
				f.shape.lineTo(i*pixwin,
						drawingHeight- (prev_y[i]/max_xxx)*drawingHeight
					);
				}
			
			for(int i= header.length-1;i>=0;--i)
				{
				curr_y[i] = prev_y[i]+f.sizes[i];
				if(i>0) curr_y[i]+=f.sizes[i-1];
				f.shape.lineTo(i*pixwin,
						drawingHeight- (curr_y[i]/max_xxx)*drawingHeight
						);
				}
			f.shape.closePath();
			prev_y=curr_y;
			}
		Debug.debug();
		dirty=false;
		}
	
	private void paintDrawingArea(Graphics2D g)
		{
		
		if(dirty) computeShapes();

	
		for(Figure f: this.figures)
			{
			if(!f.displayed) continue;
			g.setColor(f.fill);
			g.fill(f.shape);
			}
		
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			Debug.setDebugging(true);
			BufferedReader r= new BufferedReader(new FileReader(new File("/home/pierre/jeter2.txt")));
			RevisionVisualization frame=new RevisionVisualization(r);
			r.close();
			SwingUtils.center(frame, 200,200);
			SwingUtils.show(frame);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
