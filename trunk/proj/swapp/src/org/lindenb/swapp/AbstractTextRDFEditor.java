package org.lindenb.swapp;

import javax.swing.JComponent;
import javax.swing.JTextField;


public abstract class AbstractTextRDFEditor
extends RDFEditor
	{
    /** text field */
    private JTextField tf= new JTextField(15);
    private boolean required=false;
    /** maximum length */
    private Integer maxLength=null;
    /** minimum length */
    private Integer minLength=null;
    JTextField getTextField() { return this.tf;}
    
   
    
    public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
		}
    
    public void setMinLength(Integer minLength) {
		this.minLength = minLength;
		}
     
    public Integer getMinLength() {
		return minLength;
		}
   
    public Integer getMaxLength() {
		return maxLength;
		}
    
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

	@Override
	public String getValidationMessage() {
		String s= getTextField().getText();
		if(s.length()==0 && !isRequired()) return null;
		
		if(getMinLength()!=null && s.length()< getMinLength())
			{
			return getSchema().lang("TOO_SHORT");
			}
		
		if(getMaxLength()!=null && s.length()>= getMaxLength())
			{
			return getSchema().lang("TOO_LARGE");
			}
		
		return null;
		}
	
	}
