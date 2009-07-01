package org.lindenb.tinytools;

/*
 * Author: Pierre Lindenbaum PhD
 * Contact: plindenbaum (at) yahoo (dot) fr
 *
 * For condition of distribution and use, see the accompanying README file.

 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.IllegalInputException;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;






/**
 * GeneticProg
 *
 */
public class GeneticProg 
	{
	private static final long serialVersionUID = 1L;
	//private static final int EXTRA_COLUMNS=3;
	private static final int SIMPLE_MATH_DEFAULT_SCORE=20;
	private static final int PREDICATE_DEFAULT_SCORE=0;
	private static final int TRIGO_DEFAULT_SCORE=1;
	
	
	private Solution bestSolution=null;
	private int maxNodeInATree=100;
	private double probaLeaf=0.1;
	private double probaMutation=0.5;
	private int numberOfParent=100;
	private double maxNANPercent=0.1;
	private int numberOfExplorer=0;
	private int numberOfGeneration=-1;
	private int total_operator_weight=0;
	private Map<Operator, Integer> operator2weight=new HashMap<Operator, Integer>();
	
	/**
	 * Types of Node in the Genetic Programming Tree
	 *
	 */
	protected static enum NodeType
		{
		COLUMN,
		CONSTANT,
		FUNCTION
		};
	
	/**
	 * 
	 * Spreadsheet
	 * A table holding the input. The last column is the result
	 *
	 */
	private class Spreadsheet
		{
		private double minmax[]=new double[]{Double.MAX_VALUE,-Double.MAX_VALUE};
		private String headers[];
		private List<double[]> rows= new ArrayList<double[]>();
		
		public Spreadsheet()
			{
			
			}
		
		public int getColumnCount()
			{
			return this.headers.length-1;
			}
		
		public int getRowCount()
			{
			return this.rows.size();
			}
		public double[] getRowAt(int index)
			{
			return this.rows.get(index);
			}
		public double getValueAt(int row,int column)
			{
			if(column>= getColumnCount()) throw new IndexOutOfBoundsException("boum "+column);
			return getRowAt(row)[column];
			}
		
		public Double getNormalizedResultAt(int row)
			{
			return (getResultAt(row)-minmax[0])/(minmax[1] -minmax[0]);
			}
		
		void read(BufferedReader in) throws IOException
			{
			String line;
			this.headers=null;
			this.rows.clear();
			
			Pattern TAB=Pattern.compile("[\t]");
			while((line=in.readLine())!=null)
				{
				if(line.trim().length()==0) continue;
				String tokens[]=TAB.split(line);
				
				if(this.headers==null)
					{
					if(tokens.length<2)
						{
						throw new IOException("Expected at least 2 columns in "+line);
						}
					this.headers=tokens;
					continue;
					}
				
				if(tokens.length!=headers.length)
					{
					throw new org.lindenb.lang.IllegalTokenCount(headers.length,tokens);
					}
				double row[]=new double[tokens.length];
				for(int i=0;i< tokens.length;++i)
					{
					if(!Cast.Double.isA(tokens[i]))
						{
						throw new IllegalInputException("Error in "+line+" \""+tokens[i]+"\" is not a number");
						}
					row[i]= Cast.Double.cast(tokens[i]);
					if(i+1==tokens.length)
						{
						this.minmax[0]=Math.min(this.minmax[0],row[i]);
						this.minmax[1]=Math.max(this.minmax[1],row[i]);
						}
					}
				this.rows.add(row);
				}
			if(getRowCount()==0) throw new IOException("Found no data");
			}
	
		public double getResultAt(int row)
			{
			double[] line= getRowAt(row);
			return line[line.length-1];
			}
		
		}
		
	/**
	 * Node in the Genetic Programming Tree
	 * @author lindenb
	 *
	 */
	abstract class Node
		{
		protected Node parent;
		
		public GeneticProg getGeneticProg()
			{
			return GeneticProg.this;
			}
		
		Spreadsheet getSpreadsheet()
			{
			return getGeneticProg().getSpreadsheet();
			}
		
		Random getRandom()
			{
			return getGeneticProg().getRandom();
			}
		
		public Node getRoot()
	      {
		  Node curr= this;
	      while(curr.parent!=null)
	               {
	               curr=curr.parent;
	               }
	      return(curr);
	      }
		


		/**
		 * Mute this node
		 */
		void mute()
	       {
	       //int old_type=my_type;
	       Node choosen=null;
	       List<Node> nodes=null;
	       int tried=0;
	       while( tried<2 && getGeneticProg().rnd() < getGeneticProg().proba_mutation())
	           {
	    	   ++tried;
		   		if(nodes==null)
		   			{
		   			nodes= getAllNodes();
		   			}
	           assert(nodes.size()>0);
	           choosen = nodes.get(getRandom().nextInt(nodes.size() ));
	           //random: mute this node
	           if(getGeneticProg().rnd()>0.5)
		           {
		           choosen.muteIt();
		           }
	           //else remove this node from its parent and replace it by a random node
	           else
	           	  {
	        	  Node parent=choosen.parent;
	        	  if(parent!=null)
	        	  	{
	        		assert(parent.getNodeType()== NodeType.FUNCTION);
	        		((Function)parent).replaceChildren(choosen,getGeneticProg().choose_random_node(null));  
	        	  	}
	           	  }
	           }
	       }

	 /** retrieve all nodes  under this node, including 'this' */
	 public List<Node> getAllNodes() 
		 {
		 List<Node> nodes= new ArrayList<Node>();
		 collect_node(nodes);
		 return nodes;
		 }
	 
	
	private void collect_node(List<Node> nodes) 
	       {
	       nodes.add(this);
	       for(int i=0;i< getChildCount();++i)
	               {
	               getChildAt(i).collect_node(nodes);
	               }
	       }


	public int countDescendant()
		{
		int n=1;
		 for(int i=0;i< getChildCount();++i)
		     {
		     n+= getChildAt(i).countDescendant();
		     }
		 return n;
		}

	public abstract int getChildCount();
	public abstract Node getChildAt(int index);
	/** mute only the content of this node */
	public abstract void muteIt();
	
	public abstract Double calc(int row);
	public abstract void print(PrintWriter out);
	public abstract Node clone(Node parent);
	public abstract NodeType getNodeType();
	public abstract boolean equals(Object o);


	@Override
	public String toString() {
		StringWriter w= new StringWriter();
		this.print(new PrintWriter(w,true));
		w.flush();
		return w.toString();
		}
	}
	
	
	/**
	 * 
	 * Leaf
	 *
	 */
	protected abstract class Leaf extends Node
		{
		protected Leaf()
			{
			}
		
		public int getChildCount()
			{
			return 0;
		 	}
		
		public Node getChildAt(int index)
			{
			return null;
			}
		}
	
	/**
	 * Column
	 *
	 */
	protected class Column
		extends Leaf
		{
		private int columnIndex;
		
		public Column()
			{
			mute();
			}
		
		public NodeType getNodeType()
			{
			return NodeType.COLUMN;
			}
		
		@Override
		public void muteIt()
			{
			//choose another column
			this.columnIndex= getRandom().nextInt(getSpreadsheet().getColumnCount());
			}
	
		public void print(PrintWriter out)
			{
			out.print("($"+(1+this.columnIndex)+")");
			}
	
		public Double calc(int row)
			{
			return getSpreadsheet().getValueAt(row,this.columnIndex);
			}
	
		public Node clone(Node parent)
			{
			Column c= new Column();
			c.parent=parent;
			c.columnIndex=columnIndex;
			return c;
			}
		
		@Override
		public boolean equals(Object o) {
			if(this==o) return true;
			if(!(o instanceof Column)) return false;
			Column cp=(Column)o;
			return cp.columnIndex==this.columnIndex;
			}
			
		}
	
	/** A Constant */
	class Constant extends Leaf
		{
		private double value=Math.random(); 
		
		public Constant()
			{
			muteIt();
			}

		@Override
		public NodeType getNodeType()
			{
			return NodeType.CONSTANT;
			}
		
		@Override
		public void muteIt()
			{
			int log= 1+getRandom().nextInt(6);
			if(getRandom().nextFloat()<0.5f) log=-log;
			int exp=  getGeneticProg().getRandom().nextInt((int)Math.pow(10, log));
			if(getRandom().nextFloat()<0.5f) exp=-exp;
			if(getRandom().nextFloat()<0.5)
				{
				this.value = exp;
				}
			else
				{
				this.value +=exp;
				}
			}
		
		@Override
		public void print(PrintWriter out)
			{
			out.print("("+this.value+")");
			}

		public Node clone(Node parent)
			{
			Constant c= new Constant();
			c.parent=parent;
			c.value=value;
			return c;
			}
		
		@Override
		public boolean equals(Object o) {
			if(this==o) return true;
			if(!(o instanceof Constant)) return false;
			Constant cp=(Constant)o;
			return cp.value==this.value;
			}
		
		@Override
		public Double calc(int row)
			{
			return this.value;
			}
		}
	
	static private class MutableInteger
		{
		private int value=0;
		MutableInteger()
			{
			this(0);
			}
		MutableInteger(int value)
			{
			this.value=value;
			}
		public int getValue() {
			return value;
			}
		public void setValue(int value) {
			this.value = value;
			}
		}
	
	/**
	 * A Function 
	 * @author lindenb
	 *
	 */
	public class Function extends Node
		{
		private Node children[];
		private Operator operator;
		
		/**
		 * @param parent
		 * @param context
		 */
		public Function(MutableInteger count)
			{
			this.operator= getGeneticProg().choose_operator();
			this.children= new Node[operator.argc()];
			for(int i=0;i< this.children.length;++i)
				{
				setChildrenAt(getGeneticProg().choose_random_node(count),i);
				}
			}
		
		/**
		 * Copy constructor
		 * @param cp
		 */
		private Function(Function cp)
			{
			this.operator=cp.operator;
			this.children=new Node[cp.children.length];
			for(int i=0;i< this.children.length;++i)
				{
				setChildrenAt(cp.getChildAt(i).clone(this),i);
				}
			}
		
		@Override
		public NodeType getNodeType()
			{
			return NodeType.FUNCTION;
			}
		

		
		@Override
		public boolean equals(Object o) {
			if(this==o) return true;
			if(!(o instanceof Function)) return false;
			Function cp=(Function)o;
			if(!this.operator.equals(cp.operator)) return false;
			if(cp.getChildCount()!=this.getChildCount()) return false;
			for(int i=0;i< getChildCount();++i)
				{
				if(!getChildAt(i).equals(cp.getChildAt(i))) return false;
				}
			return true;
			}
		
		@Override
		public int getChildCount()
			{
			return this.children.length;
			}

		@Override
		public Node getChildAt(int index)
			{
			return this.children[index];
			}
		
		
		void replaceChildren(Node old,Node recent)
			{
			assert(old.parent==this);
		
			for(int i=0;i< getChildCount();++i)
				{
				if(getChildAt(i)==old)
					{
					setChildrenAt(recent,i);
					return;
					}
				}
			}
		
		/** replace children a given index */
		protected void setChildrenAt(Node node,int index)
			{
			if(this.children[index]!=null)
				{
				this.children[index].parent=null;
				}
			this.children[index]=node;
			node.parent=this;
			}
		
		
		public void muteIt()
			{
			if(getGeneticProg().rnd()<0.5)
				{
				int tried=0;
				while(tried<10)
					{
					++tried;
					Operator newoperator= getGeneticProg().choose_operator();
					if(newoperator==this.operator || newoperator.argc()!=this.operator.argc()) continue;
					this.operator=newoperator;
					return;
					}
				}
			else
				{
				int n=  getGeneticProg().getRandom().nextInt(this.getChildCount());
				int nodefromroot= getRoot().countDescendant();
				int nodefromhere= this.countDescendant();
				MutableInteger shutte= new MutableInteger(0);
				shutte.setValue( nodefromroot-nodefromhere);
				this.setChildrenAt(getGeneticProg().choose_random_node(shutte),n);
				}
			}

		@Override
		public Double calc(int row)
			{
			Double val= this.operator.calc(row,children);
			if(val==null) return null;
			return new Double(val.doubleValue());
			}

		@Override
		public Node clone(Node parent)
			{
			Function c= new Function(this);
			return c;
			}
		
		
		@Override
		public void print(PrintWriter out) {
			out.print(toString());
			}

		@Override
		public String toString() {
			StringBuilder b=new StringBuilder( this.operator.getName()+"( ");
			for(int i=0;i< getChildCount();++i)
				{
				if(i!=0) b.append(" , ");
				b.append(getChildAt(i).toString());
				}
			b.append(" )");
			return b.toString();
			}
		
		}
	
	/**
	 * Operator
	 *
	 */
	abstract class Operator
		{
		@Override
		public int hashCode()
			{
			return getName().hashCode();
			}
		@Override
		public boolean equals(Object o)
			{
			return this==o;
			}
		/** number of arguments */
	    public abstract int argc();
	    /** calculate the result for this operator at the given row */
	    public abstract Double calc(int rowIndex,Node childs[]);
	    /** answer the name of this operator */
	    public abstract String getName();
	    @Override
	    public String toString() {
	    	return getName();
	    	}
		}
	
	/**
	 * Unary operator takes only one argument. E.g: sin/cos/
	 *
	 */
	public abstract class UnaryOperator
		extends Operator
		{
		private String name;
		protected UnaryOperator(String name)
			{
			this.name=name;
			}	
			
		@Override
		public final int argc()
			{
			return 1;
			}
		@Override
		public String getName() {
			return name;
			}

		@Override
		public Double calc(int rowIndex, Node[] childs)
			{
			assert(childs!=null && childs.length==1);
			Number value= childs[0].calc(rowIndex);
			if(value==null || Double.isInfinite(value.doubleValue()) || Double.isNaN(value.doubleValue())) return null;
			return calc(rowIndex,value.doubleValue());
			}

		public abstract Double calc(int rowIndex,double value);
		}
	
	/**
	 * Unary operator takes two arguments. E.g: add/mul/
	 *
	 */
	public abstract class BinaryOperator extends Operator
		{
		private String name;
		protected BinaryOperator(String name)
			{
			this.name=name;
			}	
			
		@Override
		public int argc()
			{
			return 2;
			}
	
		@Override
		public String getName() {
			return name;
			}
	
		@Override
		public Double calc(int rowIndex, Node[] childs)
			{
			assert(childs!=null && childs.length==2);
			Number value1= childs[0].calc(rowIndex);
			if(value1==null ||
					Double.isInfinite(value1.doubleValue()) ||
					Double.isNaN(value1.doubleValue())) return null;
			Number value2= childs[1].calc(rowIndex);
			if(value2==null || 
					Double.isInfinite(value2.doubleValue()) ||
					Double.isNaN(value2.doubleValue())) return null;
			return calc(rowIndex,value1.doubleValue(),value2.doubleValue());
			}
	
		public abstract Double calc(int rowIndex,double v1,double v2);
		}
	
	/**
	 * PredicateOperator operator for comparison. E.g: lt, gt, le, eq,ne... for is (A compare B ? C : D)
	 *
	 */
	public abstract class PredicateOperator extends Operator {
		private String name;
		/**
		 * @param sheet
		 */
		public PredicateOperator(String name)
			{
			this.name=name;
			}


		@Override
		public int argc()
			{
			return 4;
			}


		@Override
		public Double calc(int rowIndex, Node[] childs)
			{
			assert(childs!=null && childs.length==4);
			Number value1= childs[0].calc(rowIndex);
			if(value1==null || Double.isNaN(value1.doubleValue())) return null;
			Number value2= childs[1].calc(rowIndex);
			if(value2==null || Double.isNaN(value2.doubleValue())) return null;

			
			if(compare(value1.doubleValue(),value2.doubleValue()))
				{
				return childs[2].calc(rowIndex);
				}
			else
				{
				return childs[3].calc(rowIndex);
				}
			
			}

		
		abstract boolean  compare(double n1,double n2);

		
		@Override
		public String getName() {
			return this.name;
		}

	}
	
	/**
	 * A  Solution
	 *
	 */
	public class Solution implements Comparable<Solution>,Cloneable
		{
		/** root node */
		private Node node;
		/** score for this node */
		private Double score;
		/** generation index */
		private int generation;
		/** number of rows returning a Nan (fromage) */
		private int countNaN;
		
		public Solution(Node node,int generation)
			{
			this.node=node;
			this.generation=generation;
			this.score=null;
			this.countNaN=0;
			}

		public int getGeneration()
			{
			return this.generation;
			}
		
		@Override
		public Object clone() 
			{
			Solution sol= new Solution(this.getNode().clone(null),getGeneration());
			return sol;
			}
		
		public GeneticProg getGeneticProg()
			{
			return GeneticProg.this;
			}
		
		
		@Override
		public boolean equals(Object obj)
			{
			if(this==obj) return true;
			Solution cp=(Solution)obj;
			return cp.getNode().equals(this.getNode());
			}
		
		public Node getNode()
			{
			return this.node;
			}
		
		public int compareTo(Solution src) 
			{
			int i= getScore().compareTo(src.getScore());
			if(i!=0) return i;
			return this.countNaN-src.countNaN;
			}
		
		public Double getScore() 
			{
			if(this.score==null)
				{
				calc();
				}
			return this.score;
			}
		
		public void mute()
			{
			if(getGeneticProg().rnd()<0.05)
				{
				List<Node> nodes= getNode().getAllNodes();
				Node newroot= nodes.get(getGeneticProg().getRandom().nextInt(nodes.size()));
				newroot.parent=null;
				this.node=newroot;
				}
			else
				{
				getNode().mute();
				}
			this.score=null;
			}
		
		
		public double[]  calc()
			{
			double my_score=0;
	        int number_of_NaN=(0);
	        double y_list[]= new double[getSpreadsheet().getRowCount()];
	        double the_min =  Double.MAX_VALUE;
	        double the_max = -Double.MAX_VALUE;

	        my_score=0;

	        for(int i=0;i< getSpreadsheet().getRowCount();++i)
	            {
	        	Number val = getNode().calc(i);
	            if(val==null ||  Double.isInfinite(val.doubleValue())  ||  Double.isNaN(val.doubleValue()) )
	                    {
	                    y_list[i] = Double.MAX_VALUE;
	                    ++number_of_NaN;
	                    }
	            else
	                    {
	            		y_list[i]=val.doubleValue();
	                    the_min= Math.min(y_list[i],the_min);
	                    the_max= Math.max(y_list[i],the_max);
	                    }
	            }
	        
	        //normalisation of the values
	        if(	the_min!=the_max &&
	        		getSpreadsheet().getRowCount()* getGeneticProg().getMaxNANPercent() > number_of_NaN) // permettre 10% deNaN
	                {
	                int count_good_values=(0);
	                for(int i=0;i<y_list.length;++i)
	                    {
	                    if( y_list[i]==Double.MAX_VALUE) continue;
	                    y_list[i]= (y_list[i]-the_min)/(the_max-the_min);
	                    }
	                for(int i=0;i< getSpreadsheet().getRowCount();++i)
	                    {
	                    if( y_list[i]==Double.MAX_VALUE) continue;
	                    ++count_good_values;
	                    double v=(getSpreadsheet().getNormalizedResultAt(i) - y_list[i]);
	                    my_score+=  (v<0?-v:v);
	                    }
	               
	                my_score/=(double)count_good_values;
	                }
	        else
	                {
	                my_score=Double.MAX_VALUE;
	                number_of_NaN=Integer.MAX_VALUE;
	                }
	        if(Double.isNaN(my_score) || Double.isInfinite(my_score))
	        	{
	        	my_score=Double.MAX_VALUE;
	        	number_of_NaN=Integer.MAX_VALUE;
	        	}
	        this.score= new Double(my_score);
	        this.countNaN= number_of_NaN;
	        return y_list;
			}
		
		@Override
		public String toString()
			{
			return getNode().toString()+ " ("+getGeneration()+") ["+getScore()+"]";
			}
		
		}
	
	private static Logger LOG=Logger.getLogger(GeneticProg.class.getName());
	

	private Spreadsheet spreadsheet=new Spreadsheet();
	private Random random= new Random();
	private Vector<Solution> history;

	
	
	
	public GeneticProg()
		{
		this.spreadsheet= null;
		this.history=new Vector<Solution>();
		
		putOperator(new PredicateOperator("lt")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1<n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));
		
		putOperator(new PredicateOperator("gt")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1>n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));		

		
		putOperator(new PredicateOperator("eq")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1==n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));
		
		putOperator(new PredicateOperator("ne")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1!=n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));		
		
		putOperator(new PredicateOperator("ge")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1>=n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));	
		
		putOperator(new PredicateOperator("le")
				{
				@Override
				boolean compare(double n1, double n2)
					{
					return n1<=n2;
					}
				},new Integer(PREDICATE_DEFAULT_SCORE));		
		
		
		putOperator(new BinaryOperator("Add")
				{
				@Override
				public Double calc(int rowIndex, double v1, double v2) {
					return new Double(v1+v2);
					}
				},new Integer(SIMPLE_MATH_DEFAULT_SCORE));
		
		putOperator(new BinaryOperator("Minus")
				{
				@Override
				public Double calc(int rowIndex, double v1, double v2) {
					return new Double(v1-v2);
					}
				},new Integer(SIMPLE_MATH_DEFAULT_SCORE));		
		
		putOperator(new BinaryOperator("Mul")
				{
				@Override
				public Double calc(int rowIndex, double v1, double v2) {
					return new Double(v1*v2);
					}
				},new Integer(SIMPLE_MATH_DEFAULT_SCORE));				
		
		putOperator(new BinaryOperator("Div")
				{
				@Override
				public Double calc(int rowIndex, double v1, double v2) {
					if(v2==0.0) return null;
					return new Double(v1/v2);
					}
				},new Integer(SIMPLE_MATH_DEFAULT_SCORE));			
		
		putOperator(new UnaryOperator("sqrt")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					if(value<=0) return null;
					return Math.sqrt(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));	
		
		
		putOperator(new UnaryOperator("cos")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					return Math.cos(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));	
		
		putOperator(new UnaryOperator("sin")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					return Math.sin(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));
		
		putOperator(new UnaryOperator("tan")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					return Math.tan(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));
		putOperator(new UnaryOperator("log")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					if(value<=0) return null;
					return Math.log(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));
		putOperator(new UnaryOperator("exp")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					return Math.exp(value);
					}
				},new Integer(TRIGO_DEFAULT_SCORE));
		putOperator(new UnaryOperator("negate")
				{
				@Override
				public Double calc(int rowIndex, double value)
					{
					return -value;
					}
				},new Integer(TRIGO_DEFAULT_SCORE));		
		
		}
		

	

	
	private void putOperator(Operator op,Integer weight)
		{
		//if(weight.intValue()<=0) return;
		Integer w= this.operator2weight.get(op);
		this.operator2weight.put(op,weight);
		this.total_operator_weight+=weight;
		}
	
	private void calcWeight()
		{
		int total=0;
		for(Iterator<Operator> r=this.operator2weight.keySet().iterator();r.hasNext();)
			{
			Operator op=r.next();
			total+= operator2weight.get(op);
			}
		this.total_operator_weight=total;
		}
	



	private int num_extra_parents=5;
	public void run()
		{
		int geneIndex=0;
		List<Solution> solutions= new ArrayList<Solution>();
		for(int generation=geneIndex;generation< geneIndex;++generation)
			{
			while(solutions.size()< this.num_parents())
				{
				solutions.add(new Solution(choose_random_node(null),generation));
				}
			
		    int n=solutions.size();
		    for(int i=0;i< n;++i)
		    	{
		    	  for(int j=0;j< n;++j)
			    	{
		    		
		    		Node n1=  solutions.get(i).getNode().clone(null);
		    		Node n2=  solutions.get(j).getNode().clone(null);
		    		crossover(n1,n2);
		    		Solution s1= new Solution(n1,generation);
		    		Solution s2= new Solution(n1,generation);
		    		s1.mute();
		    		s2.mute();
		    		solutions.add(s1);
		    		solutions.add(s2);
			    	}
		    	}
		    
			while(solutions.size()< num_extra_parents())
				{
				solutions.add(new Solution(choose_random_node(null),generation));
				}
		    
			Collections.sort(solutions);
			
			//remove big ones
			int k=0;
			while(k< solutions.size())
				{
				if(k!=0 && solutions.get(k).getNode().countDescendant()>  max_nodes_in_a_tree())
					{
					solutions.remove(k);
					}
				else
					{
					++k;
					}
				}
			
			//remove duplicates
			k=0;
			while(k+1< solutions.size())
				{
				if(solutions.get(k).equals(solutions.get(k+1)))
					{
					solutions.remove(k);
					}
				else
					{
					++k;
					}
				}
			//TODO fix getGeneticProg().challenge(this.sols.firstElement());
			while(solutions.size()> num_parents())
				{
				solutions.remove(solutions.size()-1);
				}
			//if(generation%5==0 ) sols.setSize(1);
			}
	}


