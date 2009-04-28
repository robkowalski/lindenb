package org.lindenb.bio;
/*
original code Copyright (c)
Whitehead Institute for Biomedical Research, Steve Rozen
(http://jura.wi.mit.edu/rozen), and Helen Skaletsky
*/
public class OligoTm {
static private final double OLIGOTM_ERROR=-999999.9999;
/* 
 * Tables of nearest-neighbor thermodynamics for DNA bases.
 * See Breslauer, Frank, Blocker, and Markey, 
 * "Predicting DNA duplex stability from the base sequence."
 * Proc. Natl. Acad. Sci. USA, vol 83, page 3746 (1986).
 * Article free at
 * http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=323600&blobtype=pdf
 * See table 2.
 */
private static final int neighbours[][][]=new int[][][]
  {
  /* S */
  new int[][]{
	  /*A */ new int[] { 240,173,208,239,215 },/* A C G T N */
	  /*C*/  new int[] { 129,266,278,208,220 },
	  /*G*/  new int[] { 135,267,266,173,210 },
	  /*T*/  new int[] { 169,135,129,240,168 },
	  /*N*/  new int[] { 168,210,220,215,203 }
      },
  /* H */
  new int[][]{
		  /*A */ new int[] { 91,65,78,86,80},/* A C G T N */
		  /*C*/  new int[] { 58,110,119,78,91 },
		  /*G*/  new int[] { 56,111,110,65,85},
		  /*T*/  new int[] { 60,56,58,91,66 },
		  /*N*/  new int[] { 66,85,91,80,80 }
  	},
  /* G */
  new int[][]{
  		  /*A */ new int[] {1900,1300,1600,1500,1575},/* A C G T N */
  		  /*C*/  new int[] {1900,3100,3600,1600,2550 },
  		  /*G*/  new int[] {1600,3100,3100,1300,2275},
  		  /*T*/  new int[] {900,1600,1900,1900,1575 },
  		  /*N*/  new int[] {1575,2275,2550,1575,1994 }
    	},
  };



private static int char2index(char base)
	{
	switch(base)
		{
		case 'a':case 'A': return 0;
		case 'c':case 'C': return 1;
		case 'g':case 'G': return 2;
		case 't':case 'T': return 3;
		default: return 4;
		}
	}




/** Return the delta G of the last len bases of oligo if oligo is at least len
bases long; otherwise return the delta G of oligo. */


/** Calculate the melting temperature of substr(seq, start, length) using the
formula from Bolton and McCarthy, PNAS 84:1390 (1962) as presented in
Sambrook, Fritsch and Maniatis, Molecular Cloning, p 11.46 (1989, CSHL
Press).

Tm = 81.5 + 16.6(log10([Na+])) + .41*(%GC) - 600/length

Where [Na+] is the molar sodium concentration, (%GC) is the percent of Gs
and Cs in the sequence, and length is the length of the sequence.

A similar formula is used by the prime primer selection program in GCG
(http://www.gcg.com), which instead uses 675.0 / length in the last term
(after F. Baldino, Jr, M.-F. Chesselet, and M.E.  Lewis, Methods in
Enzymology 168:766 (1989) eqn (1) on page 766 without the mismatch and
formamide terms).  The formulas here and in Baldino et al. assume Na+ rather
than K+.  According to J.G. Wetmur, Critical Reviews in BioChem. and
Mol. Bio. 26:227 (1991) 50 mM K+ should be equivalent in these formulae to .2
M Na+.

This function takes salt_conc to be the millimolar (mM) concentration,
since mM is the usual units in PCR applications.
*/

static public double long_seq_tm(CharSequence seq, int start, int len, double salt_conc)
	{
	  int GC_count=0;
	  for(int i=0;i< len;++i)
	  	{
		switch(seq.charAt(start+i))
			{
			case 'g':case 'c':
			case 'G':case 'C': ++GC_count;
			default:break;
			}
	  	}

	  return
	    81.5
	    + (16.6 * Math.log10(salt_conc / 1000.0))
	    + (41.0 * (((double) GC_count) / len))
	    - (600.0 / len);

	}


/** Return the melting temperature of the given oligo, as calculated using eqn
 (ii) in Rychlik, Spencer, Roads, Nucleic Acids Research, vol 18, no 21, page
 6410, with tables of nearest-neighbor thermodynamics for DNA bases as
 provided in Breslauer, Frank, Bloecker, and Markey,
 Proc. Natl. Acad. Sci. USA, vol 83, page 3748. */

public static double oligotm(CharSequence seq, 
		double DNA_nM,
		double K_mM)
	{
	return oligotm(seq,0,seq.length(),DNA_nM,K_mM);
	}


public static double oligotm(CharSequence seq, int start, int length, 
		double DNA_nM,
		double K_mM)
	{
	int dh = 0, ds = 108;
	
	if(length<2) return OLIGOTM_ERROR;
	
	int n=1;
	while(n<length)
		{
		dh+= neighbours[1][char2index(seq.charAt(start+n-1))][char2index(seq.charAt(start+n))];
		ds+= neighbours[0][char2index(seq.charAt(start+n-1))][char2index(seq.charAt(start+n))];
		++n;
		}
	
	double delta_H = dh * -100.0;  /* 
	     * Nearest-neighbor thermodynamic values for dh
	     * are given in 100 cal/mol of interaction.
	     */
	double delta_S = ds * -0.1;     /*
	      * Nearest-neighbor thermodynamic values for ds
	      * are in in .1 cal/K per mol of interaction.
	      */

	/* 
	* See Rychlik, Spencer, Rhoads,
	* "Optimization of the annealing temperature for
	* DNA amplification in vitro."
	* Nucleic Acids Research, vol 18, no 21, page 6409 (1990).
	* Article free at 
	* http://www.pubmedcentral.nih.gov/articlerender.fcgi?tool=pubmed&pubmedid=2243783
	* See eqn (ii).
	*/
	return delta_H / (delta_S + 1.987 *  Math.log(DNA_nM/4000000000.0))
	- 273.15 + 16.6 * Math.log10(K_mM/1000.0);
	}
/*
static private double oligodg(CharSequence seq)
	{
	return oligodg(seq,0,seq.length());
	}

static private double oligodg(CharSequence seq,int start,int length)
	{
	int dg=0;
	int n=1;
	if(length<2) return OLIGOTM_ERROR;
	while(n<length)
		{
		dg+= neighbours[2][char2index(seq.charAt(start+n-1))][char2index(seq.charAt(start+n))];
		++n;
		}
	return dg/1000.0;
	}*/


/** Return the delta G of disruption of oligo using the nearest neighbor model;
seq should be relatively short, given the characteristics of the nearest
neighbor model. */
/*
static private double end_oligodg(CharSequence oligo,int len)
	{
	int x = oligo.length();
	return x < len ? oligodg(oligo) : oligodg(oligo ,x - len,len);
	}*

/** Return the melting temperature of a given sequence, 'seq'. */
/*
static private double seqtm(CharSequence seq,
double dna_conc,
double salt_conc,
int    nn_max_len)
	{
	int len = seq.length();
	return (len > nn_max_len)
	  ? long_seq_tm(seq, 0, len, salt_conc) : oligotm(seq, dna_conc, salt_conc);
	}*/
public static final double DEFAULT_DNA_CONC=50;
public static final double DEFAULT_K_CONC=50;
/*
public static void main(String[] args) {
	System.out.println(oligotm("cttccatgcctggcccattg", 50,50 ));
	System.out.println(oligotm("acatggtgagaaggtgcctg", 50,50 ));
	}*/
}
