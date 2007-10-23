package org.lindenb.tool.cloneit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.lindenb.bio.NucleotideUtils;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.swing.table.AbstractGenericTableModel;
import org.lindenb.util.Algorithms;
import org.lindenb.util.Pair;
import org.lindenb.util.XObject;
import org.lindenb.util.iterator.AbstractIterator;
import org.lindenb.util.iterator.FilterIterator;
import org.lindenb.util.iterator.YIterator;

enum Orientation
	{
	Forward,Reverse
	}
	
enum CutType
	{
	BLUNT,OVERHANG_3, OVERHANG_5
	}
	
	
/**
 * Enzyme
 * @author pierre
 *
 */
class Enzyme
	extends XObject
	implements Comparable<Enzyme>
	{
	private String name;
	private String fullSite;
	private String site;
	private int pos3_5;
	private int pos5_3;
	private boolean palindromic=false;
	public Enzyme(String name,String fullSite)
		{
		this.name=name;
		this.fullSite=fullSite;
		this.site="";
		this.palindromic =fullSite.indexOf("^")!=-1;

        if(this.palindromic)
                {
                this.pos5_3= fullSite.indexOf("^");
                this.site = fullSite.substring(0,this.pos5_3).toUpperCase()+
                			fullSite.substring(this.pos5_3+1).toUpperCase()
                			;
                this.pos3_5 = this.site.length() - this.pos5_3;
                }
        else
                {
        		int var_parenthese_gauche= this.fullSite.indexOf('(');
        		int var_slash= this.fullSite.indexOf('/',var_parenthese_gauche+1);
        		int var_parenthese_droite= fullSite.indexOf(')',var_slash+1);
        		this.site= this.fullSite.substring(0,var_parenthese_gauche).toUpperCase();
        		
                this.pos5_3 = this.site.length() + Integer.parseInt(fullSite.substring(var_parenthese_gauche+1,var_slash));
                this.pos3_5 = this.site.length() + Integer.parseInt(fullSite.substring(var_slash+1,var_parenthese_droite));
                
                while(this.site.length()< Math.max(this.pos5_3,this.pos3_5) )
                        {
                		this.site+="N";
                        }
                }
		}
	
	public boolean isPalindromic()
		{
		return this.palindromic;
		}
	
	public int size()
		{
		return this.getSite().length();
		}
	

	
	public String getName() {
		return name;
		}
	
	public String getSite() {
		return site;
		}
	
	public String getFullSite() {
		return fullSite;
		}
	
	public int compareTo(Enzyme o) {
		if(o==this) return 0;
		return getName().compareTo(o.getName());
		}
	
	public String getURL()
	    {
	    StringBuilder b=new StringBuilder("http://rebase.neb.com?");
	    b.append(getName().replaceAll("[ ]", ""));
	    b.append(".html");
	    return b.toString();
	    }

	public char getCharAt(int pos,Orientation sens)
		{
		if(sens.equals(Orientation.Forward))
	        {
	        return(getSite().charAt(pos));
	        }
		 return complementary(getSite().charAt((size()-1)-pos));
		}
	
	public int getPos3_5(Orientation sens)
		{
		return (sens.equals(Orientation.Forward) ? 
				pos3_5 :
				(size()-1)-pos5_3
				);
		}
	public int getPos5_3(Orientation sens)
	    {
		return (sens.equals(Orientation.Forward) ? 
				pos5_3 :
				(size()-1)-pos5_3
				);
	    }
	public int getOverhang(Orientation sens)
	    {
	    return (getPos5_3(sens) - getPos3_5(sens));
	    }
	
	public int getOverhang()
	    {
	    return (getOverhang(Orientation.Forward)<0?getOverhang(Orientation.Reverse):getOverhang(Orientation.Forward));
	    }

	public boolean isSiteDegenerate()
	    {
		for(char c: new char[]{'N','Y','R','M','K','S','W','B','D','H','V'})
			{
			if( getSite().indexOf(c)!=-1) return true;
			}
	    return false;
	    }

	
	public CutType getType()
	    {
	    if(pos3_5==pos5_3) return(CutType.BLUNT);
	    else if(pos3_5<pos5_3) return(CutType.OVERHANG_3);
	    return(CutType.OVERHANG_5);
	    }

	
	@Override
	public boolean equals(Object obj) {
		return this==obj;
		}
	@Override
	public int hashCode() {
		return getFullSite().hashCode();
		}
	@Override
	public String toString() {
		return getName().toString();
		}
	
	static public boolean DNAcmp(char plasmid,char b)
	    {
		b= Character.toUpperCase(b);
	    switch( Character.toUpperCase(plasmid))
	            {
	            case 'A':return "ANDHVMWR".indexOf(b)!=-1;
	            case 'T':return "TNYBDHKW".indexOf(b)!=-1;
	            case 'G':return "GNRBDVKS".indexOf(b)!=-1;
	            case 'C':return "CNYBHVMS".indexOf(b)!=-1;
	            case 'N':return false;
	            default: return  false;
	            }
	    }

	static private char complementary(char c)
		{
        switch(Character.toLowerCase(c))
                {
                case 'a': return 'T';
                case 't': return 'A';
                case 'g': return 'C';
                case 'c': return 'G';
                case 'n': return 'N';

                case 'y': return 'R';
                case 'r': return 'Y';

                case 'm': return 'K';
                case 'k': return 'M';
                case 's': return 'S';
                case 'w': return 'W';

                case 'b': return 'V';
                case 'd': return 'H';
                case 'h': return 'D';
                case 'v': return 'B';
                case '*': return '*';
                default: return '?';
                }
		}
	
	}

