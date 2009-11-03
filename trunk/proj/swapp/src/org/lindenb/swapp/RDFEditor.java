package org.lindenb.swapp;


import javax.swing.JComponent;

import org.lindenb.sw.RDFException;
import org.lindenb.sw.nodes.RDFNode;
import org.lindenb.sw.nodes.Resource;





public abstract class RDFEditor
	{
    /** subject of this editor  */
    private Resource subject=null;
    /** property of this editor */
    private Resource property=null;
    /** schema for this editor */
    private Schema schema=null;
    /** model */
    private AbstractJSONDBApplication model=null;
    
    protected RDFEditor()
    	{    	
    	}
    
    /** set the subject */
    public void setSubject(Resource subject) {
            this.subject = subject;
            }
    
    /** get the subject */
    public Resource getSubject() {
            return subject;
            }
    
    /** set the property */
    public void setProperty(Resource property)
    	{
        this.property = property;
        getComponent().setName(getSchema().shortForm(property.getURI()));
        }
    
    /** get the property */
    public Resource getProperty() {
            return property;
            }
    
    /** get the schema */
    public Schema getSchema()
    	{
    	return this.schema;
    	}
    
    /** set the schema */
    public void setSchema(Schema schema)
    	{
    	this.schema=schema;
    	}
    
    /** get RDF Model */
    public AbstractJSONDBApplication getModel()
    	{
    	return model;
    	}
    
    public void setModel(AbstractJSONDBApplication model) {
		this.model = model;
		}
    
    public abstract JComponent getComponent();
    public abstract boolean isEmpty();
    public boolean isValid() { return getValidationMessage()==null;}
    public abstract String getValidationMessage();
    public abstract void loadFromModel() throws RDFException;
    /** save the content of this editor to the model */
    public abstract void saveToModel() throws RDFException;
	
    
    protected int removeAll(Resource r, Resource p,RDFNode s) throws RDFException
    	{
    	return getModel().remove(r, p, s);
    	}
    
	}
