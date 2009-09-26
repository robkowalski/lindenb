package org.lindenb.berkeley.db;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class PrimaryDB<K,V>
	extends BerkeleyDB<K,V>
	{
	private Database database;
	
	
	
	public PrimaryDB(
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
		if(dbConfig.getSortedDuplicates()) throw new IllegalArgumentException();
		if(databaseName==null)
			{
			databaseName= getDefaultName();
			}
		this.database= env.openDatabase(txn, databaseName, dbConfig);
		}
	
	public boolean containsKey(Transaction txn,K key) throws DatabaseException
		{
		DatabaseEntry data= new DatabaseEntry();
		return getDatabase().get(txn, keyToEntry(key), data, LockMode.DEFAULT)==OperationStatus.SUCCESS;
		}
	
	public V get(Transaction txn,K key) throws DatabaseException
		{
		DatabaseEntry data= new DatabaseEntry();
		return
			getDatabase().get(txn, keyToEntry(key), data, LockMode.DEFAULT)!=OperationStatus.SUCCESS
			?  null: entryToValue(data);
		}

	
	public Database getDatabase()
		{
		return this.database;
		}
	
	
	
	
	}
