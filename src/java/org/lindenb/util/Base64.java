package org.lindenb.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

/***
 *  base64 utilities for encoding/decoding
 *  hacked somewhere on the net
 *  
 */

public class Base64
{
private final static String BASE64 =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	/***
	 *  Decodes BASE64 encoded string.
	 *  @param encoded string
	 *  @return decoded data as byte array
	 */
	public  static byte[] decode(String encoded)
		{
		try {
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			StringReader inReader= new StringReader(encoded);
			decode(inReader,out);
			return out.toByteArray();
			}
		catch (IOException e)
			{
			throw new RuntimeException(e);
			}
		}

/***
 *  Decodes BASE64 encoded string.
 *  @param encoded string
 *  @return decoded data as byte array
 */
public static void decode(Reader dataIn,OutputStream out) throws IOException 
	{
	byte output[] = new byte[3];
	int state=0;
	int alpha;
	
	while((alpha=dataIn.read())!=-1)
		{
	    byte c;

	    if (Character.isWhitespace(alpha))
	    	{
	    	continue;
	    	}
	    else if ((alpha >= 'A') && (alpha <= 'Z'))
			{
			c = (byte)(alpha - 'A');
			}
		else if ((alpha >= 'a') && (alpha <= 'z'))
			{
			c = (byte)(26 + (alpha - 'a'));
			}
		else if ((alpha >= '0') && (alpha <= '9'))
			{
			c = (byte)(52 + (alpha - '0'));
			}
	   	else if (alpha=='+')
	   		{
	   		c = 62;
	   		}
		else if (alpha=='/')
			{
			c = 63;
			}
	   	else if (alpha=='=')
	   		{
	   		break; // end
	   		}
		else
			{
			throw new IOException("Illegal character for Base64 "+alpha); // error
			}
	   
	
	    switch(state)
	        {   case 0: output[0] = (byte)(c << 2);
	        			state++;
	                    break;
	            case 1: output[0] |= (byte)(c >>> 4);
	                    output[1] = (byte)((c & 0x0F) << 4);
	        			state++;
	                    break;
	            case 2: output[1] |= (byte)(c >>> 2);
	                    output[2] =  (byte)((c & 0x03) << 6);
	        			state++;
	                    break;
	            case 3: output[2] |= c;
	                    out.write(output);
	        			state=0;
	                    break;
	        }
		} // for

if (alpha=='=') /* then '=' found, but the end of string */
        switch(state)
        {   
        case 2: out.write(output,0,1); break;
    	case 3: out.write(output,0,2); break;
    	default: break;
        }
out.flush();
} // decode

public static String encode(String s)
	{
	return encode(s.getBytes());
	}

public static String encode( byte data[])
	{
	try {
		ByteArrayInputStream in=new ByteArrayInputStream(data);
		StringWriter out= new StringWriter();
		encode(in,out);
		return out.toString();
		}
	catch (IOException e)
		{
		throw new RuntimeException(e);
		}
	}

/**
 *  Encodes binary data by BASE64 method.
 *  @param data binary data as byte array
 *  @return encoded data as String
 */
public static void encode(InputStream dataIn,Writer out) throws IOException
	{
	char output[] = new char[4];
	int restbits = 0;
	 int chunks = 0;
	int c;
	int nFill=0;
	
	while((c=dataIn.read())!=-1)
		{
		int ic = ( c >= 0 ? c : (c & 0x7F) + 128);
		//array3[nFill]=(byte)ic;
	   
	    switch (nFill)
	        {	
	        case 0:
	        	{
	        	output[nFill] = BASE64.charAt(ic >>> 2);
	            restbits = ic & 0x03;
	            nFill++;
	            break;
	        	}
	       case 1:
	    	    {
	    		output[nFill] = BASE64.charAt((restbits << 4) | (ic >>> 4));
	    	    restbits = ic & 0x0F;
	    	    nFill++;
	            break;
	    	    }
	       case 2:
	    	   	{
	    	   	output[nFill  ] = BASE64.charAt((restbits << 2) | (ic >>> 6));
	    	   	output[nFill+1] = BASE64.charAt(ic & 0x3F);
	            out.write(output);
	            // keep no more the 76 character per line
	            chunks++;
	            if ((chunks % 19)==0) out.write("\r\n");
	            nFill=0;
	            break;
	    	   	}
	        }
		} // for
	
		/* final */
		switch (nFill)
		{    case 1:
	         	 output[1] = BASE64.charAt((restbits << 4));
	             output[2] = output[3] = '=';
	             out.write(output);
	             break;
	         case 2:
	         	 output[2] = BASE64.charAt((restbits << 2));
	             output[3] = '=';
	             out.write(output);
	             break;
		}
	
	out.flush();
} // encode()


public static void main(String[] args)
	{
	try {
		if(args.length!=2)
			{
			System.err.println("args:\n\t(decode|encode) url");
			return;
			}

		if(args[0].equals("encode"))
			{
			InputStream in= new URL(args[1]).openStream();
			Base64.encode(in, new OutputStreamWriter(System.out));
			in.close();
			System.out.flush();
			}
		else if(args[0].equals("decode"))
			{
			Reader in= new InputStreamReader(new URL(args[1]).openStream());
			Base64.decode(in,System.out);
			in.close();
			System.out.flush();
			}
		else
			{
			throw new IllegalArgumentException("bad argument "+args[0]);
			}
		}
	catch (Throwable e)
		{
		e.printStackTrace();
		}
	}
  

}
