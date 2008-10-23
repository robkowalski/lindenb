package org.lindenb.wikipedia.api;

public abstract class QueryFilter
	{
	public static class Revision extends QueryFilter
		{
		public boolean accept(org.lindenb.wikipedia.api.Revision rev)
			{
			return true;
			}
		}
	
	public int getMaximumSize()
		{
		return Integer.MAX_VALUE;
		}
	}
