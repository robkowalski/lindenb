package org.lindenb.semanticapp;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lindenb.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ControlServlet extends HttpServlet
	{
	private static final long serialVersionUID = 1L;
	/** document builder */
	private DocumentBuilder docBuilder=null;
	
	@Override
	public void init() throws ServletException
		{
		try {
			DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
			f.setCoalescing(true);
			f.setNamespaceAware(true);
			f.setValidating(false);
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(false);
			f.setIgnoringElementContentWhitespace(true);
			this.docBuilder= f.newDocumentBuilder();
			}
		catch (ParserConfigurationException e)
			{
			throw new ServletException(e);
			}

		}
	
	@Override
	public void destroy()
		{
		
		}
	
	protected RDFStore getRDFStore()
		{
		RDFStore rdfStore=(RDFStore)getServletContext().getAttribute("rdfstore.handle");
		if(rdfStore==null) throw new NullPointerException("no rdfStore");
		return rdfStore;
		}
	
	@Override
	protected void service(
			HttpServletRequest req,
			HttpServletResponse res
			)
			throws ServletException, IOException
		{
		String action= req.getParameter("action");
		
		
		try {
			Method method=null;
			if(action!=null && !(StringUtils.isIn(action, "service","doPost","doGet")))
				{
				try
					{
					method= getClass().getMethod(action, HttpServletRequest.class,HttpServletResponse.class);
					}
				catch (Exception e) {
					method=null;
					}
				}
			if(method!=null)
				{
				method.invoke(this, req,res);
				return;
				}
			RequestDispatcher dispatcher =req.getRequestDispatcher("");
			dispatcher.forward(req, res);
			}
		catch (ServletException err) {
			throw err;
			}
		catch (IOException err) {
			throw err;
			}
		catch (Exception err) {
			throw new ServletException(err);
			}
		}
	
	public void validate(HttpServletRequest req,HttpServletResponse res)
		throws IOException,ServletException
		{
		String xml= req.getParameter("xml");
		Document dom=null;
		if(xml==null) throw new ServletException("xml is null");
		
		try {
			dom = this.docBuilder.parse(new InputSource(new StringReader(xml)));
			} 
		catch (SAXException e)
			{
			throw new IOException(e);
			}
		Element root= dom.getDocumentElement();
		if(root==null) throw new IOException("No Root");
		
		}
	}