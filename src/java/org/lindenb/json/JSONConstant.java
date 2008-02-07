/**
 * 
 */
package org.lindenb.json;


import java.io.PrintWriter;



/**
 * @author pierre
 *
 */
public class JSONConstant extends JSONItem
{
private static final long serialVersionUID = 1L;
public static final JSONConstant TRUE= new JSONConstant(Boolean.TRUE);
public static final JSONConstant FALSE= new JSONConstant(Boolean.FALSE);

/** internal value */
private Object value;

/** constructor with internal value */
public JSONConstant(Object value)
	{
	this.value=value;
	if(value==null) throw new NullPointerException("JSONConstant set to null");
	}

/** return the internal value*/
public Object getValue()
	{
	return value;
	}


@Override
public int hashCode() {
	return getValue().hashCode();
	}

@Override
public boolean equals(Object o) {
	if (o == this)
		return true;
	if (o == null || !(o instanceof JSONConstant))
		return false;
	Object cp = JSONConstant.class.cast(o).getValue();
	return getValue().equals(cp);
	}

@Override
public boolean isConstant() {
	return true;
	}


@Override
protected void print_(PrintWriter out,int tab) {
	if(isString())
		{
		out.print('\"');
		escape(out,value.toString());
		out.print('\"');
		}
	else
		{
		out.print(value);
		}
	}

@Override
public Object clone() {
	return  this;
	}

@Override
public String toString() {
	return value.toString();
	}

}
