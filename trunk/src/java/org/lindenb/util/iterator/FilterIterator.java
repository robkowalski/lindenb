package org.lindenb.util.iterator;

import java.util.Iterator;

/**
 * an Iterator wich must implements accept(E)
 * 
 */
public abstract class FilterIterator<E> extends DefaultIterator<E>
{
private E found;
/**
 * FilterIterator
 * @param iter delegate iterator
 * @param userData  a value that might be interesting to use when using 'accept'
 */
public 	FilterIterator(Iterator<E> iter,Object userData)
	{
	super(iter,userData);
	this.found=null;
	}

/**
 * FilterIterator
 * @param iter delegate iterator
 */
public 	FilterIterator(Iterator<E> iter)
	{
	this(iter,null);
	}



/** return wether the iterator can accept a data from the delegate iterator */
public abstract boolean accept(E data);

@Override
public boolean hasNext()
	{
    if(found!=null) return true;
    found=null;
    while(found==null)
            {
            if(!getDelegate().hasNext())
            	{
            	close();
            	break;
            	}
            E x= getDelegate().next();
            if(accept(x))
                    {
                    found=x;
                    break;
                    }
            }
    return found!=null;
    }

@Override
public E next()
    {
    assert(found!=null);
    E old=found;
    found=null;
    return old;
    }

}
