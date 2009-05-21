package org.lindenb.sw.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.RDFException;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.C;
import org.lindenb.util.XObject;



/**
 * DerbyModel a RDF store
 * @author pierre
 *
 */
public abstract class AbstractRDFModel
{
/** an iterator that should be closed at the end of its processing */
public static interface CloseableIterator<E> extends Iterator<E>
	{
	/** close this iterator */
	public void close() throws RDFException;
	};

protected class CloseableIteratorAdapter<E>
	implements CloseableIterator<E>
	{
	private Iterator<E> iter;
	protected E last=null;
	CloseableIteratorAdapter(Iterator<E> x)
		{
		this.iter=x;
		}
	@Override
	public boolean hasNext() { 
		boolean b= this.iter.hasNext();
		if(!b) last=null;
		return b;
		}
	@Override
	public E next() {
		return (last=this.iter.next());
	}
	@Override
	public void remove() {
		this.iter.remove();
		}
	public void close() throws RDFException {}
	}
	
/**
 * 
 * @author pierre
 *
 */
public interface RDFNode extends Serializable
	{
	public AbstractRDFModel getModel();
	public abstract short getNodeType();
	public boolean isResource();
	public boolean isLiteral();
	public Resource asResource();
	public Literal asLiteral();
	public CloseableIterator<Statement> listStatementsWithValue()  throws RDFException;
	public CloseableIterator<Statement> listStatementsWithValue(Resource predicate) throws RDFException;
	}
	
/**
 * Resource
 *
 */
public interface Resource
	extends RDFNode,Comparable<Resource>
	{
    @Override
    public short getNodeType();
	/** @return the namespace of the URI or null if it cannot be extracted */
	public String getNamespaceURI();
	/** @return the local name of the URI or null if it cannot be extracted */
	public String getLocalName();
	
	/** return the URI of this resource */
	public String getURI();
	/** @return the prefix of the URI extracted from the prefixMap of the model or null if it cannot be extracted */
	public String getPrefix();
	
	public String getQName();
	
	public String getShortName();
	
	public boolean isOpaqueURI();
	
	public boolean isAbsolute();
	
	public boolean isURL();
	
	public URI asURI();
	
	public URL asURL();
	
	
	@Override
	public int compareTo(Resource o);
	
	
	public Resource getPropertyAsResource(Resource predicate,Resource defaultValue) throws RDFException;
	
	
	public Resource getPropertyAsResource(Resource predicate) throws RDFException;
	
	
	public Literal getPropertyAsLiteral(Resource predicate,Literal defaultValue) throws RDFException;


	public Literal getPropertyAsLiteral(Resource predicate) throws RDFException;
	
	
	public Resource setProperty(Resource predicate,RDFNode value) throws RDFException;
	
	public Resource addProperty(Resource predicate,RDFNode value) throws RDFException;
	
	
	public CloseableIterator<Statement> listProperties() throws RDFException;
	
	public CloseableIterator<Statement> listProperties(String predicate) throws RDFException;
	
	public CloseableIterator<Statement> listProperties(Resource predicate) throws RDFException;
	
	//public CloseableIterator<RDFNode> listValues(Resource predicate) throws RDFException;
		
	//public CloseableIterator<Literal> listValuesAsLiteral(Resource predicate) throws RDFException;
	
	
	//public CloseableIterator<Resource> listValuesAsResource(Resource predicate) throws RDFException;
	
	public Statement getProperty(Resource predicate) throws RDFException;
	
	
	public String getString(Resource predicate,String defaultValue) throws RDFException;
	
	public String getString(Resource predicate) throws RDFException;
	
	
	public Statement getRequiredProperty(Resource predicate) throws RDFException;
	
	public boolean hasProperty(Resource predicate) throws RDFException;
	
	public boolean hasProperty(Resource predicate,RDFNode value) throws RDFException;
	
	
	public int removeProperties() throws RDFException;
	
	public int removeAll(Resource predicate) throws RDFException;
	

	}

/**
 * Literal
 *
 */
public interface Literal
	extends RDFNode,Comparable<Literal>
	{
	@Override
    public short getNodeType();
	public String getString();
	@Override
	public int compareTo(Literal o);
	public <T> T castTo(Class<T> clazz);
	public boolean canAs(Class<?> clazz);
	}

public interface Statement
	extends Comparable<Statement>,Serializable
	{
	@Override
	public int compareTo(Statement o);
	public AbstractRDFModel getModel();
	public Resource getSubject();
	public Resource getPredicate();
	public RDFNode getValue();
	}



/**
 * 
 * @author pierre
 *
 */
protected abstract class RDFNodeImpl
	extends XObject
	implements RDFNode
	{
	private static final long serialVersionUID = 1L;

	protected RDFNodeImpl()
		{
		}
	
	public abstract short getNodeType();
	public boolean isResource() {return getNodeType()==VALUE_IS_RESOURCE;}
	public boolean isLiteral() {return getNodeType()==VALUE_IS_LITERAL;}
	public Resource asResource() { return Resource.class.cast(this);}
	public Literal asLiteral() { return Literal.class.cast(this);}
	
	public CloseableIterator<Statement> listStatementsWithValue() throws RDFException
		{
		return getModel().listStatements(
				null, null, getNodeType(),
				isResource()?asResource().getURI():asLiteral().getString()
				);
		}
	
	public CloseableIterator<Statement> listStatementsWithValue(Resource predicate) throws RDFException
		{
		return getModel().listStatements(
				null, predicate, getNodeType(),
				isResource()?asResource().getURI():asLiteral().getString()
				);
		}
	
	
	
	}
	
/**
 * Resource
 *
 */
protected abstract class ResourceImpl
	extends RDFNodeImpl
	implements Resource
	{
	private static final long serialVersionUID = 1L;
	private String uri;

	protected ResourceImpl(String uri)
		{
		this.uri=uri;
		}

    @Override
    public short getNodeType() {
    	return VALUE_IS_RESOURCE;
    	}
	
	protected int delim()
		{
		int i= getURI().lastIndexOf('#');
		if(i==-1) i=getURI().lastIndexOf('/');
		return i;
		}
	/** @return the namespace of the URI or null if it cannot be extracted */
	public String getNamespaceURI()
		{
		int i= delim();
		return i==-1?null:getURI().substring(0,i+1);
		}
	
	/** @return the local name of the URI or null if it cannot be extracted */
	public String getLocalName() {
		int i= delim();
		return i==-1?null:getURI().substring(i+1);
		}
	
	/** return the URI of this resource */
	public String getURI()
		{
		return this.uri;
		}
	/** @return the prefix of the URI extracted from the prefixMap of the model or null if it cannot be extracted */
	public String getPrefix()
		{
		String ns=getNamespaceURI();
		return ns==null?null:getModel().getPrefixMapping().getNsURIPrefix(ns);
		}
	
	public String getQName()
		{
		String p=getPrefix();
		String L= getLocalName();
		return (p!=null && L!=null?p+":"+L:null);
		}
	
	public String getShortName()
		{
		String s=getQName();
		return s==null?getURI():s;
		}
	
	@Override
	public int hashCode() {
		return getURI().hashCode();
		}
	
	public boolean isOpaqueURI()
		{
		return asURI().isOpaque();
		}
	
	public boolean isAbsolute()
		{
		return asURI().isAbsolute();
		}
	
	public boolean isURL()
		{
		try {
			new URL(getURI());
			return true;
			}
		catch (MalformedURLException e)
			{
			return false;
			}
		}
	
	public URI asURI()
		{
		return URI.create(getURI());
		}
	
	public URL asURL()
		{
		try {
			return new URL(getURI());
			}
		catch (MalformedURLException e)
			{
			return null;
			}
		}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null || !(obj instanceof Resource)) return false;
		Resource cp = Resource.class.cast(obj);
		return	getURI().equals(cp.getURI())
				;
		}
	
	@Override
	public int compareTo(Resource o) {
		return getURI().compareTo(o.getURI());
		}
	
	
	
	public Resource getPropertyAsResource(Resource predicate,Resource defaultValue) throws RDFException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this, predicate, VALUE_IS_RESOURCE,null);
		Resource value=(iter.hasNext()?iter.next().getValue().asResource():defaultValue);
		iter.close();
		return value;
		}
	
	
	public Resource getPropertyAsResource(Resource predicate) throws RDFException
		{
		return getPropertyAsResource(predicate,null);
		}
	
	
	public Literal getPropertyAsLiteral(Resource predicate,Literal defaultValue) throws RDFException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this, predicate, VALUE_IS_LITERAL,null);
		Literal value=(iter.hasNext()?iter.next().getValue().asLiteral():defaultValue);
		iter.close();
		return value;
		}


	public Literal getPropertyAsLiteral(Resource predicate) throws RDFException
		{
		return getPropertyAsLiteral(predicate,null);
		}
	
	
	public Resource setProperty(Resource predicate,RDFNode value) throws RDFException
		{
		removeAll(predicate);
		addProperty(predicate,value);
		return this;
		}
	
	public Resource addProperty(Resource predicate,RDFNode value) throws RDFException
		{
		addStatement(createStatement(this, predicate, value));
		return this;
		}
	
	
	public CloseableIterator<Statement> listProperties() throws RDFException
		{
		return getModel().listStatements(this, null, null, null);
		}
	
	public CloseableIterator<Statement> listProperties(String predicate) throws RDFException
		{
		return listProperties(createResource(predicate));
		}
	
	public CloseableIterator<Statement> listProperties(Resource predicate) throws RDFException
		{
		return getModel().listStatements(this, predicate, null, null);
		}
	
	
	public Statement getProperty(Resource predicate) throws RDFException
		{
		CloseableIterator<Statement> iter= listProperties(predicate);
		Statement value=(iter.hasNext()?iter.next():null);
		iter.close();
		return value;
		}
	
	
	public String getString(Resource predicate,String defaultValue) throws RDFException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this,predicate, AbstractRDFModel.VALUE_IS_LITERAL, null);
		Statement value=(iter.hasNext()?iter.next():null);
		iter.close();
		return value==null?defaultValue:value.getValue().asLiteral().getString();
		}
	
	public String getString(Resource predicate) throws RDFException
		{
		return getString(predicate,null);
		}
	
	
	public Statement getRequiredProperty(Resource predicate) throws RDFException
		{
		Statement stmt= getProperty(predicate);
		if(stmt==null) throw new RDFException("Property not found "+predicate);
		return stmt;
		}
	
	public boolean hasProperty(Resource predicate) throws RDFException
		{
		return getProperty(predicate)!=null;
		}
	
	public boolean hasProperty(Resource predicate,RDFNode value) throws RDFException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this,predicate , value);
		boolean b= iter.hasNext();
		iter.close();
		return b;
		}
	
	
	public int removeProperties() throws RDFException
		{
		return getModel()._removeStatements(this, null, null, null);
		}
	
	public int removeAll(Resource predicate) throws RDFException
		{
		return getModel()._removeStatements(this, predicate, null, null);
		}
	
	@Override
	public String toString() {
		return getShortName();
		}
	
	
	}

