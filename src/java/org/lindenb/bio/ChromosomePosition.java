package org.lindenb.bio;

import java.io.Serializable;

public class ChromosomePosition
implements Comparable<ChromosomePosition>,Serializable
{
private static final long serialVersionUID = 1L;
private Chromosome chr=null;
private Range range;



/**
 * constructor
 */
public ChromosomePosition(
		Chromosome c,
		long start,
		long e)
        {
        this.range=new Range(start,e);
        this.chr= c;
        }




/**
 * return the chromosome
 *
 * @return the chromosome
 *
 */
public Chromosome getChromosome()
    {
    return this.chr;
    }



public Range getRange()
	{
	return this.range;
	}

public long getStart()
	{
	return getRange().getStart();
	}

public long getEnd()
	{
	return getRange().getEnd();
	}



	
/**
 *
 * implementation of compareTo
 * sorts on chr/start/end
 *
 * @param o object to compare
 * @return integer used to sort Position
 *
 */
public int compareTo(ChromosomePosition p)
        {
        int i = getChromosome().compareTo(p.getChromosome());
        if( i!=0) return i;
        return getRange().compareTo(p.getRange());
        }




/**
 *  checks if CRPosition Ranges overlap
 *
 * @param p the CRPosition to compare
 * @return true if the two CRPosition overlap
 *
 */
public boolean overlap(ChromosomePosition p)
        {
        if(!  getChromosome().equals(p.getChromosome())) return false;
        return  getRange().overlap(p.getRange());
        }

public Range getOverlap(ChromosomePosition p)
	{
	if(!  getChromosome().equals(p.getChromosome())) return null;
	return getRange().getOverlap(p.getRange());
	}


@Override
public int hashCode() {
	return getRange().hashCode();
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
        return getChromosome().toString()+":"+ getRange().toString();
        }


}
