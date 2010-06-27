package org.lindenb.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Abstract lightweight List for SQL
 * @author pierre
 *
 * @param <T>
 */
public abstract class AbstractSqlList<T>
	extends AbstractList<T>
	{
	private int _size=-1;
	private int _index=-1;
	private List<T> buffer=null;
	public abstract DataSource getDataSource();
	public abstract ObjectSQLFactory<T> getObjectSQLFactory();
	
	protected Connection getConnection() throws SQLException
		{
		return getDataSource().getConnection();
		}
	protected int getBufferSize()
		{
		return 1000;
		}
	protected abstract String getSelectCountQuery();
	protected abstract String getSelectQuery(int start,int count);
	
	protected void load(int position)
		{
		if( position>= this._index &&
			position< this._index+this.buffer.size()) return;
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet row=null;
		
		try
			{
			con= getConnection();
			pstmt=con.prepareStatement(getSelectQuery(position,getBufferSize()));
			row=pstmt.executeQuery();
			
			if(this.buffer==null)
				{
				this.buffer=new ArrayList<T>(getBufferSize());
				}
			else
				{
				this.buffer.clear();
				}
			
			while(row.next())
				{
				buffer.add(getObjectSQLFactory().newInstance(row));
				}
			this._index=position;
			}
		catch (Exception err)
			{
			this.buffer.clear();
			this._index=-1;
			throw new RuntimeException(err);
			}
		finally
			{
			if(row!=null)
				{
				try { row.close(); }
				catch(SQLException err) {}
				}
			if(pstmt!=null)
				{
				try { pstmt.close(); }
				catch(SQLException err) {}
				}
			if(con!=null)
				{
				try { con.close(); }
				catch(SQLException err) {}
				}
			
			}
		}	
	
	protected int evalSize()
		{
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet row=null;
		try
			{
			con= getConnection();
			pstmt=con.prepareStatement(getSelectCountQuery());
			row=pstmt.executeQuery();
			while(row.next())
				{
				this._size= row.getInt(1);
				break;
				}
			}
		catch (Exception err)
			{
			throw new RuntimeException(err);
			}
		finally
			{
			if(row!=null)
				{
				try { row.close(); }
				catch(SQLException err) {}
				}
			if(pstmt!=null)
				{
				try { pstmt.close(); }
				catch(SQLException err) {}
				}
			if(con!=null)
					{
					try { con.close(); }
					catch(SQLException err) {}
					}
			
			}
		return this._size;
		}
	
	@Override
	public T get(int position)
		{
		load(position);
		return this.buffer.get(position-this._index);
		}

	@Override
	public int size()
		{
		if(_size<0)
			{
			_size = evalSize();
			}
		return _size;
		}
	
	

	}
