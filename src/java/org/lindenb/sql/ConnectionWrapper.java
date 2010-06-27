package org.lindenb.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

/**
 * A wrapper for a connection
 * @author Pierre Lindenbaum
 *
 */
public class ConnectionWrapper
	implements Connection
	{
	private Connection delegate;
	public ConnectionWrapper(Connection delegate)
		{
		this.delegate=delegate;
		}
	
	
	public Connection getDelegate()
		{
		return this.delegate;
		}
	
	@Override
	public void clearWarnings() throws SQLException
		{
		getDelegate().clearWarnings();
		}

	public final void safeClose()
		{
		try
			{
			this.close();
			}
		catch (SQLException e)
			{
			//ignore
			}
		}
	
	@Override
	public void close() throws SQLException
		{
		getDelegate().close();
		}

	@Override
	public void commit() throws SQLException
		{
		getDelegate().commit();
		}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException
		{
		return getDelegate().createArrayOf(typeName, elements);
		}

	@Override
	public Blob createBlob() throws SQLException
		{
		return getDelegate().createBlob();
		}

	@Override
	public Clob createClob() throws SQLException
		{
		return getDelegate().createClob();
		}

	@Override
	public NClob createNClob() throws SQLException
		{
		return getDelegate().createNClob();
		}

	@Override
	public SQLXML createSQLXML() throws SQLException
		{
		return getDelegate().createSQLXML();
		}

	@Override
	public Statement createStatement() throws SQLException
		{
		return getDelegate().createStatement();
		}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
		{
		return getDelegate().createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
		}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException
		{
		return getDelegate().createStatement(resultSetType, resultSetConcurrency);
		}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException
		{
		return getDelegate().createStruct(typeName, attributes);
		}

	@Override
	public boolean getAutoCommit() throws SQLException
		{
		return getDelegate().getAutoCommit();
		}

	@Override
	public String getCatalog() throws SQLException
		{
		return getDelegate().getCatalog();
		}
	
	@Override
	public Properties getClientInfo() throws SQLException
		{
		return getDelegate().getClientInfo();
		}

	@Override
	public String getClientInfo(String name) throws SQLException
		{
		return getDelegate().getClientInfo(name);
		}

	@Override
	public int getHoldability() throws SQLException
		{
		return getDelegate().getHoldability();
		}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException
		{
		return getDelegate().getMetaData();
		}

	@Override
	public int getTransactionIsolation() throws SQLException
		{
		return getDelegate().getTransactionIsolation();
		}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException
		{
		return getDelegate().getTypeMap();
		}

	@Override
	public SQLWarning getWarnings() throws SQLException
		{
		return getDelegate().getWarnings();
		}

	@Override
	public boolean isClosed() throws SQLException
		{
		return getDelegate().isClosed();
		}

	@Override
	public boolean isReadOnly() throws SQLException
		{
		return getDelegate().isReadOnly();
		}

	@Override
	public boolean isValid(int timeout) throws SQLException
		{
		return getDelegate().isValid(timeout);
		}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
		{
		return getDelegate().isWrapperFor(iface);
		}

	@Override
	public String nativeSQL(String sql) throws SQLException
		{
		return getDelegate().nativeSQL(sql);
		}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
		{
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
		}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException
		{
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency);
		}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException
		{
		return getDelegate().prepareCall(sql);
		}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
		{
		return getDelegate().prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
		}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException
		{
		return getDelegate().prepareStatement(sql, resultSetType,
				resultSetConcurrency);
		}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException
		{
		return getDelegate().prepareStatement(sql, autoGeneratedKeys);
		}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException
		{
		return getDelegate().prepareStatement(sql, columnIndexes);
		}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException
		{
		return getDelegate().prepareStatement(sql, columnNames);
		}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException
		{
		return getDelegate().prepareStatement(sql);
		}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException
		{
		getDelegate().releaseSavepoint(savepoint);
		}

	@Override
	public void rollback() throws SQLException
		{
		getDelegate().rollback();
		}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException
		{
		getDelegate().rollback(savepoint);
		}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException
		{
		getDelegate().setAutoCommit(autoCommit);
		}

	@Override
	public void setCatalog(String catalog) throws SQLException
		{
		getDelegate().setCatalog(catalog);
		}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException
		{
		getDelegate().setClientInfo(properties);
		}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException
		{
		getDelegate().setClientInfo(name, value);
		}

	@Override
	public void setHoldability(int holdability) throws SQLException
		{
		getDelegate().setHoldability(holdability);
		}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException
		{
		getDelegate().setReadOnly(readOnly);
		}

	@Override
	public Savepoint setSavepoint() throws SQLException
		{
		return getDelegate().setSavepoint();
		}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException
		{
		return getDelegate().setSavepoint(name);
		}

	@Override
	public void setTransactionIsolation(int level) throws SQLException
		{
		getDelegate().setTransactionIsolation(level);
		}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException
		{
		getDelegate().setTypeMap(map);
		}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
		{
		return getDelegate().unwrap(iface);
		}
	@Override
	public int hashCode()
		{
		return getDelegate().hashCode();
		}
	@Override
	public boolean equals(Object obj)
		{
		return obj==this;
		}
	@Override
	public String toString()
		{
		return getClass().getSimpleName()+":"+getDelegate().toString();
		}
	}
