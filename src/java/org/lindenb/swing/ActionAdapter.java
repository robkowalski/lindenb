/**
 * 
 */
package org.lindenb.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

/**
 * @author pierre
 *
 */
public abstract class ActionAdapter extends AbstractAction
	{
	private static final long serialVersionUID = 1L;
	private List<Object> list=new ArrayList<Object>();
	/**
	 * 
	 */
	public ActionAdapter(String name) {
		super(name);
		}

	/**
	 * @param arg0
	 */
	public ActionAdapter(String name,Object...values)
		{
		this(name);
		for(Object o:values) list.add(o);
		}
	
	public int getObjectCount()
		{
		return this.list.size();
		}
	
	public Object getObject(int index)
		{
		return this.list.get(index);
		}
	
	public <T> Object getObject(Class<T> clazz,int index)
		{
		return clazz.cast(getObject(index));
		}
	
	}
