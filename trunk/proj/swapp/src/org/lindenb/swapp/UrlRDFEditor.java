package org.lindenb.swapp;

import java.net.URL;

import org.lindenb.util.Cast;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class UrlRDFEditor extends AbstractTextRDFEditor
	{
	public UrlRDFEditor()
		{
		
		}
	
    @Override
    public String getValidationMessage()
		{
    	if(isEmpty())
    		{
    		if(isRequired())
    			{
    			return getSchema().lang("VALUE_REQUIRED");
    			}
    		}
    	else if(!Cast.URL.isA(getTextField().getText()))
        	{
        	return getSchema().lang("NOT_AN_URL");
            }
        return null;
        }
    
    public URL getURL()
    	{
    	return Cast.URL.cast(getTextField().getText());
    	}
    
    @Override
    public void loadFromModel()
            {
    		getTextField().setText("");
            NodeIterator iter= getModel().listObjectsOfProperty(getSubject(),getProperty());
            while(iter.hasNext())
                    {
                    RDFNode node= iter.nextNode();
                    if(!node.isResource()) continue;
                    if(node.isAnon()) continue;
                    getTextField().setText(Resource.class.cast(node).getURI());
                    break;
                    }
            iter.close();
            getTextField().setCaretPosition(0);
            }

    @Override
    public void saveToModel()
    		{
            String s= getTextField().getText().trim();
            removeAll(getSubject(), getProperty(),null);
            if(s.length()!=0) getModel().add(
                            getSubject(),
                            getProperty(),
                            getModel().createResource(s)
                            );
            }
	}
