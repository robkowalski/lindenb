package org.lindenb.njson;

import java.io.IOException;
import java.io.StringWriter;

public abstract class  ComplexNode extends Node
	{
	protected ComplexNode()
		{
		}
	public boolean isComplex()
		{
		return true;
		}
	public abstract int size();
	public abstract boolean isEmpty();
	@Override
	public String toString()
		{
		try
			{
			StringWriter s=new StringWriter();
			this.print(s);
			return s.toString();
			}
		catch (IOException e)
			{
			throw new RuntimeException(e);
			}
		}
	}