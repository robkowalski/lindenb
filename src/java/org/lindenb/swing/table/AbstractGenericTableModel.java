/**
 * 
 */
package org.lindenb.swing.table;

import java.util.Vector;



/**
 * @author pierre
 *
 */
public abstract class AbstractGenericTableModel<T> extends XAbstractTableModel
	{
	public abstract T elementAt(int rowIndex);
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		T t= elementAt(rowIndex);
		if(t==null) return null;
		return getValueOf(t,columnIndex);
		}
	
	public abstract Object getValueOf(T object, int columnIndex);
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
		}
	
	public int indexOf(T t)
		{
		return indexOf(t,0);
		}
	
	public int getElementCount()
		{
		return getRowCount();
		}
	
	public int indexOf(T t,int i)
		{
		if(t==null)
			{
			while(i< getElementCount())
				{
				if(elementAt(i)==null) return i;
				++i;
				}
			}
		else
			{
			while(i< getElementCount())
				{
				T x= elementAt(i);
				if(x!=null && x.equals(t)) return i;
				++i;
				}
			}
		return -1;
		}
	
	public boolean contains(T o)
		{
		return indexOf(o)!=-1;
		}
	
	public Vector<T> asVector()
		{
		Vector<T> v= new Vector<T>(getElementCount());
		for(int i=0;i< getElementCount();++i) v.addElement(elementAt(i));
		return v;
		}
	}
