package org.lindenb.util;

public class C extends StringUtils
	{
	protected C() {}
	 /**
     * escape a C string
     * @param s a word to convert
     * @return the converted string
     */
    static public String escape(CharSequence s)
        {
    	if(s==null) return null;
        int needed=-1;
        for(int i=0;i< s.length();++i)
	        {
	        switch(s.charAt(i))
	            {
	            case('\"'): 
	            case('\''): 
	            case('\n'): 
	            case('\t'):
	            case('\r'):
	            case('\b'):
	            case('\f'):
	            case('\\'): needed=i; break;
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
                case('\"'): buffer.append("\\\"");break;
                case('\''): buffer.append("\\\'");break;
                case('\n'): buffer.append("\\n");break;
                case('\t'): buffer.append("\\t");break;
                case('\r'): buffer.append("\\r");break;
                case('\b'): buffer.append("\\b");break;
                case('\f'): buffer.append("\\f");break;
                case('\\'): buffer.append("\\\\");break;
                default: buffer.append(s.charAt(i)); break;
                }
            }
        return buffer.toString();
        }
    
    /** unescape a C string */
    static public String unescape(CharSequence s)
    	{
    	if(s==null) return null;
      	StringBuilder sb= new StringBuilder(s.length());
      	for(int i=0;i< s.length();++i)
      		{
      		if(s.charAt(i)=='\\')
    			{
    			if(i+1< s.length())
    				{
    				++i;
    				switch(s.charAt(i))
    					{
    					case 'n': sb.append('\n'); break;
    					case 'r': sb.append('\r'); break;
    					case '\\': sb.append('\\'); break;
    					case 'b': sb.append('\b'); break;
    					case 't': sb.append('\t'); break;
    					case 'f': sb.append('\f'); break;
    					case '\'': sb.append('\''); break;
    					case '\"': sb.append('\"'); break;
    					default: sb.append(s.charAt(i));
    					}
    				}
      			}
      		else
      			{
      			sb.append(s.charAt(i));
      			}
      		}
      	return sb.toString();
    	}
}
