package org.lindenb.swing.layout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.lindenb.swing.SwingUtils;


/**
 * PackingLayout
 * @author lindenb
 * A,layout manager packing its children in a top/down layout of N columns
 * All columns have the same preferred size
 * The height of each component is the same as its preferred height
 *
 */
public class PackingLayout implements LayoutManager{
	private int hgap;
	private int vgap;
	private int prefWidth;
	
	/**
	 *  PackingLayout
	 *
	 */
	public PackingLayout()
		{
		this(200);
		}
	
	/**
	 *  PackingLayout
	 *
	 */
	public PackingLayout(int prefWidth)
		{
		this(prefWidth,5,5);
		}
	
	/**
	 *  PackingLayout
	 *
	 */
	public PackingLayout(int prefWidth,int hgap,int vgap)
		{
		this.prefWidth=prefWidth;
		if(prefWidth<0) throw new IllegalArgumentException("Bad width < 0 "+prefWidth);
		setHgap(hgap);
		setVgap(vgap);
		}
	
   
    public int getHgap() {
        return hgap;
    }

 
    public void setHgap(int hgap) {
    	if(hgap<0) throw new IllegalArgumentException("Bad hgap < 0 "+hgap);
        this.hgap = hgap;
    }

    public int getVgap() {
        return vgap;
    }

    public void setVgap(int vgap) {
    	if(vgap<0) throw new IllegalArgumentException("Bad vgap < 0 "+vgap);
        this.vgap = vgap;
    }

	
	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	public void addLayoutComponent(String name, Component comp) {
		//nothing todo

	}
	
	private Dimension doLayout(Container target,boolean calcMinSize)
		{
		synchronized (target.getTreeLock())
			{
			Dimension minSize= new Dimension(0,0);	
			Insets insets= target.getInsets();
	
	        //width per component
	        int width=0;
	        int ncols=0;
	        if(calcMinSize && target.getWidth()==0)
	        	{
	        	width= 0;
	        	for(int i=0;i< target.getComponentCount();++i)
	        	  {
	        	  width = Math.max(width, target.getComponent(i).getPreferredSize().width); 
	        	  }
	        	minSize.width=width;
	        	ncols=1;
	        	}
	        else
	        	{
	        	int h_space= target.getWidth() - (insets.left+ insets.right);
	        	ncols= h_space/this.prefWidth;
	        	width = (h_space  - (ncols-1)* getHgap())/ncols;
	        	
	        	}
	        if(ncols<1) ncols=1;
	        int y_array[]=new int[ncols];
	        Arrays.fill(y_array, insets.top);
	        
	        if(width<=0) width=1;
	        
	      
	        for(int i=0;i< target.getComponentCount();++i)
	        	{
	        	Dimension dim = (calcMinSize?
	        			target.getComponent(i).getPreferredSize():
	        			target.getComponent(i).getPreferredSize()
	        			)
	        			;
	        	int bestCol=-1;
	        	for(int j=0;j< y_array.length;++j)
	        		{
	        		if(bestCol==-1 || y_array[j]< y_array[bestCol])
	        			{
	        			bestCol=j;
	        			}
	        		}
	        	int x= insets.left+ + bestCol*(width+getHgap());
	        	if(!calcMinSize)
		        	{
		        	target.getComponent(i).setBounds(
		        			x, 
		        			y_array[bestCol] + (i< ncols?0:getVgap()),
		        			width, 
		        			dim.height
		        			);
		        	}
	        	
	        	y_array[bestCol]+= dim.height+getVgap();
	        	}
	        
	        for(int i=0;i< y_array.length ;++i)
	        	{
	        	y_array[i]+= (insets.bottom-getVgap());
	        	minSize.height=Math.max(y_array[i], minSize.height);
	        	
	        	}
	 
	        return minSize;
			}
		}
	
	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container target)
		{
		doLayout(target,false);
		}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container target) {
		 return doLayout(target,true);
	      }

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(Component comp) {
//		nothing todo

	}

	
	public static void main(String[] args) {
		JFrame f= new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pane= new JPanel(new PackingLayout());
		Random r= new Random();
		pane.setBorder(new EmptyBorder(r.nextInt(20),r.nextInt(20),r.nextInt(20),r.nextInt(20)));
		
		for(int i=0;i< 30; ++i)
			{
			JLabel label= new JLabel(""+i);
			label.setOpaque(true);
			int c=r.nextInt(255);
			label.setBackground(new Color(c,c,c));
			Dimension d=new Dimension(100+r.nextInt(200),100+r.nextInt(300));
			label.setPreferredSize(d);
			//label.setMinimumSize(d);
			c=r.nextInt(255);
			label.setBorder(new LineBorder(new Color(c,c,c)));
			pane.add(label);
			label.addMouseListener(new MouseAdapter()
				{
				@Override
				public void mouseClicked(MouseEvent e)
					{
					Container parent=e.getComponent().getParent();
					 parent.remove(e.getComponent());
					 parent.invalidate();
					parent.doLayout();
					}
				});
			}
		f.setContentPane(new JScrollPane(pane));
		SwingUtils.center(f,100,100);
		SwingUtils.show(f);
	}
	
}
