package org.lindenb.tool.unix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Assert;
import org.lindenb.util.Pair;

public class GroupBy
	{
	private Vector<Integer> columns= new Vector<Integer>(2,1);
	private int maxCol=-1;

	
	private Pattern delim=Pattern.compile("[\t]");
    private boolean caseSensible=true;
    private TreeMap <Tokens, Tokens> tokens = new TreeMap<Tokens, Tokens>();
    private Vector<Pair<Action, Object>> appendAction= new Vector<Pair<Action,Object>>(1,1);
    
   
    private static  enum Action
        {
        SUM,COUNT,MEAN,MIN,MAX
        }
   
   
    private static abstract class Tail
        {
        abstract void touch(String tokens[]);
        abstract void echo(PrintStream out);
        }
   
   
    private static class TailCount extends Tail
        {
        int count=0;
        @Override
        void touch(String tokens[])
            {
            ++count;
            }
        @Override
        void echo(PrintStream out)
            {
            out.print(count);
            }
        }
   
    private static abstract class TailOnColumn  extends Tail
        {
        protected int column=0;
        TailOnColumn(int column)
            {
            this.column=column;
            }
        }
   
    private static class TailSum extends TailOnColumn
        {
        double sum=0.0;
        int count=0;
        TailSum(int column)
            {
            super(column);
            }
       
        @Override
        void touch(String tokens[])
            {
            if(tokens.length<= column) return;
            try {
                double v= Double.parseDouble(tokens[this.column].trim());
                sum+=v;
                count++;
                }
            catch (NumberFormatException e) {
                return;
                }
            }
        @Override
        void echo(PrintStream out) {
            if(count>0)//may append if no number
                {
                out.print(sum);
                }
            }
        }
   
   
    private static class TailMean extends TailSum
        {
        TailMean(int column)
            {
            super(column);
            }
        @Override
        void echo(PrintStream out) {
            if(count>0)//may append if no number
                {
                out.print(sum/count);
                }
            }
        }
   
    private static class TailMin extends TailOnColumn
        {
        Double value=null;
        TailMin(int column)
            {
            super(column);
            }
       
        @Override
        void touch(String tokens[])
            {
            if(tokens.length<= column) return;
            try {
                double v= Double.parseDouble(tokens[this.column].trim());
                value=(value==null?v:Math.min(value, v));
                }
            catch (NumberFormatException e) {
                return;
                }
            }
        @Override
        void echo(PrintStream out) {
            if(value!=null)//may append if no number
                {
                out.print(value);
                }
            }
        }
   
    private static class TailMax extends TailOnColumn
        {
        Double value=null;
        TailMax(int column)
            {
            super(column);
            }
       
        @Override
        void touch(String tokens[])
            {
            if(tokens.length<= column) return;
            try {
                double v= Double.parseDouble(tokens[this.column].trim());
                value=(value==null?v:Math.max(value, v));
                }
            catch (NumberFormatException e) {
                return;
                }
            }
        @Override
        void echo(PrintStream out) {
            if(value!=null)//may append if no number
                {
                out.print(value);
                }
            }
        }
   
    /**
     * A row to be grouped
     *
     */
    private class Tokens implements Comparable<Tokens>
        {
        String line;//a line of input containing the tokens
        String identifier[];//the tokens
        Vector<Tail> tails;//aggregate calculation
        Tokens(String line)
            {
            this.line=line;
            this.identifier=new String[GroupBy.this.columns.size()];
            this.tails= new Vector<Tail>(GroupBy.this.appendAction.size(),1);
            }
       
        @Override
        public int hashCode() {
            return Arrays.hashCode(identifier);
            }
       
        @Override
        public boolean equals(Object obj)
            {
            if(obj==null) return false;
            if(obj==this) return true;
            return this.compareTo(Tokens.class.cast(obj))==0;
            }
       
        @Override
        public int compareTo(Tokens o)
            {
            for(int i=0;i< identifier.length;++i)
                {
                int j= identifier[i].compareTo(o.identifier[i]);
                if(j!=0) return j;
                }
            return 0;
            }
        }
   
    private GroupBy()
        {
       
        }
   
    private void read(BufferedReader in) throws IOException
        {
        String line;

        while((line=in.readLine())!=null)
            {
            String tokens[]=this.delim.split(line);
	        if(this.maxCol>=tokens.length)
	           	{
	        	System.err.println("#GroupBy: Warning index out of range found "+
            			tokens.length
            			+" columns in "+line+" but expected at least "+(this.maxCol+1));
	           	continue;
	           	}
            
            Tokens item= new Tokens(line);
            int n=0;
            for(Integer index: this.columns)
                {
                String s=tokens[index];
                if(!caseSensible) s=s.toLowerCase();
                item.identifier[n]=s;
                n++;
                }
            //if this token was aleardy found, use the previous
            Tokens previous= this.tokens.get(item);
            if(previous!=null)
                {
                item=previous;
                }
            else
                {
                this.tokens.put(item,item);
                
                for(Pair<Action,Object> action:this.appendAction)
                    {
                    Tail t= null;
                    switch(action.first())
                        {
                        case SUM:t= new TailSum(Integer.class.cast(action.second()));break;
                        case MEAN:t= new TailMean(Integer.class.cast(action.second()));break;
                        case MIN:t= new TailMin(Integer.class.cast(action.second()));break;
                        case MAX:t= new TailMax(Integer.class.cast(action.second()));break;
                        case COUNT:t= new TailCount();break;
                        default: Assert.assertUnreachableStatement();break;
                        }
                    item.tails.addElement(t);
                    }
                }
           
            for(Tail t:item.tails)
                {
                t.touch(tokens);
                }
            }
        }
   
    private int parsePosition(String s)
    	{
    	int columnIndex= Integer.parseInt(s.trim())-1;
        if(columnIndex<0) throw new IllegalArgumentException("Bad Column index :"+columnIndex);
        this.maxCol=Math.max(this.maxCol, columnIndex);
        return columnIndex;
    	}
    
    
    public int exec(String[] args)
        {
    
        try
            {
            int optind=0;
            /** loop over args */
            while (optind < args.length)
                {
                if (args[optind].equals("-h"))
                    {
                    System.err.println(" -f fields comma separated 1 indexed \n");
                    System.err.println(" -d  regex pattern for delimiter (default:tab)\n");
                    System.err.println(" -c  append count distinct\n");
                    System.err.println(" -sum <col>  append sum of column\n");
                    System.err.println(" -mean <col>  append mean of column\n");
                    System.err.println(" -min <col>  append *numeric min* of column\n");
                    System.err.println(" -max <col>  append *numeric max* of column\n");
                    System.err.println(" -i  case insensible\n");
                    return 0;
                    }
                else if (args[optind].equals("-c"))
                    {
                    this.appendAction.addElement(new Pair<Action,Object>(Action.COUNT,null));
                    }
                else if (args[optind].equals("-sum"))
                    {
                    this.appendAction.addElement(new Pair<Action,Object>(Action.SUM,parsePosition(args[ ++optind ])));
                    }
                else if (args[optind].equals("-mean"))
                    {
                    this.appendAction.addElement(new Pair<Action,Object>(Action.MEAN,parsePosition(args[ ++optind ])));
                    }
                else if (args[optind].equals("-min"))
                    {
                    this.appendAction.addElement(new Pair<Action,Object>(Action.MIN,parsePosition(args[ ++optind ])));
                    }
                else if (args[optind].equals("-max"))
                    {
                    this.appendAction.addElement(new Pair<Action,Object>(Action.MAX,parsePosition(args[ ++optind ])));
                    }
                else if (args[optind].equals("-i"))
                    {
                    this.caseSensible=false;
                    }
                else if (args[optind].equals("-f"))
                    {
                	if(!this.columns.isEmpty())
                		{
                		throw new IllegalArgumentException("-f defined twice");
                		}
                	
                    String ss[]=args[ ++optind ].split(",");
                    for(String s:ss)
                        {
                        s=s.trim();
                        if(s.length()==0) continue;
                        int columnIndex= parsePosition(s);
                        if(this.columns.contains(columnIndex)) throw new IllegalArgumentException("Index defined twice :"+(columnIndex+1));
            	        this.columns.addElement(columnIndex);
            	        }
                    }
                else if (args[optind].equals("-d"))
                    {
                    this.delim =Pattern.compile(args[ ++optind ]);
                    }
                else if (args[optind].equals("--"))
                    {
                    ++optind;
                    break;
                    }
                else if (args[optind].startsWith("-"))
                    {
                    System.err.println("bad argument " + args[optind]);
                    System.exit(-1);
                    }
                else
                    {
                    break;
                    }
                ++optind;
                }
           
            if(columns.isEmpty())
                {
                System.err.println("No Column defined");
                return -1;
                }
           
           
            if(optind<args.length)
                {
                while(optind< args.length)
                    {
                    BufferedReader in= IOUtils.openReader(args[optind++]);
                    read(in);
                    in.close();
                    }
                }
            else
                {
                read(new BufferedReader(new InputStreamReader(System.in)));
                }
           
            for(Tokens g: this.tokens.keySet())
                {
                System.out.print(g.line);
                for(Tail t:g.tails)
                    {
                    System.out.print("\t");
                    t.echo(System.out);
                    }
                System.out.println();
                }
            System.out.flush();
           
            } catch(Exception err)
            {
            err.printStackTrace(System.err);
            return -1;
            }
        return 0;
        }

   
    public static void main(String[] args)
        {
        GroupBy app= new GroupBy();
        app.exec(args);
        }
}
