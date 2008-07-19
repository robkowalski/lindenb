package org.lindenb.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class FOAF extends org.lindenb.sw.vocabulary.FOAF
	{
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Person = m.createResource(NS+ "Person" );
	public static final Resource Group = m.createResource(NS+ "Group" );
	public static final Resource Organization = m.createResource(NS+ "Organization" );
	public static final Resource Document = m.createResource(NS+ "Document" );
	public static final Resource Image = m.createResource(NS+ "Image" );
	public static final Resource PersonalProfileDocument = m.createResource(NS+ "PersonalProfileDocument" );
	public static final Resource OnlineAccount =  m.createResource(NS+ "OnlineAccount" );
	public static final Property name = m.createProperty(NS, "name" );
	public static final Property firstName = m.createProperty(NS, "firstName" );
	public static final Property givenname = m.createProperty(NS, "givenname" );
	public static final Property family_name = m.createProperty(NS, "family_name" );
	public static final Property surname = m.createProperty(NS, "surname" );
	public static final Property knows = m.createProperty(NS, "knows" );
	public static final Property primaryTopic = m.createProperty(NS, "primaryTopic" );
	public static final Property maker = m.createProperty(NS, "maker" );
	public static final Property made = m.createProperty(NS, "made" );
	public static final Property gender = m.createProperty(NS, "gender" );
	public static final Property birthday = m.createProperty(NS, "birthday" );
	public static final Property schoolHomepage = m.createProperty(NS, "schoolHomepage" );
	public static final Property homepage = m.createProperty(NS, "homepage" );
	public static final Property based_near = m.createProperty(NS, "based_near" );
	public static final Property accountServiceHomepage = m.createProperty(NS, "accountServiceHomepage" );
	public static final Property accountName = m.createProperty(NS, "accountName" );
	public static final Property holdsAccount = m.createProperty(NS, "holdsAccount" );
	public static final Property depicts = m.createProperty(NS, "depicts" );
	public static final Property depiction = m.createProperty(NS, "depiction" );
	public static final Property member = m.createProperty(NS, "member" );
	public static final Property mbox_sha1sum = m.createProperty(NS, "mbox_sha1sum" );
	public static final Property mbox = m.createProperty(NS, "mbox" );
	public static final Property interest = m.createProperty(NS, "interest" );
	
	}