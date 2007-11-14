package org.lindenb.sw.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import org.lindenb.sql.SQLUtilities;
import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.XObject;


/**
 * DerbyModel a RDF store
 * @author pierre
 *
 */
public class DerbyModel
{
/** an iterator that should be closed at the end of its processing */
public static interface CloseableIterator<E> extends Iterator<E>
	{
	/** close this iterator */
	public void close() throws SQLException;
	};

	
	
private abstract class ResultSetIterator<E>
	implements CloseableIterator<E>
	{
	private ResultSet row;
	private boolean currentRowIsNext=false;
	private boolean currentRowExists=true;
	protected ResultSetIterator(ResultSet row) throws SQLException
		{
		this.row=row;
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
	
	@Override
	public void close() throws SQLException
		{
		if(!this.row.isClosed())
			{
			java.sql.Statement owner= this.row.getStatement();
			Connection con= owner.getConnection();
			this.row.close();
			owner.close();
			recycleConnection(con);
			}
		this.currentRowExists=false;
		}

	@Override
	public boolean hasNext()
		{
		if(!this.currentRowExists) return false;
		if(!this.currentRowIsNext)
			{
			try
				{
				this.currentRowExists= this.row.next();
				this.currentRowIsNext=true;
				if(!this.currentRowExists)
					{
					close();
					}
				}
			catch(SQLException sql)
				{
				throw new java.lang.RuntimeException("next():"+sql.getMessage(),sql);
				}
			}
		return this.currentRowExists;
		}

	@Override
	public E next()
		{
		if(!this.currentRowIsNext) hasNext();
		if(!this.currentRowExists) throw new RuntimeException("ResultSet is EOF");
		try
			{
			E value=create(this.row);
			this.currentRowIsNext=false;
			return value;
			}
		catch(SQLException sql)
			{
			throw new java.lang.RuntimeException("next():"+sql.getMessage(),sql);
			}
		
		}

	protected abstract E create(ResultSet row) throws SQLException;
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from "+getClass());
		}
	
	};
	

	private abstract class StmtIterator extends ResultSetIterator<Statement>
		{
		private Statement last=null;
		StmtIterator(ResultSet row) throws SQLException { super(row);}
		@Override
		protected Statement create(ResultSet row) throws SQLException {
			return getModel().readStatement(row);
			}
		
		@Override
		public Statement next() {
			this.last= super.next();
			return this.last;
			}
		
		@Override
		public void remove()
			{
			try
				{
				DerbyModel.this.remove(this.last);
				}
			catch(SQLException err)
				{
				throw new RuntimeException("Cannot remove "+this.last,err);
				}
			}
		
		abstract DerbyModel getModel();
		}
	
	/***
	 * 
	 */
	private class SubjectIterator
		extends ResultSetIterator<Resource>
		{
		SubjectIterator(ResultSet row) throws SQLException
			{
			super(row);
			}
		@Override
		protected Resource create(ResultSet row) throws SQLException {
			return readSubject(row);
			}
		}
	
	/**
	 * 
	 * @author pierre
	 *
	 */
	private class ValueExtractor
		implements CloseableIterator<RDFNode>
		{
		private CloseableIterator<Statement> iter;
		ValueExtractor(CloseableIterator<Statement> iter)
			{
			this.iter=iter;
			}
		@Override
		protected void finalize() throws Throwable {
			try {close();}catch(SQLException err) {}
			super.finalize();
			}
		@Override
		public void close() throws SQLException { this.iter.close();}
		@Override
		public boolean hasNext() { return this.iter.hasNext();}
		@Override
		public RDFNode next() { return this.iter.next().getValue();}
		@Override
		public void remove() { this.iter.remove();}
		}
	
