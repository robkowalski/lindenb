<?xml version='1.0' encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
          <!ENTITY LT "&amp;lt;">
          <!ENTITY GT "&amp;gt;">
          <!ENTITY AMP "&amp;amp;">
          ]>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version='1.0'
	>
<xsl:param name="package">test</xsl:param>
<!--

This stylesheet transforms the output of 'desc table *' in mysql+XML
into an POJO Java File

-->
<xsl:output method="text"/>
<xsl:param name="base">generated</xsl:param>
<xsl:param name="package"></xsl:param>
<xsl:param name="package-path"></xsl:param>
<xsl:param name="package-decl"></xsl:param>

<xsl:template match="/">
&lt;?xml version="version" encoding="UTF-8"?&gt;
&lt;archive&gt;
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'ObjectFactory.java')"/>&gt;
<xsl:value-of select="package-decl"/>
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

&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>ObjectFactory.java"&gt;
<xsl:value-of select="package-decl"/>
public interface ObjectFactory&LT;T&GT;
	{
	public T newInstance();
	}
&lt;/file&gt;
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>DefaultObjectFactory.java"&gt;
public class DefaultObjectFactory&LT;T&GT;
	implements ObjectFactory&LT;T&GT;
	{
	private java.lang.reflect.Constructor&LT;T&GT; ctor;
	public DefaultObjectFactory(Class&LT;T&GT; clazz)
		{
		this.ctor=clazz.getConstructor();
		}
	public T newInstance()
		{
		return this.ctor.newInstance();
		}
	}
&lt;/file&gt;
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>ObjectSQLFactory.java"&gt;
public interface ObjectSQLFactory&lt;T&gt;
	{
	public T newInstance(java.sql.ResultSet row) throws java.sql.SQLException;
	}
&lt;/file&gt;
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="$package-path"/>DefaultObjectSQLFactory.java"&gt;
public abstract class AbstractObjectSQLFactory&lt;T&gt;
	implements ObjectSQLFactory&lt;T&gt;
	{
	private ObjectFactory&lt;T&gt; objectFactory=null;
	public ObjectFactory&lt;T&gt; getObjectFactory()
		{
		return this.objectFactory;
		}
		
	public void setObjectFactory(ObjectFactory&lt;T&gt; objectFactory)
		{
		this.objectFactory=objectFactory;
		}	
	public abstract T newInstance(java.sql.ResultSet row) throws java.sql.SQLException;
	}
&lt;/file&gt;
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'Database.java')"/>&gt;
<xsl:value-of select="package-decl"/>
public interface Database
	{
	<xsl:apply-templates select="resultset" mode="database-decl"/>
	public void close();
	}
&lt;/file&gt;
<!--
==============================================================================
==============================================================================
==============================================================================
-->
&lt;file path="<xsl:value-of select="concat($base,'/src/',$package-path,'DatabaseImpl.java')"/>&gt;
<xsl:value-of select="package-decl"/>
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * DatabaseImpl
 *
 */
