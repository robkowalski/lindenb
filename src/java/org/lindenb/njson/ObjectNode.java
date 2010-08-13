package org.lindenb.njson;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class  ObjectNode extends ComplexNode
	implements Map<String,Node>
	{
	private java.util.Map<String,Node> map;
	
	public ObjectNode()
		{
		this.map=new java.util.HashMap<String,Node>();
		}
	
	public ObjectNode(int capacity)
		{
		this.map=new java.util.HashMap<String,Node>(capacity);
		}
	@Override
	public Type getType()
		{
		return Type.OBJECT;
		}
	
	@Override
	public int size()
		{
		return this.map.size();
		}
	
	@Override
	public boolean isEmpty()
		{
		return this.map.isEmpty();
		}
	@Override
	public void print(java.io.Writer out) throws java.io.IOException
		{
		out.write('{');
		boolean first=true;
		for(String s:map.keySet())
			{
			if(!first) out.write(',');
			first=false;
			out.write(s);
			out.write(':');
			get(s).print(out);
			}
		out.write('}');
		}
	@Override
	public int hashCode()
		{
		return map.hashCode();
		}
	@Override
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(o==null || !(o instanceof ObjectNode)) return false;
		return ObjectNode.class.cast(o).map.equals(map);
		}
	@Override
	public Object clone()
		{
		ObjectNode a= new ObjectNode(size());
		for(String s:map.keySet())
			{
			a.put(s,Node.class.cast(get(s).clone()));
			}
		return a;
		}

	@Override
	public void clear()
		{
		this.map.clear();
		}

	@Override
	public boolean containsKey(Object key)
		{
		return this.map.containsKey(key);
		}

	@Override
	public boolean containsValue(Object value)
		{
		return this.map.containsValue(value);
		}

	@Override
	public Set<java.util.Map.Entry<String, Node>> entrySet()
		{
		return this.map.entrySet();
		}

	@Override
	public Node get(Object key)
		{
		return this.map.get(key);
		}

	/** 
	 * get value for given key having 'type'.
	 * @param key
	 * @param type
	 * @return null if doesn't contains object or if value has not the right type
	 */
	public Node get(Object key,Node.Type type)
		{
		Node n=this.get(key);
		return (n==null||n.getType()!=type?null:n);
		}
	
	public NilNode getNil(String key) { return NilNode.class.cast(get(key,Type.NIL));}
	public BoolNode getBool(String key) { return BoolNode.class.cast(get(key,Type.BOOL));}
	public IntNode getInt(String key) { return IntNode.class.cast(get(key,Type.INT));}
	public DecimalNode getDecimal(String key) { return DecimalNode.class.cast(get(key,Type.FLOAT));}
	public StringNode getString(String key) { return StringNode.class.cast(get(key,Type.STRING));}
	public ArrayNode getArray(String key) { return ArrayNode.class.cast(get(key,Type.ARRAY));}
	public ObjectNode getObject(String key) { return ObjectNode.class.cast(get(key,Type.OBJECT));}
	public TerminalNode getTerminal(String key) {Node n=get(key); return (n==null|| !n.isTerminal()?null:TerminalNode.class.cast(n));}
	public ComplexNode getComplex(String key) {Node n=get(key); return (n==null|| !n.isComplex()?null:ComplexNode.class.cast(n));}
	
	@Override
	public Set<String> keySet()
		{
		return this.map.keySet();
		}

	@Override
	public void putAll(Map<? extends String, ? extends Node> m)
		{
		this.map.putAll(m);
		}

	@Override
	public Node remove(Object key)
		{
		return this.map.remove(key);
		}

	@Override
	public ArrayNode values()
		{
		Collection<Node> n=this.map.values();
		ArrayNode a=new ArrayNode(n.size());
		a.addAll(n);
		return a;
		}

	@Override
	public Node put(String key, Node value)
		{
		return  put(key,value);
		}
	}