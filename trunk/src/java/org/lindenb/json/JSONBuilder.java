package org.lindenb.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lindenb.util.C;

/**
 * JSONBuilder
 * @author pierre
 *
 */
public class JSONBuilder
	{
	private Map<String,Object> map= new TreeMap<String,Object>();
	private char quote='\'';
	public JSONBuilder put(String key,Object value)
		{
		if(key==null) throw new NullPointerException("key is null");
		if(map.containsKey(key)) throw new IllegalArgumentException("duplicate key:"+key);
		this.map.put(key, value);
		return this;
		}
	
	public void setDoubleQuote()
		{
		this.quote = '\"';
		}
	
	public void setSimpleQuote()
		{
		this.quote = '\'';
		}
	
	public String getString()
		{
		try
			{
			StringWriter s=new StringWriter();
			write(s);
			s.flush();
			return s.toString();
			}
		catch(IOException err)
			{
			throw new RuntimeException(err);
			}
		}
	
	public void write(Writer out) throws IOException
		{
		write(out,this.map);
		}
	
	public void write(Writer b,Object o) throws IOException
		{
		if(o ==null)
			{
			b.append("null");
			}
		else if(o instanceof JSONable)
			{
			b.append(JSONable.class.cast(o).toJSON());
			}
		else if((o instanceof Boolean) || (o instanceof Number))
			{
			b.append(o.toString());
			}
		
		else if((o instanceof Map<?,?>))
			{
			Map<?,?> _map=Map.class.cast(o);
			boolean found=false;
			b.append("{");
			for(Object k:_map.keySet())
				{
				String key= k.toString();
				if(found) b.append(",");
				found=true;
				b.append(this.quote);
				b.append(C.escape(key));
				b.append(this.quote);
				b.append(':');
				write(b,_map.get(key));
				}
			b.append("}");
			}
		else if(o.getClass().isArray())
			{
			Object list[]=(Object[])o;
			b.append("[");
			for(int i=0;i< list.length;++i )
				{
				if(i!=0) b.append(",");
				write(b,list[i]);
				}
			b.append("]");
			}
		else if((o instanceof List<?>))
			{
			List<?> list=List.class.cast(o);
			b.append("[");
			for(int i=0;i< list.size();++i )
				{
				if(i!=0) b.append(",");
				write(b,list.get(i));
				}
			b.append("]");
			}
		else if((o instanceof Calendar))
			{
			Calendar c=Calendar.class.cast(o);
			b.append("{");
			b.append(quote).append("year").append(quote).append(String.valueOf(c.get(Calendar.YEAR))).append(",");
			b.append(quote).append("month").append(quote).append(String.valueOf(1+c.get(Calendar.MONTH))).append(",");
			b.append(quote).append("day").append(quote).append(String.valueOf(c.get(Calendar.DAY_OF_MONTH))).append(",");
			b.append(quote).append("hour").append(quote).append(String.valueOf(c.get(Calendar.HOUR_OF_DAY))).append(",");
			b.append(quote).append("min").append(quote).append(String.valueOf(c.get(Calendar.MINUTE))).append(",");
			b.append(quote).append("sec").append(quote).append(String.valueOf(c.get(Calendar.SECOND)));
			b.append("}");
			}
		else
			{
			b.append(quote).
			  append(C.escape(o.toString())).
			  append(quote)
			  ;
			}
		}
	
	@Override
	public String toString()
		{
		return getString();
		}
	}
