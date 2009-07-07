package org.lindenb.sw.nodes;

import java.io.Serializable;


/**
 * Just a Predicate and an Object
 * @author lindenb
 *
 */
public class PredicateObject
	implements Serializable,Comparable<PredicateObject>
	{
	private static final long serialVersionUID = 1L;
	private Resource predicate;
	private RDFNode value;
	
	public PredicateObject(Resource predicate, RDFNode value)
		{
		
		this.predicate = predicate;
		this.value = value;
		}
	
	public Resource getPredicate() {
		return predicate;
		}
	public RDFNode getValue() {
		return value;
		}
	
	
	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + predicate.hashCode();
		result = prime * result +  value.hashCode();
		return result;
		}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PredicateObject))
			return false;
		PredicateObject other = (PredicateObject) obj;
		
		if (!predicate.equals(other.predicate)) return false;
		if (!value.equals(other.value)) return false;
		
		return true;
		}
	/** shortcuts to  getValue().isLiteral() */
	public boolean isLiteral()
		{
		return getValue().isLiteral();
		}
	
	/** shortcuts to  getValue().isResource() */
	public boolean isResource()
		{
		return getValue().isResource();
		}

	
	@Override
	public int compareTo(PredicateObject o)
		{
		if(o==this) return 0;
		if(o==null) return 1;
		int i= getPredicate().compareTo(o.getPredicate());
		if(i!=0) return i;
		if(isResource())
			{
			if(!o.isResource()) return -1;
			return o.getValue().asResource().compareTo(o.getValue().asResource());
			}
		
		if(!o.isLiteral()) return 1;
		return o.getValue().asLiteral().compareTo(o.getValue().asLiteral());
		}
	
	/**
	 * return wether this statement match a given rule
	 * @param predicate used as a filter. if null, select wathever 
	 * @param value used as a filter. Can be a String or a Resource. If null, select wathever
	 * @returna true if this Stmt match the rules
	 */
	public boolean match(
				Resource predicate,
				RDFNode value
				)
		{
		if(predicate!=null && !getPredicate().equals(predicate)) return false;
		if(value!=null)
			{
			if(value.isResource())
				{
				if(!getValue().isResource()) return false;
				return value.equals(getValue());
 				}
			else if(value.isLiteral())
				{
				if(!getValue().isLiteral()) return false;
				return value.equals(getValue());
				}
			return false;
			}
		return true;
		}
	
	@Override
	public String toString() {
		return getPredicate().asN3()+" "+getValue().asN3();
		}
	

}
