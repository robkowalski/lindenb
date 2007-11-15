package org.lindenb.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;


/**
 * @author lindenb
 *
 * various static Methods for SQL
 */
public class SQLUtilities
    {
    /**
     * escape a SQL string
     * @param s a word to convert
     * @return the converted string
     */
    static public String escape(CharSequence s)
        {
    	if(s==null) return null;
        int needed=-1;
        for(int i=0;i< s.length();++i)
	        {
	        switch(s.charAt(i))
	            {
	            case('\"'): 
	            case('\''): 
	            case('\n'): 
	            case('\t'):
	            case('\\'): needed=i; break;
	            default: break;
	            }
	        if(needed!=-1) break;
	        }
        if(needed==-1) return s.toString();
        
        StringBuffer buffer=new StringBuffer(s.subSequence(0,needed));
        
        for(int i=needed;i< s.length();++i)
            {
            switch(s.charAt(i))
                {
                case('\"'): buffer.append("\\\"");break;
                case('\''): buffer.append("\\\'");break;
                case('\n'): buffer.append("\\n");break;
                case('\t'): buffer.append("\\t");break;
                case('\\'): buffer.append("\\\\");break;
                default: buffer.append(s.charAt(i)); break;
                }
            }
        return buffer.toString();
        }

    static public String quote(CharSequence s)
        {
        if(s==null) return null;
        return "\""+s+"\"";
        }

    static public String escapeAndQuote(CharSequence s)
        {
        if(s==null) return null;
        return quote(escape(s));
        }

    static public String escapeAndQuote(CharSequence s[])
	    {
	    if(s==null || s.length==0) return null;
	    StringBuilder buffer= new StringBuilder();
	    for(int i=0;i< s.length;++i)
	    	{
	        if(i>0) buffer.append(",");
	        buffer.append(s[i]==null?"NULL":escapeAndQuote(s[i]));
	    	}
	    return buffer.toString();
	    }
    
    /** return a collection as a comma delimited list of values */
    static public String collectionToString(Collection<?> collection) 
    	{
    	if(collection==null || collection.isEmpty()) throw new IllegalArgumentException("Collection should not be empty");
    	StringBuilder builder=null;
    	for(Object o: collection)
    		{
    		if(o==null) throw new IllegalArgumentException("collection contains a null value");
    		builder= (builder==null?new StringBuilder():builder.append(","));
    		if(o instanceof Number)
    			{
    			builder.append(o.toString());
    			}
    		else
    			{
    			builder.append(escapeAndQuote(o.toString()));
    			}
    		}
    	return builder.toString();
    	}
    
    
    /** put the current row into an array */
	static public Object[] toArray(ResultSet row) throws SQLException
    	{
    	Object array[]= new Object[row.getMetaData().getColumnCount()];
    	for(int i=0;i<array.length;++i)
    		array[i]=row.getObject(i+1);
    	return array;
    	}
    
    /** put first row of a ResultSet in a Vector */
    @SuppressWarnings("unchecked")
	static public Vector<Object> resultSetToVector(ResultSet row) throws SQLException
    	{
    	Vector<Object> data=new Vector<Object>();
    	while(row.next()) data.add(row.getObject(1));
    	return data;
    	}
    
    /** put first row of a ResultSet in a generic Vector */
	static public <X> Vector<X> resultSetToVector(ResultSet row,Class<X> clazz) throws SQLException
    	{
    	Vector<X> data=new Vector<X>();
    	while(row.next()) data.add(clazz.cast(row.getObject(1)));
    	return data;
    	}
    
    /** put each ResultSet in a new TableModel */
    static public DefaultTableModel resultSetToTableModel(ResultSet row) throws SQLException
    	{
    	return resultSetToTableModel(new DefaultTableModel(), row);
    	}
    
    /** put each ResultSet in an existing TableModel if model==null, a new one is created */
    static public DefaultTableModel resultSetToTableModel(DefaultTableModel model,ResultSet row) throws SQLException
    	{
    	Object prevValue=null;
    	ResultSetMetaData meta= row.getMetaData();
    	if(model==null) model= new DefaultTableModel();
    	String cols[]=new String[meta.getColumnCount()];
    	for(int i=0;i< cols.length;++i)
    		{
    		cols[i]= meta.getColumnLabel(i+1);
    		}
    	
		model.setColumnIdentifiers(cols);
		
    	while(row.next())
    		{
    		Object data[]= new Object[cols.length];
    		for(int i=0;i< data.length;++i)
	    		{
	    		data[i]=row.getObject(i+1);
	    		
	    		if(	data[i]!=null &&
	    			model.getRowCount()>0 &&
	    			(prevValue=model.getValueAt(model.getRowCount()-1, i))!=null
	    			&& prevValue.equals(data[i])
	    			)
	    			{
	    			data[i]=prevValue;
	    			}
	    		}
    		model.addRow(data);
    		}
    	return model;
    	}
    
    
    
    public static Number selectLastInsertId(Connection con) throws SQLException
    	{
    	PreparedStatement stmt=con.prepareStatement("select last_insert_id()");
    	Number n= selectOneValue(stmt.executeQuery(),Number.class);
    	stmt.close();
    	return n;
    	}
    
    public static Object selectOneValue(ResultSet row) throws SQLException
		{
	    boolean found=false;
	    Object value=null;
		while(row.next())
			{
			if(found) throw new SQLException("Excpected only one value");
			value=row.getObject(1);
			found=true;
			}
		if(!found) throw new SQLException("Excpected one value");
		return value;
		}
    
    public static <X> X selectOneValue(ResultSet row,Class<X> clazz) throws SQLException
    	{
    	return clazz.cast(selectOneValue(row));
    	}
    
    
    public static Object selectOneOrZeroValue(ResultSet row) throws SQLException
		{
	    boolean found=false;
	    Object value=null;
		while(row.next())
			{
			if(found) throw new SQLException("Excpected only one value");
			value=row.getObject(1);
			found=true;
			}
		return value;
		}
    
    public static <X> X selectOneOrZeroValue(ResultSet row,Class<X> clazz) throws SQLException
		{
		return clazz.cast(selectOneOrZeroValue(row));
		}
    
    
    }