/**
 * Literal
 *
 */
protected abstract  class LiteralImpl
	extends RDFNodeImpl
	implements Literal
	{
	private static final long serialVersionUID = 1L;
	private String text;
	protected LiteralImpl(String text)
		{
		this.text=text;
		}
	@Override
    public short getNodeType() {
    	return VALUE_IS_LITERAL;
    	}
	
	@Override
	public int hashCode() {
		return getString().hashCode();
		}
	
	public String getString()
		{
		return this.text;
		}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null || !(obj instanceof Literal)) return false;
		Literal cp = Literal.class.cast(obj);
		return	getString().equalsIgnoreCase(cp.getString())
				;
		}
	
	@Override
	public int compareTo(Literal o) {
		return getString().compareToIgnoreCase(o.getString());
		}
	
	@Override
	public String toString() {
		return getString();
		}
	
	
	public <T> T castTo(Class<T> clazz)
		{
		try {
			Constructor<T> cst=clazz.getConstructor(String.class);
			return cst.newInstance(this.getString());
			}
		catch (Exception e) {
			return null;
			}
		}

	public boolean canAs(Class<?> clazz)
		{
		try {
			return castTo(clazz)!=null;
			} 
		catch (Exception e) {
			return false;
			}
		}
	

	}

public abstract class AbstractStatementImpl extends XObject
	implements Statement
	{
	private static final long serialVersionUID = 1L;
	private Resource subject;
	private Resource predicate;
	private RDFNode value;
	
	private AbstractStatementImpl(
			Resource subject,
			Resource predicate,
			RDFNode value
			)
		{
		this.subject=subject;
		this.predicate=predicate;
		this.value=value;
		}
	
	@Override
	public int compareTo(Statement o) {
		int i= getSubject().compareTo(o.getSubject());
		if(i!=0) return i;
		i= getPredicate().compareTo(o.getPredicate());
		if(i!=0) return i;
		
		if(getValue().isLiteral() )
			{
			if( o.getValue().isLiteral())
				{
				return getValue().asLiteral().compareTo(o.getValue().asLiteral());
				}
			else
				{
				return 1;
				}
			}
		else
			{
			if(o.getValue().isResource())
				{
				return getValue().asResource().compareTo(o.getValue().asResource());
				}
			else
				{
				return -1;
				}
			}
		}
	
	@Override
	public abstract AbstractRDFModel getModel();
	@Override
	public Resource getSubject() { return this.subject; }
	@Override
	public Resource getPredicate() { return this.predicate; }
	@Override
	public RDFNode getValue() { return this.value; }
	
	@Override
	public int hashCode() {
		return getSubject().hashCode()+getPredicate().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null || !(obj instanceof Statement)) return false;
		Statement cp = Statement.class.cast(obj);
		return	getSubject().equals(cp.getSubject()) &&
				getPredicate().equals(cp.getPredicate()) &&
				getValue().equals(cp.getValue())
				;
		}
	
	@Override
	public String toString() {
		return getSubject().toString()+" "+getPredicate()+" "+getValue();
		}
	
	
	
	}





