/**
 * 
 */
package org.lindenb.json;


import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;


/**
 * @author pierre
 *
 */
public class JSONArray extends JSONItem
implements Iterable<JSONItem>
{
private static final long serialVersionUID = 1L;
private Vector<JSONItem> elements= new Vector<JSONItem>(1,1);

/**
 * constructor
 *
 */
public JSONArray()
	{
	
	}

/** add an element in the array. Any null item will be converted to JSONConstant.NULL */
public void addElement(JSONItem item)
	{
	this.elements.addElement(item);
	}


@Override
public boolean isArray() {
	return true;
	}

public boolean isEmpty()
	{
	return this.elements.isEmpty();
	}


@Override
public boolean equals(Object o) {
	if (o == this)
		return true;
	if (o == null || !(o instanceof JSONArray))
		return false;
	return this.elements.equals(JSONArray.class.cast(o).elements);
	}

@Override
public int hashCode() {
	return  this.elements.hashCode();
	}


public void clear() {
	elements.clear();
}

public boolean contains(JSONItem item) {
	return elements.contains(item);
}

public boolean removeElement(JSONItem item) {
	return elements.removeElement(item);
}

public void removeElementAt(int index) {
	elements.removeElementAt(index);
}

public int size()
	{
	return this.elements.size();
	}

/** answer a value from its index. returns <code>null</code> if not found
 * @see valueAt
 * */
public JSONItem elementAt(int index)
	{
	return this.elements.elementAt( index);
	}


public Iterator<JSONItem> iterator() {
	return elements.iterator();
	}



@Override
protected void print_(PrintWriter out,int tab)
	{
	for(int i=0;i<tab && tab!=-1;++i) out.print(' ');
	out.print('[');
	for(int i=0;i< size();++i)
		{
		if(i>0) out.print(',');
		if(tab!=-1) out.println();
		for(int j=0;j<tab+1 && tab!=-1;++j) out.print(' ');
		JSONItem item=elementAt(i);
		if(item!=null)
			{
			item.print_(out,tab==-1?tab:tab+1);
			}
		else
			{
			out.print("null");
			}
		}
	for(int i=0;i<tab && tab!=-1;++i) out.print(' ');
	out.print(']');
	
	}

@Override
public Object clone() {
	JSONArray array=new JSONArray();
	for(JSONItem i: this) array.elements.add(JSONItem.class.cast(i.clone()));
	return array;
	}
}
