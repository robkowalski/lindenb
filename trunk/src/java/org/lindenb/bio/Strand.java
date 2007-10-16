package org.lindenb.bio;

import java.io.Serializable;

/**
 * @author pierre
 * define a strand on a chromosome.
 * should be + or -
 */
public class Strand implements Serializable
		{
		private static final long serialVersionUID = 1L;
        static public final Strand PLUS=new Strand('+');
        static public final Strand MINUS=new Strand('-');
        static public final Strand UNKNOWN=new Strand('?');

        private Character strand;

        public Strand(char c)
        {
                switch(c)
                {
                        case('+'): strand= c; break;
                        case('-'): strand= c; break;
                        case('?'): strand= c; break;
                        default: throw new IllegalArgumentException("Bad strand :"+c);
                 
                }
        }

        public Strand(Character c)
            {
            this(c.charValue());
            }

        public Strand(String s)
        	{
            this(s.charAt(0));
        	}

       

        @Override
        public int hashCode() {
        	return strand.hashCode();
        	}
        
        @Override
        public boolean equals(Object obj) {
        	if(obj == this) return true;
        	if(obj ==null || !(obj instanceof Strand)) return false;
        	return strand.equals(((Strand)obj).strand);
        	}
        
        
        public boolean isPlus() { return strand== '+';}
        public boolean isMinus() { return strand== '-';}
        public boolean isUnknown() { return strand== '?';}

        @Override
        public String toString()
        {
         return strand.toString();
        }
        
        public static Strand newInstance(char c)
        	{
        	if(PLUS.strand==c) return PLUS;
        	if(MINUS.strand==c) return MINUS;
        	return UNKNOWN;
        	}
        
}
