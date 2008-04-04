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
	
	/** default constructor with null value */
	public Observed() {
		this(null);
		}
	
	/** set the value of this and notify the listeners */
	public synchronized void setValue(T value)
		{
		setValue(value,true);
		}
	
	/** set the value of this
	 * @param value the new value
	 * @param notifyListeners should it notify the listeners
	 */
	public synchronized void setValue(T value,boolean notifyListeners)
		{
		this.object=value;
		if(notifyListeners)
			{
			fireValueChanged();
			}
		}
	
	/** notify all listeners that the value has changed */
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
	/** return the value. may be null */
	public synchronized T getValue() {
		return object;
		}
	
	@Override
	/** return the hascode of getValue or -1 if value is null */
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
