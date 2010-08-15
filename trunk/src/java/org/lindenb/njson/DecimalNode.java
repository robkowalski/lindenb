package org.lindenb.njson;

public class  DecimalNode extends TerminalNode
	implements Comparable<DecimalNode>
	{
	private java.math.BigDecimal value;
	public DecimalNode(java.math.BigDecimal value)
		{
		this.value=value;
		}
	public DecimalNode(double value)
		{
		this.value=new java.math.BigDecimal(value);
		}
	public DecimalNode(String value)
		{
		this.value=new java.math.BigDecimal(value);
		}
	@Override
	public Type getType()
		{
		return Type.DECIMAL;
		}

	public java.math.BigDecimal getValue()
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
		if(o==null || !(o instanceof DecimalNode)) return false;
		return DecimalNode.class.cast(o).getValue().equals(getValue());
		}
	@Override
	public Object clone()
		{
		return new DecimalNode(getValue());
		}
	
	@Override
	public int compareTo(DecimalNode o)
		{
		return getValue().compareTo(o.getValue());
		}
	
	@Override
	public String toString()	
		{
		return String.valueOf(getValue());
		}
	}