protected static final short VALUE_IS_LITERAL=0;
protected static final short VALUE_IS_RESOURCE=1;
private PrefixMapping prefixMapping = new PrefixMapping();
protected static  long ID_GENERATOR= System.currentTimeMillis();
protected static Random RANDOM= new Random(System.currentTimeMillis());

protected AbstractRDFModel()
	{
	
	}



@Override
protected void finalize() throws Throwable {
	try {
		close();
		} 
	catch (Exception e) {
		}
	super.finalize();
	}

/** closes the model */
public void close() throws RDFException
	{
	
	}

/** @return the internal prefixMapping */
public PrefixMapping getPrefixMapping()
	{
	return this.prefixMapping;
	}

/** @return the number of statements in the model */
public int size() throws RDFException
	{
	int n=0;
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext()) { iter.next();++n;}
	iter.close();
	return n;
	}

/** @return wether this model is empty */
public boolean isEmpty() throws RDFException
	{
	CloseableIterator<Statement> iter= listStatements();
	boolean e=!iter.hasNext();
	iter.close();
	return e;
	}

/** List all distinct subjects.*/
public CloseableIterator<Resource> listSubjects() throws RDFException
	{
	HashSet<Resource> subjects= new HashSet<Resource>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext()) { subjects.add(iter.next().getSubject());}
	iter.close();
	return new CloseableIteratorAdapter<Resource>(subjects.iterator());
	}

