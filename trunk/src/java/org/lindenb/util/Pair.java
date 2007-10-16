package org.lindenb.util;



public class Pair<K,V> extends XObject {
	private K key;
	private V value;
	
	public Pair(K key,V value)
		{
		this.key=key;
		this.value=value;
		}
	
	public K first() { return this.key;}
	public V second() { return this.value;}
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj==this) return true;
		if(obj==null || !(obj instanceof Pair)) return false;
		Pair<?,?> p= Pair.class.cast(obj);
		return this.first().equals(p.first()) &&
				this.second().equals(p.second())
				;
		}

	@Override
	public int hashCode()
		{
		return first().hashCode()+second().hashCode();
		}

	@Override
	public String toString() {
		return "("+first().toString()+","+second().toString()+")";
	}

}