	/**
	 * ClassCastIterator
	 */
	private static class ClassCastIterator<X>
		implements CloseableIterator<X>
		{
		CloseableIterator<?> iter;
		private Class<X> clazz;
		ClassCastIterator(CloseableIterator<?> iter,Class<X> clazz) throws SQLException
			{
			this.iter=iter;
			this.clazz=clazz;
			}
		@Override
		protected void finalize() throws Throwable {
			try {close();}catch(SQLException err) {}
			super.finalize();
			}
		@Override
		public void close() throws SQLException { this.iter.close();}
		@Override
		public boolean hasNext() { return this.iter.hasNext();}

		@Override
		public X next() { return this.clazz.cast(iter.next()); }

		@Override
		public void remove() { this.iter.remove();}
		
		}
	
	
	
/**
 * 
 * @author pierre
 *
 */
public abstract class RDFNode
	extends XObject
	{
	protected RDFNode()
		{
		}
	
	public DerbyModel getModel()
		{
		return DerbyModel.this;
		}
	public abstract short getNodeType();
	public boolean isResource() {return getNodeType()==VALUE_IS_RESOURCE;}
	public boolean isLiteral() {return getNodeType()==VALUE_IS_LITERAL;}
	public Resource asResource() { return Resource.class.cast(this);}
	public Literal asLiteral() { return Literal.class.cast(this);}
	
	public CloseableIterator<Statement> listStatementsWithValue() throws SQLException
		{
		return getModel().listStatements(
				null, null, getNodeType(),
				isResource()?asResource().getURI():asLiteral().getString()
				);
		}
	
	public CloseableIterator<Statement> listStatementsWithValue(Resource predicate) throws SQLException
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
public abstract class Resource
	extends RDFNode
	implements Comparable<Resource>
	{
	private String uri;

	protected Resource(String uri)
		{
		this.uri=uri;
		}

    @Override
    public short getNodeType() {
    	return VALUE_IS_RESOURCE;
    	}
	
	private int delim()
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
	
	
	
	public Resource getPropertyAsResource(Resource predicate,Resource defaultValue) throws SQLException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this, predicate, VALUE_IS_RESOURCE,null);
		Resource value=(iter.hasNext()?iter.next().getValue().asResource():defaultValue);
		iter.close();
		return value;
		}
	
	
	public Resource getPropertyAsResource(Resource predicate) throws SQLException
		{
		return getPropertyAsResource(predicate,null);
		}
	
	
	public Literal getPropertyAsLiteral(Resource predicate,Literal defaultValue) throws SQLException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this, predicate, VALUE_IS_LITERAL,null);
		Literal value=(iter.hasNext()?iter.next().getValue().asLiteral():defaultValue);
		iter.close();
		return value;
		}


	public Literal getPropertyAsLiteral(Resource predicate) throws SQLException
		{
		return getPropertyAsLiteral(predicate,null);
		}
	
	
	public Resource setProperty(Resource predicate,RDFNode value) throws SQLException
		{
		removeAll(predicate);
		addProperty(predicate,value);
		return this;
		}
	
	public Resource addProperty(Resource predicate,RDFNode value) throws SQLException
		{
		addStatement(createStatement(this, predicate, value));
		return this;
		}
	
	
	public CloseableIterator<Statement> listProperties() throws SQLException
		{
		return getModel().listStatements(this, null, null, null);
		}
	
	public CloseableIterator<Statement> listProperties(String predicate) throws SQLException
		{
		return listProperties(createResource(predicate));
		}
	
	public CloseableIterator<Statement> listProperties(Resource predicate) throws SQLException
		{
		return getModel().listStatements(this, predicate, null, null);
		}
	
	public CloseableIterator<RDFNode> listValues(Resource predicate) throws SQLException
		{
		return new ValueExtractor(listProperties(predicate));
		}
	
	public CloseableIterator<Literal> listValuesAsLiteral(Resource predicate) throws SQLException
		{
		return new ClassCastIterator<Literal>(new ValueExtractor(
				getModel().listStatements(this, predicate, VALUE_IS_LITERAL, null)	
			),Literal.class);
		}
	
	public CloseableIterator<Resource> listValuesAsResource(Resource predicate) throws SQLException
		{
		return new ClassCastIterator<Resource>(new ValueExtractor(
				getModel().listStatements(this, predicate, VALUE_IS_RESOURCE, null)	
			),Resource.class);
		}
	
	public Statement getProperty(Resource predicate) throws SQLException
		{
		CloseableIterator<Statement> iter= listProperties(predicate);
		Statement value=(iter.hasNext()?iter.next():null);
		iter.close();
		return value;
		}
	
	
	public String getString(Resource predicate,String defaultValue) throws SQLException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this,predicate, DerbyModel.VALUE_IS_LITERAL, null);
		Statement value=(iter.hasNext()?iter.next():null);
		iter.close();
		return value==null?defaultValue:value.getValue().asLiteral().getString();
		}
	
	public String getString(Resource predicate) throws SQLException
		{
		return getString(predicate,null);
		}
	
	
	public Statement getRequiredProperty(Resource predicate) throws SQLException
		{
		Statement stmt= getProperty(predicate);
		if(stmt==null) throw new SQLException("Property not found "+predicate);
		return stmt;
		}
	
	public boolean hasProperty(Resource predicate) throws SQLException
		{
		return getProperty(predicate)!=null;
		}
	
	public boolean hasProperty(Resource predicate,RDFNode value) throws SQLException
		{
		CloseableIterator<Statement> iter= getModel().listStatements(this,predicate , value);
		boolean b= iter.hasNext();
		iter.close();
		return b;
		}
	
	
	public int removeProperties() throws SQLException
		{
		return getModel()._removeStatements(this, null, null, null);
		}
	
	public int removeAll(Resource predicate) throws SQLException
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
public abstract  class Literal
	extends RDFNode
	implements Comparable<Literal>
	{
	private String text;
	protected Literal(String text)
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

public abstract class Statement
	extends XObject
	implements Comparable<Statement>
	{
	private Resource subject;
	private Resource predicate;
	private RDFNode value;
	
	private Statement(
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
	
	public DerbyModel getModel()
		{
		return DerbyModel.this;
		}
	public Resource getSubject() { return this.subject; }
	public Resource getPredicate() { return this.predicate; }
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
	
	public void addToModel() throws SQLException
		{
		getModel().addStatement(this);
		}
	
	public void remove() throws SQLException
		{
		getModel().remove(this);
		}
	
	@Override
	public String toString() {
		return getSubject().toString()+" "+getPredicate()+" "+getValue();
		}
	
	public void printAsN3(PrintWriter out)
		{
		out.print("<"+this.getSubject().getURI()+">\t");
		out.print("<"+this.getPredicate().getURI()+">\t");
		if(this.getValue().isResource())
			{
			out.print("<"+this.getValue().asResource().getURI()+">");
			}
		else
			{
			String s= this.getValue().asLiteral().getString();
			s=s.replaceAll("[\"]", "\\\"");
			out.print("\"<"+s+"\"");
			}
		out.println(".");
		}
	
	private void write(XMLStreamWriter w) throws XMLStreamException
		{
		w.writeStartElement("rdf", "Statement", RDF.NS);
		w.writeCharacters("\n ");
		
		w.writeEmptyElement("rdf", "subject", RDF.NS);
		w.writeAttribute("rdf","resource",RDF.NS,getSubject().getURI());
		w.writeCharacters("\n ");
		
		w.writeEmptyElement("rdf", "predicate", RDF.NS);
		w.writeAttribute("rdf","resource",RDF.NS,getPredicate().getURI());
		w.writeCharacters("\n ");
		
		
		if(getValue().isResource())
			{
			w.writeEmptyElement("rdf", "object", RDF.NS);
			w.writeAttribute("rdf","resource",RDF.NS,getValue().asResource().getURI());
			}
		else
			{
			w.writeStartElement("rdf", "object", RDF.NS);
			w.writeCharacters(getValue().asLiteral().getString());
			w.writeEndElement();
			}
		
		
		w.writeCharacters("\n");
		w.writeEndElement();
		w.writeCharacters("\n");
		}
	
	}


private static final String COLUMN_SUBJECT_URI="subjecturi";
private static final String COLUMN_PREDICATE_URI="predicateuri";
private static final String COLUMN_VALUE_TYPE ="valuetype";
private static final String COLUMN_VALUE ="value";
private static final short VALUE_IS_LITERAL=0;
private static final short VALUE_IS_RESOURCE=1;
private PrefixMapping prefixMapping = new PrefixMapping();
private static final String DEFAULT_SCHEMA_NAME="schema01";
private static final String DEFAULT_GRAPH_NAME="rdf001";
private static final String DEFAULT_PREFIX2URI_NAME="prefix2uri";
private Stack<Connection> connections= new Stack<Connection>();
private File fileName;
private static  long ID_GENERATOR= System.currentTimeMillis();
private static Random RANDOM= new Random(System.currentTimeMillis());
public static final String JDBC_DRIVER_NAME="org.apache.derby.jdbc.EmbeddedDriver";
public DerbyModel(File fileName) throws SQLException
	{
	try {
		Class.forName(JDBC_DRIVER_NAME);
	} catch (ClassNotFoundException e) {
		throw new SQLException("Cannot find "+JDBC_DRIVER_NAME+" in classpath",e);
	}
	this.fileName=fileName;
	if(!fileName.exists())
		{
		Properties dbProperties=new Properties();
		dbProperties.setProperty("user", "anonymous");
		dbProperties.setProperty("password","");
		dbProperties.setProperty("create", "true");	
		Connection con=DriverManager.getConnection("jdbc:derby:"+fileName,dbProperties);
		java.sql.Statement stmt= con.createStatement();
		stmt.executeUpdate("create schema "+DEFAULT_SCHEMA_NAME);
		stmt.executeUpdate("create table "+DEFAULT_SCHEMA_NAME+"."+DEFAULT_GRAPH_NAME+
				"("+
				COLUMN_SUBJECT_URI+" varchar("+getResourceMaxLength()+") not null,"+
				COLUMN_PREDICATE_URI+" varchar("+getResourceMaxLength()+") not null,"+
				COLUMN_VALUE_TYPE+" int  not null,"+
				COLUMN_VALUE+" varchar("+Math.max(getResourceMaxLength(),getLiteralMaxLength())+")  not null,"+
				"CONSTRAINT n3 UNIQUE"+"("+COLUMN_SUBJECT_URI+","+COLUMN_PREDICATE_URI+","+COLUMN_VALUE_TYPE+","+COLUMN_VALUE+")"+
				")");
		stmt.executeUpdate("create index "+
				DEFAULT_SCHEMA_NAME+"."+COLUMN_SUBJECT_URI+"idx "+
				"on "+DEFAULT_SCHEMA_NAME+"."+DEFAULT_GRAPH_NAME+
				"("+COLUMN_SUBJECT_URI+")"
				);
		stmt.executeUpdate("create index "+
				DEFAULT_SCHEMA_NAME+"."+COLUMN_PREDICATE_URI+"idx "+
				"on "+DEFAULT_SCHEMA_NAME+"."+DEFAULT_GRAPH_NAME+
				"("+COLUMN_PREDICATE_URI+")"
				);
		
		stmt.executeUpdate("create table "+DEFAULT_SCHEMA_NAME+"."+DEFAULT_PREFIX2URI_NAME+
				"("+
				"uri varchar("+getResourceMaxLength()+") not null,"+
				"prefix varchar(50) not null,"+
				"CONSTRAINT prefixuniq UNIQUE(prefix),"+
				"CONSTRAINT uriuniq UNIQUE(uri)"+
				")");
		
		con.close();
		}
	}

public int getResourceMaxLength()
	{
	return 255;
	}

public int getLiteralMaxLength()
	{
	return 255;
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

/** closes the connections, disposes the derby drivers */
public void close() throws SQLException
	{
	while(!this.connections.isEmpty())
		{
		this.connections.pop().close();
		}
	Properties dbProperties= new Properties();
	dbProperties.setProperty("user", "anonymous");
	dbProperties.setProperty("password","");
	dbProperties.setProperty("shutdown", "true");
	try
		{
		DriverManager.getConnection("jdbc:derby:"+this.fileName,dbProperties);
		} catch(Exception err) {}
	}

/** @return the internal prefixMapping */
public PrefixMapping getPrefixMapping()
	{
	return this.prefixMapping;
	}

/** @return the number of statements in the model */
public int size() throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select count(*) from "+getTable()
			);
	int n= SQLUtilities.selectOneValue(pstmt.executeQuery(), Number.class).intValue();
	pstmt.close();
	recycleConnection(con);
	return n;
	}
/** @return wether this model is empty */
public boolean isEmpty() throws SQLException
	{
	return size()==0;
	}

/** List all distinct subjects.*/
public CloseableIterator<Resource> listSubjects() throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_SUBJECT_URI+
			" from "+getTable());
	return new ResultSetIterator<Resource>(pstmt.executeQuery())
		{
		@Override
		protected Resource create(ResultSet row) throws SQLException {
			return readSubject(row);
			}
		};
	}

