package org.lindenb.tinytools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lindenb.sql.SQLUtilities;
import org.lindenb.util.Compilation;

/** parse HTML file given by eigenfactor.org
 * e.g. "http://www.eigenfactor.org/results.php?fulljournalname1=&nam=names&issnnumber=&ordering=perarticle&finecat=%25&rosvcat=%25&pub=&resultsperpage=8300&year=2006&grping=%25&Submit=Search
 * */
public class EigenFactor01 {
private EigenFactor01()
	{
	
	}

private void parse(BufferedReader in) throws IOException
	{
	final String TOKEN1="<td align=\"left\" valign=\"bottom\"><p align=\"center\"><font size=\"1\">";
	final String TOKEN2="ISSN:";
	String line;
	int index=-1;
	String issn=null;
	String influence=null;
	String eigen=null;
	while((line=in.readLine())!=null)
		{

		if(line.contains("javascript:popUp(\'detail.php"))
			{
			issn=null;
			influence=null;
			eigen=null;
			}
		else if((index=line.indexOf(TOKEN2))!=-1)
			{
			int j= line.indexOf("<br>",index+TOKEN2.length());
			issn=line.substring(index+TOKEN2.length(),j).trim();
			}
		else if((index=line.indexOf(TOKEN1))!=-1)
			{
			int j= line.indexOf("</font></p>",index+TOKEN1.length());
			String s=line.substring(index+TOKEN1.length(),j).trim();
			if(influence==null)
				{
				influence=s;
				}
			else
				{
				eigen=s;
				if(issn!=null
					&& !issn.equals("****-****")
					&& !eigen.equals("N/A"))
					{
					System.out.println(
						"update journals set influence="+influence+","+
						"eigenFactor="+eigen+" where issn=\""+
						SQLUtilities.escape(issn)+
						"\";"
						);
					}
				issn=null;
				influence=null;
				eigen=null;
				}
			}
		}
	}

public static void main(String[] args)
	{
	EigenFactor01 app= new EigenFactor01();
	int optind=0;
	while(optind<args.length)
        {
        if(args[optind].equals("-h"))
           {
        	System.err.println(Compilation.getLabel());
        	System.err.println(" parse HTML file given by eigenfactor.org\n."+
        		"e.g. http://www.eigenfactor.org/results.php?fulljournalname1=&nam=names&issnnumber=&ordering=perarticle&finecat=%25&rosvcat=%25&pub=&resultsperpage=8300&year=2006&grping=%25&Submit=Search\n"
        			);
        	System.err.println("\t-h this screen");
        	System.err.println("html file");
        	System.err.println();
			return;
           	}
        else if(args[optind].equals("--"))
            {
            ++optind;
            break;
            }
        else if(args[optind].startsWith("-"))
            {
            throw new IllegalArgumentException("Unknown option "+args[optind]);
            }
        else
            {
            break;    
            }
        //++optind;
        }
	try
	{
	//parse stdin
	if(args.length==optind)
		{
		app.parse(new BufferedReader(new InputStreamReader( System.in)));
		}
	//parse file
	else if(optind+1==args.length)
		{
		app.parse(new BufferedReader(new FileReader(args[optind])));
		}
	//too much files
	else
		{
		System.err.println("Illegal number of arguments");
		System.exit(-1);
		}
	} catch(IOException err)
		{
		err.printStackTrace();
		}
	}
}
