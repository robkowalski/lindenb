package org.lindenb.wikipedia.api;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class CategoryTree
	implements Comparable<CategoryTree>
	{
	private Category category;
	private Set<CategoryTree> parents=new TreeSet<CategoryTree>();
	private Set<CategoryTree> children=new TreeSet<CategoryTree>();
	public CategoryTree(Category category)
		{
		this.category=category;
		}
	
	public Category getCategory() {
		return category;
		}
	
	public Set<CategoryTree> getParents()
		{
		return this.parents;
		}
	
	public Set<CategoryTree> getChildren()
		{
		return this.children;
		}
	
	public Set<Category> getAllNodes()
		{
		Set<Category> set= new TreeSet<Category>();
		_getAllNodes(set,this);
		return set;
		}
	
	private static void _getAllNodes(Set<Category> set,CategoryTree cat)
		{
		if(!set.add(cat.getCategory()))
			{
			return;
			}
		
		for(CategoryTree p: cat.getParents())
			{
			_getAllNodes(set,p);
			}
		for(CategoryTree p: cat.getChildren())
			{
			_getAllNodes(set,p);
			}
		}
	
	@Override
	public int hashCode() {
		return getCategory().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if( obj==this) return true;
		if(obj==null || getClass()!=obj.getClass()) return false;
		return getCategory().equals(CategoryTree.class.cast(obj).getCategory());
		}
	
	public void toRDF(PrintStream out,String base)
		{
		_toRDF(out,this,base,new HashSet<Category>());
		}
	
	private static void _toRDF(PrintStream out,CategoryTree node,String base,Set<Category> seen)
		{
		if(!seen.add(node.getCategory())) return;
		out.println("<rdfs:Class rdf:about=\""+base+node.getCategory().getQName()+"\">");
		for(CategoryTree p: node.getParents())
			{
			out.println("  <rdfs:subClassOf rdf:resource=\""+base+p.getCategory().getQName()+"\"/>");
			}
		out.println("</rdfs:Class>");
		out.println();
		
		for(CategoryTree p: node.getParents()) _toRDF(out,p,base,seen);
		for(CategoryTree p: node.getChildren()) _toRDF(out,p,base,seen);
		}

	@Override
	public int compareTo(CategoryTree o) {
		return getCategory().compareTo(o.getCategory());
		}
	
	@Override
	public String toString() {
		return getClass().getName()+"("+getCategory()+")";
		}
	}
