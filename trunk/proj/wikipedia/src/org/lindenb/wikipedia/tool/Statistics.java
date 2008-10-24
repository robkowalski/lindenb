package org.lindenb.wikipedia.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.lindenb.lang.ResourceUtils;
import org.lindenb.sql.ConnectionStack;
import org.lindenb.sql.SQLUtilities;
import org.lindenb.wikipedia.api.Entry;
import org.lindenb.wikipedia.api.MWNamespace;
import org.lindenb.wikipedia.api.MWQuery;
import org.lindenb.wikipedia.api.Page;
import org.lindenb.wikipedia.api.Revision;
import org.lindenb.wikipedia.api.User;


public class Statistics
	{
	private static final String VERSION="1.0.0";
	private static final String JDBC_DRIVER_NAME="org.apache.derby.jdbc.EmbeddedDriver";
	private File dbFile;
	private ConnectionStack connectionsStack= new ConnectionStack()
		{
		@Override
		protected Connection createConnection() throws SQLException {
			Properties dbProperties=new Properties();
			dbProperties.setProperty("user", "anonymous");
			dbProperties.setProperty("password","");
			dbProperties.setProperty("create", "false");	
			Connection con=DriverManager.getConnection("jdbc:derby:"+dbFile,dbProperties);
			return con;
			}
		};
	
	
	public Statistics(File dbFile) throws SQLException
		{
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
				"create table MW.revision(id int not null generated always as identity ,mwuser_id int not null references MW.entry(id),page_id int not null references MW.entry(id),when timestamp not null,size int not null default 0,revcomment varchar(255));"
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
			Connection con= connectionsStack.getConnection();
			Statement stmt=con.createStatement();
			String version=SQLUtilities.selectOneValue(stmt.executeQuery("select version from MW.version"), String.class);
			connectionsStack.recycle(con);
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
		connectionsStack.close();
		Properties dbProperties= new Properties();
		dbProperties.setProperty("user", "anonymous");
		dbProperties.setProperty("password","");
		dbProperties.setProperty("shutdown", "true");
		try
			{
			DriverManager.getConnection("jdbc:derby:"+this.dbFile,dbProperties);
			} catch(Exception err) {}
		}
	
	
	public void clear() throws SQLException
		{
		Connection con= connectionsStack.getConnection();
		Statement stmt= con.createStatement();
		for(String s: new String[]{"MW.entry","MW.revision"})
			{
			stmt.executeUpdate("DELETE FROM "+s); 
			}
		stmt.close();
		connectionsStack.recycle(con);
		}
	
	
	
	public int insertEntry(Entry entry) throws SQLException
		{
		Number id=null;
		Connection con= connectionsStack.getConnection();
		while(true)
			{
			PreparedStatement pstmt= con.prepareStatement("select id from MW.entry where namespace=? and name=?");
			pstmt.setInt(1, entry.getNamespace().getId());
			pstmt.setString(2, entry.getLocalName());
			id= SQLUtilities.selectOneOrZeroValue(pstmt.executeQuery(), Number.class);
			if(id!=null) break;
			pstmt.close();
			pstmt= con.prepareStatement(
				"insert into MW.entry(namespace,name) values(?,?)");
			pstmt.setInt(1, entry.getNamespace().getId());
			pstmt.setString(2, entry.getLocalName());
			if(pstmt.executeUpdate()!=1) throw new SQLException("Cannot insert into MW.entry:"+entry);
			}
		connectionsStack.recycle(con);
		return id.intValue();
		}
	
	public Collection<Entry> listEntries(MWNamespace ns) throws SQLException
		{
		Connection con= connectionsStack.getConnection();
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
		connectionsStack.recycle(con);
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
	
	public void insertRevision(Revision rev) throws SQLException
		{
		int user_id= insertEntry(rev.getUser());
		int page_id= insertEntry(rev.getEntry());
		Connection con= connectionsStack.getConnection();
		PreparedStatement pstmt= con.prepareStatement(
			"insert into MW.revision(mwuser_id,page_id,when,size,revcomment) values" +
			"(?,?,?,?,?)"
			);
		pstmt.setInt(1, user_id);
		pstmt.setInt(2, page_id);
		pstmt.setTimestamp(3, new Timestamp(rev.getDate().getTime()));
		pstmt.setInt(4, rev.getSize());
		pstmt.setString(5, rev.getComment());
		con.close();
		}
	
	public static void main(String[] args) {
		try {
			Statistics app= new Statistics(new File("/home/pierre/tmp/derbydb"));
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
