/**
 * 
 */
package org.lindenb.swing.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author pierre
 *
 */
public abstract class GenericTableModel<T> extends AbstractGenericTableModel<T>
	{
	private static final long serialVersionUID = 1L;
	private Vector<T> data= new Vector<T>();
	
	public GenericTableModel()
		{
		
		}

	protected Vector<T> getDataVector()
		{
		return this.data;
		}
	
	@Override
	public T elementAt(int rowIndex) {
		return getDataVector().elementAt(rowIndex);
		}


	@Override
	public int getRowCount() {
		return getDataVector().size();
		}
	
	public void addElement(T t)
		{
		getDataVector().addElement(t);
		fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
		}
	
	public void addAll(Collection<T> col)
		{
		if(col.isEmpty()) return;
		int i= getRowCount();
		getDataVector().addAll(col);
		fireTableRowsInserted(i,i+col.size()-1);
		}
	
	public void removeElement(T t)
		{
		int i = indexOf(t);
		if(i!=-1) removeElementAt(i);
		}
	
	public void removeElementAt(int i)
		{
		getDataVector().removeElementAt(i);
		fireTableRowsDeleted(i,i);
		}
	
	public void clear()
		{
		int n= getDataVector().size();
		if(n==0) return;
		getDataVector().clear();
		fireTableRowsDeleted(0, n-1);
		}
	
	public void sort(Comparator<T> comparator)
		{
		if(isEmpty()) return;
		Collections.sort(getDataVector(),comparator);
		fireTableRowsUpdated(0, getDataVector().size()-1);
		}
	
	@Override
	public Iterator<T> listElements()
		{
		return new MyIterator();
		}	
	
	
	public Collection<T> elements()
		{
		return new ArrayList<T>(getDataVector());
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
			removeElementAt(index--);
			}
		}
	}
