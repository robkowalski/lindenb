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
public class NamedObject<K> extends Pair<K,String> {

	/**
	 * @param key
	 * @param value
	 */
	public NamedObject(K key, String name) {
		super(key, name);
		}

	public NamedObject(K key) {
		this(key, key.toString());
		}
	
	public K getObject()
		{
		return first();
		}
	
	public String getName()
		{
		return second();
		}
	
	@Override
	public int hashCode() {
		return getObject()==null?-1:getObject().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj==null || !(obj instanceof NamedObject)) return false;
		NamedObject<?> p= NamedObject.class.cast(obj);
		return this.getObject().equals(p.getObject())
				;
		}
	
	@Override
	public String toString() {
		return getName();
		}
}