class IntegerField extends JTextField
	{
	private static final long serialVersionUID = 1L;
	
	
	
	
	IntegerField()
		{
		super();
		setDocument(new PlainDocument()
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				StringBuilder b= new StringBuilder(str.length());
				for(int i=0;i< str.length();++i)
					{
					if(!Character.isDigit(str.charAt(i))) continue;
					b.append(str.charAt(i));
					}
				super.insertString(offs, b.toString(), a);
				}
			});
		setText("0");
		}
	public int getValue()
		{
		return Integer.parseInt(getText());
		}
	
	public void setValue(int v)
		{
		setText(String.valueOf(v));
		}
	
	}



class EnzymeList
	extends AbstractGenericTableModel<Enzyme>
	implements Iterable<Enzyme>,Cloneable
	{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final String COLS[]={"Use","Name","Site"};
	private Vector<Enzyme> enzymes=new Vector<Enzyme>();
	private HashMap<Enzyme, Boolean> enzymeUsage= new HashMap<Enzyme, Boolean>();
	
	static private final String RE_BASE[]={
		"AarI","CACCTGC(4/8)","AatII","GACGT^C","Acc36I","ACCTGC(4/8)","Acc65I","G^GTACC","AccBSI","CCGCTC(-3/-3)","AccI","GT^MKAC","AccII","CG^CG",
		"AciI","CCGC(-3/-1)","AcsI","R^AATTY","AfaI","GT^AC","AflII","C^TTAAG","AflIII","A^CRYGT","AluI","AG^CT","Alw21I","GWGCW^C","Alw26I","GTCTC(1/5)",
		"AlwNI","CAGNNN^CTG","Aor51HI","AGC^GCT","ApaI","GGGCC^C","ApaLI","G^TGCAC","AscI","GG^CGCGCC","AvaI","C^YCGRG","AvaII","G^GWCC","AviII","TGC^GCA",
		"BalI","TGG^CCA","BamHI","G^GATCC","BanII","GRGCY^C","BbeI","GGCGC^C","BbvCI","CCTCAGC(-5/-2)","BceAI","ACGGC(12/14)","BcnI","CC^SGG","BfiI","ACTGGG(5/4)",
		"BfrBI","ATG^CAT","BfuI","GTATCC(6/5)","BglI","GCCNNNN^NGGC","BglII","A^GATCT","BlnI","C^CTAGG","BpiI","GAAGAC(2/6)","Bpu10I","CCTNAGC(-5/-2)","Bpu1102I","GC^TNAGC",
		"BsaWI","W^CCGGW","BscBI","GGN^NCC","BseDI","C^CNNGG","BseGI","GGATG(2/0)","BseMI","GCAATG(2/0)","BseMII","CTCAG(10/8)","BseNI","ACTGG(1/-1)","BseRI","GAGGAG(10/8)",
		"BseSI","GKGCM^C","BseXI","GCAGC(8/12)","BsgI","GTGCAG(16/14)","Bsh1285I","CGRY^CG","Bsh1365I","GATNN^NNATC","BsiYI","CCNNNNN^NNGG","BsmFI","GGGAC(10/14)","BsmI","GAATGC(1/-1)",
		"Bsp120I","G^GGCCC","Bsp1286I","GDGCH^C","Bsp1407I","T^GTACA","BspPI","GGATC(4/5)","BssHII","G^CGCGC","BssKI","^CCNGG","Bst1107I","GTA^TAC","Bst2BI","CACGAG(-5/-1)",
		"BstAPI","GCANNNN^NTGC","BstBAI","YAC^GTR","BstDSI","C^CRYGG","BstSFI","C^TRYAG","BstXI","CCANNNNN^NTGG","BtrI","CACGTC(-3/-3)","BtsI","GCAGTG(2/0)","Cac8I","GCN^NGC",
		"Cfr10I","R^CCGGY","Cfr13I","G^GNCC","ClaI","AT^CGAT","CpoI","CG^GWCCG","Csp6I","G^TAC","CviTI","RG^CY","DdeI","C^TNAG","DpnI","GA^TC",
		"DraI","TTT^AAA","DraIII","CACNNN^GTG","DseDI","GACNNNN^NNGTC","EaeI","Y^GGCCR","Eam1104I","CTCTTC(1/4)","Eam1105I","GACNNN^NNGTC","EciI","GGCGGA(11/9)","Ecl136II","GAG^CTC",
		"Eco31I","GGTCTC(1/5)","Eco52I","C^GGCCG","Eco57I","CTGAAG(16/14)","Eco57MI","CTGRAG(16/14)","Eco64I","G^GYRCC","Eco81I","CC^TNAGG","EcoO109I","RG^GNCCY","EcoO65I","G^GTNACC",
		"EcoRI","G^AATTC","EcoRII","^CCWGG","EcoRV","GAT^ATC","EcoT14I","C^CWWGG","EcoT22I","ATGCA^T","EheI","GGC^GCC","Esp3I","CGTCTC(1/5)","FbaI","T^GATCA",
		"FokI","GGATG(9/13)","FseI","GGCCGG^CC","FspAI","RTGC^GCAY","GsuI","CTGGAG(16/14)","HaeII","RGCGC^Y","HaeIII","GG^CC","HgaI","GACGC(5/10)","HhaI","GCG^C",
		"Hin6I","G^CGC","HincII","GTY^RAC","HindIII","A^AGCTT","HinfI","G^ANTC","HpaI","GTT^AAC","HphI","GGTGA(8/7)","Hpy188I","TCN^GA","Hpy188III","TC^NNGA",
		"Hpy8I","GTN^NAC","Hpy99I","CGWCG^","HpyF44III","TG^CA","KasI","G^GCGCC","Kpn2I","T^CCGGA","KpnI","GGTAC^C","LweI","GCATC(5/9)","MabI","A^CCWGGT",
		"MaeII","A^CGT","MaeIII","^GTNAC","MboI","^GATC","MboII","GAAGA(8/7)","MflI","R^GATCY","MluI","A^CGCGT","MnlI","CCTC(7/6)","MseI","T^TAA",
		"Msp17I","GR^CGYC","MspA1I","CMG^CKG","MspI","C^CGG","MssI","GTTT^AAAC","MunI","C^AATTG","MvaI","CC^WGG","MwoI","GCNNNNN^NNGC","NaeI","GCC^GGC",
		"NarI","GG^CGCC","NcoI","C^CATGG","NdeI","CA^TATG","NgoAIV","G^CCGGC","NheI","G^CTAGC","NlaIII","CATG^","NmuCI","^GTSAC","NotI","GC^GGCCGC",
		"NruI","TCG^CGA","NspI","RCATG^Y","NspV","TT^CGAA","OliI","CACNN^NNGTG","PacI","TTAAT^TAA","PciI","A^CATGT","PinAI","A^CCGGT","PmaCI","CAC^GTG",
		"PpsI","GAGTC(4/5)","Ppu10I","A^TGCAT","PshAI","GACNN^NNGTC","PsiI","TTA^TAA","Psp1406I","AA^CGTT","Psp5II","RG^GWCCY","PstI","CTGCA^G","PvuI","CGAT^CG",
		"PvuII","CAG^CTG","RcaI","T^CATGA","SacI","GAGCT^C","SalI","G^TCGAC","SanDI","GG^GWCCC","SapI","GCTCTTC(1/4)","SatI","GC^NGC","ScaI","AGT^ACT",
		"SchI","GAGTC(5/5)","ScrFI","CC^NGG","SfiI","GGCCNNNN^NGGCC","SgfI","GCGAT^CGC","SgrAI","CR^CCGGYG","SmaI","CCC^GGG","SmiI","ATTT^AAAT","SmiMI","CAYNN^NNRTG",
		"SmlI","C^TYRAG","SmuI","CCCGC(4/6)","SnaBI","TAC^GTA","SpeI","A^CTAGT","SphI","GCATG^C","SrfI","GCCC^GGGC","Sse8387I","CCTGCA^GG","Sse9I","^AATT",
		"SspI","AAT^ATT","SstII","CCGC^GG","StuI","AGG^CCT","SunI","C^GTACG","TaaI","ACN^GT","TaiI","ACGT^","TaqI","T^CGA","TatI","W^GTACW",
		"TauI","GCSG^C","TfiI","G^AWTC","TseI","G^CWGC","TspRI","CASTGNN^","Tth111I","GACN^NNGTC","Van91I","CCANNNN^NTGG","VspI","AT^TAAT","XagI","CCTNN^NNNAGG",
		"XbaI","T^CTAGA","XcmI","CCANNNNN^NNNNTGG","XhoI","C^TCGAG","XmaI","C^CCGGG","XmnI","GAANN^NNTTC","XspI","C^TAG"
	};
	
	
	public static EnzymeList newEnzymeList()
		{
		EnzymeList L= new EnzymeList();
		for(int i=0;i< RE_BASE.length;i+=2)
			{
			Enzyme e=new Enzyme( RE_BASE[i+0], RE_BASE[i+1]);
			L.enzymes.add(e);
			L.enzymeUsage.put(e,true);
			}
		return L;
		}
	
	
	public EnzymeList()
		{
		
		}
	
	@SuppressWarnings("unchecked")
	EnzymeList(EnzymeList cp)
		{
		this();
		this.enzymes= (Vector<Enzyme>)cp.enzymes.clone();
		this.enzymeUsage= (HashMap<Enzyme, Boolean>)cp.enzymeUsage.clone();
		}
	
	@Override
	public Enzyme getElementAt(int rowIndex) {
		return this.enzymes.elementAt(rowIndex);
		}
	
	
	
	public Enzyme getEnzymeAt(int index)
		{
		return getElementAt(index);
		}
	
	public void addEnzyme( Enzyme e)
		{
		this.enzymes.add(e);
		fireTableRowsInserted(getEnzymeCount()-1, getEnzymeCount()-1);
		}
	
	public int getEnzymeCount()
		{
		return this.enzymes.size();
		}
	
	@Override
	public String getColumnName(int column) {
		return COLS[column];
		}
	
	public int getColumnCount() {
		return COLS.length;
		}

	public int getRowCount()
		{
		return getEnzymeCount();
		}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex==0;
		}
	
	@Override
	public Class<?> getColumnClass(int column) {
		switch(column)
			{
			case 0: return Boolean.class;
			case 1: return String.class;
			case 2: return String.class;
			}
		return null;
		}
	

	@Override
	public Object getValueOf(Enzyme e, int column) {
		switch(column)
			{
			case 0: return isEnzymeEnabled(e);
			case 1: return e.getName();
			case 2: return e.getFullSite();
			}
		return null;
		}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex==0)
			{
			Boolean b=(Boolean)aValue;
			setEnzymeEnabled(getEnzymeAt(rowIndex),b);
			fireTableCellUpdated(rowIndex, columnIndex);
			}
		
		}
	
	public boolean isEnzymeEnabled(Enzyme e)
		{
		return enzymeUsage.get(e);
		}
	
	public void setEnzymeEnabled(Enzyme e,boolean enabled) {
		enzymeUsage.put(e,enabled);
		}
	
	public Iterator<Enzyme> iterator() {
		return enzymes.iterator();
		}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new EnzymeList(this);
		}
	
	
	
	}