/*
 * TODO
 
private boolean challenge(Solution best) throws DatabaseException
	{
	if(bestSolution==null || best.compareTo(bestSolution)<0)
			{
			
			bestSolution= (Solution)best.clone();
		
			
			GeneticProg.this.history.addElement(bestSolution);
			keep_best_repesentation();
			
			
			for(int i=0;i< GeneticProg.this.history.size();++i)
				{
				//TODO GeneticProg.this.svgPanes[i].setSolution(history.elementAt(i));
				}
			
			double ylist[]= bestSolution.calc();
			for(int i=0;i< ylist.length && i< getTableModel().getRowCount();++i)
				{
				if(ylist[i]!=Double.MAX_VALUE)
					{
					getTableModel().setValueAt(new Double(ylist[i]),i, getColumnCount()+2);
					getTableModel().setValueAt(new Double(Math.abs(ylist[i]-getNormalizedResultAt(i))),i, getColumnCount()+3);
					}
				else
					{
					getTableModel().setValueAt(null,i, getColumnCount()+2);
					getTableModel().setValueAt(null,i, getColumnCount()+3);
					}
				}
			getTableModel().fireTableDataChanged();
			GeneticProg.this.functionArea.setText(bestSolution.getNode().toString());
			GeneticProg.this.functionArea.setCaretPosition(0);
			return true;
			}
	return false;
	}*/

