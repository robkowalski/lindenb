package org.lindenb.bio;

import java.io.Serializable;



/****************************
 *
 * class Range
 *
 */
public class Range
        implements Comparable<Range>,Serializable
    {
	private static final long serialVersionUID = 1L;
	private long begin;
	private long end;

    /****************************
     *
     * Range
     *
     */
    public Range(long begin, long end)
            {
            this.begin=begin;
            this.end=end;
            }


    /**
    *
    * constructor
    * @param p1 index of the left
    * @param p2 index of the right
    *
    */
   public  Range(String p1, String p2)
           {
           this.begin = java.lang.Long.parseLong(p1);
           this.end= java.lang.Long.parseLong(p2);
           }

   /****************************
    *
    * Pair
    *
    */
   public Range()
           {
           this(-1,-1);
           }

    
    
    public long getStart()
        {
        return begin;
        }

    public long getEnd()
        {
        return end;
        }

    public long getLeft()
        {
        return begin;
        }

    public long getRight()
        {
        return end;
        }



    /**
     *
     * return the size range = this.distance()+1
     * @return return the size range
     *
     */
    public long size()
            {
            return this.distance() +1;
            }

    /**
     *
     * return the distance = end-begin
     * @return return the distance range
     *
     */
    public long distance()
            {
            return getEnd() -this.getStart();
            }


    /**
     *  checks if two Ranges overlap
     *
     * @param the Range to compare
     * @return true if the two ranges overlap
     *
     */
    public boolean overlap(Range o)
            {
            return  !(getEnd() <= o.getStart() || o.getEnd() < this.getStart() )
                                    ;
            }




    public boolean contains(int p)
    {
        return !(p< this.getStart() || p>=getEnd());
    }

    /**
     *
     * implementation of compareTo
     * it sorts on begin on getStart, then on getEnd
     *
     * @param o object to compare
     * @return integer used to sort Range
     *
     */
    public int compareTo(Range cp)
            {
            if(this.getStart()!=cp.getStart())
                    return (this.getStart()<cp.getStart() ? -1 : 1);
            if(getEnd()!=cp.getEnd())
                    return (getEnd()<cp.getEnd() ? -1 : 1);
            return 0;
            }
    @Override
    public boolean equals(Object o)
        {
    	if(o==this) return true;
    	if(o==null || !(o instanceof Range)) return false;
        return compareTo((Range)o)==0;
        }
    @Override
    public Object clone()
            {
            return new Range();
            }

    /****************************
     *
     * toString
     *
     */
    @Override
    public String toString()
            {
            return ""+this.getStart()+"-"+getEnd();
            }

    /****************************
     *
     * elementAt
     *
     */
    public long at(int i)
            {
            assert(i<0 || i>=2);
            return (i==0?this.getStart():getEnd());
            }


    /**
     * overides/implements parent
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
            return (int)(this.getStart()%50021);//50021 est un nombre premier
            }


/**
 * <pre>
 * this              ===================
 * cp                    **********************
 * returns               +++++++++++++++
 *
 * cp                   ***********
 * returns              +++++++++++
 *
 * cp         *****************
 * returns            +++++++++
 *
 *
 * cp            ****************************
 * returns           ++++++++++++++++++
 *
 * @param cp
 * @return</pre>
 */
public Range getOverlap(Range cp)
    {
    //a gauche
    if(getStart()<=cp.getStart() && getEnd() > cp.getStart() && getEnd() <= cp.getEnd())
        {
        return new Range(cp.getStart(),getEnd());
        }
    //overlapp total
    else if(getStart()<=cp.getStart() && getEnd() >= cp.getEnd())
        {
        return new Range(cp.getStart(), cp.getEnd());
        }
    //a droite
    else if(getStart()>= cp.getStart() && getStart() < cp.getEnd() && getEnd()>=cp.getEnd())
        {
        return new Range(getStart(), cp.getEnd());
        }
    //inside
    else if(getStart()>=cp.getStart() && getEnd() <=cp.getEnd())
        {
        return new Range(getStart(),getEnd());
        }
    return null;
    }


   }
