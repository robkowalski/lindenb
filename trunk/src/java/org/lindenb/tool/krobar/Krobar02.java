/** Basic template for a simple jaav gui program */
package org.lindenb.tool.krobar;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.net.URL;
import java.util.EventObject;
import java.util.Observable;
import java.util.Observer;


import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.lindenb.awt.Cap;
import org.lindenb.awt.ColorUtils;
import org.lindenb.awt.Join;
import org.lindenb.lang.RunnableObject;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.swing.FileExtensionFilter;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;
import org.lindenb.util.NamedKey;
import org.lindenb.util.Observed;
import org.lindenb.util.Pair;



/**
 * @author pierre
 *
 */
public class Krobar02 extends JFrame
	{
	private static final long serialVersionUID = 1L;
	private Model model;
	private JPanel drawingArea;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private Image offscreenBackground=null;
	private Dimension offscreenSize=null;

	
	private HandTool handTool= new HandTool();
	private ZoomTool zoomTool= new ZoomTool();
	private DrawTool drawTool= new DrawTool();
	private LayerTool layerTool= new LayerTool();
	private BackgroundImageTool bckgTool= new BackgroundImageTool();
	private Tool tools[]=new Tool[]{bckgTool,layerTool,handTool,zoomTool,drawTool};
	private Tool currentTool=null;
	private JDialog floatingFrame=null;
	private JPanel cardPane;
	private CardLayout cardLayout;
	private JLabel infoLabel;
	private JCheckBoxMenuItem checkAntiliasing;
	
	public static class SpinnerEditor extends AbstractCellEditor
    implements TableCellEditor
    	{
		private static final long serialVersionUID = 1L;
		private JSpinner spinner;

		// Initializes the spinner.
		public SpinnerEditor(JSpinner spinner)
			{
		    this.spinner=spinner;
			}

		// Prepares the spinner component and returns it.
		public Component getTableCellEditorComponent(JTable table, Object value,
		        boolean isSelected, int row, int column) {
		    spinner.setValue(value);
		    return spinner;
		}

		// Enables the editor only for double-clicks.
		public boolean isCellEditable(EventObject evt) {
			
		    if (evt instanceof MouseEvent) {
		        return ((MouseEvent)evt).getClickCount() > 1;
		    }
		    return true;
		}

	// Returns the spinners current value.
		public Object getCellEditorValue() {
		    return spinner.getValue();
		}
    	}
	
	
	/**
	 * Tool
	 * @author pierre
	 *
	 */
	private class Tool extends AbstractAction
		implements MouseListener,MouseMotionListener,KeyListener
		{
		private static final long serialVersionUID = 1L;
		protected Point mouseStart=null;
		protected Point prevMouse=null;
		protected Tool(String name)
			{
			super(name);
			putValue(AbstractAction.SHORT_DESCRIPTION, name);
			int c=getKeyStroke();
			if(c!=-1) putValue(AbstractAction.ACCELERATOR_KEY,KeyStroke.getKeyStroke((char)c));
			}
		
		protected int getKeyStroke()
			{
			return -1;
			}
		
		protected Rectangle createRectangle(java.awt.Point p1, java.awt.Point p2)
			{
			return new Rectangle(
					Math.min(p1.x, p2.x),
					Math.min(p1.y, p2.y),
					Math.abs(p1.x-p2.x),
					Math.abs(p1.y-p2.y)
					);
			}
	
		protected void drawRect(Graphics2D g,java.awt.Point p1, java.awt.Point p2)
			{
			g.draw(createRectangle(p1,p2));
			}
		
		@Override
		public void keyPressed(KeyEvent e) {
			
			}
		
		@Override
		public void keyReleased(KeyEvent e) {
			
			}  
		@Override
		public void keyTyped(KeyEvent e) {
			
			}
		
		public void initTool()
			{
			
			}
		
		void install()
			{
			//this.putValue(AbstractAction.NAME,"TOTO");
			}
		
		void uninstall()
			{
			//this.putValue(AbstractAction.NAME,"TITI");
			}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
			}
		@Override
		public void mouseEntered(MouseEvent e)
			{
			}
		@Override
		public void mouseExited(MouseEvent e)
			{
			}
		@Override
		public void mousePressed(MouseEvent e)
			{
			}
		@Override
		public void mouseDragged(MouseEvent e)
			{
			}
		@Override
		public void mouseReleased(MouseEvent e)
			{
			}
		
		@Override
		public void mouseMoved(MouseEvent e)
			{
			}
		
		public Krobar02 getKrobar()
			{
			return Krobar02.this;
			}
		
		public Model getModel()
			{
			return getKrobar().getModel();
			}
		
		public JPanel getDrawingArea()
			{
			return getKrobar().getDrawingArea();
			}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			getKrobar().setTool(this);
			}
		}
	/**
	 * BackgroundImageTool
	 * @author pierre
	 *
	 */
	private class BackgroundImageTool extends Tool
		{
		private static final long serialVersionUID = 1L;
		/** position de l'image dans le modele */
		private Point2D.Double modelXY=null;
		/** l'image */
		private BufferedImage backgroundImage=null;
		/** BackgroundImageTool */
		BackgroundImageTool()
			{
			super("BckgImage");
			}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(this.backgroundImage==null) return;
			double d= zoomTool.zoom.getValue();
			if(e.isShiftDown()) d*=5;
			switch(e.getKeyCode())
				{
				case KeyEvent.VK_KP_LEFT:modelXY.x-=d; imageLocationChanged(); System.err.println("x-="+d);break;
				case KeyEvent.VK_KP_RIGHT:modelXY.x+=d; imageLocationChanged(); break;
				case KeyEvent.VK_KP_UP:modelXY.y-=d; imageLocationChanged(); break;
				case KeyEvent.VK_KP_DOWN:modelXY.y+=d; imageLocationChanged(); break;
				default:break;
				}
			
			}
		
		
		void setBbackgroundImage(BufferedImage img)
			{
			this.backgroundImage=img;
			this.modelXY=(img==null?null:new Point2D.Double());
			imageLocationChanged();
			}
		
		@Override
		public void initTool() {
			super.initTool();
			JPanel pane=new JPanel(new BorderLayout());
			JPanel pane2= new JPanel(new FlowLayout(FlowLayout.LEADING));
			pane.add(pane2,BorderLayout.SOUTH);
			pane2.add(new JButton(new AbstractAction("None")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e)
					{
					setBbackgroundImage(null);
					}
				}));
			pane2.add(new JButton(new AbstractAction("Open File...")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e)
					{
					try {
						JFileChooser chooser= new JFileChooser();
						chooser.setFileFilter(FileExtensionFilter.createImageFilter());
						if(chooser.showOpenDialog(Krobar02.this)!=JFileChooser.APPROVE_OPTION) return;
						BufferedImage img= ImageIO.read(chooser.getSelectedFile());
						setBbackgroundImage(img);
						} 
					catch (Exception er)
						{
						ThrowablePane.show(getKrobar(),er);
						setBbackgroundImage(null);
						}
					}
				}));
			pane2.add(new JButton(new AbstractAction("Open URL...")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e)
					{
					String value=JOptionPane.showInputDialog(getKrobar(), "Input an URL", "URL", JOptionPane.QUESTION_MESSAGE);
					if(value==null || value.trim().length()==0 ) return;
					
					try {
						URL url= new URL(value.trim());
						BufferedImage img= ImageIO.read(url);
						setBbackgroundImage(img);
						} 
					catch (Exception er)
						{
						ThrowablePane.show(getKrobar(),er);
						setBbackgroundImage(null);
						}
					}
				}));
			getKrobar().cardPane.add(pane,"BCKGIMAGE");
			}
		
		@Override
		void install() {
			super.install();
			getKrobar().cardLayout.show(Krobar02.this.cardPane, "BCKGIMAGE");
			}
		
		@Override
		public void mousePressed(MouseEvent e)
			{
			prevMouse=null;
			getDrawingArea().requestFocus();
			if(this.backgroundImage==null) return;
			Rectangle2D.Double r= new Rectangle2D.Double(this.modelXY.x,this.modelXY.y,
				this.backgroundImage.getWidth(),
				this.backgroundImage.getHeight()
				);
			if(!r.contains(
					convertViewXToModel(e.getX())
					,convertViewYToModel(e.getY()))
					) return;
			prevMouse=new Point(e.getX(),e.getY());
			}
		@Override
		public void mouseDragged(MouseEvent e) {
			if(prevMouse==null) return;
			int dx= e.getX()-prevMouse.x;
			int dy= e.getY()-prevMouse.y;
			this.modelXY.x+=(dx*getKrobar().zoomTool.zoom.getValue());
			this.modelXY.y+=(dy*getKrobar().zoomTool.zoom.getValue());
			prevMouse=new Point(e.getX(),e.getY());
			imageLocationChanged();
			}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(prevMouse==null) return;
			imageLocationChanged();
			}
		
		private void imageLocationChanged()
			{
			getKrobar().resetOffscreenBackground();
			getKrobar().adjustScrollBar();
			getDrawingArea().repaint();
			}
		
		}
	
	/**
	 * LayerTool
	 *
	 */
	private class LayerTool extends Tool
		{
		private static final long serialVersionUID = 1L;
		private AbstractTableModel tableModel= new AbstractTableModel()
			{
			private static final long serialVersionUID = 1L;
			@Override
			public String getColumnName(int columnIndex) {
				switch(columnIndex)
					{
					case 0: return "Idx";
					case 1: return "Vis";
					case 2: return "Alpha";
					case 3: return "Count";
					}
				return null;
				}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex)
					{
					case 0: return Integer.class;
					case 1: return Boolean.class;
					case 2: return Double.class;
					case 3: return Integer.class;
					}
				return Object.class;
				}
			@Override
			public int getRowCount() {
				return getModel().layers().size();
				}
			@Override
			public int getColumnCount() {
				return 4;
				}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return  columnIndex==2;
				}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Layer L= getModel().layers().elementAt(rowIndex);
				
				switch(columnIndex)
					{
					case 0: return (rowIndex+1);
					case 1: return rowIndex< getCurrentLayerIndex();
					case 2: return L.getOpacity();
					case 3: return L.figures().size();
					}
				return null;
				}
			@Override
			public void setValueAt(Object value, int rowIndex,
					int columnIndex) {
				Layer L= getModel().layers().elementAt(rowIndex);
				switch(columnIndex)
					{
					case 2:
						if(value instanceof Number)
							{
							L.setOpacity(Number.class.cast(value).floatValue());
							layerChanged();
							}
						 break;
						}
				}
			};
		private JTable table= new JTable(this.tableModel);
		private JCheckBox  layerBelowSlave;
		private JComboBox  layersBelowDisplay;
		private JCheckBox  layersUpAsWireFrame;
		private AbstractAction moveUp;
		private AbstractAction moveDown;
		
		LayerTool()
			{
			super("Layer");
			this.layerBelowSlave= new JCheckBox("Layer Below Slave",true);
			this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.table.getColumnModel().getColumn(2).setCellEditor(new SpinnerEditor(
				new JSpinner(new SpinnerNumberModel(1.0,0.0,1.0,0.01))
				));	
			this.layersBelowDisplay= new JComboBox(new String[]{"Show Below","Wire Frame Below","HideBelow"});
			this.layersBelowDisplay.setSelectedIndex(0);
			this.layersUpAsWireFrame= new JCheckBox("Wire Frame Up");
			this.moveUp= new AbstractAction("Move Up")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					int n= table.getSelectedRow();
					if(n==-1 || n+1>=getModel().layers().size()) return;
					Layer L1= getModel().layers().elementAt(n);
					Layer L2= getModel().layers().elementAt(n+1);
					getModel().layers().setElementAt(L2,n);
					getModel().layers().setElementAt(L1,n+1);
					tableModel.fireTableRowsUpdated(n, n+1);
					table.getSelectionModel().setSelectionInterval(n+1, n+1);
					layerChanged();
					}
				};
			this.moveDown= new AbstractAction("Move Up")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					int n= table.getSelectedRow();
					if(n<=0) return;
					Layer L1= getModel().layers().elementAt(n);
					Layer L2= getModel().layers().elementAt(n-1);
					getModel().layers().setElementAt(L2,n);
					getModel().layers().setElementAt(L1,n-1);
					tableModel.fireTableRowsUpdated(n-1, n);
					table.getSelectionModel().setSelectionInterval(n-1, n-1);
					layerChanged();
					}
				};
			}
		
		
		void layerChanged()
			{
			getKrobar().resetOffscreenBackground();
			getDrawingArea().repaint();
			}
		
		
		
		int getCurrentLayerIndex()
			{
			int i = this.table.getSelectedRow();
			if(i!=-1) i= this.table.convertRowIndexToModel(i);
			if(i==-1) i= getModel().layers().size()-1;
			return i;
			}
		
		Layer getCurrentLatyer()
			{
			return getModel().layers().elementAt(getCurrentLayerIndex());
			}
		
		@Override
		public void initTool()
			{
			super.initTool();
			JPanel pane= new JPanel(new BorderLayout());
			pane.add(new JScrollPane(this.table),BorderLayout.CENTER);
			
			getKrobar().cardPane.add(pane,"LAYER");
			
			JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
			pane.add(top,BorderLayout.NORTH);
			top.add(this.layerBelowSlave);
			top.add(this.layersBelowDisplay);
			top.add(this.layersUpAsWireFrame);
			
			JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
			pane.add(bot,BorderLayout.SOUTH);

			bot.add(new JButton(new AbstractAction("New")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					Layer l= new Layer(getModel());
					getModel().layers().add(l);
					int i= getModel().layers().size()-1;
					tableModel.fireTableRowsInserted(i, i);
					table.getSelectionModel().setSelectionInterval(i, i);
					layerChanged();
					}
				}));
			bot.add(new JButton(this.moveDown));
			bot.add(new JButton(this.moveUp));
			
			
			
			int b= getModel().layers().size()-1;
			this.table.getSelectionModel().setSelectionInterval(b, b);
			this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e)
					{
					if(table.getSelectedRow()==-1)
						{
						int i= getModel().layers().size()-1;
						table.getSelectionModel().setSelectionInterval(i, i);
						return;
						}
					for(int i=0;i< tableModel.getRowCount();++i)
						{
						tableModel.fireTableCellUpdated(i, 1);
						}
					moveDown.setEnabled(table.getSelectedRow()>0);
					moveUp.setEnabled(table.getSelectedRow()+1< tableModel.getRowCount());
					layerChanged();
					}
				});
			
			ItemListener listen=new ItemListener()
				{
				@Override
				public void itemStateChanged(ItemEvent e)
					{
					getKrobar().resetOffscreenBackground();
					getKrobar().getDrawingArea().repaint();
					}
				};
			
			this.layersUpAsWireFrame.addItemListener(listen);
			this.layersBelowDisplay.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					getKrobar().resetOffscreenBackground();
					getKrobar().getDrawingArea().repaint();
					}
				});
			}
		
		@Override
		void install() {
			super.install();
			getKrobar().cardLayout.show(Krobar02.this.cardPane, "LAYER");
			getDrawingArea().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		
		@Override
		void uninstall() {
			getDrawingArea().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			super.uninstall();
			}
		
		@Override
		public void mousePressed(MouseEvent e)
			{
			prevMouse=null;
			getDrawingArea().requestFocus();
			}
		
		@Override
		public void mouseDragged(MouseEvent e)
			{
			if(prevMouse!=null)
				{
				double zoom= getKrobar().zoomTool.zoom.getValue();
				double dx= (e.getX()-prevMouse.x)*zoom;
				double dy= (e.getY()-prevMouse.y)*zoom;
				moveLayerRelative(dx,dy);
				}
			prevMouse=new Point(e.getX(),e.getY());
			
			}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(prevMouse==null) return;
			prevMouse=null;
			layerLocationChanged();
			}
		
		private void moveLayerRelative(double dx,double dy)
			{
			Layer current= getKrobar().layerTool.getCurrentLatyer();
			AffineTransform tr=AffineTransform.getTranslateInstance(dx, dy);
			if(!layerBelowSlave.isSelected())
				{
				for(Figure f: current.figures())
					{
					f.getPath().transform(tr);
					}
				}
			else
				{
				for(int i=0;i<= getKrobar().layerTool.getCurrentLayerIndex();++i)
					{
					Layer L= getModel().layers().elementAt(i);
					for(Figure f: L.figures())
						{
						f.getPath().transform(tr);
						}
					}
				}
			layerLocationChanged();
			}
		
		private void layerLocationChanged()
			{
			getKrobar().resetOffscreenBackground();
			getKrobar().adjustScrollBar();
			getDrawingArea().repaint();
			}
		
		}
	
	private class HandTool extends Tool
		{
		private static final long serialVersionUID = 1L;
		HandTool()
			{
			super("Hand");
			}

		@Override
		public void mousePressed(MouseEvent e) {
			prevMouse= new Point(e.getX(),e.getY());
			getDrawingArea().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			}
		@Override
		public void mouseDragged(MouseEvent e) {
			int d= -(e.getX()-prevMouse.x);
			int v=getKrobar().hScrollBar.getValue();
			if(!(v+d<0 ||
			   v+d+getKrobar().hScrollBar.getBlockIncrement()>Krobar02.this.hScrollBar.getMaximum()))
				{
				getKrobar().hScrollBar.setValue(v+d);
				}
			
			
			d= -(e.getY()-prevMouse.y);
			v=Krobar02.this.vScrollBar.getValue();
			if(!(v+d<0 ||
			   v+d+getKrobar().vScrollBar.getBlockIncrement()>Krobar02.this.vScrollBar.getMaximum()))
				{
				getKrobar().vScrollBar.setValue(v+d);
				}
			prevMouse= new Point(e.getX(),e.getY());
			}
		@Override
		public void mouseReleased(MouseEvent e) {
			getDrawingArea().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	
	
	private class ZoomTool extends Tool
		{
		private static final long serialVersionUID = 1L;
		private Observed<Double> zoom=new Observed<Double>(1.0);
		
		ZoomTool()
			{
			super("zoom");
			this.zoom.addObserver(new Observer()
				{
				@Override
				public void update(Observable o, Object arg) {
					getKrobar().adjustScrollBar();
					getKrobar().resetOffscreenBackground();

					getDrawingArea().repaint();
					}
				});
			}
		@Override
		public void initTool() {
			Box box= Box.createVerticalBox();
			box.add(new JButton(new AbstractAction("1/1")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					zoom.setValue(1.0);
					}
				}));
			box.add(new JButton(new AbstractAction("Out")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomIt(1.05);
					}
				}));
			box.add(new JButton(new AbstractAction("In")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomIt(0.95);
					}
				}));
			getKrobar().cardPane.add(box,"ZOOM");
			}
		@Override
		void install() {
			super.install();
			getKrobar().cardLayout.show(getKrobar().cardPane, "ZOOM");
			getKrobar().drawingArea.setCursor( Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		
		@Override
		void uninstall() {
			getKrobar().drawingArea.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			super.uninstall();
			}
		
		@Override
		public void mousePressed(MouseEvent e) {
			prevMouse= new Point(e.getX(),e.getY());
			}
		
		@Override
		public void mouseDragged(MouseEvent e)
			{
			int cx= getDrawingArea().getWidth()/2;
			int cy= getDrawingArea().getHeight()/2;
			
			
			Point now= new Point(e.getX(),e.getY());
			double d0= prevMouse.distance(cx,cy);
			double d1= now.distance(cx,cy);
			if(d0!=0)
				{
				//this.zoomIt(d1/d0);
				}
			prevMouse= now;
			}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()>1)
				{
				zoomIt(e.isShiftDown()?0.5:2.0);
				}
			}
		private void zoomIt(double coef)
			{
			this.zoom.setValue(this.zoom.getValue()*coef);
			getKrobar().hScrollBar.setMaximum((int)(hScrollBar.getMaximum()*coef));
			getKrobar().vScrollBar.setMaximum((int)(vScrollBar.getMaximum()*coef));
			
			getKrobar().hScrollBar.setValue((int)(hScrollBar.getValue()*coef));
			getKrobar().vScrollBar.setValue((int)(vScrollBar.getValue()*coef));
			}
		
		}
	
	
	/**
	 * DrawTool
	 * @author pierre
	 *
	 */
	enum DrawType {DRAW,RESIZE_CURSOR};
	private class DrawTool extends Tool
		{
		
		private static final long serialVersionUID = 1L;
		private JPanel cursorWidthPane;
		private JScrollBar opacityBar;
		private float viewRadius=1.0f;
		private JColorChooser colorChooser;
		private JComboBox capStyleCombo;
		private JComboBox joinStyleCombo;
		private GeneralPath viewPath=null;
		private GeneralPath modelPath=null;
		private DrawType drawType;
		private int button=-1;
		
		DrawTool(){ super("draw");}
		
		@Override
		public void initTool() {
			super.initTool();
			JPanel pane= new JPanel(new BorderLayout());
			getKrobar().cardPane.add(pane,"DRAW");
			JTabbedPane tabPane= new JTabbedPane();
			pane.add(tabPane,BorderLayout.CENTER);
			this.cursorWidthPane= new JPanel(null)
				{
				private static final long serialVersionUID = 1L;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					paintArea(Graphics2D.class.cast(g));
					}
				};
			this.cursorWidthPane.setOpaque(true);
			this.cursorWidthPane.setBorder(new LineBorder(Color.BLACK));
			this.cursorWidthPane.setBackground(Color.WHITE);
			tabPane.addTab("Radius",cursorWidthPane);
			
			this.colorChooser= new JColorChooser(Color.BLACK);
			this.colorChooser.getSelectionModel().addChangeListener(new ChangeListener()
				{
				@Override
				public void stateChanged(ChangeEvent e) {
					}
				});
			tabPane.addTab("Color",new JScrollPane(colorChooser));
			this.opacityBar= new JScrollBar(JScrollBar.HORIZONTAL,99,1,0,100);
			pane.add(this.opacityBar,BorderLayout.SOUTH);
			
			JPanel pane3= new JPanel();
			BoxLayout boxLayout= new BoxLayout(pane3,BoxLayout.Y_AXIS);
			pane3.setLayout(boxLayout);
			tabPane.addTab("Style",pane3);
			Object array[]=new Object[]{
				new NamedKey<Cap>(Cap.BUTT,"CAP BUTT"),
				new NamedKey<Cap>(Cap.ROUND,"CAP ROUND"),
				new NamedKey<Cap>(Cap.SQUARE,"CAP SQUARE")
				};
			this.capStyleCombo= new JComboBox(array);
			this.capStyleCombo.setSelectedIndex(1);
			pane3.add(capStyleCombo);
			
			array=new Object[]{
					new NamedKey<Join>(Join.BEVEL,"JOIN BEVEL"),
					new NamedKey<Join>(Join.ROUND,"JOIN ROUND"),
					new NamedKey<Join>(Join.MITER,"JOIN MITER")
					};
			this.joinStyleCombo= new JComboBox(array);
			this.joinStyleCombo.setSelectedIndex(1);
			pane3.add(joinStyleCombo);
			
			MouseAdapter ma= new MouseAdapter()
				{
				@Override
				public void mousePressed(MouseEvent e)
					{
					changeRadius(e.getX(),e.getY());
					}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					changeRadius(e.getX(),e.getY());
					}
				
				@Override
				public void mouseReleased(MouseEvent e) {
					changeRadius(e.getX(),e.getY());
					}
				};
			this.cursorWidthPane.addMouseListener(ma);
			this.cursorWidthPane.addMouseMotionListener(ma);
			this.opacityBar.addAdjustmentListener(new AdjustmentListener()
				{
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					cursorWidthPane.repaint();
					}
				});
			}
		
		private void changeRadius(int x,int y)
			{
			int cx= cursorWidthPane.getWidth()/2;
			int cy= cursorWidthPane.getHeight()/2;
			this.viewRadius= (float)Point.distance(cx, cy, x, y);
			this.cursorWidthPane.repaint();
			}
		
		
		private void paintArea(Graphics2D g)
			{
			for(int i=0;i< cursorWidthPane.getWidth();i+=25)
				{
				for(int j=0;j< cursorWidthPane.getHeight();j+=25)
					{
					g.setColor((i/25)%2==(j/25)%2?Color.LIGHT_GRAY:Color.WHITE);
					g.fillRect(i, j, 25, 25);
					}
				}
			int x= cursorWidthPane.getWidth()/2;
			int y= cursorWidthPane.getHeight()/2;
			g.setColor(Color.BLACK);
			g.drawLine(x-5, y, x+5, y);
			g.drawLine(x, y-5, x, y+5);
			fillOval(g,x,y);
			
			}
		
		@SuppressWarnings("unchecked")
		public Join getJoinStyle()
			{
			return  ((NamedKey<Join>)this.joinStyleCombo.getSelectedItem()).getId();
			}
		@SuppressWarnings("unchecked")
		public Cap getCapStyle()
			{
			return  ((NamedKey<Cap>)this.capStyleCombo.getSelectedItem()).getId();
			}
		
		private float getAlpha()
			{
			return this.opacityBar.getValue()/100f;
			}
		
		private Color getColor()
			{
			Color c=this.colorChooser.getColor();
			return c==null?Color.BLACK:c;
			}
		
		private void fillOval(Graphics2D g,int cx,int cy)
			{
			g.setColor(getColor());
			Composite old=g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
			g.fillOval((int)(cx-viewRadius),(int)(cy-viewRadius), (int)(viewRadius*2), (int)(viewRadius*2));
			g.setComposite(old);
			}
		
		private void drawOval(Graphics2D g,int cx,int cy)
			{
			g.drawOval(
					(int)(cx-viewRadius),
					(int)(cy-viewRadius),
					(int)(viewRadius*2),
					(int)(viewRadius*2)
					);
			}
		
		
		@Override
		void install() {
			super.install();
			getKrobar().cardLayout.show(	Krobar02.this.cardPane, "DRAW");
			getKrobar().drawingArea.setCursor( Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		
		@Override
		void uninstall() {
			getKrobar().drawingArea.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			super.uninstall();
			}
		
		@Override
		public void mouseMoved(MouseEvent e)
			{
			Graphics2D g= Graphics2D.class.cast(Krobar02.this.drawingArea.getGraphics());
			g.setXORMode(Color.RED);
			if(prevMouse!=null)
				{
				drawOval(g, prevMouse.x, prevMouse.y);
				}
			prevMouse=new Point(e.getX(),e.getY());
			drawOval(g, prevMouse.x, prevMouse.y);
			g.setPaintMode();
			g.dispose();
			}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			
			}
		
		@Override
		public void mouseExited(MouseEvent e)
			{
			
			}
		
		@Override
		public void mousePressed(MouseEvent e)
			{
			this.button= e.getButton();
			switch(this.button)
				{
				case MouseEvent.BUTTON1:
					{
					this.mouseStart= new Point(e.getX(),e.getY());
					if(e.isShiftDown())
						{
						this.drawType= DrawType.RESIZE_CURSOR;
						}
					else
						{
						this.drawType= DrawType.DRAW;
						this.viewPath= new GeneralPath();
						this.modelPath= new GeneralPath();
						this.viewPath.moveTo(e.getX(),e.getY());
						this.modelPath.moveTo(
								convertViewXToModel(e.getX()),
								convertViewYToModel(e.getY())
								);
						}
					break;
					}
				case MouseEvent.BUTTON3:
					{
					grabPixel(e);
					break;
					}
				}
			}
		
		@Override
		public void mouseDragged(MouseEvent e)
			{
			switch(this.button)
				{
				case MouseEvent.BUTTON1:
					{
					Graphics2D g= Graphics2D.class.cast(drawingArea.getGraphics());
					
					switch(this.drawType)
						{
						case DRAW:
							{
							Composite oldc=g.getComposite();
							g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
							//Stroke oldstroke= g.getStroke();
							BasicStroke bs=new BasicStroke(this.viewRadius*2,
									getCapStyle().stroke(),
									getJoinStyle().stroke()
									);
							//g.setStroke(bs);
							g.setXORMode(ColorUtils.negative(getColor()));
							Shape shape = bs.createStrokedShape(viewPath);
							g.fill(shape);
							viewPath.lineTo(e.getX(),e.getY());
							modelPath.lineTo(
									convertViewXToModel(e.getX()),
									convertViewYToModel(e.getY())
									);
							shape = bs.createStrokedShape(viewPath);
							g.fill(shape);
							//g.setStroke(oldstroke);
							g.setComposite(oldc);
							prevMouse= new Point(e.getX(),e.getY());
							break;
							}
						case RESIZE_CURSOR:
							{
							float len;
							g.setXORMode(ColorUtils.negative(getColor()));
							if(this.prevMouse!=null)
								{
								len=Math.max(
									Math.abs(mouseStart.x-prevMouse.x),
									Math.abs(mouseStart.y-prevMouse.y)
									);
								g.drawOval(
										mouseStart.x-(int)len,
										mouseStart.y-(int)len,
										(int)(len*2),
										(int)(len*2)
										);
								}
							this.prevMouse= new Point(e.getX(),e.getY());
							len=Math.max(
									Math.abs(mouseStart.x-prevMouse.x),
									Math.abs(mouseStart.y-prevMouse.y)
									);
							g.drawOval(
									mouseStart.x-(int)len,
									mouseStart.y-(int)len,
									(int)(len*2),
									(int)(len*2)
									);
							break;
							}
						}
					g.dispose();
					break;
					}
				case MouseEvent.BUTTON3:
					{
					grabPixel(e);
					break;
					}
				}
			}
		
		@Override
		public void mouseReleased(MouseEvent e)
			{
			switch(this.button)
				{
				case MouseEvent.BUTTON1:
					{
					switch(this.drawType)
						{
						case DRAW:
							{
							Layer L = getKrobar().layerTool.getCurrentLatyer();
							Figure f= new Figure();
							f.setColor( getColor());
							f.setOpacity( getAlpha());
							f.setStrokeWidth( (float)((this.viewRadius/getKrobar().zoomTool.zoom.getValue())*2.0));
							f.setStrokeCap( getCapStyle());
							f.setStrokeJoin(  getJoinStyle() );
							f.setPath( modelPath );
							f.setLayer(L);
							if(e.isControlDown()) f.setFilled(true);
							L.figures().addElement(f);
							getModel().updateDimension();
							resetOffscreenBackground();
							getKrobar().adjustScrollBar();
							
							break;
							}
						case RESIZE_CURSOR:
							{
							if(this.prevMouse!=null)
								{
								double distance= this.prevMouse.distance(this.mouseStart);
								this.viewRadius= (float)distance;
								this.cursorWidthPane.repaint();
								}
							break;
							}
						}
					
					getDrawingArea().repaint();
					updateInfoLabel();
					break;
					}
				case MouseEvent.BUTTON3:
					{
					grabPixel(e);
					break;
					}
				}
			this.button=-1;
			this.prevMouse=null;
			this.modelPath=null;
			this.viewPath=null;
			}
		

		private void grabPixel(MouseEvent e)
			{
			
			getDrawingArea().repaint();
			Image pix= getKrobar().createImage(1, 1);
			Graphics g= pix.getGraphics();
			g.drawImage(getKrobar().offscreenBackground,
				0,0,1,1,
				e.getX(),e.getY(),e.getX()+1,e.getY()+1,
				null
				);
			g.dispose();
			
			PixelGrabber pixelGrab= new PixelGrabber(
				pix,
				0,0,1,1,true
				);
			try
			{
			if(pixelGrab.grabPixels())
				{
				int[] data = (int[]) pixelGrab.getPixels();
				if(data.length>0)
					{
					Color c= new Color(data[0]);
					this.colorChooser.setColor(c);
					//NON...pas beau this.opacityBar.setValue(100);
					
					}
				}

			} catch(InterruptedException err)
				{
				err.printStackTrace();
				}
			}
			
		
		}


	

	
	
	
	/** Krobar02 */
	private Krobar02(Model model)
		{
		super("Krobar02");
		this.model=model;
		JMenuBar bar= new JMenuBar();
		setJMenuBar(bar);
		JMenu menu= new JMenu("File");
		bar.add(menu);
		AbstractAction action;
		menu.add(new AbstractAction("About...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(Krobar02.this, Compilation.getLabel());
				}
			});
		menu.add(new JSeparator());
		menu.add(action=new AbstractAction("New",loadIcon("New24.gif"))
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Model m= new Model();
				Krobar02 newframe= new Krobar02(m);
				newframe.setVisible(true);
				}
			});
		
		menu.add(action=new AbstractAction("Close")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Krobar02.this.doMenuClosing();
				}
			});
		
		menu.add(action=new AbstractAction("Open",loadIcon("Open24.gif"))
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser= new JFileChooser();
				chooser.setFileFilter(new FileExtensionFilter("SVG files",".svg",".svgz",".svg.gz"));
				if(chooser.showOpenDialog(Krobar02.this)!=JFileChooser.APPROVE_OPTION) return;
				File f= chooser.getSelectedFile();
				try {
					Model newmodel= Model.parse(f);
					Krobar02 newin= new Krobar02(newmodel);
					newin.setVisible(true);
					} 
				catch (Exception err)
					{
					ThrowablePane.show(Krobar02.this, err);
					}
				}
			});
		action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		menu.add(action=new AbstractAction("Save")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuSaveAs(getModel().getFile());
				}
			});
		action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		
		menu.add(new AbstractAction("Save As...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuSaveAs(null);
				}
			});
		menu.add(new AbstractAction("Export As SVG...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuExport();
				}
			});
		
		menu= new JMenu("Edit");
		bar.add(menu);
		menu.add(action=new AbstractAction("Pop Layer")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Layer last= layerTool.getCurrentLatyer();
				if(!last.figures().isEmpty())
					{
					last.figures().removeElementAt(last.figures().size()-1);
					resetOffscreenBackground();
					adjustScrollBar();
					getDrawingArea().repaint();
					}
				else
					{
					Toolkit.getDefaultToolkit().beep();
					}
				}
			});
		action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
		menu.add(checkAntiliasing=new JCheckBoxMenuItem(action=new AbstractAction("Antialias")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e)
				{
				resetOffscreenBackground();
				getDrawingArea().repaint();
				}
			}));
		
		
		Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
		this.floatingFrame= new JDialog(this,"Tools");
		this.floatingFrame.setBounds(screen.width-200, screen.height-200, 200, 200);
		this.floatingFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.cardPane= new JPanel(this.cardLayout= new CardLayout());
		JPanel floatingPane= new JPanel(new BorderLayout());
		floatingPane.add(this.cardPane,BorderLayout.CENTER);
		this.floatingFrame.setContentPane(floatingPane);
		
		this.cardPane.add(new JPanel(),"EMPTY");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel contentPane0= new JPanel(new BorderLayout());
		JPanel contentPane= new JPanel(new BorderLayout(2,2));
		contentPane0.add(contentPane,BorderLayout.CENTER);
		setContentPane(contentPane0);
		contentPane0.setBorder(new EmptyBorder(2,2,2,2));
		JToolBar toolBar= new JToolBar();
		for(Tool t:tools)
			{
			t.initTool();
			JButton but= new JButton(t);
			but.setVerticalTextPosition(SwingConstants.BOTTOM);
			but.setHorizontalTextPosition(SwingConstants.CENTER);
			toolBar.add(but);
			}
		setTool(currentTool);
		floatingPane.add(toolBar,BorderLayout.NORTH);
		
		contentPane.add(this.vScrollBar= new JScrollBar(JScrollBar.VERTICAL),BorderLayout.EAST);
		contentPane.add(this.hScrollBar= new JScrollBar(JScrollBar.HORIZONTAL),BorderLayout.SOUTH);
		contentPane.add(this.drawingArea= new JPanel(null,true)
			{
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g1) {
				super.paintComponent(g1);
				Graphics2D g= Graphics2D.class.cast(g1);
				paintDrawingArea(g);
				}
			},BorderLayout.CENTER);
		this.drawingArea.setOpaque(true);
		this.drawingArea.setBackground(Color.WHITE);
		this.drawingArea.setFocusTraversalKeysEnabled(false);
		this.drawingArea.setFocusable(true);
		
		this.addComponentListener(new ComponentAdapter()
			{
			@Override
			public void componentResized(ComponentEvent e)
				{
				resetOffscreenBackground();
				adjustScrollBar();
				drawingArea.repaint();
				}
			});
		
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowOpened(WindowEvent e) {
				Krobar02.this.floatingFrame.setVisible(true);
				adjustScrollBar();
				getDrawingArea().requestFocus();
				}
			@Override
			public void windowClosing(WindowEvent e)
				{
				doMenuClosing();
				}
			});
		
		AdjustmentListener aL= new AdjustmentListener()
			{
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
				{
				resetOffscreenBackground();
				drawingArea.repaint();
				}
			};
		this.hScrollBar.addAdjustmentListener(aL);
		this.vScrollBar.addAdjustmentListener(aL);
		
		JPanel bot= new JPanel(new FlowLayout(FlowLayout.LEADING));
		contentPane0.add(bot,BorderLayout.SOUTH);
		bot.add(this.infoLabel=new JLabel("Info",JLabel.LEFT));
		this.infoLabel.setFont(new Font("Times",Font.PLAIN,9));
		updateInfoLabel();
		
		setTool(this.drawTool);
		SwingUtils.setFontDeep(floatingPane,new Font("Helvetica",Font.PLAIN,10));
		SwingUtils.center(this, 150);
		}
	
	private void updateInfoLabel()
		{
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		this.infoLabel.setText(String.valueOf(runtime.freeMemory()/1024)+"Ko free.");
		}
	
	private void doMenuSaveAs(File file)
		{
		if(file==null)
			{
			JFileChooser chooser= new JFileChooser();
			if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)
				{
				return;
				}
			file = chooser.getSelectedFile();
			if(file.exists() &&
					JOptionPane.showConfirmDialog(this, file.getName()+" exists ? Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null)!=JOptionPane.OK_OPTION
					)
				{
				return;
				}
			}
		
		try {
			getModel().writeSVG(file);
			getModel().setFile( file );
			} 
		catch (Exception e)
			{
			ThrowablePane.show(this, e);
			}
		}
	
	private void doMenuExport()
		{
		JFileChooser chooser= new JFileChooser();
		if(chooser.showSaveDialog(Krobar02.this)!=JFileChooser.APPROVE_OPTION)
			{
			return;
			}
		File f= chooser.getSelectedFile();
		if(f.exists() &&
			JOptionPane.showConfirmDialog(this, f.getName()+" exists ? Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null)!=JOptionPane.OK_OPTION
			)
			{
			return;
			}
		
		try {
			this.model.writeSVG(f);
		} catch (Exception e) {
			ThrowablePane.show(this, e);
			}
		
		}
	
	private Point2D.Double getTopLeftView()
		{
		return new Point2D.Double(
				this.hScrollBar.getValue(),
				this.vScrollBar.getValue()
				);
		}
	
	private double convertViewXToModel(int x)
		{
		return (x+hScrollBar.getValue())/zoomTool.zoom.getValue();
		}
	
	private double convertViewYToModel(int y)
		{
		return (y+vScrollBar.getValue())/zoomTool.zoom.getValue();
		}
	
	
	
	
	private AffineTransform getViewTransform()
		{
		Point2D.Double topLeft=getTopLeftView();
		double zoom= this.zoomTool.zoom.getValue();
		AffineTransform tr= AffineTransform.getScaleInstance(zoom,zoom);
		tr.translate(-topLeft.x/zoom, -topLeft.y/zoom);
		return tr;
		}
	
	private void paintDrawingArea(Graphics2D g1)
		{
		Rectangle visibleRect=getDrawingArea().getVisibleRect();
		if(this.offscreenBackground==null ||
				   this.offscreenSize.width!=visibleRect.width &&
				   this.offscreenSize.height!=visibleRect.height
				   )
				{
				this.offscreenSize= new Dimension(visibleRect.width,visibleRect.height);
				this.offscreenBackground=this.drawingArea.createImage(visibleRect.width,visibleRect.height);
				Graphics2D gOff= Graphics2D.class.cast(this.offscreenBackground.getGraphics());
				if(checkAntiliasing.isSelected())
					{
					gOff.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					}
				else
					{
					gOff.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
					}
				gOff.setColor(Color.WHITE);
				gOff.fillRect(0, 0, visibleRect.width, visibleRect.height);
				paintOffscreen(gOff);
				gOff.dispose();
				}
		g1.drawImage(this.offscreenBackground,
					0, 0,
					getDrawingArea());
		}
	
	private void resetOffscreenBackground()
		{
		this.offscreenBackground=null;
		}
	
	private void paintOffscreen(Graphics2D g)
		{
		Composite com=g.getComposite();
		Paint oldPaint= g.getPaint();
		AffineTransform old= g.getTransform();
		g.setTransform(getViewTransform());
		if(bckgTool.backgroundImage!=null)
			{
			g.drawImage(bckgTool.backgroundImage,
				(int)bckgTool.modelXY.x, (int)bckgTool.modelXY.y, 
				Krobar02.this.drawingArea	
				);
			}
		int currentLayer= layerTool.getCurrentLayerIndex();
		int displayBelowIndex= layerTool.layersBelowDisplay.getSelectedIndex();
		boolean wireFrameUp= layerTool.layersUpAsWireFrame.isSelected();
		for(int i=0;i< getModel().layers().size();++i)
			{
			boolean isWireFrame=false;
			if(i< currentLayer)
				{
				if(displayBelowIndex==2) continue;
				isWireFrame=(displayBelowIndex==1);
				}
			else if(i>currentLayer)
				{
				if(!wireFrameUp) continue;
				isWireFrame=true;
				}
			Layer L = getModel().layers().elementAt(i);

			if(isWireFrame)
				{
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
				g.setColor(Color.LIGHT_GRAY);
				g.setStroke(new BasicStroke((float)(1f/zoomTool.zoom.getValue())));
				}
			
			for(Figure f: L.figures())
				{
				if(!isWireFrame)
					{
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, L.getOpacity()*f.getOpacity()));
					g.setStroke(f.getStroke());
					g.setColor(f.getColor());
					/*
					Pair<Point2D.Double,Point2D.Double> pair=f.getStartEndPoints();
					Color c1= f.getColor();
					Color c2= Color.WHITE;
					g.setPaint(new LinearGradientPaint(
						pair.first(),
						pair.second(),
						new float[]{0.0f,1f},
						new Color[]{c1,c2}
						));
					System.err.println(pair+" "+c1+" "+c2);*/
					g.draw(f.getPath());
					if(f.isFilled())
						{
						g.fill(f.getPath());
						}
					}
				else
					{
					g.draw(f.getStroke().createStrokedShape(f.getPath()));
					}
				}
			}
		g.setTransform(old);
		g.setComposite(com);
		g.setPaint(oldPaint);
		}
	
	private void adjustScrollBar()
		{
		Dimension view= new Dimension((int)getModel().getWidth(),(int)getModel().getHeight());
		view.height=(int)(view.height*zoomTool.zoom.getValue());
		view.width=(int)(view.width*zoomTool.zoom.getValue());
		if(bckgTool.backgroundImage!=null)
			{
			view.width =  Math.max(view.height,bckgTool.backgroundImage.getWidth()+(int)bckgTool.modelXY.x);
			view.height=  Math.max(view.height,bckgTool.backgroundImage.getHeight()+(int)bckgTool.modelXY.y);
			}
		
		
		Rectangle visibleRect=this.drawingArea.getVisibleRect();
		int extent=visibleRect.height;
		int value= vScrollBar.getValue();
		int max=Math.max(value+extent,view.height);
		this.vScrollBar.setValues(value, extent,0, max);
		
		extent=visibleRect.width;
		value= hScrollBar.getValue();
		max=Math.max(value+extent,view.width);
		this.hScrollBar.setValues(value, extent,0, max);
		}
	
	public void doMenuClosing()
		{
		this.floatingFrame.setVisible(false);
		this.floatingFrame.dispose();
		this.setVisible(false);
		this.dispose();
		}
	
	void setTool(Tool newtool)
		{
		if(this.currentTool==newtool || newtool==null) return;
		if(this.currentTool!=null)
			{
			getDrawingArea().removeMouseListener(this.currentTool);
			getDrawingArea().removeMouseMotionListener(this.currentTool);
			getDrawingArea().removeKeyListener(this.currentTool);
			this.currentTool.uninstall();
			}
		this.currentTool=newtool;
		getDrawingArea().addMouseListener(this.currentTool);
		getDrawingArea().addMouseMotionListener(this.currentTool);
		getDrawingArea().addKeyListener(this.currentTool);
		this.currentTool.install();
		updateInfoLabel();
		}
	
	public Model getModel()
		{
		return this.model;
		}
	
	public JPanel getDrawingArea()
		{
		return this.drawingArea;
		}
	
	
	public static Icon loadIcon(String name)
		{
		try
			{
			return new ImageIcon("/images/"+name);
			}
		catch(Exception err)
			{
			return new Icon()
				{
				@Override
				public void paintIcon(Component c, Graphics g, int x, int y) {
					
					}
				@Override
				public int getIconHeight() {
					return 24;
					}
				@Override
				public int getIconWidth() {
					return 24;
					}
				};
			}
		}
	
	public static void main(String[] args)
		{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		Debug.setDebugging(false);
		
		Debug.setDebugging(true);
		try
			{
			Model m =new Model();
			SwingUtilities.invokeAndWait(new RunnableObject<Model>(m)
				{
				@Override
				public void run()
					{
					JFrame f= new Krobar02(getObject());
					f.setVisible(true);
					}
				});
			} catch(Exception err)
			{
			err.printStackTrace();
			}
		}

}