public abstract class DatabaseImpl
	implements Database
	{
	private List&LT;Connection&GT; list = new ArrayList&LT;Connection&GT;();
	
	/** Iterator impl */
	protected class IterImpl&LT;T&GT;
		implements Iterator&LT;T&GT;
		{
		private Connection con;
		private ResultSet row;
		private ObjectSQLFactory&LT;T&GT; factory;
		private boolean _nextTested=false;
		private boolean _hasNext=false;
		private T _object=null;
		IterImpl(Connection con,ResultSet row, ObjectSQLFactory&LT;T&GT; factory)
			{
			this.con=con;
			this.row=row;
			this.factory=factory;
			}
		@Override
		public boolean hasNext()
			{
			if(_nextTested) return _hasNext;
			if(this.con==null) throw new IllegalStateException();
			_nextTested=true;
			_hasNext = this.row.next();
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
			if(!_nextTested)
				{
				if(!hasNext()) throw new IllegalStateException();
				}
			_nextTested=false;
			_hasNext=false;
		
			T x= _object;
			_object=null;
			return x;
			}
		
		public void close()
			{
			DatabaseImpl.this.recycle(this.con);
			this.con=null;
			}
		}
	
	synchronized protected void recycle(Connection con)
		{
		if(con==null) return;
		try
			{
			if(con.isClosed()) return;
			}
		catch(SQLException err)
			{
			return;
			}
		this.list.add(con);
		}
		
	protected abstract Connection createConnection() throws SQLException;
	
	protected synchronized  Connection getConnection() throws SQLException
		{
		for(int i=0;i&LT; this.list.size() ; i++)
			{
			Connection con= list.get(0);
			if(con==null)
				{
				list.remove(0);
				continue;
				}
			try
				{
				if(con.isClosed())
					{
					list.remove(0);
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
			catch(SQLException err)q
				{
				return;
				}
			}
		}
	
	/** retrieves one */
	protected &LT;T&GT; one(
		ResultSet row,
		ObjectSQLFactory&LT;T&GT; factory
		) throws SQLException
		{
		T object=null;
		while(row.next())
			{
			if(object!=null) throw new RuntimeException("expected only one value");
			object = factory.newInstance(row);
			}
		return object;
		}
		
	/** retrieve an iterator */
	protected CloseableIterator&LT;T&GT; many(
		Connection con,
		ResultSet row,
		ObjectSQLFactory&LT;T&GT; factory
		) throws SQLException
		{
		return new IterImpl&LT;T&GT;(con,row,factory);
		}
	
	<xsl:apply-templates select="resultset" mode="objectsqlfactory"/>
	<xsl:apply-templates select="resultset" mode="database-impl"/>
	}
&lt;/file&gt;


<xsl:apply-templates select="resultset" mode="objectsqlfactory"/>
<xsl:apply-templates select="resultset" mode="class"/>
<xsl:apply-templates select="resultset" mode="interface"/>
<xsl:apply-templates select="resultset" mode="enum"/>
&lt;/archive&gt;
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
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="javaType">
<xsl:call-template name="guessPrimitive">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
	<xsl:variable name="funName">
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
</xsl:variable>
<xsl:choose>
  <xsl:when test="field[@name='Key']='PRI' or field[@name='Key']='UNI'">
  	public <xsl:value-of select="$tableName"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'By',$funName)"/>(<xsl:value-of select="concat($javaType,' ',$fieldName)"/>);
  </xsl:when>
  <xsl:when test="field[@name='Key']='MUL'">
  	public <xsl:value-of select="concat('CloseableIterator&LT;',$tableName,'&GT;')"/><xsl:text> </xsl:text> <xsl:value-of select="concat('get',$tableName,'sBy',$funName)"/>(<xsl:value-of select="concat($javaType,' ',$fieldName)"/>);
  </xsl:when>
  <xsl:otherwise>
  </xsl:otherwise>
 </xsl:choose>
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
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="javaType">
<xsl:call-template name="guessPrimitive">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="funName">
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="jdbcFun">
<xsl:call-template name="guessJDBCSetter">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<!--
	private ObjectSQLFactory<xsl:value-of select="concat('&LT;',$tableName,'&GT;')"/> <xsl:value-of select="concat('_',$tableName,SQLFactory(),'= null')"/>;
	protected ObjectSQLFactory<xsl:value-of select="concat('&LT;',$tableName,'&GT;')"/> get<xsl:value-of select="concat($tableName,SQLFactory())"/>
		{
		return _;
		} -->
<xsl:choose>
  <xsl:when test="field[@name='Key']='PRI' or field[@name='Key']='UNI'">
  	/**
  	 * 
  	 *
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
  				"select * from <xsl:value-of select="$tableName"/> where <xsl:value-of select="field[@name='Field']"/> =?"
  				);
  			pstmt.<xsl:value-of select="$jdbcFun"/>(1,<xsl:value-of select="$fieldName"/>);
  			ResultSet row=pstmt.executeQuery();
  			
  			<xsl:value-of select="$tableName"/> object=null;
  			while(row.next())
  				{
  				if(object==null) throw new SQLException("expected only one value");
  				object=factory.newInstance(row);
  				}
  			row.close();
  			pstmt.close();
  			}
  		catch(SQLException err)
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
  	 * 
  	 *
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
  				"select * from <xsl:value-of select="$tableName"/> where <xsl:value-of select="field[@name='Field']"/> =?"
  				);
  			pstmt.<xsl:value-of select="$jdbcFun"/>(1,<xsl:value-of select="$fieldName"/>);
  			ResultSet row=pstmt.executeQuery();
  			return new IterImpl(con,pstmt,row,factory);
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
</xsl:variable>public interface <xsl:value-of select="$tableName"/>
	{
	<xsl:apply-templates name="row" mode="interface"/>
	}
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="resultset" mode="enum">
<xsl:apply-templates name="row" mode="enum"/>
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
		/** Default InstanceSQLFactory for <xsl:value-of select="$tableName" /> */
		

		/** SQLFactory for <xsl:value-of select="$tableName" /> */
		private <xsl:value-of select="concat('ObjectSQLFactory&LT;',$tableName,'&GT;	_',$tableName,'SQLFactory = null;')"/>

		protected 

		public class <xsl:value-of select="$tableName"/>InstanceSQLFactory
			extends DefaultObjectFactory&LT;<xsl:value-of select="$tableName"/>&GT;
			{
			public <xsl:value-of select="$tableName"/>InstanceFactory()
				{
				super.set(<xsl:value-of select="concat($tableName,'Impl')"/>.class);
				}
			
			}
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="enum">
<xsl:if test="starts-with(field[@name='Type'],'enum(')">
<xsl:variable name="tableName">
<xsl:apply-templates select=".." mode="className"/>
</xsl:variable>
<xsl:variable name="fieldName">
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="funName">
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
</xsl:variable>
&lt;file path="<xsl:value-of select="$base"/>/src/<xsl:value-of select="concat($package-path,$tableName,$funName,'.java')"/>"&gt;
public enum <xsl:value-of select="concat($tableName,$funName)"/>
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
<xsl:template match="resultset" mode="class">
<xsl:variable name="tableName">
	<xsl:apply-templates select="." mode="className"/>
</xsl:variable>

<xsl:text>public class </xsl:text>
<xsl:value-of select="concat($tableName,'Impl')"/>
	implements <xsl:value-of select="$tableName"/>
	{
<xsl:apply-templates name="row" mode="field"/>
	public <xsl:value-of select="$tableName"/>()
		{
		}
<xsl:apply-templates name="row" mode="setget"/>
	}
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="field">
<xsl:variable name="javaType">
<xsl:call-template name="guessType">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="fieldName">
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
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
<xsl:call-template name="guessType">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
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
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
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
<xsl:call-template name="guessType">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="fieldName">
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="funName">
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
</xsl:variable>	/** get the value for <xsl:value-of select="$fieldName"/> */
	public <xsl:value-of select="$javaType"/><xsl:text> </xsl:text>get<xsl:value-of select="$funName"/>();</xsl:template>


<!--
==============================================================================
==============================================================================
-->
<xsl:template match="row" mode="setget">
<xsl:variable name="javaType">
<xsl:call-template name="guessType">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="fieldName">
<xsl:call-template name="guessName">
  <xsl:with-param name="node" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:variable name="funName">
<xsl:call-template name="guessFunName">
  <xsl:with-param name="name" select="$fieldName"/>
</xsl:call-template>
</xsl:variable>
	/** set the value of <xsl:value-of select="$fieldName"/> */
	public void set<xsl:value-of select="$funName"/>(<xsl:value-of select="$javaType"/><xsl:text> </xsl:text><xsl:value-of select="$fieldName"/>)
		{
		this.<xsl:value-of select="$fieldName"/>=<xsl:value-of select="$fieldName"/>;
		}
	
	/** get the value of <xsl:value-of select="$fieldName"/> */
	public <xsl:value-of select="$javaType"/><xsl:text> </xsl:text>get<xsl:value-of select="$funName"/>()
		{
		return this.<xsl:value-of select="$fieldName"/>;
		}

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
<xsl:template name="guessType">
<xsl:param name="node"/>
<xsl:variable name="sqlType" select="$node/field[@name='Type']"/>
<xsl:variable name="null" select="$node/field[@name='Null']"/>
<xsl:choose>
	<xsl:when test="starts-with($sqlType,'int(') and $null='NO'">
		<xsl:text>int</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'int(') and $null='YES'">
		<xsl:text>Integer</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'smallint(') and $null='NO'">
		<xsl:text>short</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'smallint(') and $null='YES'">
		<xsl:text>Short</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'tinyint(') and $null='NO'">
		<xsl:text>byte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'tinyint(') and $null='YES'">
		<xsl:text>Byte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'enum(') and $null='YES'">
		<xsl:variable name="tableName">
			<xsl:apply-templates select="$node/.." mode="className"/>
		</xsl:variable>
		<xsl:variable name="fieldName">
			<xsl:apply-templates select="$node" mode="className"/>
		</xsl:variable>
		
		<xsl:value-of select="concat($tableName,$fieldName)"/>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'varchar(') or $sqlType='text'">
		<xsl:text>String</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">
		unknown field type "<xsl:value-of select="$sqlType"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="guessPrimitive">
