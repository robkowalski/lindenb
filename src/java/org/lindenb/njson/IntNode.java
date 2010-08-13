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
	@Override
	public Type getType()
		{
		return Type.INT;
		}
	
	public java.math.BigInteger getValue()
		{
		return value;
		}
	@Override
	public void print(java.io.Writer out) throws java.io.IOException
		{
		out.write(String.valueOf(getValue()));
		}
	@Override
	public int hashCode()
		{
		return getValue().hashCode();
		}
	@Override
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
	@Override
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