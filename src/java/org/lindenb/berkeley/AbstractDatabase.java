package org.lindenb.berkeley;


import java.util.List;
import java.util.Set;
import org.lindenb.util.AbstractWalker;
import org.lindenb.util.Pair;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

/**
 * AbstractDatabase
 * @author pierre
 *
 * @param <K> key type
 * @param <V> value type
 */
public abstract class AbstractDatabase<K, V>
	{
	/** berkeleyDB database */
	private Database database;
	
	public static abstract class SecondaryDB<K2,K1,V1>
		extends AbstractDatabase<K2,V1>
		{
		SecondaryDB(SecondaryDatabase second, AbstractDatabase<K1,V1> primary)
			{
			super(second);
			}
		
		public SecondaryDatabase getSecondaryDatabase()
			{
			return SecondaryDatabase.class.cast(getDatabase());
			}
		
		@Override
		public Cursor cursor(Transaction txn) throws DatabaseException
			{
			CursorConfig cfg= new CursorConfig();
			return getSecondaryDatabase().openSecondaryCursor(txn, cfg);
			}
		}
	

	
	
	
	public class KeyValueIterator
		extends AbstractWalker<Pair<K, V>>
	    {
		protected Cursor cursor=null;
	    protected boolean firstCall=true;
	    protected Pair<DatabaseEntry, DatabaseEntry> _nextValue=new Pair<DatabaseEntry, DatabaseEntry>(
            	new DatabaseEntry(),
            	new DatabaseEntry()
            	);
	    
	    public KeyValueIterator(Cursor cursor)
	            {
	            this.cursor=cursor;
	            }
	    
	    
	    public void close()
	            {
	            BerkeleyUtils.safeClose(this.cursor);
	            this.cursor=null;
	            }
	    

	    public  Pair<K, V> next()
	    		{
	    		if(cursor==null) return null;
	            try {
	            	while(true)
		            	{
	                    //move the cursor to the next position
	                    OperationStatus status= moveCursor();
	                    firstCall=false;
	                    if(status!=OperationStatus.SUCCESS)
	                    	{
	                    	close();
	                    	return null;
	                    	}
	                    if(validateCursor())
	                    	{
	                    	return new Pair<K, V>(
                    	    		entryToKey(_nextValue.first()),
                    	    		entryToValue(_nextValue.second())
                    	    		);
	                    	}
		            	 }
	                   } 
	            catch (DatabaseException e)
	                    {
	                    this.close();
	                    throw new RuntimeException(e);
	                    }
	            }
	    
	    
	    /** move the cursor to its next position */
	    protected OperationStatus moveCursor() throws DatabaseException
	            {
	            return this.cursor.getNext(
	                            _nextValue.first(),
	                            _nextValue.second(),
	                            LockMode.DEFAULT
	                            );
	            }
	    
	    /** gives this iterator a chance to say: ok I've succefully
	     * moved the cursor, now should I stop the scanning ?
	     * @return true if this Iteraror 'hasNext'
	     * @throws DatabaseException
	     */
	    protected boolean validateCursor() throws DatabaseException
	            {
	            return true;
	            }
	    
	    public Cursor getCursor() {
			return cursor;
			}
	    
	    }
	
	protected abstract class DelegateWalker<X>
		extends AbstractWalker<X>
		{
		private KeyValueIterator delegate;
		protected DelegateWalker(KeyValueIterator delegate )
			{
			this.delegate = delegate;
			}

		protected KeyValueIterator getDelegate() {
			return delegate;
			}
		@Override
		public void close() {
			getDelegate().close();
			}
		}
	
	public class KeyWalker
		extends DelegateWalker<K>
		{
		protected KeyWalker(KeyValueIterator delegate )
			{
			super(delegate);
			}
		@Override
		public K next() {
			return getDelegate().next().first();
			}
		}
	
	public class ValueWalker
	extends DelegateWalker<V>
		{
		protected ValueWalker(KeyValueIterator delegate )
			{
			super(delegate);
			}
		
		@Override
		public V next() {
			return getDelegate().next().second();
			}
		}
	
	protected AbstractDatabase(
		Database database
		)
		{
		this.database=database;
		}
	
	public Cursor cursor(Transaction txn) throws DatabaseException
		{
		CursorConfig cfg= new CursorConfig();
		return getDatabase().openCursor(txn,cfg );
		}
	
	public Cursor cursor() throws DatabaseException
		{
		return cursor(null );
		}
	
	public abstract DatabaseEntry keyToEntry(K key);
	public abstract DatabaseEntry valueToEntry(V value);
	public abstract K entryToKey(DatabaseEntry entry);
	public abstract V entryToValue(DatabaseEntry entry);
	
	public OperationStatus delete(K key) throws DatabaseException
		{
		return delete(null,key);
		}
	
	public OperationStatus delete(Transaction txn,K key) throws DatabaseException
		{
		return getDatabase().delete(txn, keyToEntry(key));
		}
	
	public Database getDatabase()
		{
		return this.database;
		}
	
	@Override
	public boolean equals(Object obj) {
		return obj==this;
		}
	
	
	public void close()
		{
		if(this.database==null) return;
		try
			{
			this.database.close();
			}
		catch(Exception err)
			{
			
			}
		this.database=null;
		}
	
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		throw new CloneNotSupportedException("Cannot clone "+getClass());
		}
	
	public long size() throws DatabaseException
		{
		return getDatabase().count();
		}
	
	@Override
	public String toString() {
		return getClass().getName();
		}
	
	
	public String getName() throws DatabaseException
		{
		return getDatabase().getDatabaseName();
		}
	
	public void clear(Transaction txn)throws DatabaseException
		{
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry data=new DatabaseEntry();
		Cursor c=null;
		try {
			c= cursor(txn);
			while(c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				c.delete();
				}
		} catch (DatabaseException e) {
			throw e;
			}
		finally
			{
			try { if(c!=null) c.close();} catch(DatabaseException err) {} 
			}
		}
	
	public void clear()throws DatabaseException
		{
		clear(null);
		}
	
	
	public KeyValueIterator listKeyValues() throws DatabaseException
		{
		return new KeyValueIterator(cursor());
		}
	
	public ValueWalker listValues() throws DatabaseException
		{
		return new ValueWalker(listKeyValues());
		}
	
	public List<V> getValues() throws DatabaseException
		{
		return listValues().asList();
		}
	
	public Set<K> getKeys() throws DatabaseException
		{
		return listKeys().asSet();
		}
	
	public KeyWalker listKeys() throws DatabaseException
		{
		return new KeyWalker(listKeyValues());
		}
	
	public boolean contains(Transaction txn,K key)  throws DatabaseException
		{
		DatabaseEntry e= keyToEntry(key);
		DatabaseEntry data= new DatabaseEntry();
		return(getDatabase().get(txn, e, data, LockMode.DEFAULT)==OperationStatus.SUCCESS);
		}
	
	public boolean contains(K key)  throws DatabaseException
		{
		return contains(null,key);
		}
	
	
	public KeyValueIterator iterator()
		{
		try {
			return listKeyValues();
			}
		catch (DatabaseException e) {
			throw new RuntimeException(e);
			}
		}

	}
