package org.lindenb.sw.nodes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.lindenb.util.iterator.FilterIterator;

/** A Set of Statements */
public class StmtSet
	implements Set<Statement>
	{
	private Set<Statement> stmts= new HashSet<Statement>();
	
	/**
	 * default constructor
	 */
	public StmtSet()
		{
		}
	
	/**
	 * Constructor with a collection
	 * @param stmts
	 */
	public StmtSet(Collection<Statement> stmts)
		{
		getStmts().addAll(stmts);
		}
	
	/**
	 * Constructor with an iterator
	 * @param stmts
	 */
	public StmtSet(Iterator<Statement> iter)
		{
		while(iter.hasNext()) add(iter.next());
		}
	
	/** get all distinct subjects in the model */
	public Set<Resource> getSubjects()
		{
		Set<Resource> subjects= new HashSet<Resource>();
		for(Statement stmt:getStmts())
			{
			subjects.add(stmt.getSubject());
			}
		return subjects;
		}
	
	public Set<Resource> listSubjectsWithProperty(Resource property)
		{
		Set<Resource> subjects= new HashSet<Resource>();
		for(Statement stmt:getStmts())
			{
			if(!stmt.getPredicate().equals(property)) continue;
			subjects.add(stmt.getSubject());
			}
		return subjects;
		}
	
	public Set<Resource> listSubjectsWithProperty(Resource property,RDFNode value)
		{
		Set<Resource> subjects= new HashSet<Resource>();
		for(Statement stmt:getStmts())
			{
			if(!(stmt.getPredicate().equals(property) &&
					 stmt.getValue().equals(value))) continue;
			subjects.add(stmt.getSubject());
			}
		return subjects;
		}
	
	
	/** get all distinct predicates in the model */
	public Set<Resource> getPredicates()
		{
		Set<Resource> subjects= new HashSet<Resource>();
		for(Statement stmt:getStmts())
			{
			subjects.add(stmt.getPredicate());
			}
		return subjects;
		}
	
	
	public Set<RDFNode> getValues()
		{
		Set<RDFNode> objects= new HashSet<RDFNode>();
		for(Statement stmt:getStmts())
			{
			objects.add(stmt.getValue());
			}
		return objects;
		}
	
	public Set<RDFNode> listValuesOfProperty(Resource property)
		{
		Set<RDFNode> objects= new HashSet<RDFNode>();
		for(Statement stmt:getStmts())
			{
			if(!stmt.getPredicate().equals(property)) continue;
			objects.add(stmt.getValue());
			}
		return objects;
		}
	
	public Set<RDFNode> listValuesOfProperty(
			Resource subject,
			Resource property)
		{
		Set<RDFNode> objects= new HashSet<RDFNode>();
		for(Statement stmt:getStmts())
			{
			if(!(stmt.getSubject().equals(subject) &&
				 stmt.getPredicate().equals(property))) continue;
			objects.add(stmt.getValue());
			}
		return objects;
		}
	
	
	public StmtSet getStatementsOfSubject(Resource subject)
		{
		StmtSet set= new StmtSet();
		for(Statement stmt:getStmts())
			{
			if(!(stmt.getSubject().equals(subject) )) continue;
			set.add(stmt);
			}
		return set;
		}
	
	public boolean add(Statement stmt)
		{
		return getStmts().add(stmt);
		}
	
	public boolean remove(Statement stmt)
		{
		return getStmts().remove(stmt);
		}
	
	
	
	protected Set<Statement> getStmts()
		{
		return this.stmts;
		}
	
	public int size()
		{
		return getStmts().size();
		}
	
	@Override
	public Iterator<Statement> iterator() {
		return getStmts().iterator();
		}
	
	@Override
	public Object clone()
		{
		return new StmtSet(getStmts());
		}
	
	public Iterator<Statement> select(Resource r,Resource p,RDFNode o)
		{
		RDFNode array[]=new RDFNode[]{r,p,o};
		return new FilterIterator<Statement>(getStmts().iterator(),array)
			{
			@Override
			public boolean accept(Statement data)
				{
				RDFNode array[]= (RDFNode[])getUserData();
				return data.match(
						Resource.class.cast(array[0]),
						Resource.class.cast(array[1]),
						RDFNode.class.cast(array[2])
						);
				}
			};
		}
	
	public boolean contains(Resource r,Resource p)
		{
		return select(r, p, null).hasNext();
		}
	
	public boolean contains(Resource  r,Resource p,RDFNode o)
		{
		return select(r, p, o).hasNext();
		}

	
	
	public StmtSet filter(Resource r,Resource p,RDFNode o)
		{
		return new StmtSet(select(r, p, o));
		}

	@Override
	public boolean addAll(Collection<? extends Statement> c) {
		return getStmts().addAll(c);
		}

	@Override
	public void clear() {
		getStmts().clear();	
		}

	
	
	@Override
	public boolean contains(Object o) {
		return getStmts().contains(o);
		}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getStmts().containsAll(c);
		}

	@Override
	public boolean isEmpty() {
		return getStmts().isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		return getStmts().remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return getStmts().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return getStmts().retainAll(c);
	}

	@Override
	public Statement[] toArray() {
		return getStmts().toArray(new Statement[size()]);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return getStmts().toArray(a);
		}
	
	
	
	}