/** List all distinct predicates .*/
public CloseableIterator<Resource> listProperties() throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_PREDICATE_URI+
			" from "+getTable());
	return new ResultSetIterator<Resource>(pstmt.executeQuery())
		{
		@Override
		protected Resource create(ResultSet row) throws SQLException {
			return readPredicate(row);
			}
		};
	}





/** add all the statements in the List */
public DerbyModel add(Collection<Statement> col) throws SQLException
	{
	return add(col.iterator());
	}

/** add all the statements in the iterator */
public DerbyModel add(Iterator<Statement> iter) throws SQLException
	{
	while(iter.hasNext())
		{
		addStatement(iter.next());
		}
	return this;
	}

/** add all the statements from another model */
public DerbyModel add(DerbyModel model) throws SQLException
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
public DerbyModel remove(Collection<Statement> collection) throws SQLException
	{
	return remove(collection.iterator());
	}

/** remove all the statements in the iterator */
public DerbyModel remove(Iterator<Statement> iter) throws SQLException
	{
	while(iter.hasNext())
		{
		remove(iter.next());
		}
	return this;
	}

/** Answer a statement (s, p) from this model. If none exist, return null*/
public Statement getProperty( Resource s, Resource p ) throws SQLException
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
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate) throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_SUBJECT_URI+
			" from "+getTable()+ 
			" where "+COLUMN_PREDICATE_URI+"=?");
	pstmt.setString(1, predicate.getURI());
	return new SubjectIterator(pstmt.executeQuery());
	}

