<?xml version='1.0' encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
          <!ENTITY LT "&amp;lt;">
          <!ENTITY GT "&amp;gt;">
          <!ENTITY AMP "&amp;amp;">
          <!ENTITY true "true">
          <!ENTITY FALSE "false">
          ]>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version='1.0'
	>

<!--
Author:
	Pierre Lindenbaum PHD
WWW:
	http://plindenbaum.blogspot.com
mail:
	plindenbaum@yahoo.fr
Motivation:
	This stylesheet transforms the output of 'desc table *' in mysql+XML
	into a set of Java Files. 

Documentation:
	 http://plindenbaum.blogspot.com/2010/06/so-you-want-to-generate-java-api-for.html

-->
<xsl:output method="text"/>
<xsl:param name="base">generated</xsl:param>
<xsl:param name="package">org.myapi</xsl:param>
<xsl:param name="package-path">org/myapi/</xsl:param>
<xsl:variable name="package-impl-path" select="concat($package-path,'impl/')"/>
<xsl:variable name="package-decl">package <xsl:value-of select="$package"/>;</xsl:variable>
<xsl:variable name="package-impl-decl">package <xsl:value-of select="$package"/>.impl;</xsl:variable>

<xsl:template match="/">&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;archive&gt;
<xsl:apply-templates select="database"/>
&lt;/archive&gt;
</xsl:template>

<xsl:template match="database">
<!--
==============================================================================
README
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/','README')"/>"&gt;

=Compile=

=Install=

Edit ./build.properties and add the path of the mysql jdbc driver library (jar).

Edit ./src/META-INF/connection.properties and edit the parameters for the mysql connection.

&lt;/file&gt;
<!--
==============================================================================
build.xml
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/','build.xml')"/>"&gt;&LT;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&GT;
&LT;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&GT;
&LT;project name="database" default="jar"&GT;
&LT;property name=&quot;src.dir&quot; value=&quot;src&quot;/&GT;
&LT;property name=&quot;build.dir&quot; value=&quot;build&quot;/&GT;
&LT;property name=&quot;dist.dir&quot; value=&quot;dist&quot;/&GT;
&LT;property file="build.properties"/&GT;


 &LT;target name="db"&GT;
	&LT;mkdir dir=&quot;${build.dir}&quot;/&GT;
	
	&LT;copy todir="${build.dir}"&GT;
            &LT;fileset dir="${src.dir}"&GT;
            
            &LT;/fileset&GT;
        &LT;/copy>

	
        &LT;javac srcdir=&quot;${build.dir}&quot;
                destdir=&quot;${build.dir}&quot;
                debug=&quot;${compile.debug}&quot;
		optimize=&quot;${compile.optimize}&quot;
                source=&quot;1.6&quot;
                target=&quot;1.6&quot;&GT;
               
                 &LT;compilerarg value="-Xlint"/&GT;

                 &LT;include name=&quot;**/TestDatabase.java&quot;/&GT;
        &LT;/javac&GT;
 &LT;/target&GT;
 
 &LT;target name="jar" depends="db"&GT;
	&LT;mkdir dir=&quot;${dist.dir}&quot;/&GT;
        &LT;jar destfile=&quot;${dist.dir}/database.jar&quot;
                basedir=&quot;${build.dir}&quot;
                &GT;
        &LT;/jar&GT;
 &LT;/target&GT;
 
  &LT;target name="test" depends="jar"&GT;
	&LT;mkdir dir=&quot;${build.dir}&quot;/&GT;
        &LT;java  classname="<xsl:value-of select="$package"/>.impl.TestDatabase" fork="true"&GT;
        	&LT;classpath path="${dist.dir}/database.jar"/&GT;
		&LT;classpath path="${jdbc.library}"/&GT;
        &LT;/java&GT;
 &LT;/target&GT;
 
 
&LT;/project&GT;
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/','build.properties')" />" overwrite="false"&gt;#path to jdb jar
#for example jdbc.library=/usr/local/lib/mysql-connector-java.jar
jdbc.library=
compile.debug=true
compile.optimize=false
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'CloseableIterator.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * A CloseableIterator.
 */
public interface CloseableIterator&LT;T&GT;
	extends java.util.Iterator&LT;T&GT;
	{
	/** 
	 * Closes the Iterator
	 * Allows the undeerlying implementation to do any cleanup and discoonnect from any source that it may be using.
	 */
	public void close();
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'SQLMetaColumn.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * SQLMetaColumn
 */
public @interface SQLMetaColumn
	{
	String name();
	String type();
	String key();
	String extra();
	String nil();
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'package-info.java')"/>"&gt;
/**
 * Provides the classes necessary to query the database
 *
 */
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>ObjectFactory.java"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * A Factory creating an object of type &LT;T&GT;
 */
public interface ObjectFactory&LT;T&GT;
	{
	public T newInstance();
	}
&lt;/file&gt;
<!--
==============================================================================
DatabaseRecordImpl
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-impl-path,'DatabaseRecordImpl.java')"/>"&gt;
<xsl:value-of select="$package-impl-decl"/>
import <xsl:value-of select="$package"/>.DatabaseRecord;
import <xsl:value-of select="$package"/>.Database;

/**  DatabaseRecordImpl */
public abstract class DatabaseRecordImpl
	implements DatabaseRecord
	{
	/** linked database */
	private transient Database database=null;
	
	protected DatabaseRecordImpl()
		{
			
		}
	
	public void setDatabase(Database database)
		{
		this.database=database;
		}
	
	@Override
	public Database getDatabase()
		{
		return this.database;
		}

	/** escapes a C string */
	protected String escape(String s)
		{
		if(s==null) return null;
		StringBuilder b=new StringBuilder(s.length());
		for(int i=0;i &LT;s.length();++i)
			{
			switch(s.charAt(i))
				{
				case '\'': b.append("\\\'"); break;
				case '\"': b.append("\\\""); break;
				case '\n': b.append("\\n"); break;
				case '\r': b.append("\\r"); break;
				case '\\': b.append("\\\\"); break;
				case '\t': b.append("\\t"); break;
				default: b.append(s.charAt(i)); break;
				}
			}
		return s.toString();
		}
	
	/** quote a string */
	protected String quote(String s)
		{
		if(s==null) return null;
		return "\""+ escape(s)+"\"";
		}

	/** export the result to JSON */
	public abstract void toJSON(java.io.PrintWriter out);

	@Override
	public String toString()
		{
		try
			{
			java.io.StringWriter s= new java.io.StringWriter();
			java.io.PrintWriter w= new java.io.PrintWriter(s);
			toJSON(w);
			w.flush();
			return s.toString();
			}
		catch(Throwable err)
			{
			return String.valueOf(err.getMessage());
			}
		
		}
	}
&lt;/file&gt;
<!--
==============================================================================
DefaultObjectFactory
==============================================================================
-->
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-impl-path"/>DefaultObjectFactory.java"&gt;
<xsl:value-of select="$package-impl-decl"/>
/**
 * DefaultObjectFactory
 */
