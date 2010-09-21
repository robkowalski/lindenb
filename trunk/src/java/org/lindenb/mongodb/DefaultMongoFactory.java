package org.lindenb.mongodb;

import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
/**
 * Default implementation of a MongoFactory
 *
 */
public class DefaultMongoFactory implements MongoFactory
	{
	private String host=ServerAddress.defaultHost();
	private int port=ServerAddress.defaultPort();

	public DefaultMongoFactory()
		{
		}
	
	
	public int getPort()
		{
		return port;
		}
	
	public void setPort(int port)
		{
		this.port = port;
		}
	
	public String getHost()
		{
		return host;
		}
	
	public void setHost(String host)
		{
		this.host = host;
		}
	
	@Override
	public Mongo newMongo()
	 	throws UnknownHostException
		{
		return new Mongo(
			getHost(),
			getPort()
			);
		}

	}
