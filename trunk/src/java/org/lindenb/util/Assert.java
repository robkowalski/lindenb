package org.lindenb.util;

/** Testing utility class */
public class Assert
{
protected Assert() {}

protected static StackTraceElement getStackTraceElement(int depth)
	{
	try {
		throw new Exception();
		}
	catch (Exception e) {
		return e.getStackTrace()[depth];
		}
	}

/** Asserts that this statement is never reached. */
public static void assertUnreachableStatement()
	{
	throw new AssertionError(getStackTraceElement(2)+":Assertion Unreached statement failed");
	}


/** Asserts that a object is null. */
public static void assertNull(Object o)
	{
	if(o==null) return;
	throw new AssertionError(getStackTraceElement(2)+":Assertion Null Failed with "+o);
	}

/** Asserts that an object is null */
public static void assertNotNull(Object o)
	{
	if(o!=null) return;
	throw new AssertionError(getStackTraceElement(2)+":Assertion NOT-Null Failed");
	}

/** Asserts that a condition is true. */
public static void assertTrue(boolean test)
	{
	if(test) return;
	throw new AssertionError(getStackTraceElement(2)+":Assertion Failed");
	}

/** Asserts that a condition is true. */
public static void assertTrue(String message,boolean test)
	{
	if(test) return;
	if(message==null) message="Assertion Failed";
	throw new AssertionError(getStackTraceElement(2)+":"+message);
	}

/** Asserts that a condition is false. */
public static void assertFalse(boolean test)
	{
	if(!test) return;
	throw new AssertionError(getStackTraceElement(2)+":Assertion Failed");
	}

/** Asserts that a condition is false. */
public static void assertFalse(String message,boolean test)
	{
	if(!test) return;
	if(message==null) message="Assertion Failed";
	throw new AssertionError(getStackTraceElement(2)+":"+message);
	}

}
