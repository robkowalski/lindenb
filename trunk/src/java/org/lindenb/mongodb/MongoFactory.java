package org.lindenb.mongodb;

import java.net.UnknownHostException;

import com.mongodb.Mongo;

/** mongo factory. Creates a new @{link com.mongodb.Mongo} */
public interface MongoFactory
	{
	/** create a new Instance of Mongo */
	public Mongo newMongo() throws UnknownHostException;
	}