/** List all subjects with a given property and property value*/
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate, RDFNode value) throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_SUBJECT_URI+
			" from "+getTable()+ 
			" where "+COLUMN_PREDICATE_URI+"=? and "+
			COLUMN_VALUE_TYPE+"=? and "+
			COLUMN_VALUE+"=?");
	pstmt.setString(1, predicate.getURI());
	pstmt.setShort(2, value.getNodeType());
	pstmt.setString(3, value.isLiteral()?
				value.asLiteral().getString():
				value.asResource().getURI()
				);
	return new SubjectIterator(pstmt.executeQuery());
	}

/** List all values in a model.*/
public CloseableIterator<RDFNode> listObjects() throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_VALUE_TYPE+","+COLUMN_VALUE+
			" from "+getTable()
			);
	return new ResultSetIterator<RDFNode>(pstmt.executeQuery())
		{
		@Override
		protected RDFNode create(ResultSet row) throws SQLException {
			return readValue(row);
			}
		};
	}

/** List all objects of a given property..*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource predicate) throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_VALUE_TYPE+","+COLUMN_VALUE+
			" from "+getTable()+
			" where "+COLUMN_PREDICATE_URI+"=?"
			);
	pstmt.setString(1,predicate.getURI());
	return new ResultSetIterator<RDFNode>(pstmt.executeQuery())
		{
		@Override
		protected RDFNode create(ResultSet row) throws SQLException {
			return readValue(row);
			}
		};
	}

/** List the values of a property of a resource*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource subject,Resource predicate) throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select distinct "+
			COLUMN_VALUE_TYPE+","+COLUMN_VALUE+
			" from "+getTable()+
			" where "+
			COLUMN_SUBJECT_URI+"=? and "+
			COLUMN_PREDICATE_URI+"=?"
			);
	pstmt.setString(1,subject.getURI());
	pstmt.setString(2,predicate.getURI());
	return new ResultSetIterator<RDFNode>(pstmt.executeQuery())
		{
		@Override
		protected RDFNode create(ResultSet row) throws SQLException {
			return readValue(row);
			}
		};
	}

/** Determine whether this model contains any statements with a given subject*/
public boolean contains(Resource s, Resource p) throws SQLException
	{
	return contains(s, p,null);
	}

