package org.lindenb.util;

import java.util.Collection;
import java.util.Iterator;


/**
 * public methods for Iterators
 * @author pierre
 *
 */
public class Iterators
	{
	/** return the number of item in this iterator */
	public static int count(Iterator<?> iter)
		{
		int c=0;
		while(iter.hasNext())
			{
			iter.next();
			++c;
			}
		return c;
		}
	
	public static <T> Collection<T> copyTo(Iterator<T> iter,Collection<T> collection)
		{
		while(iter.hasNext()) collection.add(iter.next());
		return collection;
		}
	
	public static <T> boolean contains(Iterator<T> iter,T object)
		{
		while(iter.hasNext())
			{
			T o= iter.next();
			if(o==null && object==null) return true;
			if(o.equals(object)) return true;
			}
		return false;
		}
	
	
	
	
	}
