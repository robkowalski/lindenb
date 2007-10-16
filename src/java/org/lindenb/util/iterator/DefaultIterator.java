package org.lindenb.util.iterator;

import java.util.Enumeration;
import java.util.Iterator;


public class DefaultIterator<T> extends AbstractIterator<T> {
	/** delegate iterator */
	private Iterator<T> delegate;
	/** a free value that can be carried by this iterator*/
	private Object userData=null;
	
	/**
	 * 
	 * @param delegate 
	 * @param userData
	 */
	public DefaultIterator(Iterator<T> delegate,Object userData)
		{
		this.delegate=delegate;
		setUserData(userData);
		}
	
	/**
	 * @param delegate delegate iterator
	 */
	public DefaultIterator(Iterator<T> delegate)
		{
		this(delegate,null);
		}
	
	/**
	 * @param delegate delegate iterator
	 */
	public DefaultIterator(Enumeration<T> delegate)
		{
		this(new EnumIter<T>(delegate));
		}
	
	/**
	 * @param delegate delegate iterator
	 */
	public DefaultIterator(Enumeration<T> delegate,Object userData)
		{
		this(new EnumIter<T>(delegate),userData);
		}
	
	
	/** set the user Object */
	public void setUserData(Object userData) {
		this.userData = userData;
		}
	
	/** return the user's Object */
	public Object getUserData()
		{
		return this.userData;
		}
	
	/** return the delegate iterator */
	public Iterator<T> getDelegate()
		{	
		return this.delegate;
		}
	
	@Override
	public boolean hasNext() {
		boolean b = getDelegate().hasNext();
		if(!b) close();
		return b;
	}

	@Override
	public T next() {
		return getDelegate().next();
	}

	@Override
	public void remove() {
		 getDelegate().remove();
		}
	

	
	private static class EnumIter<T> implements Iterator<T>
		{
		private Enumeration<T> enumeration;
		EnumIter(Enumeration<T> enumeration)
			{
			this.enumeration=enumeration;
			}
		public boolean hasNext()
			{
			return enumeration.hasMoreElements();
			}
		
		public T next()
			{
			return this.enumeration.nextElement();
			}
		public void remove()
			{
			throw new UnsupportedOperationException("Cannot remove from enum");
			}
		}
	
	
}