/** /** Determine if an (S, P, O) pattern is present in this model, with null a lowed*/
public boolean contains(Resource s, Resource p,RDFNode value) throws SQLException
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
public boolean contains(Statement stmt) throws SQLException
	{
	return contains(stmt.getSubject(),stmt.getPredicate(),stmt.getValue());
	}

/** Determine if a subject is present in this model.*/
public boolean containsSubject(Resource subject) throws SQLException
	{
	CloseableIterator<Statement> iter= listStatements(
			subject, null,null
			);
	boolean found=iter.hasNext();
	iter.close();
	return found;
	}

private Connection getConnection() throws SQLException
	{
	while(!this.connections.isEmpty())
		{
		Connection con=this.connections.pop();
		if(con.isClosed()) continue;
		return con;
		}
	Properties dbProperties= new Properties();
	dbProperties.setProperty("user", "anonymous");
	dbProperties.setProperty("password","");
	return DriverManager.getConnection("jdbc:derby:"+this.fileName,dbProperties);
	}

private void recycleConnection(Connection con) throws SQLException
	{
	if(con.isClosed()) return;
	con.clearWarnings();
	this.connections.push(con);
	}

protected boolean isParanoid()
	{
	return true;
	}

protected void report(SQLWarning w)
	{
	while(w!=null)
		{
		System.err.println(w.getLocalizedMessage());
		w.getNextWarning();
		}
	}

