package org.lindenb.wikipedia.tool;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.lindenb.sql.SQLUtilities;
import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.wikipedia.api.Category;
import org.lindenb.wikipedia.api.Entry;
import org.lindenb.wikipedia.api.MWNamespace;
import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.User;
import org.lindenb.xml.XMLUtilities;



public class Statistics
	{
	private static final String VERSION="1.0.1";
	private static final String JDBC_DRIVER_NAME="org.apache.derby.jdbc.EmbeddedDriver";
	private File dbFile=null;
	private Stack<Connection> connectionsStack= new Stack<Connection>();

	
	
	public Statistics()
		{
		}
	
	public void open(File dbFile) throws SQLException
		{
		if(this.dbFile!=null) close();
		try {
			Class.forName(JDBC_DRIVER_NAME);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Cannot find "+JDBC_DRIVER_NAME+" in classpath",e);
		}
		this.dbFile=dbFile;
		if(!dbFile.exists())
			{
			Properties dbProperties=new Properties();
			dbProperties.setProperty("user", "anonymous");
			dbProperties.setProperty("password","");
			dbProperties.setProperty("create", "true");	
			Connection con=DriverManager.getConnection("jdbc:derby:"+dbFile,dbProperties);
			java.sql.Statement stmt= con.createStatement();
			String createStatements[]=new String[]{
				"create schema MW;",
				"create table MW.version(version varchar(50) not null unique);",
				"insert into  MW.version(version) values ('"+VERSION+"');",
				"create table MW.entry(id int not null PRIMARY KEY generated always as identity , namespace int not null,name varchar(255) not null,unique (namespace,name));",
				"create table MW.revision(id int not null unique ,mwuser_id int not null references MW.entry(id),page_id int not null references MW.entry(id),when timestamp not null,size int not null default 0,revcomment varchar(255));",
				"create table MW.entry2entry(entry1_id int not null references MW.entry(id),entry2_id int not null references MW.entry(id),unique (entry1_id,entry2_id));"
				};
			

			for(String line:createStatements)
				{
				if(line.endsWith(";")) line=line.substring(0,line.length()-1);
				stmt.executeUpdate(line);
				}

				
			
			con.close();
			}
		else
			{
			Connection con= getConnection();
			Statement stmt=con.createStatement();
			String version=SQLUtilities.selectOneValue(stmt.executeQuery("select version from MW.version"), String.class);
			recycleConnection(con);
			if(!version.equals(VERSION))
				{
				throw new SQLException(
						dbFile.toString()+" was based on version "+version+
						" but current tool is version "+VERSION);
				}
			}
		}
	

	
	public void close()
		{
		if(this.dbFile==null) return;
		
		while(!connectionsStack.isEmpty())
			{
			SQLUtilities.safeClose(connectionsStack.pop());
			}
		Properties dbProperties= new Properties();
		dbProperties.setProperty("user", "anonymous");
		dbProperties.setProperty("password","");
		dbProperties.setProperty("shutdown", "true");
		try
			{
			DriverManager.getConnection("jdbc:derby:"+this.dbFile,dbProperties);
			} catch(Exception err) {}
		this.dbFile=null;
		}
	
	protected Connection getConnection() throws SQLException 
		{
		if(this.dbFile==null) throw new NullPointerException("DB was not open");
		if(!connectionsStack.isEmpty()) return connectionsStack.pop();
		Properties dbProperties=new Properties();
		dbProperties.setProperty("user", "anonymous");
		dbProperties.setProperty("password","");
		dbProperties.setProperty("create", "false");	
		Connection con=DriverManager.getConnection("jdbc:derby:"+dbFile,dbProperties);
		return con;
		}
	
	protected void recycleConnection(Connection con)
		{
		if(con==null || SQLUtilities.safeIsClosed(con)) return;
		try { con.clearWarnings(); } catch(SQLException err) {}
		connectionsStack.push(con);
		}
	
	public void clear() throws SQLException
		{
		Connection con= getConnection();
		Statement stmt= con.createStatement();
		for(String s: new String[]{"MW.entry2entry","MW.revision","MW.entry"})
			{
			stmt.executeUpdate("DELETE FROM "+s); 
			}
		stmt.close();
		recycleConnection(con);
		}
	
	public Entry findEntryById(int id) throws SQLException
		{
		Entry entry=null;
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(
				"select namespace,name from MW.entry where id=?"
				);
		pstmt.setInt(1, id);
		ResultSet row= pstmt.executeQuery();
		while(row.next())
			{
			entry= Entry.create(MWNamespace.findById(row.getInt(1)), row.getString(2));
			break;
			}
		pstmt.close();
		recycleConnection(con);
		return entry;
		}
	
	public <T extends Entry> T findEntryById(int id,Class<T> clazz)  throws SQLException
		{
		return clazz.cast(findEntryById(id));
		}
	
	
	public Collection<Revision> listRevisions(
			Entry entry,
			User user,
			Timestamp start,
			Timestamp end
			)   throws SQLException
		{
		ArrayList<Revision> items= new ArrayList<Revision>();
		Number user_id = null;
		Number entry_id = null;
		if(user!=null)
			{
			user_id=findEntryId(user);
			if(user_id==null) return items;
			}
		
		if(entry!=null)
			{
			entry_id=findEntryId(entry);
			if(entry_id==null) return items;
			}
		
		StringBuilder sql= new StringBuilder(
			"select mwuser_id,page_id,when,size,revcomment,id from MW.revision where 1=1 "
				);
		
		if(user_id!=null) sql.append(" and mwuser_id=?");
		if(entry_id!=null) sql.append(" and page_id=?");
		if(start!=null) sql.append(" and when>=?");
		if(end!=null) sql.append(" and when<?");
		
		
		int i=0;
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(sql.toString());
		if(user_id!=null) pstmt.setInt(++i, user_id.intValue());
		if(entry_id!=null) pstmt.setInt(++i, entry_id.intValue());
		if(start!=null)  pstmt.setTimestamp(++i, start);
		if(end!=null) pstmt.setTimestamp(++i, end);
		ResultSet row= pstmt.executeQuery();
		while(row.next())
			{
			Revision rev= new Revision(
					row.getInt(6),
					(entry==null?findEntryById(row.getInt(2), Entry.class):entry),
					row.getTimestamp(3),
					(user==null?findEntryById(row.getInt(1), User.class):user),
					row.getInt(4),
					row.getString(5)
					);
			items.add(rev);
			}
		pstmt.close();
		
		recycleConnection(con);
		return items;
		}
	
	public Number findEntryId(Entry entry) throws SQLException
		{
		Number id=null;
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement("select id from MW.entry where namespace=? and name=?");
		pstmt.setInt(1, entry.getNamespace().getId());
		pstmt.setString(2, entry.getLocalName());
		id= SQLUtilities.selectOneOrZeroValue(pstmt.executeQuery(), Number.class);
		recycleConnection(con);
		return id;
		}
	
	public int insertEntry(Entry entry) throws SQLException
		{
		Number id=null;
		
		while(true)
			{
			id= findEntryId(entry);
			if(id!=null) break;
			Connection con= getConnection();
			PreparedStatement pstmt= con.prepareStatement(
				"insert into MW.entry(namespace,name) values(?,?)");
			pstmt.setInt(1, entry.getNamespace().getId());
			pstmt.setString(2, entry.getLocalName());
			if(pstmt.executeUpdate()!=1) throw new SQLException("Cannot insert into MW.entry:"+entry);
			recycleConnection(con);
			}
		
		return id.intValue();
		}

	public void insertLinks(Entry one,Collection<? extends Entry> many) throws SQLException
		{
		Number id1=insertEntry(one);
		
		for(Entry child : many)
			{
			Number id2= insertEntry(child);
			Connection con= getConnection();
			PreparedStatement pstmt= con.prepareStatement(
				"insert into MW.entry2entry(entry1_id,entry2_id) values(?,?)");
			pstmt.setInt(1, id1.intValue());
			pstmt.setInt(2,  id2.intValue());
			try
				{
				if(pstmt.executeUpdate()!=1) throw new SQLException("Cannot insert into MW.entry2entry:"+one+"->"+child);
				}
			catch(SQLIntegrityConstraintViolationException e)
				{
				
				//just ignore
				}
			recycleConnection(con);
			}
		}
	
	
	public Collection<Entry> listEntries(MWNamespace ns) throws SQLException
		{
		Connection con= getConnection();
		ArrayList<Entry> items= new ArrayList<Entry>();
		PreparedStatement pstmt= con.prepareStatement(
			"select name from MW.entry where namespace=?"	
			);
		pstmt.setInt(1, ns.getId());
		ResultSet row= pstmt.executeQuery();
		while(row.next())
			{
			items.add(Entry.create(ns,row.getString(1)));
			}
		recycleConnection(con);
		return items;
		}
	
	public <T extends Entry >Collection<T> listEntries(MWNamespace ns,Class<T> clazz) throws SQLException
		{
		ArrayList<T> items= new ArrayList<T>();
		for(Entry e: listEntries(ns))
			{
			items.add(clazz.cast(e));
			}
		return items;
		}
	
	public Collection<Page> listPages() throws SQLException
		{
		return listEntries(MWNamespace.Main,Page.class);
		}
	
	public Collection<User> listUsers() throws SQLException
		{
		return listEntries(MWNamespace.User,User.class);
		}
	public Collection<Category> listCategories() throws SQLException
		{
		return listEntries(MWNamespace.Category,Category.class);
		}
	//"create table MW.entry2cat(entry_id int not null,cat_id int not null,unique (entry_id,cat_id));"
	
	public Set<Category> listCategories(Entry entry) throws SQLException
		{
		Set<Category> items= new TreeSet<Category>();
		Number entryid= findEntryId(entry);
		if(entryid==null) return items;
		Connection con= getConnection();
		
		PreparedStatement pstmt= con.prepareStatement(
			"select MW.entry.name " +
			"from " +
				"MW.entry, " +
				"MW.entry2entry " +
			"where " +
				"MW.entry2entry.entry1_id=? and "+	
				"MW.entry2entry.entry2_id= MW.entry.id and "+
				"MW.entry.namespace=?"	
			);
		pstmt.setInt(1, entryid.intValue());
		pstmt.setInt(2, MWNamespace.Category.getId());
		ResultSet row= pstmt.executeQuery();
		while(row.next())
			{
			items.add(new Category(row.getString(1)));
			}
		recycleConnection(con);
		return items;
		}
	
	public void insertRevision(Revision rev) throws SQLException
		{
		int user_id= insertEntry(rev.getUser());
		int page_id= insertEntry(rev.getEntry());
		Connection con= getConnection();
		PreparedStatement pstmt= con.prepareStatement(
			"insert into MW.revision(mwuser_id,page_id,when,size,revcomment,id) values" +
			"(?,?,?,?,?,?)"
			);
		pstmt.setInt(1, user_id);
		pstmt.setInt(2, page_id);
		pstmt.setTimestamp(3, new Timestamp(rev.getDate().getTime()));
		pstmt.setInt(4, rev.getSize());
		pstmt.setString(5, rev.getComment());
		pstmt.setInt(6, rev.getRevId());
		
		try
			{
			if(pstmt.executeUpdate()!=1) throw new SQLException("Cannot insert "+rev);
			}
		catch(SQLIntegrityConstraintViolationException e)
			{
			//just ignore for duplicate
			}
		con.close();
		}
	
	
	public void toRDF(PrintStream out,String base) throws SQLException,IOException
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
			
			

			out.println("  </Page>");
			}
		for(Page page:listPages())
			{
			for(Revision rev: listRevisions(page,null,null,null))
				{
				out.println("  <Revision rdf:ID=\"#rev"+rev.getRevId()+"\">");
				out.println("    <dc:date>"+XMLUtilities.escape(rev.getDate().toString())+"</dc:date>");
				out.println("    <page rdf:resource=\""+base+ "/"+ page.getQNameEncoded() +"\"/>");
				out.println("    <user rdf:resource=\""+base+ "/"+ rev.getUser().getQNameEncoded() +"\"/>");
				if(rev.getSize()>0) out.println("    <size>"+rev.getSize()+"</size>");
				out.println("    <comment>"+XMLUtilities.escape(rev.getComment())+"</comment>");
				out.println("  </Revision>");
				}
			}
		out.println("<rdf:RDF>");
		}
	
	public static void main(String[] args) {
		try {
			Statistics app= new Statistics();
			app.open(new File("/home/lindenb/tmp/derbydb"));
			app.clear();
			for(Revision r:new MWQuery().listRevisions(new Page("Rotavirus")))
				{
				app.insertRevision(r);
				}
			System.err.println(app.listPages());
			System.err.println(app.listUsers());
			app.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
