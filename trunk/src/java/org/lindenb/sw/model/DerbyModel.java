package org.lindenb.sw.model;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;
import java.util.Stack;
import org.lindenb.sql.SQLUtilities;



/**
 * DerbyModel a RDF store
 * @author pierre
 *
 */
public class DerbyModel extends AbstractRDFModel
{


	
private abstract class ResultSetIterator<E>
	implements CloseableIterator<E>
	{
	private ResultSet row;
	private boolean currentRowIsNext=false;
	private boolean currentRowExists=true;
	protected ResultSetIterator(ResultSet row) throws RDFException
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
	public void close() throws RDFException
		{
		try
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
		catch(SQLException sqlerr)
			{
			throw new RDFException(sqlerr);
			}
		
		
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
			catch(RDFException err)
				{
				throw new java.lang.RuntimeException("next():"+err.getMessage(),err);
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
		catch(RDFException sql)
			{
			throw new java.lang.RuntimeException("next():"+sql.getMessage(),sql);
			}
		
		}

	protected abstract E create(ResultSet row) throws RDFException;
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from "+getClass());
		}
	
	};
	

	private abstract class StmtIterator extends ResultSetIterator<Statement>
		{
		private Statement last=null;
		StmtIterator(ResultSet row) throws RDFException { super(row);}
		@Override
		protected Statement create(ResultSet row) throws RDFException {
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
			catch(RDFException err)
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
		SubjectIterator(ResultSet row) throws RDFException
			{
			super(row);
			}
		@Override
		protected Resource create(ResultSet row) throws RDFException {
			return readSubject(row);
			}
		}
	
	/**
	 * 
	 * @author pierre
	 *
	
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
			try {close();}catch(RDFException err) {}
			super.finalize();
			}
		@Override
		public void close() throws RDFException { this.iter.close();}
		@Override
		public boolean hasNext() { return this.iter.hasNext();}
		@Override
		public RDFNode next() { return this.iter.next().getValue();}
		@Override
		public void remove() { this.iter.remove();}
		}
	 */
	
