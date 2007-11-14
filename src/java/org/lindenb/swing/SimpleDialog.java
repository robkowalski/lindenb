/**
 * 
 */
package org.lindenb.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;


/**
 * @author pierre
 *
 */
public class SimpleDialog extends JDialog
	{
	private static final long serialVersionUID = 1L;
	public static final int OK_OPTION= JOptionPane.OK_OPTION;
	public static final int CANCEL_OPTION= JOptionPane.CANCEL_OPTION;
	private ConstrainedAction<SimpleDialog> okAction;
	private JTextField errorMessageLabel;
	protected JPanel contentPane;
	protected JPanel bottomPane;
	private int status=CANCEL_OPTION;
	
	protected void init()
		{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.contentPane= new JPanel(new BorderLayout(5,5));
		this.contentPane.setBorder(new EmptyBorder(5,5,5,5));
		setContentPane(this.contentPane);
		this.bottomPane= new JPanel(new FlowLayout(FlowLayout.TRAILING));
		this.contentPane.add(this.bottomPane,BorderLayout.SOUTH);
		JButton button1;
		
		this.errorMessageLabel= new JTextField("",20);
		this.errorMessageLabel.setForeground(Color.RED);
		this.errorMessageLabel.setEditable(false);
		this.bottomPane.add(this.errorMessageLabel);
		this.errorMessageLabel.setBorder(new EmptyBorder(0,0,0,0));
		this.errorMessageLabel.setFont(new Font("Dialog",Font.PLAIN,9));
		this.bottomPane.add(new JSeparator(JSeparator.VERTICAL));
		this.okAction= createOKAction();
		this.bottomPane.add(button1=new JButton(this.okAction));
		

		
		this.okAction.addPropertyChangeListener(new PropertyChangeListener()
			{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
				{
				if("enabled".equals(evt.getPropertyName()))
					{
					setErrorMessage(okAction.getErrorMessage());
					}
				}
			});
		
		JButton button2;
		this.bottomPane.add(button2=new JButton(new AbstractAction(getCancelLabel(),getCancelIcon())
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialogWithStatus(CANCEL_OPTION);
				}
			}));
		Dimension d1= button1.getPreferredSize();
		Dimension d2= button2.getPreferredSize();
		d1.setSize(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
		button1.setPreferredSize(d1);
		button2.setPreferredSize(d2);
		}
	
	protected String getCancelLabel()
		{
		return "Cancel";
		}
	
	protected Icon getCancelIcon()
		{
		return null;
		}
	
	protected String getOKLabel()
		{
		return "OK";
		}
	
	protected Icon getOKIcon()
		{
		return null;
		}
	
	protected ConstrainedAction<SimpleDialog> createOKAction()
		{
		return new ConstrainedAction<SimpleDialog>(this,
				getOKLabel(),
				this.getOKIcon())
			{
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getErrorMessage() {
				String s= super.getErrorMessage();
				SimpleDialog.this.setErrorMessage(s);
				return s;
				}
			
			@Override
			public void actionPerformed(ActionEvent e)
				{
				String s= SimpleDialog.this.getErrorMessage();
				SimpleDialog.this.setErrorMessage(s);
				if(s!=null)
					{
					return;
					}
				closeDialogWithStatus(OK_OPTION);
				}
			};
		}
	
	protected void setErrorMessage(String s)
		{
		this.errorMessageLabel.setText(s==null?"":s);
		this.errorMessageLabel.setToolTipText(s);
		this.errorMessageLabel.setCaretPosition(0);
		}
	
	protected void closeDialogWithStatus(int status)
		{
		this.status= status;
		setVisible(false);
		dispose();
		}
	
	/**
	 * @param owner
	 * @param modal
	 */
	public SimpleDialog(Component owner,String title)
		{
		super(owner==null?null:SwingUtilities.getWindowAncestor(owner),title, ModalityType.APPLICATION_MODAL);
		init();
		}
	
	public String getErrorMessage()
		{
		return getOKAction().getErrorMessage();
		}
	
	public ConstrainedAction<?> getOKAction()
		{
		return this.okAction;
		}
	
	/**
	 * @param owner
	 * @param modal
	 */
	public SimpleDialog(Component owner)
		{
		this(owner,"Dialog");
		}

	public int showDialog()
		{
		SwingUtils.packAndCenter(this);
		setVisible(true);
		return this.status;
		}
	
	public static void main(String[] args) {
		SimpleDialog d= new SimpleDialog(null);
		System.err.println(d.showDialog());
		}

}
