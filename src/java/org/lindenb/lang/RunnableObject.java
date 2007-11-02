/**
 * 
 */
package org.lindenb.lang;

/**
 * @author Pierre Lindenbaum
 * Utility class containing a user object
 */
public  abstract class RunnableObject<T> implements Runnable
{
private T object;

/**
 * @param object a user object
 */
public RunnableObject(T object)
	{
	this.object= object;
	}

/**
 * @return the user's object
 */
public T getObject() {
	return object;
	}

@Override
public abstract void run();
}
