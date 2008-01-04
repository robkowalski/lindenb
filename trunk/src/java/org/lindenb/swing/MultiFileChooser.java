/**
 * 
 */
package org.lindenb.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.lindenb.swing.table.GenericTableModel;

/**
 * @author pierre
 *
 */
public class MultiFileChooser extends JPanel
{
private static final long serialVersionUID = 1L;
private JTable table;
private ConstrainedAction<MultiFileChooser> removeFileAction;
private File defaultDirectory=null;
private FileFilter fileFilter=null;
private GenericTableModel<File> tableModel= new GenericTableModel<File>()
		{
		private static final long serialVersionUID = 1L;
		@Override
		public String getColumnName(int column) {
			switch(column)
				{
				case 0: return "Name";
				case 1: return "Path";
				}
			return null;
			}

		@Override
		public Object getValueOf(File object, int column)
			{
			switch(column)
				{
				case 0: return object.getName();
				case 1: return object.getPath();
				}
			return null;
			}

		@Override
		public int getColumnCount()
			{
			return 2;
			}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
			}
	};

	
public MultiFileChooser()
	{
	this(null);
	}

public MultiFileChooser(File defaultDirectory)
	{
	super(new BorderLayout());
	setDefaultDirectory(defaultDirectory);
	add(new JScrollPane(this.table= new JTable(this.tableModel)),BorderLayout.CENTER);
	JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
	add(bot,BorderLayout.SOUTH);
	
	JButton b1= new JButton(new AbstractAction("Add...")
		{
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser= new JFileChooser(getDefaultDirectory());
			chooser.setMultiSelectionEnabled(true);
			FileFilter f= getFileFilter();
			if(f!=null) chooser.setFileFilter(f);
			if(chooser.showOpenDialog(MultiFileChooser.this)!=JFileChooser.APPROVE_OPTION) return;
			
			HashSet<File> alreadyIn= new HashSet<File>();
			alreadyIn.addAll(getFiles());
			for(File file:chooser.getSelectedFiles())
				{
				if(alreadyIn.contains(file)) continue;
				alreadyIn.add(file);
				tableModel.addElement(file);
				setDefaultDirectory(file);
				}
			}
		});
	bot.add(b1);
	
	JButton b2= new JButton(this.removeFileAction= new ConstrainedAction<MultiFileChooser>(this,"Remove")
		{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Vector<Integer> v= new Vector<Integer>();
			for(int i:table.getSelectedRows())
				{
				int idx= table.convertRowIndexToModel(i);
				if(idx==-1) continue;
				v.add(idx);
				}
			Collections.reverse(v);
			for(int i: v)
				{
				tableModel.removeElementAt(i);
				}
			}
		});
	bot.add(b2);
	this.removeFileAction.mustHaveAtLeastOneRowSelected(this.table);
	SwingUtils.setFontSize(this, 9);
	Dimension d1= b1.getPreferredSize();
	Dimension d2= b2.getPreferredSize();
	d1.width= Math.max(d1.width, d2.width);
	d1.height= Math.max(d1.height, d2.height);
	b1.setPreferredSize(d1);
	b2.setPreferredSize(d2);
	}


public JTable getTable() {
	return table;
	}

public GenericTableModel<File> getTableModel() {
	return tableModel;
	}

public Vector<File> getFiles()
	{
	Vector<File> f= new Vector<File>(tableModel.getRowCount());
	for(int i=0;i< tableModel.getElementCount();++i) f.addElement(tableModel.elementAt(i));
	return f;
	}

public void setDefaultDirectory(File defaultDirectory) {
	this.defaultDirectory = defaultDirectory;
	}

public File getDefaultDirectory() {
	return defaultDirectory;
	}

public FileFilter getFileFilter() {
	return fileFilter;
	}

public void setFileFilter(FileFilter fileFilter) {
	this.fileFilter = fileFilter;
	}


public static void main(String[] args) {
	JOptionPane.showMessageDialog(null, new MultiFileChooser());
	}

}
