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
	public int hashCode() {
		int result1 = (( first() == null) ? 0 : first().hashCode());
		int result2 = ((second() == null) ? 0 : second().hashCode());
		return 31* (result1+result2);
		}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?,?> other = (Pair<?,?>) obj;
		if (first() == null) {
			if (other.first() != null)
				return false;
		} else if (!first().equals(other.first()))
			return false;
		if (second() == null) {
			if (other.second() != null)
				return false;
		} else if (!second().equals(other.second()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "("+first().toString()+","+second().toString()+")";
	}

}
