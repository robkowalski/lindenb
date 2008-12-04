package org.lindenb.berkeley;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lindenb.util.Pair;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * AbstractDatabase
 * @author pierre
 *
 * @param <K> key type
 * @param <V> value type
 */
public abstract class AbstractDatabase<K, V>
	{
	private Database database;
	private TupleBinding<K> keyBinding;
	private TupleBinding<V> valueBinding;
	
	/**
	 * AbstractIterator
	 *
	 */
	protected abstract class AbstractIterator<X>
	implements Iterator<X>
		{
		@Override
		public void remove() {
			close();
			throw new UnsupportedOperationException("Cannot remove");
			}
		/** transforms this iterator as a Set */
		public Set<X> asSet()
			{
			Set<X> set= new HashSet<X>();
			while(hasNext()) set.add(next());
			close();
			return set;
			}
		
		/** transforms this iterator as a List */
		public List<X> asList(int start,int count)
			{
			int index=-1;
			List<X> set= new ArrayList<X>();
			while(hasNext())
				{
				++index;
				if(index >= (start+count)) break;
				if(index >= start)
					{
					set.add(next());
					}
				}
			close();
			return set;
			}
		
		/** transforms this iterator as a List */
		public List<X> asList()
			{
			List<X> set= new ArrayList<X>();
			while(hasNext()) set.add(next());
			close();
			return set;
			}
		
		/** count number of items */
		public int count()
			{
			int i=0;
			while(hasNext()) { next(); ++i;}
			close();
			return i;
			}
		
		public void close()
			{
			
			}
		}
	
	
	public class KeyValueIterator
		extends AbstractIterator<Pair<K, V>>
	    {
		private boolean _eofMet=false;
		private boolean _hasNextTested=false;
		private boolean _hasNext=false;
	    private Cursor cursor=null;
	    
	    protected Pair<DatabaseEntry, DatabaseEntry> _nextValue=new Pair<DatabaseEntry, DatabaseEntry>(
            	new DatabaseEntry(),
            	new DatabaseEntry()
            	);
	    
	    public KeyValueIterator(Cursor cursor)
	            {
	            this.cursor=cursor;
	            }
	    
	    @Override
	    public void remove()
	            {
	            try {
	                getCursor().delete();
	                }
	            catch (DatabaseException e)
	            	{
	                throw new RuntimeException(e);
	                }
	            }
	    
	    public void close()
	            {
	            if(this.cursor!=null)
	                    {
	                   	try { this.cursor.close();} catch(Throwable err) { }
	                    this.cursor=null;
	                    }
	            }
	    

	    public boolean hasNext()
	    		{
	            if(this._eofMet) return false;
	            if(this._hasNextTested) return this._hasNext;
	            this._hasNextTested=true;
	            this._hasNext=false;
	            try {
                    //move the cursor to the next position
                    OperationStatus status= moveCursor();
                    //move was a success and we validate this position
                    if(!(status==OperationStatus.SUCCESS &&
                            validateCursor()))
                            {
                            this._hasNext=false;
                            this._eofMet=true;
                            close();
                            }
                    else
                            {
                            this._hasNext=true;
                            }
	                    } 
	            catch (DatabaseException e)
	                    {
	                    this.close();
	                    this._eofMet=true;
	                    throw new RuntimeException(e);
	                    }
	            return this._hasNext;
	            }
	    
	    @Override
	    public Pair<K, V> next()
	    	{
	    	if(_eofMet) throw new IllegalStateException("EOF-MET");
	    	if(!_hasNextTested)
	    		{
	    		hasNext();
	    		}
	    	if(!_hasNext)  throw new IllegalStateException();
	    	return new Pair<K, V>(
	    		getKeyBinding().entryToObject(_nextValue.first()),
	    		getValueBinding().entryToObject(_nextValue.second())
	    		);
	    	}
	
	    protected Cursor getCursor()
            {
            return this.cursor;
            }
	    
	    /** move the cursor to its next position */
	    protected OperationStatus moveCursor() throws DatabaseException
	            {
	            return getCursor().getNext(
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
	    
	    }
	
	protected abstract class DelegateIterator<X>
		extends AbstractIterator<X>
		{
		private KeyValueIterator delegate;
		protected DelegateIterator(KeyValueIterator delegate )
			{
			this.delegate = delegate;
			}
		
		@Override
		public boolean hasNext() {
			return getDelegate().hasNext();
			}
		
		protected KeyValueIterator getDelegate() {
			return delegate;
			}
		}
	
	public class KeyIterator
		extends DelegateIterator<K>
		{
		protected KeyIterator(KeyValueIterator delegate )
			{
			super(delegate);
			}
		@Override
		public K next() {
			return getDelegate().next().first();
			}
		}
	
	public class ValueIterator
	extends DelegateIterator<V>
		{
		protected ValueIterator(KeyValueIterator delegate )
			{
			super(delegate);
			}
		
		@Override
		public V next() {
			return getDelegate().next().second();
			}
		}
	
	protected AbstractDatabase(
		Database database,
		TupleBinding<K> keyBinding,
		TupleBinding<V> valueBinding
		)
		{
		this.database=database;
		}
	
	protected Cursor cursor() throws DatabaseException
		{
		CursorConfig cfg= new CursorConfig();
		return getDatabase().openCursor(null,cfg );
		}
	
	protected TupleBinding<K> getKeyBinding() {
		return keyBinding;
		}
	
	protected TupleBinding<V> getValueBinding() {
		return valueBinding;
		}
	
	public Database getDatabase()
		{
		return this.database;
		}
	
	@Override
	public boolean equals(Object obj) {
		return obj==this;
		}
	
	
	public void close() throws DatabaseException
		{
		if(this.database==null) return;
		this.database.close();
		this.database=null;
		}
	
	@Override
	protected Object clone() throws CloneNotSupportedException
		{
		throw new CloneNotSupportedException("Cannot clone "+getClass());
		}
	
	@Override
	public String toString() {
		return getClass().getName();
		}
	}
