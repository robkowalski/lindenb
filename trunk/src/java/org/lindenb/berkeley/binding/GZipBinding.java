package org.lindenb.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;


public class GZipBinding<T>
	extends XTupleBinding<T>
	{
	private TupleBinding<T> delegate;
	public GZipBinding(TupleBinding<T> delegate)
		{
		this.delegate= delegate;
		}
	
	public TupleBinding<T> getDelegateBinding()
		{
		return delegate;
		}
	
	@Override
	public T entryToObject(TupleInput in)
		{
		//read and unzip bytes from a DatabaseEntry
		byte array[]=this.readGZippedBytes(in);
		if(array==null) return null;
		//decode unzipped bytes
		return getDelegateBinding().entryToObject(new DatabaseEntry(array));
		}

	@Override
	public void objectToEntry(T o, TupleOutput out)
		{
		if(o==null)
			{
			this.writeGZippedBytes(null, out);
			return;
			}
		//copy object to a fresh DatabaseEntry
		DatabaseEntry entry= new DatabaseEntry();
		getDelegateBinding().objectToEntry(o, entry);
		//zip the data
		this.writeGZippedBytes(entry.getData(),out);
		}

	}
