package org.lindenb.berkeley.db;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class Walker<K,V>
	{

	private Cursor cursor;
	protected BerkeleyDB<K,V> owner;
	private DatabaseEntry key=new DatabaseEntry();
	private DatabaseEntry data=new DatabaseEntry();
	
	Walker(Cursor cursor,BerkeleyDB<K,V> owner)
		{
		this.cursor=cursor;
		this.owner=owner;
		}
	
	public Cursor getCursor()
		{
		return cursor;
		}
	public  BerkeleyDB<K,V> getOwner()
		{
		return this.owner;
		}
	
	public K getKey() throws DatabaseException
		{
		return getOwner().entryToKey(this.key);
		}
	
	public V getValue() throws DatabaseException
		{
		return getOwner().entryToValue(this.data);
		}
	
	
	public void close() throws DatabaseException
		{
		getCursor().close();
		}

	public OperationStatus delete() throws DatabaseException
		{
		return getCursor().delete();
		}

	
	public OperationStatus getSearchKeyRange(K k) throws DatabaseException
		{
		this.key= getOwner().keyToEntry(k);
		return getCursor().getSearchKeyRange(this.key, data, LockMode.DEFAULT);
		}
	
	public boolean next() throws DatabaseException
		{
		return getNext()==OperationStatus.SUCCESS;
		}
	
	public boolean prev() throws DatabaseException
		{
		return getPrev()==OperationStatus.SUCCESS;
		}
	
	public boolean first() throws DatabaseException
		{
		return getFirst()==OperationStatus.SUCCESS;
		}
	
	public boolean last() throws DatabaseException
		{
		return getLast()==OperationStatus.SUCCESS;
		}
	
	public OperationStatus getNext() throws DatabaseException
		{
		return getCursor().getNext(key, data, LockMode.DEFAULT);
		}

	public OperationStatus getPrev() throws DatabaseException
		{
		return getCursor().getPrev(key, data, LockMode.DEFAULT);
		}
	
	public OperationStatus getLast() throws DatabaseException
		{
		return getCursor().getLast(key, data, LockMode.DEFAULT);
		}
	public OperationStatus getFirst() throws DatabaseException
		{
		return getCursor().getFirst(key, data, LockMode.DEFAULT);
		}
	}
