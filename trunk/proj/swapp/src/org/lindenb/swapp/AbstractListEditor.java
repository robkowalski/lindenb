package org.lindenb.swapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.lindenb.sw.RDFException;
import org.lindenb.sw.nodes.RDFNode;
import org.lindenb.sw.nodes.Statement;
import org.lindenb.swing.ConstrainedAction;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Walker;


/**
 * 
 * AbstractListEditor
 *
 */
public  abstract class AbstractListEditor extends RDFEditor
	{
	private JPanel pane;
	private JTextField inputField;
	protected JList list;
	AbstractListEditor()
		{
		this.pane= new JPanel(new BorderLayout());
		this.pane.setBorder(new LineBorder(Color.GRAY));
		JPanel top=new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.pane.add(top,BorderLayout.NORTH);
		top.add(new JLabel("Add:",JLabel.RIGHT));
		this.inputField= new JTextField(10);
		top.add(this.inputField);
		AbstractAction action= new AbstractAction("OK")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s=inputField.getText().trim();
				if(!accept(s))
					{
					Toolkit.getDefaultToolkit().beep();
					return;
					}
				DefaultListModel model= DefaultListModel.class.cast(list.getModel());
				int i=0;
				for(i=0;i< model.getSize();++i)
					{
					String x=String.class.cast(model.elementAt(i));
					if(x.equalsIgnoreCase(s))
						{
						Toolkit.getDefaultToolkit().beep();
						return;
						}
					}
				model.addElement(s);
				inputField.setText("");
				}
			};
		this.inputField.addActionListener(action);
		
		
		
		top.add(new JButton(action));
		this.list= new JList(new DefaultListModel());
		this.pane.add(new JScrollPane(this.list),BorderLayout.CENTER);
		
		
		ConstrainedAction<JList> remove= new ConstrainedAction<JList>(this.list,"Remove")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0)
				{
				int i= list.getSelectedIndex();
				if(i==-1) return;
				DefaultListModel m= DefaultListModel.class.cast(list.getModel());
				m.removeElementAt(i);
				}
			};
		top.add(new JButton(remove));
		remove.mustHaveOneRowSelected(list);
		SwingUtils.setFontDeep(this.pane, new Font("Dialog",Font.PLAIN,9));
		}
	/** no edition is possible (e.g. FOAF.mbox_sha1sum */
	public void disable()
		{
		this.inputField.setEnabled(false);
		this.list.setEnabled(false);
		}
	
	@Override
	public JComponent getComponent() {
		return this.pane;
		}
	
	@Override
	public String getValidationMessage() {
		return null;
		}
	@Override
	public boolean isEmpty() {
		return this.list.getModel().getSize()==0;
		}
	
	protected abstract void loadNode(RDFNode node);
	protected abstract void saveNode(String s);
	//added this for PersonEditor, then I can save mbox and mbox_sha1 at the same time
	protected void cleanModel() throws RDFException
		{
		removeAll(getSubject(), getProperty(), null);
		}
	protected abstract boolean accept(String s);
	
	@Override
	public void loadFromModel() throws RDFException
		{
		DefaultListModel model= DefaultListModel.class.cast(list.getModel());
		inputField.setText("");
		model.setSize(0);
		Walker<Statement> iter=getModel().filter(getSubject(),getProperty(),null);
		Statement stmt=null;
		while((stmt=iter.next())!=null)
			{
			loadNode(iter.nextNode());
			}
		iter.close();
		}
	
	
	
	@Override
	public void saveToModel() throws RDFException
		{
		if(!this.inputField.isEnabled())
			{
			//need this after I found conflict with mbox who created is own mbox_sha1
			return;
			}
		DefaultListModel model= DefaultListModel.class.cast(list.getModel());
		cleanModel();
		for(int i=0;i< model.getSize();++i)
			{
			saveNode(String.class.cast(model.elementAt(i)));
			}
		}


}