<xsl:param name="node"/>
<xsl:variable name="sqlType" select="$node/field[@name='Type']"/>
<xsl:choose>
	<xsl:when test="starts-with($sqlType,'int(')">
		<xsl:text>int</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'smallint(')">
		<xsl:text>short</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'tinyint(')">
		<xsl:text>byte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'enum(')">
		<xsl:variable name="tableName">
			<xsl:apply-templates select="$node/.." mode="className"/>
		</xsl:variable>
		<xsl:variable name="fieldName">
			<xsl:apply-templates select="$node" mode="className"/>
		</xsl:variable>
		
		<xsl:value-of select="concat($tableName,$fieldName)"/>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'varchar(') or $sqlType='text'">
		<xsl:text>String</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">
		unknown field type "<xsl:value-of select="$sqlType"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="guessJDBCFun">
<xsl:param name="node"/>
<xsl:variable name="sqlType" select="$node/field[@name='Type']"/>
<xsl:variable name="null" select="$node/field[@name='Null']"/>
<xsl:choose>
	<xsl:when test="starts-with($sqlType,'int(')">
		<xsl:text>getInt</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'smallint(')">
		<xsl:text>getShort</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'tinyint(')">
		<xsl:text>getByte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'enum(')">
		<xsl:text>getString</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'varchar(') or $sqlType='text'">
		<xsl:text>getString</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">
		unknown field type "<xsl:value-of select="$sqlType"/>"
		</xsl:message>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!--
==============================================================================
==============================================================================
-->
<xsl:template name="guessJDBCSetter">
<xsl:param name="node"/>
<xsl:variable name="sqlType" select="$node/field[@name='Type']"/>
<xsl:variable name="null" select="$node/field[@name='Null']"/>
<xsl:choose>
	<xsl:when test="starts-with($sqlType,'int(')">
		<xsl:text>setInt</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'smallint(')">
		<xsl:text>setShort</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'tinyint(')">
		<xsl:text>setByte</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'enum(')">
		<xsl:text>setString</xsl:text>
	</xsl:when>
	<xsl:when test="starts-with($sqlType,'varchar(') or $sqlType='text'">
		<xsl:text>setString</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">
		unknown field type "<xsl:value-of select="$sqlType"/>"
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
<xsl:template name="guessFunName">
<xsl:param name="name"/>

<xsl:call-template name="titleize">
	<xsl:with-param name="name">
		<xsl:call-template name="java-name">
			<xsl:with-param name="name" select="$name"/>
		</xsl:call-template>
	</xsl:with-param>
</xsl:call-template>
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