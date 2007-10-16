/**
 * 
 */
package org.lindenb.swing.table;



/**
 * @author pierre
 *
 */
public abstract class AbstractGenericTableModel<T> extends XAbstractTableModel
	{
	public abstract T getElementAt(int rowIndex);
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		T t= getElementAt(rowIndex);
		if(t==null) return null;
		return getValueOf(t,columnIndex);
		}
	
	public abstract Object getValueOf(T object, int columnIndex);
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
		}
	
	}
