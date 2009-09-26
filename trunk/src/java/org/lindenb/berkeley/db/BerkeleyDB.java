package org.lindenb.berkeley.db;

import java.io.File;
import java.util.logging.Logger;

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


public abstract class BerkeleyDB<K,V>
	{
	public static final Logger LOG= Logger.getLogger("org.lindenb.berkeley");
	
	
	
	protected static DatabaseConfig defaultDbCfg(Environment env,DatabaseConfig cfg) throws DatabaseException
		{
		if(env==null) throw new NullPointerException("env is null");
		if(cfg!=null) return cfg;
		cfg= new DatabaseConfig();
		cfg.setAllowCreate(env.getConfig().getAllowCreate());
		cfg.setReadOnly(env.getConfig().getReadOnly());
		cfg.setTransactional(env.getConfig().getTransactional());
		return cfg;
		}
	
	/** key binding */
	private TupleBinding<K> keyBinding=null;
	/** valueinding */
	private TupleBinding<V> valueBinding=null;
	
	protected BerkeleyDB(
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding
			)
		{
		this.keyBinding=keyBinding;
		this.valueBinding=valueBinding;
		if(this.keyBinding==null) throw new NullPointerException("keyBinding is null");
		if(this.valueBinding==null) throw new NullPointerException("valueBinding is null");
		}
	
	public String getDefaultName()
		{
		return getClass().getSimpleName().toLowerCase();
		}
	
	public TupleBinding<K> getKeyBinding()
		{
		return this.keyBinding;
		}
	
	public TupleBinding<V> getValueBinding()
		{
		return this.valueBinding;
		}
	
	public abstract Database getDatabase();
	
	protected void clearInternalBuffers()
		{
		//empty
		}
	
	
	public K entryToKey(DatabaseEntry key) throws DatabaseException
		{
		return getKeyBinding().entryToObject(key);
		}
	
	public V entryToValue(DatabaseEntry value) throws DatabaseException
		{
		return getValueBinding().entryToObject(value);
		}
	
	public  void keyToEntry(K key, DatabaseEntry entry) throws DatabaseException
		{
		getKeyBinding().objectToEntry(key, entry);
		}
	
	public  void valueToEntry(V value, DatabaseEntry entry) throws DatabaseException
		{
		getValueBinding().objectToEntry(value, entry);
		}
	
	public final DatabaseEntry valueToEntry(V value) throws DatabaseException
		{
		DatabaseEntry entry= new DatabaseEntry();
		valueToEntry(value,entry);
		return entry;
		}
	
	public  final DatabaseEntry keyToEntry(K key) throws DatabaseException
		{
		DatabaseEntry entry= new DatabaseEntry();
		keyToEntry(key,entry);
		return entry;
		}
	
	public OperationStatus put(Transaction txn,K key,V value) throws DatabaseException
		{
		clearInternalBuffers();
		return getDatabase().put(txn, keyToEntry(key), valueToEntry(value));
		}
	
	public OperationStatus putNoOverwrite(Transaction txn,K key,V value) throws DatabaseException
		{
		clearInternalBuffers();
		return getDatabase().putNoOverwrite(txn, keyToEntry(key), valueToEntry(value));
		}
	
	public final void safeClose()
		{
		try{ close();} catch(Throwable err) {}
		}
	
	public void close() throws DatabaseException
		{
		clearInternalBuffers();
		Database db=getDatabase();
		if(db!=null)
			{
			db.close();
			}
		}
	
	public long count()  throws DatabaseException
		{
		return getDatabase().count();
		}
	public Cursor openCursor(Transaction txn)  throws DatabaseException
		{
		return getDatabase().openCursor(txn, null);
		}
	public String getName() throws DatabaseException
		{
		return getDatabase().getDatabaseName();
		}
	
	public OperationStatus delete(Transaction txn,K key) throws DatabaseException
		{
		clearInternalBuffers();
		return getDatabase().delete(txn, keyToEntry(key));
		}
	
	public void clear(Transaction txn) throws DatabaseException
		{
		clearInternalBuffers();
		Cursor c=null;
		try
			{
			c= openCursor(txn);
			DatabaseEntry key= new DatabaseEntry();
			DatabaseEntry data= new DatabaseEntry();
			while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				c.delete();
				}
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
	
	@Override
	public String toString()
		{
		try
			{
			return getDatabase().getEnvironment().getHome().toString()+
					File.pathSeparator+
				    getName();
			}
		catch (Exception e)
			{
			return "Error:"+e.getMessage();
			}
		}	
	
	public Walker<K,V> openWalker(Transaction txn)
		throws DatabaseException
		{
		Cursor c= openCursor(txn);
		return new Walker<K,V>(c,this);
		}
	
	}