/** List all distinct predicates .*/
public CloseableIterator<Resource> listProperties() throws RDFException
	{
	HashSet<Resource> predicates = new HashSet<Resource>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext()) { predicates.add(iter.next().getPredicate());}
	iter.close();
	return new CloseableIteratorAdapter<Resource>(predicates.iterator());
	}

/** add all the statements in the List */
public AbstractRDFModel add(Collection<Statement> col) throws RDFException
	{
	return add(col.iterator());
	}

/** add all the statements in the iterator */
public AbstractRDFModel add(Iterator<Statement> iter) throws RDFException
	{
	while(iter.hasNext())
		{
		addStatement(iter.next());
		}
	return this;
	}

/** add all the statements from another model */
public AbstractRDFModel add(AbstractRDFModel model) throws RDFException
	{
	if(this==model) return this;
	CloseableIterator<Statement> iter= model.listStatements();
	while(iter.hasNext())
		{
		addStatement(iter.next());
		}
	iter.close();
	return this;
	}


/** remove all the statements in the iterator */
public AbstractRDFModel remove(Collection<Statement> collection) throws RDFException
	{
	return remove(collection.iterator());
	}

/** remove all the statements in the iterator */
public AbstractRDFModel remove(Iterator<Statement> iter) throws RDFException
	{
	while(iter.hasNext())
		{
		remove(iter.next());
		}
	return this;
	}