private String getTable()
	{
	return DEFAULT_SCHEMA_NAME+"."+DEFAULT_GRAPH_NAME;
	}

private String createAnonymousURI() throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select count(*) from "+getTable()+" where "+
			COLUMN_SUBJECT_URI+"=?");
	String id=null;
	while(true)
		{
		id="_"+(++ID_GENERATOR)+"_"+RANDOM.nextInt(Short.MAX_VALUE);
		pstmt.setString(1, id);
		if(SQLUtilities.selectOneValue(pstmt.executeQuery(), Number.class).intValue()==0) break;
		}
	pstmt.close();
	recycleConnection(con);
	return id;
	}

private String createAnonymousURI(String template) throws SQLException
	{
	if(template==null || template.trim().length()==0) return createAnonymousURI();
	template=template.trim().replaceAll("[^a-zA-Z0-9_]", "").trim();
	if(template.length()==0) return createAnonymousURI();
	if(!template.startsWith("_")) template="_"+template;
	
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
			"select count(*) from "+getTable()+" where "+
			COLUMN_SUBJECT_URI+"=?");
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
		pstmt.setString(1, id);
		if(SQLUtilities.selectOneValue(pstmt.executeQuery(), Number.class).intValue()==0) break;
		id=null;
		}
	pstmt.close();
	recycleConnection(con);
	if(id==null) return createAnonymousURI();
	return id;
	}


public boolean addStatement(Resource subject,Resource predicate,RDFNode value)    throws SQLException
	{
	return addStatement(createStatement(subject, predicate, value));
	}

public boolean addStatement(Statement stmt)   throws SQLException
	{
	Connection con= getConnection();
	PreparedStatement pstmt= con.prepareStatement(
		"insert into "+getTable()+"("+
		COLUMN_SUBJECT_URI+","+
		COLUMN_PREDICATE_URI+","+
		COLUMN_VALUE_TYPE+","+
		COLUMN_VALUE+") values (?,?,?,?)"
		);
	pstmt.setString(1, stmt.getSubject().getURI());
	pstmt.setString(2, stmt.getPredicate().getURI());
	pstmt.setShort(3, stmt.getValue().getNodeType());
	pstmt.setString(4,
			stmt.getValue().isResource()?
			stmt.getValue().asResource().getURI():
			stmt.getValue().asLiteral().getString()
			);
	int n=0;
	try
		{
		n=pstmt.executeUpdate();
		}
	catch(java.sql.SQLIntegrityConstraintViolationException error)
		{
		n=0;
		}
	catch(java.sql.SQLDataException error)
		{
		n=0;
		}
	report(con.getWarnings());
	recycleConnection(con);
	return n==1;
	}



private Resource readSubject(ResultSet row)  throws SQLException
	{
	return readResource(row,COLUMN_SUBJECT_URI);
	}

private Resource readPredicate(ResultSet row)  throws SQLException
	{
	return readResource(row,COLUMN_PREDICATE_URI);
	}


