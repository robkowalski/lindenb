package org.lindenb.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Bidiectional input reader
 * @author pierre
 *
 */
public class YReader extends Reader {
	private Reader in1;
	private Reader in2;
	private boolean useStream1=true;
	public YReader(Reader in1,Reader in2)
		{
		this.in1=in1;
		if(in1==null ) throw new NullPointerException("reader 1 is null");
		this.in2=in2;
		if(in2==null ) throw new NullPointerException("reader 2 is null");
		}
	
	
	@Override
	public int read() throws IOException {
		if(useStream1)
			{
			int c= this.in1.read();
			if(c!=-1)
				{
				return c;
				}
			useStream1=false;
			}
		return this.in2.read();
		}
	
	@Override
	public int read(char[] b) throws IOException {
		return read(b,0,b.length);
		}
	
	@Override
	public int read(char[] b, int off, int len) throws IOException {
		if(useStream1)
			{
			int n= this.in1.read(b,off,len);
			if(n!=-1)
				{
				return n;
				}
			useStream1=false;
			}
		return this.in2.read(b,off,len);
		}
	
	@Override
	public void close() throws IOException {
		this.in1.close();
		this.in2.close();
		}
}
