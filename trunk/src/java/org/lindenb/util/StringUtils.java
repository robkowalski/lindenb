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
	/** remove simple or double quote from a String */
	public static String unquote(String s)
		{
		if(s==null) return null;
		if(s.length()>1 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("\"") && s.endsWith("\""))))
			{
			return s.substring(1, s.length()-1);
			}
		return s;
		}
	
	}
