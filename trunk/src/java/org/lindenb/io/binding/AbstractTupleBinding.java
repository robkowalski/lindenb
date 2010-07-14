package org.lindenb.io.binding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * see protected class java.io.Bits http://kickjava.com/src/java/io/Bits.java.htm
 * 
 * AbstractTupleBinding
 *
 */
public abstract class AbstractTupleBinding<T>
	implements TupleBinding<T>
	{
	private byte _buff[]=new byte[8];
	@Override
	public abstract T readObject(InputStream in) throws IOException;
	@Override
	public abstract void writeObject(T object, java.io.OutputStream out) throws IOException;
	
	
	private byte[] fillBuff(InputStream in,int count) throws IOException
		{
		int nRead= in.read(this._buff,0,count);
		if(nRead!=count) throw new IOException("cannot read "+count+" bytes");
		return this._buff;
		}

	public byte readByte(InputStream in) throws IOException
		{
		int c= in.read();
		if(c==-1) throw new IOException("cannot read one byte");
		return (byte)c;
		}
	
	public byte[] readBytes(InputStream in,int count) throws IOException
		{
		byte buffer[]=new byte[count];
		int nRead= in.read(buffer,0,count);
		if(nRead!=count) throw new IOException("cannot read "+count+" bytes");
		return buffer;
		}
	
	public boolean readBool(InputStream in) throws IOException
		{
		return readByte(in)!=0;
		}
	

	public char readChar(InputStream in) throws IOException
		{
		byte b[]=fillBuff(in,2);
		return (char)(((b[1] & 0xFF) << 0) +
				      ((b[0] & 0xFF) << 8));
		}

	public short readShort(InputStream in) throws IOException
		{
		byte b[]=fillBuff(in,2);
		return (short) (((b[1] & 0xFF) << 0) +
						((b[0] & 0xFF) << 8));
		}

	public int readInt(InputStream in) throws IOException
		{
		byte b[]=fillBuff(in,4);
		return ((b[3] & 0xFF) << 0) +
			   ((b[2] & 0xFF) << 8) +
			   ((b[1] & 0xFF) << 16) +
			   ((b[0] & 0xFF) << 24)
			   ;
		}
	public float readFloat(InputStream in) throws IOException
		{
		return Float.intBitsToFloat(readInt(in));
		}
	
	public long readLong(InputStream in) throws IOException
		{
		byte b[]=fillBuff(in,8);
		return ((b[7] & 0xFFL) << 0) +
				((b[6] & 0xFFL) << 8) +
				((b[5] & 0xFFL) << 16) +
				((b[4] & 0xFFL) << 24) +
				((b[3] & 0xFFL) << 32) +
				((b[2] & 0xFFL) << 40) +
				((b[1] & 0xFFL) << 48) +
				((b[0] & 0xFFL) << 56);
		}

	

	public double readDouble(InputStream in) throws IOException
		{
		byte b[]=fillBuff(in,8);
		long j = ((b[7] & 0xFFL) << 0) +
				((b[6] & 0xFFL) << 8) +
				((b[5] & 0xFFL) << 16) +
				((b[4] & 0xFFL) << 24) +
				((b[3] & 0xFFL) << 32) +
				((b[2] & 0xFFL) << 40) +
				((b[1] & 0xFFL) << 48) +
				((b[0] & 0xFFL) << 56);
		return Double.longBitsToDouble(j);
		}
	
	public String readString(InputStream in) throws IOException
		{
		int n=readInt(in);
		return new String(readBytes(in, n));
		}
	
	public BigInteger readBigInteger(InputStream in) throws IOException
		{
		int n=readInt(in);
		return new BigInteger(readBytes(in, n));
		}
	
	/*
	* Methods for packing primitive values into byte arrays starting at given
	* offsets.
	*/

	public void writeByte(byte b,OutputStream out) throws IOException
		{
		out.write(b);
		}

	public void writeBytes(byte b[],OutputStream out) throws IOException
		{
		out.write(b,0,b.length);
		}
	
	public void writeBool(boolean b,OutputStream out) throws IOException
		{
		writeByte((byte) (b ? 1 : 0),out);
		}
	
	public void writeChar(char val,OutputStream out) throws IOException
		{
		this._buff[1] = (byte) (val >>> 0);
		this._buff[0] = (byte) (val >>> 8);
		out.write(this._buff, 0, 2);
		}

	public void writeShort(short val,OutputStream out) throws IOException
		{
		this._buff[1] = (byte) (val >>> 0);
		this._buff[0] = (byte) (val >>> 8);
		out.write(this._buff, 0, 2);
		}
	
	public void writeInt(int val,OutputStream out) throws IOException
		{
		this._buff[3] = (byte) (val >>> 0);
		this._buff[2] = (byte) (val >>> 8);
		this._buff[1] = (byte) (val >>> 16);
		this._buff[0] = (byte) (val >>> 24);
		out.write(this._buff, 0, 4);
		}

	public void writeFloat(float value,OutputStream out) throws IOException
		{
		writeInt(Float.floatToIntBits(value),out);
		}
	public void writeLong(long val,OutputStream out) throws IOException
		{
		this._buff[7] = (byte) (val >>> 0);
		this._buff[6] = (byte) (val >>> 8);
		this._buff[5] = (byte) (val >>> 16);
		this._buff[4] = (byte) (val >>> 24);
		this._buff[3] = (byte) (val >>> 32);
		this._buff[2] = (byte) (val >>> 40);
		this._buff[1] = (byte) (val >>> 48);
		this._buff[0] = (byte) (val >>> 56);
		out.write(this._buff, 0, 8);
		}
	
	public void writeDouble(double val,OutputStream out) throws IOException
		{
		writeLong(Double.doubleToLongBits(val),out);
		}

	public void writeString(String val,OutputStream out) throws IOException
		{
		byte b[]=val.getBytes();
		writeInt(b.length, out);
		writeBytes(b, out);
		}
	
	public void writeBigInteger(BigInteger val,OutputStream out) throws IOException
		{
		byte b[]=val.toByteArray();
		writeInt(b.length, out);
		writeBytes(b, out);
		}
	
	@Override
	public String toString()
		{
		return getClass().getName();
		}
	
	}