public class DefaultObjectFactory&LT;T&GT;
	implements ObjectFactory&LT;T&GT;
	{
	private java.lang.reflect.Constructor&LT;T&GT; ctor;
	public DefaultObjectFactory(Class&LT;T&GT; clazz)
		{
		try
			{
			this.ctor=clazz.getConstructor();
			}
		catch(Exception err)
			{
			throw new RuntimeError(err);
			}
		}
	public T newInstance()
		{
		try
			{
			return this.ctor.newInstance();
			}
		catch(Exception err)
			{
			throw new RuntimeError(err);
			}
		}
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>ObjectSQLFactory.java"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * ObjectSQLFactory
 */
public interface ObjectSQLFactory&LT;T&GT;
	{
	/** builds a new object of type &LT;T&GT; from a  java.sql.ResultSet */
	public T newInstance(java.sql.ResultSet row) throws java.sql.SQLException;
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-impl-path"/>DefaultObjectSQLFactory.java"&gt;
<xsl:value-of select="$package-impl-decl"/>
import <xsl:value-of select="$package"/>.ObjectSQLFactory;
/**
 * AbstractObjectSQLFactory
 */
public abstract class AbstractObjectSQLFactory&LT;T&GT;
	implements ObjectSQLFactory&LT;T&GT;
	{
	private ObjectFactory&LT;T&GT; objectFactory=null;
	public ObjectFactory&LT;T&GT; getObjectFactory()
		{
		return this.objectFactory;
		}
		
	public void setObjectFactory(ObjectFactory&LT;T&GT; objectFactory)
		{
		this.objectFactory=objectFactory;
		}	
	public abstract T newInstance(java.sql.ResultSet row) throws java.sql.SQLException;
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'Database.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * Database
 */
public interface Database
	{
	/** open the Database */
	public void open();
	/** close the database , cleanup the resources */
	public void close();
	<xsl:apply-templates select="resultset" mode="database-decl"/>
	
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-impl-path,'DefaultDatabase.java')"/>"&gt;
<xsl:value-of select="$package-impl-decl"/>

/**
 * DefaultDatabase
 */
public class DefaultDatabase
	extends DatabaseImpl
	{
	private java.util.Properties prop=new java.util.Properties();
	@Override
	public void open()
		{
		try
			{
			this.prop=new java.util.Properties();
			java.io.InputStream in=openConfig();
			if(in==null) throw new java.io.IOException("Cannot read "+getResourceConfigName());
			this.prop.loadFromXML(in);
   			in.close();
   			Class.forName(prop.getProperty("jdbc.driver"));
			}
		catch(Throwable err)
			{
			throw new RuntimeException(err);
			}
		}
	
	protected java.io.InputStream openConfig() throws java.io.IOException
		{
		return getClass().getResourceAsStream(getResourceConfigName());
		}
	
	protected String getResourceConfigName()
		{
		return "/META-INF/connection.properties";
		}
	
	@Override
	protected java.sql.Connection createConnection() throws java.sql.SQLException
		{
		return java.sql.DriverManager.getConnection(
			prop.getProperty("jdbc.url"),
			prop.getProperty("jdbc.login"),
			prop.getProperty("jdbc.password")
			);
		}
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/META-INF/connection.properties')" />" overwrite="false"&gt;&LT;?xml version="1.0" encoding="UTF-8"?&GT;
&LT;properties&GT;
 &LT;entry key="jdbc.driver"&GT;com.mysql.jdbc.Driver&LT;/entry&GT;
 &LT;entry key="jdbc.url"&GT;jdbc:mysql://localhost/test&LT;/entry&GT;
 &LT;entry key="jdbc.login"&GT;login&LT;/entry&GT;
 &LT;entry key="jdbc.password"&GT;password&LT;/entry&GT;
&LT;/properties&GT;
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-impl-path,'TestDatabase.java')"/>"&gt;
<xsl:value-of select="$package-impl-decl"/>
import <xsl:value-of select="$package"/>.*;
/**
 * TestDatabase
 */
public class TestDatabase
	extends DefaultDatabase
	{
	public void testAll() throws Exception
		{
		<xsl:apply-templates select="resultset" mode="test"/>
		}
	
	public static void main(String args[])
		{
		TestDatabase db=null;
		try
			{
			db=new TestDatabase();
			db.open();
			db.testAll();
			System.out.println("OK");
			}
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		finally
			{
			if(db!=null) db.close();
			}
		}
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'DatabaseRecord.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * DatabaseRecord
 */
public interface DatabaseRecord
	{
	/** retrieves the Database associated to this record */
	public Database getDatabase();
	/** writes to JSON */
	public void toJSON(java.io.PrintWriter out);
	/** writes to XML */
	public void writeXML(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException;
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-impl-path,'DatabaseImpl.java')"/>"&gt;
<xsl:value-of select="$package-impl-decl"/>
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import <xsl:value-of select="$package"/>.*;

/**
 *  An abstact implementation of Database
 *
 */
public abstract class DatabaseImpl
	implements Database
	{
	/** pool of SQL connections */
	private List&LT;Connection&GT; queue = new ArrayList&LT;Connection&GT;();
	
	/** Iterator impl */
	protected class IterImpl&LT;T&GT;
		implements CloseableIterator&LT;T&GT;
		{
		private Connection con;
		private Statement stmt;
		private ResultSet row;
		private ObjectSQLFactory&LT;T&GT; factory=null;
		private boolean _nextTested=false;
		private boolean _hasNext=false;
		private T _object=null;
		private boolean _closed=false;
		IterImpl(Connection con,Statement stmt,ResultSet row, ObjectSQLFactory&LT;T&GT; factory)
			{
			this.con=con;
			this.stmt=stmt;
			this.row=row;
			this.factory=factory;
			}
		@Override
		public boolean hasNext()
			{
			if(_closed) return false;
			if(_nextTested) return _hasNext;
			if(this.con==null) throw new IllegalStateException();
			_nextTested=true;
			try
				{
				_hasNext = this.row.next();
				}
			catch(java.sql.SQLException err)
				{
				close();
				throw new RuntimeException(err);
				}
			_object=null;
			if(_hasNext)
				{
				try
					{
					this._object = this.factory.newInstance(row);
					}
				catch(SQLException err)
					{
					close();
					throw new RuntimeException(err);
					}
				}
		
			if(!_hasNext)
				{
				close();
				}
			return _hasNext;
			}
		
		@Override
		public T next()
			{
			if(_closed) throw new IllegalStateException("Iterator closed");
			if(!_nextTested)
				{
				if(!hasNext()) throw new IllegalStateException("hasNext==false");
				}
			_nextTested=false;
			_hasNext=false;
		
			T x= _object;
			_object=null;
			return x;
			}
		@Override
		public void remove()
			{
			throw new UnsupportedOperationException("Cannot remove");
			}
			
			
		@Override
		public void close()
			{
			if(_closed) return;
			_closed=true;
			try { if(this.row!=null) this.row.close(); } catch(java.sql.SQLException err) {}
			try { if(this.stmt!=null) this.stmt.close(); } catch(java.sql.SQLException err) {}
			DatabaseImpl.this.recycle(this.con);
			this.stmt=null;
			this.row=null;
			this.con=null;
			}
		}
	
	

	/** recycle the connection 'con' */
	synchronized protected void recycle(Connection con)
		{
		if(con==null) return;
		try
			{
			if(con.isClosed()) return;
			con.clearWarnings();
			}
		catch(SQLException err)
			{
			return;
			}
		this.queue.add(con);
		}
	
	/** creates a new java.sql.Connection */
	protected abstract Connection createConnection() throws SQLException;
	
	protected synchronized  Connection getConnection() throws SQLException
		{
		for(int i=0;i&LT; this.queue.size() ; i++)
			{
			Connection con= queue.get(0);
			queue.remove(0);
			if(con==null)
				{
				continue;
				}
			try
				{
				if(con.isClosed())
					{
					queue.remove(0);
					continue;
					}
				}
			catch(SQLException err)
				{
				//what should we do ?
				continue;
				}
			return con;
			}
		return createConnection();
		}

	/** close this database */
	public synchronized  void close()
		{
		while(!this.queue.isEmpty())
			{
			Connection con= this.queue.remove(0);
			if(con==null) continue;
			try
				{
				con.close();
				}
			catch(SQLException err)
				{
				//continue
				}
			}
		}
	
	<xsl:apply-templates select="resultset" mode="objectsqlfactory"/>
	<xsl:apply-templates select="resultset" mode="database-impl"/>
	
	}

&lt;/file&gt;


<xsl:apply-templates select="resultset" mode="class"/>
<xsl:apply-templates select="resultset" mode="interface"/>
<xsl:apply-templates select="resultset" mode="enum"/>
<xsl:apply-templates select="resultset" mode="set"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="test">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
<xsl:variable name="iter">
	<xsl:value-of select="concat(' _iter_',generate-id(.))"/>
</xsl:variable>
<xsl:variable name="count">
	<xsl:value-of select="concat(' _count_',generate-id(.))"/>
</xsl:variable>
	System.out.println("[LOG] Starting test for <xsl:value-of select="$tableName"/>");
	// retrieves all the <xsl:value-of select="$tableName"/>.
	<xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/> <xsl:value-of select="$iter"/>=<xsl:value-of select="concat('listAll',$tableName,'(0L,3L);')"/>
	while(<xsl:value-of select="$iter"/>.hasNext())
		{
		System.out.println(<xsl:value-of select="$iter"/>.next());
		}
	<xsl:value-of select="$iter"/>.close();
	System.out.println("[LOG] End of test for <xsl:value-of select="$tableName"/>");
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="className">
<xsl:call-template name="titleize">
	<xsl:with-param name="name">
		<xsl:call-template name="java-name">
			<xsl:with-param name="name" select="normalize-space(substring(./@statement,5))"/>
		</xsl:call-template>
	</xsl:with-param>
</xsl:call-template>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="className">
  <xsl:apply-templates select="field[@name='Field']" mode="className"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="field" mode="className">
<xsl:call-template name="titleize">
	<xsl:with-param name="name">
		<xsl:call-template name="java-name">
			<xsl:with-param name="name" select="."/>
		</xsl:call-template>
	</xsl:with-param>
</xsl:call-template>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="database-decl">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
        /** list all the <xsl:value-of select="$tableName"/> */
	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('listAll',$tableName,'()')"/>;
	 /** list all the <xsl:value-of select="$tableName"/> from 'start' for 'count' */
	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('listAll',$tableName,'(long start,long count)')"/>;
  	<xsl:apply-templates select="row" mode="database-decl"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="database-decl">
<xsl:variable name="tableName">
	<xsl:apply-templates select=".." mode="className"/>
</xsl:variable>
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="argType">
  <xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:variable name="funName">
  <xsl:apply-templates select="." mode="funName"/>
</xsl:variable>

<xsl:choose>
  <xsl:when test="field[@name='Key']='PRI' or field[@name='Key']='UNI'">
  	/** returns a <xsl:value-of select="$tableName"/> for a primary key. */
  	public <xsl:value-of select="$tableName"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'By',$funName)"/>(<xsl:value-of select="concat($argType,' ',$fieldName)"/>);
  </xsl:when>
  <xsl:when test="field[@name='Key']='MUL'">
  	/** returns all the <xsl:value-of select="$tableName"/> for a given <xsl:value-of select="$fieldName"/>. */
  	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'sBy',$funName)"/>(<xsl:value-of select="concat($argType,' ',$fieldName)"/>);
  </xsl:when>
  <xsl:otherwise></xsl:otherwise>
 </xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->

<xsl:template match="resultset" mode="database-impl">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
	/**
         * retrieves all the <xsl:value-of select="$tableName"/>.
         */
	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('listAll',$tableName,'(long start,long count)')"/>
		{
		ObjectSQLFactory&LT;<xsl:value-of select="$tableName"/>&GT; factory=get<xsl:value-of select="$tableName"/>ObjectSQLFactory();
  		Connection con=null;
  		try
  			{
  			con = getConnection();
  			String sql="select * from <xsl:value-of select="normalize-space(substring(./@statement,5))"/> ";
  			if(!(start==0L &AMP;&AMP; count==Long.MAX_VALUE))
  				{
  				sql +=" limit "+start+","+count;
  				}
  			
  			PreparedStatement pstmt=con.prepareStatement(sql);
  			ResultSet row=pstmt.executeQuery();
  			return new IterImpl&LT;<xsl:value-of select="$tableName"/>&GT;(con,pstmt,row,factory);
  			}
  		catch(java.sql.SQLException err)
  			{
  			recycle(con);
  			throw new RuntimeException(err);
  			}
		}
	/**
         * retrieves all the <xsl:value-of select="$tableName"/>.
         */
	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('listAll',$tableName,'()')"/>
		{
		return <xsl:value-of select="concat('listAll',$tableName,'(0L,Long.MAX_VALUE)')"/>;
		}
	<xsl:apply-templates select="row" mode="database-impl"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="database-impl">
<xsl:variable name="tableName">
	<xsl:apply-templates select=".." mode="className"/>
</xsl:variable>
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="javaType">
	<xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:variable name="funName">
  <xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:variable name="jdbcSetter">
	<xsl:apply-templates select="." mode="jdbcSet"/>
</xsl:variable>
<xsl:variable name="type" select="field[@name='Type']"/>

<xsl:choose>
  <xsl:when test="field[@name='Key']='PRI' or field[@name='Key']='UNI'">
  	<xsl:variable name="key" select="field[@name='Key']"/>
  	/**
  	 * 
  	 * returns a  <xsl:value-of select="$tableName"/> from <xsl:value-of select="$fieldName"/>
  	 */
  	@Override
  	public <xsl:value-of select="$tableName"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'By',$funName)"/>(<xsl:value-of select="concat($javaType,' ',$fieldName)"/>)
  		{
  		ObjectSQLFactory&LT;<xsl:value-of select="$tableName"/>&GT; factory=null;//TODO
  		Connection con=null;
  		try
  			{
  			con = getConnection();
  			PreparedStatement pstmt=con.prepareStatement(
  				"select * from <xsl:value-of select="normalize-space(substring(../@statement,5))"/> where <xsl:value-of select="field[@name='Field']"/> =?"
  				);
  			pstmt.<xsl:value-of select="$jdbcSetter"/>(1,<xsl:choose>
  				<xsl:when test="starts-with($type,'set(')">
  					<xsl:apply-templates select="." mode="enumName"/>.unparseValues(<xsl:value-of select="$fieldName"/>)
  				</xsl:when>
  				<xsl:when test="starts-with($type,'enum(')">
  					String.valueOf(<xsl:value-of select="$fieldName"/>)
  				</xsl:when>
  				<xsl:otherwise>
  					<xsl:value-of select="$fieldName"/>
  				</xsl:otherwise>
  				</xsl:choose>);
  			ResultSet row=pstmt.executeQuery();
  			
  			<xsl:value-of select="$tableName"/> object=null;
  			while(row.next())
  				{
  				if(object==null) throw new SQLException("expected only one value");
  				object=factory.newInstance(row);
  				}
  			row.close();
  			pstmt.close();
  			return object;
  			}
  		catch(java.sql.SQLException err)
  			{
  			throw new RuntimeException(err);
  			}
  		finally
  			{
  			recycle(con);
  			}
  		}
  </xsl:when>
  <xsl:when test="field[@name='Key']='MUL'">
  	/**
  	 *  returns a  <xsl:value-of select="$tableName"/> from <xsl:value-of select="$fieldName"/>
  	 */
  	@Override
  	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'sBy',$funName)"/>(<xsl:value-of select="concat($javaType,' ',$fieldName)"/>)
  		{
		ObjectSQLFactory&LT;<xsl:value-of select="$tableName"/>&GT; factory=null;//TODO
  		Connection con=null;
  		try
  			{
  			con = getConnection();
  			PreparedStatement pstmt=con.prepareStatement(
  				"select * from <xsl:value-of select="normalize-space(substring(../@statement,5))"/> where <xsl:value-of select="field[@name='Field']"/> =?"
  				);
  			pstmt.<xsl:value-of select="$jdbcSetter"/>(1,<xsl:choose>
  				<xsl:when test="starts-with($type,'set(')">
  					<xsl:apply-templates select="." mode="enumName"/>.unparseValues(<xsl:value-of select="$fieldName"/>)
  				</xsl:when>
  				<xsl:when test="starts-with($type,'enum(')">
  					String.valueOf(<xsl:value-of select="$fieldName"/>)
  				</xsl:when>
  				<xsl:otherwise>
  					<xsl:value-of select="$fieldName"/>
  				</xsl:otherwise>
  				</xsl:choose>);
  			ResultSet row=pstmt.executeQuery();
  			return new IterImpl&LT;<xsl:value-of select="$tableName"/>&GT;(con,pstmt,row,factory);
  			}
  		catch(SQLException err)
  			{
  			recycle(con);
  			throw new RuntimeException(err);
  			}
  		}
  </xsl:when>
  <xsl:otherwise></xsl:otherwise>
 </xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="interface">

<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,$tableName,'.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * Interface <xsl:value-of select="$tableName"/>
 */
public interface <xsl:value-of select="$tableName"/>
	extends DatabaseRecord
	{
	<xsl:apply-templates name="row" mode="interface"/>
	}
&lt;/file&gt; 
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="enum">
<xsl:apply-templates name="row" mode="enum"/>
</xsl:template>
<xsl:template match="resultset" mode="set">
<xsl:apply-templates name="row" mode="set"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="objectfactory">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
public class <xsl:value-of select="$tableName"/>InstanceFactory
	extends DefaultObjectFactory&lt;<xsl:value-of select="$tableName"/>&gt;
	{
	public <xsl:value-of select="$tableName"/>InstanceFactory()
		{
		super.setObjectFactory(new DefaultObjectFactory&lt;<xsl:value-of select="$tableName"/>&gt;(<xsl:value-of select="concat($tableName,'Impl')"/>.class));
		}
	public T newInstance(java.sql.ResultSet row) throws java.sql.SQLException
		{
		
		}

	}
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="objectsqlfactory">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
<xsl:variable name="impl">
	<xsl:value-of select="concat($tableName,'Impl')"/>
</xsl:variable>
<xsl:variable name="generic">
	<xsl:value-of select="concat('&LT;',$impl,'&GT;')"/>
</xsl:variable>
<xsl:variable name="objectFactory">
	<xsl:value-of select="concat('_',$tableName,'Factory')"/>
</xsl:variable>
<xsl:variable name="objectSQLFactory">
	<xsl:value-of select="concat('_',$tableName,'SQLFactory')"/>
</xsl:variable>
		/** Default InstanceFactory for <xsl:value-of select="$tableName" /> */
		private <xsl:value-of select="concat('ObjectFactory&LT;',$tableName,'&GT; ',$objectFactory,'=null;')"/>
		
		
		
		/** get InstanceFactory for <xsl:value-of select="$tableName" /> */
		protected  <xsl:value-of select="concat('ObjectFactory&LT;',$tableName,'&GT; ')"/> <xsl:value-of select="concat('get',$tableName,'ObjectFactory()')"/>
			{
			if(<xsl:value-of select="$objectFactory"/>==null)
				{
				<xsl:value-of select="$objectFactory"/>=new <xsl:value-of select="concat('ObjectFactory&LT;',$tableName,'&GT;')"/>()
					{
					@Override
					public <xsl:value-of select="$tableName"/> newInstance()
						{
						<xsl:value-of select="$impl"/> object= new <xsl:value-of select="$impl"/>();
						object.setDatabase(DatabaseImpl.this);
						return object;
						}
					};
				}
			return <xsl:value-of select="$objectFactory"/>;
			}
		

		
		/** Default InstanceSQLFactory for <xsl:value-of select="$tableName" /> */
		private <xsl:value-of select="concat('ObjectSQLFactory&LT;',$tableName,'&GT; ',$objectSQLFactory,'=null;')"/>

		/** SQLFactory for <xsl:value-of select="$tableName" /> */
		protected  <xsl:value-of select="concat('ObjectSQLFactory&LT;',$tableName,'&GT; ')"/> 	<xsl:value-of select="concat('get',$tableName,'ObjectSQLFactory()')"/>
			{
			if(<xsl:value-of select="$objectSQLFactory"/>==null)
				{
				<xsl:value-of select="$objectSQLFactory"/>=new <xsl:value-of select="concat('ObjectSQLFactory&LT;',$tableName,'&GT;')"/>()
					{
					@Override
					public <xsl:value-of select="$impl"/> newInstance(java.sql.ResultSet row) throws java.sql.SQLException
						{
						<xsl:value-of select="$impl"/> object= <xsl:value-of select="$impl"/>.class.cast(<xsl:value-of select="concat('get',$tableName,'ObjectFactory().newInstance()')"/>);
						<xsl:apply-templates select="row" mode="objectsqlfactory"/>
						return object;
						}
					};
				}
			return <xsl:value-of select="$objectSQLFactory"/>;
			}
		
		
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="objectsqlfactory">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
<xsl:variable name="setter">
	<xsl:apply-templates select="." mode="setter"/>
</xsl:variable>
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:variable name="sqlName" select="field[@name='Field']"/>
<xsl:variable name="jdbcgetter">
	<xsl:apply-templates select="." mode="jdbcgetter"/>
</xsl:variable>
<xsl:variable name="is_pointer">
	<xsl:apply-templates select="." mode="is_pointer"/>
</xsl:variable>
<xsl:variable name="javaType">
	<xsl:apply-templates select="." mode="javaType"/>
</xsl:variable>
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>


<xsl:choose>
<xsl:when test="starts-with($type,'enum(')">
	<xsl:variable name="enumName">
		<xsl:apply-templates select="." mode="enumName"/>
	</xsl:variable>
	String <xsl:value-of select="concat('_',$fieldName)"/>= row.getString("<xsl:value-of select="$sqlName"/>");
	object.<xsl:value-of select="$setter"/>(<xsl:value-of select="concat('_',$fieldName)"/>==null?null:<xsl:value-of select="$enumName"/>.valueOf(<xsl:value-of select="concat('_',$fieldName)"/>));
</xsl:when>
<xsl:when test="starts-with($type,'set(')">
	<xsl:variable name="enumName">
		<xsl:apply-templates select="." mode="enumName"/>
	</xsl:variable>
	String <xsl:value-of select="concat('_',$fieldName)"/>= row.getString("<xsl:value-of select="$sqlName"/>");
	object.<xsl:value-of select="$setter"/>(<xsl:value-of select="concat('_',$fieldName)"/>==null?null:<xsl:value-of select="$enumName"/>.parseValues(<xsl:value-of select="concat('_',$fieldName)"/>));
</xsl:when>
<xsl:when test="$is_pointer='true' and not(contains($javaType,'.') or $javaType='String')">
<xsl:value-of select="$javaType"/>  <xsl:value-of select="concat(' _',$fieldName)"/>=row.<xsl:value-of select="$jdbcgetter"/>("<xsl:value-of select="$sqlName"/>");
if(row.wasNull()) <xsl:value-of select="concat('_',$fieldName)"/>=null;
object.<xsl:value-of select="$setter"/>(<xsl:value-of select="concat('_',$fieldName)"/>);
</xsl:when>
<xsl:otherwise>
object.<xsl:value-of select="$setter"/>(row.<xsl:value-of select="$jdbcgetter"/>("<xsl:value-of select="$sqlName"/>"));
</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="jdbcSet">
<xsl:variable name="primitive">
	<xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="$primitive='long'">
		<xsl:text>setLong</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='int'">
		<xsl:text>setInt</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='short'">
		<xsl:text>setShort</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='byte'">
		<xsl:text>setByte</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='float'">
		<xsl:text>setFloat</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='double'">
		<xsl:text>setDouble</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='String'">
		<xsl:text>setString</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='java.sql.Timestamp'">
		<xsl:text>setTimestamp</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='java.sql.Date'">
		<xsl:text>setDate</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='byte[]'">
		<xsl:text>setBytes</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'enum(') or starts-with($type,'set(')">
		<xsl:text>setString</xsl:text>
	</xsl:when>
	
	<xsl:otherwise>
		<xsl:message terminate="yes">
		jdbcgetter unknown field type "<xsl:value-of select="$primitive"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>


