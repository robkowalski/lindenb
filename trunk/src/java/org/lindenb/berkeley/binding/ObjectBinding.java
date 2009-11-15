package org.lindenb.berkeley.binding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/** simple serialization object */
public class ObjectBinding extends TupleBinding<Object>
	{
	private final static byte OP_CODE_NULL=0;
	private final static byte OP_CODE_BOOLEAN=1;
	private final static byte OP_CODE_STRING=2;
	private final static byte OP_CODE_DOUBLE=3;
	private final static byte OP_CODE_LIST=4;
	private final static byte OP_CODE_MAP=5;
	private final static byte OP_CODE_BIG_INTEGER=6;
	private final static byte OP_CODE_INTEGER=7;
	private final static byte OP_CODE_BYTE=8;
	private final static byte OP_CODE_SHORT=9;
	private final static byte OP_CODE_LONG=10;
	private final static byte OP_CODE_FLOAT=11;
	private final static byte OP_CODE_CHAR=12;
	private final static byte OP_CODE_BIG_DECIMAL=13;
	
	
	@Override
	public Object entryToObject(TupleInput in)
		{
		switch(in.readByte())
		{
		case OP_CODE_NULL: return null;
		case OP_CODE_BOOLEAN: return in.readBoolean();
		case OP_CODE_STRING: return in.readString();
		case OP_CODE_BIG_INTEGER: return in.readBigInteger();
		case OP_CODE_INTEGER: return in.readInt();
		case OP_CODE_DOUBLE: return in.readDouble();
		case OP_CODE_FLOAT: return in.readFloat();
		case OP_CODE_BYTE: return in.readByte();
		case OP_CODE_LONG: return in.readLong();
		case OP_CODE_SHORT: return in.readShort();
		case OP_CODE_CHAR: return in.readChar();
		case OP_CODE_LIST:
			{
			int n=in.readInt();
			List<Object> array= new ArrayList<Object>(n);
			for(int i=0;i<n;++i)
				{
				array.add(entryToObject(in));
				}
			return array;
			}
		case OP_CODE_MAP:
			{
			int n=in.readInt();
			Map<String,Object> map= new HashMap<String,Object>(n);
			for(int i=0;i<n;++i)
				{
				map.put(
					in.readString(),	
					entryToObject(in)
					);
				}
			return map;
			}
		case OP_CODE_BIG_DECIMAL:
			{
			return new BigDecimal(in.readString());
			}
		default:throw new IllegalArgumentException();
		}
	}

	@Override
	public void objectToEntry(Object object, TupleOutput out)
		{
		if(object==null)
			{
			out.writeByte(OP_CODE_NULL);
			}
		else if(object instanceof Boolean)
			{
			out.writeByte(OP_CODE_BOOLEAN);
			out.writeBoolean(Boolean.class.cast(object));
			}
		else if(object instanceof Byte)
			{
			out.writeByte(OP_CODE_BYTE);
			out.writeByte(Byte.class.cast(object));
			}
		else if(object instanceof Long)
			{
			out.writeByte(OP_CODE_LONG);
			out.writeLong(Long.class.cast(object));
			}
		else if(object instanceof Short)
			{
			out.writeByte(OP_CODE_SHORT);
			out.writeShort(Short.class.cast(object));
			}
		else if(object instanceof String)
			{
			out.writeByte(OP_CODE_STRING);
			out.writeString(String.class.cast(object));
			}
		else if(object instanceof BigInteger)
			{
			out.writeByte(OP_CODE_BIG_INTEGER);
			out.writeBigInteger(BigInteger.class.cast(object));
			}
		else if(object instanceof Integer)
			{
			out.writeByte(OP_CODE_INTEGER);
			out.writeInt(Integer.class.cast(object));
			}
		else if(object instanceof BigDecimal)
			{
			out.writeByte(OP_CODE_BIG_DECIMAL);
			out.writeString(BigDecimal.class.cast(object).toPlainString());
			}
		else if(object instanceof Float)
			{
			out.writeByte(OP_CODE_FLOAT);
			out.writeFloat(Float.class.cast(object));
			}
		else if(object instanceof Double)
			{
			out.writeByte(OP_CODE_DOUBLE);
			out.writeDouble(Double.class.cast(object));
			}
		else if(object instanceof Character)
			{
			out.writeByte(OP_CODE_CHAR);
			out.writeChar(Character.class.cast(object));
			}
		else if(object instanceof List<?>)
			{
			out.writeByte(OP_CODE_LIST);
			List<?> L=( List<?>)object;
			out.writeInt(L.size());
			for(Object o:L)
				{
				objectToEntry(o,out);
				}
			}
		else if(object.getClass().isArray())
			{
			out.writeByte(OP_CODE_LIST);
			Object L[]=( Object[])object;
			out.writeInt(L.length);
			for(Object o:L)
				{
				objectToEntry(o,out);
				}
			}
		else if(object instanceof Map<?,?>)
			{
			out.writeByte(OP_CODE_MAP);
			Map <?,?> m=(Map <?,?>)object;
			out.writeInt(m.size());
			for(Object o:m.keySet())
				{
				out.writeString(String.valueOf(o));
				objectToEntry(m.get(o),out);
				}
			}
		else
			{
			throw new IllegalArgumentException(
				"Cannot write class"+object.getClass()
				);
			}
		}

	}
