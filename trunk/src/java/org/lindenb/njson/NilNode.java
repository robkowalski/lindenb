package org.lindenb.njson;

public class  NilNode extends TerminalNode
	{
	public static final NilNode INSTANCE=new NilNode();
	public NilNode()
		{
		}
	@Override
	public Type getType()
		{
		return Type.NIL;
		}
	@Override
	public void print(java.io.Writer out) throws java.io.IOException
		{
		out.write("null");
		}
	@Override
	public int hashCode()
		{
		return 13;
		}
	@Override
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(o==null || !(o instanceof NilNode)) return false;
		return true;
		}
	@Override
	public Object clone()
		{
		return new NilNode();
		}
	
	@Override
	public String toString()	
		{
		return "null";
		}
	}