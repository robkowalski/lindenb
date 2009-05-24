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
	
	/** anwsers wether the first string is in the choice */
	public static boolean isIn(String search,String...choice)
		{
		for(String s:choice) if(s.equals(search)) return true;
		return false;
		}
	
	/** anwsers wether the first string is in the choice */
	public static boolean isInIgnoreCase(String search,String...choice)
		{
		for(String s:choice) if(s.equalsIgnoreCase(search)) return true;
		return false;
		}
	
	
	}
