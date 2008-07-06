package org.lindenb.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * JSON Parser
 * builds a JSON tree from an input
 * @author pierre
 *
 */
public class Parser
	{
	private StreamTokenizer st;
	
	/** constructor */
	public Parser()
		{
		
		}
	
	/** create a new instance of builder */
	public static Parser newInstance()
		{
		return new Parser();
		}
	
	/** parse an inputstream */
	public Object parse(InputStream in) throws IOException
		{
		return parse(new InputStreamReader(in));
		}
	
	/** parse a string */
	public Object parse(String s) throws IOException
		{
		return parse(new StringReader(s));
		}
	
	/** parse a reader */
	public Object parse(Reader in) throws IOException
		{
		this.st= new StreamTokenizer(in);
		st.quoteChar('\'');
		st.quoteChar('\"');
		st.wordChars('_', '_');
	
		st.eolIsSignificant(false);
		st.parseNumbers();
		return nextElement();
		}
	
	@SuppressWarnings("fallthrough")
	private Object nextElement() throws IOException
		{
		int tt= this.st.nextToken();
	
		switch(tt)
			{
			
			case StreamTokenizer.TT_EOF: return null;
			case StreamTokenizer.TT_NUMBER: return createConstant(st.nval);
			case StreamTokenizer.TT_WORD:
				{
				if(this.st.sval.equals("null"))
					{
					return createConstant(null);
					}
				else if(this.st.sval.equals("true"))
					{
					return createConstant(Boolean.TRUE);
					}
				else if(this.st.sval.equals("false"))
					{
					return createConstant(Boolean.FALSE);
					}
				else throw new IOException("Illegal word:"+st.sval);
				}
			case '\"':  //threw
			case '\'': return createConstant(this.st.sval);
			case '[': return parseJSONArray();
			case '{': return parseJSONObject();
			default: throw new IOException("Syntax Error");
			}
		}
	
	
	//parse an array
	private Collection<Object> parseJSONArray() throws IOException
			{
	
			Collection<Object> json= createArray();
			int tt;
			while(true)
				{
				tt= st.nextToken();
	
				if(tt==']')
					{
					return json;
					}
				else if(json.isEmpty())
					{
					st.pushBack();
					json.add(nextElement());
					}
				else if(tt==',')
					{
					json.add(nextElement());
					}
				else
					{
					throw new IOException("Bad array "+json);
					}
				}
			}
	
	//parse an object
	private Map<String,Object> parseJSONObject() throws IOException
			{
			Map<String,Object> json= createObject();
			int tt;
			while(true)
				{
				tt=st.nextToken();
				if(tt=='}')
					{
					return json;
					}
				else if(tt=='\"' || tt=='\'' || tt==StreamTokenizer.TT_WORD)
					{
					String key= st.sval;
					if(!(tt=='\"' || tt=='\''))
						{
						if( key.equals("null") ||
							key.equals("false") ||
							key.equals("true"))
							{
							throw new IOException("Illegal key "+key);
							}
						}
					
					
					if(st.nextToken()!=':')
						{
						throw new IOException("Expected : after "+key);
						}
					
					json.put(key,nextElement());
					
	
					
					tt=st.nextToken();
					if(tt=='}') return json;
					if(tt!=',') throw new IOException("Expected , or }");
					}
				else
					{
					throw new IOException("Expected \'\"\' but found \'"+(char)tt+"\' line:"+st.lineno());
					}
				}
			}
	
	/** create a Collection. May be overloaded with extended builder */
	protected Collection<Object> createArray()
		{
		return new ArrayList<Object>();
		}
	
	/** create a Object May be overloaded with extended builder */
	protected Object createConstant(Object value)
		{
		if(value==null) return null;
		else if(Boolean.TRUE.equals(value)) return Boolean.TRUE;
		else if(Boolean.FALSE.equals(value)) return Boolean.FALSE;
		return new JSONConstant(value);
		}
	
	/** create a new JSONObject. May be overloaded with extended builder */
	protected Map<String,Object> createObject()
		{
		return new HashMap<String, Object>();
		}
	
	
	public static void main(String[] args) {
		try {
			String q1="{ 'status': '200 OK', 'qname': { 'status': '/mql/status/error', 'messages': [ { 'status': '/mql/status/result_error', 'info': { 'count': 100, 'result': [ { 'guid': [ '#9202a8c04000641f800000000000001f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000023' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000027' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000002b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000002f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000033' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000037' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000003b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000003f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000043' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000047' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000004b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000004f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000053' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000057' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000005b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000000a3' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000012' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000010' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000000ba' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000000be' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000000c2' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000000c6' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000053f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000553' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000559' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000055f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000565' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000056b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000583' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000669' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000066f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000675' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000067b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000681' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000687' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000068d' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000693' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000699' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000069f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000077f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000785' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000791' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000079d' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007a9' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007b5' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007c1' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007cd' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007d9' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007e5' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000000007f1' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000803' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000080f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000815' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000081b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000ab0' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000bd3' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000bd9' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000bdf' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000be5' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000beb' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000ca8' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cae' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cb4' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cba' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cc0' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cc6' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000ccc' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000cd2' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000db5' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000000dbb' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001237' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001243' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001249' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000124f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001255' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000125b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000130d' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001313' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000000001319' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f800000000000131f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047a8c' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047a99' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047ba4' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047bba' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047d88' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f8000000001047d8e' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c2ca7' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c2e9b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c2eb9' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c2ec0' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c3000' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c3172' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c319b' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c3213' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c3221' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c322f' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c323d' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c326e' ], 'type': '/type/type' }, { 'guid': [ '#9202a8c04000641f80000000010c3471' ], 'type': '/type/type' } ] }, 'path': '', 'query': { 'guid': [], 'type': '/type/type', 'error_inside': '.' }, 'message': 'Unique query may have at most one result. Got 100', 'type': '/mql/error' } ] }}";
			
			Object item=new Parser().parse(q1);
			System.err.println("ok "+item);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		}
}
