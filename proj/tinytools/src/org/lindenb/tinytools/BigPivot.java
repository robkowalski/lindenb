package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.IllegalInputException;

import org.lindenb.util.Compilation;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.JoinConfig;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;


/**
 * BigPivot
 *
 */
public class BigPivot
	{
	private static final Logger LOG= Logger.getLogger(BigPivot.class.getName());
	static enum DataType { IGNORE,STRING,ISTRING,INTEGER,BOOLEAN,DOUBLE};
	
	private long rowCount=0L;
	private Environment environment=null;
	private Database index2row=null;
	private List<String> header=null;
	private Pattern delim= Pattern.compile("[\t]");
	private boolean firstLineIsHeader=true;
	private String NULL_VALUE="NULL";
	private SecondaryDatabase leftColumns;
	private SecondaryDatabase topColumns;
	//private Database dataColumns;
	private ColumnModel leftDefinitions=new ColumnModel();
	private ColumnModel topDefinitions=new ColumnModel();
	private ColumnModel dataDefinitions=new ColumnModel();
	private HashMap<Integer,DataType> column2type= new HashMap<Integer, DataType>();
	private boolean trimColumns=true;
	private boolean casesensible =true;
	private static final RowBinding ROW_BINDING = new RowBinding();
	private HashMap<Choice, Boolean> choices= new HashMap<Choice, Boolean>();
	private boolean print_vertical_total=true;
	private boolean print_horizontal_total=true;
	
	private enum Choice
		{
		DEFAULT,
		MIN,MAX,
		SUM,MEAN,
		COUNT_DISTINCT,
		STDEV,
		COUNT,
		HW_FREQ_A1,HW_FREQ_A2,HW_CHI2
		}
	
	/***********************************
	*
	*  class Data
	*
	*/
	private class Data
	    {
	    TreeMap<List<Object>,Integer> data2count=null;//TODO
	    
	    Data()
	        {
	        }
	        

	    
	    void  add(List<Object> val)
	        {
	        Integer i= 0;//TODO dataDefinitions.get(val);
	        if(i==null) i=0;
	        data2count.put(val, i+1);
	        }
	    
	    public String getValue(Choice choice)
	    	{
	    	switch(choice)
	    		{
	    		case HW_CHI2:
	    		case HW_FREQ_A1:
	    		case HW_FREQ_A2:
	    			{
	    			if(BigPivot.this.dataDefinitions.size()!=1)
	    				{
	    				return NULL_VALUE;
	    				}
	    			
	    			HashMap<String, Integer> genotype2count=new HashMap<String, Integer>();
	    			TreeSet<String> alleleSet= new TreeSet<String>();
	    			int total=0;
	    			for(List<Object> d:this.data2count.keySet())
	 			 		{
	    				int n =this.data2count.get(d);
	    				String genotype= null;//TODO d.at(0);
	    				
	    				if(genotype.length()==0 ) continue;//TODO || genotype.equals(EMPTY_VALUE)) continue;
	    				genotype= genotype.toUpperCase().replaceAll("[\\[\\]]", "").trim();
	    				String genotypes[]=genotype.split("[ ,\\-\t]+");
	    				if(genotypes.length==1 && genotype.length()==2)
	    					{
	    					genotypes= new String[]{
	    							""+genotype.charAt(0),
	    							""+genotype.charAt(1)};
	    					}
	    					
	    				if(genotypes.length!=2) continue;
	    				
	    				if(genotypes[0].compareTo(genotypes[1])> 0)
	    					{
	    					String stock= genotypes[0];
	    					genotypes[0]=genotypes[1];
	    					genotypes[1]=stock;
	    					}
	    				genotype= genotypes[0]+genotypes[1];
	    				
	    				alleleSet.add(genotypes[0]);
	    				alleleSet.add(genotypes[1]);
	    				if(alleleSet.size()>2) return NULL_VALUE;
	    				
	    				Integer count= genotype2count.get(genotype);
	    				if(count==null) count=0;
	    				genotype2count.put(genotype,count+n);
	    				total+=n;
	 			 		}
	    			
	    			if(total==0) return NULL_VALUE;
	    			if(alleleSet.size()==1) alleleSet.add("");
	    			String alleles[]=alleleSet.toArray(new String[alleleSet.size()]);
	    			
	    			Integer oAA= genotype2count.get(alleles[0]+alleles[0]);
	    			if(oAA==null) oAA=0;
	    			Integer oAB= genotype2count.get(alleles[0]+alleles[1]);
	    			if(oAB==null) oAB=0;
	    			Integer oBB= genotype2count.get(alleles[1]+alleles[1]);
	    			if(oBB==null) oBB=0;
	    			double freqA=   (2.0*oAA+oAB)/(2.0*total);
	    			
	    			switch(choice)
	    				{
	    				case HW_FREQ_A1:
	    					{
	    					return String.valueOf(alleles[0]+" : "+freqA);
	    					}
	    	    		case HW_FREQ_A2:
	    	    			{
	        				return String.valueOf(alleles[1]+" : "+(1.0-freqA));
	    	    			}
	    	    		default:case HW_CHI2:
	    	    			{
	    	    			if(freqA==1.0) return NULL_VALUE;
	    	    			double eAA=  Math.pow(freqA,2)*total;
	    	    			double eAB=  2.0*freqA*(1.0-freqA)*total;
	    	    			double eBB=  Math.pow((1.0-freqA),2)*total;
	    	    				
	    	    			return String.valueOf(Math.sqrt(
	    	    					Math.pow((oAA-eAA),2)/eAA+
	    	    					Math.pow((oAB-eAB),2)/eAB+
	    	    					Math.pow((oBB-eBB),2)/eBB
	    	    					));
	    	    			}
	    				}
	    			
	    			//break;
	    			}
	    		case COUNT:
	    			{
	    			int n=0;
	    			for(Object d:this.data2count.keySet()) n+=this.data2count.get(d);
	    			return String.valueOf(n);
	    			}
	    		case COUNT_DISTINCT:
	    			{
	    			return String.valueOf(this.data2count.size());
	    			}
			case STDEV:
	    		case MIN:
	    		case MAX:
	    		case SUM:
	    		case MEAN:
	    			{
	    			if(BigPivot.this.dataDefinitions.size()!=1)
	    				{
	    				return NULL_VALUE;
	    				}
	    			double min= Double.MAX_VALUE;
	    			double max=-Double.MAX_VALUE;
	    			double total=0.0;
	    			int count=0;
	    			 for(List<Object> d:this.data2count.keySet())
	    			 	{
	    				try {
							Double val= 0.0;//TODO new Double(d.at(0));
							min=Math.min(min, val);
							max=Math.max(max, val);
							int c= data2count.get(d);
							total+= val.doubleValue()*c;
							count+=c;
	    					} 
	    				catch (Exception e)
	    					{
						//ignore
						}
	    			 	}
	    			if(count==0) return NULL_VALUE;
	    			double mean=total/count;
	    			switch(choice)
	    				{
	    				case MIN: return String.valueOf(min);
	    				case MAX: return String.valueOf(max);
	    				case SUM: return String.valueOf(total);
	    				case MEAN:return String.valueOf(mean);
	    				case STDEV:
	    					{
	    					if(count==1) return NULL_VALUE;
	    					double stdev=0;
	    					 for(List<Object> d:this.data2count.keySet())
	    	    			 	{
	    	    				try {
	    							Double val= 0.0;//TODO new Double(d.at(0));
	    							int c= data2count.get(d);
	    							stdev+=(c*Math.pow(mean-val,2));
	    	    					}
	    	    				catch (Exception e)
	    	    					{
	    	    					//ignore
	    	    					}
	    	    			 	}
	    	    				return String.valueOf(Math.sqrt(stdev/(count-1)));
	    					}
	    				}
	    			return NULL_VALUE;
	    			}
	    		case DEFAULT:
	    			{
			        int i=data2count.size();
			        if(i==0)
			            {
			            return NULL_VALUE;
			            }
			        StringBuilder b= new StringBuilder();
			        if(i>1) b.append("{");
			        boolean found=false;
			        for(Object d:this.data2count.keySet())
			            {
			            if(found) b.append(";");
			            found=true;
			            int c= data2count.get(d);
			            b.append(d.toString());
			            if(!(i==1 && c==1)) b.append(":"+c);
			            }
			            
			        if(i>1) b.append("}");
			        return b.toString();
	    			}
	    		default:return NULL_VALUE;
	    		}
	    	}
	    
	    
	    @Override
	    public String toString()
	        {
	    	return getValue(Choice.DEFAULT);
	        }
	    }






	
	/**
	* ColumnModel
	* a vector of indexes of the table columns
	*/
	private static class ColumnModel implements Iterable<Integer>
	    {
	    private List<Integer> indexes= new ArrayList<Integer>();
	    private int maxCol=-1;
	    
	    ColumnModel()
	        {
	        
	        }
	    
	    public void add(int columnIndex)
	        {
	        if(columnIndex<0) throw new IllegalArgumentException("Bad index :"+columnIndex);
	        if(this.indexes.contains(columnIndex)) throw new IllegalArgumentException("Index defined twice :"+(columnIndex+1));
	        this.indexes.add(columnIndex);
	        this.maxCol=Math.max(this.maxCol, columnIndex);
	        }
	    
	    public int size() { return this.indexes.size();}
	    public boolean isEmpty() { return this.indexes.isEmpty();}
	    public int at(int index) { return this.indexes.get(index);}
	    
	    @Override
	    public Iterator<Integer> iterator() { return indexes.iterator();}
	    }
	
	/**
	 * RowBinding: TupleBinding for a List<Object> 
	 *
	 */
	static public class RowBinding
		extends TupleBinding<List<Object>>
		{
		@Override
		public List<Object> entryToObject(TupleInput input)
			{
			//get the size of the row
			int listSize = input.readInt();
			List<Object> list= new ArrayList<Object>(listSize);
			//loop over the items
			for(int i=0;i< listSize;++i)
				{
				switch(DataType.values()[input.readByte()])
					{
					case IGNORE: list.add(null); break;
					case STRING:case ISTRING: list.add(input.readString()); break;
					case BOOLEAN: list.add(input.readBoolean()); break;
					case DOUBLE: list.add(input.readDouble()); break;
					case INTEGER:list.add(input.readBigInteger()); break;
					default: throw new IllegalStateException("Unknown data type");
					}
				}
			return list;
			}
		@Override
		public void objectToEntry(List<Object> list, TupleOutput output)
			{
			//write the size of the row
			output.writeInt(list.size());
			//save each item
			for(int i=0;i< list.size();++i)
				{
				Object o= list.get(i);
				if(o==null)
					{
					output.writeByte((byte)DataType.IGNORE.ordinal());
					}
				else if(o.getClass()==String.class)
					{
					output.writeByte((byte)DataType.STRING.ordinal());
					output.writeString(String.class.cast(o));
					}
				else if(o.getClass()==Boolean.class)
					{
					output.writeByte((byte)DataType.BOOLEAN.ordinal());
					output.writeBoolean(Boolean.class.cast(o));
					}
				else if(o.getClass()==BigInteger.class)
					{
					output.writeByte((byte)DataType.INTEGER.ordinal());
					output.writeBigInteger(BigInteger.class.cast(o));
					}
				else if(o.getClass()==Double.class)
					{
					output.writeByte((byte)DataType.DOUBLE.ordinal());
					output.writeDouble(Double.class.cast(o));
					}
				else
					{
					throw new IllegalStateException("Class not handled "+o.getClass());
					}
				}
			}
		}
	
	/**
	 * Row Comparator
	 * @author lindenb
	 *
	 */
	public static class RowComparator
		implements Comparator<byte[]>
		{
		
		@SuppressWarnings("unchecked")
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
		}
	
private abstract class AbstractPrinter
		{
		protected PrintStream out;
		protected AbstractPrinter(PrintStream out)
			{
			this.out=out;
			}

		protected abstract void oTable();
		protected abstract void cTable();
		protected abstract void oTR();
		protected abstract void cTR();
		protected abstract void TH(Object s);
		protected abstract void TD(Object s);
		protected BigPivot getPivot() { return BigPivot.this;}
		
		private void print() throws DatabaseException
		    {
			if(getPivot().header==null) return;
		  
		    Vector<Choice> display=new Vector<Choice>();
		    for(Choice c: Choice.values())
		    	{
		    	if(getPivot().choices.get(c))
		    		{
		    		display.addElement(c);
		    		}
		    	}
		   
		    
		    
		    /**********************************************
		     *
		     * Titles TOP
		     *
		     **********************************************/
		
		    oTable();
		    
		    DatabaseEntry keyEntry1= new DatabaseEntry();
		    DatabaseEntry dataEntry1= new DatabaseEntry();
		    DatabaseEntry keyEntry2= new DatabaseEntry();
		    DatabaseEntry dataEntry2= new DatabaseEntry();
		    DatabaseEntry dataEntry3= new DatabaseEntry();
		    long topColumnCount= getPivot().topColumns.count();
		    long leftColumnCount= getPivot().leftColumns.count();
		    Cursor topCursor=getPivot().topColumns.openCursor(null, null);
		    Cursor leftCursor=getPivot().leftColumns.openCursor(null, null);
		    
		    for(int x=0;x< getPivot().topDefinitions.size();++x)
		        {
		    	oTR();
		        //add empty columns for left_labels
		        for(int i=0;i< getPivot().leftDefinitions.size();++i)
		            {
		            TH("");
		            }
		
		        //offset of left header min/max/mean...
		        TH(getPivot().header.get(getPivot().topDefinitions.at(x)));
		        
		        
		        
		        //reload
		        keyEntry1= new DatabaseEntry();
		        if(topCursor.getFirst(keyEntry1, dataEntry1, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
		        	{
		        	throw new RuntimeException("Cannot go first");
		        	}
		        while(topCursor.getNext(keyEntry1, dataEntry1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
		            {
		        	List<Object> rtop = ROW_BINDING.entryToObject(dataEntry1);
		            TH(rtop.get(x));
		            }
		       
		        TH("");//add an extra column will be used for 'Total' on the right, fill with col name
		        cTR();
		        }
		    
		    
		    /**********************************************
		     *
		     * add one extra line that will contains left header labels
		     *
		     **********************************************/    
		    oTR();
		    for(Integer j: getPivot().leftDefinitions)
		        {
		        TH(getPivot().header.get(j));
		        }
		    TH("Data");
		    for(long i=0;i< topColumnCount;++i)
		        {
		        TH(String.valueOf(i+1));
		        }
		    if(getPivot().print_horizontal_total)
				{
				TH("Total");
				}
		    cTR();
		    
		    
		    
		    keyEntry1= new DatabaseEntry();
		    topCursor=getPivot().topColumns.openCursor(null, null);
		    //loop over the distinct rows
		    while(leftCursor.getNextNoDup(keyEntry1, dataEntry1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
		        {
		    	if(getPivot().index2row.get(null, dataEntry1, dataEntry3, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
		    		{
		    		throw new RuntimeException("Cannot get row");
		    		}
		    	List<Object> rleft = ROW_BINDING.entryToObject(dataEntry3);
		        for(int displayIndex=0;displayIndex<display.size();++displayIndex)
			        {
		        	oTR();
			        for(int i=0;i< rleft.size();++i)
			            {
			            TH(rleft.get(i));
			            }
			        keyEntry2= new DatabaseEntry();
			        if(topCursor.getFirst(keyEntry2, dataEntry2, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
			        	{
			        	throw new RuntimeException("Cannot go first");
			        	}
			        
			        TH(getLabel(display.elementAt(displayIndex)));
			        while(topCursor.getNextNoDup(keyEntry2, dataEntry2, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			            {
			        	List<Object> rtop= ROW_BINDING.entryToObject(dataEntry2);
			            Data data =getData(
			                    rleft,
			                    rtop
			                    );
			            TD(data.getValue(display.elementAt(displayIndex)));
			            }
			        
			        if(BigPivot.this.print_horizontal_total)
				        {
				        Data dataTotal=getData(rleft,null);
				        TD(dataTotal.getValue(display.elementAt(displayIndex)));
				        }
			        
			        cTR();
			        }
		        }
		   
		    
		    //bottom total
		    if(BigPivot.this.print_vertical_total)
			    {
			    for(int displayIndex=0;displayIndex<display.size();++displayIndex)
			        {
			    	oTR();
			        for(int i=0;i< leftDefinitions.size();++i)
			            {
			            TH("");
			            }
			        
			      //TODO re-open topCursor
			        
			        while(topCursor.getNextNoDup(keyEntry2, dataEntry2, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			        	{
			        	List<Object> rtop= ROW_BINDING.entryToObject(dataEntry2);
			           
			            Data data =getData(null,
			            		rtop
			                    );
			            TD(data.getValue(display.elementAt(displayIndex)));
			            }
			        
			        Data dataTotal=getData(null,null);
			        TD(dataTotal.getValue(display.elementAt(displayIndex)));
			        
			        
			        cTR();
			        }
			    }
		    
		   
		    
		    cTable();
		    leftCursor.close();
		    topCursor.close();
		    }
		
		}	
		
	
	
	
	
	private BigPivot() 
		{
		}
	
	private Data getData(List<Object> rLeft,List<Object> rtop)
		throws DatabaseException
	    {
	    Data data=new Data();
	    HashSet<Integer> set= null;
	    SecondaryCursor cursorLeft=null;
	    SecondaryCursor cursorTop=null;
	    JoinCursor jc=null;
	    DatabaseEntry keyL=null;
        DatabaseEntry dataL=null;
        DatabaseEntry keyT =null;
        DatabaseEntry dataT=null;
        List<SecondaryCursor> cursors= new ArrayList<SecondaryCursor>();
	    try
		    {
		    if(rLeft!=null)
		    	{
		    	cursorLeft = leftColumns.openSecondaryCursor(null, null);
		    	keyL= new DatabaseEntry();
		    	dataL= new DatabaseEntry();
		    	ROW_BINDING.objectToEntry(rLeft, keyL);
		    	if(cursorLeft.getSearchKey(keyL, dataL, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
		    		{
		    		cursors.add(cursorLeft);
		    		}
		    	
		    	}
		    if(rtop!=null)
		    	{
		    	cursorTop = topColumns.openSecondaryCursor(null, null);
		    	keyT= new DatabaseEntry();
		    	dataT= new DatabaseEntry();
		    	ROW_BINDING.objectToEntry(rtop, keyT);
		    	cursorTop.getSearchKey(keyT, dataT, LockMode.DEFAULT);
		    	if(cursorTop.getSearchKey(keyT, dataT, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
		    		{
		    		cursors.add(cursorTop);
		    		}
		    	}
		    if(rLeft==null && rtop==null)
		    	{
		    	
		    	}
		    
		    JoinConfig jcfg=new JoinConfig();
		     jc= this.index2row.join(cursors.toArray(new Cursor[cursors.size()]), null);
		    DatabaseEntry key=new DatabaseEntry();
	        DatabaseEntry value=new DatabaseEntry();
		    while( jc.getNext(key, value, null)==OperationStatus.SUCCESS)
		    	{
		    	List<Object> row= ROW_BINDING.entryToObject(value);
		    	 if(!this.dataDefinitions.isEmpty())
		            {
		            data.add(row);
		            }
		        else
		            {
		            data.add(null);
		            }
		    	}
	
		    return data;
		    }
	    catch(DatabaseException err)
	    	{
	    	throw err;
	    	}
	    finally
	    	{
	    	if(cursorLeft!=null) cursorLeft.close();
	    	if(cursorTop!=null) cursorTop.close();
	    	if(jc!=null) jc.close();
	    	}
	    }
	
	
	
	private static String getLabel(Choice c)
		{
		switch(c)
			{
			default:
				return c.toString().toLowerCase().replace('_', '-');
			}
		//return c.toString();
		}	
	
	private void open(File dir) throws DatabaseException,IOException
		{
		if(!dir.exists()) throw new IOException(dir.toString()+" does not exist");
		if(!dir.isDirectory()) throw new IOException(dir.toString()+" is not a directory");
		close();
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
		SecondaryConfig cfg2nd= new SecondaryConfig();
		cfg2nd.setAllowCreate(true);
		cfg2nd.setReadOnly(false);
		cfg2nd.setTemporary(true);
		cfg2nd.setSortedDuplicates(true);
		cfg2nd.setKeyCreator(new SecondaryKeyCreator()
			{
			@Override
			public boolean createSecondaryKey(SecondaryDatabase secondary,
					DatabaseEntry key, DatabaseEntry data,
					DatabaseEntry result) throws DatabaseException
				{
				if(leftDefinitions.isEmpty()) return false;
				//original row
				List<Object> list = ROW_BINDING.entryToObject(data);
				
				//Left definitions
				List<Object> list2= new ArrayList<Object>(BigPivot.this.leftDefinitions.size());
				for(int i=0;i< leftDefinitions.size();++i)
					{
					list2.add(list.get(leftDefinitions.at(i)));
					}
				ROW_BINDING.objectToEntry(list2, result);
				return true;
				}
			});
		this.leftColumns= this.environment.openSecondaryDatabase(null, "left"+rand.nextLong(), index2row,  cfg2nd);
	 	
		
		cfg2nd= new SecondaryConfig();
		cfg2nd.setAllowCreate(true);
		cfg2nd.setReadOnly(false);
		cfg2nd.setTemporary(true);
		cfg2nd.setSortedDuplicates(true);
		cfg2nd.setKeyCreator(new SecondaryKeyCreator()
			{
			@Override
			public boolean createSecondaryKey(SecondaryDatabase secondary,
					DatabaseEntry key, DatabaseEntry data,
					DatabaseEntry result) throws DatabaseException
				{
				if(topDefinitions.isEmpty()) return false;
				//original row
				List<Object> list = ROW_BINDING.entryToObject(data);
				
				//Left definitions
				List<Object> list2= new ArrayList<Object>(BigPivot.this.topDefinitions.size());
				for(int i=0;i< topDefinitions.size();++i)
					{
					list2.add(list.get(topDefinitions.at(i)));
					}
				ROW_BINDING.objectToEntry(list2, result);
				return true;
				}
			});
		this.topColumns= this.environment.openSecondaryDatabase(null, "top"+rand.nextLong(),index2row,  cfg2nd);
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
	
	public void print()
		{
		
		}
	
	/**
	 * Read input Stream
	 * @param in
	 * @throws IOException
	 * @throws DatabaseException
	 */
	public void read(BufferedReader in) throws IOException,DatabaseException
		{
		LOG.info("Start Reading");
		DatabaseEntry keyEntry= new DatabaseEntry();
		DatabaseEntry dataEntry= new DatabaseEntry();
		//initialize row Count
		rowCount=0L;
		//initialize header
		header=null;
		String line;
		String tokens[];
		//column we can ignore and replace by NIL
		Set<Integer> unusedColumns=new HashSet<Integer>();
		
		while((line=in.readLine())!=null)
			{
			tokens= delim.split(line);
			if(header==null)
				{
				LOG.info("parsing header");
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
				//default set all column as unused
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
				if(trimColumns) s=s.trim();
				if(!casesensible) s=s.toLowerCase();
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
					DataType b=column2type.get(i);
					if(b==null) continue;
					switch(b)
						{
						case INTEGER: list.set(i, new BigInteger(s)); break;
						case DOUBLE: list.set(i, new Double(s)); break;
						case BOOLEAN: list.set(i, new Boolean(s)); break;
						case ISTRING: list.set(i, s.toLowerCase());break;
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
				throw new RuntimeException("Cannot insert "+line);
				}

			rowCount++;
			}
		}
	
	
	
	static private void assignColumnModel(String arg,ColumnModel columnModel,String option)
		{
		if(columnModel.size()!=0)
			{
		    throw new IllegalArgumentException("-"+option+" defined twice");
		    }
		String tokens[]=arg.split("[,]");
		
		for(String s:tokens)
		    {
		    s=s.trim();
		    if(s.length()==0) continue;
		    try {
		        Integer i= new Integer(s);
		        if(i<1) throw new IllegalArgumentException("in option -"+option+" bad index"+arg+" <1 ");
		        columnModel.add(i-1);
		        }
		    catch (NumberFormatException e)
		        {
		        throw new IllegalArgumentException("Bad Column in "+arg,e);
		        }
		    }
		}
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args)
		{
		BigPivot pivot= new BigPivot();
		try
			{
			String directory="/tmp/pivot";
			
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.out.println("Pivot [options] (<File>|<file.gz>|<url>|stdin)");
					System.err.println(Compilation.getLabel());
	                System.out.println("Author: Pierre Lindenbaum PhD. 2009");
	                System.out.println(" -h help (this screen)");
	                System.out.println(" -L \'column1,column2,column3,...\' columns for left. (required)");
	                System.out.println(" -T \'column1,column2,column3,...\' columns for top. (optional)");
	                System.out.println(" -D \'column1,column3,column3,...\' columns for data.(required)");
	                System.out.println(" -C columns types: \'column1:type1,column2:type2,column3:type3,...\' with type in:");
	                for(DataType dt: DataType.values())
	                		{
	                		if(dt!=DataType.IGNORE)
	                		System.out.println("\n"+dt);
	                		}
	                System.out.println(" -p <regex> pattern used to break the input into tokens default:TAB");
	                System.out.println(" -i case insensitive");
	                System.out.println(" -t trim each column");
	                System.out.println(" -null <string> value for null");
	                System.out.println(" -f first line is NOT the header");
	                //System.out.println(" -html html output");
	                //System.out.println(" -no-vt disable vertical summary");
	                //System.out.println(" -no-ht disable horizontal summary");
	                //System.out.println(" -hw (Hardy Weinberg display option)");
					}
				else if(args[optind].equals("-C"))
					{
					String tokens[]=args[++optind].split("[,]");
					
					for(String s:tokens)
					    {
					    s=s.trim();
					    if(s.length()==0) continue;
					    int loc= s.indexOf(":");
					    if(loc==-1)  throw new IllegalArgumentException("Bad column type "+s);
					    
					    try {
					    	int column= Integer.parseInt(s.substring(0,loc).trim());
					    	if(column<1) throw new IllegalArgumentException("in option -C bad index:"+column+" <1 ");
					    
					    	DataType t=  DataType.valueOf(s.substring(loc+1).trim());
					        if(pivot.column2type.containsKey(column-1))
					        	{
					        	throw new IllegalArgumentException("Column Def "+ (column)+" defined twice");
					        	}
					        pivot.column2type.put(column-1, t);
					        }
					    catch (NumberFormatException e)
					        {
					        throw new IllegalArgumentException("Bad Column index in "+s,e);
					        }
					    catch (EnumConstantNotPresentException e)
					        {
					        throw new IllegalArgumentException("Bad Column type in "+s,e);
					        }
					    }
					}
				else if(args[optind].equals("-L"))
					{
					assignColumnModel(args[++optind],pivot.leftDefinitions,"L");
					}
				else if(args[optind].equals("-T"))
	                {
	            	assignColumnModel(args[++optind],pivot.topDefinitions,"T");
	                }
	            else if(args[optind].equals("-D"))
	                {
	            	assignColumnModel(args[++optind],pivot.dataDefinitions,"D");
	                }
	            else if(args[optind].equals("-p"))
	                {
	                pivot.delim= Pattern.compile(args[++optind]);
	                }
	            else if(args[optind].equals("-i"))
		            {
	            	pivot.casesensible=false;
		            }
	            else if(args[optind].equals("-t"))
		            {
		            pivot.trimColumns=true;
		            }
	            else if(args[optind].equals("-null"))
		            {
		            pivot.NULL_VALUE=args[++optind];
		            }
	            else if(args[optind].equals("-f"))
		            {
		            pivot.firstLineIsHeader=false;
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
			
			pivot.open(new File(directory));
			if(optind==args.length)
		        {
		        pivot.read(new BufferedReader(new InputStreamReader(System.in)));
		        }
			else if(optind+1==args.length)
		        {
		    	BufferedReader in=IOUtils.openReader(args[optind]);
		        pivot.read(in);
		        in.close();
		        }
		    else
		        {
		        throw new IllegalArgumentException("Too many arguments");
		        }
			
			} 
	catch(Throwable err)
		{
		err.printStackTrace();
		}
	finally
		{
		pivot.close();
		}
	
	
	}
}