</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="z">

</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="enum">
<xsl:if test="starts-with(field[@name='Type'],'enum(')">


<xsl:variable name="funName">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:variable name="enumName">
	<xsl:apply-templates select="." mode="enumName"/>
</xsl:variable>

&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="concat($package-path,$enumName,'.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * Enum for <xsl:value-of select="$enumName"/>
 *
 */
public enum <xsl:value-of select="$enumName"/>
	{
	<xsl:value-of select='translate(substring(field[@name="Type"],6,string-length(field[@name="Type"])-7),"&apos;"," ")'/>
	}
&lt;/file&gt;
</xsl:if>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="set">
<xsl:if test="starts-with(field[@name='Type'],'set(')">


<xsl:variable name="funName">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:variable name="enumName">
	<xsl:apply-templates select="." mode="enumName"/>
</xsl:variable>

&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="concat($package-path,$enumName,'.java')"/>"&gt;
<xsl:value-of select="$package-decl"/>
/**
 * enum for <xsl:value-of select="$enumName"/>
 *
 */
public enum <xsl:value-of select="$enumName"/>
	{
	<xsl:value-of select='translate(substring(field[@name="Type"],5,string-length(field[@name="Type"])-6),"&apos;"," ")'/>;
	public static java.util.Set&LT;<xsl:value-of select="$enumName"/>&GT; parseValues(String s)
		{
		java.util.Set&LT;<xsl:value-of select="$enumName"/>&GT; set=new java.util.HashSet&LT;<xsl:value-of select="$enumName"/>&GT;();
		for(String sub:s.split("[,]"))
			{
			sub=sub.trim();
			if(sub.startsWith("'") &AMP;&AMP; sub.endsWith("'"))
				{
				sub=sub.substring(1,sub.length()-1);
				}
			if(sub.isEmpty()) continue;
			set.add(<xsl:value-of select="$enumName"/>.valueOf(sub));
			}
		return set;
		}
	public static String unparseValues(java.util.Set&LT;<xsl:value-of select="$enumName"/>&GT; set)
		{
		StringBuilder b=new StringBuilder();
		for(<xsl:value-of select="$enumName"/> o:set)
			{
			if(b.length()!=0) b.append(",");
			b.append("'");
			b.append(o.toString());
			b.append("'");
			}
		return b.toString();
		}
	}
