package org.lindenb.berkeley;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public abstract class SingleMapDatabase<K,V>
	extends AbstractDatabase<K,V> {

	public SingleMapDatabase(
			Database database)
		{
		super(database);
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
	
	
	
	private static abstract class AbstractTupleBindingDB<X,Y>
	extends SingleMapDatabase<X, Y>
		{
		public AbstractTupleBindingDB(Database database)
			{
			super(database);
			}
		public abstract TupleBinding<X> getKeyBinding();
		public abstract TupleBinding<Y> getValueBinding();
		@Override
		public X entryToKey(DatabaseEntry entry) {
			return getKeyBinding().entryToObject(entry);
			}
		@Override
		public Y entryToValue(DatabaseEntry entry) {
			return getValueBinding().entryToObject(entry);
			}
		
		public DatabaseEntry keyToEntry(X key)
			{
			DatabaseEntry e= new DatabaseEntry();
			getKeyBinding().objectToEntry(key, e);
			return e;
			};
			
		public DatabaseEntry valueToEntry(Y value)
			{
			DatabaseEntry e= new DatabaseEntry();
			getValueBinding().objectToEntry(value, e);
			return e;
			};
		}
	
	static public class DefaultTupleBindingDB<X,Y>
	extends AbstractTupleBindingDB<X, Y>
		{
		private TupleBinding<X> bindingX;
		private TupleBinding<Y> bindingY;
		public DefaultTupleBindingDB(Database database,TupleBinding<X> bindingX,TupleBinding<Y> bindingY)
			{
			super(database);
			this.bindingX=bindingX;
			this.bindingY=bindingY;
			}
		public  TupleBinding<X> getKeyBinding()
			{
			return this.bindingX;
			}
		public  TupleBinding<Y> getValueBinding()
			{
			return this.bindingY;
			}
		
		}

	}
