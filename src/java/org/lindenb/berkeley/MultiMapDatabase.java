package org.lindenb.berkeley;


import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public abstract class MultiMapDatabase<K,V>
	extends AbstractDatabase<K,V>
	{
	public MultiMapDatabase( Database database)
		{
		super(database);
		}
	
	public List<V> get(K key)  throws DatabaseException
		{
		List<V> list=new ArrayList<V>();
		DatabaseEntry e= keyToEntry(key);
		DatabaseEntry data= new DatabaseEntry();
		Cursor c=null;
		try {
			c= cursor();
			while(c.getNextDup(e, data, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				list.add(entryToValue(data));
				}
			return list;
		} catch (DatabaseException e2) {
			throw e2;
		} finally
			{
			if(c!=null) try { c.close();} catch(Exception err) {} 
			}
		}
	
	public KeyWalker listKeys() throws DatabaseException
		{
		return new KeyWalker(new KeyValueIterator(cursor())
			{
			@Override
			protected OperationStatus moveCursor() throws DatabaseException
				{
	            return getCursor().getNextNoDup(
                    _nextValue.first(),
                    _nextValue.second(),
                    LockMode.DEFAULT
                    );
				}
			});
		}
	}
