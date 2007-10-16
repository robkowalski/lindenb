package org.lindenb.bio;

public class NucleotideUtils extends BioUtils
	{
	public static final char  DEGENERATE_BASES[]={
		'W','S','R','Y','M','K',
		'B','D','H','V',
		'N'
		};
	
	
	public static String reverseComplement(CharSequence s)
		{
		StringBuilder b= new StringBuilder(s.length());
		for(int i=0;i< s.length();++i)
			{
			b.append(complement(s.charAt((s.length()-1)-i)));
			}
		return b.toString();
		}
	
	public static char complement(char b)
		{
		switch(b)
	        {
	        case 'A': return 'T';
	        case 'T': return 'A';
	        case 'G': return 'C';
	        case 'C': return 'G';
	        
	        case 'a': return 't';
	        case 't': return 'a';
	        case 'g': return 'c';
	        case 'c': return 'g';	        
	        
	        case 'w': return 'w';
	        case 'W': return 'W';
	        
	        case 's': return 's';
	        case 'S': return 'S';
	        
	        case 'y': return 'r';
	        case 'Y': return 'R';
	        
	        case 'r': return 'y';
	        case 'R': return 'Y';
	        
	        case 'k': return 'm';
	        case 'K': return 'M';
	        
	        case 'm': return 'k';
	        case 'M': return 'K';
	        
	        case 'b': return 'v';
	        case 'd': return 'h';
	        case 'h': return 'd';
	        case 'v': return 'b';
	        
	        
	        case 'B': return 'V';
	        case 'D': return 'H';
	        case 'H': return 'D';
	        case 'V': return 'B';
	        
	        case 'N': return 'N';
	        case 'n': return 'n';
	        
	        default: throw new IllegalArgumentException("bad base "+b);
	        }	
		}
	
	
	public static char[] degenerate2bases(char b)
		{
		switch(Character.toUpperCase(b))
	        {
	        case 'A': return new char[]{'A'};
	        case 'T': return new char[]{'T'};
	        case 'G': return new char[]{'G'};
	        case 'C': return new char[]{'C'};
		
		
	        case 'W': return new char[]{'A','T'};
	        case 'S': return new char[]{'G','C'};
	        case 'R': return new char[]{'A','G'};
	        case 'Y': return new char[]{'C','T'};
	        case 'M': return new char[]{'A','C'};
	        case 'K': return new char[]{'G','T'};


	        case 'B': return new char[]{'C','G','T'};
	        case 'D': return new char[]{'A','G','T'};
	        case 'H': return new char[]{'A','C','T'};
	        case 'V': return new char[]{'A','C','G'};

	        case 'N': return new char[]{'A','C','G','T'};

	        default: throw new IllegalArgumentException("bad base \""+b+"\"");
	        }
		}

}
