package org.lindenb.util.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;




public abstract class AbstractIterator<T>
	implements ExtendedIterator<T>,
	CloseableIterator<T>,
	Iterable<T>
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
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#count()
	 */
	public int count()
		{
		int n=0;
		while(hasNext()) {++n;next();}
		close();
		return n;
		}
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#asCollection(java.util.Collection)
	 */
	public Collection<T> asCollection(Collection<T> col)
		{
		if(col==null) col=new ArrayList<T>();
		while(hasNext()) col.add(next());
		close();
		return col;
		}
	
	public Collection<T> asCollection()
		{
		return asCollection(new ArrayList<T>());
		}
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#asSet()
	 */
	public Set<T> asSet()
		{
		return (Set<T>)asCollection(new HashSet<T>());
		}
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#asVector()
	 */
	public Vector<T> asVector()
		{
		return (Vector<T>)asCollection(new Vector<T>());
		}
	
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#asEnumeration()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#iterator()
	 */
	public Iterator<T> iterator() {
		return this;
		}
	
	/** implements CloseableIterator */
	public void close()
		{
		}
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#theOne()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#theOneOrNull()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.lindenb.util.iterator.ExtendedIterator#first()
	 */
	public T first()
		{
		return(hasNext()?next():null);
		}
	
	@Override
	public boolean equals(Object obj) {
		return obj==this;
		}
	
	@Override
	protected Object clone()throws CloneNotSupportedException 
		{
		throw new CloneNotSupportedException("Cannot clone "+getClass());
		}
}
