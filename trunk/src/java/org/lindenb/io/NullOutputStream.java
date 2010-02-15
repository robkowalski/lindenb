package org.lindenb.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An outputstream doing nothing but counting the bytes
 *
 */
public class NullOutputStream extends OutputStream
	{
	private long count=0L;
	public NullOutputStream()
		{
		}
	
	@Override
	public void write(int b) throws IOException
		{
		count++;
		}
	@Override
	public void write(byte[] b) throws IOException
		{
		count+=b.length;
		}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
		{
		count+=len;
		}
	
	public long getSize()
		{
		return count;
		}
	public boolean isEmpty()
		{
		return getSize()==0L;
		}
	}
