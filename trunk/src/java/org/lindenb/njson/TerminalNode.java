package org.lindenb.njson;

public abstract class  TerminalNode extends Node
	{
	protected TerminalNode()
		{
		}
	@Override
	public boolean isComplex()
		{
		return false;
		}

	}