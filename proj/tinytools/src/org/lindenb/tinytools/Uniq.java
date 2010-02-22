package org.lindenb.tinytools;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.*;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Compilation;


public class Uniq
{
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



private static class ScalarList
implements Comparable<ScalarList>
    {
    private String tokens[];
   
    public ScalarList( String tokens[])
        {
        this.tokens=tokens;
        }
   

   
    public int compareTo(ScalarList o)
        {
        if(o==this) return 0;
        if(this.getSize()!=o.getSize()) throw new RuntimeException();
       
        for(int j=0;j< getSize();++j)
            {
            int i= at(j).compareTo(o.at(j));
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
        return Arrays.hashCode(this.tokens);
        }
   
   
    public String at(int i)
        {
        return this.tokens[i];
        }
   
    public int getSize()
        {
        return this.tokens.length;
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



private ColumnModel columns= new ColumnModel();
private Pattern delim=Pattern.compile(",");
private boolean casesensible=true;
private boolean trimTokens=true;
private TreeMap<ScalarList, List<String>> scalar2lines= new TreeMap<ScalarList, List<String>>();

public void read(BufferedReader in) throws IOException
    {
    String line;
    while((line=in.readLine())!=null)
        {
        if(line.startsWith("#") || line.trim().length()==0) continue;
        String tokens[]=this.delim.split(line);
        if(this.columns.maxCol>=tokens.length)
            {
            System.err.println("#ignoring line :"+line+" because expected "+ this.columns.size()+" columns but found "+tokens.length);
            continue;
            }
        String array[]=new String[this.columns.size()];
        for(int i=0;i< this.columns.size();++i)
            {
            array[i]=tokens[this.columns.at(i)];
            if(trimTokens) array[i]=array[i].trim();
            if(!casesensible) array[i]=array[i].toLowerCase();
            }
        ScalarList l= new ScalarList(array);
        List<String> lines= this.scalar2lines.get(l);
        if(lines==null)
            {
            lines= new ArrayList<String>();
            this.scalar2lines.put(l,lines);
            }
        lines.add(line);
        }
    }



public static void main(String args[])
    {
    try
        {
        Uniq main= new Uniq();
        int optind=0;
        boolean count_option=false;
        boolean uniq_option=false;
        boolean duplicated_option=false;
        boolean print_all_option=false;
        while (optind < args.length)
                {
                if (args[optind].equals("-h"))
                    {
                	System.out.println(Compilation.getLabel());
                    System.out.println("Uniq [options] (<Files>|stdin)");
                    System.out.println("\t-i case insensible");
                    System.out.println("\t-t delimiter pattern (default:TAB)");
                    System.out.println("\t-f column1,column2,....");
                    System.out.println("\t-c count prefix lines by the number of occurrences");
                    System.out.println("\t-u print only uniq lines");
                    System.out.println("\t-d  only print duplicate lines");
                    System.out.println("\t-D  only all duplicate lines");
                    return;
                    }
                else if (args[optind].equals("-i"))
                    {
                    main.casesensible=false;
                    }
                else if (args[optind].equals("-c"))
                    {
                    count_option=true;
                    }
                else if (args[optind].equals("-d"))
                      {
                    duplicated_option=true;
                      }
                else if (args[optind].equals("-D"))
                      {
                    duplicated_option=true;
                    print_all_option=true;
                      }
                else if (args[optind].equals("-u"))
                    {
                    uniq_option=true;
                    }
                else if (args[optind].equals("-t"))
                    {   
                    main.delim=Pattern.compile(args[++optind]);
                    }
                else if (args[optind].equals("-f"))
                    {   
                    String ss[]=args[++optind].split("[,]");
                    for(String s:ss)
                        {
                        if(s.trim().length()==0) continue;
                        int i=Integer.parseInt(s.trim());
                        if(i<1) throw new IllegalArgumentException("Index should be greater than 0 :"+s);
                        main.columns.add(i-1);
                        }
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
       
        if(main.columns.isEmpty())
            {
            throw new IllegalArgumentException("Column indexes not defined");
            }
       
        if(uniq_option && duplicated_option)
            {
            throw new IllegalArgumentException("duplicate and unique flags both selected");
            }
       
        if(optind==args.length)
            {
            main.read(new BufferedReader(new InputStreamReader(System.in)));   
            }
        else
            {
            while(optind< args.length)
                {
                BufferedReader in= IOUtils.openReader(args[optind++]);
                main.read(in);   
                in.close();
                }
            }
       
        //echo
        for(ScalarList scalar:main.scalar2lines.keySet())
            {
            List<String> lines= main.scalar2lines.get(scalar);
            int count=lines.size();
            if(count!=1 && uniq_option) continue;
            if(count==1 && duplicated_option) continue;

            if(print_all_option)
                {
                for(String line:lines)
                    {
                    if(count_option)
                        {
                        System.out.print(count);
                        System.out.print("\t");
                        }
                    System.out.print(line);
                    System.out.println();
                    }
               
                }
            else
                {
                if(count_option)
                    {
                    System.out.print(count);
                    System.out.print("\t");
                    }
                System.out.print(lines.get(0));
                System.out.println();
                }
           
            }
       
       
        }
    catch(Exception err)
        {
        err.printStackTrace();
        }
   
}



}
