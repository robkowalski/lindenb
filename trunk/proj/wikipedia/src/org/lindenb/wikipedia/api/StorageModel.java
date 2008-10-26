package org.lindenb.wikipedia.api;



import java.io.PrintStream;


import java.sql.Timestamp;

import java.util.Collection;
import java.util.Set;

import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.Entry;
import org.lindenb.wikipedia.api.MWNamespace;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.User;




public interface StorageModel
	{
	public void clear() throws MWException;
	
	public Collection<Revision> listRevisions(
			Entry entry,
			User user,
			Timestamp start,
			Timestamp end
			)   throws MWException;
	
	
	public void insertEntry(Entry entry) throws MWException;
	public void insertLinks(Entry one,Collection<? extends Entry> many) throws MWException;
	public Set<Entry> listEntries(MWNamespace ns) throws MWException;
	
	public <T extends Entry >Set<T> listEntries(MWNamespace ns,Class<T> clazz) throws MWException;
	
	public Set<Page> listPages() throws MWException;
	
	public Set<User> listUsers() throws MWException;
	public Set<Category> listCategories() throws MWException;
	//"create table MW.entry2cat(entry_id int not null,cat_id int not null,unique (entry_id,cat_id));"
	
	public Set<Category> listCategories(Entry entry) throws MWException;
	
	public void insertRevision(Revision rev) throws MWException;
	
	
	public void toRDF(PrintStream out,String base) throws MWException;
	}
