package org.lindenb.util.iterator;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;




public abstract class AbstractIterator<T>
	implements CloseableIterator<T>,Iterable<T>
	{
	public abstract boolean hasNext();

	public abstract T next();

	public void remove() {
		throw new UnsupportedOperationException("cannot remove");
		}
	
	@Override
	protected void finalize() throws Throwable
		{
		close();
		super.finalize();
		}
	
	public int count()
		{
		int n=0;
		while(hasNext()) {++n;next();}
		close();
		return n;
		}
	
	public Collection<T> asCollection(Collection<T> col)
		{
		if(col==null) col=new Vector<T>();
		while(hasNext()) col.add(next());
		close();
		return col;
		}
	
	public Set<T> asSet()
		{
		return (Set<T>)asCollection(new HashSet<T>());
		}
	
	public Vector<T> asVector()
		{
		return (Vector<T>)asCollection(new Vector<T>());
		}
	
	
	public Enumeration<T> asEnumeration()
		{
		return new Enumeration<T>()
			{
			public boolean hasMoreElements() {
				return AbstractIterator.this.hasNext();
				}
			public T nextElement() {
				return AbstractIterator.this.next();
				}
			};
		}
	
	/** impelements Iterable: just return this */
	public Iterator<T> iterator() {
		return this;
		}
	
	/** implements CloseableIterator */
	public void close()
		{
		}
	
	/** return the one and only one element found in the iterator */
	public T theOne()
		{
		boolean ok=false;
		T found=null;
		while(hasNext())
			{
			if(ok) throw new RuntimeException("Expected only one element but fount twice");
			found=next();
			ok=true;
			}
		if(!ok) throw new RuntimeException("Expected at least one element but fount none");
		return found;
		}
	
	/** return the one  element found in the iterator or null if no such element exists*/
	public T theOneOrNull()
		{
		boolean ok=false;
		T found=null;
		while(hasNext())
			{
			if(ok) throw new RuntimeException("Expected only one element but fount twice");
			found=next();
			ok=true;
			}
		return found;
		}
	
	/** return the first element in the iterator without throwing a "no such element exception" or null if no such element exists*/
	public T first()
		{
		return(hasNext()?next():null);
		}
	
}
