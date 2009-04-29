package org.lindenb.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tee input stream
 * @author pierre
 *
 */
public class TInputStream extends InputStream {
	private InputStream in;
	private OutputStream out;
	
	/**
	 * Initialize with out=System.err
	 * @param in input stream
	 */
	public TInputStream(InputStream in)
		{
		this(in,System.err);
		}
	
	/**
	 * @param in input stream
	 * @param out output stream
	 */
	public TInputStream(InputStream in,OutputStream out)
		{
		this.in=in;
		if(in==null ) throw new NullPointerException("input is null");
		this.out=out;
		if(out==null ) throw new NullPointerException("output is null");
		}
	
	
	@Override
	public int read() throws IOException {
		int c= in.read();
		if(c!=-1) out.write(c);
		return c;
		}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
		}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
		{	
		int n= this.in.read(b,off,len);
		if(n!=-1)  out.write(b, off, n);
		return n;
		}
	
	@Override
	public void close() throws IOException {
		this.in.close();
		this.out.flush();
		}
}
