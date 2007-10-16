package org.lindenb.bio;

import java.io.Serializable;
import java.util.HashMap;





	/**
	 *
	 * version of a human genome draft
	 *
	 * @author $Author: lindenb $
	 * @version $Version: $
	 * @since $Date: 2007/03/13 09:23:25 $
	 * $Log: Build.java,v $
	 * Revision 1.11  2007/03/13 09:23:25  lindenb
	 * cont
	 *
	 * Revision 1.10  2007/02/07 07:12:37  lindenb
	 * cont
	 *
	 * Revision 1.9  2007/01/31 07:14:49  lindenb
	 * cont
	 *
	 * Revision 1.8  2007/01/24 16:23:00  lindenb
	 * cont
	 *
	 * Revision 1.6  2006/11/02 07:23:51  lindenb
	 * cont
	 *
	 * Revision 1.8  2006/10/31 15:48:49  pierre
	 * cont
	 *
	 * Revision 1.5  2006/09/27 06:47:48  lindenb
	 * *** empty log message ***
	 *
	 * Revision 1.7  2006/09/22 13:21:48  pierre
	 * cont
	 *
	 * Revision 1.4  2006/09/18 15:27:32  lindenb
	 * *** empty log message ***
	 *
	 * Revision 1.5  2006/09/15 19:27:32  pierre
	 * *** empty log message ***
	 *
	 * Revision 1.3  2006/09/15 09:54:55  lindenb
	 * *** empty log message ***
	 *
	 * Revision 1.4  2006/09/14 18:00:35  pierre
	 * *** empty log message ***
	 *
	 * Revision 1.2  2006/09/14 11:29:40  lindenb
	 * int to long
	 *
	 * Revision 1.2  2006/09/14 11:14:45  pierre
	 * long instead of int
	 *
	 * Revision 1.1  2006/09/12 10:05:50  lindenb
	 * source20060912
	 *
	 * Revision 1.1  2006/09/12 09:36:45  pierre
	 * change
	 *
	 *
	 */
	public class Build extends Number
	    implements Comparable<Build>,Serializable
        {
		private static final long serialVersionUID = 1L;

		private static transient HashMap<String,Build> DRAFTPOOL=null;

        private String build;

        /**
         * Constructor for <code>Draft</code>
         * @param i
         */
        public Build(int i)
                {
        		build = String.valueOf(i);
                }
        /**
         * Constructor for <code>Draft</code>
         * @param s
         */
        public Build(String s)
                {
                this.build = s;
                }

        /**
         * @return draft as an int value
         */
        public int getBuild()
                {
                return Integer.parseInt( build);
                }

        public String getName()
                {
                return this.build;
                }
        
        /** get UCSC human genome version */
        public int getHg()
                {
                return getBuild()-18;
                }

        /**
         *
         * implementation of compareTo
         *
         * @param cp object to compare
         * @return integer used to sort Position
         *
         */
        public int compareTo(Build cp)
                {
                return getName().compareToIgnoreCase(cp.getName());
                }


        /**
         * overides/implements parent
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o)
                {
        		if(o==this) return true;
	            if(o==null || !(o instanceof Build)) return false;
	            return getName().equalsIgnoreCase(Build.class.cast(o).getName());
                }


        /**
         * convert this object to a String
         *
         * @return the generated String
         *
         */
        @Override
        public String toString()
            {
            return getName();
            }

        /**
         * overides/implements parent
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
            {
        	return getName().hashCode();//7 est un nombre premier
            }


    /**
     * build a new Build using a pool: greatly decrease memory usage
     * when using always the same draft
     * @param build
     * @return
     */
    static public Build newInstance(int d)
    {
      
      return newInstance(String.valueOf(d));
     }

    /**
     * calls newInstance(int)
     * @param d
     * @return
     */
    static public Build newInstance(String d)
     	{
    	if(DRAFTPOOL==null) DRAFTPOOL=new HashMap<String, Build>();
        Build b= DRAFTPOOL.get(d);
        if(b!=null) return b;
        DRAFTPOOL.put(d,(b= new Build(d)));
        return b;
     	}

	@Override
	public double doubleValue() {
		return getBuild();
	}

	@Override
	public float floatValue() {
		return getBuild();
	}

	@Override
	public int intValue() {
		return getBuild();
	}

	@Override
	public long longValue() {
		return getBuild();
	}


        

}