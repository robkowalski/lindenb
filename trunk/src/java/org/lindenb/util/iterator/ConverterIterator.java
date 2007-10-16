package org.lindenb.util.iterator;

import java.util.Iterator;

/**
 * 
 * @author pierre
 *
 * @param <I>
 * @param <O>
 */
public abstract class ConverterIterator<I,O> extends AbstractIterator<O> {
	private Iterator<I> delegate;
	private Object userObject;
	public ConverterIterator( Iterator<I> delegate)
		{
		this(delegate,null);
		}
	
	public ConverterIterator( Iterator<I> delegate,Object userObject)
		{
		this.delegate=delegate;
		this.userObject=userObject;
		}

	
	public Object getUserData()
		{
		return this.userObject;
		}
	
	@Override
	public boolean hasNext()
		{
		return this.delegate.hasNext();
		}

	@Override
	public O next()
		{
		return convert(this.delegate.next());
		}

	public abstract O convert(I in);
	
}
