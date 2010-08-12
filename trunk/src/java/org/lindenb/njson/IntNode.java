package org.lindenb.njson;

public class  IntNode extends TerminalNode
	implements Comparable<IntNode>
	{
	private java.math.BigInteger value;
	public IntNode(java.math.BigInteger value)
		{
		this.value=value;
		}
		
	public IntNode(String value)
		{
		this.value=new java.math.BigInteger(value);
		}
	
	public Type getType()
		{
		return Type.INT;
		}
	
	public java.math.BigInteger getValue()
		{
		return value;
		}
	
	public void print(java.io.Writer out) throws java.io.IOException
		{
		out.write(String.valueOf(getValue()));
		}
	
	public int hashCode()
		{
		return getValue().hashCode();
		}
	
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(o==null || !(o instanceof IntNode)) return false;
		return IntNode.class.cast(o).getValue().equals(getValue());
		}
	
	@Override
	public int compareTo(IntNode o)
		{
		return getValue().compareTo(o.getValue());
		}
	
	public Object clone()
		{
		return new IntNode(getValue());
		}
	
	@Override
	public String toString()	
		{
		return String.valueOf(getValue());
		}
	}