/**
 * Site
 * @author pierre
 *
 */
class Site extends XObject implements Comparable<Site>
	{
	private Plasmid seq;
	private Enzyme enzyme;
	private int loc;
	private Orientation orient;
	public Site(Plasmid seq,Enzyme enz,int loc,Orientation orient)
		{
		this.seq=seq;
		this.enzyme=enz;
		this.loc=loc;
		this.orient=orient;
		}
	
	
	
	public Enzyme getEnzyme() {
		return enzyme;
		}
	
	public Plasmid getSequence() {
		return seq;
		}
	
	public Orientation getOrientation()
		{
		return orient;
		}
	
	public int getPosition()
		{
		return this.loc;
		}
	
	public int end()
		{
		return getPosition()+getEnzyme().size();
		}
	
	public int compareTo(Site o)
		{
		assert(o!=null);
		if(o==this) return 0;
		assert(o.getSequence()==getSequence());
		return getPosition()-o.getPosition();
		}
	
	@Override
	public boolean equals(Object obj) {
		return obj==this;
		}
	@Override
	public int hashCode() {
		return getPosition();
		}
	@Override
	public String toString() {
		return getEnzyme().toString()+ " on "+getSequence().getName()+" at "+getPosition();
		}
	}
class IndexIterator implements Iterator<Integer>
	{
	private int cur;
	private int end;
	public IndexIterator(int beg,int end)
		{
		this.cur=beg;
		this.end=end;
		if(this.cur> this.end) throw new IllegalArgumentException();
		}
	
	@Override
	public boolean hasNext()
		{
		return cur<end;
		}
	
	@Override
	public Integer next() {
		return cur++;
		}
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		}
}
/**
 * SiteList
 * @author pierre
 *
 */
