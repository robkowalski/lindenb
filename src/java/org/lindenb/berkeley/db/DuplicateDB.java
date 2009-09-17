package org.lindenb.berkeley.db;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public abstract class DuplicateDB<K,V>
	extends BerkeleyDB<K,V>
	{
	private Database database;
	public DuplicateDB(
			Environment env,
			Transaction txn,
			String databaseName,
			DatabaseConfig dbConfig,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding
			) throws DatabaseException
		{
		super(keyBinding,valueBinding);
		dbConfig=defaultDbCfg(env,dbConfig);
		dbConfig.setSortedDuplicates(true);
		if(databaseName==null)
			{
			databaseName= getDefaultName();
			}
		this.database= env.openDatabase(txn, databaseName, dbConfig);
		}
	
	public List<V> get(Transaction txn,K key) throws DatabaseException
		{
		Cursor c=null;
		List<V> list= new ArrayList<V>();
		try
			{
			DatabaseEntry ke = keyToEntry(key);
			DatabaseEntry kd = new DatabaseEntry();
			c= openCursor(txn);
			while((list.isEmpty()
				 ?c.getSearchKey(ke, kd, LockMode.DEFAULT)
				 :c.getNextDup(ke, kd, LockMode.DEFAULT))==OperationStatus.SUCCESS)
				{
				list.add(entryToValue(kd));
				}
			return list;
			}
		catch (DatabaseException e)
			{
			throw e;
			}
		finally
			{
			if(c!=null) c.close();
			}
		}
	
	
	
	public OperationStatus putNoDupData(Transaction txn,K key,V value) throws DatabaseException
		{
		clearInternalBuffers();
		return getDatabase().putNoDupData(txn, keyToEntry(key), valueToEntry(value));
		}
	
	public Database getDatabase()
		{
		return this.database;
		}
	}