&lt;/file&gt;
</xsl:if>
</xsl:template>



<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="class">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
<xsl:variable name="impl">
	<xsl:value-of select="concat($tableName,'Impl')"/>
</xsl:variable>
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-impl-path,$impl,'.java')"/>"&gt;
<xsl:value-of select="$package-impl-decl"/>;

<xsl:apply-templates select="." mode="import"/>;

/**
 * <xsl:value-of select="concat($tableName,'Impl')"/>
 *
 */
class <xsl:value-of select="concat($tableName,'Impl')"/>
	extends DatabaseRecordImpl
	implements <xsl:value-of select="$tableName"/>
	{
<xsl:apply-templates name="row" mode="field"/>
	public <xsl:value-of select="concat($tableName,'Impl')"/>()
		{
		}
	
	<xsl:apply-templates name="row" mode="setget"/>

	@Override
	public boolean equals(Object o)
		{
		if(o==this) return true;
		if(this==null || o.getClass()!=this.getClass()) return false;
		<xsl:value-of select="concat($tableName,'Impl')"/> other=<xsl:value-of select="concat($tableName,'Impl.class.cast(o)')"/>;
		if(getDatabase()!=other.getDatabase()) return false;
		<xsl:choose>
			<xsl:when test="row/field[@name='Key']='PRI'">
				/* just compare the primary key */
				<xsl:apply-templates select="row[field[@name='Key']='PRI']" mode="equals"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="row" mode="equals"/>
			</xsl:otherwise>
		</xsl:choose>
		return true;
		}
	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		<xsl:choose>
			<xsl:when test="row/field[@name='Key']='PRI'">
				/* just use the hash for primary key */
				<xsl:apply-templates select="row[field[@name='Key']='PRI']" mode="hashCode"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="row" mode="hashCode"/>
			</xsl:otherwise>
		</xsl:choose>
		return result;
		}
	
	/** prints this <xsl:value-of select="$tableName"/> to XML */
	public void writeXML(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException
		{
		out.writeStartElement("<xsl:value-of select="$tableName"/>");
		<xsl:apply-templates select="row" mode="xml"/>
		out.writeEndElement();
		}

	/** prints this <xsl:value-of select="$tableName"/> to JSON */
	@Override
	public void toJSON(java.io.PrintWriter out)
		{
		out.print("{\"_class\":");
		out.print(quote("<xsl:value-of select="$tableName"/>"));
		<xsl:apply-templates select="row" mode="toJSON"/>
		out.print("}");
		}
	}
