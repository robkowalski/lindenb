package org.lindenb.njson;

public class  BoolNode extends TerminalNode
	{
	public static final BoolNode TRUE=new BoolNode(true);
	public static final BoolNode FALSE=new BoolNode(false);
	private boolean value;
	public BoolNode(boolean value)
		{
		this.value=value;
		}
	@Override
	public Type getType()
		{
		return Type.BOOL;
		}
	
	public boolean getValue()
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
		return getValue()?31:32;
		}
	@Override
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(o==null || !(o instanceof BoolNode)) return false;
		return BoolNode.class.cast(o).getValue()== getValue();
		}
	@Override
	public Object clone()
		{
		return new BoolNode(getValue());
		}
	
	@Override
	public String toString()	
		{
		return String.valueOf(getValue());
		}
	}