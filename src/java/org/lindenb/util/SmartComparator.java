package org.lindenb.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


/**
 * @author pierre
 * a comparator with compare the two to string of a, object using
 * tostring() method, case insensitive, parsing integers in the string
 * if a numeric if found
 */
public class SmartComparator implements Comparator<CharSequence>
    {
	//global instance
	private static volatile SmartComparator INSTANCE=null;
	
	
	/** return a global instance of a SmartComparator */
	public static SmartComparator getInstance()
		{
		if(INSTANCE==null)
			{
			synchronized(SmartComparator.class)
				{
				if(INSTANCE==null) INSTANCE=new SmartComparator();
				}
			}
		return INSTANCE;
		}
	
	
	public SmartComparator()
		{
		//empty
		}
	
    /**
     * overrides parent's method
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CharSequence a, CharSequence b)
        {
        if(a==null && b==null) return 0;
        if(a==null && b!=null) return -1;
        if(a!=null && b==null) return 1;
        String ss[]={a.toString(),b.toString()};
        char c[]={0,0};
        int i[]={0,0};
        while(true)
            {
            if(i[0] == a.length() &&
               i[1] == b.length()) return 0;
            if(i[0] == a.length()) return -1;
            if(i[1] == b.length()) return 1;

            c[0]=Character.toUpperCase(a.charAt(i[0]));
            c[1]=Character.toUpperCase(b.charAt(i[1]));

            //search for integer
            if( Character.isDigit(c[0]) &&
                Character.isDigit(c[1]))
                {
                int j[]={i[0]+1,i[1]+1};
                try
	                {
	                BigInteger values[]={null,null};
	                for(int side=0;side<2;++side)
	                    {
	                    while(  j[side]< ss[side].length() &&
	                                Character.isDigit(ss[side].charAt(j[side])))
                            {
                            j[side]++;
                            }
	                    values[side]=new BigInteger(ss[side].substring(i[side],j[side]));
	                    }
	                int k=values[0].compareTo(values[1]);
	                i[0]=j[0];
	                i[1]=j[1];
	                if(k!=0) return k;
	                continue;
	                }
                catch(NumberFormatException err)
                	{
                	throw err;
                	}
                
                }
           
            int k=c[0]-c[1];
            if(k!=0) return (k<0?-1:1);
            i[0]++;
            i[1]++;
            }
        }

    /****
     * sort a set of string from stdin
     *
     * 
     */
    public static void main(String[] args)
        {
        try
            {
                Vector<String> v= new Vector<String>();
                BufferedReader reader= new BufferedReader(new InputStreamReader(System.in));
                String line;
                while((line=reader.readLine())!=null)
                    {
                    v.addElement(line);
                    }
                Collections.sort(v,getInstance());
                for(String s: v) System.out.println(s);
            }
        catch (Throwable error)
            {
                error.printStackTrace();
                System.exit(-1);
            }
        }



    }
