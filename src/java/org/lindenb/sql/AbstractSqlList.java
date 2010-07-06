package org.lindenb.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.TreeMap;

/**
 * Abstract lightweight List for SQL
 * @author pierre
 *
 * @param <T>
 */
public abstract class AbstractSqlList<T>
	extends AbstractList<T>
	implements RandomAccess
	{
	private int _size=-1;
	private int _index=-1;
	private List<T> buffer=null;
	
	protected abstract Connection getConnection()  throws SQLException;
	protected abstract void releaseConnection(Connection con) throws SQLException;
	protected abstract ObjectSQLFactory<T> getObjectSQLFactory();
	
	protected AbstractSqlList()
		{
		}
	
	protected int getBufferSize()
		{
		return 1000;
		}
	protected abstract String getSelectCountQuery();
	protected abstract String getSelectQuery();
	
	@SuppressWarnings("unchecked")
	protected Map<Integer,Object> getParams()
		{
		return Collections.EMPTY_MAP;
		}
	
	protected String getSelectQuery(int start,int count)
		{
		return getSelectQuery()+" limit "+start+","+count;
		}
	
	protected void fillPreparedStatement(PreparedStatement pstmt) throws SQLException
		{
		for(Integer i0: this.getParams().keySet())
			{
			int i=i0+1;
			Object o= this.getParams().get(i0);
			if(o.getClass()==String.class || o instanceof CharSequence || o.getClass().isEnum())
				{
				pstmt.setString(i, o.toString());
				}
			else if(o.getClass()==Long.class)
				{
				pstmt.setLong(i, Long.class.cast(o));
				}
			else if(o.getClass()==Integer.class)
				{
				pstmt.setInt(i, Integer.class.cast(o));
				}
			else if(o.getClass()==Short.class)
				{
				pstmt.setShort(i, Short.class.cast(o));
				}
			else if(o.getClass()==Byte.class)
				{
				pstmt.setByte(i, Byte.class.cast(o));
				}
			else if(o.getClass()==Float.class)
				{
				pstmt.setFloat(i, Float.class.cast(o));
				}
			else if(o.getClass()==Double.class)
				{
				pstmt.setDouble(i, Double.class.cast(o));
				}
			else
				{
				pstmt.setObject(i, o);
				}
			}
		}
	
	protected void load(int position)
		{
		if( this.buffer!=null &&
			position>= this._index &&
			position< this._index+this.buffer.size()) return;
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet row=null;
		
		try
			{
			con= getConnection();
			pstmt=con.prepareStatement(getSelectQuery(position,getBufferSize()));
			fillPreparedStatement(pstmt);
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
				T object=getObjectSQLFactory().newInstance(row);
				buffer.add(object);
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
				try {releaseConnection(con); }
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
			fillPreparedStatement(pstmt);
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
					try { releaseConnection(con); }
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
	
	public final int getSize()
		{	
		return this.size();
		}
	
	
	}
