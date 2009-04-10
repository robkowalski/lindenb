package org.lindenb.util;

/**
 * Utilities for Strings or CharSequence
 * @author lindenb
 *
 */
public class StringUtils
	{
	protected StringUtils()
		{
		}
	
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
		}
	
	/** returns wether the sequence is empty of null */
	public static boolean isEmpty(CharSequence s)
		{
		return s==null || s.length()==0;
		}
	
	/** return wether the sequence is null, empty of contains only white characters */
	public static boolean isBlank(CharSequence s)
		{
		if(isEmpty(s)) return true;
		for(int i=0;i< s.length();++i)
			{
			if(!Character.isWhitespace(s.charAt(i))) return false;
			}
		return true;
		}
	}
