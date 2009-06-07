package org.lindenb.sw.nodes;

import java.io.Serializable;



public class Statement
	implements Serializable,Comparable<Statement>
	{
	private static final long serialVersionUID = 1L;
	private Resource subject;
	private Resource predicate;
	private RDFNode value;
	
	public Statement(Resource subject, Resource predicate, RDFNode value)
		{
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
		}
	
	public Resource getSubject() {
		return subject;
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
		result = prime * result + subject.hashCode();
		result = prime * result +  value.hashCode();
		return result;
		}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Statement))
			return false;
		Statement other = (Statement) obj;
		
		if (!subject.equals(other.subject)) return false;
		if (!predicate.equals(other.predicate)) return false;
		if (!value.equals(other.value)) return false;
		
		return true;
		}
	
	public boolean isLiteral()
		{
		return getValue().isLiteral();
		}
	
	public boolean isResource()
		{
		return getValue().isResource();
		}
	
	public String asN3()
		{
		StringBuilder b=new StringBuilder();
		b.append(getSubject().asN3()).append(" ");
		b.append(getPredicate().asN3()).append(" ");
		b.append(getValue().asN3()).append(" .");
		return b.toString();
		}

	
	@Override
	public int compareTo(Statement o)
		{
		if(o==this) return 0;
		if(o==null) return 1;
		int i= getSubject().compareTo(o.getSubject());
		if(i!=0) return i;
		i= getPredicate().compareTo(o.getPredicate());
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
	 * @param subject used as a filter. if null, select wathever 
	 * @param predicate used as a filter. if null, select wathever 
	 * @param value used as a filter. Can be a String or a Resource. If null, select wathever
	 * @returna true if this Stmt match the rules
	 */
	public boolean match(
				Resource subject,
				Resource predicate,
				RDFNode value
				)
		{
		if(subject!=null && !getSubject().equals(subject)) return false;
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
		return asN3();
		}
	

}