&lt;/file&gt;
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="import">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>
import <xsl:value-of select="concat($package,'.',$tableName)"/>;
<xsl:apply-templates select="row" mode="import"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="import">
<xsl:if test="starts-with(field[@name='Type'],'enum(') or starts-with(field[@name='Type'],'set(') ">
<xsl:variable name="name">
	<xsl:apply-templates select="." mode="enumName"/>
</xsl:variable>
import <xsl:value-of select="concat($package,'.',$name)"/>;
</xsl:if>
</xsl:template>

<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="field">
<xsl:variable name="javaType">
	<xsl:apply-templates select="." mode="javaType"/>
</xsl:variable>
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:text>	/** defined as </xsl:text>
<xsl:value-of select="concat(field[@name='Field'],' ',field[@name='Type'],' NULL=',field[@name='Null'])"/>
<xsl:text> */
	private </xsl:text>
<xsl:value-of select="$javaType"/>
<xsl:text>	</xsl:text>
<xsl:value-of select="$fieldName"/>
<xsl:if test="field[@name='Null']='YES'">=null</xsl:if>
<xsl:text>;
</xsl:text>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="sql">
<xsl:variable name="javaType">
  <xsl:apply-templates select="." mode="javaType"/>
</xsl:variable>
<xsl:variable name="jdbcFun">
<xsl:call-template name="guessJDBCFun">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="fieldName">
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="funName">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:choose>
<xsl:when test="starts-with(field[@name='Type'],'enum(')">
<xsl:variable name="tableName">
	<xsl:apply-templates select="$node/.." mode="className"/>
