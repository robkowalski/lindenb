package org.lindenb.net;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.lindenb.io.IOUtils;
import org.lindenb.util.Pair;

public abstract class HttpLight
    {
    /** listening port */
    private int port=8080;
    /** host */
    private String host="localhost";
    private int maxSessionCount=50;
    private long sessionTimeoutMillisec=60*10*1000;
    private boolean closing=false;
    private long maximumInputLength=Long.MAX_VALUE;
    private int connectionSoTimeOut=15*1000;
    
    private static class Cookie
    	{
    	private String name;
    	private String value;
    	private String comment;
    	private String domain;
    	//private int maxAge=-1;
    	private String path;
    	public Cookie(String name,String value)
    		{
    		this.name=name;
    		this.value=value;
    		}
    	public String getName() {
			return name;
			}
    	
    	public String getValue() {
			return value;
			}
    	public String getComment() {
			return this.comment;
			}
    	public String getDomain() {
			return domain;
			}
    	public String getPath() {
			return path;
			}
    	
    	public void setDomain(String domain) {
			this.domain = domain;
		}
    	public void setPath(String path) {
			this.path = path;
		}
    	public void setComment(String comment) {
			this.comment = comment;
		}
    	
    	@Override
    	public int hashCode() {
    		return getName().hashCode();
    		}
    	
    	
    	@Override
    	public boolean equals(Object obj) {
    		if(obj==this) return true;
    		if(obj==null || !(obj instanceof Cookie)) return false;
    		Cookie cp=Cookie.class.cast(obj);
    		return getName().equals(cp.getName())
    			;
    		}
    	public String toString()
    		{
    		return getName()+"="+getValue();
    		}
    	}
    
    /** HttpBase */
    private class HttpBase
    	{
    	private Vector<Pair<String,String>> headers= new Vector<Pair<String,String>>();
        private HashSet<Cookie> cookies= new HashSet<Cookie>();
        
    	protected HttpBase()
    		{
    		
    		}
    	public Vector<Pair<String,String>> getHeaders()
	        {
	        return this.headers;
	        }
	    public HashSet<Cookie> getCookies()
	        {
	        return this.cookies;
	        }
	
	    public String getHeader(String key)
	        {
	        for(Pair<String,String> p: getHeaders())
	            {
	            if(p.first().equalsIgnoreCase(key)) return p.second();
	            }
	        return null;
	        }
	   
	    public String[] getHeaders(String key)
	        {
	        Vector<String> v= new Vector<String>();
	        for(Pair<String,String> p: getHeaders())
	            {
	            if(p.first().equalsIgnoreCase(key)) v.addElement( p.second());
	            }
	        return v.toArray(new String[v.size()]);
	        }
	   
	    public Set<String> getHeaderNames()
	        {
	        HashSet<String> set= new HashSet<String>();
	        for(Pair<String,String> p:getHeaders())
	            {
	            set.add(p.first());
	            }
	        return set;
	        }
	   
	   
	    public Cookie getCookie(String key)
	        {
	        for(Cookie p: getCookies())
	            {
	            if(p.getName().equals(key)) return p;
	            }
	        return null;
	        }
	   
	    public Cookie[] getCookies(String key)
	        {
	        Vector<Cookie> v= new Vector<Cookie>();
	        for(Cookie p: getCookies())
	            {
	            if(p.getName().equals(key)) v.addElement(p);
	            }
	        return v.toArray(new Cookie[v.size()]);
	        }
	   
	    public Set<String> getCookieNames()
	        {
	        HashSet<String> set= new HashSet<String>();
	        for(Cookie p:getCookies())
	            {
	            set.add(p.getName());
	            }
	        return set;
	        }
    	}
    
    private class HttpRequest extends HttpBase
    	{
    	private HttpResponse response=null;
    	private String requestMethod=null;
        private String requestURI=null;
        private String requestHttpVersion=null;
        private Vector<Pair<String,String>> requestParameters= new Vector<Pair<String,String>>();
        public HttpRequest()
        	{
        	
        	}
        
        public Vector<Pair<String,String>> getParameters()
	        {
	        return this.requestParameters;
	        }
	   
	    
	   
	   
	    public String getParameter(String key)
	        {
	        for(Pair<String,String> p: getParameters())
	            {
	            if(p.first().equals(key)) return p.second();
	            }
	        return null;
	        }
	
	    public String[] getParameters(String key)
	        {
	        Vector<String> v= new Vector<String>();
	        for(Pair<String,String> p: getParameters())
	            {
	            if(p.first().equals(key)) v.addElement( p.second());
	            }
	        return v.toArray(new String[v.size()]);
	        }
	   
	    public Set<String> getParameterNames()
	        {
	        HashSet<String> set= new HashSet<String>();
	        for(Pair<String,String> p:getParameters())
	            {
	            set.add(p.first());
	            }
	        return set;
	        }
	    
	    public HttpSession getSession() throws IOException
	        {
	        return getSession(true);
	        }
   
   
	    public HttpSession getSession(boolean create) throws IOException
	        {
	        Cookie cookie= getCookie(getSessionCookieName());
	        HttpSession session =null;
	        if(cookie!=null)
	            {
	            session=HttpLight.this.id2session.get(cookie.getValue());
	            }
	       
	        if(session!=null)
	            {
	            session.lastConnectionTime= System.currentTimeMillis();
	            }
	        else if(create)
	            {
	            if(HttpLight.this.id2session.size()+1> getMaxSessionCount())
	                {
	                throw new IOException("Too Many Connection");
	                }
	            session= createHttpSession();
	            HttpLight.this.id2session.put(session.id, session);
	            }
	        if(this.response!=null)
	        	{
	        	Cookie c=new Cookie(getSessionCookieName(),session.getId());
	        	this.response.getCookies().add(c);
	        	}
	        return session;
	        }
    	}
    
    private class HttpResponse extends HttpBase
		{
    	 private PrintStream outputStream;
    	 private PrintWriter writer=null;
    	 private int httpStatusCode=200;
    	 private String httpStatusMessage=getStatusMessage(200);
    	 private boolean http_header_written=false;
    	 private String contentType="text/plain";
    	 private Integer contentLength=null;
    	 
    	 private class PrintHttpHeader extends FilterOutputStream
    	 	{
    		PrintHttpHeader(OutputStream out)
    		 	{
    			super(out);
    		 	}
    		private void print(String s) throws IOException
    			{
    			write(s.getBytes());
    			}
    		
    		private void check() throws IOException
    			{
    			if(HttpResponse.this.http_header_written) return;
    			HttpResponse.this.http_header_written=true;
    			print("HTTP/1.1 "+httpStatusCode+" "+httpStatusMessage+"\r\n");
    			print("Content-Type: "+contentType+"\r\n");
    			if(contentLength!=null) print("Content-Length: "+contentLength+"\r\n");
    			for(Pair<String,String> p: getHeaders())
    				{
    				print(p.first()+": "+p.second()+"\r\n");
    				}
    			for(Cookie c: getCookies())
    				{
    				print("Set-Cookie: "+c.getName()+"="+c.getValue());
    				if(c.getComment()!=null)
						{
						print("; Comment="+c.getComment());
						}
    				if(c.getDomain()!=null)
    					{
    					print("; Domain="+c.getDomain());
    					}
    				if(c.getPath()!=null)
						{
						print("; Path="+c.getPath());
						}
    				print("\r\n");
    				}
    			print("\r\n");
    			flush();
    			
    			/*
    			 *  "Comment" "=" value
                 |       "Domain" "=" value
                 |       "Max-Age" "=" value
                 |       "Path" "=" value
                 |       "Secure"
                 |       "Version" "=" 1*DIGIT
    			 */
    			
    			
    			//Set-Cookie: GMAIL_RTT=EXPIRED; Domain=.google.com; Expires=Tue, 13-Mar-07
    			//21:03:04 GMT; Path=/mail
    			//Set-Cookie: GMAIL_LOGIN=EXPIRED; Domain=.google.com; Expires=Tue,
    			//13-Mar-07 21:03:04 GMT; Path=/mail
    			
    			
    			//		Content-Type: text/html\r\nSet-Cookie: "+getSessionCookieName()+"="+session.getId()+"\r\n\r\n"
    			}
    		
    		@Override
    		public void write(int b) throws IOException
    			{
    			check();
    			System.err.write(b);
    			super.out.write(b);
    			}
    		@Override
    		public void write(byte[] b) throws IOException {
    			check();
    			System.err.write(b);
    			super.out.write(b);
    			}
    		
    		@Override
    		public void write(byte[] buf, int off, int len) throws IOException
    			{
    			check();
    			System.err.write(buf, off, len);
    			super.out.write(buf, off, len);
    			}
    		
    	 	}
    	 
    	 public HttpResponse(OutputStream outputStream)
    	 	{
    		this.outputStream=new PrintStream(new PrintHttpHeader(outputStream));
    	 	}
    	 
    	 public PrintStream getOutputStream()
	         {
	         return this.outputStream;
	         }
    	 
    	 public PrintWriter getWriter()
    	 	{
    		if(this.writer==null)
    			{
    			this.writer=new PrintWriter(new OutputStreamWriter(this.outputStream));
    			}
    		return this.writer;
    	 	}
    	 
    	public	void sendError(int code)
    		{
    		sendError(code,getStatusMessage(code));
    		}
    	
    	public	void sendError(int code,String message)
			{
			this.httpStatusCode=code;
			if(message==null) message=getStatusMessage(code);
			this.httpStatusMessage=(message==null?"ERROR":message);
			}
    	
    	public void setContentType(String mime)
    		{
    		this.contentType=mime;
    		}
    	public void setContentLent(int length)
    		{
    		this.contentLength=length;
			}
		}
    
    
    
   
   
    
   private HashMap<String, HttpSession> id2session=new HashMap<String, HttpSession>();
   private String sessionCookieName="__session_id";
   
   
   
    protected  class HttpSession
        {
        private String id;
        private long creationTime=System.currentTimeMillis();
        private long lastConnectionTime= creationTime;
        private HashMap<String, Object> attributes= new HashMap<String, Object>();
        HttpSession()
            {
        	 do
	             {
	             this.id= createID(20);
	             } while(HttpLight.this.id2session.containsKey(this.id));
            }
       
        String getId()
            {
            return this.id;
            }
       
        public void destroy()
            {
           
            }
       
        public long duration()
            {
            return lastConnectionTime-creationTime;
            }
       
        public Object getAttribute(String key)
            {
            return this.attributes.get(key);
            }
       
        public void setAttribute(String key,Object value)
            {
            this.attributes.put(key, value);
            }
       
       
        }
   
   
    private static class FixedContentLength extends
        FilterReader
        {
        private int count=0;
        private int maxLen;
        private FixedContentLength(Reader delegate,int maxLen)
            {
            super(delegate);
            this.maxLen=maxLen;
            }
       
        @Override
        public int read() throws IOException
            {
            if(this.count>= maxLen)
                {
                return -1;
                }
            int c= super.read();
            if(c==-1) return c;
            System.err.print((char)c);
            this.count++;
            return c;
            }
       
        @Override
        public int read(char[] b, int off, int len) throws IOException {
            int n=0;
            for(int i=0;i< len;++i)
                {
                int c= this.read();
                if(c==-1)
                    {
                    if(n==0) return -1;
                    break;
                    }
                b[off+i]=(char)c;
                ++n;
                }
            return n;
            }
        }
   
   
    private class LimitedSizeInputStream extends
        FilterInputStream
        {
        private long count=0L;
        private LimitedSizeInputStream(InputStream delegate)
            {
            super(delegate);
            }
       
        @Override
        public int read() throws IOException
            {
            int c= super.read();
            if(c==-1) return c;
            System.err.print((char)c);
            this.count++;
            if(this.count>= HttpLight.this.getMaximumInputLength())
                {
                throw new IOException("Content Too lage "+this.count+" bytes");
                }
            return c;
            }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n=0;
            for(int i=0;i< len;++i)
                {
                int c= this.read();
                if(c==-1)
                    {
                    if(n==0) return -1;
                    break;
                    }
                b[off+i]=(byte)c;
                ++n;
                }
            return n;
            }
       
        }
   
   
    public HttpLight()
        {
       
        }
    protected String getSessionCookieName() {
        return sessionCookieName;
        }
   
   public int getSoTimeout()
   	{
	return this.connectionSoTimeOut;   
   	}
   public void setSoTimeOut(int connectionSoTimeOut) {
	this.connectionSoTimeOut = connectionSoTimeOut;
   	}
   
    public void setSessionTimeoutMillisec(long sessionTimeoutMillisec) {
        this.sessionTimeoutMillisec = sessionTimeoutMillisec;
        }
   
   
    public long getSessionTimeoutMillisec() {
        return sessionTimeoutMillisec;
        }
   
    public int getMaxSessionCount() {
        return maxSessionCount;
        }
   
    public void setMaxSessionCount(int maxSessionCount) {
        this.maxSessionCount = maxSessionCount;
        }
   
    public long getMaximumInputLength() {
        return maximumInputLength;
        }
   
    public void setMaximumInputLength(long maximumInputLength) {
        this.maximumInputLength = maximumInputLength;
        }
   
    public String getHost() {
        return host;
        }
   
    public void setHost(String host) {
        this.host = host;
        }
   
    public boolean isClosing() {
        return closing;
        }
   
    public void setClosing(boolean closing) {
        this.closing = closing;
        }
   
    public void setPort(int port) {
        this.port = port;
        }
   
    public int getPort() {
        return port;
        }
   
    public abstract void process(HttpRequest req,HttpResponse resp) throws IOException;
   
   
    private void run() throws IOException
        {
        ServerSocket serverSocket =  new ServerSocket(getPort(), 1,InetAddress.getByName(getHost()));
        cleanup();
        while(!isClosing())
            {
            Socket socket = null;
           
            InputStream input = null;
            try {
                //SocketChannel channel= SocketChannel.open();
                //channel.configureBlocking(false);
                //channel.connect(new InetSocketAddress(getHost(), getPort()));
                socket = serverSocket.accept();
                socket.setSoTimeout(getSoTimeout());
                input = socket.getInputStream();
               
                ///security limit input size
                input = new LimitedSizeInputStream(input);
                
                HttpResponse response= createHttpResponse( socket.getOutputStream());
                HttpRequest request= createHttpRequest();
                request.response=response;
                PrintStream out=  response.getOutputStream();
                parseRequest(request,input);
                
           
                try
	                {
	                process(request,response);
	                }
                catch(IOException err)
	                {
	                err.printStackTrace(out);
	                }
                
                IOUtils.safeFlush(out);
                IOUtils.safeClose(out);
                IOUtils.safeClose(input);
                socket.close();
                System.err.println("Done http");
                }
            catch (Throwable e) {
                e.printStackTrace();
                }
            cleanup();
            }
        }
   
   
    protected void cleanup()
        {
        for(Iterator<String> iter=id2session.keySet().iterator();iter.hasNext();)
            {
            String id= iter.next();
            HttpSession session= id2session.get(id);
            if(session.duration()> getSessionTimeoutMillisec())
                {
                session.destroy();
                iter.remove();
                }
            }
        }
   
   
    protected void parseRequest(HttpRequest request,InputStream in) throws IOException
        {
        boolean postMethod=false;
        String line;
        int nLine=0;
        line= readLine(in);
        if(line==null) throw new IOException("Bad HTTP Header");
        String tokens[]=line.split("[ \t]+");
        if(tokens.length!=3)
                {
                throw new IOException("Bad HTTP Header "+line);
                }
        request.requestMethod= tokens[0];

        if(tokens[0].equals("GET"))
                {
                postMethod=false;
                request.requestURI= tokens[1];
                request. requestHttpVersion=tokens[2];
                int loc=request.requestURI.indexOf('?');
                if(loc!=-1)
                    {
                    StringReader reader= new StringReader(request.requestURI.substring(loc+1));
                    parseParameters(request,reader);
                    }
                }
        else if(tokens[0].equals("POST"))
                {
                postMethod=true;
                request.requestURI= tokens[1];
                request.requestHttpVersion=tokens[2];
                }
        else
                {
                throw new IOException("Not a POST/GET "+line);
                }
       
       
        while((line=readLine(in))!=null)
                {
                if(line.length()==0)
                        {
                        ++nLine;
                        break;
                        }
                int loc= line.indexOf(':');
                if(loc==-1) throw new IOException(""+line+" is missing \":\"");
                Pair<String,String> p=new Pair<String,String>(
                        line.substring(0,loc).trim(),
                        line.substring(loc+1).trim()
                        );
                request.getHeaders().addElement(p);
               
                if(p.first().equalsIgnoreCase("Cookie"))
                    {
                    tokens=p.second().split("[;]");
                    for(String kv: tokens)
                        {
                        loc=  kv.indexOf('=');
                        Cookie c =new Cookie(
                                kv.substring(0,loc).trim(),
                                kv.substring(loc+1).trim()
                                );
                        request.getCookies().add(c);
                        }
                    }
                ++nLine;
                }
       
        if(postMethod)
            {
            String contentLengthStr= request.getHeader("Content-Length");
            if(contentLengthStr==null) throw new IOException("bad http header missing Content-Length");
            int contentLength=0;
            try {
                contentLength=Integer.parseInt(contentLengthStr);
                }
            catch (NumberFormatException e) {
                throw new IOException("Bad content-lenth "+contentLengthStr);
                }
           
            FixedContentLength r= new FixedContentLength(new InputStreamReader(in),contentLength);
            parseParameters(request,r);
            }
       
        /*
        while((line=readLine(in))!=null)
            {
             System.err.println(line);
            }*/
        System.err.println("EOF parseRequest");
        }
   
    
   
    protected HttpSession createHttpSession()
        {
        return new HttpSession();
       
        }
   
   
    protected void parseParameters(HttpRequest request,Reader reader) throws IOException
        {
        int c;
        while(true)
            {
            StringBuilder key= new StringBuilder();
            while((c=reader.read())!=-1)
                {
                if(c=='=' || c=='&') break;
                key.append((char)c);
                }
           
           
            StringBuilder value= new StringBuilder();
            if(c=='=')
                {
                while((c=reader.read())!=-1)
                    {
                    if(c=='&') break;
                    value.append((char)c);
                    }
                }
            if(value.length()==0 && key.length()==0) break;
            request.getParameters().addElement(new Pair<String,String>(
                    URLDecoder.decode(key.toString(), "UTF-8"),
                    URLDecoder.decode(value.toString(), "UTF-8")
                    ));
           
            }
        }
   
    protected static String createID(int len)
        {
        final String charPool="abcdefghijklmnopqrstuvwxyz_0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder b= new StringBuilder(len);
        Random rand= new Random(System.currentTimeMillis());
        while(b.length()<len)
            {
            b.append(charPool.charAt(rand.nextInt(charPool.length())));
            }
        return b.toString();
        }
   
    protected HttpRequest createHttpRequest()
    	{
    	return new HttpRequest();
    	}
    
    protected HttpResponse createHttpResponse(OutputStream out)
		{
		return new HttpResponse(out);
		}

   
    private static String readLine(InputStream from_client) throws IOException
        {
        StringBuilder buff= null;
        int c=0;
        while((c= from_client.read())!=-1)
                {
                if(buff==null) buff= new StringBuilder();
                if(c=='\n')
                    {
                    if(buff.length()>0 && buff.charAt(buff.length()-1)=='\r')
                        {
                        buff.setLength(buff.length()-1);
                        }
                    break;
                    }
                buff.append((char)c);
                }
        return buff==null?null:buff.toString();
        }
   
    
   
   
    public static String getStatusMessage(int status) {
        switch (status) {
            /* HTTP/1.1 RFC-2616 */
            case 100: return "Continue";
            case 101: return "Switching Protocols";
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 203: return "Non-Authoritative Information";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";
            case 300: return "Multiple Choices";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 305: return "Use Proxy";
            case 306: return "(Unused)";
            case 307: return "Temporary Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 402: return "Payment Required";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 406: return "Not Acceptable";
            case 407: return "Proxy Authentication Required";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 411: return "Length Required";
            case 412: return "Precondition Failed";
            case 413: return "Request Entity Too Large";
            case 414: return "Request-URI Too Long";
            case 415: return "Unsupported Media Type";
            case 416: return "Requested Range Not Satisfiable";
            case 417: return "Expectation Failed";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            case 505: return "HTTP Version Not Supported";

            /* DAV/1.0 RFC-2518 */
            case 102: return "Processing";
            case 207: return "Multi-Status";
            case 422: return "Unprocessable Entity";
            case 423: return "Locked";
            case 424: return "Failed Dependency";
            case 507: return "Insufficient Storage";
            /* Unknown */
            default:  return null;
        }
    }
   
   
    public static void main(String[] args) {
        try {
            HttpLight test= new HttpLight()
                {
                @Override
                public void process(HttpRequest req,HttpResponse resp) throws IOException
                    {
                    HttpSession session=req.getSession();
                    Integer i= (Integer)session.getAttribute("key");
                    if(i==null) i=1;
                    session.setAttribute("key", (i+1));
                    resp.setContentType("text/html");
                    resp.getOutputStream().println(
                            "<html><body>" +
                            "<h1>Session id="+session.getId()+"</h1>\n"+
                            "Hello World key"+session.getAttribute("key")+"\n"+System.currentTimeMillis()
                            +"\n"+resp.getCookies()+
                            "</body></html>"
                            );
                    }
                };
            test.setPort(8079);
            test.run();
            }
        catch (Exception e) {
            e.printStackTrace();
            }
        }
   
    }
 