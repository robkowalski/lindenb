/**
 * 
 */
package org.lindenb.swing.table;

import java.util.Vector;



/**
 * @author pierre
 *
 */
public abstract class GenericTableModel<T> extends AbstractGenericTableModel<T>
	{
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
	
	}