class SiteList  implements Iterable<Site>
	{
	private static final Algorithms<Site,Integer> SORT_BY_LOC=new Algorithms<Site,Integer>(new Comparator<Integer>()
			{
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o1);
				}
			})
			{
			@Override
			public  Integer getKey(Site value)
				{
				return value.getPosition();
				}
			};
			
	
			
	private Vector<Site> sites=new Vector<Site>(1000,100);
	
	public SiteList()
		{
		
		}
	
	public void addSite(Site site)
		{
		this.sites.addElement(site);
		}
	
	public void clear()
		{
		this.sites.clear();
		}
	
	public void sort()
		{
		Collections.sort(this.sites);
		}
	
	private int lower_bound(int position)
		{
		return SORT_BY_LOC.lower_bound(this.sites, position);
		}
	
	private int upper_bound(int position)
		{
		return SORT_BY_LOC.upper_bound(this.sites, position);
		}
	
	public IndexIterator listSitesIn(int start,int end)
		{
		return new IndexIterator(
			lower_bound(start),
			upper_bound(end)
			);
		}
	
	public AbstractIterator<Integer> listSitesOut(int start,int end)
		{
		return new YIterator<Integer>(
			new IndexIterator(
				0,
				lower_bound(start)
				),
			new IndexIterator(
					upper_bound(end),
					this.sites.size()
					)
			);
		}
	
	
	public AbstractIterator<Integer> listSitesOut(Enzyme e,int start,int end)
		{
		return new FilterIterator<Integer>(listSitesOut(start,end),e)
			{
			@Override
			public boolean accept(Integer index) {
				return getSiteAt(index).getEnzyme().equals(getUserData());
				}
			};
		}
	
	public AbstractIterator<Integer> listSitesIn(Enzyme e,int start,int end)
		{
		return new FilterIterator<Integer>(listSitesIn(start,end),e)
			{
			@Override
			public boolean accept(Integer index) {
				return getSiteAt(index).getEnzyme().equals(getUserData());
				}
			};
		}
	
	
	public Site getSiteAt(int i)
		{
		return this.sites.elementAt(i);
		}
	
	public int getSiteCount()
		{
		return this.sites.size();
		}
	
	public Iterator<Site> iterator() {
		return sites.iterator();
		}
	/** remove sites with enzyme in 'set' */
	public void removeEnzymes(Set<Enzyme> enzToRemove)
		{
		int j=0;
		for(int i=0;i< this.sites.size();++i)
			{
			
			if(!enzToRemove.contains( getSiteAt(i).getEnzyme()))
				{
				this.sites.setElementAt(this.sites.elementAt(i), j);
				j++;
				}
			}
		sites.setSize(j);
		}
	
	}

enum Polymerase
	{
	NO_TREATMENT,
	POLYMERASE
	}

class SiteUsage
	{
	private Site site;
	private Polymerase polymerase;
	public SiteUsage(Site site,Polymerase polymerase)
		{
		this.site= site;
		this.polymerase=polymerase;
		}
	
	public Polymerase getPolymerase() {
		return polymerase;
		}
	
	public Site getSite() {
		return site;
		}
	
	public int getPos5_3()
		{
		return -1;//TODO
		}
	
	public int getPos3_5()
		{
		return -1;//TODO
		}

	
	}

abstract class  HemiStrategy
	{
	protected SiteUsage siteUsages[]=new SiteUsage[2];
	
	public HemiStrategy(SiteUsage s1,SiteUsage s2)
		{
		siteUsages[0]=s1;
		siteUsages[1]=s2;
		}
	}

