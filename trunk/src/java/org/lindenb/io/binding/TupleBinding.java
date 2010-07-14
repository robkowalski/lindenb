package org.lindenb.io.binding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TupleBinding<T>
	{
	public T readObject(InputStream in) throws IOException;
	public void writeObject(T object,OutputStream out)throws IOException;
	}
