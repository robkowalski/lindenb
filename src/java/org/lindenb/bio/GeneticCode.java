package org.lindenb.bio;

public abstract class GeneticCode
	{
	private static final char STOP_CODON='*';
	private static final GeneticCode standard=  new StdGeneticCode();
	public abstract String getName();
	public abstract char translate(char a, char b,char c);
	public boolean isStopCodon(char c) { return c==STOP_CODON;}
	
	public static GeneticCode getStandard()
		{
		return standard;
		}
	
	private static class StdGeneticCode extends GeneticCode
		{
	    //static public AminoAcidImpl STOP= new AminoAcidImpl('*');

		@Override
	    public String getName()
		    {
		    return "Standard genetic code";
		    }

	    /**
	     * overides/implements parent
	     * 
	     */
		@Override
	   public char translate(char b1,char b2 ,char b3)
	           {
	           char c;

	           char c1=Character.toLowerCase(b1);
	           char c2=Character.toLowerCase(b2);
	           char c3=Character.toLowerCase(b3);
	           switch(c1)
	                   {
	                   case 'a':switch(c2)
	                           {
	                           case 'a':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='K';break;
	                                           case 't':c='N';break;
	                                           case 'g':c='K';break;
	                                           case 'c':c='N';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 't':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='I';break;
	                                           case 't':c='I';break;
	                                           case 'g':c='M';break;
	                                           case 'c':c='I';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'g':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='R';break;
	                                           case 't':c='S';break;
	                                           case 'g':c='R';break;
	                                           case 'c':c='S';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'c':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='T';break;
	                                           default: c='?';break;
	                                           }break;
	                           default: c='?';break;
	                           }break;
	                   case 't':switch(c2)
	                           {
	                           case 'a':
	                                   switch(c3)
	                                           {
	                                           case 'a':c=STOP_CODON;break;
	                                           case 't':c='Y';break;
	                                           case 'g':c=STOP_CODON;break;
	                                           case 'c':c='Y';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 't':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='L';break;
	                                           case 't':c='F';break;
	                                           case 'g':c='L';break;
	                                           case 'c':c='F';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'g':
	                                   switch(c3)
	                                           {
	                                           case 'a':c= STOP_CODON;break;
	                                           case 't':c='C';break;
	                                           case 'g':c='W';break;
	                                           case 'c':c='C';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'c':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='S';break;
	                                           default: c='?';break;
	                                           }break;

	                           default: c='?';break;
	                           }break;
	                   case 'g':switch(c2)
	                           {
	                           case 'a':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='E';break;
	                                           case 't':c='D';break;
	                                           case 'g':c='E';break;
	                                           case 'c':c='D';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 't':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='V';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'g':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='G';break;
	                                           default: c='?';break;
	                                           }break;
	                           case 'c':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='A';break;
	                                           default: c='?';break;
	                                           }break;
	                           default: c='?';break;
	                           }break;
	                   case 'c':switch(c2)
	                           {
	                           case 'a':
	                                   switch(c3)
	                                           {
	                                           case 'a':c='Q';break;
	                                           case 't':c='H';break;
	                                           case 'g':c='Q';break;
	                                           case 'c':c='H';break;
	                                           default: c='?';break;
	                                           }break;
	                           case 't':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='L';break;
	                                           default: c='?';break;
	                                           }break;

	                           case 'g':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='R';break;
	                                           default: c='?';break;
	                                           }break;
	                           case 'c':
	                                   switch(c3)
	                                           {
	                                           case 'a':
	                                           case 't':
	                                           case 'g':
	                                           case 'c':c='P';break;
	                                           default: c='?';break;
	                                           }break;
	                           default: c='?';break;
	                           }break;
	                   default: c='?';break;
	                   }
	           return c;
	           }



	}

	}
