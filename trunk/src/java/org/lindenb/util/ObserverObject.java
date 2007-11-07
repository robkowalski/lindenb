/**
 * 
 */
package org.lindenb.util;

import java.util.Observable;
import java.util.Observer;

/**
 * @author lindenb
 *
 */
public abstract class ObserverObject<T> implements Observer {
	private T object;
	
	
	public ObserverObject(T object)
		{	
		this.object=object;
		}
	
	public T getObject() {
		return object;
		}
	

	@Override
	public abstract void update(Observable o, Object arg);
}