private RDFNode readValue(ResultSet row)  throws SQLException
	{
	short valueType= row.getShort(COLUMN_VALUE_TYPE);
	switch(valueType)
		{
		case VALUE_IS_LITERAL:return readLiteral(row);
		case VALUE_IS_RESOURCE:return readResource(row,COLUMN_VALUE);
		default: throw new SQLException("unexpected statement readched with "+COLUMN_VALUE_TYPE+"="+valueType);
		}
	}

private Literal readLiteral(ResultSet row)  throws SQLException
	{
	String text= row.getString(COLUMN_VALUE);
	return createLiteral(text);
	}


private Resource readResource(ResultSet row,String columnLabel)  throws SQLException
	{
	String uri= row.getString(columnLabel);
	if(isParanoid())
		{
		try {
			new URI(uri);
		} catch (URISyntaxException e) {
			throw new SQLException("Cannot parse "+uri+" to java.net.URI ",e);
			}
		}
	return createResource(uri);
	}

/** creates an anonymous resource using template as a template for the uri*/
public Resource createAnonymousResource(String template) throws SQLException
	{
	return createResource(createAnonymousURI(template));
	}

/** creates an anonymous resource */
public Resource createResource() throws SQLException
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
public Resource createResource(String uri)
	{
	if(uri==null) throw new NullPointerException("createResource(null)");
	return new Resource(uri)
		{
		@Override
		public DerbyModel getModel() {
			return DerbyModel.this;
			}
		};
	}

/** creates a literal from a text */
public Literal createLiteral(String text)
	{
	if(text==null) throw new NullPointerException("createLiteral(null)");
	return new Literal(text)
		{
		@Override
		public DerbyModel getModel() {
			return DerbyModel.this;
			}
		};
	}


private Statement readStatement(ResultSet row) throws SQLException
	{
	//Long id= row.getLong(COLUMN_ID);
	Resource subject= readSubject(row);
	Resource predicate= readPredicate(row);
	RDFNode value= readValue(row);
	return new Statement(subject,predicate,value)
		{
		@Override
		public DerbyModel getModel() {
			return DerbyModel.this;
			}
		};
	}	

public Statement createStatement( Resource subject,Resource predicate,RDFNode value) throws SQLException
	{
	return new Statement(subject,predicate,value)
		{
		@Override
		public DerbyModel getModel() {
			return DerbyModel.this;
			}
		};
	}

public CloseableIterator<Statement> listStatements() throws SQLException
	{
	return listStatements(null,null,null,null);
	}

public CloseableIterator<Statement> listStatements(
		Resource subject,
		Resource predicate,
		RDFNode value
		) throws SQLException
	{
	return listStatements(subject, predicate,
			value==null?null:value.getNodeType(),
			value==null?null:value.isLiteral()?value.asLiteral().getString():value.asResource().getURI());
	}
		


public CloseableIterator<Statement> listStatements(
	Resource subject,
	Resource predicate,
	Short valueType,
	String value
	) throws SQLException
	{
	Connection con= getConnection();
	StringBuilder query= new StringBuilder(
			"select "+
			COLUMN_SUBJECT_URI+","+
			COLUMN_PREDICATE_URI+","+
			COLUMN_VALUE_TYPE+","+
			COLUMN_VALUE+
			" from "+getTable()
			);
	boolean needWhere=true;
	if(subject!=null)
		{
		needWhere=false;
		query.append(" where ").
			  append(COLUMN_SUBJECT_URI).
			  append("=?");
		}
	
	if(predicate!=null)
		{
		query.append(needWhere?" where ":" and ").
				append(COLUMN_PREDICATE_URI).
				append("=?");
		needWhere=false;
		}
	
	if(valueType!=null)
		{
		query.append(needWhere?" where ":" and ").
			append(COLUMN_VALUE_TYPE).append("=?");
		needWhere=false;
		}
	
	if(value!=null)
		{
		query.append(needWhere?" where ":" and ").
			append(COLUMN_VALUE).append("=?");
		needWhere=false;
		}
	
	PreparedStatement stmt= con.prepareStatement(
			query.toString()
			);
	
	int index=1;
	
	if(subject!=null)
		{
		stmt.setString(index++, subject.getURI());
		}
	
	if(predicate!=null)
		{
		stmt.setString(index++, predicate.getURI());
		}
	
	if(valueType!=null)
		{
		stmt.setShort(index++, valueType);
		}
	
	if(value!=null)
		{
		stmt.setString(index++, value );
		}
	
	return new StmtIterator(stmt.executeQuery())
		{
		@Override
		public DerbyModel getModel() {
			return DerbyModel.this;
			}
		};
	}
