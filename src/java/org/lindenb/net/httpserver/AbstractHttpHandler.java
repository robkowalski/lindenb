package org.lindenb.net.httpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lindenb.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public abstract class AbstractHttpHandler
	implements HttpHandler
	{
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
	

	
	public abstract void handle(HttpExchange exchange) throws IOException;
	
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
			InputStream in= http.getRequestBody();
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
	
	}
