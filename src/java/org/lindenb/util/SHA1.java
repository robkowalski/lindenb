package org.lindenb.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** public methods encrypting a string as SHA1 */
public class SHA1
{
private static final String ALGORITHM="SHA-1";
private static final String ENCODING="UTF-8";
public static boolean hasSHA()
	{
	try {
		MessageDigest.getInstance(ALGORITHM);
		return true;
		}
	catch (NoSuchAlgorithmException err)
		{
		return false;
		}
	}
	
public static String encrypt(String string)
	{
	byte[] hash = null;
	try {
		hash = MessageDigest.getInstance(ALGORITHM).digest(string.getBytes(ENCODING));
		}
	catch (NoSuchAlgorithmException err)
		{
		throw new Error("no "+ALGORITHM+" support in this VM");
		}
	catch (UnsupportedEncodingException err)
		{
		throw new Error("no "+ENCODING+" support in this VM");
		}
	StringBuilder hashString = new StringBuilder(42);

	for (int i = 0; i < hash.length; ++i)
		{
	    String x = Integer.toHexString(hash[i]&0xff);
	    if (x.length()<2) hashString.append("0");
	    hashString.append(x);
		}

	return hashString.toString();
	}
public static void main(String[] args)
	{
	if(!hasSHA())
		{
		System.err.println("Doesn't support "+ALGORITHM);
		System.exit(-1);
		}
	
	for(String arg: args)
		{
		System.out.println(encrypt(arg));
		}
	}
}