private void keep_best_repesentation() 
	        {
	        double smallest_delat_score=(Double.MAX_VALUE);

	        //y_list_list.sort(); non deja fait

	        if( history.size()<10) return;
	        Collections.sort(this.history);
	        int r=(1)/* pas le premier !*/, r_end=(this.history.size()),r2;
	        int the_bad=(history.size());

	        while(r!=r_end)
	                {
	                r2=r+1;
	                if(r2!=r_end)
	                        {
	                        double d =  history.elementAt(r).getScore()-history.elementAt(r2).getScore();
	                        if( d<=smallest_delat_score)
	                                {
	                                the_bad = r;//r et pas r2, cela permet de garder le meilleur de la liste
	                                smallest_delat_score = d;
	                                }
	                        }
	                ++r;
	                };
	        if( the_bad != r_end)
	                {
	        		history.removeElementAt( the_bad );
	                }
	        }

	
	
	
	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
		}
	
	

	
	
	
	

	
	public Random getRandom()
		{
		return this.random;
		}
	
	public double rnd()
		{
		return getRandom().nextDouble();
		}
	
	
	public int max_nodes_in_a_tree()
		{
		return this.maxNodeInATree; 
		}
	
	
	public double proba_create_leaf()
		{
		return this.probaLeaf; 
		}	
	
	
	
	public double proba_mutation()
		{
		return this.probaMutation; 
		}
	
	
	public int num_parents()
		{
		return this.numberOfParent ;
		}
	
	
	public double getMaxNANPercent()
		{
		return this.maxNANPercent; 
		}
	
	
	public int getNumberOfExplorer()
		{
		return this.numberOfExplorer; 
		}
	
	public int getGenerationPerExplorer()
		{
		return this.numberOfGeneration; 
		}
	
	public int num_extra_parents()
		{
		return 10;
		}
	
	private int getOperatorWeight(Operator op)
		{
		return this.operator2weight.get(op);
		}
	
	Operator choose_operator()
		{
		int n= getRandom().nextInt(this.total_operator_weight);
		for (Iterator<Operator> iter = this.operator2weight.keySet().iterator(); iter.hasNext();) {
			Operator op =  iter.next();
			int weight=getOperatorWeight(op);
			if(n<weight)
				{
				return op;
				}
			n-=weight;
			}
		throw new IllegalStateException("boum");
		}
	
	

 Node choose_random_node(MutableInteger count)
        {
		if(count==null)
			{
			count=new MutableInteger(0);
			}
		Node nn=null;

		count.setValue(count.getValue()+1);
		
        if( count.getValue()+1>= max_nodes_in_a_tree() ||
               rnd() <  proba_create_leaf() )
                {
                nn = make_leaf();
                }
        else
                {
                nn = new Function(count);
                }
        assert(nn!=null);
        return nn;
        }

