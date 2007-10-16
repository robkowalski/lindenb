package org.lindenb.util.iterator;

import java.util.Vector;
import org.lindenb.util.Pair;


public class RandomAccessIterator<E> extends AbstractIterator<E>
{
private int end;
private int curr;
private Vector<E> data;

public RandomAccessIterator(Vector<E> data)
	{
	this(data,0,data.size());
	}
public RandomAccessIterator(Vector<E> data,Pair<Integer,Integer> bounds)
	{
	this(data,bounds.first(),bounds.second());
	}

public RandomAccessIterator(Vector<E> data,int beg,int end)
	{
	this.data=data;
	this.end=end;
	this.curr=beg;
	}

@Override
public boolean hasNext()
	{
	return curr< end;
	}

@Override
public E next()
	{
	return this.data.elementAt(curr++); 
	}

}