/** Answer a statement (s, p) from this model. If none exist, return null*/
public Statement getProperty( Resource s, Resource p ) throws RDFException
	{
	CloseableIterator<Statement> iter= listStatements(s, p, null, null);
	Statement found=(iter.hasNext()?iter.next():null);
	iter.close();
	return found;
	}



	/** List all subjects with a given property. .remove() is not implemented on
     *  this iterator.
     * @return an iterator over the subjects
     * @param p the property sought.
     */
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate) throws RDFException
	{
	HashSet<Resource> subjects= new HashSet<Resource>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		if(!(stmt.getPredicate().equals(predicate))) continue;
		subjects.add(stmt.getSubject());
		}
	iter.close();
	return new CloseableIteratorAdapter<Resource>(subjects.iterator());
	}

/** List all subjects with a given property and property value*/
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate, RDFNode value) throws RDFException
	{
	HashSet<Resource> subjects= new HashSet<Resource>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		if(!(stmt.getPredicate().equals(predicate) &&
			stmt.getValue().equals(value))) continue;
		subjects.add(stmt.getSubject());
		}
	iter.close();
	return new CloseableIteratorAdapter<Resource>(subjects.iterator());
	}

/** List all values in a model.*/
public CloseableIterator<RDFNode> listObjects() throws RDFException
	{
	HashSet<RDFNode> values= new HashSet<RDFNode>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		values.add(iter.next().getValue());
		}
	iter.close();
	return new CloseableIteratorAdapter<RDFNode>(values.iterator());
	}

/** List all objects of a given property..*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource predicate) throws RDFException
	{
	HashSet<RDFNode> values= new HashSet<RDFNode>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		if(!(stmt.getPredicate().equals(predicate))) continue;
		values.add(iter.next().getValue());
		}
	iter.close();
	return new CloseableIteratorAdapter<RDFNode>(values.iterator());
	}

/** List the values of a property of a resource*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource subject,Resource predicate) throws RDFException
	{
	HashSet<RDFNode> values= new HashSet<RDFNode>();
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		if(!(stmt.getSubject().equals(subject) &&
			stmt.getPredicate().equals(predicate))) continue;
		values.add(iter.next().getValue());
		}
	iter.close();
	return new CloseableIteratorAdapter<RDFNode>(values.iterator());
	
	
	}

/** Determine whether this model contains any statements with a given subject*/
public boolean contains(Resource s, Resource p) throws RDFException
	{
	return contains(s, p,null);
	}

/** /** Determine if an (S, P, O) pattern is present in this model, with null a lowed*/
public boolean contains(Resource s, Resource p,RDFNode value) throws RDFException
	{
	CloseableIterator<Statement> iter= listStatements(
			s, p,
			value==null?null:value.getNodeType(),
			value==null?null:value.isLiteral()?
						value.asLiteral().getString():
						value.asResource().getURI()
			);
	boolean found=iter.hasNext();
	iter.close();
	return found;
	}