</xsl:variable>
			String	<xsl:text> </xsl:text><xsl:value-of select="generate-id()"/>=row.getString(&quot;<xsl:value-of select="field[@name='Field']"/>&quot;);
			if(<xsl:value-of select="generate-id()"/>!=null)
				{
				object.set<xsl:value-of select="$funName"/>(<xsl:value-of select="concat($tableName,$fieldName)"/>.parse(<xsl:value-of select="generate-id()"/>);
				}
</xsl:when>
<xsl:when test="$javaType!='String' and field[@name='Null']='YES'">
				<xsl:value-of select="$javaType"/><xsl:text> </xsl:text><xsl:value-of select="generate-id()"/>=row.<xsl:value-of select="$jdbcFun"/>(&quot;<xsl:value-of select="field[@name='Field']"/>&quot;);
				if(row.wasNull()) <xsl:value-of select="generate-id()"/>=null;
				object.set<xsl:value-of select="$funName"/>(<xsl:value-of select="generate-id()"/>);
</xsl:when>
<xsl:otherwise>			object.set<xsl:value-of select="$funName"/>(row.<xsl:value-of select="$jdbcFun"/>(&quot;<xsl:value-of select="field[@name='Field']"/>&quot;));
</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="interface">
<xsl:variable name="javaType">
	<xsl:apply-templates select="." mode="javaType"/>
</xsl:variable>
<xsl:variable name="getter">
	<xsl:apply-templates select="." mode="getter"/>
</xsl:variable>		/** get the value for <xsl:apply-templates select="." mode="fieldName"/> */
	<xsl:apply-templates select="row" mode="meta"/>
	public <xsl:value-of select="$javaType"/><xsl:value-of select="concat(' ',$getter,'()')"/>;
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="meta">
	@SQLMetaColumn(name="<xsl:value-of select="field[@name='Field']"/>",type="<xsl:value-of select="field[@name='Type']"/>",key="<xsl:value-of select="field[@name='Key']"/>",extra="<xsl:value-of select="field[@name='Extra']"/>",nil="<xsl:value-of select="field[@name='Null']"/>")
</xsl:template>

<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="setget">
<xsl:variable name="javaType">
	<xsl:apply-templates select="." mode="javaType"/>
</xsl:variable>
<xsl:variable name="fieldName">
  <xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="funName">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:variable name="getter">
	<xsl:apply-templates select="." mode="getter"/>
</xsl:variable>
<xsl:variable name="setter">
	<xsl:apply-templates select="." mode="setter"/>
</xsl:variable>

	/** set the value of <xsl:value-of select="$fieldName"/> */
	public void <xsl:value-of select="$setter"/>(<xsl:value-of select="$javaType"/><xsl:text> </xsl:text><xsl:value-of select="$fieldName"/>)
		{
		this.<xsl:value-of select="$fieldName"/>=<xsl:value-of select="$fieldName"/>;
		}
	
	/** get the value of <xsl:value-of select="$fieldName"/> */
	public <xsl:value-of select="$javaType"/><xsl:text> </xsl:text><xsl:value-of select="$getter"/>()
		{
		return this.<xsl:value-of select="$fieldName"/>;
		}

</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="equals">
<xsl:variable name="getter">
	<xsl:apply-templates select="." mode="getter"/>
</xsl:variable>
<xsl:variable name="is_pointer">
	<xsl:apply-templates select="." mode="is_pointer"/>
</xsl:variable>
<xsl:choose>
	<xsl:when test="$is_pointer='true'">
		if(<xsl:value-of select="concat('this.',$getter,'()')"/>==null)
			{
			if(<xsl:value-of select="concat('this.',$getter,'()')"/>!=null)
				{
				return false;
				}
			}
		else
			{
			return <xsl:value-of select="concat('this.',$getter,'().equals(other.',$getter,'());')"/>
			}
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="concat('if(this.',$getter,'()!= other.',$getter,'()) return false;')"/>
	</xsl:otherwise>
</xsl:choose>
<xsl:text>;
</xsl:text>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="hashCode">
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="is_pointer">
	<xsl:apply-templates select="." mode="is_pointer"/>
</xsl:variable>
<xsl:variable name="is_number">
	<xsl:apply-templates select="." mode="is_number"/>
</xsl:variable>
<xsl:variable name="primitive">
	<xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:variable name="sqlType" select="field[@name='Type']"/>
<xsl:variable name="null" select="field[@name='Null']"/>
<xsl:text>result = prime * result + </xsl:text>
<xsl:choose>
	<xsl:when test="$is_pointer='true'">
		(this.<xsl:value-of select="$fieldName"/>==null?0:this.<xsl:value-of select="$fieldName"/>.hashCode());
	</xsl:when>
	<xsl:when test="$primitive='int'">
		<xsl:value-of select="concat('this.',$fieldName)"/>
	</xsl:when>
	<xsl:when test="$primitive='long'">
		(int)(<xsl:value-of select="concat('this.',$fieldName)"/>^(<xsl:value-of select="concat('this.',$fieldName)"/>&GT;&GT;&GT;32))
	</xsl:when>
	
	<xsl:when test="$is_number='true'">
		(int)<xsl:value-of select="concat('this.',$fieldName)"/>
	</xsl:when>
	
	<xsl:otherwise>
		<xsl:message terminate="no">
		'hashCode' unknown field type "<xsl:value-of select="$sqlType"/>" <xsl:value-of select="$is_number"/>
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
<xsl:text>;
</xsl:text>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="xml">
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="getter">
	<xsl:apply-templates select="." mode="getter"/>
</xsl:variable>
<xsl:variable name="sqlType" select="field[@name='Type']"/>
<xsl:variable name="null" select="field[@name='Null']"/>
<xsl:choose>
	<xsl:when test="$null='YES'">
		if(<xsl:value-of select="concat($fieldName,'==null')"/>)
			{
			out.writeEmptyElement("<xsl:value-of select="$fieldName"/>");
			}
		else
			{
			<xsl:choose>
			<xsl:when test="starts-with($sqlType,'set(')">
				<xsl:variable name="var" select="concat('v',generate-id(.))"/>
				<xsl:variable name="setName">
					<xsl:apply-templates select="." mode="enumName"/>
				</xsl:variable>
		
				for(<xsl:value-of select="concat($setName,' ',$var,':',$getter,'()')"/>)
					{
					out.writeCharacters(String.valueOf(String.valueOf(<xsl:value-of select="$var"/>)));
					}
			</xsl:when>
			<xsl:otherwise>
			out.writeStartElement("<xsl:value-of select="$fieldName"/>");
			out.writeCharacters(String.valueOf(<xsl:value-of select="concat($getter,'()')"/>));
			out.writeEndElement();
			</xsl:otherwise>
			</xsl:choose>
			}
	</xsl:when>
	<xsl:otherwise>
		out.writeStartElement("<xsl:value-of select="$fieldName"/>");
		out.writeCharacters(String.valueOf(<xsl:value-of select="concat($getter,'()')"/>));
		out.writeEndElement();
	</xsl:otherwise>
</xsl:choose>

</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="toJSON">
<xsl:for-each select="row">
<xsl:variable name="is_blob">
	<xsl:apply-templates select="." mode="is_blob"/>
</xsl:variable>
<xsl:if test="$is_blob='false'">
	<xsl:apply-templates select="." mode="toJSON"/>
</xsl:if>
</xsl:for-each>
</xsl:template>

<xsl:template match="row" mode="toJSON">
<xsl:variable name="fieldName">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:variable>
<xsl:variable name="getter">
	<xsl:apply-templates select="." mode="getter"/>
</xsl:variable>
<xsl:variable name="sqlType" select="field[@name='Type']"/>
<xsl:variable name="null" select="field[@name='Null']"/>
<xsl:variable name="is_number">
	<xsl:apply-templates select="." mode="is_number"/>
</xsl:variable>
<xsl:variable name="is_pointer">
	<xsl:apply-templates select="." mode="is_pointer"/>
</xsl:variable>
out.print(",");
out.print(quote("<xsl:value-of select="$fieldName"/>"));
out.print(":");
<xsl:if test="$is_pointer='true'">
if(<xsl:value-of select="concat($getter,'()')"/>==null)
	{
	out.print("null");
	}
else
	{
</xsl:if>
<xsl:choose>
	<xsl:when test="$is_number='true'">
		out.print(String.valueOf(<xsl:value-of select="concat($getter,'()')"/>));
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'set(')">
		<xsl:variable name="found" select="concat('f',generate-id(.))"/>
		<xsl:variable name="var" select="concat('v',generate-id(.))"/>
		<xsl:variable name="setName">
			<xsl:apply-templates select="." mode="enumName"/>
		</xsl:variable>
		boolean <xsl:value-of select="$found"/>=false;
		out.print("[");
		for(<xsl:value-of select="concat($setName,' ',$var,':',$getter,'()')"/>)
			{
			if(<xsl:value-of select="$found"/>) out.print(",");
			<xsl:value-of select="$found"/>=true;
			out.print(quote(String.valueOf(<xsl:value-of select="$var"/>)));
			} 
		out.print("]");
	</xsl:when>
	<xsl:otherwise>
		out.print(quote(String.valueOf(<xsl:value-of select="concat($getter,'()')"/>)));
	</xsl:otherwise>
</xsl:choose>
<xsl:if test="$is_pointer='true'">
	}
