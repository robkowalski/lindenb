package org.lindenb.swapp;

import javax.swing.JComponent;
import javax.swing.JTextField;


public abstract class AbstractTextRDFEditor
extends RDFEditor
	{
    /** text field */
    private JTextField tf= new JTextField(15);
    private boolean required=false;

    JTextField getTextField() { return this.tf;}
    
    @Override
    public boolean isEmpty()
    	{
        return getTextField().getText().trim().length()==0;
        }
    
    public boolean isRequired() {
		return required;
		}
    
    public void setRequired(boolean required) {
		this.required = required;
		}
    
	@Override
	public JComponent getComponent() {
		return getTextField();
	   }

	}
