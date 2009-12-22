/**
 * 
 */
package org.lindenb.util;

/**
 * @author pierre
 * a useful class to store the name of a given key
 * for example use it as an item in a JList/JCombobox to display a verbose label while
 * keeping the id
 *
 */
public class NamedKey<K extends Comparable<K>> extends NamedObject<K> {

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
		return getObject();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj==null || !(obj instanceof NamedKey<?>)) return false;
		NamedKey<?> p= NamedKey.class.cast(obj);
		return this.getId().equals(p.getId())
				;
		}
	}
