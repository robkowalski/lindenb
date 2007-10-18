package org.lindenb.tool.unix;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class Join
{

/**
* ColumnModel
* a vector of indexes of the table columns
*/
private class ColumnModel implements Iterable<Integer>
    {
    private Vector<Integer> indexes= new Vector<Integer>(2,1);
    private int maxCol=-1;
    
    ColumnModel()
        {
        
        }
    
    public void add(int columnIndex)
        {
        if(columnIndex<0) throw new IllegalArgumentException("Bad index :"+columnIndex);
        if(this.indexes.contains(columnIndex)) throw new IllegalArgumentException("Index defined twice :"+(columnIndex+1));
        this.indexes.addElement(columnIndex);
        this.maxCol=Math.max(this.maxCol, columnIndex);
        }
    
    public int size() { return this.indexes.size();}
    public boolean isEmpty() { return this.indexes.isEmpty();}
    public int at(int index) { return this.indexes.elementAt(index);}
    
    @Override
    public Iterator<Integer> iterator() { return indexes.iterator();}
    }

private abstract class ScalarList
implements Comparable<ScalarList>
	{
	private int row;
	
	public ScalarList(int row)
	    {
	    this.row=row;
	    }
	
	public abstract ColumnModel getColumnModel();
	
	public int compareTo(ScalarList o)
	    {
	    if(o==this) return 0;
	    if(this.getSize()!=o.getSize()) throw new RuntimeException();
	    
	    for(int j=0;j< getSize();++j)
	        {
	        int i= (Join.this.casesensible?
	        		at(j).compareTo(o.at(j)):
	        		at(j).compareToIgnoreCase(o.at(j))
	        		);
	        if(i!=0) return i;
	        }
	    return 0;
	    }
	
	@Override
	public boolean equals(Object obj)
	    {
	    if(obj==this) return true;
	    if(obj==null) return false;
	    return compareTo(ScalarList.class.cast(obj))==0;
	    }
	
	@Override
	public int hashCode()
	    {
	    int i=0;
	    for(int j=0;j< getSize();++j)
	        {
	        i+=(Join.this.casesensible?
	        	at(j).hashCode():
	        	at(j).toLowerCase().hashCode()
	        	);
	        }
	    return i;
	    }
	
	public abstract Vector<Vector<String>> getTable();
	
	public String at(int i)
	    {
	    return getTable().elementAt(this.row).elementAt(getColumnModel().at(i));
	    }
	
	public int getSize()
	    {
	    return getColumnModel().size();
	    }
	
	@Override
	public String toString() {
	    StringBuilder b=new StringBuilder();
	    for(int i=0;i< getSize();++i)
	        {
	        if(i!=0) b.append("-");
	        b.append(at(i));
	        }
	    return b.toString();
	    }
	}


private class Input
	{
	Pattern delimiterIn= Pattern.compile("[\t]");
	ColumnModel columns= new ColumnModel();
	Vector<Vector<String>> table= new Vector<Vector<String>>(10000,100);
	TreeMap<ScalarList, TreeSet<Integer>> scalar2rows= new TreeMap<ScalarList, TreeSet<Integer>>();
	
	
	private int getColumnCount()
		{
		return this.table.isEmpty()?0:this.table.firstElement().size();
		}
	
	private void read(BufferedReader in) throws IOException
	    {
	    int nLine=0;
	    String line;
	    String tokens[];
	    
	    
	    while((line=in.readLine())!=null)
	        {
	        ++nLine;
	        tokens= this.delimiterIn.split(line);
	        if(Join.this.trimTokens)
	        	{
	        	for(int i=0;i< tokens.length;++i)
	        		{
	        		tokens[i]=tokens[i].trim();
	        		}
	        	}
	        if(this.table.isEmpty() &&
	        	this.columns.maxCol>= tokens.length)
	    		{
	    		throw new IOException("Found "+tokens.length+" columns : out of range with indexes");
	    		}
	        
	        if(!this.table.isEmpty() &&
	        	tokens.length!=this.table.firstElement().size())
	            {
	            throw new IOException(
	                    "Expected "+this.table.firstElement().size()+
	                    " columns but found "+
	                    tokens.length+" in "+line);
	            }
	        
	       
	        
	        
	        Vector<String> row= new Vector<String>(tokens.length);
	        for(String s:tokens) row.addElement(s);
	   
	        
	        
	        this.table.addElement(row);
	        
	        ScalarList scalar= new ScalarList(this.table.size()-1)
	        	{
	        	@Override
	        	public ColumnModel getColumnModel() {
	        		return Input.this.columns;
	        		}
	        	@Override
	        	public Vector<Vector<String>> getTable() {
	        		return Input.this.table;
	        		}
	        	};
	        TreeSet<Integer> set= this.scalar2rows.get(scalar);
	        if(set==null)
	        		{
	        		set= new TreeSet<Integer>();
	        		this.scalar2rows.put(scalar,set);
	        		}
	        set.add(this.table.size()-1);
	        }
	    }
	}

private boolean  casesensible=true;
private boolean trimTokens=false;
private Input input1= new Input();
private Input input2= new Input();
private String emptyString="null";

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
private void echo(int n)
	{
	for(int i=0;i< n;++i)
		{
		if(i>0) System.out.print("\t");
		System.out.print(emptyString);
		}
	}
private void echo(Vector<String> row)
	{
	for(int i=0;i< row.size();++i)
		{
		if(i>0) System.out.print("\t");
		System.out.print(row.elementAt(i));
		}
	}


private static BufferedReader open(String uri) throws IOException
	{
	if(	uri.startsWith("http://") ||
			uri.startsWith("https://") ||
			uri.startsWith("file://") ||
			uri.startsWith("ftp://")
    		)
    		{
    		URL url= new URL(uri);
    		if(uri.endsWith(".gz"))
        		{
        		return new BufferedReader(new InputStreamReader(new GZIPInputStream(url.openStream())));
        		}
        	else
        		{
        		return new BufferedReader(new InputStreamReader(url.openStream()));
        		}
    		}
    	else
        	{
        	File fin= new File(uri);
        	if(fin.getName().endsWith(".gz"))
        		{
        		return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fin))));
        		}
        	else
        		{
        		return new BufferedReader(new FileReader(fin));
        		}
        	}
	}

