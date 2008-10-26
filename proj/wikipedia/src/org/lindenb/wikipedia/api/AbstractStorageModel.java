package org.lindenb.wikipedia.api;


import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.MWNamespace;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.User;
import org.lindenb.xml.XMLUtilities;

public abstract class AbstractStorageModel
	implements StorageModel
	{
	public AbstractStorageModel()
		{
		}

	
	public <T extends Entry >Set<T> listEntries(MWNamespace ns,Class<T> clazz) throws MWException
		{
		Set<T> items= new TreeSet<T>();
		for(Entry e: listEntries(ns))
			{
			items.add(clazz.cast(e));
			}
		return items;
		}
	
	public Set<Page> listPages() throws MWException
		{
		return listEntries(MWNamespace.Main,Page.class);
		}
	
	public Set<User> listUsers() throws MWException
		{
		return listEntries(MWNamespace.User,User.class);
		}
	public Set<Category> listCategories() throws MWException
		{
		return listEntries(MWNamespace.Category,Category.class);
		}
	
	
	public void toRDF(PrintStream out,String base) throws MWException
		{
		out.println("<rdf:RDF xmlns:rdf=\""+RDF.NS+"\"" +
				" xmlns:dc=\"" + DC.NS+"\""+
				" xmlns=\"http://"+base+"\">");
		for(Page page:listPages())
			{
			out.println("  <Page rdf:about=\""+base+ "/"+
					page.getQNameEncoded()
					+"\">");
			for(Category cat: listCategories(page))
				{
				out.println("    <category rdf:resource=\""+
						base+ "/"+ cat.getQNameEncoded()
						+"\"/>");
				}
			for(Revision rev: listRevisions(page,null,null,null))
				{
				out.println("  <revision>");
				out.println("    <Revision rdf:ID=\"#rev"+rev.getRevId()+"\">");
				out.println("      <dc:date>"+XMLUtilities.escape(rev.getDate().toString())+"</dc:date>");
				out.println("      <user rdf:resource=\""+base+ "/"+ rev.getUser().getQNameEncoded() +"\"/>");
				if(rev.getSize()>0) out.println("    <size>"+rev.getSize()+"</size>");
				out.println("      <comment>"+XMLUtilities.escape(rev.getComment())+"</comment>");
				out.println("    </Revision>");
				out.println("  </revision>");
				}
			out.println("  </Page>");
			}
		
		out.println("<rdf:RDF>");
		}
	
	
	
	}
