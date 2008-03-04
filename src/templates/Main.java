import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.lindenb.util.Compilation;

public class Main
{
private Pattern delim=Pattern.compile("[\t]");

private Main()
	{
	}

private void run(BufferedReader in) throws IOException
        {
        String line;
        while((line=in.readLine())!=null)
                {
                if(line.startsWith("#") || line.trim().length()==0) continue;
                String tokens[]=delim.split(line);
                }
        }


public static void main(String args[])
        {
        try
                {
                Main main= new Main();
                 int optind=0;
                 while (optind < args.length)
                {
                if (args[optind].equals("-h"))
                        {
			System.err.println(Compilation.getLabel());
	                System.err.println("-h this screen");
                        System.err.println("-d <pattern> delimiter default: TAB");
                        return;
                        }
                else if(args[optind].equals("-d"))
                        {
                        main.delim= Pattern.compile(args[++optind]);
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

                if(optind==args.length)
                        {
                        main.run(new BufferedReader(new InputStreamReader(System.in)));
                        }
                else
                        {
                        while(optind< args.length)
                                {
                                BufferedReader in= new BufferedReader(new FileReader(args[optind++]));
                                main.run(in);
                                in.close();
                                }
                        }
                }
        catch(Exception err)
                {
                err.printStackTrace();
                }

}



}
