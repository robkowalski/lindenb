package org.lindenb.tinytools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lindenb.util.Compilation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A SAX parser tool invoking Javascript functions as callback 
 * @author lindenb
 *
 */
public class SAXScript extends DefaultHandler
	{
	/** the JAVASCRIPT scripting engine */
	private ScriptEngine scriptEngine;
	/** map of boolean anwsers if a SAX callback method was implemented in javascript */
	private Map<String,Boolean> methodImplemented= new HashMap<String,Boolean>();
	private SAXScript(ScriptEngine  scriptEngine)
		{
		this.scriptEngine=scriptEngine;
		}
	
	private void invoke(String function,Object ... parameters) throws SAXException
		{
		if(methodImplemented.get(function)!=null) return ;
		try
			{
			Invocable.class.cast(scriptEngine).invokeFunction(function,parameters);
			}
		catch(NoSuchMethodException err)
			{
			methodImplemented.put(function, Boolean.FALSE);
			}
		catch(ScriptException err)
			{
			throw new SAXException(err);
			}
		}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException
		{
		invoke("characters",new String(ch,start,length));
		}

	@Override
	public void endDocument() throws SAXException
		{
		invoke("endDocument");
		}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		invoke("endElement",uri,localName,name);
		}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		invoke("endPrefixMapping",prefix);
		}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		invoke("ignorableWhitespace",new String(ch,start,length));
		
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		invoke("processingInstruction",target,data);
		
	}



	@Override
	public void skippedEntity(String name) throws SAXException {
		invoke("skippedEntity",name);
		
	}

	@Override
	public void startDocument() throws SAXException {
		invoke("startDocument");
		
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		invoke("startElement",uri,localName,name,atts);
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		invoke("startPrefixMapping",prefix,uri);
		
	}
	
	
	
	public static void main(String[] args) {
		try {
			boolean namespaceAware=true;
			boolean validating=false;
			Map<String,Object> properties4binding= new HashMap<String, Object>();
			int optind=0;
			File scriptFile=null;
			String scriptString=null;
		    while(optind<args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println("A SAX Parser paring one or more XML file and invoking some SAX CallBack written in javascript");
					System.err.println("Pierre Lindenbaum PhD.");
					System.err.println(Compilation.getLabel());
					System.err.println("-h this screen");
					System.err.println("-f <file> read javascript script from file");
					System.err.println("-e 'script' read javascript script from argument");
					System.err.println("-D <variable-name> <variable-value> add a variable (as string) in the scripting context");
					System.err.println("-n SAX parser is NOT namespace aware (default true)");
					System.err.println("-v SAX parser is validating (default false)");
					System.err.println("Script Example:\n");
					System.err.println("function startDocument()\n"+
					"\t{println(\"Start doc\");}\n"+
					"function endDocument()\n"+
					"\t{println(\"End doc\");}\n"+
					"function startElement(uri,localName,name,atts)\n"+
					"\t{\n"+
					"\tprint(\"\"+__FILENAME__+\" START uri: \"+uri+\" localName:\"+localName);\n"+
					"\tfor(var i=0;atts!=undefined && i< atts.getLength();++i)\n"+
					"\t\t{\n"+
					"\t\tprint(\" @\"+atts.getQName(i)+\"=\"+atts.getValue(i));\n"+
					"\t\t}\n"+
					"\tprintln(\"\");\n"+
					"\t}\n"+
					"function characters(s)\n"+
					"\t{println(\"Characters :\" +s);}\n"+
					"function endElement(uri,localName,name)\n"+
					"\t{println(\"END: uri: \"+uri+\" localName:\"+localName);}\n");
					return;
					}
				else if(args[optind].equals("-f"))
					{
					scriptFile= new File(args[++optind]);
					}
				else if(args[optind].equals("-e"))
					{
					scriptString= args[++optind];
					}
				else if(args[optind].equals("-n"))
					{
					namespaceAware=false;
					}
				else if(args[optind].equals("-v"))
					{
					validating=false;
					}
				else if(args[optind].equals("-D"))
					{
					properties4binding.put(args[++optind], args[++optind]);
					}
				 else if (args[optind].equals("--"))
				     {
				     ++optind;
				     break;
				     }
				else if (args[optind].startsWith("-"))
				     {
				     System.err.println("bad argument " + args[optind]);
				     System.exit(-1);
				     }
				else
				     {
				     break;
				     }
				++optind;
				}
		    
		 if(scriptFile==null && scriptString==null)
		 	{
			System.err.println("Undefined Script");
			return;
		 	}
		 if(scriptFile!=null && scriptString!=null)
		 	{
			System.err.println("options '-f' and '-e' both defined");
			return;
		 	}
		 ScriptEngineManager mgr=new  ScriptEngineManager();
         ScriptEngine scripEngine= mgr.getEngineByExtension("js");
         
         if(scripEngine==null)
         	{
        	System.err.println("Cannot get a javascript engine");
        	return;
         	}
         if(scriptFile!=null)
         	{
        	scripEngine.eval(new java.io.FileReader(scriptFile));
         	}
         else 
         	{
        	scripEngine.eval(new java.io.StringReader(scriptString));
         	}
        
         SAXParserFactory saxFactory= SAXParserFactory.newInstance();
         saxFactory.setNamespaceAware(namespaceAware);
         saxFactory.setValidating(validating);
         SAXParser parser= saxFactory.newSAXParser();
         
         SAXScript handler= new SAXScript(scripEngine);
         
         scripEngine.put("__ARGS__",args);
         for(String key: properties4binding.keySet())
         	{
        	scripEngine.put(key, properties4binding.get(key)); 
         	}
         
         if(optind==args.length)
	    	{
        	 scripEngine.put("__FILENAME__", "<STDIN>");
        	 parser.parse(System.in, handler);
	    	}
	    else
	    	{
	    	while(optind< args.length)
	    		{
	    		File f= new File(args[optind++]);
	    		scripEngine.put("__FILENAME__", f.toString());
	    		parser.parse(f, handler);
	    		}
	    	}
         
		}
		catch (ScriptException e) {
		System.err.println("Error (ScriptException):"+e.getMessage());
		}
	catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
