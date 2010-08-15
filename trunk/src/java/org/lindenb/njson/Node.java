package org.lindenb.njson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Node base JSON node
 * @author pierre
 *
 */
public abstract class Node
	implements JSONable
	{
	public enum Type {NIL,BOOL,INT,DECIMAL,STRING,ARRAY,OBJECT};
	protected Node()
		{
		}
	public abstract boolean isComplex();
	public final boolean isTerminal()
		{
		return !isComplex();
		}
	public abstract void print(java.io.Writer out) throws java.io.IOException;
	public void print(java.io.OutputStream out) throws java.io.IOException
		{
		Writer w=new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
		print(w);
		w.flush();
		}
	public abstract Type getType();
	@Override
	public abstract Object clone();
	
	public boolean isNil() { return getType()==Type.NIL;}
	public boolean isBool() { return getType()==Type.BOOL;}
	public boolean isInt() { return getType()==Type.INT;}
	public boolean isDecimal() { return getType()==Type.DECIMAL;}
	public boolean isString() { return getType()==Type.STRING;}
	public boolean isArray() { return getType()==Type.ARRAY;}
	public boolean isObject() { return getType()==Type.OBJECT;}
	
	public NilNode asNil() { return (isNil()?NilNode.class.cast(this):null);}
	public BoolNode asBool() { return (isBool()?BoolNode.class.cast(this):null);}
	public IntNode asInt() { return (isInt()?IntNode.class.cast(this):null);}
	public DecimalNode asDecimal() { return (isDecimal()?DecimalNode.class.cast(this):null);}
	public StringNode asString() { return (isString()?StringNode.class.cast(this):null);}
	public ArrayNode asArray() { return (isArray()?ArrayNode.class.cast(this):null);}
	public ObjectNode asObject() { return (isObject()?ObjectNode.class.cast(this):null);}
	
	@Override
	public Node asJSONNode()
		{
		return this;
		}
	}