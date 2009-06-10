package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.IllegalInputException;
import org.lindenb.util.Compilation;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;

public class BigPivot
	{
	static final byte IGNORE=0;
	static final byte STRING=1;
	static final byte INTEGER=2;
	static final byte BOOLEAN=3;
	static final byte DOUBLE=4;
	
	private long rowCount=0L;
	private Environment environment=null;
	private Database index2row=null;
	private List<String> header=null;
	private Pattern delim= Pattern.compile("[\t]");
	private boolean firstLineIsHeader=true;
	private String NULL_VALUE="NULL";
	private SecondaryDatabase leftColumns;
	private SecondaryDatabase topColumns;
	private SecondaryDatabase dataColumns;
	private List<Integer> leftDefinitions=new ArrayList<Integer>();
	private List<Integer> topDefinitions=new ArrayList<Integer>();
	private List<Integer> dataDefinitions=new ArrayList<Integer>();
	private HashMap<Integer,Byte> column2type= new HashMap<Integer, Byte>();
	

	
	/**
	 * RowBinding
	 *
	 */
	static public class RowBinding
		extends TupleBinding<List<Object>>
		{
		@Override
		public List<Object> entryToObject(TupleInput input)
			{
			int listSize = input.readInt();
			List<Object> list= new ArrayList<Object>(listSize);
			for(int i=0;i< listSize;++i)
				{
				switch(input.readByte())
					{
					case IGNORE: list.add(null); break;
					case STRING: list.add(input.readString()); break;
					case BOOLEAN: list.add(input.readBoolean()); break;
					case DOUBLE: list.add(input.readDouble()); break;
					case INTEGER:list.add(input.readBigInteger()); break;
					default: throw new IllegalStateException("Unknown opcode");
					}
				}
			return list;
			}
		@Override
		public void objectToEntry(List<Object> list, TupleOutput output)
			{
			output.writeInt(list.size());
			for(int i=0;i< list.size();++i)
				{
				Object o= list.get(i);
				if(o==null)
					{
					output.writeByte(IGNORE);
					}
				else if(o.getClass()==String.class)
					{
					output.writeByte(STRING);
					output.writeString(String.class.cast(o));
					}
				else if(o.getClass()==Boolean.class)
					{
					output.writeByte(BOOLEAN);
					output.writeBoolean(Boolean.class.cast(o));
					}
				else if(o.getClass()==BigInteger.class)
					{
					output.writeByte(INTEGER);
					output.writeBigInteger(BigInteger.class.cast(o));
					}
				else if(o.getClass()==Double.class)
					{
					output.writeByte(DOUBLE);
					output.writeDouble(Double.class.cast(o));
					}
				else
					{
					throw new IllegalStateException("Class not handled "+o.getClass());
					}
				}
			}
		}
	/*
	public static class RowComparator
		implements Comparator<byte[]>
		{
	
		@Override
		public int compare(byte[] array1, byte[] array2)
			{
			RowBinding binding= new RowBinding();//not a member of the class because comparator are shared between thread
			List<Object> list1 = binding.entryToObject(new DatabaseEntry(array1));
			List<Object> list2 = binding.entryToObject(new DatabaseEntry(array2));
			if(list1.size()!=list2.size()) throw new IllegalStateException("Not the same size");
			for(int i=0;i< list1.size();++i)
				{
				Object o1= list1.get(i);
				Object o2= list2.get(i);
				if(o1==null)
					{
					if(o2==null) continue;
					return -1;
					}
				if(o2==null) return 1;
				if(o1.getClass()!=o2.getClass())  throw new IllegalStateException("Not the same Classes");
				int c= Comparable.class.cast(o1).compareTo(o2);
				if(c!=0) return 0;
				}
			return 0;
			}
		}*/
	
	
	private static final RowBinding ROW_BINDING = new RowBinding();
	
	
	private BigPivot(File dir) throws DatabaseException,IOException
		{
		if(!dir.exists()) throw new IOException(dir.toString()+" does not exist");
		if(!dir.isDirectory()) throw new IOException(dir.toString()+" is not a directory");
		EnvironmentConfig envCfg= new EnvironmentConfig();
		envCfg.setAllowCreate(true);
		envCfg.setReadOnly(false);
		this.environment= new Environment(dir,envCfg);
		DatabaseConfig dbCfg= new DatabaseConfig();
		dbCfg.setAllowCreate(true);
		dbCfg.setReadOnly(false);
		dbCfg.setTemporary(true);
		Random rand= new Random();
		this.index2row= this.environment.openDatabase(null, "index2row"+rand.nextLong(), dbCfg);
		SecondaryConfig config2= new SecondaryConfig();
		config2.setAllowCreate(true);
		config2.setReadOnly(false);
		config2.setTemporary(true);
		//config2.setDuplicateComparator(RowComparator.class);
		config2.setSortedDuplicates(true);
		this.leftColumns= this.environment.openSecondaryDatabase(null, "left"+rand.nextLong(), this.index2row, config2);
		this.topColumns= this.environment.openSecondaryDatabase(null, "top"+rand.nextLong(), this.index2row, config2);
		this.dataColumns= this.environment.openSecondaryDatabase(null, "data"+rand.nextLong(), this.index2row, config2);
		}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
		}
	
	public void close()
		{
		if(this.leftColumns!=null)
			{
			try{ leftColumns.close();} catch(Throwable err) {}
			this.leftColumns=null;
			}
		if(this.topColumns!=null)
			{
			try{ topColumns.close();} catch(Throwable err) {}
			this.topColumns=null;
			}
		if(this.dataColumns!=null)
			{
			try{ dataColumns.close();} catch(Throwable err) {}
			this.dataColumns=null;
			}
		if(this.index2row!=null)
			{
			try{ index2row.close();} catch(Throwable err) {}
			this.index2row=null;
			}
		
		if(environment!=null)
			{
			try{ environment.close();} catch(Throwable err) {}
			this.environment=null;
			}
		}
	
	
	
	public long getRowCount()
		{
		return rowCount;
		}
	
	public void read(BufferedReader in) throws IOException,DatabaseException
		{
		DatabaseEntry keyEntry= new DatabaseEntry();
		DatabaseEntry dataEntry= new DatabaseEntry();
		rowCount=0L;
		header=null;
		String line;
		String tokens[];
		Set<Integer> unusedColumns=new HashSet<Integer>();
		while((line=in.readLine())!=null)
			{
			tokens= delim.split(line);
			if(header==null)
				{
				if(firstLineIsHeader)
					{
					header=new ArrayList<String>(tokens.length);
					for(String s:tokens)
						{
						if(s.startsWith("#")) s=s.substring(1);
						s=s.trim();
						if(s.length()==0) s="$"+(header.size()+1);
						header.add(s);
						}
					
					}
				else
					{
					for(int i=0;i< tokens.length;++i)
						{
						header.add("$"+(i+1));
						}
					}
				
				for(int i=0;i< tokens.length;++i)
					{
					unusedColumns.add(i);
					}
				
				for(Integer i: leftDefinitions)
					{
					unusedColumns.remove(i);
					if(i  >= header.size())
						{
						throw new IOException("Found "+this.header.size()+" columns : out of range with left indexes");
						}
					}
				for(Integer i: topDefinitions)
					{
					unusedColumns.remove(i);
					if(i  >= header.size())
						{
						throw new IOException("Found "+this.header.size()+" columns : out of range with top indexes");
						}
					}
				for(Integer i: dataDefinitions)
					{
					unusedColumns.remove(i);
					if(i  >= header.size())
						{
						throw new IOException("Found "+this.header.size()+" columns : out of range with data indexes");
						}
					}
				if(firstLineIsHeader) continue;
				}
			
			
			
			List<Object> list= new ArrayList<Object>(tokens.length);
			for(String s:tokens)
				{
				list.add(s);
				}
			while(list.size() < header.size())
				{
				list.add(NULL_VALUE);
				}
			
			if(list.size()!=header.size())
				{
				throw new org.lindenb.lang.IllegalTokenCount(this.header.size(),tokens);
				}
			
			
			for(int i=0;i< list.size();++i)
				{
				String s= String.class.cast(list.get(i));
				if(s==null || NULL_VALUE.equals(s) || unusedColumns.contains(i))
					{
					list.set(i, null);
					continue;
					}
				//TODO if column never used set NULL
				
				try
					{
					Byte b=column2type.get(i);
					if(b==null) continue;
					switch(b)
						{
						case INTEGER: list.set(i, new BigInteger(s)); break;
						case DOUBLE: list.set(i, new Double(s)); break;
						case BOOLEAN: list.set(i, new Boolean(s)); break;
						case STRING: break;
						case IGNORE: list.set(i, null); break;
						default: throw new IllegalStateException("Bad column type "+b);
						}
					}
				catch(Throwable err)
					{
					throw new IllegalInputException("Bad input "+s+" column "+i+" "+err.getMessage());
					}
				}
			
			LongBinding.longToEntry(rowCount, keyEntry);
			ROW_BINDING.objectToEntry(list, dataEntry);
			if(this.index2row.put(null, keyEntry, dataEntry)!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot insert "+line);
				}
			
			//Left definitions
			List<Object> list2= new ArrayList<Object>(this.leftDefinitions.size());
			for(int i=0;i< leftDefinitions.size();++i)
				{
				list2.add(list.get(leftDefinitions.get(i)));
				}
			ROW_BINDING.objectToEntry(list2, dataEntry);
			if(this.leftColumns.put(null, dataEntry, keyEntry)!= OperationStatus.SUCCESS)//yes data and then key
				{
				throw new DatabaseException("Cannot insert "+line);
				}
			
			
			//top definitions
			list2= new ArrayList<Object>(this.topDefinitions.size());
			for(int i=0;i< topDefinitions.size();++i)
				{
				list2.add(list.get(topDefinitions.get(i)));
				}
			ROW_BINDING.objectToEntry(list2, dataEntry);
			if(this.topColumns.put(null, dataEntry, keyEntry)!= OperationStatus.SUCCESS)//yes data and then key
				{
				throw new DatabaseException("Cannot insert "+line);
				}
			
			//data definitions
			list2= new ArrayList<Object>(this.dataDefinitions.size());
			for(int i=0;i< dataDefinitions.size();++i)
				{
				list2.add(list.get(dataDefinitions.get(i)));
				}
			ROW_BINDING.objectToEntry(list2, dataEntry);
			if(this.dataColumns.put(null, dataEntry, keyEntry)!= OperationStatus.SUCCESS)//yes data and then key
				{
				throw new DatabaseException("Cannot insert "+line);
				}
			
			
			rowCount++;
				
			}
		}
	
	
	public static void main(String[] args) {
	try
		{
		int optind=0;
		while(optind< args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println(Compilation.getLabel());
				}
			else if(args[optind].equals("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].startsWith("-"))
				{
				System.err.println("Unknown option "+args[optind]);
				}
			else 
				{
				break;
				}
			++optind;
			}
		if(optind==args.length)
                {
                //TODO
                }
        else
                {
                while(optind< args.length)
                        {
                      	java.io.BufferedReader r= IOUtils.openReader(args[optind++]);
                        
                        r.close();
                        }
                }
		
		} 
	catch(Throwable err)
		{
		err.printStackTrace();
		}
	
	
	
	}
}
