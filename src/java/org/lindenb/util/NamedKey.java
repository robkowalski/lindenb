/**
 * 
 */
package org.lindenb.util;

/**
 * @author pierre
 *
 */
public class NamedKey<K extends Comparable<K>> extends Pair<K,String> {

	/**
	 * @param key
	 * @param value
	 */
	public NamedKey(K key, String name) {
		super(key, name);
		}

	public NamedKey(K key) {
		this(key, key.toString());
		}
	
	public K getId()
		{
		return first();
		}
	
	public String getName()
		{
		return second();
		}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj==null || !(obj instanceof NamedKey)) return false;
		NamedKey<?> p= NamedKey.class.cast(obj);
		return this.getId().equals(p.getId())
				;
		}
	
	@Override
	public String toString() {
		return getName();
		}
}
