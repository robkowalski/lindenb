package org.lindenb.swapp;


import javax.swing.JComponent;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public abstract class RDFEditor
	{
    /** subject of this editor  */
    private Resource subject=null;
    /** property of this editor */
    private Property property=null;
    /** schema for this editor */
    private Schema schema=null;
    
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
    public void setProperty(Property property)
    	{
        this.property = property;
        getComponent().setName(getModel().shortForm(property.getURI()));
        }
    
    /** get the property */
    public Property getProperty() {
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
    public Model getModel()
    	{
    	return getSchema().getModel();
    	}
    
    public abstract JComponent getComponent();
    public abstract boolean isEmpty();
    public boolean isValid() { return getValidationMessage()==null;}
    public abstract String getValidationMessage();
    public abstract void loadFromModel();
    /** save the content of this editor to the model */
    public abstract void saveToModel();
	
    
    protected int removeAll(Resource r, Property p,RDFNode s)
    	{
    	int n=0;
    	StmtIterator iter= getModel().listStatements(r,p,s);
    	while(iter.hasNext())
    		{
    		iter.next();
    		iter.remove();
    		++n;
    		}
    	iter.close();
    	return n;
    	}
    
	}