class LeftStrategy extends HemiStrategy
	{
	public LeftStrategy(SiteUsage s1,SiteUsage s2)
		{
		super(s1,s2);
		}
	}

class RightStrategy extends HemiStrategy
	{
	public RightStrategy(SiteUsage s1,SiteUsage s2)
		{
		super(s1,s2);
		}
	}



class Strategy implements Comparable<Strategy>
	{
	private HemiStrategy hemiStgy[]=new HemiStrategy[2];
	
	public Strategy(LeftStrategy s1,RightStrategy s2)
		{
		hemiStgy[0]=s1;
		hemiStgy[1]=s2;
		}
	@Override
	public int compareTo(Strategy o) {
		return 0;
		}
	}

/**
 * Plasmid
 * @author pierre
 *
 */
class Plasmid
	{
	private String name="";
	private byte sequence[];
	private SiteList sites=new SiteList();
	
	public Plasmid()
		{
		}
	
	public SiteList getSites() {
		return sites;
		}
	
	public void digest(EnzymeList rebase)
		{
		getSites().clear();
		
		for(Enzyme e: rebase)
			{
			for(Orientation orient: Orientation.values())
				{
				for(int i=0;i< size();++i)
					{
					int j=0;
					for(j=0;j<e.size();++j)
						{
						if(at(i+j)!='?')
							{
							break;
							}
						}
					if(j==e.size())
						{
						getSites().addSite( new Site(this,e,i,orient) );
						}
					}
				if(e.isPalindromic()) break;
				}
			}
		getSites().sort();
		}
	
	public char at(int index)
		{
		return (char)sequence[index];
		}
	
	public int size()
		{
		return sequence.length;
		}
	
	public void setName(String name) {
		this.name = name;
		}
	
	public String getName() {
		return name;
		}
	
	public boolean isVector()
		{
		return false;
		}
	
	public boolean isInsert()
		{
		return false;
		}
	
	public Vecteur asVector()
		{
		return Vecteur.class.cast(this);
		}
	
	public Insert asInsert()
		{
		return Insert.class.cast(this);
		}
	
	public void setSequence(String s)
		{
		byte array[]=new byte[s.length()];
		int j=0;
		for(int k=0;k< s.length();++k)
			{
			if(Character.isWhitespace(s.charAt(k))) continue;
			if(Character.isDigit(s.charAt(k))) continue;
			if(!Character.isLetter(s.charAt(k))) continue;
			array[j++]=(byte)s.charAt(k);
			}
		this.sequence= new byte[j];
		System.arraycopy(array, 0, this.sequence, 0, this.sequence.length);
		}
	
	public int indexOf(CharSequence substr,int pos)
		{
		for(int i=pos; i+substr.length() < size();++i)
			{
			int j=0;
			for(j=0;j< substr.length();++j)
				{
				if( substr.charAt(j)!=at(i+j)) break;
				}
			if(j==substr.length()) return i;
			}
		return -1;
		}
	
	public Pair<Integer,Integer> findPolylinker(String forward,String reverse)
		{
		forward= forward.toUpperCase();
		reverse= NucleotideUtils.reverseComplement(reverse.toUpperCase());
		
		int n1= indexOf(forward,0);
		if(n1!=-1)
			{
			int n2= indexOf(reverse, n1+forward.length());
			if(n2!=-1) return new Pair<Integer,Integer>(n1,n2+reverse.length());
			}
		
		String s=forward;
		forward= NucleotideUtils.reverseComplement(reverse);
		reverse= NucleotideUtils.reverseComplement(s);
		
		n1= indexOf(forward,0);
		if(n1!=-1)
			{
			int n2= indexOf(reverse, n1+forward.length());
			if(n2!=-1) return new Pair<Integer,Integer>(n1,n2+reverse.length());
			}
		
		return null;
		}
	
	
	@Override
	public String toString() {
		return getName()+" ("+size()+" pb)";
		}
	
	public int countIn(Enzyme e,int beg,int end)
		{
		return getSites().listSitesIn(e, beg, end).count();
		}
	
	public int countOut(Enzyme e,int beg,int end)
		{
		return getSites().listSitesOut(e, beg, end).count();
		}
}

/**
 * Vecteur
 * @author pierre
 *
 */
class Vecteur extends Plasmid
{
static final int BOX5=0;
static final int BOX3=1;

int polylinker[]=new int[2];
@Override
public boolean isVector() {
	return true;
	}
public boolean isInPolylinker(int loc)
	{
	return polylinker[BOX5]<=loc && loc <= polylinker[BOX3];
	}
}

class Insert extends Plasmid
	{
	static final int BOX5=0;
	static final int BOX5_INT=1;
	static final int BOX3_INT=2;
	static final int BOX3=3;
	int polylinker[]=new int[4];
	@Override
	public boolean isInsert() {
		return true;
		}
	
	
	
	
	public boolean isInPolylinker5(int loc)
		{
		return polylinker[0]<=loc && loc <= polylinker[1];
		}
	
	public boolean isInPolylinker3(int loc)
		{
		return polylinker[2]<=loc && loc <= polylinker[3];
		}
	}

