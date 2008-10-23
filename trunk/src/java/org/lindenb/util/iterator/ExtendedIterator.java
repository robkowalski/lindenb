package org.lindenb.util.iterator;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;


public interface ExtendedIterator<T>
	extends Iterator<T>,Iterable<T>,CloseableIterator<T>
	{

	public abstract int count();

	public abstract Collection<T> asCollection(Collection<T> col);
	
	public abstract Collection<T> asCollection();

	public abstract Set<T> asSet();

	public abstract Enumeration<T> asEnumeration();

	/** impelements Iterable: shoudl just return this */
	public abstract Iterator<T> iterator();

	/** return the one and only one element found in the iterator */
	public abstract T theOne();

	/** return the one  element found in the iterator or null if no such element exists*/
	public abstract T theOneOrNull();

	/** return the first element in the iterator without throwing a "no such element exception" or null if no such element exists*/
	public abstract T first();

}