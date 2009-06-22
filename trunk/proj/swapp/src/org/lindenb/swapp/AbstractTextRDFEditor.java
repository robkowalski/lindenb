package org.lindenb.swapp;

import javax.swing.JComponent;
import javax.swing.JTextField;


public abstract class AbstractTextRDFEditor
extends RDFEditor
	{
    /** text field */
    private JTextField tf= new JTextField(15);
   
    JTextField getTextField() { return this.tf;}

    @Override
    public boolean isEmpty()
    	{
        return getTextField().getText().trim().length()==0;
        }

	@Override
	public JComponent getComponent() {
		return getTextField();
	   }

	
	}
