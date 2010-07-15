package org.lindenb.util;

/**
 * inspired from Google collections 'Predicate'
 * Determines a true or false value for a given input.
 * @author lindenb
 *
 * @param <T>
 */
public interface Predicate<T>
	{
	/** Determines a true or false value for the given input. */
	public boolean apply(T input); 
	}
