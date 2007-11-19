/**
 * 
 */
package org.lindenb.swing.table;

import java.util.Iterator;
import java.util.Vector;



/**
 * @author pierre
 *
 */
public abstract class AbstractGenericTableModel<T> extends XAbstractTableModel
	{
	public abstract T elementAt(int rowIndex);
	@Override
	public Object getValueAt(int rowIndex, int column) {
		T t= elementAt(rowIndex);
		if(t==null) return null;
		return getValueOf(t,column);
		}
	
	public abstract Object getValueOf(T object, int column);
	
	@Override
	public boolean isCellEditable(int rowIndex, int column) {
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
	
	public Iterator<T> listElements()
		{
		return new MyIterator();
		}	
	
	private class MyIterator implements Iterator<T>
		{
		int index=-1;
		@Override
		public boolean hasNext() {
			return index+1< getRowCount();
			}
		@Override
		public T next() {
			return elementAt(++index);
			}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			}
		}
	
	}
