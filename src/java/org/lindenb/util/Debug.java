package org.lindenb.util;

import java.io.PrintStream;

/** Testing utility class */
public class Debug
{
protected Debug() {}

private static PrintStream out=System.err;
private static boolean debugging=false;

public static void setDebugging(boolean debugging) {
	Debug.debugging = debugging;
	}

public static boolean isDebugging() {
	return debugging;
	}

public static void setDebuggingStream(PrintStream out)
	{
	if(out==null) out=System.out;
	Debug.out=out;
	}


protected static String getStackTraceElement(int depth)
	{
	try {
		throw new Exception();
		}
	catch (Exception e) {
		StackTraceElement t= e.getStackTrace()[depth];
		
		return (t.getFileName()==null?"":t.getFileName()+":")+
				(t.getLineNumber()==-1?"":t.getLineNumber()+":")+
				t.getMethodName();
		}
	}

public static void debug()
	{
	if(!isDebugging()) return;
	synchronized (out)
		{
		out.println("[DEBUG]"+getStackTraceElement(2));
		out.flush();
		}
	}

public static void debug(Object o)
	{
	if(!isDebugging()) return;
	synchronized (out)
		{
		out.print("[DEBUG]"+getStackTraceElement(2)+" : ");
		if(o instanceof Throwable)
			{
			Throwable.class.cast(o).printStackTrace(out);
			}
		else
			{
			out.print(o);
			}
		out.println();
		out.flush();
		}
	}

}
