package org.lindenb.wikipedia.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Statistics
	{
	private static final String JDBC_DRIVER_NAME="org.apache.derby.jdbc.EmbeddedDriver";
	private File dbFile;
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
			stmt.executeUpdate("create schema "+"");
			
			con.close();
			}
		}
	
	public void close()
		{
		Properties dbProperties= new Properties();
		dbProperties.setProperty("user", "anonymous");
		dbProperties.setProperty("password","");
		dbProperties.setProperty("shutdown", "true");
		try
			{
			DriverManager.getConnection("jdbc:derby:"+this.dbFile,dbProperties);
			} catch(Exception err) {}
		}
	
	
	public static void main(String[] args) {
		try {

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
