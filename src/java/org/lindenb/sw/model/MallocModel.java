/**
 * 
 */
package org.lindenb.sw.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.lindenb.sw.RDFException;
import org.lindenb.util.Algorithms;
import org.lindenb.util.Pair;




/**
 * naive implementation of AbstractRDFModel
 *
 */
public class MallocModel extends AbstractRDFModel
	{
	private static final Algorithms<Statement,Resource> ALGO_FOR_INSTANCE= new Algorithms<Statement, Resource>(new Comparator<Resource>()
	                {
	                public int compare(Resource o1, Resource o2) {
	                        return o1.compareTo(o2);
	                        }
	                })
	        {
	        @Override
	        public Resource getKey(Statement n)
	                {
	                return n.getSubject();
	                }
	        };
    private static final Algorithms<Statement,Resource> ALGO_FOR_PROPERTY= new Algorithms<Statement, Resource>(new Comparator<Resource>()
	                {
	                public int compare(Resource o1, Resource o2) {
	                        return o1.compareTo(o2);
	                        }
	                })
	        {
	        @Override
	        public Resource getKey(Statement n)
	                {
	                return n.getPredicate();
	                }
	        };
	      
	  private static final Algorithms<Statement,Short> ALGO_FOR_VALUE_TYPE= new Algorithms<Statement, Short>(new Comparator<Short>()
	                {
	                public int compare(Short o1, Short o2) {
	                        return o1.compareTo(o2);
	                        }
	                })
		        {
		        @Override
		        public Short getKey(Statement n)
		                {
		                return n.getValue().getNodeType();
		                }
		        };
	
       private static final Algorithms<Statement,String> ALGO_FOR_VALUE= new Algorithms<Statement, String>(new Comparator<String>()
		                {
		                public int compare(String o1, String o2) {
		                       	return o1.compareTo(o2);
		                        }
		                })
			        {
			        @Override
			        public String getKey(Statement n)
			                {
			                return n.getValue().isLiteral()?
			                		n.getValue().asLiteral().getString().toLowerCase():
			                		n.getValue().asResource().getURI()
			                		;
			                }
			        };
			        
		        
		        
	
	private Vector<Statement> statements= new Vector<Statement>(1000,100);
	
	public MallocModel()
		{
		}
	
	protected Vector<Statement> getDataVector()
		{
		return this.statements;
		}
	
	/** @return equal range of index for this instance */
	private Pair<Integer,Integer> equal_range(Resource r)
	        {
	        return ALGO_FOR_INSTANCE.equal_range(getDataVector(), r);
	        }

	/** @return equal range of index for this instance/property */
	private Pair<Integer,Integer> equal_range(Resource r,Resource p)
	        {
	        return  ALGO_FOR_PROPERTY.equal_range(getDataVector(),
	                        equal_range(r),p);
	        }

	/** @return equal range of index for this instance/property/valueType */
	private Pair<Integer,Integer> equal_range(Resource r,Resource p,Short valueType)
	        {
	        return  ALGO_FOR_VALUE_TYPE.equal_range(getDataVector(),
	                        equal_range(r,p),valueType);
	        }
	
	/** @return equal range of index for this instance/property/valueType */
	private Pair<Integer,Integer> equal_range(Resource r,Resource p,Short valueType,String value)
	        {
			if(valueType!=null && value!=null && valueType==VALUE_IS_LITERAL)
				{
				value=value.toLowerCase();
				}
	        return  ALGO_FOR_VALUE.equal_range(getDataVector(),
	                        equal_range(r,p,valueType),value);
	        }
	
	/** @return i-th statement in data vector */
	private Statement getStatementAt(int i)
	        {
	        return getDataVector().elementAt(i);
	        }

	
	/**
	 * search index of stmt in vector using equal_range algorithm
	 * @param stmt the searched statement
	 * @return tindex of stmt in vector
	 */
	private  int indexOf(Statement stmt)
	        {
	        Pair<Integer,Integer> p= equal_range(
	                        stmt.getSubject(),
	                        stmt.getPredicate(),
	                        stmt.getValue().getNodeType(),
	                        stmt.getValue().isLiteral()?
	                        	stmt.getValue().asLiteral().getString().toLowerCase():
	                        	stmt.getValue().asResource().getURI()   
	        
	        				);
	        for(int i=p.first();i< p.second();++i)
	                {
	                if(getStatementAt(i).equals(stmt)) return i;
	                }
	        return -1;
	        }

	
	/** @return wether this contains the given statement */
	@Override
	public boolean contains(Statement statement)  throws RDFException
	        {
	        return indexOf(statement)!=-1;
	        }

	/** @return wether this controler contains the given subject */
	@Override
	public boolean containsSubject(Resource subject) throws RDFException
	        {
	        Pair<Integer,Integer> p= equal_range(subject);
	        return !(p.first().equals(p.second()));
	        }
	
	@Override
	public int hashCode() {
		return getDataVector().hashCode();
		}

	
	@SuppressWarnings("unused")
	private int removeStatements(Pair<Integer,Integer> bounds)
	    {
	    int len= bounds.second()-bounds.first();
	    for(int i=0;i< len;++i)
	            {
	            getDataVector().removeElementAt(bounds.first());
	            }
	    return len;
	    }

	/**
	 * remove a statement if it  already exists
	 * @param stmt the statement to remove
	 * @return true if the statement was removed
	 */
	@Override
	public boolean remove(Statement stmt) throws RDFException
	        {
	        int i= indexOf(stmt);
	        if(i==-1) return false;
	        getDataVector().removeElementAt(i);
	        return true;
	        }

	
	
	/* (non-Javadoc)
	 * @see org.lindenb.sw.model.AbstractRDFModel#_removeStatements(org.lindenb.sw.model.AbstractRDFModel.Resource, org.lindenb.sw.model.AbstractRDFModel.Resource, java.lang.Short, java.lang.String)
	 */
	@Override
	protected int _removeStatements(Resource subject, Resource predicate,
			Short valueType, String value) throws RDFException {
		int n=0;
		for(Iterator<Statement> iter= getDataVector().iterator();
				iter.hasNext();)
			{
			Statement x=iter.next();
			if(subject!=null && !subject.equals(x.getSubject())) continue;
			if(predicate!=null && !predicate.equals(x.getPredicate())) continue;
			if(valueType!=null && !valueType.equals(x.getValue().getNodeType())) continue;
			if(value!=null && !value.equals(x.getValue())) continue;
			iter.remove();
			}
		return n;
		}


	

	/**
	 * add a new statement if it does not already exists
	 * @param stmt the statement to add
	 * @return true if the statement was added
	 */
	@Override
	public boolean addStatement(Statement stmt) throws RDFException
	        {
	        Pair<Integer,Integer> bounds= equal_range(
	                        stmt.getSubject(),
	                        stmt.getPredicate()
	                        );
	        for(int i=bounds.first();i< bounds.second();++i)
	                {
	                if(getStatementAt(i).equals(stmt))
	                        {
	                        return false;
	                        }
	                }
	        getDataVector().insertElementAt(stmt, bounds.first());
	        return true;
	        }

	
	/* (non-Javadoc)
	 * @see org.lindenb.sw.model.AbstractRDFModel#clear()
	 */
	@Override
	public int clear() throws RDFException {
		int n= size();
		getDataVector().clear();
		return n;
	}

	/**
	 * find some statments matching some paramaters.
	 *  null parameters may be used as a wildcard
	 *
	 * @param instanceId the instance to search or null
	 * @param property the property to search or null
	 * @param value the value to search or null
	 * @return an iterator over the matching statements
	 */
	@Override
	public CloseableIterator<Statement> listStatements(
	                Resource subject,
	                Resource predicate,
	                Short valueType,
	                String value
	                ) throws RDFException
	        {
	        Pair<Integer,Integer> bounds=new Pair<Integer,Integer>(0,size());
	        if(subject!=null)
	                {
	                if(predicate==null)
	                        {
	                        bounds=equal_range(subject);
	                        }
	                else
	                        {
	                		if(valueType==null)
	                			{
	                			bounds=equal_range(subject,predicate);
	                			}
	                		else
	                			{
	                			if(value==null)
	                				{
	                				bounds=equal_range(subject,predicate,valueType);
	                				}
	                			else
	                				{
	                				bounds=equal_range(subject,predicate,valueType,value);
	                				}
	                			 
	                			}
	                        }
	                }
	        
	    	Vector<Statement> stmt= new Vector<Statement>(bounds.second()-bounds.first());
			for(int index=bounds.first();index< bounds.second();++index)
				{
				Statement x= getStatementAt(index);
				if(subject!=null && !subject.equals(x.getSubject())) continue;
				if(predicate!=null && !predicate.equals(x.getPredicate())) continue;
				if(valueType!=null && !valueType.equals(x.getValue().getNodeType())) continue;
				if(value!=null && !value.equals(x.getValue())) continue;
				stmt.addElement(x);
				}
			
			return new CloseableIteratorAdapter<Statement>(stmt.iterator())
				{
				@Override
				public void remove() {
					try {MallocModel.this.remove(this.last);}
					 catch( RDFException err ) { throw new RuntimeException(err);}
					}
				};
	        

	        }

	

	@Override
	public boolean isEmpty() throws RDFException {
		return getDataVector().isEmpty();
		}
	
	@Override
	public int size() throws RDFException {
		return getDataVector().size();
	}
	
	@Override
	public String toString() {
		return getDataVector().toString();
		}
	

}
