package org.lindenb.bio;

import org.lindenb.util.XObject;


/**
 *
 * version of a human genome draft
 */

public class Organism
	extends XObject
    implements Comparable<Organism>
    {
	private static final long serialVersionUID = 1L;

	/** NCBI organism ID */
	private int organismId;
	/** organism name */
	private String name;
	
	public static final Organism HomoSapiens=new Organism(9606,"Homo Sapiens");
	private static  Organism DEFAULT=HomoSapiens;
	
	public Organism(int organismId)
		{
		this(organismId,null);
		}
	
	public Organism(int organismId,String name)
		{
		this.organismId=organismId;
		this.name=name;
		}
	
	public static Organism getDefaultOrganism()
		{
		return DEFAULT;
		}
	
	public static void setDefaultOrgansim(Organism organism)
		{
		DEFAULT=organism;
		}
	
	public int getOrganismId()
		{
		return this.organismId;
		}
	
	public String getName()
		{
		return (this.name==null?String.valueOf(getOrganismId()):this.name);
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj==this) return true;
		if(obj==null || !(obj instanceof Organism)) return false;
		return this.compareTo(Organism.class.cast(obj))==0;
		}
	
	public int compareTo(Organism o) {
		
		return getOrganismId()-o.getOrganismId();
		}
	
	@Override
	public int hashCode() {
		return getOrganismId();
		}
	
	@Override
	public String toString()
		{
		return getName()+"("+getOrganismId()+")";
		}

}