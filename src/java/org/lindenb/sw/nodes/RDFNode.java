package org.lindenb.sw.nodes;

import java.io.Serializable;

import org.lindenb.util.XObject;

public abstract class RDFNode
	extends XObject
	implements Serializable
	{
	private static final long serialVersionUID = 1L;
	protected RDFNode()
		{
		
		}
	
	public abstract boolean isResource();
	
	public abstract boolean isLiteral();
	
	public Resource asResource()
		{
		return Resource.class.cast(this);
		}
	public Literal asLiteral()
		{
		return Literal.class.cast(this);
		}
	
	public abstract String asN3();
	
	}
