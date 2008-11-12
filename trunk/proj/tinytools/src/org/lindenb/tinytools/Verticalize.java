package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.lindenb.me.Me;
import org.lindenb.util.Compilation;

public class Verticalize
	{
	private Pattern delim=Pattern.compile("[\t]");
	private boolean firstLineIsHeader=true;
	
	private Verticalize()
		{
		
		}
	
	private void run(BufferedReader in,PrintStream out) throws IOException
		{
		String header[]=null;
		String token[]=null;
		String line;
		int nLine=0;
		if(firstLineIsHeader)
			{
			line=in.readLine();
			++nLine;
			if(line==null) return;
			header=delim.split(line);
			}
		
		while((line=in.readLine())!=null)
			{
			++nLine;
			token=delim.split(line);
			out.println(">>"+nLine);
			for(int i=0;i< token.length;++i)
				{
				out.print("$"+(i+1)+" ");
				if(firstLineIsHeader && i< header.length)
					{
					out.print(header[i]);
					}
				else
					{
					out.print("?");
					}
				out.print(" : ");
				out.print(token[i]);
				out.println();
				}
			for(int i=token.length;firstLineIsHeader && i< header.length;++i)
				{
				out.print("$"+(i+1)+" ");
				out.print(header[i]);
				out.print(" : ");
				out.print("!N/A");
				out.println();
				}
			out.println("<<"+nLine);
			}
		
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Verticalize app= new Verticalize();
	try {
		int optind=0;
		String regex=null;
	    while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println("Pierre Lindenbaum PhD. "+Me.MAIL);
				System.err.println(Compilation.getLabel());
				System.err.println("-h this screen");
				System.err.println("-d <regex> split pattern default:TAB");
				System.err.println("-n first line is not the header");
				return;
				}
			else if(args[optind].equals("-d"))
				{
				System.err.println("Pierre Lindenbaum PhD.");
				System.err.println("-h this screen");
				return;
				}
			else if(args[optind].equals("-n"))
				{
				app.firstLineIsHeader=false;
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
	   if(regex!=null) app.delim= Pattern.compile(regex);
	   if(optind==args.length)
	   	{
		app.run(new BufferedReader(new InputStreamReader(System.in)),System.out);   
	   	}
	   else
	   	{
		while(optind<args.length)
			{
			BufferedReader r= new BufferedReader(new FileReader(args[optind++]));
			app.run(r,System.out);
			r.close();
			}
	   	}
	   
	   
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}
