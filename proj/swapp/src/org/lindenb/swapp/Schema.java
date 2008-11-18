package org.lindenb.swapp;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public  interface Schema
	{
	/** @return the associated jena model */
	public Model getModel();
	/** @return a title for this resource */
	public String getTitle(Resource subject);
	/** @return a i18 String */
	public String lang(String s);
	}
