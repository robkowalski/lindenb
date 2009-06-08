package org.lindenb.mwrdf;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.RDFException;
import org.lindenb.sw.nodes.Literal;
import org.lindenb.sw.nodes.RDFNode;
import org.lindenb.sw.nodes.Resource;
import org.lindenb.sw.nodes.Statement;
import org.lindenb.sw.nodes.StmtSet;
import org.lindenb.sw.vocabulary.OWL;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.RDFS;
import org.lindenb.util.Cast;
import org.lindenb.xsd.XSD;


public class Schema
	{
	public static final String NS="http://mwrdf.lindenb.org/";
	private static final Resource RDF_TYPE= new Resource(RDF.NS,"type");
	
	private abstract class OntNode
		extends Resource
		{
		private static final long serialVersionUID = 1L;

		OntNode(Resource rsrc)
			{
			super(rsrc);
			}
		
		String getQName()
			{
			return getPrefixMapping().shortForm(getURI());
			}
		
		protected String getString(Resource property)
			{
			return getString(property,null);
			}
		
		protected String getString(Resource property,String defaultValue)
			{
			Literal L= getOntology().getLiteral(this,property);
			return L==null?defaultValue:L.getLexicalForm();
			}
		
		public Boolean getBoolean(Resource property)
			{
			return Cast.Boolean.cast(getString(property));
			}
		
		public boolean getBoolean(Resource property,boolean defaultValue)
			{
			Boolean b= getBoolean(property);
			return (b==null?defaultValue:b);
			}
		
		public String getLabel()
			{
			return getString(new Resource(RDFS.NS,"label"),getURI());
			}
		
		public String getComment()
			{
			return getString(new Resource(RDFS.NS,"comment"),getLabel());
			}
		public abstract String createDefault();
		}
	
	/** Something like a OWLClass :-) */
	class OntClass extends OntNode
		{
		private static final long serialVersionUID = 1L;
		private Set<OntClass> parentClasses=null;
		private Set<OntClass> childClasses=null;
		private Set<OntProperty> properties=null;
		
		OntClass(Resource e)
			{
			super(e);
			}
		
		boolean isAbstract()
			{
			return  getOntology().contains(
					this,RDF_TYPE,
					new Resource("AbstractClass")
					);
			}	
		
		/** returns all the categories for the given class */
		public Set<String> getCategories()
			{
			
			Set<String> set= new TreeSet<String>();
			for(Statement stmt:  getOntology().filter(
					new Resource(this),new Resource(NS,"category"),null))
				{
				
				if(!stmt.isLiteral()) continue;
				set.add(stmt.getValue().asLiteral().getLexicalForm());
				}
			for(OntClass parent:getSuperClasses())
				{
				set.addAll(parent.getCategories());
				}
			
			return set;
			}
		
		
		
		Set<OntClass> getSuperClasses()
			{
			if(parentClasses!=null) return parentClasses;
			this.parentClasses= new HashSet<OntClass>();
			for(Statement stmt:  getOntology().filter(
					this,new Resource(RDFS.NS,"subClassOf"),null))
				{
				if(!stmt.isResource()) continue;
				Resource parent= stmt.getValue().asResource();
				OntClass clazz= Schema.this.uri2class.get(parent.getURI());
				if(clazz==null) continue;
				this.parentClasses.add(clazz);
				}
			return this.parentClasses;
			}
		
		Set<OntClass> getChildClasses()
			{
			if(this.childClasses!=null) return childClasses;
			this.childClasses = new HashSet<OntClass>();
			for(Statement stmt:  getOntology().filter(
					null,new Resource(RDFS.NS,"subClassOf"),this))
				{
				OntClass clazz= Schema.this.uri2class.get(stmt.getSubject().getURI());
				if(clazz==null) continue;
				this.childClasses.add(clazz);
				}
			return this.childClasses;
			}
		
		Set<OntProperty> getOntProperties()
			{
			if(this.properties!=null) return this.properties;
			this.properties= new HashSet<OntProperty>();
			
			for(Statement stmt:  getOntology().filter(
					null,new Resource(RDFS.NS,"domain"),this))
				{
				OntProperty prop= Schema.this.uri2property.get(stmt.getSubject().getURI());
				if(prop==null) continue;
				this.properties.add(prop);
				}
			for(OntClass parent: getSuperClasses())
				{
				this.properties.addAll(parent.getOntProperties());
				}
			return this.properties;
			}
		
		
		/** create a default */
		@Override
		public String createDefault()
			{
			StringWriter sw= new StringWriter();
			PrintWriter w= new PrintWriter(sw);
			w.print("<"+getQName());
			//if(!isAnonymous()) w.print(" rdf:about=\" fix-me \"");
			w.println(">");
			for(OntProperty p:this.getOntProperties())
				{
				w.print(p.createDefault());
				}
			w.print("<"+getQName()+"/>");
			w.flush();
			return sw.toString();
			}
		
		public void validate(Resource subject) throws RDFException
			{
			//if it is abstract, check childClass has been implemented
			Schema.this.validateAbstraction(subject,this);
			
			//validate properties
			for(OntProperty prop: getOntProperties())
				{
				StmtSet stmts= Schema.this.getCurrentRDFStore().filter(subject, prop.asResource(), null);
				
				Schema.this.processed.addAll(stmts);
				Integer max= prop.getMaxCardinality();
				Integer min= prop.getMinCardinality();
				
				
				//System.err.println("n="+stmts.size()+" min="+min+" max="+max+" "+prop.getQName());
				if(prop.isResource() || ((prop instanceof OntDataTypeProperty) && !OntDataTypeProperty.class.cast(prop).isLocalized()))
					{
					if(min!=null && stmts.size() < min)
						{
						throw new RDFException(subject.getURI()+" "+prop.getQName()+" has min cardinality:"+min);
						}
					if(max!=null && stmts.size() > max)
						{
						throw new RDFException(subject.getURI()+" "+prop.getQName()+" has max cardinality:"+min);
						}
					for(Statement stmt:stmts)
						{
						Schema.this.processed.add(stmt);
						prop.validate(stmt.getValue());
						}
					}
				else // group by language
					{
					Map<String, Integer> lang2stmt = new HashMap<String, Integer>();
					for(Statement stmt:stmts)
						{
						prop.validate(stmt.getValue());
						Schema.this.processed.add(stmt);
						String lang=stmt.getValue().asLiteral().getLanguage();
						Integer n=lang2stmt.get(lang);
						if(n==null) n=0;
						lang2stmt.put(lang,1+n);
						}
					for(Integer n: lang2stmt.values())
						{
						if(min!=null && n < min)
							{
							throw new RDFException(prop.getURI()+" has min cardinality:"+min);
							}
						if(max!=null && n > max)
							{
							throw new RDFException(prop.getURI()+" has max cardinality:"+min);
							}
						}
					}
				}

			//validate parent
			for( OntClass parent: getSuperClasses())
				{
				parent.validate(subject);
				}
			}
		}
	
	/** Something like a OWL:Property :-) */
	private abstract class OntProperty
		extends OntNode
		{
		private static final long serialVersionUID = 1L;
		OntProperty(Resource e)
			{
			super(e);
			}
		

		
		boolean isCaseSensible()
			{
			return getBoolean(new Resource(NS,"case-sensible"), true);
			}
		
		String getStartWith()
			{
			return getString(new Resource(NS,"starts-with"));
			}
		
		String getEndsWith()
			{
			return getString(new Resource(NS,"ends-with"));
			}
		
		public  Resource  getRange()
			{
			final Resource rdfRange= new Resource(RDF.NS,"range");
			for(Statement stmt: getOntology().filter(this, rdfRange, null))
				{
				if(!stmt.isResource()) continue;
				return stmt.getValue().asResource();
				}
			return null;
			}
		
		
		public abstract void validate(RDFNode node) throws RDFException;
		
		
		
		
		
		
		public void validateTextContent(String content) throws RDFException
			{
			boolean casesensible= isCaseSensible();
			if(!casesensible) content= content.toLowerCase();
			
			String startWith = getStartWith();
			if(startWith!=null)
				{
				if(!casesensible) startWith= startWith.toLowerCase();
				if(!content.startsWith(startWith)) throw new RDFException(getURI()+" should start with "+startWith);
				}
			
			String endsWith = getEndsWith();
			if(endsWith!=null)
				{
				if(!casesensible) endsWith= endsWith.toLowerCase();
				if(!content.endsWith(endsWith)) throw new RDFException(getURI()+" should end with "+startWith);
				}
			
			Integer len= Cast.Integer.cast(getString(new Resource(NS,"max-length")));
			if(len!=null && len< content.length())  throw new RDFException(getURI()+" should have a length lower than "+len);
			
			len= Cast.Integer.cast(getString(new Resource(NS,"min-length")));
			if(len!=null && len> content.length())  throw new RDFException(getURI()+" should have a length greater than "+len);
			
			len= Cast.Integer.cast(getString(new Resource(NS,"length")));
			if(len!=null && len!= content.length())  throw new RDFException(getURI()+" should have a length equals to "+len);
			
			String className= getString(new Resource(NS,"java-class"));
			if(className!=null)
				{
				try
					{
					Class<?> clazz=Class.forName(className);
					Constructor<?> cstor =clazz.getConstructor(String.class);
					cstor.newInstance(content);
					}
				catch(Throwable err)
					{
					throw new RDFException("content should match the java-class:"+className);
					}
				}
			
			
			String regex= getString(new Resource(NS,"pattern"));
			if(regex!=null)
				{
				Pattern pattern= Pattern.compile(regex, casesensible?0:Pattern.CASE_INSENSITIVE);
				if(!pattern.matcher(content).matches()) throw new RDFException(getURI()+" doesn't macth "+regex);
				}
			}
		
		public Integer getMinCardinality()
			{
			String s= getString(new Resource(OWL.NS,"minCardinality"),getString(new Resource(OWL.NS,"cardinality")));
			return Cast.Integer.cast(s);
			}
		
		public Integer getMaxCardinality()
			{
			String s= getString(new Resource(OWL.NS,"maxCardinality"),getString(new Resource(OWL.NS,"cardinality")));
			return Cast.Integer.cast(s);
			}
		@Override
		public String createDefault()
			{
			StringBuilder b= new StringBuilder();
			b.append("<"+getQName());
			if(isLiteral())
				{
				b.append(">");
				b.append("fix-me");
				b.append("</"+getQName()+">");
				}
			else
				{
				b.append("/>");
				}
			return b.toString();
			}
		}
	

	/**
	 * ObjectProperty
	 */
	class OntObjectProperty
		extends OntProperty
		{
		private static final long serialVersionUID = 1L;
		private OntClass range;
		OntObjectProperty(Resource rsrc)
			{
			super(rsrc);
			}
		
		public OntClass getOntClassInRange()
			{
			if(range!=null) return range;
			for(Statement stmt:Schema.this.getOntology().filter(this, new Resource(RDFS.NS,"range"), null))
				{
				if(!stmt.isResource()) continue;
				this.range= Schema.this.uri2class.get(stmt.getValue().asResource().getURI());
				if(this.range!=null)
					{
					return this.range;
					}
				}
			return null;
			}
		
		@Override
		public void validate(RDFNode node) throws RDFException
			{
			if(!node.isResource()) throw new RDFException(getQName()+" should be a resource");
			String content=node.asResource().getURI();
			
			String anonymous=getString(new Resource(NS,"anonymous"),"").toLowerCase();
			if(anonymous.equals("always") && !node.asResource().isAnonymous())
				{
				throw new RDFException(getURI()+" should be an anonymous resource");
				}
			else if(anonymous.equals("never") && node.asResource().isAnonymous())
				{
				throw new RDFException(getURI()+" shouldn't be an anonymous resource");
				}
			
			
			OntClass classInRange= getOntClassInRange();
			if(classInRange!=null && classInRange.isAbstract())
				{
				if(!Schema.this.getCurrentRDFStore().contains(
					node.asResource(),
					RDF_TYPE,
					classInRange.asResource())
					)
					{
					throw new RDFException(getQName()+" (rdfs:range) "+classInRange.getQName()+" range is anonymous resource: missing");
					}
				}				
			
			validateTextContent(content);
			}
		
		}
	
	/**
	 * OntDataTypeProperty
	 */
	class OntDataTypeProperty
		extends OntProperty
		{
		private static final long serialVersionUID = 1L;
		private Constructor<?> constructor=null;
		OntDataTypeProperty(Resource rsrc)
			{
			super(rsrc);
			}
		
		
		public boolean isLocalized()
			{
			Boolean b= Cast.Boolean.cast(getString(new Resource(NS,"locale"), "false"));
			if(b==null) b=false;
			return b;
			}
		
		public Constructor<?> getJavaClassInRange()throws RDFException
			{
			if(this.constructor!=null) return constructor;
			StmtSet ranges=Schema.this.getOntology().filter(
					this,
					new Resource(RDFS.NS,"range"),
					null
					);
			
			for(Statement stmt:ranges)
				{
				//System.err.println("X1:"+stmt);
				if(!stmt.isResource()) continue;
				try {
					String ttype=stmt.getValue().asResource().getURI();
					XSD<?> xsdType= XSD.findTypeByURI(ttype);
					Class<?> range= null;
					if(xsdType!=null)
						{
						range= xsdType.getJavaClass();
						}
					else if(ttype.startsWith("java:"))
						{
						range=Class.forName(ttype.substring(5));
						}
					else
						{
						range=String.class;
						}
					this.constructor= range.getConstructor(String.class);
					return constructor;
				} catch (Exception e)
					{
					e.printStackTrace();
					}
			
				}
			throw new RDFException("Cannot find java class for "+getQName());
			}
		
		
		@Override
		public void validate(RDFNode node) throws RDFException
			{
			if(!node.isLiteral()) throw new RDFException(getURI()+" shoudl be a literal");
			String content=node.asLiteral().getLexicalForm();
	
			if(isLocalized() && node.asLiteral().getLanguage()==null) throw new RDFException(getURI()+" must be localized");
			
			Constructor<?> cstor= getJavaClassInRange();
			try {
				cstor.newInstance(content);
				} 
			catch (Exception e)
				{
				throw new RDFException(getQName()+" should be a "+cstor.getDeclaringClass().getName());
				}
			
			validateTextContent(content);
			}
		
		}
	
	
	/** schema model */
	//private Document dom;
	private PrefixMapping prefixMapping= new PrefixMapping();
	private Map<String,OntClass> uri2class = new HashMap<String, OntClass>();
	private Map<String,OntProperty> uri2property = new HashMap<String, OntProperty>();
	private StmtSet currentStore=null;
	private StmtSet processed=new StmtSet();
	private StmtSet ontology=new StmtSet();
	private Set<String> categories=new TreeSet<String>();
	public StmtSet getOntology()
		{
		return this.ontology;
		}
	
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
		}
	
	public Schema(final StmtSet stmts,PrefixMapping prefixMapping)
		{
		this.ontology=new StmtSet(stmts);
		this.prefixMapping=prefixMapping;
		
		final Resource owlClass= new Resource(OWL.NS,"Class");
		final Resource objectProperty = new Resource(OWL.NS,"ObjectProperty");
		final Resource dataTypeProperty = new Resource(OWL.NS,"DataTypeProperty");
		
		for(int i=0;i< 2;++i)
			{
			//collect i=0 classes i=1 properties
			for(Statement stmt: this.ontology)
				{
				if(!stmt.isResource()) continue;
				if(!stmt.getPredicate().equals(RDF_TYPE)) continue;
				if(i==0 && owlClass.equals(stmt.getValue().asResource()))
					{
					this.uri2class.put(stmt.getSubject().getURI(),new OntClass(stmt.getSubject()));
					}
				else if(i==1)
					{
					if(objectProperty.equals(stmt.getValue().asResource()))
						{
						this.uri2property.put(stmt.getSubject().getURI(),new OntObjectProperty(stmt.getSubject()));
						}
					else if(dataTypeProperty.equals(stmt.getValue().asResource()))
						{
						this.uri2property.put(stmt.getSubject().getURI(),new OntDataTypeProperty(stmt.getSubject()));
						}
					}
				}
			}
		
		
		}
	
	public Collection<OntClass> getOntClasses()
		{
		return this.uri2class.values();
		}
	
	public String createEmptyRDFDocument()
		{
		StringBuilder b= new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
		b.append(getPrefixMapping().createDocType());
		b.append("<rdf:RDF\n");
		for(String p:getPrefixMapping().getPrefixes())
			{
			b.append("  xmlns:"+p+"=\"&"+p+";\"\n");
			}
		b.append("  >\n");
		
		b.append("</rdf:RDF>");
		return b.toString();
		}
	
	private final StmtSet getCurrentRDFStore()
		{
		return this.currentStore;
		}
	

	
	public void validate(StmtSet stmts) throws RDFException
		{
		this.currentStore=stmts;
		this.processed= new StmtSet();
		this.categories.clear();
		//check all rdf:type
		StmtSet types=stmts.filter(null, RDF_TYPE, null);
		if(types.isEmpty()) throw new RDFException("Document is missing a rdf:type");
		for(Statement stmt: types)
			{
			
			if(!stmt.isResource()) throw new RDFException("rdf:type is not a resource");
			
			OntClass clazz= uri2class.get(stmt.getValue().asResource().getURI());
			if(clazz==null) throw new RDFException("Unknown type :"+stmt);
			
			clazz.validate(stmt.getSubject());
			this.categories.addAll(clazz.getCategories());

			this.processed.add(stmt);
			}
		//check if there is no unprocessed statement
		StmtSet copy= new StmtSet(stmts);
		copy.removeAll(this.processed);
		if(!copy.isEmpty())
			{
			//TODO
			//throw new RDFException("Unprocessed statement :  "+copy.iterator().next());
			}
		this.processed=null;
		this.currentStore=null;
		}
	
	
	public Set<String> getCategories()
		{
		return this.categories;
		}
	
	
	/** recursive call to check that an abstract class was implemented */
	private  boolean validateAbstraction(Resource subject,OntClass clazz)
		{
		if(!clazz.isAbstract())
			{
			if(!getCurrentRDFStore().filter(null, RDF_TYPE,clazz.asResource()).isEmpty())
				{
				return true;
				}
			}
		
		for(OntClass child:clazz.getChildClasses())
			{
			validateAbstraction(subject,child);
			}
		return false;
		}
	

	}