abstract class CloneItProgram
	implements Runnable
	{
	Insert insert=new Insert();
	EnzymeList rebase=new EnzymeList();
	
	public void setEnzymeList(EnzymeList enzymes)
		{
		this.rebase= new EnzymeList(enzymes);
		}

	
	public boolean canceled()
		{
		return false;
		}
	public abstract void run();
	}

 class SubCloning
	extends CloneItProgram
	{
	static private final int INSERT=0;
	static private final int VECTOR=1;
	int maxPartialDigestionCount=0;
	boolean usePolymerase=false;
	boolean useCIAP=false;
	Vecteur vector=new Vecteur();
	Plasmid plasmids[]=new Plasmid[]{super.insert,vector};
	
	void digest()
		{
		for(Plasmid p:this.plasmids)
			{
			if(canceled()) break;
			p.digest(this.rebase);
			}
		}
	 
	@Override
	public void run()
		{
		digest();
		HashSet<Enzyme> remove= new HashSet<Enzyme>();
		for(Enzyme enz: this.rebase)
			{
			if(canceled()) break;
			if(this.insert.countIn(
				enz,
				this.insert.polylinker[Insert.BOX5_INT]+1,
				this.insert.polylinker[Insert.BOX3_INT]-1)
				> maxPartialDigestionCount)
				{
				remove.add(enz);
				}
			}
		this.insert.getSites().removeEnzymes(remove);
		
		remove.clear();
		for(Enzyme enz: this.rebase)
			{
			if(canceled()) break;
			if(this.vector.countOut(
					enz,
					this.insert.polylinker[Vecteur.BOX5]-1,
					this.insert.polylinker[Vecteur.BOX3]+1)
					> maxPartialDigestionCount)
					{
					remove.add(enz);
					}
			}
		this.vector.getSites().removeEnzymes(remove);
		
		/** loop over 5' in insert */
		Iterator<Integer> iterI5= this.insert.getSites().listSitesIn(
				this.insert.polylinker[Insert.BOX5],
				this.insert.polylinker[Insert.BOX5_INT]
				);
		while(iterI5.hasNext())
			{
			int indexI5= iterI5.next();
			Site siteI5= this.insert.getSites().getSiteAt(indexI5);
			
			for(int usePolI5=0;usePolI5<2;++usePolI5)
			{
			if(!usePolymerase && usePolI5==1) continue;
			/** loop over 5' in vector */
			Iterator<Integer> iterV5= this.vector.getSites().listSitesIn(
					this.vector.polylinker[Vecteur.BOX5],
					this.vector.polylinker[Vecteur.BOX3]
					);
			while(iterV5.hasNext())
				{
				int indexV5= iterV5.next();
				Site siteV5= this.vector.getSites().getSiteAt(indexV5);
				
				for(int usePolV5=0;usePolV5<2;++usePolV5)
				{
				if(!usePolymerase && usePolV5==1) continue;
				if(!compatible(siteV5,siteI5,usePolymerase)) continue;
				
				/** loop over 3' in vector */
				Iterator<Integer> iterV3= this.vector.getSites().listSitesIn(
						siteV5.getPosition(),
						this.vector.polylinker[Vecteur.BOX3]
						);
				while(iterV3.hasNext())
					{
					int indexV3= iterV3.next();
					Site siteV3= this.vector.getSites().getSiteAt(indexV3);
					
					for(int usePolV3=0;usePolV3<2;++usePolV3)
					{
					if(!usePolymerase && usePolV3==1) continue;
					if(!useCIAP && compatible(siteV5,siteV3,usePolymerase)) continue;
					
					/** loop over 3' in insert */
					Iterator<Integer> iterI3= this.insert.getSites().listSitesIn(
							this.insert.polylinker[Insert.BOX3_INT],
							this.insert.polylinker[Insert.BOX3]
							);
					while(iterI3.hasNext())
						{
						int indexI3= iterI3.next();
						Site siteI3= this.insert.getSites().getSiteAt(indexI3);
						for(int usePolI3=0;usePolI3<2;++usePolI3)
							{
							if(!usePolymerase && usePolI3==1) continue;
							if(!compatible(siteI3,siteV3,usePolymerase)) continue;
							}
						}
					
					}
					}
					}
				
				}
				
				}
			}
		
		}
	
	private boolean compatible(Site left,Site right,boolean usePolymerase)
		{
		//TODO
		return false;
		}
	}

 
