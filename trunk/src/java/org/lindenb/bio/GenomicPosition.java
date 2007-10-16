package org.lindenb.bio;

import java.io.Serializable;

public class GenomicPosition
implements Comparable<GenomicPosition>,Serializable
{
private static final long serialVersionUID = 1L;
private Organism organism;
private Build build;
private Chromosome chr=null;
private Range range;



/**
 * constructor
 */
public GenomicPosition(
		Organism organism,
		Build build,
		Chromosome c,
		long start,
		long e)
        {
		this.organism=organism;
		this.build=build;
        this.range=new Range(start,e);
        this.chr= c;
        }

public GenomicPosition(
		Build build,
		Chromosome c,
		long start,
		long e)
        {
		this(Organism.getDefaultOrganism(),build,c,start,e);
        }

/**
*
* DCRPosition
* @param s position to parse in in the format <code>(draft)chrom:start-end</code>
*/
public GenomicPosition(String s)
       {
       this(
    	   Build.newInstance(s.substring(s.indexOf('(')+1, s.indexOf(')'))),
           Chromosome.newInstance(s.substring(s.indexOf(')')+1, s.indexOf(':'))),
           Long.parseLong(s.substring(s.indexOf(':')+1, s.indexOf('-'))),
           Long.parseLong(s.substring(s.indexOf('-')+1))
           );
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

	public Organism getOrganism() {
		return organism;
	}

public Build getBuild() {
	return build;
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
public int compareTo(GenomicPosition p)
        {
       	int i= getOrganism().compareTo(p.getOrganism());
       	if( i!=0) return i;
       	i= getBuild().compareTo(p.getBuild());
       	if( i!=0) return i;
        i = getChromosome().compareTo(p.getChromosome());
        if( i!=0) return i;
        return getRange().compareTo(p.getRange());
        }


/**
 * convert this object to an NCBI URI
 * draft is not garanteed
 * @return the URL
 */
public String toNCBIURI()
        {
        String c= getChromosome().toString().replaceAll("chr","");
        return new String(
                "http://www.ncbi.nlm.nih.gov/mapview/maps.cgi?taxid="+
                getOrganism().getOrganismId()
                +"&CHR="+c+"&BEG="+getStart()+"&END="+getEnd()
                  );
        }



/**
 *  checks if CRPosition Ranges overlap
 *
 * @param p the CRPosition to compare
 * @return true if the two CRPosition overlap
 *
 */
public boolean overlap(GenomicPosition p)
        {
       	if(! getOrganism().equals(p.getOrganism())) return false;
       	if(! getBuild().equals(p.getBuild())) return false;
       	if(!  getChromosome().equals(p.getChromosome())) return false;
        return  getRange().overlap(p.getRange());
        }

public Range getOverlap(GenomicPosition p)
	{
	if(! getOrganism().equals(p.getOrganism())) return null;
   	if(! getBuild().equals(p.getBuild())) return null;
   	if(!  getChromosome().equals(p.getChromosome())) return null;
	return getRange().getOverlap(p.getRange());
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
        return "("+getBuild()+")"+getChromosome().toString()+":"+ getRange().toString();
        }


}