/****************************************
 *
 * make_leaf
 *
 */
private Node make_leaf()
    {
	Node nn=null;
    if( rnd() < 0.5 )
            {
            nn = new Constant();
            }
    else
            {
            nn = new Column();
            }
    assert(nn!=null);
    return nn;
    }

 private void crossover(Node node1, Node node2)
    {
	Node father[]=new Node[]{null,null};
	Node node_to_swap[]=new Node[]{null,null};
    List<Node> nodes[]=new ArrayList[]
                  {
					   new ArrayList<Node>(),
					   new ArrayList<Node>()
                  };
    //get all the nodes
    node1.collect_node(nodes[0]);
    node2.collect_node(nodes[1]);


    //get the father of those list
    //find each curr in the father child list

    for(int i=0;i<2;++i)
            {
            //get a random node_t in the list
            node_to_swap[i] = nodes[i].get( getRandom().nextInt(nodes[i].size()));
;

            father[i] = node_to_swap[i].parent;
            //test it is not the root
            if( father[i]==null) return;
            //test it is a node with children
            if(father[i].getChildCount()==0) return;

            assert( father[i]!=null);
            }

    /* swap link child->father */
    node_to_swap[0].parent = father[1];
    node_to_swap[1].parent = father[0];

    /*
    swap link father->child
    ATTENTION a ce stade les father sont de type func_t (!=leaf)
    */

    Function fun_father[]=new Function[]
         {
         (Function)father[0],
         (Function)father[1]
         };

    for(int i=0;i<2;++i)
            {
            for(int j=0; j< fun_father[i].getChildCount();++j)
                    {
                    if( fun_father[i].getChildAt(j).parent != fun_father[i])
                            {
                            assert( fun_father[i].getChildAt(j).parent == fun_father[(i==0?1:0)] );
                            assert( node_to_swap[ (i==0?1:0) ].parent ==fun_father[i] );
                            fun_father[i].setChildrenAt(node_to_swap[ (i==0?1:0) ],j);
                            break;
                            }
                    }
            }
    }




public static void main(String[] args) {
	try {
		GeneticProg app= new GeneticProg();
		int optind=0;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println(Compilation.getLabel());
				System.err.println("-h this screen");
				return;
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
	      if(optind==args.length)
	    	{
	    	app.getSpreadsheet().read(new BufferedReader(new InputStreamReader(System.in)));
	    	}
	      else if(optind+1!=args.length)
	    	{
	    	System.err.println("Illegal number of arguments");
			System.exit(-1);
	    	}
		   else
		    	{
	    		BufferedReader in=null;
	    		try {
					in= IOUtils.openReader(args[optind++]);
					app.getSpreadsheet().read(in);
					}
	    		catch (java.io.IOException e) {
					throw e;
					}
				finally
					{
					if(in!=null) in.close();
					in=null;
					}
		    	}
	    app.run();
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}