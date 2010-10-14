package org.lindenb.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils
	{
	/** creates a filtered list */
	public static  <T> List<T> filterList(List<T> source,Predicate<T> filter)
		{
		List<T> list=new ArrayList<T>();
		for(T object:source)
			{
			if(filter.apply(object)) list.add(object);
			}
		return list;
		}
	}
