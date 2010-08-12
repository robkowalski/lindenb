package org.lindenb.njson;

public class  NilNode extends TerminalNode
	{
	public static final NilNode INSTANCE=new NilNode();
	public NilNode()
		{
		}
	public Type getType()
		{
		return Type.NIL;
		}
	public void print(java.io.Writer out) throws java.io.IOException
		{
		out.write("null");
		}
	
	public int hashCode()
		{
		return 13;
		}
	
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(o==null || !(o instanceof NilNode)) return false;
		return true;
		}
	
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