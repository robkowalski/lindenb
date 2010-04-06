package org.lindenb.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

public abstract class ConnectionStack
{
//init function called once ?
private boolean inited=false;
//stack of connections
private Stack<Connection> connections=new Stack<Connection>();
public ConnectionStack()
	{
	
	}

/** called once before getConnection, give the ConnectionStack a chance to
 * initialize itself before the first connection, for example to load the SQLDriver . For example call
 * Class.forName("com.mysql.jdbc.Driver")
 */
protected void init()throws SQLException
	{
	
	}

@Override
/** calls <code>close</code> */
protected void finalize() throws Throwable {
	this.close();
	super.finalize();
	}

/** close all the connections and empties stack */
public synchronized void close()
	{
	while(!connections.isEmpty())
		{
		SQLUtilities.safeClose(connections.pop());
		}
	}

public synchronized Connection getConnection() throws SQLException
	{
	if(!inited)
		{
		//give a chance to initialize the driver
		init();
		inited=true;
		}
	while(!connections.isEmpty())
		{
		Connection con=connections.pop();
		if(con==null || con.isClosed()) continue;
		return con;
		}
	return createConnection();
	}

/** recycle the connection, push it into the stack */
public synchronized void recycle(Connection con)
	{
	try
		{
		if(con==null || con.isClosed()) return;
		con.clearWarnings();
		connections.push(con);
		}
	catch(SQLException err)
		{
			
		}
	}

/** create a new connection */
protected abstract Connection createConnection() throws SQLException;

@Override
protected Object clone() throws CloneNotSupportedException {
	return new CloneNotSupportedException("Cannot clone "+getClass());
	}
}
