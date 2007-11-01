package org.lindenb.swing.table;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public abstract class XAbstractTableModel extends AbstractTableModel
	{
	private static final long serialVersionUID = 1L;
	public XAbstractTableModel()
		{
		}
	
	public <T> T getValueAt(int rowIndex, int columnIndex,Class<T> clazz)
		{
		return clazz.cast(getValueAt(rowIndex, columnIndex));
		}
	
	/** return all values in a column */
	public <X> Vector<X> getValuesInColumn(
			 Class<X> clazz,
			 int columnIndex,
			 boolean ignoreNullValues
			 )
		{
		Vector<X> data= new Vector<X>(getRowCount());
		for(int i=0;i< getRowCount();++i)
			{
			X val= getValueAt(columnIndex,i,clazz);
			if(val==null && ignoreNullValues) continue;
			data.addElement(val);
			}
		return data;
		}
	
	/** return all values in a column for the given row indexes */
	public <X> Vector<X> getValuesInColumn(
			 Class<X> clazz,
			 int columnIndex,
			 int rowIndexes[],
			 boolean ignoreNullValues
			 )
		{
		Vector<X> data= new Vector<X>(rowIndexes.length);
		for(int i:rowIndexes)
			{
			X val= getValueAt(columnIndex,i,clazz);
			if(val==null && ignoreNullValues) continue;
			data.addElement(val);
			}
		return data;
		}
	
	/** return all values in a column , ignoring null values*/
	public <X> Set<X> getDistinctValuesInColumn(
			 Class<X> clazz,
			 int columnIndex
			  )
			{
			HashSet<X> data= new HashSet<X>();
			for(int i=0;i< getRowCount();++i)
				{
				X val= getValueAt(columnIndex,i,clazz);
				if(val==null) continue;
				data.add(val);
				}
			return data;
			}
	
	
	/** return all values in a column for the given row indexes , ignoring null values*/
	public <X> Set<X> getDistinctValuesInColumn(
			 Class<X> clazz,
			 int columnIndex,
			 int rowIndexes[]
			  )
			{
			HashSet<X> data= new HashSet<X>();
			for(int i:rowIndexes)
				{
				X val= getValueAt(columnIndex,i,clazz);
				if(val==null) continue;
				data.add(val);
				}
			return data;
			}
	
	public String getStringAt( int rowIndex,int columnIndex)
		{
		Object o= getValueAt(rowIndex, columnIndex);
		return (o==null?null:o.toString());
		}
	
	public Integer getIntAt( int rowIndex,int columnIndex)
		{
		Object o= getValueAt(rowIndex, columnIndex);
		if(o==null) return null;
		if(o instanceof Number) return Number.class.cast(o).intValue();
		return new Integer(o.toString().trim());
		}
	
	public Double getDoubleAt( int rowIndex,int columnIndex)
		{
		Object o= getValueAt(rowIndex, columnIndex);
		if(o==null) return null;
		if(o instanceof Number) return Number.class.cast(o).doubleValue();
		return new Double(o.toString().trim());
		}
	
	public int findValueIndex(int columnIndex,Object o,int fromRowIndex)
		{
		while(fromRowIndex< getRowCount())
			{
			Object o2= getValueAt(fromRowIndex, columnIndex);
			if((o2==null && o==null) || 
			   (o2!=null && o2.equals(o)) ) return fromRowIndex;
			++fromRowIndex;
			}
		return -1;
		}
	
	public int findValueIndex(int columnIndex,Object o)
		{
		return findValueIndex(columnIndex, o,0);
		}
	
	public boolean isEmpty()
		{
		return getRowCount()==0;
		}
	

	
	}