	/**
	 * ClassCastIterator
	
	private static class ClassCastIterator<X>
		implements CloseableIterator<X>
		{
		CloseableIterator<?> iter;
		private Class<X> clazz;
		ClassCastIterator(CloseableIterator<?> iter,Class<X> clazz) throws RDFException
			{
			this.iter=iter;
			this.clazz=clazz;
			}
		@Override
		protected void finalize() throws Throwable {
			try {close();}catch(RDFException err) {}
			super.finalize();
			}
		@Override
		public void close() throws RDFException { this.iter.close();}
		@Override
		public boolean hasNext() { return this.iter.hasNext();}

		@Override
		public X next() { return this.clazz.cast(iter.next()); }

		@Override
		public void remove() { this.iter.remove();}
		
		}
	 */




private static final String COLUMN_SUBJECT_URI="subjecturi";
private static final String COLUMN_PREDICATE_URI="predicateuri";
private static final String COLUMN_VALUE_TYPE ="valuetype";
private static final String COLUMN_VALUE ="value";

private static final String DEFAULT_SCHEMA_NAME="schema01";
private static final String DEFAULT_GRAPH_NAME="rdf001";
private static final String DEFAULT_PREFIX2URI_NAME="prefix2uri";
private Stack<Connection> connections= new Stack<Connection>();
private File fileName;
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


/** closes the connections, disposes the derby drivers */
public void close() throws RDFException
	{
	while(!this.connections.isEmpty())
		{
		try
			{
			this.connections.pop().close();
			}
		catch(SQLException sqlerr)
			{
			throw new RDFException(sqlerr);
			}
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


/** @return the number of statements in the model */
public int size() throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}
/** @return wether this model is empty */
@Override
public boolean isEmpty() throws RDFException
	{
	return size()==0;
	}

/** List all distinct subjects.*/
public CloseableIterator<Resource> listSubjects() throws RDFException
	{
	try
		{
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(
				"select distinct "+
				COLUMN_SUBJECT_URI+
				" from "+getTable());
		return new ResultSetIterator<Resource>(pstmt.executeQuery())
			{
			@Override
			protected Resource create(ResultSet row) throws RDFException {
				return readSubject(row);
				}
			};
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}

/** List all distinct predicates .*/
public CloseableIterator<Resource> listProperties() throws RDFException
	{
	try
		{
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(
				"select distinct "+
				COLUMN_PREDICATE_URI+
				" from "+getTable());
		return new ResultSetIterator<Resource>(pstmt.executeQuery())
			{
			@Override
			protected Resource create(ResultSet row) throws RDFException {
				return readPredicate(row);
				}
			};
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}




	/** List all subjects with a given property. .remove() is not implemented on
     *  this iterator.
     * @return an iterator over the subjects
     * @param p the property sought.
     */
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate) throws RDFException
	{
	try{
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(
				"select distinct "+
				COLUMN_SUBJECT_URI+
				" from "+getTable()+ 
				" where "+COLUMN_PREDICATE_URI+"=?");
		pstmt.setString(1, predicate.getURI());
		return new SubjectIterator(pstmt.executeQuery());	
		
		
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}

/** List all subjects with a given property and property value*/
public CloseableIterator<Resource> listSubjectsWithProperty(Resource predicate, RDFNode value) throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	
	

	}

/** List all values in a model.*/
public CloseableIterator<RDFNode> listObjects() throws RDFException
	{
	try
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
			protected RDFNode create(ResultSet row) throws RDFException {
				return readValue(row);
				}
			};
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	
	}

/** List all objects of a given property..*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource predicate) throws RDFException
	{
	try
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
			protected RDFNode create(ResultSet row) throws RDFException {
				return readValue(row);
				}
			};
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}	
	
	
	}

/** List the values of a property of a resource*/
public CloseableIterator<RDFNode> listObjectsOfProperty(Resource subject,Resource predicate) throws RDFException
	{
	try
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
			protected RDFNode create(ResultSet row) throws RDFException {
				return readValue(row);
				}
			};
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	
	}


private Connection getConnection() throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}

private void recycleConnection(Connection con) throws RDFException
	{
	try
		{
		if(con.isClosed()) return;
		con.clearWarnings();
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
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

private String createAnonymousURI() throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}	


	}

@Override
protected String createAnonymousURI(String template) throws RDFException
	{
	if(template==null || template.trim().length()==0) return createAnonymousURI();
	template=template.trim().replaceAll("[^a-zA-Z0-9_]", "").trim();
	if(template.length()==0) return createAnonymousURI();
	if(!template.startsWith("_")) template="_"+template;
	
	try
		{
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}


public boolean addStatement(Resource subject,Resource predicate,RDFNode value)    throws RDFException
	{
	return addStatement(createStatement(subject, predicate, value));
	}

public boolean addStatement(Statement stmt)   throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}



private Resource readSubject(ResultSet row)  throws RDFException
	{
	return readResource(row,COLUMN_SUBJECT_URI);
	}

private Resource readPredicate(ResultSet row)  throws RDFException
	{
	return readResource(row,COLUMN_PREDICATE_URI);
	}


private RDFNode readValue(ResultSet row)  throws RDFException
	{
	try
		{
		short valueType= row.getShort(COLUMN_VALUE_TYPE);
		switch(valueType)
			{
			case VALUE_IS_LITERAL:return readLiteral(row);
			case VALUE_IS_RESOURCE:return readResource(row,COLUMN_VALUE);
			default: throw new RDFException("unexpected statement readched with "+COLUMN_VALUE_TYPE+"="+valueType);
			}
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}

	}

private Literal readLiteral(ResultSet row)  throws RDFException
	{
	try
	{
		String text= row.getString(COLUMN_VALUE);
	return createLiteral(text);	
	}
catch(SQLException sqlerr)
	{
	throw new RDFException(sqlerr);
	}

	}


private Resource readResource(ResultSet row,String columnLabel)  throws RDFException
	{
	try
		{
		String uri= row.getString(columnLabel);
		if(isParanoid())
			{
			try {
				new URI(uri);
			} catch (URISyntaxException e) {
				throw new RDFException("Cannot parse "+uri+" to java.net.URI ",e);
				}
			}
		return createResource(uri);	
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}





private Statement readStatement(ResultSet row) throws RDFException
	{
	//Long id= row.getLong(COLUMN_ID);
	Resource subject= readSubject(row);
	Resource predicate= readPredicate(row);
	RDFNode value= readValue(row);
	return createStatement(subject,predicate,value);
	}	


@Override
public CloseableIterator<Statement> listStatements(
	Resource subject,
	Resource predicate,
	Short valueType,
	String value
	) throws RDFException
	{
	try
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
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
		
	
	}
/** remove ALL the statements from the RDF store
 * @return number of statements deleted
 * */
@Override
public int clear()throws RDFException
	{
	try
		{
		Connection con = getConnection();
		PreparedStatement pstmt= con.prepareStatement("delete from "+getTable());
		int n=pstmt.executeUpdate();
		pstmt.close();
		recycleConnection(con);
		return n;		
		}
	catch(SQLException sqlerr)
		{
		throw new RDFException(sqlerr);
		}
	}

@Override
protected int _removeStatements(
		Resource subject,
		Resource predicate,
		Short valueType,
		String value
		) throws RDFException
		{
	try
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
catch(SQLException sqlerr)
	{
	throw new RDFException(sqlerr);
	}
	
	
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