</xsl:if>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="field" mode="enum">
	<xsl:apply-templates name="enumItem">
		<xsl:with-param name="s" select="."/>
		<xsl:with-param name="delim"></xsl:with-param>
	</xsl:apply-templates>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="enumItem">
<xsl:param name="s"/>
<xsl:param name="delim"/>
<xsl:message><xsl:value-of select="concat('[',$s,']')"/></xsl:message>
<xsl:choose>
<xsl:when test="$s=')'">
</xsl:when>
<xsl:when test="starts-with($s,'enum(')">
	<xsl:call-template name="enumItem">
		<xsl:with-param name="s" select="substring($s,6)"/>
		<xsl:with-param name="delim"></xsl:with-param>
	</xsl:call-template>
</xsl:when>
<xsl:when test='starts-with($s,"&apos;")'>
	<xsl:call-template name="enumItem">
		<xsl:with-param name="s" select="substring($s,2)"/>
		<xsl:with-param name="delim"></xsl:with-param>
	</xsl:call-template>
</xsl:when>
<xsl:when test='starts-with($s,",")'>
	<xsl:call-template name="enumItem">
		<xsl:with-param name="s" select="substring($s,2)"/>
		<xsl:with-param name="delim"></xsl:with-param>
	</xsl:call-template>
</xsl:when>
<xsl:when test='contains($s,"&apos;")'>
	<xsl:call-template name="enumItem">
		<xsl:with-param name="s" select='substring-before($s,"&apos;")'/>
		<xsl:with-param name="delim"></xsl:with-param>
	</xsl:call-template>
	<xsl:call-template name="enumItem">
		<xsl:with-param name="s" select='substring-after($s,"&apos;")'/>
		<xsl:with-param name="delim"><xsl:text>,ssss
		</xsl:text></xsl:with-param>
	</xsl:call-template>	
</xsl:when>
<xsl:otherwise>
	<xsl:value-of select="$delim"/>
	<xsl:value-of select="$s"/>
</xsl:otherwise>
<xsl:choose>

