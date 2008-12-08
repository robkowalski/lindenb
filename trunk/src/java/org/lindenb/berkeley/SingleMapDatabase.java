package org.lindenb.berkeley;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
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
	
	
	
	
	public V get(K key) throws DatabaseException
		{
		DatabaseEntry value= new DatabaseEntry();
		OperationStatus op=getDatabase().get(null,
				keyToEntry(key),
				value,
				null);
		if(op==OperationStatus.SUCCESS)
			{
			return entryToValue(value);
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
	
	public static class ComparableDB<X extends Comparable<X>,Y>
		extends SingleMapDatabase<X, Y>
		{
		protected ComparableDB(Database db,TupleBinding<X> keyBinding,TupleBinding<Y> valueBinding)
			{
			super(db,keyBinding,valueBinding);
			}	
		
		}
	
	public static class INTEGER<Y>
	extends ComparableDB<Integer, Y>
		{
		public INTEGER(Database db,TupleBinding<Y> valueBinding)
			{
			super(db,new IntegerBinding(),valueBinding);
			}
		}
	
	public static class LONG<Y>
	extends ComparableDB<Long, Y>
		{
		public LONG(Database db,TupleBinding<Y> valueBinding)
			{
			super(db,new LongBinding(),valueBinding);
			}
		}
	
	public static class STRING<Y>
	extends ComparableDB<String, Y>
		{
		public STRING(Database db,TupleBinding<Y> valueBinding)
			{
			super(db,new StringBinding(),valueBinding);
			}
		}
	
	}
