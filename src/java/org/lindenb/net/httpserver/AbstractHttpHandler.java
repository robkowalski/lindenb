package org.lindenb.net.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Cast;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


/**
 * AbstractHttpHandler
 * @author pierre
 *
 */
public abstract class AbstractHttpHandler
	implements HttpHandler
	{
	private static final Logger LOG= Logger.getLogger(HttpHandler.class.getName());
	/** HttpServer */
	private HttpServer server;
	
	/** a basic wrapper for request parameters */
	public static abstract class Parameters
		{
		/** returns all the parameters names */
		public abstract Set<String> getParameterNames();
		/** returns all the values for a given parameter */
		public abstract List<String> getParameters(String name);
		
		/** return a value for a given parameter or a default value */
		public String getParameter(String name,String defaultValue)
			{
			List<String> params= getParameters(name);
			return params.isEmpty()?defaultValue:params.get(0);
			}
		
		/** return a value for a given parameter or null */
		public String getParameter(String name)
			{
			return getParameter(name,null);
			}
		/** returns true if there is a parameter with this name */
		public boolean contains(String name)
			{
			return getParameter(name)!=null;
			}
		
		/** return true if the given paremeters is null or is blank */
		public boolean isEmpty(String name)
			{
			String value= getParameter(name);
			return value==null || value.trim().length()==0;
			}
		public Double getDouble(String name)
			{
			return Cast.Double.cast(getParameter(name));
			}
		public Float getFloat(String name)
			{
			return Cast.Float.cast(getParameter(name));
			}
		public Integer getInt(String name)
			{
			return Cast.Integer.cast(getParameter(name));
			}
		
		public Long getLong(String name)
			{
			return Cast.Long.cast(getParameter(name));
			}
		public Short getShort(String name)
			{
			return Cast.Short.cast(getParameter(name));
			}
		}
	
	protected static class ParametersImpl extends Parameters
		{
		private Map<String, List<String>> paramMap= new HashMap<String, List<String>>();
		
		public Map<String, List<String>> getMap()
			{
			return this.paramMap;
			}
		
		@Override
		public Set<String> getParameterNames() {
			return new HashSet<String>(getMap().keySet());
			}
		@Override
		public List<String> getParameters(String name)
			{
			List<String> values= getMap().get(name);
			if(values==null) return new ArrayList<String>(0);
			return new ArrayList<String>(values);
			}
		void add(String name,String value)
			{
			List<String> v= this.paramMap.get(name);
			if(v==null)
				{
				v=new ArrayList<String>();
				this.paramMap.put(name,v);
				}
			v.add(value==null?"":value);
			}
		}
	
	
	private class MaxInputStream
		extends FilterInputStream
		{
		private long curr_size=0L;
		
		MaxInputStream(InputStream in)
			{
			super(in);
			}
		
		}
	
	
	public AbstractHttpHandler()
		{
		this(null);
		}
	
	public AbstractHttpHandler(HttpServer server)
		{
		this.server=server;
		}
	
	public HttpServer getServer()
		{
		return server;
		}
	
	public abstract void service(HttpExchange exchange) throws IOException;
	
	public void init() throws IOException
		{
		}
	
	public void release()
		{
		
		}
	
	public void handle(HttpExchange exchange) throws IOException
		{
		try {
			init();
			service(exchange);
			} 
		catch (IOException e)
			{
			throw e;
			}
		catch (Throwable e) {
			throw new IOException(e);
			}
		finally
			{
			release();
			}
		}
	
	public Parameters getParameters(HttpExchange http) throws IOException
		{
		ParametersImpl params= new ParametersImpl();
		String query=null;
		if(http.getRequestMethod().equalsIgnoreCase("GET"))
			{
			query= http.getRequestURI().getRawQuery();
			}
		else if(http.getRequestMethod().equalsIgnoreCase("POST"))
			{
			InputStream in= new MaxInputStream(http.getRequestBody());
			if(in!=null)
				{
				ByteArrayOutputStream bytes =new ByteArrayOutputStream();
				IOUtils.copyTo(in, bytes);
				query= new String(bytes.toByteArray());
				in.close();
				}
			}
		
		if(query!=null)
			{
			for(String s:query.substring(1).split("[&]"))
	            {
	            s=s.replace('+', ' ');
	            int eq= s.indexOf('=');
	            if(eq>0)
	                {
	                params.add(
	                	URLDecoder.decode(s.substring(0,eq),"UTF-8"),
	                    URLDecoder.decode(s.substring(eq+1),"UTF-8")
	                    );
	                }
	            }
			}
		
		return params;
		}
	
	
	protected long getMaxLengthInput()
		{
		return Integer.MAX_VALUE;
		}
	
	
	
	
	protected void log(Object o)
		{
		if(LOG.getLevel()==Level.OFF) return;
		try {
			throw new Exception();
			}
		catch (Exception e)
			{
			LOG.log(LOG.getLevel(),getClass().getName()+":"+String.valueOf(o));
			}
		}
	
	protected void log()
		{
		if(LOG.getLevel()==Level.OFF) return;
		try {
			throw new Exception();
			}
		catch (Exception e)
			{
			LOG.log(LOG.getLevel(),getClass().getName());
			}
		}
	}
