package org.lindenb.util.iterator;


public class NullIterator<T> extends AbstractIterator<T> {

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		return null;
	}

}
