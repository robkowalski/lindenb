/**
 * 
 */
package org.lindenb.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * @author pierre
 *
 */
public class SwingUtils implements SwingConstants {
protected SwingUtils() {}

public static void center(Window window)
	{
	Dimension dim=window.getPreferredSize();
	Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
	window.setBounds(
		Math.max(0,(screen.width-dim.width)/2),
		Math.max(0,(screen.height-dim.height)/2),
		dim.width,dim.height
		);
	}

public static void center(Window window,int marginH,int marginV)
	{
	Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
	window.setBounds(
		Math.max(0,(screen.width-marginH)/2),
		Math.max(0,(screen.height-marginV)/2),
		screen.width-marginH,screen.height-marginV
		);
	}


public static void center(Window window,int margin)
	{
	center(window,margin,margin);
	}

public static void packAndCenter(Window window)
	{
	window.pack();
	center(window);
	}

public static <T extends Component> T findComponentByName(Component root,String name,Class<T> clazz)
	{
	if(clazz.isInstance(root) && name.equals(root.getName()))
		{
		return clazz.cast(root);
		}
	else if(root instanceof JComponent)
		{
		JComponent c=JComponent.class.cast(root);
		for(int i=0;i< c.getComponentCount();++i)
			{
			T t= findComponentByName(c.getComponent(i),name,clazz);
			if(t!=null) return t;
			}
		}
	return null;
	}

public static JComponent withFont(JComponent c,String fontName,int fontFace,int fontSize)
	{
	return withFont(c, new Font(fontName,fontFace,fontSize));
	}

public static JComponent withFont(JComponent c,Font font)
	{
	c.setFont(font);
	return c;
	}
}
