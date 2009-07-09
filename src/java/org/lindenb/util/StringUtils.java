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
	/** return wether the sequence is null, empty of contains only white characters */
	public static boolean isBlank(char array[],int start,int length)
		{
		if(array==null || length==0) return true;
		for(int i=0;i< length ;++i)
			{
			if(!Character.isWhitespace(array[start+i])) return false;
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
	
	/** anwsers wether the first string starts with any of the other strings */
	public static boolean startsWith(String search,String...starts)
		{
		for(String s:starts) if(search.startsWith(s)) return true;
		return false;
		}
	/** anwsers wether the first string ends with any of the other strings */
	public static boolean endsWith(String search,String...ends)
		{
		for(String s:ends) if(search.endsWith(s)) return true;
		return false;
		}
	}