/** Determine if a statement is present in this model.*/
public boolean contains(Statement stmt) throws RDFException
	{
	return contains(stmt.getSubject(),stmt.getPredicate(),stmt.getValue());
	}

/** Determine if a subject is present in this model.*/
public boolean containsSubject(Resource subject) throws RDFException
	{
	CloseableIterator<Statement> iter= listStatements(
			subject, null,null
			);
	boolean found=iter.hasNext();
	iter.close();
	return found;
	}


protected boolean isParanoid()
	{
	return true;
	}



private String createAnonymousURI() throws RDFException
	{
	String id=null;
	while(true)
		{
		id="_"+(++ID_GENERATOR)+"_"+RANDOM.nextInt(Short.MAX_VALUE);
		if(!containsSubject(createResource(id))) break;
		}
	return id;
	}

protected String createAnonymousURI(String template) throws RDFException
	{
	if(template==null || template.trim().length()==0) return createAnonymousURI();
	template=template.trim().replaceAll("[^a-zA-Z0-9_]", "").trim();
	if(template.length()==0) return createAnonymousURI();
	if(!template.startsWith("_")) template="_"+template;
	
	String id=null;
	for(int tries=0;tries<10;++tries)
		{
		id= template+"_"+(++ID_GENERATOR);
		
		try {
			new URI(id);
			}
		catch (URISyntaxException e) {
			id=null;
			continue;
			}
		if(!containsSubject(createResource(id))) break;
		}
	if(id==null) return createAnonymousURI();
	return id;
	}


public boolean addStatement(Resource subject,Resource predicate,RDFNode value)    throws RDFException
	{
	return addStatement(createStatement(subject, predicate, value));
	}

public abstract boolean addStatement(Statement stmt)   throws RDFException;


/** creates an anonymous resource using template as a template for the uri*/
public Resource createAnonymousResource(String template) throws RDFException
	{
	return createResource(createAnonymousURI(template));
	}

/** creates an anonymous resource */
public Resource createResource() throws RDFException
	{
	return createResource(createAnonymousURI());
	}

/** creates resource from a namespace and a localName */
public Resource createResource(String namespaceURI,String localName)
	{
	return createResource(namespaceURI+localName);
	}

/** creates resource from an URL */
public Resource createResource(URL url)
	{
	return createResource(url.toString());
	}

/** creates resource from an URI */
public Resource createResource(URI uri)
	{
	return createResource(uri.toString());
	}

/** creates resource from a String URI */
public  Resource createResource(String uri)
	{
	return new ResourceImpl(uri)
		{
		private static final long serialVersionUID = 1L;

		@Override
		public AbstractRDFModel getModel() {
			return AbstractRDFModel.this;
			}
		};
	}

/** creates a literal from a text */
public Literal createLiteral(String text)
	{
	return new LiteralImpl(text)
		{
		private static final long serialVersionUID = 1L;
		@Override
		public AbstractRDFModel getModel() {
			return AbstractRDFModel.this;
			}
		};
	}



public Statement createStatement( Resource subject,Resource predicate,RDFNode value) throws RDFException
	{
	return new AbstractStatementImpl(subject,predicate,value)
		{	
		private static final long serialVersionUID = 1L;

		@Override
		public AbstractRDFModel getModel()
			{
			return AbstractRDFModel.this;
			}
		};
	}

public CloseableIterator<Statement> listStatements() throws RDFException
	{
	return listStatements(null,null,null,null);
	}

public CloseableIterator<Statement> listStatements(
		Resource subject,
		Resource predicate,
		RDFNode value
		) throws RDFException
	{
	return listStatements(subject, predicate,
			value==null?null:value.getNodeType(),
			value==null?null:value.isLiteral()?value.asLiteral().getString():value.asResource().getURI());
	}
		


