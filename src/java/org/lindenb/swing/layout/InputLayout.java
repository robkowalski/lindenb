/**
 * 
 */
package org.lindenb.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;



/**
 * @author lindenb
 *
 */
public class InputLayout implements LayoutManager
	{
	private int hgap;
	private int vgap;
	
	/**
	 *  SimpleInputLayout
	 *
	 */
	public InputLayout()
		{
		this(5,5);
		}
	
	/**
	 *  SimpleInputLayout
	 *
	 */
	public InputLayout(int hgap,int vgap)
		{
		setHgap(hgap);
		setVgap(vgap);
		}
	
    /**
     * Gets the horizontal gap between components
     * and between the components and the borders
     * of the <code>Container</code>
     *
     * @return     the horizontal gap between components
     *             and between the components and the borders
     *             of the <code>Container</code>
     * @see        java.awt.FlowLayout#setHgap
     * @since      JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * Sets the horizontal gap between components and
     * between the components and the borders of the
     * <code>Container</code>.
     *
     * @param hgap the horizontal gap between components
     *             and between the components and the borders
     *             of the <code>Container</code>
     * @see        java.awt.FlowLayout#getHgap
     * @since      JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * Gets the vertical gap between components and
     * between the components and the borders of the
     * <code>Container</code>.
     *
     * @return     the vertical gap between components
     *             and between the components and the borders
     *             of the <code>Container</code>
     * @see        java.awt.FlowLayout#setVgap
     * @since      JDK1.1
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * Sets the vertical gap between components and between
     * the components and the borders of the <code>Container</code>.
     *
     * @param vgap the vertical gap between components
     *             and between the components and the borders
     *             of the <code>Container</code>
     * @see        java.awt.FlowLayout#getVgap
     * @since      JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

	
	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	public void addLayoutComponent(String name, Component comp) {
		//nothing todo

	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container target)
		{
		synchronized (target.getTreeLock()) {
	         
		  	int maxLeft=0;
		  	int maxRight=0;
		  	int nmembers = target.getComponentCount();
		  	
		  	for (int i = 0 ; i < nmembers ; i+=2)
	  			{
		  		Dimension d1 = target.getComponent(i).getMinimumSize();
		  		maxLeft = Math.max(maxLeft, d1.width);
	  			}
		  	
		  	for (int i = 1 ; i < nmembers ; i+=2)
				{
		  		Dimension d1 = target.getComponent(i).getPreferredSize();
		  		maxRight = Math.max(maxRight, d1.width);
				}
		  	Insets insets= target.getInsets();
		  	int rightWidth  = target.getWidth()-(3*getHgap()+maxLeft+insets.left + insets.right);
  			if(rightWidth<=0) rightWidth=1;
		  	
		  	int y= insets.top;
		  	int prevy=0;


		  	for (int i = 0 ; i < nmembers ; i++)
		  		{
		  		Component c= target.getComponent(i);
		  		
		  		if(i%2==0)
		  			{
		  			Dimension d1 = c.getMinimumSize();
		  			y +=getVgap();
		  			prevy =y;
		  			
		  			c.setBounds(getHgap(), y, maxLeft , d1.height);
		  			y+=d1.height;
		  
		  			}
		  		else
		  			{
		  			Dimension d1 = c.getPreferredSize();
		  			c.setBounds(maxLeft+getHgap()*2, prevy, rightWidth, d1.height);
		  			y = Math.max(y, prevy+d1.height);
		  			}
		  	    }
		  


		        }
		
		}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container target) {
	      synchronized (target.getTreeLock()) {
	         
	  	
	  	int nmembers = target.getComponentCount();
	  	int prevy=0;
	  	int maxLeft=0;
	  	int maxRight=0;
	  	
	  	for (int i = 0 ; i < nmembers ; i+=2)
  			{
	  		Dimension d1 = target.getComponent(i).getMinimumSize();
	  		maxLeft = Math.max(maxLeft, d1.width);
  			}
	  	
	  	for (int i = 1 ; i < nmembers ; i+=2)
			{
	  		Dimension d1 = target.getComponent(i).getMinimumSize();
	  		maxRight = Math.max(maxRight, d1.width);
			}
	  	
	  	Dimension dim = new Dimension(maxLeft+getHgap()+maxRight,0);
	  	
	  	for (int i = 0 ; i < nmembers ; i++)
	  		{
	  		Dimension d1 = target.getComponent(i).getMinimumSize();
	  		if(i%2==0)
	  			{
	  			if(i!=0) dim.height +=getVgap();
	  			prevy =dim.height;
	  			dim.height =dim.height+d1.height;
	  			}
	  		else
	  			{
	  			dim.height= Math.max(dim.height, prevy+d1.height);
	  			}
	  	    }
	  



		  	Insets insets = target.getInsets();
		  	dim.width += insets.left + insets.right + getHgap()*2;
		  	dim.height += insets.top + insets.bottom + getVgap()*2;
		  	return dim;
	        }
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

}
