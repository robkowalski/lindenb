/**
 * 
 */
package org.lindenb.semanticapp;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sleepycat.je.DatabaseException;

/**
 *
 * AppContextListener
 *
 */
public class AppContextListener implements ServletContextListener
	{
	@Override
	public void contextInitialized(ServletContextEvent event)
		{
		ServletContext ctx = event.getServletContext ();
		try
	        {
			String s= ctx.getInitParameter("rdfstore.home");
			if(s==null) throw new IllegalStateException("rdfstore.home was not defined");
			File dir= new File(s);
	        RDFStore rdfStore = new RDFStore(dir);
	        ctx.setAttribute ("rdfstore.handle", rdfStore);
	        ctx.log ("initialized RDFStore "+dir);
	        }
	   catch (DatabaseException e)
	        {
	        ctx.log ("Couldn't create RDFStore " + e.getMessage ());
	        }
		}
	
	
	
	@Override
	public void contextDestroyed(ServletContextEvent event)
		{
		ServletContext ctx = event.getServletContext ();
		ctx.log ("removing RDFStore ");
		RDFStore rdfStore = RDFStore.class.cast( ctx.getAttribute ("rdfstore.handle"));
		if(rdfStore!=null) rdfStore.close();
		ctx.removeAttribute("rdfstore.handle");
		}

	

}
