/**
 * 
 */
package org.lindenb.util;

import java.util.Observable;

/**
 * @author lindenb
 *
 */
public class Observed<T> extends Observable {
	private T object;
	/**
	 * ObservableObject
	 */
	public Observed(T  object)
		{
		super();
		this.object=object;
		}
	
	
	public Observed() {
		this(null);
		}
	
	public synchronized void setValue(T value)
		{
		setValue(value,true);
		}
	
	public synchronized void setValue(T value,boolean notifyListeners)
		{
		this.object=value;
		if(notifyListeners)
			{
			fireValueChanged();
			}
		}
	
	public synchronized void fireValueChanged()
		{
		setChanged();
		notifyObservers(this.object);
		clearChanged();
		}
	/** return the internal value is not null */
	public synchronized boolean hasValue()
		{
		return getValue()!=null;
		}
	
	public synchronized T getValue() {
		return object;
		}
	
	@Override
	public synchronized int hashCode() {
		return getValue()==null?-1:getValue().hashCode();
		}
	
	@Override
	public synchronized boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null || !(obj instanceof Observed<?>)) return false;
		Object o= Observed.class.cast(obj).getValue();
		if(getValue()==null)
			{
			return o==null;
			}
		return getValue().equals(o);
		}

	@Override
	public synchronized String toString() {
		return String.valueOf(getValue());
		}
	
}
