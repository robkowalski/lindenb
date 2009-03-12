package org.lindenb.tinytools;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.util.C;
import org.lindenb.xml.XMLUtilities;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
/**
 * A stupid RDF Server
 * Just a test with com.sun.net.httpserver.*
 * @author pierre
 *
 */
public class StupidRDFServer
    implements HttpHandler
    {
    private final static String CONTEXT="/rdf";
    private final static String ACTION_ADD="Add Statement";
    private final static String ACTION_QUERY_N3="Query N3";
    private final static String ACTION_QUERY_RDF="Query RDF";
    private final static int PORT=8000;
    
    /**
     * A Simple RDF Statement
     * @author pierre
     *
     */
    private static class Statement
        {
    	/** subject of this statement */
        private URI subject;
        /** predicate of this statement */
        private URI predicate;
        /** value of this statement a String or a URI*/
        private Object value;
        
        
        boolean isLiteral()
            {
            return value.getClass()==String.class;
            }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + predicate.hashCode();
            result = prime * result + subject.hashCode();
            result = prime * result + value.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            Statement other = (Statement) obj;
            return  subject.equals(other.subject) &&
                    predicate.equals(other.predicate) &&
                    value.equals(other.value)
                    ;
            }


        @Override
        public String toString() {
            return "<"+subject+"> <"+predicate+"> "+(isLiteral()
                    ?"\""+C.escape(String.class.cast(value))+"\""
                    :"<"+value+"> ."
                    );
            }
        }
    /** the set of statements */
    private Set<Statement> statements= Collections.synchronizedSet(new HashSet<Statement>());
    
    /** handle http query */
    public void handle(HttpExchange http) throws IOException
        {
        Headers respHeader =  http.getResponseHeaders();
        PrintWriter out= new PrintWriter(http.getResponseBody());
        String query = http.getRequestURI().toString().substring(CONTEXT.length());
        if(!query.startsWith("?"))
            {
            respHeader.add("Content-Type", "text/html");
            http.sendResponseHeaders(200, 0);
            printForm(out,null);
            }
        else
            {
        	/** parse parameters */
            Properties params= new Properties();
            for(String s:query.substring(1).split("[&]"))
                {
                s=s.replace('+', ' ');
                int eq= s.indexOf('=');
                if(eq>0 && eq+1 < s.length())
                    {
                    params.setProperty(
                        s.substring(0,eq),
                        URLDecoder.decode(s.substring(eq+1),"UTF-8")
                        );
                    }
                }
            
            Statement stmt=new Statement();
            try {
                stmt.subject= parseURI(params.getProperty("S"));
                stmt.predicate= parseURI(params.getProperty("P"));
                //value is a URI
                if(!params.containsKey("isL"))
                    {
                    stmt.value= parseURI(params.getProperty("V"));
                    }
                //value is a literal
                else
                    {
                    stmt.value= params.getProperty("V");
                    //URLDecoder.decode(,"UTF-8");
                    if( stmt.value!=null &&
                    	stmt.value.toString().trim().length()==0)
                		{
                		stmt.value=null;
                		}   
                    }
                }
            catch (Throwable e) {
            	respHeader.add("Content-Type", "text/html");
                http.sendResponseHeaders(200, 0);
                printForm(out,e.getMessage());
                out.flush();
                out.close();
                return;
                }
            
            
            String action= params.getProperty("act", "");
            if(action.equals(ACTION_ADD) &&
                    stmt.subject!=null &&
                    stmt.predicate!=null &&
                    stmt.value!=null)
                {
                respHeader.add("Content-Type", "text/html");
                http.sendResponseHeaders(200, 0);
                boolean added=this.statements.add(stmt);
                printForm(out,(added?"Statement Added":"Statement already in model"));
                }
            else if(action.equals(ACTION_QUERY_N3))
                {
                respHeader.add("Content-Type", "text/plain");
                http.sendResponseHeaders(200, 0);
                synchronized(statements)
                	{
                	Iterator<Statement> iter= statements.iterator();
                	while(iter.hasNext())
                		{
                		Statement triple=iter.next();
                		if(
                                (stmt.subject==null || stmt.subject.equals(triple.subject)) &&
                                (stmt.predicate==null || stmt.predicate.equals(triple.predicate)) &&
                                (stmt.value==null || stmt.value.equals(triple.value))
                                )
                                {
                                out.println(triple);
                                }
                		}
                	}

                }
            else if(action.equals(ACTION_QUERY_RDF))
	            {
	            respHeader.add("Content-Type", "application/rdf+xml");
	            http.sendResponseHeaders(200, 0);
	            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	            out.println("<rdf:RDF xmlns:rdf=\""+RDF.NS+"\">");
	            synchronized(statements)
	            	{
	            	Iterator<Statement> iter= statements.iterator();
	            	while(iter.hasNext())
	            		{
	            		Statement triple=iter.next();
	            		if(
	    	                    (stmt.subject==null || stmt.subject.equals(triple.subject)) &&
	    	                    (stmt.predicate==null || stmt.predicate.equals(triple.predicate)) &&
	    	                    (stmt.value==null || stmt.value.equals(triple.value))
	    	                    )
	    	                    {
	    	                	out.println("<rdf:Statement>");
	    	                	out.println(" <rdf:subject rdf:resource=\""+URLEncoder.encode(triple.subject.toString(),"UTF-8")+"\"/>");
	    	                    out.println(" <rdf:predicate rdf:resource=\""+URLEncoder.encode(triple.predicate.toString(),"UTF-8")+"\"/>");
	    	                    if(triple.isLiteral())
	    	                    	{
	    	                    	out.println(" <rdf:object>"+XMLUtilities.escape(triple.value.toString())+"</rdf:object>");
	    	                    	}
	    	                    else
	    	                    	{
	    	                    	out.println(" <rdf:object rdf:resource=\""+URLEncoder.encode(triple.predicate.toString(),"UTF-8")+"\"/>");
	    	  	                   	}
	    	                    out.println("</rdf:Statement>");
	    	                    }
	            		}
	            	}
	            out.println("</rdf:RDF>");
	            }
            else
                {
                respHeader.add("Content-Type", "text/html");
                http.sendResponseHeaders(200, 0);
                printForm(out,"Unknown Action");
                }
            }
        
    
        out.flush();
        out.close();
          }

    private void printForm(PrintWriter out,String message)
        {
        out.print("<html><body><form action='"+CONTEXT+"' method='GET'>");
        if(message!=null) out.println("<p>"+message+"</p>");
        out.print("<fieldset><legend>Add Statement</legend>" +
                "<label for='S'>Subject</label>&nbsp;<input type='text' id='S' name='S' size='20' value=''/><br/>" +
                "<label for='P'>Predicate</label>&nbsp;<input type='text' id='P' name='P' size='20' value=''/><br/>" +
                "<label for='V'>Value</label> <input type='checkbox' id='isL' name='isL' checked='false' /><label for='isL'>Literal</label>&nbsp;<input type='text' id='V' name='V' size='20' value=''/><br/>" +
                "<input name='act' type='submit' value='"+ACTION_ADD+"'/>&nbsp;" +
                "<input  name='act' type='submit' value='"+ACTION_QUERY_N3+"'/>&nbsp;"+
                "<input  name='act' type='submit' value='"+ACTION_QUERY_RDF+"'/>"+
                "</fieldset>");
        out.print("</form></body></html>");
        }
    
    
    private static URI parseURI(String s) throws URISyntaxException
        {    
        if(s==null || s.trim().length()==0) return null;
        return new URI(s.trim());
        }
    
    public static void main(String[] args) 
          {
    		try
	    		{
			    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			    server.createContext(CONTEXT, new StupidRDFServer());
			    server.start();
	    		}
    		catch(IOException err)
    			{
    			err.printStackTrace();
    			}
          }
    }
