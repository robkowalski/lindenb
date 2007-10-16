/**
 * 
 */
package org.lindenb.util.iterator;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author pierre
 *
 */
public class UniqueIterator<T> extends AbstractIterator<T>
	{
	private Iterator<T> delegate;
	private Comparator<T> cmp;
	private T nextValue=null;
	private T prevValue=null;
	/**
	 * UniqueIterator
	 */
	public UniqueIterator(Iterator<T> delegate,Comparator<T> cmp)
		{
		this.delegate=delegate;
		this.cmp=cmp;
		}
	
	/**
	 * UniqueIterator
	 */
	public UniqueIterator(Iterator<T> delegate)
		{
		this(delegate,null);
		}

	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.AbstractIterator#hasNext()
	 */
	@Override
	public boolean hasNext()
		{
		if(nextValue!=null) return true;
		while(this.delegate.hasNext())
			{
			nextValue=delegate.next();
			if(prevValue==null) break;
			if(!equals(prevValue, nextValue)) break;
			nextValue=null;
			}
		return nextValue!=null;
		}
	
	public boolean equals(T o1,T o2)
		{
		return (this.cmp!=null ? cmp.compare(o1, o2)==0 : o1.equals(o2) );
		}

	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.AbstractIterator#next()
	 */
	@Override
	public T next()
		{
		if(!hasNext()) return null;
		prevValue=nextValue;
		nextValue=null;
		return prevValue;
		}

}