class Standalone
	{
	void exec(String args[])
		{
		int optind=0;
		String vectorSource=null;
		String insertSource=null;
		while(optind<args.length)
	         {
	         if(args[optind].equals("-h"))
	            {
	        	System.err.println("\t-h help(this screen)");
				}
	         else if(args[optind].equals("-v"))
	         	{
	        	vectorSource= args[++optind];
	         	}
	         else if(args[optind].equals("-i"))
	         	{
	        	insertSource= args[++optind];
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
	         ++optind;
	         }
		
		if(optind!=args.length)
			{
			 throw new IllegalArgumentException("Too many arguments");	
			}
		
		}
	}
 
 
public class CloneIt extends JPanel
	{
	private static final long serialVersionUID = 1L;
	static private String POLYLINKER[]=new String[]{
		"pGAD424","CCAAAAAAAGAGATC","TTCAGTATCTACGATTCAT",
		"T7/T3","aattaaccctcactaaaggg","taatacgactcactataggg",
		"pTOPO","ACCATGATTACGCCAAGCTTG","ATACGACTCACTATAGGGCGA",
		"pFASTBAC","TATTCCGGATTATTCATACC","GATTATGATCCTCTAGTACTTCTCGAC",
		"Baculo","ttttactgttttcgtaacagtttt","cggatttccttgaagagagta",
		"pBK","ggtctatataagcagagctggt","acaggaaacagctatgaccttg",
		"GEX","atcgaaggtcg","tcagtcagtcacgatg",
		"pET25B","TAATACGACTCACTATA","CCCGTTTAGAGGCCCCAAGGGGTTA",
		"pIIIMS2-1","ttccggctagaactagtggatcc","tcgactctagaggatcg",
		"pIIIMS2-2","agagtcgacctgcaggcatgcaagctg","gctagaactagtggatcc",
		"pGBT9","CAGTTGACTGTATCGCCG","GCCCGGAATTAGCTTGG",
		"pGADGL","CCAAAAAAAGAGATC","ACTATAGGGCGAATTGG",
		"pcDNAFLAG","atggactacaaggacgacgatgacaa","cttggtaccgagctcggatcc",
		"pcDNA3","CACTATAGGGAGACCC","AGGTGACACTATAGAATA",
		"pYX213","TAACGTCAAGGAGAAAAAACCCCGGAT","GAAAAACGTTCATTGTTCCTTAT"
		};
	
	private static final int INSERT=0;
	private static final int VECTOR=1;
	private EnzymeList rebase= EnzymeList.newEnzymeList();
	
	
	
	private IntegerField spinnerInsertBox[]=new IntegerField[4];
	private IntegerField spinnerInsertATG;
	
	private IntegerField spinnerVectorBox[]=new IntegerField[2];
	private IntegerField spinnerVectorATG;
	/*
	private Vecteur vector=new Vecteur();
	private Insert insert=new Insert();
	
	*/
	
	private JTextArea seqArea[]= new JTextArea[2];
	private JTextField seqName[]= new JTextField[2];
	
	public CloneIt()
		{
		super(new BorderLayout(5,5));
		
		JPanel pane0=new JPanel(new GridLayout(0,1,10,10));
		this.add(pane0,BorderLayout.CENTER);
		
		Font smallFont= new Font("Dialog",Font.PLAIN,9);
		this.seqArea[0]= new JTextArea();
		this.seqArea[0].setFont(new Font("Courier",Font.PLAIN,10));
		this.seqArea[1]= new JTextArea();
		this.seqArea[1].setFont(new Font("Courier",Font.PLAIN,10));
		
		JPanel pane= new JPanel(new GridLayout(0,1,5,5));
		pane0.add(pane);
		JPanel pane2= new JPanel(new GridLayout(1,0,5,5));
		pane.add(pane2);
		
		JPanel pane3 = new JPanel(new BorderLayout(5,5));
		Box vBox= Box.createVerticalBox();
		JPanel pane4= new JPanel(new GridLayout(1,0,5,5));
		vBox.add(pane4);
		pane4.add(this.seqName[0]=new JTextField("Insert",20));
		this.seqName[0].setFont(smallFont);
		this.seqName[0].setBorder(BorderFactory.createTitledBorder("Name"));
		pane4.add(this.spinnerInsertATG=new IntegerField());
		spinnerInsertATG.setBorder(BorderFactory.createTitledBorder("ATG"));
		spinnerInsertATG.setFont(smallFont);
		pane4= new JPanel(new GridLayout(1,0,5,5));
		vBox.add(pane4);
		pane4.add(new JLabel("5'",JLabel.RIGHT));
		pane4.add(spinnerInsertBox[0]=new IntegerField());
		spinnerInsertBox[0].setBorder(BorderFactory.createTitledBorder("5'"));
		pane4.add(spinnerInsertBox[1]=new IntegerField());
		spinnerInsertBox[1].setBorder(BorderFactory.createTitledBorder("5' int"));
		pane4.add(spinnerInsertBox[2]=new IntegerField());
		spinnerInsertBox[2].setBorder(BorderFactory.createTitledBorder("3' int"));
		pane4.add(spinnerInsertBox[3]=new IntegerField());
		spinnerInsertBox[3].setBorder(BorderFactory.createTitledBorder("3'"));
		
		for(IntegerField spin: spinnerInsertBox)
			{
			spin.setFont(smallFont);
			}
		
		pane3.add(vBox,BorderLayout.NORTH);
		pane3.setBorder(BorderFactory.createTitledBorder("Insert"));
		pane2.add(pane3);
		pane3.add( new JScrollPane(this.seqArea[VECTOR]),BorderLayout.CENTER);
		
		
		
		pane3 = new JPanel(new BorderLayout());
		vBox= Box.createVerticalBox();
		pane4= new JPanel(new GridLayout(1,0,5,5));
		vBox.add(pane4);
		pane4.add(this.seqName[1]=new JTextField("Vector",20));
		this.seqName[1].setBorder(BorderFactory.createTitledBorder("Name"));
		seqName[1].setFont(smallFont);
		pane4.add(this.spinnerVectorATG=new IntegerField());
		spinnerVectorATG.setBorder(BorderFactory.createTitledBorder("ATG"));
		spinnerVectorATG.setFont(smallFont);
		pane4= new JPanel(new GridLayout(1,0,5,5));
		vBox.add(pane4);
		pane4.add(spinnerVectorBox[0]=new IntegerField());
		spinnerVectorBox[0].setBorder(BorderFactory.createTitledBorder("5'"));
		pane4.add(spinnerVectorBox[1]=new IntegerField());
		spinnerVectorBox[1].setBorder(BorderFactory.createTitledBorder("3'"));
		
		for(IntegerField spin: spinnerVectorBox)
			{
			spin.setFont(smallFont);
			}
		
		pane3.add(vBox,BorderLayout.NORTH);
		pane3.setBorder(BorderFactory.createTitledBorder("Vector"));
		pane2.add(pane3);
		pane3.add( new JScrollPane(this.seqArea[INSERT]),BorderLayout.CENTER);
		
		
		
		
		JPanel pane5= new JPanel(new GridLayout(0,2,10,10));
		pane0.add(pane5);
		
		JPanel pane6= new JPanel(new BorderLayout());
		JTable table= new JTable(this.rebase);
		
		pane6.add(new JScrollPane(table));
		pane5.add(pane6);
		
		JTabbedPane tabbedPane=new JTabbedPane();
		pane5.add(tabbedPane);
		
		
		JPanel panel7= new JPanel();
		tabbedPane.addTab("CloneIt",panel7);
		
		JPanel panel8= new JPanel(new GridLayout(1,0,3,3));
		panel7.add(panel8,BorderLayout.SOUTH);
		panel8.add(new JProgressBar());
		panel8.add(new JButton(new AbstractAction("CloneIt")
			{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
				{
				doMenuCloneIt();
				}
			}));
		
		
		tabbedPane.addTab("Parameters",new JPanel(new BorderLayout()));
		}
	
	
	public EnzymeList getEnzymeList()
		{
		return this.rebase;
		}
	
	
	public void doMenuCloneIt()
		{
		SubCloning prog = new SubCloning();
		for(int i=0;i< 2;++i)
			{
			prog.plasmids[i].setSequence(this.seqArea[i].getText());
			prog.plasmids[i].setName(this.seqName[i].getText().trim());
			prog.plasmids[i].digest(getEnzymeList());
			}
		if(!fillInsertData(prog.insert)) return;
		
		
		
		Thread t= new Thread(prog);
		t.start();
		try {
			t.join();
			}
		catch (InterruptedException e)
			{
			
			}
		}
	
	
	private boolean fillInsertData(Insert insert)
		{
		int bounds[]=new int[4];
		for(int i=0;i< this.spinnerInsertBox.length;++i)
			{
			bounds[i]= this.spinnerInsertBox[i].getValue();
			if(i>0 && bounds[i-1]>=bounds[i])
				{
				JOptionPane.showMessageDialog(this,
						"Error with Insert bounds: "+bounds[i]+"<"+bounds[i-1],
						"Error",
						JOptionPane.ERROR_MESSAGE,
						null);
				return false;
				}
			}
		if(bounds[3]>= insert.size())
			{
			JOptionPane.showMessageDialog(this,
					"Error with Insert bounds: length="+insert.size()+"<="+bounds[3],
					"Error",
					JOptionPane.ERROR_MESSAGE,
					null);
			return false;
			}
		
		return true;
		}
	
	private Pair<Integer,Integer> findPolylinker(Plasmid plasmid)
		{
		Pair<Integer,Integer> p=null;
		for(int i=0;i<POLYLINKER.length;i+=3)
			{
			p= plasmid.findPolylinker(POLYLINKER[i+1], POLYLINKER[i+2]);
			if(p!=null)
				{
				return p;
				}
			}
		return null;
		}
	
	public void readSequence(int seq,InputStream is) throws IOException
		{
		BufferedReader in= new BufferedReader(new InputStreamReader(is));
		String line;
		String name=null;
		StringBuilder b=new StringBuilder();
		while((line=in.readLine())!=null)
			{
			if(line.startsWith(">"))
				{
				if(name!=null) break;
				name=line.substring(1).trim();
				}
			else if(name!=null)
				{
				b.append(line).append("\n");
				}
			}
		if(name==null) throw new IOException("No sequence was found");
		this.seqArea[seq].setText(b.toString());
		this.seqName[seq].setText(name);
		this.seqName[seq].setCaretPosition(0);
		this.seqArea[seq].setCaretPosition(0);
		Vecteur tmp= new Vecteur();
		tmp.setSequence(b.toString());
		Pair<Integer,Integer> bounds=findPolylinker(tmp);
		if(bounds!=null)
			{
			if(seq==INSERT)
				{
				int n=bounds.first();
				int m=bounds.second();
				this.spinnerInsertBox[0].setValue(n);
				this.spinnerInsertBox[1].setValue(n+(m-n)/10);
				this.spinnerInsertBox[2].setValue(m-(m-n)/10);
				this.spinnerInsertBox[3].setValue(m);
				}
			else
				{
				this.spinnerVectorBox[0].setValue(bounds.first());
				this.spinnerVectorBox[1].setValue(bounds.second());
				}
			}
		}
	
	/**
	 * @param args
	 */
	public static void createAndShowGUI() {
		try {
			CloneIt cloneit= new CloneIt();
			//cloneit.readSequence(CloneIt.VECTOR,CloneIt.class.getResourceAsStream("/org/lindenb/app/cloneit/pGAD424.txt"));
			//cloneit.readSequence(CloneIt.INSERT,CloneIt.class.getResourceAsStream("/org/lindenb/app/cloneit/pBS_RF2.txt"));
			JFrame window= new JFrame("CloneIt");
			Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
			window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			window.setBounds(100, 100, screen.width-200, screen.height-200);
			window.setContentPane(cloneit);
			window.setVisible(true);
			
		} catch (Exception e) {
			ThrowablePane.show(null,e);
		}

	}

	
	
	 public static void main(String[] args)
	 	 {
		 if(args.length!=0)
		 	{
			new Standalone().exec(args);
		 	}
		 else
			 {
			 JFrame.setDefaultLookAndFeelDecorated(true);
			 JDialog.setDefaultLookAndFeelDecorated(true);
			 javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		                createAndShowGUI();
		            }
		        });
			 }
	 	}
	}
