/**
 * 
 */
package org.lindenb.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A  writer writing to multiple other writers
 *
 */
public class YWriter extends Writer {
	private Writer writers[];
	/**
	 * YWriter
	 */
	public YWriter(Writer...writers)
		{
		this.writers=writers;
		}



	/**
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException
		{
		IOException err=null;
		for(Writer w: this.writers)
			{
			try {
				w.close();
			} catch (IOException e) {
				err=e;
				}
			}
		if(err!=null) throw err;
		}

	/**
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		IOException err=null;
		for(Writer w: this.writers)
			{
			try {
				w.flush();
			} catch (IOException e) {
				err=e;
				}
			}
		if(err!=null) throw err;
		}

	/**
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		IOException err=null;
		for(Writer w: this.writers)
			{
			try {
				w.write(cbuf,off,len);
			} catch (IOException e) {
				err=e;
				}
			}
		if(err!=null) throw err;
		}


	
	}
