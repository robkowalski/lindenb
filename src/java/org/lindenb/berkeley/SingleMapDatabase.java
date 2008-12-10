package org.lindenb.berkeley;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class SingleMapDatabase<K,V>
	extends AbstractDatabase<K,V> {

	public SingleMapDatabase(
			Database database,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding) {
		super(database, keyBinding, valueBinding);
		}
	
	
	public OperationStatus remove(Transaction txn,K key) throws DatabaseException
		{
		return getDatabase().delete(txn, keyToEntry(key));
		}
	
	public OperationStatus remove(K key) throws DatabaseException
		{
		return remove(null,key);
		}

	/**  Stores the key/data pair into the database. */
	public  OperationStatus put(Transaction txn,K key,V value) throws DatabaseException
		{
		return getDatabase().put(txn,
				keyToEntry(key),
				valueToEntry(value)
				);
		}
	
	/**  Stores the key/data pair into the database. */
	public  OperationStatus put(K key,V value) throws DatabaseException
		{
		return put(null,key,value);
		}
	
	
	public V get(Transaction txn,K key) throws DatabaseException
		{
		DatabaseEntry value= new DatabaseEntry();
		OperationStatus op=getDatabase().get(txn,
				keyToEntry(key),
				value,
				null);
		if(op==OperationStatus.SUCCESS)
			{
			return entryToValue(value);
			}
		return null;
		}
	
	public V get(K key) throws DatabaseException
		{
		return get(null,key);
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
	
	private static class SingleComparableDB<X extends Comparable<X>,Y>
		extends SingleMapDatabase<X, Y>
		{

		public SingleComparableDB(Database database,
				TupleBinding<X> keyBinding, TupleBinding<Y> valueBinding)
			{
			super(database,keyBinding,valueBinding);
			}
		
	
		
		}
	
	public static class INTEGER<Y>
	extends SingleComparableDB<Integer, Y>
		{
		public INTEGER(Database database,
				TupleBinding<Y> valueBinding)
			{
			super(database,new IntegerBinding(),valueBinding);
			}
		}
	
	public static class LONG<Y>
	extends SingleComparableDB<Long, Y>
		{
		public LONG(Database database,
				TupleBinding<Y> valueBinding)
			{
			super(database,new LongBinding(),valueBinding);
			}
		}
	
	
	public static class STRING<Y>
	extends SingleComparableDB<String, Y>
		{
		public STRING(Database database,
				TupleBinding<Y> valueBinding)
			{
			super(database,new StringBinding(),valueBinding);
			}
		}
	

	}
