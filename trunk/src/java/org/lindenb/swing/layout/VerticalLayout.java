package org.lindenb.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VerticalLayout
	implements LayoutManager
	{
	private int vGap=5;
	
	public VerticalLayout()
		{
		this(5);
		}
	
	public VerticalLayout(int vGap)
		{
		this.vGap=vGap;
		}
	
	public int getvGap()
		{
		return vGap;
		}
	

	@Override
	public void addLayoutComponent(String name, Component comp)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public void layoutContainer(Container parent)
		{
		_process(parent,3);
		}

	@Override
	public Dimension minimumLayoutSize(Container parent)
		{
		return _process(parent,0);
		}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
		{
		return _process(parent,1);
		}

	
	private Dimension _process(Container parent,int mode)
		{
		synchronized (parent.getTreeLock())
			{
			Insets insets= parent.getInsets();
			int hgap=insets.left+insets.right;
			Dimension dim = new Dimension(
					hgap,
					insets.top+insets.bottom
					);
			int y=insets.top;
			for(int i=0;i< parent.getComponentCount();++i)
				{
				if(i!=0)
					{
					dim.height+=getvGap();
					y+=getvGap();
					}
				Component com=  parent.getComponent(i);
				Dimension d2;
				if(mode==0)
					{
					d2=com.getMinimumSize();
					}
				else
					{
					d2=com.getPreferredSize();
					}
				if(mode==3 || d2.width+hgap> parent.getWidth())
					{
					d2.width=parent.getWidth()-hgap;
					}
				if(d2.width<=0) d2.width=1;
				
				dim.height+=d2.height;
				dim.width=Math.max(d2.width+(hgap), dim.width);
				
				if(mode==3)
					{
					
					com.setBounds(insets.left, y, d2.width, d2.height);
					y+=d2.height;
					}
				
				}
			
			return dim;
			}
		}

	
	@Override
	public void removeLayoutComponent(Component comp)
		{
		//nothing todo
		}
	
	}
