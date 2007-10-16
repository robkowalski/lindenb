package org.lindenb.bio;

import java.io.Serializable;

import org.lindenb.util.SmartComparator;

public class Chromosome
	implements Comparable<Chromosome>,Serializable
	{
	private static final long serialVersionUID = 1L;
	
    static public transient final Chromosome DEFAULT_CHROMOSOMES[]={
        new Chromosome(1),
        new Chromosome(2),
        new Chromosome(3),
        new Chromosome(4),
        new Chromosome(5),
        new Chromosome(6),
        new Chromosome(7),
        new Chromosome(8),
        new Chromosome(9),
        new Chromosome(10),
        new Chromosome(11),
        new Chromosome(12),
        new Chromosome(13),
        new Chromosome(14),
        new Chromosome(15),
        new Chromosome(16),
        new Chromosome(17),
        new Chromosome(18),
        new Chromosome(19),
        new Chromosome(20),
        new Chromosome(21),
        new Chromosome(22),
        new Chromosome("chrX"),
        new Chromosome("chrY")
        };
	        private String chr;


        /**
         *
         * constructor
         *
         * @param c the chromosome name
         *
         */
        public Chromosome(String c)
                {
                this.chr= c;
                }

	        /**
	         *
	         * constructor
	         *
	         * @param s the chromosome
	         *
	         */
	        public Chromosome(Chromosome s)
	                {
	                chr= s.chr;
	                }


	        /**
	         *
	         * constructor(int)
	         * a string 'chr' whill be inserted at the beginning of the string
	         *
	         * @param i the chromosome number
	         *
	         */
	        public Chromosome(int n)
	                {
	                if(n==23)
	                        {
	                        this.chr= new String("chrX");
	                        }
	                else if(n==24)
	                        {
	                        this.chr= new String("chrY");
	                        }
	                else
	                        {
	                        this.chr= new String("chr"+n);
	                        }
	                }

	        /**
	         *
	         * implementation of compareTo
	         *
	         * @param cp object to compare
	         * @return integer used to sort Position
	         *
	         */
	        public int compareTo(Chromosome cp)
                {
                return SmartComparator.getInstance().compare(this.chr,cp.chr);
                }

	        @Override
	        public boolean equals(Object cp)
	            {
	        	if(cp==this) return true;
	        	if(cp==null || !(cp instanceof Chromosome)) return false;
	            return compareTo(Chromosome.class.cast(cp))==0;
	            }


	        /**
	         * convert this object to a String
	         *
	         * @return the generated Striing
	         *
	         */
	        @Override
	        public String toString()
	                {
	                return this.chr;
	                }

	        /**
	         * overides/implements parent
	         * @see java.lang.Object#hashCode()
	         */
	        @Override
	        public int hashCode()
	                {
	            	return this.chr.hashCode();
	                }


	    /**
	     * @param d
	     * @return
	     */
	    static public Chromosome newInstance(int d)
	    {
	       if(d>0 && d<25) return DEFAULT_CHROMOSOMES[d-1];
	       return new Chromosome(d);
	    }

	    /**
	     * @param d
	     * @return
	     */
	    static public Chromosome newInstance(String d)
	    {
	     for(Chromosome c: DEFAULT_CHROMOSOMES)
	     	{
	        if(c.chr.equals(d)) return  c;
	     	}
	     return new Chromosome(d);
	    }

}
