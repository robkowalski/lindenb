/**
 * 
 */
package org.lindenb.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract Implementation of a Walker
 *
 */
public abstract class AbstractWalker<T>
	implements Walker<T>
	{
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
		}
	
	
	public boolean contains(T t)
		{
		if(t==null) { close();return false;}
		T x;
		while((x=next())!=null)
			{
			if(x.equals(t)) { close(); return true;}
			}
		close();
		return false;
		}
	
	public int count(T t)
		{
		long L= countL(t);
		return L> Integer.MAX_VALUE?Integer.MAX_VALUE:(int)L;
		}
	
	public long countL(T t)
		{
		if(t==null) { close();return 0L;}
		long n=0L;
		T x;
		while((x=next())!=null)
			{
			if(x.equals(t)) { n++;}
			}
		close();
		return n;
		}
	
	
	/** transforms this iterator as a Set */
	public Set<T> asSet()
		{
		Set<T> set= new HashSet<T>();
		T x;
		while((x=next())!=null) set.add(x);
		close();
		return set;
		}
	
	/** transforms this iterator as a List */
	public List<T> asList(int start,int count)
		{
		int index=-1;
		List<T> set= new ArrayList<T>();
		T x;
		while((x=next())!=null)
			{
			++index;
			if(index >= (start+count)) break;
			if(index >= start)
				{
				set.add(x);
				}
			}
		close();
		return set;
		}
	
	/** transforms this iterator as a List */
	public List<T> asList()
		{
		List<T> set= new ArrayList<T>();
		T x;
		while((x=next())!=null) set.add(x);
		close();
		return set;
		}
	
	/** count number of items as int */
	public int count()
		{
		long L=countL();
		return L> Integer.MAX_VALUE?Integer.MAX_VALUE:(int)L;
		}

	/** count number of items as long */
	public long countL()
		{
		long i=0L;
		while(next()!=null) {++i;}
		close();
		return i;
		}
	
	@Override
	public boolean equals(Object obj)
		{
		return this==obj;
		}
	
	@Override
	public String toString() {
		return  getClass().getName();
		}
	
	}
