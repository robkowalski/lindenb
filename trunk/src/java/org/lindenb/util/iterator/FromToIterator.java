package org.lindenb.util.iterator;


import java.util.Iterator;



public class FromToIterator<T> extends DefaultIterator<T> {
	private int beg;
	private int curr;
	private int end;

	public FromToIterator(Iterator<T> delegate,int beg,int end)
		{
		super(delegate);
		this.curr=0;
		this.beg=beg;
		this.end=end;
		}
	
	public FromToIterator(Iterator<T> data)
		{
		this(data,0);
		}
	
	public FromToIterator(Iterator<T> data,int beg)
		{
		this(data,beg,Integer.MAX_VALUE);
		}
	
	@Override
	public boolean hasNext()
		{
		while(curr<beg && getDelegate().hasNext())
			{
			getDelegate().next();
			++curr;
			}
		if(curr>=end) return false;
		boolean b= getDelegate().hasNext();
		if(!b) close();
		return b;
		}
	
	@Override
	public T next()
		{
		++curr;
		return getDelegate().next();
		}
	
}
