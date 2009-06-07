package org.lindenb.mwrdf;



import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.RDFException;
import org.lindenb.sw.dom.DOM4RDF;
import org.lindenb.sw.nodes.Literal;
import org.lindenb.sw.nodes.RDFNode;
import org.lindenb.sw.nodes.Resource;
import org.lindenb.sw.nodes.Statement;
import org.lindenb.sw.nodes.StmtSet;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.RDFS;
import org.lindenb.util.Cast;
import org.lindenb.xml.NodeWrapper;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Schema
	{
	public static final String NS="http://mwrdf.lindenb.org";
	private static final Resource RDF_TYPE= new Resource(RDF.NS,"type");
	
	private class OntNode
		extends Resource
		{
		OntNode(Resource rsrc)
			{
			super(rsrc);
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

		}
	
	/** Something like a OWLClass :-) */
	private class OntClass extends OntNode
		{
		private Set<OntClass> parentClasses=null;
		private Set<OntClass> childClasses=null;
		private Set<OntProperty> properties=null;
		
		OntClass(Resource e)
			{
			super(e);
			}
		
		boolean isAbstract()
			{
			return getBoolean(new Resource(NS,"abstract"), false);
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
				OntClass clazz= Schema.this.uri2class.get(stmt.getSubject());
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
				OntProperty prop= Schema.this.uri2property.get(stmt.getSubject());
				if(prop==null) continue;
				this.properties.add(prop);
				}
			return this.properties;
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
				if(prop.isResource() || (prop.isLiteral() && !((DataTypeProperty)prop).isLocalized()))
					{
					if(min!=null && stmts.size() < min)
						{
						throw new RDFException(prop.getURI()+" has min cardinality:"+min);
						}
					if(max!=null && stmts.size() > max)
						{
						throw new RDFException(prop.getURI()+" has max cardinality:"+min);
						}
					for(Statement stmt:stmts)
						{
						prop.validate(stmt.getValue());
						}
					}
				else // group by language
					{
					Map<String, Integer> lang2stmt = new HashMap<String, Integer>();
					for(Statement stmt:stmts)
						{
						prop.validate(stmt.getValue());
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
		OntProperty(Resource e)
			{
			super(e);
			}
		public abstract boolean isLiteral();
		public final boolean isResource()
			{
			return !isLiteral();
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
		
		public abstract void validate(RDFNode node) throws RDFException;
		
		public void validate(String content) throws RDFException
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
			String s= getString(new Resource(NS,"min-cardinality"),getString(new Resource("cardinality")));
			return Cast.Integer.cast(s);
			}
		
		public Integer getMaxCardinality()
			{
			String s= getString(new Resource(NS,"max-cardinality"),getString(new Resource("cardinality")));
			return Cast.Integer.cast(s);
			}
		}
	
	private class DataTypeProperty extends OntProperty
		{
		DataTypeProperty(Resource e)
			{
			super(e);
			}
		@Override
		public boolean isLiteral() {
			return true;
			}
		
		public boolean isLocalized()
			{
			Boolean b= Cast.Boolean.cast(getString(new Resource(NS,"locale"), "false"));
			if(b==null) b=false;
			return b;
			}
		
		@Override
		public void validate(RDFNode node) throws RDFException
			{
			if(!node.isLiteral()) throw new RDFException(getURI()+" shoudl be a literal");
			String content=node.asResource().getURI();
	
			if(isLocalized() && node.asLiteral().getLanguage()==null) throw new RDFException(getURI()+" must be localized");
			
			String dataType= getString(new Resource(NS,"data-type"));
			if(dataType!=null)
				{
				if(node.asLiteral().getDatatypeURI()==null) throw new RDFException(getURI()+" must be typed");
				boolean found=false;
				for(String s: dataType.split("[ ,;]+"))
					{
					if(s.equals(node.asLiteral().getDatatypeURI()))
						{
						found=true;
						break;
						}
					}
				if(!found) throw new RDFException(getURI()+" must be typed");
				}
			
			validate(content);
			}
		}
	private class ObjectProperty extends OntProperty
		{
		ObjectProperty(Resource e)
			{
			super(e);
			}
		@Override
		public boolean isLiteral()
			{
			return false;
			}
		
		public void validate(RDFNode node) throws RDFException
			{
			if(!node.isResource()) throw new RDFException(getURI()+" should be a resource");
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
			
			String range= getString(new Resource(NS,"range"));
			if(range!=null)
				{
				OntClass classInRange= Schema.this.uri2class.get(range);
				if(classInRange!=null)
					{
					if(classInRange.isAbstract())
						{
						//TODO subClass range
						if(!Schema.this.getCurrentRDFStore().contains(
							node.asResource(),
							RDF_TYPE,
							classInRange.asResource())
							)
							{
							throw new RDFException(getURI()+" range is anonymous resource: missing");
							}
						}
					}
				}
			
			validate(content);
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
	
	public StmtSet getOntology()
		{
		return this.ontology;
		}
	
	public Schema(final StmtSet stmts)
		{
		this.ontology=new StmtSet(stmts);
		final Resource rdfsClass= new Resource(RDFS.NS,"Class");
		final Resource rdfProperty = new Resource(RDF.NS,"Class");
		for(Statement stmt: this.ontology)
			{
			if(!stmt.getValue().isResource()) continue;
			if(!stmt.getPredicate().equals(RDF_TYPE)) continue;
			if(rdfsClass.equals(stmt.getValue().asResource()))
				{
				this.uri2class.put(stmt.getSubject().getURI(),new OntClass(stmt.getSubject()));
				}
			else if(rdfProperty.equals(stmt.getValue().asResource()))
				{
				this.uri2property.put(stmt.getSubject().getURI(),new DataTypeProperty(stmt.getSubject()));
				}
			}
		//collect classes
		
		}
	
	
	public String createEmptyRDFDocument()
		{
		PrefixMapping prefixMapping= new PrefixMapping();
		StringBuilder b= new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
		b.append(prefixMapping.createDocType());
		b.append("<rdf:RDF\n");
		for(String p:prefixMapping.getPrefixes())
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
		//check all rdf:type
		StmtSet types=stmts.filter(null, RDF_TYPE, null);
		if(types.isEmpty()) throw new RDFException("Document is missing a rdf:type");
		for(Statement stmt: types)
			{
			if(!stmt.isResource()) throw new RDFException("rdf:type is not a resource");
			OntClass clazz= uri2class.get(stmt.getValue().asResource().getURI());
			if(clazz==null) throw new RDFException("Unknown type :"+stmt);
			clazz.validate(stmt.getSubject());
			
			this.processed.add(stmt);
			}
		//check if there is no unprocessed statement
		StmtSet copy= new StmtSet(stmts);
		copy.removeAll(this.processed);
		if(!copy.isEmpty())
			{
			throw new RDFException("Unprocessed statement :  "+copy.iterator().next());
			}
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