</xsl:choose>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="javaType">
<xsl:variable name="null" select="field[@name='Null']"/>
<xsl:variable name="primitive">
	<xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:choose>
	<xsl:when test="$null='NO' or contains($primitive,'.')">
		<xsl:value-of select="$primitive"/>
	</xsl:when>
	<xsl:when test="$primitive='int'">
		<xsl:text>Integer</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="titleize">
			<xsl:with-param name="name" select="$primitive"/>
		</xsl:call-template>
	</xsl:otherwise>
</xsl:choose>

</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="primitive">
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:variable name="is_string">
	<xsl:apply-templates select="." mode="is_string"/>
</xsl:variable>
<xsl:variable name="is_blob">
	<xsl:apply-templates select="." mode="is_blob"/>
</xsl:variable>
<xsl:variable name="is_unsigned" select="contains($type,' unsigned')"/>

<xsl:choose>
	<xsl:when test="$is_string='true'">
		<xsl:text>String</xsl:text>
	</xsl:when>
	<xsl:when test="$is_unsigned='true' and starts-with($type,'bigint(')">
		<xsl:text>java.math.BigInteger</xsl:text>
	</xsl:when>
	<xsl:when test="$is_unsigned='true' and (starts-with($type,'int(') or starts-with($type,'integer(') or starts-with($type,'mediumint('))">
		<xsl:text>long</xsl:text>
	</xsl:when>
	<xsl:when test="$type='bit(1)'">
		<xsl:text>boolean</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'bit(')">
		<xsl:text>byte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'int(') or starts-with($type,'integer(') or starts-with($type,'tinyint(') or starts-with($type,'smallint(')">
		<xsl:text>int</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'mediumint(') or starts-with($type,'bigint(')">
		<xsl:text>long</xsl:text>
	</xsl:when>
	
	<xsl:when test="$type='float'">
		<xsl:text>float</xsl:text>
	</xsl:when>
	<xsl:when test="$type='double'">
		<xsl:text>double</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'enum(')">
		<xsl:apply-templates select="." mode="enumName"/>
	</xsl:when>
	<xsl:when test="starts-with($type,'set(')">
		<xsl:text>java.util.Set&LT;</xsl:text>
		<xsl:apply-templates select="." mode="enumName"/>
		<xsl:text>&GT;</xsl:text>
	</xsl:when>
	<xsl:when test="$type='datetime' or $type='timestamp'">
		<xsl:text>java.sql.Timestamp</xsl:text>
	</xsl:when>
	<xsl:when test="$type='date'">
		<xsl:text>java.sql.Date</xsl:text>
	</xsl:when>
	<xsl:when test="$is_blob='true'">
		<xsl:text>byte[]</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">
		primitive unknown field type "<xsl:value-of select="$type"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="jdbcgetter">
<xsl:variable name="primitive">
	<xsl:apply-templates select="." mode="primitive"/>
</xsl:variable>
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="$primitive='long'">
		<xsl:text>getLong</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='int'">
		<xsl:text>getInt</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='short'">
		<xsl:text>getShort</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='byte'">
		<xsl:text>getByte</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='float'">
		<xsl:text>getFloat</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='double'">
		<xsl:text>getDouble</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='String'">
		<xsl:text>getString</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='java.sql.Timestamp'">
		<xsl:text>getTimestamp</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='java.sql.Date'">
		<xsl:text>getDate</xsl:text>
	</xsl:when>
	<xsl:when test="$primitive='byte[]'">
		<xsl:text>getBytes</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($type,'enum(') or starts-with($type,'set(')">
		<xsl:text>getString</xsl:text>
	</xsl:when>
	
	<xsl:otherwise>
		<xsl:message terminate="yes">
		jdbcgetter unknown field type "<xsl:value-of select="$primitive"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->

<xsl:template name="guessName">
<xsl:param name="node"/>
<xsl:variable name="sqlName" select="$node/field[@name='Field']"/>
<xsl:call-template name="java-name">
	<xsl:with-param name="name" select="$sqlName"/>
</xsl:call-template>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="java-name">
<xsl:param name="name"/>
<xsl:call-template name="underscore">
	<xsl:with-param name="name" select="$name"/>
</xsl:call-template>
</xsl:template>

<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="setter">
<xsl:variable name="name">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:value-of select="concat('set',$name)"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="getter">
<xsl:variable name="name">
	<xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:value-of select="concat('get',$name)"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="funName">
	<xsl:apply-templates select="field[@name='Field']" mode="funName"/>
</xsl:template>

<xsl:template match="field" mode="funName">
<xsl:call-template name="titleize">
<xsl:with-param name="name">
	<xsl:apply-templates select="." mode="fieldName"/>
</xsl:with-param>
</xsl:call-template>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="is_number">
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="$type='float' or $type='double' or starts-with($type,'int(') or starts-with($type,'smallint(')  or starts-with($type,'tinyint(')  or starts-with($type,'mediumint(')">
		<xsl:value-of select="true()"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="false()"/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="is_string">
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="$type='text' or $type='mediumtext'  or $type='tinytext'  or $type='longtext'  or  starts-with($type,'varchar(')">
		<xsl:value-of select="true()"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="false()"/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="is_blob">
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="$type='blob' or $type='mediumblob'  or $type='tinyblob'  or $type='longblob'">
		<xsl:value-of select="true()"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="false()"/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="is_pointer">
<xsl:variable name="null" select="field[@name='Null']"/>
<xsl:variable name="type" select="field[@name='Type']"/>
<xsl:variable name="is_string">
	<xsl:apply-templates select="." mode="is_string"/>
</xsl:variable>
<xsl:variable name="is_number">
	<xsl:apply-templates select="." mode="is_number"/>
</xsl:variable>
<xsl:choose>
	<xsl:when test="$null='YES' or $is_string='true' or $type='datetime' or starts-with($type,'enum(') or starts-with($type,'set(') ">
		<xsl:value-of select="true()"/>
	</xsl:when>
	<xsl:when test="$is_number">
		<xsl:value-of select="false()"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate='yes'>is_pointer : <xsl:value-of select="$type"/>?</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="fieldName">
	<xsl:apply-templates select="field[@name='Field']" mode="fieldName"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="field[@name='Field']" mode="fieldName">
<xsl:call-template name="java-name">
	<xsl:with-param name="name" select="."/>
</xsl:call-template>
</xsl:template>

<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="enumName">
<xsl:variable name="tableName">
  <xsl:apply-templates select=".." mode="className"/>
</xsl:variable>
<xsl:variable name="name">
  <xsl:apply-templates select="." mode="funName"/>
</xsl:variable>
<xsl:value-of select="concat($tableName,$name)"/>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="underscore">
<xsl:param name="name"/>
<xsl:choose>
	<xsl:when test="contains($name,'_')">
		<xsl:value-of select="substring-before($name,'_')"/>
		<xsl:call-template name="titleize">
			<xsl:with-param name="name">
				<xsl:call-template name="underscore">
					<xsl:with-param name="name" select="substring-after($name,'_')"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:value-of select="$name"/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!--
==============================================================================
==============================================================================
-->
<xsl:template name="titleize">
<xsl:param name="name"/><xsl:value-of select="translate(substring($name,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:value-of select="substring($name,2)"/></xsl:template>


</xsl:stylesheet>
