package org.lindenb.berkeley;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * SingleMapDatabase
 * implementation of an AbstractDatabase having unique keys
 * @author lindenb
 *
 * @param <K> key type
 * @param <V> value type
 */
public abstract class SingleMapDatabase<K,V>
	extends AbstractDatabase<K,V> {

	/**
	 * SingleMapDatabase
	 */
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
	

	
	/**
	 * return the index of this key in this database
	 * @param key
	 * @return
	 * @throws DatabaseException
	 */
	public long indexOf(K k)  throws DatabaseException
		{
		Cursor c=null;
		try
			{
			DatabaseEntry key= new DatabaseEntry();
			DatabaseEntry data= new DatabaseEntry();
			long i=0L;
			c= getDatabase().openCursor(null, null);
			while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				if(entryToKey(key).equals(k)) return i;
				++i;
				}
			return -1;
			}
		catch(DatabaseException err)
			{
			throw err;
			}
		finally
			{
			if(c!=null) c.close();
			}
		}
	
	public static abstract class AbstractTupleBindingDB<X,Y>
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
		
		public void keyToEntry(X key,DatabaseEntry e)
			{
			getKeyBinding().objectToEntry(key, e);
			};
			
		public void valueToEntry(Y value,DatabaseEntry e)
			{
			getValueBinding().objectToEntry(value, e);
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