/** remove ALL the statements from the RDF store
 * @return number of statements deleted
 * */
public int clear()throws SQLException
	{
	Connection con = getConnection();
	PreparedStatement pstmt= con.prepareStatement("delete from "+getTable());
	int n=pstmt.executeUpdate();
	pstmt.close();
	recycleConnection(con);
	return n;
	}

/** remove the given statement from the RDF store
 * @return true if a statement was deleted
 * */
public boolean remove(Statement stmt)throws SQLException
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

private int _removeStatements(
		Resource subject,
		Resource predicate,
		Short valueType,
		String value
		) throws SQLException
		{
		Connection con= getConnection();
		StringBuilder query= new StringBuilder(
				"delete from "+getTable()
				);
		boolean needWhere=true;
		if(subject!=null)
			{
			needWhere=false;
			query.append(" where ").
				  append(COLUMN_SUBJECT_URI).
				  append("=?");
			}
		
		if(predicate!=null)
			{
			query.append(needWhere?" where ":" and ").
					append(COLUMN_PREDICATE_URI).
					append("=?");
			needWhere=false;
			}
		
		if(valueType!=null)
			{
			query.append(needWhere?" where ":" and ").
				append(COLUMN_VALUE_TYPE).append("=?");
			needWhere=false;
			}
		
		if(value!=null)
			{
			query.append(needWhere?" where ":" and ").
				append(COLUMN_VALUE).append("=?");
			needWhere=false;
			}
		
		PreparedStatement stmt= con.prepareStatement(
				query.toString()
				);
		
		int index=1;
		
		if(subject!=null)
			{
			stmt.setString(index++, subject.getURI());
			}
		
		if(predicate!=null)
			{
			stmt.setString(index++, predicate.getURI());
			}
		
		if(valueType!=null)
			{
			stmt.setShort(index++, valueType);
			}
		
		if(value!=null)
			{
			stmt.setString(index++, value );
			}
		
		int n=stmt.executeUpdate();
		stmt.close();
		recycleConnection(con);
		return n;
		}

public void saveAsN3(File n3file) throws SQLException,IOException
	{
	PrintWriter out= new PrintWriter(new FileWriter(n3file));
	saveAsN3(out);
	out.flush();
	out.close();
	}

public void saveAsN3(PrintWriter out) throws SQLException,IOException
	{
	CloseableIterator<Statement> iter= listStatements();
	while(iter.hasNext())
		{
		Statement stmt= iter.next();
		stmt.printAsN3(out);
		}
	iter.close();
	out.flush();
	}

public void saveAsRDF(File rdfFile) throws XMLStreamException,SQLException,IOException
	{
	FileWriter out= new FileWriter(rdfFile);
	saveAsRDF(out);
	out.flush();
	out.close();
	}

public void saveAsRDF(Writer out) throws XMLStreamException,SQLException
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
		iter.next().write(w);
		}
	iter.close();
	
	w.writeEndElement();
	w.writeEndDocument();
	w.flush();
	}

public static void main(String[] args) {
	try {
		DerbyModel m= new DerbyModel(new File("/home/pierre/jeter.db"));
		
		Resource r1= m.createResource("http://www.test1.com");
		Resource r2= m.createResource("http://www.test2.com");
		Literal r3= m.createLiteral("OK");
		for(int i=0;i< 3;++i) m.addStatement(m.createStatement(r1,m.createResource("http://www.test2.com/"+i),r3));
		CloseableIterator<Statement> iter= m.listStatements();
		
		while(iter.hasNext())
			{
			Statement x= iter.next();
			CloseableIterator<Statement> iter2= x.getSubject().listProperties(r2);
			while(iter2.hasNext())
				{
				System.out.println(" ===> "+iter2.next());
				}
			//x.getSubject().addProperty(g.createResource("http://w2"),g.createLiteral("OK2"));
			System.out.println(x);
			//x.remove();
			}
		iter.close();
		System.out.println("Num statements="+m.size());
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
