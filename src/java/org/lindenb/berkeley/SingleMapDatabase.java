package org.lindenb.berkeley;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;

public class SingleMapDatabase<K,V>
	extends AbstractDatabase<K,V> {

	public SingleMapDatabase(
			Database database,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding) {
		super(database, keyBinding, valueBinding);
		}
	
	public V get(K key) throws DatabaseException
		{
		Cursor cursor=null;
		try {
			cursor= cursor();
			return null;
			} 
		catch (DatabaseException e) {
			
			}
		finally
			{
			cursor.close();
			}
		return null;
		}
	
	public Map<K, V> get(Set<K> keys) throws DatabaseException
		{
		HashMap<K, V> map= new HashMap<K, V>();
		for(K key:keys)
			{
			V val= get(key);
			if(val==null) continue;
			map.put(key,val);
			}
		return map;
		}
	}
