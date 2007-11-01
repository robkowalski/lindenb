package org.lindenb.xml;


public class XMLUtilities
	{
	private XMLUtilities() {}
	
/** escape the XML of a given string */
public static String escape(CharSequence s)
        {
        if(s==null) throw new NullPointerException("XML.escape(null)");
        int needed=-1;
        for(int i=0;i< s.length();++i)
	        {
	        switch(s.charAt(i))
	            {
				case '\'': 
				case '\"': 
				case '&': 
				case '<':
				case '>':  needed=i; break;
	
	            default: break;
	            }
	        if(needed!=-1) break;
	        }
		if(needed==-1) return s.toString();
		StringBuilder buffer=new StringBuilder(s.subSequence(0,needed));
		for(int i=needed;i< s.length();++i)
		       {
		       switch(s.charAt(i))
		               {
		               case '\'': buffer.append("&apos;"); break;
		               case '\"': buffer.append("&quot;"); break;
		               case '&': buffer.append("&amp;"); break;
		               case '<': buffer.append("&lt;"); break;
		               case '>': buffer.append("&gt;"); break;
		               default:  buffer.append(s.charAt(i));break;
		               }
		       }
		return buffer.toString();
        }

}
