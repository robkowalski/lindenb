package org.lindenb.util;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lindenb.me.Me;

public class AbstractApplication
	{
	/** logger */
	protected static final Logger LOG= Logger.getLogger("org.lindenb");
	
	protected AbstractApplication()
		{
		
		}
	
	/** print usage */
	protected void usage(PrintStream out)
        {
        out.println("General Options:");
        out.println(" -help (this screen)");
        out.println(" -log-level <level> default:"+LOG.getLevel());
        }

	 /** process one argument */
	 protected int processArg(String args[],int optind)
         {
         if(args[optind].equals("-help") ||
        	args[optind].equals("--help") ||
        	args[optind].equals("-?"))
                 {
                 System.out.println(Compilation.getName());
                 System.out.println(Me.FIRST_NAME+" "+Me.LAST_NAME+" "+Me.MAIL+" "+Me.WWW);
                 System.out.println(Compilation.getLabel());
                 usage(System.out);
                 System.exit(0);
                 }
         else if(args[optind].equals("-log-level"))
                 {
                 Level L=Level.parse(args[++optind]);
                 if(L==null) throw new IllegalArgumentException("Bad level:"+args[optind]);
                 LOG.setLevel(L);
                 return optind;
                 }
         return -1;
         }

	 /** process an array of arguments */
    protected int processArgs(String args[])
        {
        int optind=0;
        while(optind< args.length)
                {
                int n= processArg(args, optind);
                if(n!=-1)
                        {
                        optind=n;
                        }
                else if(args[optind].equals("--"))
                        {
                        optind++;
                        break;
                        }
                else if(args[optind].startsWith("-"))
                        {
                        System.err.println("Unknown option "+args[optind]);
                        System.err.println("type -help for more information");
                        System.exit(-1);
                        }
                else
                        {
                        break;
                        }
                ++optind;
                }
        return optind;
        }

	}
