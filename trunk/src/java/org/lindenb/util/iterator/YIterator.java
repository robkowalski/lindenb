package org.lindenb.util.iterator;

import java.util.Iterator;




public class YIterator<T> extends AbstractIterator<T> {

	private Iterator<T> first;
	private Iterator<T> second;
	
	public YIterator(Iterator<T> first, Iterator<T> second)
		{
		this.first=first;
		this.second=second;
		}
	
	
	@Override
	public boolean hasNext() {
		return first.hasNext() || second.hasNext();
	}

	@Override
	public T next()
		{
		if(first.hasNext()) return first.next();
		return second.next();
		}
}
