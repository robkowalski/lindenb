package org.lindenb.semanticapp;

import java.io.File;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.berkeley.binding.DocumentBinding;
import org.lindenb.sw.RDFException;
import org.w3c.dom.Document;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * RDFStore
 * @author pierre
 *
 */
public class RDFStore
	{
	private static Logger LOG=Logger.getLogger(RDFStore.class.getName());
	private DocumentBinding domBinding=null;
	private Environment environment=null;
	private Database uri2document=null;
	
	/**
	 * RDFStore
	 * @param cfg
	 * @throws RDFException
	 */
	public RDFStore(File directory)
		throws DatabaseException
		{
		LOG.info("opening rdfStore "+ directory);
		try {
			if(!directory.exists())
				{
				LOG.info("creating directory "+ directory);
				if(!directory.mkdir()) throw new DatabaseException("Cannot create "+directory);
				}
			else if(!directory.isDirectory())
				{
				if(!directory.mkdir()) throw new DatabaseException("Not a directory: "+directory);
				}
			DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
			f.setCoalescing(true);
			f.setNamespaceAware(true);
			f.setValidating(false);
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(false);
			f.setIgnoringElementContentWhitespace(true);
			DocumentBuilder docBuilder= f.newDocumentBuilder();
			this.domBinding= new DocumentBinding(docBuilder);

			EnvironmentConfig envCfg= new EnvironmentConfig();
			this.environment= new Environment(directory,envCfg);
			DatabaseConfig dbCfg= new DatabaseConfig();
			this.uri2document= this.environment.openDatabase(null, "uri2instance", dbCfg);
			}
		catch(DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "_ctor", err);
			close();
			throw err;
			}
		catch (Exception err)
			{
			LOG.throwing(getClass().getName(), "_ctor", err);
			close();
			throw new DatabaseException(err);
			}
		}
	
	/**
	 * get a document from its URI
	 * @param tx
	 * @param uri
	 * @return
	 * @throws RDFException
	 */
	public Document get(Transaction tx,String uri) throws DatabaseException
		{
		try {
			DatabaseEntry key=new DatabaseEntry();
			DatabaseEntry value=new DatabaseEntry();
			StringBinding.stringToEntry(uri, key);
			OperationStatus status=this.uri2document.get(tx, key,value,LockMode.DEFAULT);
			if(status==OperationStatus.SUCCESS) return this.domBinding.entryToObject(value);
			return null;
			}
		catch (DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "get", err);
			throw err;
			}
		}
	
	/**
	 * delete a document from it uri
	 * @param tx
	 * @param uri
	 * @return
	 * @throws RDFException
	 */
	public boolean delete(Transaction tx,String uri) throws DatabaseException
		{
		try {
			DatabaseEntry key=new DatabaseEntry();
			StringBinding.stringToEntry(uri, key);
			OperationStatus status=this.uri2document.delete(tx, key);
			if(status==OperationStatus.SUCCESS) return true;
			return false;
			}
		catch (DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "remove", err);
			throw err;
			}
		}
	
	public boolean put(
			Transaction tx,
			String uri,
			Document dom
			) throws DatabaseException
		{
		try {
			DatabaseEntry key=new DatabaseEntry();
			DatabaseEntry value=new DatabaseEntry();
			StringBinding.stringToEntry(uri, key);
			this.domBinding.objectToEntry(dom, value);
			OperationStatus status=this.uri2document.put(tx, key, value);
			if(status==OperationStatus.SUCCESS) return true;
			return false;
			}
		catch (DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "putDocument", err);
			throw err;
			}
		}
	/**
	 * Close the model
	 */
	public synchronized void close()
		{
		LOG.info("closing rdfStore");
		try { if(uri2document!=null) uri2document.close(); }
		catch(DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "close", err);
			}
		
		try { if(environment!=null) environment.close(); }
		catch(DatabaseException err)
			{
			LOG.throwing(getClass().getName(), "close", err);
			}
		uri2document=null;
		environment=null;
		}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
		}
	
	}