public abstract CloseableIterator<Statement> listStatements(
	Resource subject,
	Resource predicate,
	Short valueType,
	String value
	) throws RDFException;


/** remove ALL the statements from the RDF store
 * @return number of statements deleted
 * */
public abstract int clear()throws RDFException;


/** remove the given statement from the RDF store
 * @return true if a statement was deleted
 * */
public boolean remove(Statement stmt)throws RDFException
	{
	return _removeStatements(
			stmt.getSubject(),
			stmt.getPredicate(),
			stmt.getValue().getNodeType(),
			stmt.getValue().isLiteral()?
				stmt.getValue().asLiteral().getString():
				stmt.getValue().asResource().getURI()
			)==1;
	}

protected abstract int _removeStatements(
		Resource subject,
		Resource predicate,
		Short valueType,
		String value
		) throws RDFException;


public void saveAsN3(File n3file) throws RDFException,IOException
	{
	PrintWriter out= new PrintWriter(new FileWriter(n3file));
	saveAsN3(out);
	out.flush();
	out.close();
	}

public void saveAsN3(PrintWriter out) throws RDFException,IOException
	{
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		printAsN3(stmt,out);
		}
	iter.close();
	out.flush();
	}

public void saveAsRDF(File rdfFile) throws XMLStreamException,RDFException,IOException
	{
	FileWriter out= new FileWriter(rdfFile);
	saveAsRDF(out);
	out.flush();
	out.close();
	}

public void saveAsRDF(Writer out) throws XMLStreamException,RDFException
	{
	XMLOutputFactory factory= XMLOutputFactory.newInstance();
	XMLStreamWriter w= factory.createXMLStreamWriter(out);
	w.setPrefix("rdf", RDF.NS);
	w.writeStartDocument("UTF-8","1.0");
	w.writeStartElement("rdf", "RDF", RDF.NS);
	w.writeAttribute("xmlns:rdf", RDF.NS);
	
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		write(iter.next(),w);
		}
	iter.close();
	
	w.writeEndElement();
	w.writeEndDocument();
	w.flush();
	}


private void printAsN3(Statement stmt,PrintWriter out)
	{
	out.print("<"+stmt.getSubject().getURI().concat(">\t"));
	out.print("<"+stmt.getPredicate().getURI()+">\t");
	if(stmt.getValue().isResource())
		{
		out.print("<"+stmt.getValue().asResource().getURI()+">");
		}
	else
		{
		out.print("\""+C.escape(stmt.getValue().asLiteral().getString())+"\"");
		}
	out.println(".");
	}

private void write(Statement stmt,XMLStreamWriter w) throws XMLStreamException
	{
	w.writeStartElement("rdf", "Statement", RDF.NS);
	w.writeCharacters("\n ");
	
	w.writeEmptyElement("rdf", "subject", RDF.NS);
	w.writeAttribute("rdf", RDF.NS,
			(stmt.getSubject().isAbsolute()?"resource":"ID")
			,stmt.getSubject().getURI());
	w.writeCharacters("\n ");
	
	w.writeEmptyElement("rdf", "predicate", RDF.NS);
	w.writeAttribute("rdf",RDF.NS,
			(stmt.getPredicate().isAbsolute()?"resource":"ID")
			,stmt.getPredicate().getURI());
	w.writeCharacters("\n ");
	
	
	if(stmt.getValue().isResource())
		{
		w.writeEmptyElement("rdf", "object", RDF.NS);
		if(stmt.getValue().asResource().isAbsolute())
			{
			w.writeAttribute("rdf",RDF.NS,"resource",stmt.getValue().asResource().getURI());
			}
		else
			{
			w.writeAttribute("rdf",RDF.NS,"ID","#"+stmt.getValue().asResource().getURI());
			}
		}
	else
		{
		w.writeStartElement("rdf", RDF.NS,"object" );
		w.writeCharacters(stmt.getValue().asLiteral().getString());
		w.writeEndElement();
		}
	
	
	w.writeCharacters("\n");
	w.writeEndElement();
	w.writeCharacters("\n");
}


}
