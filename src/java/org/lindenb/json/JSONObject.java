/**
 * 
 */
package org.lindenb.json;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author pierre
 *
 */
public class JSONObject extends JSONItem
implements Map<String, JSONItem>
{
private static final long serialVersionUID = 1L;
private HashMap<String, JSONItem> members_= new HashMap<String,JSONItem>();

protected JSONObject()
	{
	
	}

protected HashMap<String, JSONItem> members()
	{
	return this.members_;
	}

public void clear() {
	members().clear();
}

public boolean isEmpty() {
	return members().isEmpty();
}

public Set<String> keySet() {
	return members().keySet();
}

public JSONItem remove(String key) {
	return members().remove(key);
}

public int size() {
	return members().size();
}



/** answer a value from its name. returns <code>null</code> if not found
 * @see getValue
 * */
public JSONItem get(String key) {
	return members().get(key);
}



public JSONItem put(String key, JSONItem value) {
	return members().put(key, value);
}

@Override
public boolean isObject() {
	return true;
	}

@Override
public boolean equals(Object o) {
	if (o == this)
		return true;
	if (o == null || !(o instanceof JSONObject))
		return false;
	return this.members().equals(JSONObject.class.cast(o).members());
	}

@Override
public int hashCode() {
	return members().hashCode();
	}


@Override
protected void print_(PrintWriter out,int tab)
	{
	for(int i=0;i<tab && tab!=-1;++i) out.print(' ');
	out.print('{');
	boolean found=false;
	for(String key: keySet())
		{
		if(found) out.print(',');
		found=true;
		if(tab!=-1) out.println();
		for(int i=0;i<tab+1 && tab!=-1;++i) out.print(' ');
		out.print('\"');
		escape(out,key);
		out.print("\":");
		JSONItem v=get(key);
		if(v==null)
			{
			out.print("null");
			}
		else
			{
			v.print_(out,(tab==-1?tab:tab+1));
			}
		}
	for(int i=0;i<tab && tab!=-1;++i) out.print(' ');
	out.print('}');
	if(tab!=-1) out.println();
	}

@Override
public Object clone() {
	JSONObject o= new JSONObject();
	for(String key: this.keySet())
		{
		o.put(key,JSONItem.class.cast(get(key).clone()));
		}
	return o;
	}


@Override
public boolean containsKey(Object key)
	{
	return this.members().containsKey(key);
	}


@Override
public boolean containsValue(Object value) {
	return this.members().containsValue(value);
	}


@Override
public Set<java.util.Map.Entry<String, JSONItem>> entrySet() {
	return this.members().entrySet();
	}


@Override
public JSONItem get(Object key) {
	return this.members().get(key);
}


@Override
public void putAll(Map<? extends String, ? extends JSONItem> m) {
	this.members().putAll(m);
	}


@Override
public JSONItem remove(Object key) {
	return this.members().remove(key);
}


@Override
public Collection<JSONItem> values() {
	return this.members().values();
	}
}
