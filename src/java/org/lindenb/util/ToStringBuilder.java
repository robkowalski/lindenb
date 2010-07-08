package org.lindenb.util;

import java.util.Collection;
import java.util.Map;

/**
 * ToStringBuilder inspired from the apache.common class
 * @author lindenb
 *
 */
public class ToStringBuilder
{
private StringBuilder builder=null;
public ToStringBuilder(String name)
	{
	this.builder=new StringBuilder(name);
	this.builder.append("{");
	}

@SuppressWarnings("unchecked")
private void appendValue(Object value)
	{
	if(value==null)
		{
		this.builder.append("null");
		}
	else if(value instanceof Boolean)
		{
		this.builder.append(Boolean.class.cast(value).toString());
		}
	else if(value instanceof Number)
		{
		this.builder.append(Number.class.cast(value).toString());
		}
	else if(value instanceof CharSequence || value instanceof Character)
		{
		this.builder.append("\"").append(C.escape(String.valueOf(value))).append("\"");
		}
	else if(value instanceof Map)
		{
		this.builder.append("{");
		Map<?,?> map= Map.class.cast(value);
		boolean found=false;
		for(Object k:map.keySet())
			{
			if(found) this.builder.append(",");
			found=true;
			this.builder.append(k.toString()).append(":");
			appendValue(map.get(k));
			}
		this.builder.append("}");
		}
	else if(value instanceof Collection)
		{
		this.builder.append("[");
		boolean found=false;
		for(Object k:Collection.class.cast(value))
			{
			if(found) this.builder.append(",");
			found=true;
			this.builder.append(k.toString());
			}
		this.builder.append("]");
		}
	else
		{
		this.builder.append(String.valueOf(value));
		}
	}



public ToStringBuilder append(String key,Object value)
	{
	this.builder.append(",");
	this.builder.append(key).append(":");
	appendValue(value);
	return this;
	}

@Override
	public String toString() {
		this.builder.append("}");
		return builder.toString();
		}
}