public void exec(String[] args) throws IOException
	{
    int optind=0;
    boolean option_v[]=new boolean[]{false,false};
    boolean option_a[]=new boolean[]{false,false};

    while(optind<args.length)
        {
        if(args[optind].equals("-h"))
            {
            System.out.println("Join [options] (<File>|<file.gz>|<url>|stdin)");
            System.out.println("Author: Pierre Lindenbaum PhD. 2007");
            System.out.println("$LastChangedRevision$");
            System.out.println(" -t1 <regex> pattern used to break the input-1 into tokens default:TAB");
            System.out.println(" -t2 <regex> pattern used to break the input-2 into tokens default:TAB");
            System.out.println(" -1 \'column1,column3,column3,...\' columns for data of input-1.(required)");
            System.out.println(" -2 \'column1,column3,column3,...\' columns for data of input-2 (required)");
            System.out.println(" -v [1/2] print only unmatched lines");
            System.out.println(" -a [1/2] print also unmatched lines");
            System.out.println(" -i case insensitive");
            System.out.println(" -T trim each column");
            System.out.println(" -e <string> word for empty string");
            return;
            }
        else if(args[optind].equals("-i"))
            {
        	this.casesensible=false;
            }
        else if(args[optind].equals("-T"))
	        {
	    	this.trimTokens=true;
	        }
        else if(args[optind].equals("-e"))
	        {
	    	this.emptyString=args[++optind];
	        }
        else if(args[optind].equals("-v"))
	        {
	    	int i= Integer.parseInt(args[++optind].trim());
	    	if(!(i==1 || i==2)) throw new IllegalArgumentException("in option -v expected 1 or 2");
	    	option_v[i-1]=true;
	        }
        else if(args[optind].equals("-a"))
	        {
	    	int i= Integer.parseInt(args[++optind].trim());
	    	if(!(i==1 || i==2)) throw new IllegalArgumentException("in option -a expected 1 or 2");
	    	option_a[i-1]=true;
	        }
        else if(args[optind].equals("-1"))
            {
            assignColumnModel(args[++optind], input1.columns, "t1");
            }
        else if(args[optind].equals("-2"))
	        {
	        assignColumnModel(args[++optind], input2.columns, "t2");
	        }
        else if(args[optind].equals("-t1"))
            {
            input1.delimiterIn= Pattern.compile(args[++optind]);
            }
        else if(args[optind].equals("-t2"))
	        {
	        input2.delimiterIn= Pattern.compile(args[++optind]);
	        }
        else if(args[optind].equals("--"))
            {
            ++optind;
            break;
            }
        else if(args[optind].startsWith("-"))
            {
            throw new IllegalArgumentException("Unknown option "+args[optind]);	
            }
        else
            {
            break;    
            }
        ++optind;
        }
    
    if(input1.columns.isEmpty())
        {
        throw new IllegalArgumentException("-1 undefined");
        }
    
    if(input2.columns.isEmpty())
        {
        throw new IllegalArgumentException("-2 undefined");
        }
    
    if(input2.columns.size()!=input1.columns.size())
	    {
	    throw new IllegalArgumentException("-1 and -2 have not the same number of columns");
	    }
    
   
    
    
    if(optind==args.length)
        {
        throw new IOException("Bad Number of arguments: both input are missing");
        }
    else if(optind+1==args.length)
        {
    	BufferedReader in=open(args[optind]);
    	input1.read(in);
    	input2.read(new BufferedReader(new InputStreamReader(System.in)));
    	in.close();
        }
    else if(optind+2==args.length)
    	{
    	BufferedReader in=open(args[optind]);
    	input1.read(in);
    	in.close();
    	in=open(args[optind+1]);
    	input2.read(in);
    	in.close();
    	}
    else
        {
        throw new IllegalArgumentException("Too many arguments");
        }
    
    if(option_v[0] || option_v[1])
    	{
    	if(option_v[0])
	    	{
	    	for(ScalarList scalar:input1.scalar2rows.keySet())
	    		{
	    		if(input2.scalar2rows.containsKey(scalar)) continue;
	    		for(Integer rowIndex: input1.scalar2rows.get(scalar))
	    			{
	    			Vector<String> row= input1.table.elementAt(rowIndex);
	    			echo(row);
	    			System.out.println();
	    			}
	    		}
	    	}
    	
    	if(option_v[1])
	    	{
	    	for(ScalarList scalar:input2.scalar2rows.keySet())
	    		{
	    		if(input1.scalar2rows.containsKey(scalar)) continue;
	    		for(Integer rowIndex: input2.scalar2rows.get(scalar))
	    			{
	    			Vector<String> row= input2.table.elementAt(rowIndex);
	    			echo(row);
	    			System.out.println();
	    			}
	    		}
	    	}
    	}
    else
    	{
    	for(ScalarList scalar:input1.scalar2rows.keySet())
    		{
    		TreeSet<Integer> set1= input1.scalar2rows.get(scalar);
	    	TreeSet<Integer> set2= input2.scalar2rows.get(scalar);
	    	//print unpaired of set1 
	    	if(set2==null && option_a[0])
	    		{
	    		for(int rowIndex:set1)
		    		{
		    		echo(input1.table.elementAt(rowIndex));
		    		System.out.print('\t');
		    		echo(input2.getColumnCount());
		    		System.out.println();
		    		}
	    		}
	    	else if(set2!=null)
	    		{
	    		for(int rowIndex1:set1)
	    			{
	    			for(int rowIndex2:set2)
		    			{
	    				echo(input1.table.elementAt(rowIndex1));
			    		System.out.print('\t');
			    		echo(input2.table.elementAt(rowIndex2));
			    		System.out.println();
		    			}
	    			}
	    		}

    		}
    	
    	if(option_a[1])
    		{
    		for(ScalarList scalar:input2.scalar2rows.keySet())
	    		{
		    	TreeSet<Integer> set1= input1.scalar2rows.get(scalar);
	    		if(set1==null)
	    			{
	    			TreeSet<Integer> set2= input2.scalar2rows.get(scalar);
	    			for(int rowIndex:set2)
			    		{
	    				echo(input1.getColumnCount());
			    		System.out.print('\t');
			    		echo(input2.table.elementAt(rowIndex));
			    		System.out.println();
			    		}
	    			
	    			}
	    		}
    		}
    	
    	}
    
	}

public static void main(String[] args)
	{
	try
		{
		new Join().exec(args);
	    }
	catch (Throwable e)
	    {
		StackTraceElement t[]=e.getStackTrace();
	    System.err.println("Error Join:"+e.getClass().getName()+":["+t[t.length-1].getLineNumber()+"]: "+e.getMessage());
	    System.exit(-1);
	    }
	}


}
