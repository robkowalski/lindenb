/**
 * 
 */
package org.lindenb.json;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import org.lindenb.util.XObject;


/**
 * @author pierre
 * JSONItem
 * Base class for any node in JSON tree
 */
public abstract class JSONItem
	extends XObject
	implements Serializable,Cloneable
	{
	protected JSONItem()
		{
			
		}
	
	/** print this item in JSON format */
	public void print(Writer out)
		{
		print(out,false);
		}
	
	/** print this item in JSON format with(out) indentation */
	public void print(Writer out,boolean ident)
		{
		PrintWriter pout=new PrintWriter(out,true);
		print_( pout,(ident?0:-1));
		pout.flush();
		}
	

	
	/** print this item in JSON format */
	public void print(PrintStream out)
		{
		print(out,false);
		}
	
	/** print this item in JSON format with(out) indentation */
	public void print(PrintStream out,boolean ident)
		{
		print_(new PrintWriter(out),(ident?0:-1));
		out.flush();
		}
	
	protected abstract void print_(PrintWriter out,int indent);
	
	
	@Override
	public abstract Object clone();
	
	public boolean isArray() { return false;}
	public boolean isObject() { return false;}
	public boolean isConstant() { return false;}
	public boolean isString()
		{
		return isConstant(String.class);
		}
	public boolean isNumber()
		{
		return isConstant(Number.class);
		}
	public boolean isBoolean()
		{
		return isConstant(Boolean.class);
		}
	
	public boolean isConstant(Class<?> clazz)
		{
		return isConstant() &&
			   clazz.isInstance(asConstant().getValue())
			   ;
		}
	/** return this node as a JSONArray, imply this node is a JSONArray */
	public JSONArray asArray() { return JSONArray.class.cast(this);}
	/** return this node as a JSONObject, imply this node is a JSONObject */
	public JSONObject asObject() { return JSONObject.class.cast(this);}
	/** return this node as a JSONConstant, imply this node is a JSONConstant */
	public JSONConstant asConstant() { return JSONConstant.class.cast(this);}
	/** return the String value of this node, imply this node is a JSONConstant */
	public String asString()
		{
		if(!isConstant()) throw new ClassCastException("Cannot cast to JSONConstant :"+getClass());
		JSONConstant cst= asConstant();
		return cst.getValue().toString();
		}
	/** return the Boolean value of this node, imply this node is a JSONConstant */
	public boolean asBoolean()
		{
		if(!isConstant()) throw new ClassCastException("Cannot cast to JSONConstant :"+getClass());
		Object o=asConstant().getValue();
		if(o instanceof Boolean)
			{
			return Boolean.class.cast(o);
			}
		if(o instanceof String)
			{
			return Boolean.parseBoolean(o.toString());
			}
		if(o instanceof Number)
			{
			return Number.class.cast(o).intValue()==1;
			}
		throw new ClassCastException("Cannot cast "+o.getClass()+" to Boolean");
		}
	/** return the Integer value of this node, imply this node is a Constant */
	public int asInteger()
		{
		if(!isConstant()) throw new ClassCastException("Cannot cast to JSONConstant :"+getClass());
		Object o=asConstant().getValue();
		if(o instanceof Number)
			{
			return Number.class.cast(o).intValue();
			}
		if(o instanceof String)
			{
			return Integer.parseInt(o.toString().trim());
			}
		if(o instanceof Boolean)
			{
			return Boolean.class.cast(o)?1:0;
			}
		throw new ClassCastException("Cannot cast "+o.getClass()+" to Integer");
		}
	/** return the Double value of this node, imply this node is a JSONConstant */
	public double asDouble()
		{
		if(!isConstant()) throw new ClassCastException("Cannot cast to JSONConstant :"+getClass());
		Object o=asConstant().getValue();
		if(o instanceof Number)
			{
			return Number.class.cast(o).doubleValue();
			}
		if(o instanceof String)
			{
			return Double.parseDouble(o.toString().trim());
			}
		if(o instanceof Boolean)
			{
			return Boolean.class.cast(o)?1.0:0.0;
			}
		throw new ClassCastException("Cannot cast "+o.getClass()+" to Double");
		}
	
	
	/** find a xpath like structure return null if node not found */
	public JSONItem find(String path)
		{
		JSONItem curr=this;
		while(curr!=null && path.length()>0)
			{
			if(path.startsWith("."))
				{
				path=path.substring(1);
				}
			else if(path.charAt(0)=='[')
				{
				int i=0;
				for(i=1;i< path.length();++i)
					{
					if(path.charAt(i)==']') break;
					}
				int index= Integer.parseInt( path.substring(1,i));
				if(!curr.isArray()) return null;
				if(index<0 || curr.asArray().size()<=index) return null;
				curr=curr.asArray().elementAt(index);
				path=path.substring(i+1);
				}
			else
				{
				int i=0;
				for(i=0;i< path.length();++i)
					{
					if(path.charAt(i)=='.' || path.charAt(i)=='[') break;
					}
				String key=path.substring(0,i);
				path=path.substring(i);
				if(!curr.isObject()) return null;
				if(key.length()==0 || !curr.asObject().containsKey(key)) return null;
				curr=curr.asObject().get(key);
				}
			}
		return curr;
		}
	
	
	/** find a xpath like structure return null if node not found */
	public <T extends JSONItem>  JSONItem find(String path,Class<T> clazz)
		{
		JSONItem item=find(path);
		if(item==null || !clazz.isInstance(item)) return null;
		return clazz.cast(item);
		}
	
	protected void escape(PrintWriter out,String s)
		{
		for(int i=0;i< s.length();++i)
			{
			switch(s.charAt(i))
				{
				case '\'': out.write("\\\'");break;
				case '\"': out.write("\\\"");break;
				case '\t': out.write("\\t");break;
				case '\n': out.write("\\n");break;
				case '\r': out.write("\\r");break;
				default: out.write(s.charAt(i));break;
				}
			}
		}
	
	@Override
	public String toString()
		{
		StringWriter out= new StringWriter();
		print(out);
		return out.toString();
		}
}
