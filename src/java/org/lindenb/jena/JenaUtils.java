package org.lindenb.jena;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.lindenb.jena.vocabulary.FOAF;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/** useful tools for the JENA API */

public class JenaUtils
	{
	/** getString returns the literal for the given subject/property */
	public static String getString(Model model,Resource subject,Property prop)
		{
		StmtIterator iter=model.listStatements(subject, prop, String.class.cast(null));
		String value=null;
		while(iter.hasNext())
			{
			Statement stmt=iter.nextStatement();
			if(!stmt.getObject().isLiteral()) continue;
			value=stmt.getLiteral().toString();
			break;
			}
		iter.close();
		return value;
		}
	
	/** getString returns the literal for the given subject/property.
	 * if none was found, it returns the default value */
	public static String getString(Model model,Resource subject,Property prop,String defaultValue)
		{
		String s= getString(model,subject,prop);
		return s==null?defaultValue:s;
		}
	
	/** return the first element of iterator */
	public static Statement first(StmtIterator iter)
		{
		Statement stmt=(iter.hasNext()?iter.nextStatement():null);
		iter.close();
		return stmt;
		}
	
	/** return true if subject contains at least on rdf:type */
	public static boolean hasType(Model m, Resource subject, Collection<Resource>  types)
		{
		for(Resource t: types)
			{
			if( m.contains(subject, RDF.type, t)) return true;
			}
		return false;
		}

	/** remove all matching statements. null can be used as wilcard  */
	public static void remove(Model m, Resource subject, Property prop, RDFNode object)
		{
		StmtIterator iter=m.listStatements(subject,prop,object);
		m.remove(iter);
		iter.close();
		}

	/** modal dialog prompting a new absolute URI not in model */
	public static Resource askNewURL(Model model,Component owner,String title)
		{
		String uri=null;
		while(true)
			{
			uri= JOptionPane.showInputDialog(
					(title==null?"New URL":title),
					uri==null?"":uri
					);
			if(uri==null) return null;
			uri=uri.trim();
			try {
				new URL(uri);
				} 
			catch (MalformedURLException e)
				{
				JOptionPane.showMessageDialog(owner, "Not a valid URL "+uri);
				continue;
				}
			
			Resource rsrc= model.createResource(uri);
			if(model.containsResource(rsrc))
				{
				JOptionPane.showMessageDialog(owner, "Already exists "+uri);
				continue;
				}
			return rsrc;
			}
		}	
	
	/** modal dialog prompting a new absolute URI not in model */
	public static Resource askNewURI(Model model,Component owner,String title)
		{
		String uri=null;
		while(true)
			{
			uri= JOptionPane.showInputDialog(
					(title==null?"New URI":title),
					uri==null?"":uri
					);
			if(uri==null) return null;
			uri=uri.trim();
			try {
				URI loc=new URI(uri);
				if(!loc.isAbsolute())
					{
					JOptionPane.showMessageDialog(
							owner,
							"URI should be absolute  "+uri);
					continue;
					}
				} 
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(owner, "Not a valid URI "+uri);
				continue;
				}
			Resource rsrc= model.createResource(uri);
			if(model.containsResource(rsrc))
				{
				JOptionPane.showMessageDialog(owner, "Already exists "+uri);
				continue;
				}
			return rsrc;
			}
		}

/** find all resources having the given rdf:type */
public static HashSet<Resource> findResourceHavingType(
		Model model,Resource rdfType)
	{
	HashSet<Resource> set= new HashSet<Resource>();
	ResIterator iter=model.listSubjectsWithProperty(RDF.type,rdfType);
	while(iter.hasNext())
		{
		set.add(iter.nextResource());	
		}
	iter.close();
	return set;
	}

public static String findTitle(Model model,Resource rsrc)
	{
	String title=null;
	for(Property prop: new Property[]{FOAF.name,DC.title,RDFS.label,RDFS.comment})
		{
		StmtIterator iter=model.listStatements(rsrc, prop,(RDFNode)null);
		while(iter.hasNext())
			{
			Statement stmt= iter.nextStatement();
			if(stmt.getObject().isLiteral())
				{
				title= stmt.getLiteral().getString();
				break;
				}
			}
		iter.close();
		if(title!=null) return title;
		}
	return rsrc.getURI();
	}

/** compare two RDF nodes */
public static int compare(RDFNode n1, RDFNode n2)
	{
	if(n1==null && n2==null)
		{
		return 0;
		}
	else if(n1==null && n2!=null)
		{
		return -1;
		}
	else if(n1!=null && n2==null)
		{
		return 1;
		}
	else if(n1.isResource() && !n2.isResource())
		{
		return -1;
		}
	else if(!n1.isResource() && n2.isResource())
		{
		return 1;
		}
	else if(n1.isResource() && n2.isResource())
		{
		return Resource.class.cast(n1).getURI().compareTo(Resource.class.cast(n2).getURI());
		}
	else if(n1.isLiteral() && n2.isLiteral())
		{
		return Literal.class.cast(n1).getString().compareToIgnoreCase(Literal.class.cast(n2).getString());
		}
	return n1.toString().compareTo(n2.toString());
	}

}
