/**
 * 
 */
package org.lindenb.swing;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * @author pierre
 *
 */
public abstract class ObjectAction<T> extends AbstractAction {
	private T object;
	/**
	 * ObjectAction
	 */
	public ObjectAction(T object)
		{
		this.object=object;
		}

	/**
	 * @param name
	 */
	public ObjectAction(T object,String name) {
		super(name);
		this.object=object;
		}

	/**
	 * @param name
	 * @param icon
	 */
	public ObjectAction(T object,String name, Icon icon) {
		super(name, icon);
		this.object=object;
	}

	
	public T getObject()
		{
		return this.object;
		}
}
