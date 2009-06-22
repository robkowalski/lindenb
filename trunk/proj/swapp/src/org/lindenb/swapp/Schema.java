package org.lindenb.swapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.nodes.Literal;
import org.lindenb.sw.nodes.Resource;
import org.lindenb.sw.nodes.Statement;
import org.lindenb.sw.nodes.StmtSet;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.RDFS;

/**
 * Schema
 *
 */
public  class Schema
	{
	/**
	 * OntNode
	 *
	 */
	public abstract class OntNode
		{
		private Literal label;
		private Literal comment;
		private String uri;
		protected OntNode(Resource resource)
			{
			this.uri= resource.getURI();
			this.label= new Literal(this.uri);
			this.comment= this.label;
			Resource rdfs_label= new Resource(RDFS.NS,"label");
			Resource rdfs_comment= new Resource(RDFS.NS,"comment");
			for(Statement stmt:Schema.this.statements.filter(resource, null, null))
				{
				if(stmt.isLiteral())
					{
					if(stmt.getPredicate().equals(rdfs_label))
						{
						this.label=stmt.getValue().asLiteral();
						}
					else if(stmt.getPredicate().equals(rdfs_comment))
						{
						this.comment=stmt.getValue().asLiteral();
						}
					}
				}
			}
		
		protected StmtSet getStmtSet()
			{
			return Schema.this.statements;
			}
		
		public Literal getLabel()
			{
			return this.label;
			}
		
		public Literal getComment()
			{
			return this.comment;
			}
		
		public String getURI() {
			return uri;
			}
		
		public String getQName()
			{
			return Schema.this.prefixMapping.shortForm(getURI());
			}
		
		@Override
		public int hashCode() {
			return getURI().hashCode();
			}
		
		@Override
		public boolean equals(Object obj) {
			if(this==obj) return true;
			if(obj==null || obj.getClass()!=this.getClass()) return false;
			return getURI().equals(OntNode.class.cast(obj).getURI());
			}
		
		@Override
		public String toString() {
			return getQName();
			}
		}
	
	
	
	public class Restriction
		{
		private OntProperty ontProperty;
		private Integer minCardinality=null;
		private Integer maxCardinality=null;
		
		public OntProperty getOntProperty()
			{
			return ontProperty;
			}
		
		@Override
		public int hashCode() {
			return getOntProperty().hashCode();
			}
		@Override
		public boolean equals(Object obj) {
			if(this==obj) return true;
			if(obj==null || obj.getClass()!=this.getClass()) return false;
			return getOntProperty().equals(Restriction.class.cast(obj).getOntProperty());
			}
		}
	
	public class OntClass extends OntNode
		{
		private Set<Restriction> restrictions=new HashSet<Restriction>();
		private Set<OntClass> parent= new HashSet<OntClass>();
		OntClass(Resource resource)
			{
			super(resource);
			}
		
		public Set<OntClass> getDeclaredParents()
			{
			return this.parent;
			}
		
		public Set<Restriction> getDeclaredRestrictions()
			{
			return this.restrictions;
			}
		
		public Set<OntClass> getParents()
			{
			Set<OntClass> set= new HashSet<OntClass>(getDeclaredParents());
			for(OntClass p: getDeclaredParents())
				{
				set.addAll(p.getParents());
				}
			return set;
			}
		
		public Set<OntClass> getChildren()
			{
			Set<OntClass> set= new HashSet<OntClass>(getDeclaredParents());
			for(OntClass c: Schema.this.ontClasses)
				{
				if(!c.parent.contains(this)) continue;
				set.add(c);
				set.addAll(c.getChildren());
				}
			return set;
			}
		
		
		public Set<Restriction> getRestrictions()
			{
			Set<Restriction> set= new HashSet<Restriction>(getDeclaredRestrictions());
			for(OntClass p: getParents())
				{
				set.addAll(p.getDeclaredRestrictions());
				}
			return set;
			}
		}
	
	public abstract  class OntProperty extends OntNode
		{
		OntProperty(Resource r)
			{
			super(r);
			}
		}
	
	public  class DataTypeProperty extends OntProperty
		{
		DataTypeProperty(Resource r)
			{
			super(r);
			}
		}
	
	
	public  class ObjectProperty extends OntProperty
		{
		private OntClass range;
		ObjectProperty(Resource r)
			{
			super(r);
			}
		}
	
	private StmtSet statements= new StmtSet();
	private PrefixMapping prefixMapping= new PrefixMapping();
	private Set<OntClass> ontClasses =new HashSet<OntClass>();
	
	public Schema(StmtSet statements)
		{
		Set<OntProperty> properties= new HashSet<OntProperty>();
		final Resource rdf_type=new Resource(RDF.NS,"type");
		final Resource rdfs_range=new Resource(RDFS.NS,"range");
		
		for(Statement stmt: statements.filter(null, rdf_type, new Resource(getNS(),"Class")))
			{
			OntClass clazz= new OntClass(stmt.getSubject());
			this.ontClasses.add(clazz);
			}
		
		//get Properties
		for(Statement stmt: statements.filter(null, rdf_type, new Resource(getNS(),"DataTypeProperty")))
			{
			DataTypeProperty prop= new DataTypeProperty(stmt.getSubject());
			properties.add(prop);
			}
		for(Statement stmt: statements.filter(null, rdf_type, new Resource(getNS(),"ObjectProperty")))
			{
			ObjectProperty prop= new ObjectProperty(stmt.getSubject());
			properties.add(prop);
			
			for(OntClass c: this.ontClasses)
				{
				statements.filter(stmt.getSubject(), rdfs_range,null);
				}
			}
		
		
		
		}
	public String shortForm(Resource r)
		{
		return shortForm(r.getURI());
		}
	
	public String shortForm(String uri)
		{
		return this.prefixMapping.shortForm(uri);
		}
	
	public String getNS()
		{
		return "";
		}
	}
