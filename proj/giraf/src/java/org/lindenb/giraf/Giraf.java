/**
 * GIRAF
 * Pierre Lindenbaum PhD 2009
 * 
 * pour ref: http://www.oracle.com/technology/documentation/berkeley-db/je/GettingStartedGuide/index.html
 */
package org.lindenb.giraf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lindenb.io.IOUtils;
import org.lindenb.io.TmpWriter;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;
import org.lindenb.util.SmartComparator;
import org.lindenb.util.TimeUtils;
import org.lindenb.xml.NodeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;





/**
 * A config File
 * @author lindenb
 *
 */
class Config
	{
	class ConfigNode
	extends NodeWrapper<Element>
		{
		ConfigNode(Element e)
			{
			super(e);
			}
		}
	/**
	 * A mysql DataSource
	 */
	class DataSource
		extends ConfigNode
		{
		DataSource(Element e)
			{
			super(e);
			}
		public String getId()
			{
			return getAttribute("id");
			}
		public String getDriver() { return getAttribute("driver");}
		public String getUser() { return getAttribute("user","");}
		public String getPassword() { return getAttribute("password","");}
		public String getURI() { return getAttribute("uri","");}
		}
	
	class ActionNode
	extends ConfigNode
		{
		ActionNode(Element e)
			{
			super(e);
			}
		}
	
	
	
	private Document dom;
	Config(File file) throws IOException,ParserConfigurationException,SAXException
		{
		DocumentBuilderFactory domFactory= DocumentBuilderFactory.newInstance();
		domFactory.setCoalescing(true);
		domFactory.setExpandEntityReferences(true);
		domFactory.setIgnoringComments(true);
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		domFactory.setNamespaceAware(true);
		DocumentBuilder domBuilder= domFactory.newDocumentBuilder();
		this.dom =domBuilder.parse(new InputSource(new FileReader(file)));
		}
	}

/**
 * Position
 *
 */
class Position
	implements Comparable<Position>
	{
	public static PositionBinding BINDING= new PositionBinding();
	private String chr;
	private int position;
	public Position(String chr,int position)
		{
		this.chr=chr;
		this.position=position;
		}
	
	public String getChromosome() {
		return chr;
		}
	public int getPosition() {
		return position;
		}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chr.hashCode();
		result = prime * result + position;
		return result;
		}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Position)) return false;
		return compareTo(Position.class.cast(obj))==0;
		}

	@Override
	public int compareTo(Position o)
		{
		int i = this.getChromosome().compareToIgnoreCase(o.getChromosome());
		if(i!=0) return i;
		return getPosition()-o.getPosition();
		}
	@Override
	public String toString()
		{
		return getChromosome()+":"+getPosition();
		}
	}

/**
 * Range
 */
class Range extends Position
	{
	private int end;
	Range(String chrom,int start,int end)
		{
		super(chrom,start);
		this.end=end;
		}
	public boolean contains(Position p)
		{
		return this.getChromosome().equalsIgnoreCase(p.getChromosome()) &&
			this.getPosition()<= p.getPosition() &&
			p.getPosition()<=this.end;
		}
	@Override
	public String toString() {
		return super.toString()+"-"+this.end;
		}
	}

/**
 * PositionBinding
 */
class PositionBinding
	extends TupleBinding<Position>
	{
	@Override
	public Position entryToObject(TupleInput tuple)
		{
		return new Position(tuple.readString(),tuple.readInt());
		}
	
	@Override
	public void objectToEntry(Position pos, TupleOutput tuple)
		{
		tuple.writeString(pos.getChromosome());
		tuple.writeInt(pos.getPosition());
		}
	}






/**
 * A Marker
 * @author pierre
 *
 */
class Marker
 	implements Comparable<Marker>
	{
	public static MarkerBinding BINDING= new MarkerBinding();
	private String name;
	private Position position;
	private SortedMap<String,Integer> allele2count=new TreeMap<String, Integer>(SmartComparator.getInstance());
	private SortedMap<String,String> features=new TreeMap<String, String>();
	
	
	public Marker(String name,String chrom,int position)
		{
		this.name=name;
		this.position= new Position(chrom,position);
		}
	
	public String getName() {
		return name;
		}
	
	public Position getPosition() {
		return position;
		}
	
	
	public String getChromosome()
		{
		return getPosition().getChromosome();
		}
	
	@Override
	public int hashCode()
		{
		return getName().hashCode();
		}

	public SortedMap<String, Integer> getAllele2Count() {
		return allele2count;
		}
	
	public SortedMap<String, String> getFeatures() {
		return features;
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Marker)) return false;
		Marker other = Marker.class.cast( obj);
		return this.getName().equals(other.getName());
		}
	
	@Override
	public int compareTo(Marker o) {
		return this.getName().compareTo(o.getName());
		}
	
	@Override
	public String toString() {
		return getName()+"["+getPosition()+"]";
		}
	
	public int allele2index(String a)
		{
		int i=0;
		for(String s:allele2count.keySet())
			{
			i++;
			if(a.equalsIgnoreCase(s)) return i;
			}
		return 0;
		}
	}


/**
 * 
 * HardyWeinberg calculation
 *
 */
class HardyWeinberg
	{
	private Marker marker=null;
	public int AA=0;
	public int AB=0;
	public int BB=0;
	public int N=0;

	public HardyWeinberg(Marker marker)
        {
		this.marker= marker;
        }
	
	public void see(Genotype g)
		{
		int a= this.marker.allele2index(g.A1());
		int b= this.marker.allele2index(g.A2());
		

		if(a==1 && b==1)
		    {
		    this.N++;
		    this.AA++;
		    }
		else if(a==2 && b==2)
		    {
		    this.N++;
		    this.BB++;
		    }
		else if(a==1 && b==2)
		    {
		    this.N++;
		    this.AB++;
		    }
		else
			{
			throw new IllegalStateException("Bad genotype "+g+" for "+this.marker);
			}
		}

	
    public double p()
	    {
	    return  (double)(2*AA+AB)/(double)(2*N);
	    }
	public double  q()
	    {
	    return  1-p();
	    }
	public double expectAA()
	    {
	    return Math.pow(p(),2)*(double)(N);
	    }
	public double expectBB()
	    {
	    return Math.pow(q(),2)*(double)(N);
	    }
	public double expectAB()
	    {
	    return (2.0*p()*q())*(double)(N);
	    }
	
	
	//chi2 = SUM ((obs - expected)^2) / expected)
	public double chi2()
	    {
	    return
	            (Math.pow(((double)(AA)-expectAA()),2))/expectAA()
	    +       (Math.pow(((double)(AB)-expectAB()),2))/expectAB()
	    +       (Math.pow(((double)(BB)-expectBB()),2))/expectBB()
	    ;
	    }

	@Override
	public String toString() {
		return "11:"+AA+"|12:"+AB+"|22:"+BB+"|chi2:"+chi2();
		}
	}


/**
 * TupleBinding for marker
 *
 */
class MarkerBinding
	extends TupleBinding<Marker>
	{
	@Override
	public Marker entryToObject(TupleInput tuple)
		{
		Marker m= new Marker(
			tuple.readString(),
			tuple.readString(),
			tuple.readInt()
			);
		int n= tuple.readInt();
		for(int i=0;i< n;++i)
			{
			m.getAllele2Count().put(tuple.readString(), tuple.readInt());
			}
		n= tuple.readInt();
		for(int i=0;i< n;++i)
			{
			m.getFeatures().put(tuple.readString(), tuple.readString());
			}
		return m;
		}
	
	@Override
	public void objectToEntry(Marker marker, TupleOutput tuple)
		{
		tuple.writeString(marker.getName());
		tuple.writeString(marker.getPosition().getChromosome());
		tuple.writeInt(marker.getPosition().getPosition());
		
		tuple.writeInt(marker.getAllele2Count().size());
		for(String k: marker.getAllele2Count().keySet())
			{
			tuple.writeString(k);
			tuple.writeInt(marker.getAllele2Count().get(k));
			}
		
		tuple.writeInt(marker.getFeatures().size());
		for(String k: marker.getFeatures().keySet())
			{
			tuple.writeString(k);
			tuple.writeString(marker.getFeatures().get(k));
			}
		}
	}

class PositionKeyCreator
	implements SecondaryKeyCreator
		{
		@Override
		public boolean createSecondaryKey(SecondaryDatabase arg0,
				DatabaseEntry keyEntry, DatabaseEntry dataEntry, DatabaseEntry resultEntry)
				throws DatabaseException
			{
			Position pos = Marker.BINDING.entryToObject(dataEntry).getPosition();
			Position.BINDING.objectToEntry(pos,  resultEntry);
			return true;
			}
		} 
 
/**
 * The name of an individual
 * @author pierre
 *
 */
class Name
	implements Comparable<Name>
	{
	public static final NameBinding BINDING_NAME= new NameBinding();
	private String family;
	private int firstName;
	
	public Name(String family,int firstName)
		{
		this.family=family;
		this.firstName=firstName;
		}
	
	public String getFamily() {
		return family;
		}
	
	public int getFirstName() {
		return firstName;
		}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + family.hashCode();
		result = prime * result + firstName;
		return result;
		}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Name)) return false;
		Name other = Name.class.cast(obj);
		return  compareTo(other)==0;
		}
	
	@Override
	public int compareTo(Name o) {
		int ii=  this.getFamily().compareTo(o.getFamily());
		if(ii!=0) return ii;
		return this.firstName - o.firstName;
		}
	
	@Override
	public String toString() {
		return getFamily()+":"+getFirstName();
		}
	
	public DatabaseEntry copyToEntry(DatabaseEntry entry)
		{
		Name.BINDING_NAME.objectToEntry(this, entry);
		return entry;
		}
	}

/** tuple binding for name */
class NameBinding
extends TupleBinding<Name>
	{
	@Override
	public Name entryToObject(TupleInput tuple){
		return new Name(tuple.readString(),tuple.readInt());
		}
	@Override
	public void objectToEntry(Name name, TupleOutput tuple) {
		tuple.writeString(name.getFamily()).writeInt(name.getFirstName());
		}
	}

/**
 * An individual
 * @author pierre
 *
 */
class Individual
	extends Name
	{
	public static final IndividualBinding BINDING= new IndividualBinding();
	private int father;
	private int mother;
	private int gender;
	private SortedMap<String, String> phenotypes=new TreeMap<String, String>();
	/** column in the linkage file */
	private int column=-1;
	
	public Individual(
			String family,
			int firstName,
			int father,
			int mother,
			int gender
			)
		{
		super(family,firstName);
		this.father=father;
		this.mother=mother;
		this.gender=gender;
		}
	
	public Name getName()
		{
		return new Name(getFamily(),getFirstName());
		}
	
	public boolean hasParent(int i)
		{
		return  getParent(i).getFirstName()!=0;
		}
	
	public Name getParent(int i)
		{
		return i==0?getFather():getMother();
		}
	
	public Name getFather() {
		return new Name(getFamily(),father);
		}
	
	public Name getMother() {
		return new Name(getFamily(),mother);
		}
	
	public int getGender() {
		return gender;
		}
	
	public SortedMap<String, String> getPhenotypes() {
		return phenotypes;
		}
	
	public void setColumn(int column) {
		this.column = column;
		}
	
	public int getColumn() {
		return column;
		}
	
	@Override
	public boolean equals(Object o) {
		if(!super.equals(o)) return false;
		Individual other = Individual.class.cast(o);
		if(father!=other.father) return false;
		if(mother!=other.mother) return false;
		if(gender!=other.gender) return false;
		return true;
		}

	}

/** tuple binding for individual */
class IndividualBinding
extends TupleBinding<Individual>
	{
	@Override
	public Individual entryToObject(TupleInput tuple){
		Individual indi= new Individual(
				tuple.readString(),
				tuple.readInt(),
				tuple.readInt(),
				tuple.readInt(),
				tuple.readInt()
				);
		indi.setColumn(tuple.readInt());
		
		int n= tuple.readInt();
		for(int i=0;i< n;++i)
			{
			indi.getPhenotypes().put(tuple.readString(), tuple.readString());
			}
		
		return indi;
		}
	@Override
	public void objectToEntry(Individual name, TupleOutput tuple) {
		tuple.writeString(name.getFamily()).
				writeInt(name.getFirstName()).
				writeInt(name.getFather().getFirstName()).
				writeInt(name.getMother().getFirstName()).
				writeInt(name.getGender())
				;
		tuple.writeInt(name.getColumn());
		
		tuple.writeInt(name.getPhenotypes().size());
		for(String k: name.getPhenotypes().keySet())
			{
			tuple.writeString(k);
			tuple.writeString(name.getPhenotypes().get(k));
			}
		}
	}

/**
 * Genotype
 */
class Genotype
	{
	private String a1,a2;
	Genotype(String a1,String a2)
		{
		a1=a1.toUpperCase().trim();
		a2=a2.toUpperCase().trim();
		if(SmartComparator.getInstance().compare(a1,a2)<=0)
			{
			this.a1=a1;
			this.a2=a2;
			}
		else
			{
			this.a1=a2;
			this.a2=a1;
			}
		}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a1.hashCode();
		result = prime * result + a2.hashCode();
		return result;
		}
	
	@Override
	public boolean equals(Object obj) {
		Genotype other= Genotype.class.cast(obj);
		return a1.equals(other.a1) && a2.equals(other.a2);
		}
	
	public String A1() { return a1;}
	public String A2() { return a2;}
	public String A(int i) { return i==0?A1():A2();}
	
	@Override
	public String toString() {
		return A1()+"/"+A2();
		}
	}

class DistinctGenotype extends HashSet<Genotype>
	{
	DistinctGenotype()
		{
		
		}
	DistinctGenotype(int capacity)
		{
		super(capacity);
		}
	Genotype first()
		{
		Iterator<Genotype> iter= iterator();
		return iter.hasNext()?iter.next():null;
		}
	
	}


/**
 * 
 * GenotypesBinding
 *
 */
class GenotypesBinding
	extends TupleBinding<List<DistinctGenotype>>
	{
	static public final GenotypesBinding INSTANCE=new GenotypesBinding();
	
	@Override
	public List<DistinctGenotype> entryToObject(TupleInput tuple)
		{
		int n= tuple.readInt();
		List<DistinctGenotype> list= new ArrayList<DistinctGenotype>(n);
		for(int i=0;i< n;++i)
			{
			boolean b= tuple.readBoolean();
			if(!b)
				{
				list.add(null);
				}
			else
				{
				int len=tuple.readInt();
				DistinctGenotype array=new DistinctGenotype(len);
				for(int k=0;k< len;++k)
					{
					array.add( new Genotype(tuple.readString(),tuple.readString()) );
					}
				list.add(array);
				}
			}
		return list;
		}
	@Override
	public void objectToEntry(List<DistinctGenotype> list, TupleOutput tuple)
		{
		tuple.writeInt(list.size());
		for(int i=0;i< list.size();++i)
			{
			if(list.get(i)==null || list.get(i).isEmpty())
				{
				tuple.writeBoolean(false);
				}
			else
				{
				tuple.writeBoolean(true);
				tuple.writeInt(list.get(i).size());
				for(Genotype g: list.get(i))
					{
					tuple.writeString(g.A1());
					tuple.writeString(g.A2());
					}
				}
			}
		}
	}

/**
 * 
 * Stat
 *
 */
class Stat
	{
	int total =0;
	int genotyped=0;
	int errors=0;
	double success()
		{
		if(total==0) return -1;
		return 100.0*(genotyped/(double)total);
		}
	}


/** utility to find mandelian incompatibilites */
class FamilyIncompat
	{
    /**
     * return wether a individual can really be child of parents
     * @param parent1 a parental allele
     * @param parent2 a parental allele
     * @param c1    allele 1 of a child
     * @param c2    allele 2 of a child
     * @return
     */
    static private boolean isOK(
            String parent1,
            String parent2,
            String c1,
            String c2)
    	{

        return
            (parent1.equals(c1) && parent2.equals(c2)) ||
            (parent1.equals(c2) && parent2.equals(c1))
            ;
    	}
    
    /*** return true if child has no allele from its parent */
    static public boolean childIsNotCompatibleWithOneParent(
            String parentA1,
            String parentA2,
            String childA1,
            String childA2
            )
    {
    return(!(
            childA1.equals(parentA1) ||
            childA1.equals(parentA2) ||
            childA2.equals(parentA1) ||
            childA2.equals(parentA2)));
    }
    
    /*** return true if child cannot be child of two parents */
    static public boolean parentCannotHaveThisChild
            (
    		String fatherA1,
    		String fatherA2,
    		String motherA1,
    		String motherA2,
    		String childA1,
    		String childA2
            )
    {
    if( isOK(fatherA1,motherA1,childA1,childA2)) return false;
    if( isOK(fatherA1,motherA2,childA1,childA2)) return false;
    if( isOK(fatherA2,motherA1,childA1,childA2)) return false;
    if( isOK(fatherA2,motherA2,childA1,childA2)) return false;
    return true;
    }
    

}

class ConfigShuttle
	{
	Properties properties= new Properties();
	Set<String> visitedFiles= new HashSet<String>();
	//File base=new File(System.getProperty("user.dir","."));

	
	public String getProperty(String name)
		{
		String s= this.properties.getProperty(name);
		if(s==null) s= System.getProperty(name);
		if(s==null) s="";
		return s;
		}
	}

/**
 * 
 * LineIterator
 * 
 */
class LineIterator
	implements Iterator<List<String>>
	{
	BufferedReader in=null;
	List<String> current=null;
	Pattern pattern;
	
	LineIterator(BufferedReader r,Pattern pattern)
		{
		this.in=r;
		this.pattern=pattern;
		}
	
	@Override
	public boolean hasNext()
		{
		if(current!=null) return true;
		if(in==null) return false;
		String line=null;
		String token[];
		while(true)
			{
			try { line= in.readLine(); } catch(IOException err) { throw new RuntimeException(err);}
			if(line==null)
				{
				in=null;
				current=null;
				return false;
				}
			token= this.pattern.split(line);
			this.current= new ArrayList<String>(token.length);
			for(String s:token) this.current.add(s.trim());
			break;
			}
		return current!=null;
		}
	
	public List<String> next()
		{
		if(current==null)
			{
			hasNext();
			if(current==null) throw new IllegalStateException("no next");
			}
		List<String> x= this.current;
		this.current=null;
		return x;
		}
	
	
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("cannot remove");
		}
	
	}

/**
 * Linkage
 * @author lindenb
 *
 */
class Linkage
	{
	private static long ID=System.currentTimeMillis();
	/** number of individual */
	private int individualCount=0;
	/** number of markers */
	private int markerCount=0;
	/** table RS to Marker */
	private Database markerDB = null;
	private SecondaryDatabase pos2markerDB= null;
	private Database individualDB = null;
	private Database index2indiviualName = null;
	private Database mkr2genotypesDB = null;
    private Giraf giraf;
    private Set<String> allPhenotypes= new TreeSet<String>();
	private Set<String> allMarkerFeatures= new TreeSet<String>();
	 final String zipPrefix= "GIRAF"+TimeUtils.toYYYYMMDDHHMMSS()+"/";
	
	Linkage(Giraf giraf) throws DatabaseException
		{
		String random = "_"+(++ID);
		this.giraf=giraf;
		
		/* create marker database: map<rs,marker> */
		DatabaseConfig cfg= new DatabaseConfig();
		cfg.setAllowCreate(true);
		cfg.setTemporary(true);
		cfg.setSortedDuplicates(false);
		this.markerDB= giraf.berkeleyEnv.openDatabase(null, "markers"+random, cfg);
		
		/* create position-to-marker 2nd-database: map<position,marker> */
		SecondaryConfig secondCfg= new SecondaryConfig();
		secondCfg.setAllowCreate(true);
		secondCfg.setSortedDuplicates(true);
		secondCfg.setTemporary(true);
		secondCfg.setKeyCreator(new PositionKeyCreator());
		this.pos2markerDB= giraf.berkeleyEnv.openSecondaryDatabase(null, "pos"+random, this.markerDB, secondCfg);
		
		/* create marker database: map<name,individual> */
		cfg= new DatabaseConfig();
		cfg.setAllowCreate(true);
		cfg.setTemporary(true);
		cfg.setSortedDuplicates(false);
		this.individualDB= giraf.berkeleyEnv.openDatabase(null, "individuals"+random, cfg);
		
		/* create genotypes database: map<rs,list<genotype[]> > */
		cfg= new DatabaseConfig();
		cfg.setAllowCreate(true);
		cfg.setTemporary(true);
		cfg.setSortedDuplicates(false);
		mkr2genotypesDB= giraf.berkeleyEnv.openDatabase(null, "genotypes"+random, cfg);

		cfg= new DatabaseConfig();
		cfg.setAllowCreate(true);
		cfg.setTemporary(true);
		
		this.index2indiviualName = giraf.berkeleyEnv.openDatabase(null, "id2individualName"+random, secondCfg);
		}
	
	void merge(Linkage left,Linkage right) throws DatabaseException
		{
		//merge sort 
		DatabaseEntry key1=new DatabaseEntry();
		DatabaseEntry data1 =new DatabaseEntry();
		DatabaseEntry key2=new DatabaseEntry();
		DatabaseEntry data2 =new DatabaseEntry();
		Cursor cursor = left.individualDB.openCursor(null, null);
		
		//merge individuals
		this.allPhenotypes.addAll(left.allPhenotypes);
		this.allPhenotypes.addAll(right.allPhenotypes);
	
		while(cursor.getNext(key1, data1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			Individual indi1= Individual.BINDING.entryToObject(data1);
			
			if(right.individualDB.get(null, key1, data2,LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				Individual indi2= Individual.BINDING.entryToObject(data2);
				//TODO check same and merge
				}
			Individual.BINDING.objectToEntry(indi1,data1);
			this.individualDB.put(null, key1, data1);
			}
		cursor.close();
		
		key2=new DatabaseEntry();
		data2 =new DatabaseEntry();
		cursor = right.individualDB.openCursor(null, null);
		while(cursor.getNext(key2, data2, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			if(left.individualDB.get(null, key2, data1,LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				//already done
				continue;
				}
			this.individualDB.put(null, key2, data2);
			}
		cursor.close();
		
		validatePedigree();
		
		//merge marker
		this.allMarkerFeatures.addAll(left.allMarkerFeatures);
		this.allMarkerFeatures.addAll(right.allMarkerFeatures);
		cursor = left.markerDB.openCursor(null, null);
		key1=new DatabaseEntry();
		data1 =new DatabaseEntry();
		while(cursor.getNext(key1, data1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			Marker marker1= Marker.BINDING.entryToObject(data1);
			
			if(right.markerDB.get(null, key1, data2,LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				Marker marker2= Marker.BINDING.entryToObject(data2);
				//TODO check fatures and merge in marker1
				}
			Marker.BINDING.objectToEntry(marker1,data1);
			this.markerDB.put(null, key1, data1);
			}
		cursor.close();
		
		key2=new DatabaseEntry();
		data2 =new DatabaseEntry();
		cursor = right.markerDB.openCursor(null, null);
		while(cursor.getNext(key2, data2, LockMode.DEFAULT)==OperationStatus.SUCCESS)
			{
			if(left.markerDB.get(null, key2, data1,LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				//already done
				continue;
				}
			this.markerDB.put(null, key2, data2);
			}
		cursor.close();
		
		
		
		
		
		for(int side=0;side< 2;++side)
			{
			
			Database prevDB = (side==0?left.index2indiviualName:right.index2indiviualName);
			HashMap<Integer, Integer> old2newIndex= new HashMap<Integer, Integer>();
			key1=new DatabaseEntry();
			data1 =new DatabaseEntry();
			cursor= prevDB.openCursor(null, null);
			while(cursor.getNext(key1, data1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				int oldIndex= IntegerBinding.entryToInt(key1);
				if(this.individualDB.get(null, data1, data2, LockMode.DEFAULT)!=OperationStatus.SUCCESS)
					{
					throw new DatabaseException("Cannot retrieve old individual");
					}
				Individual newIndi= Individual.BINDING.entryToObject(data2);
				old2newIndex.put(oldIndex,newIndi.getColumn());
				}

			cursor.close();
			
			
			key1=new DatabaseEntry();
			data1 =new DatabaseEntry();
			 prevDB = (side==0?left.mkr2genotypesDB:right.mkr2genotypesDB);
			cursor= prevDB.openCursor(null, null);
			while(cursor.getNext(key1, data1, LockMode.DEFAULT)==OperationStatus.SUCCESS)
				{
				List<DistinctGenotype> oldGenotypes  = GenotypesBinding.INSTANCE.entryToObject(data1);
				//genotypes
				List<DistinctGenotype> genotypes =new ArrayList<DistinctGenotype>(this.individualCount);
				while(genotypes.size()< this.individualCount)
					{
					genotypes.add(null);
					}
				for(int i=0;i< oldGenotypes.size();++i)
					{
					int newIndex= old2newIndex.get(i);
					
					DistinctGenotype prevGenotype = oldGenotypes.get(newIndex);
					
					DistinctGenotype g= genotypes.get(newIndex);
					if(prevGenotype!=null)
						{
						if(g==null)
							{
							g=prevGenotype;
							}
						else
							{
							g.addAll(prevGenotype);
							}
						}
					genotypes.set(newIndex, g);
					}
				
				
				GenotypesBinding.INSTANCE.objectToEntry(genotypes,data1);
				this.mkr2genotypesDB.put(null, key1, data1);
				}
			cursor.close();
			}
		}
	
	void close() throws DatabaseException
		{
		this.index2indiviualName.close();
		this.pos2markerDB.close();
		this.markerDB.close();
		this.individualDB.close();
		this.mkr2genotypesDB.close();
		}
	/**
	 * Read a genotype file
	 * @param genFile
	 * @throws IOException
	 * @throws DatabaseException
	 */
	  void readGenotypes(Iterator<List<String> > lines)
		throws IOException, DatabaseException
		{
		List<String> header=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry markerValue =new DatabaseEntry();
		DatabaseEntry indiValue =new DatabaseEntry();
		DatabaseEntry genotypesValue =new DatabaseEntry();
		String prevMarker=null;
		List<DistinctGenotype> genotypes= null;
		
		while(lines.hasNext())
			{
			List<String> token = lines.next();
			
			
			if(header==null)
				{
				if(token.size() <5) throw new IOException("Expected 5 columns but found "+token.size()+" in "+token);
				header= token;
				assertToken(header,0,"Marker","rs","snp","rs#");
				assertToken(header,1,"Family","fam");
				assertToken(header,2,"Individual","indi","name");
				assertToken(header,3,"Allele-1","A1");
				assertToken(header,4,"Allele-2","A2");
				continue;
				}
			
			if(token.size()!=3 && token.size()!=5) throw new IOException("expected 5 columns but found "+token.size()+" in "+token);
			
			if(token.size()==3 ||
			   token.get(0).length()==0 ||
			   token.get(1).length()==0 ||
			   token.get(2).length()==0 ||
			   token.get(3).length()==0 ||
			   token.get(4).length()==0 ||
			   token.get(3).equalsIgnoreCase("N") ||
			   token.get(3).equals("-") ||
			   token.get(3).equals("?") ||
			   token.get(3).equals("0") ||
			   token.get(4).equalsIgnoreCase("N") ||
			   token.get(4).equals("-") ||
			   token.get(4).equals("?") ||
			   token.get(4).equals("0")
			 	)
				{
				prevMarker=null;
				genotypes=null;
				//System.err.println("In "+genFile+" ignoring "+line);
				continue;
				}
			
			
			if(!Cast.Integer.isA(token.get(2)))
				{
				throw new IOException("Bad family (not name=integer) in "+token+" in ");
				}
			
			if(prevMarker!=null &&
				prevMarker.equals(token.get(0)) &&
				genotypes==null)
				{
				//marker is the same but it is not in our listing
				continue;
				}
			
			new Name(token.get(1),Cast.Integer.cast(token.get(2))).copyToEntry(key);
			if(this.individualDB.get(null,key,indiValue,null )!= OperationStatus.SUCCESS)
				{
				//individual is not in our collection
				continue;
				}
			
			Individual individual= Individual.BINDING.entryToObject(indiValue);
			
			//now key is for marker, need this to save AND load the genotypes
			StringBinding.stringToEntry(token.get(0), key);
			
			/* if marker is same as previous no need to retrieve the array */
			if(prevMarker==null || !prevMarker.equals(token.get(0)))
				{
				prevMarker=token.get(0);
				if(this.markerDB.get(null,key,markerValue,null )!= OperationStatus.SUCCESS)
					{
					//marker is noty in out listing
					genotypes=null;
					continue;
					}

				if(this.mkr2genotypesDB.get(null,key,genotypesValue,null )!= OperationStatus.SUCCESS)
					{
					genotypes = new ArrayList<DistinctGenotype>(individualCount);
					while(genotypes.size()< individualCount) genotypes.add(null);
					}
				else
					{
					genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesValue);
					}
				}
			else
				{
				prevMarker=token.get(0);
				assert(genotypes!=null);
				}
			
			Genotype genotype=new Genotype(token.get(3),token.get(4));
			DistinctGenotype gen_array= genotypes.get(individual.getColumn());
			if(gen_array==null || gen_array.isEmpty())
				{
				gen_array=new DistinctGenotype();
				gen_array.add(genotype);
				}
			else
				{
				if(gen_array.contains(genotype)) continue;
				
				gen_array.add(genotype);
				}
			genotypes.set(individual.getColumn(),gen_array);
			GenotypesBinding.INSTANCE.objectToEntry(genotypes, genotypesValue);
			if(this.mkr2genotypesDB.put(null,key,genotypesValue )!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot put genotypes for "+token.get(0));
				}
			}
		
		if(header==null)
			{
			System.err.println("Empty data in");
			}
		}
	
	
	/** read pedigrees to be excluded */
	 void readXPedigreeDB(Iterator<List<String>> iter)
	throws IOException, DatabaseException
		{
		List<String> token;
		List<String> header=null;
		DatabaseEntry key=new DatabaseEntry();
		while(iter.hasNext())
			{
			token= iter.next();
			
			if(token.size()<2) throw new IOException("Expected 2 columns but found "+token.size());
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Family","fam");
				assertToken(header,1,"Individual","indi");
				continue;
				}
			
			if(token.get(0).length()==0 || token.get(1).length()==0 ||
				!Cast.Integer.isA(token.get(1))) continue;
			
			
			Name name=new Name(token.get(0),Cast.Integer.cast(token.get(1)));
			if(this.individualDB.delete(null,name.copyToEntry(key))== OperationStatus.SUCCESS)
				{
				Giraf._LOG.info("removing "+name);
				this.individualCount--;
				assert(this.individualCount>=0);
				continue;
				}
			}
		if(header==null)
			{
			System.err.println("Nothing found");
			}
		}
	
	/**
	 * readMarkerDB
	 * read a file of marker
	 * @param markerSrcURI
	 * @throws IOException
	 * @throws DatabaseException
	 */
	void readMarkerDB(Iterator<List<String>> lines)
	throws IOException, DatabaseException
		{
		List<String> header=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while(lines.hasNext())
			{
		
			List<String> token = lines.next();
			if(token.size()<3) throw new IOException("expected 3 columns but found "+token.size()+" in "+token);
			
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Name","rs","snp","rs#","marker");
				assertToken(header,1,"Chromosome","k","chr","chrom");
				assertToken(header,2,"Position","pos","loc","location");
				for(int i=3;i<token.size() && i< header.size();++i)
					{
					Giraf._LOG.info("adding current snp feature "+header.get(i));
					this.allMarkerFeatures.add(header.get(i));
					}
				continue;
				}
			
			
			if(token.get(0).trim().length()==0) throw new IOException("bad marker name in "+token);
			
			if(token.get(1).trim().length()==0 ||
			   token.get(2).trim().length()==0)
				{
				token.set(2,"-1");
				}
			if(!Cast.Integer.isA(token.get(2))) throw new IOException("bad position in "+token);
			Marker marker = new Marker(token.get(0).trim(),token.get(1),Cast.Integer.cast(token.get(2)));
			
			if(!this.giraf.limitByPosition.isEmpty())
				{
				boolean ok=false;
				for(Range range: this.giraf.limitByPosition)
					{
					if(range.contains(marker.getPosition()))
						{
						ok=true;
						break;
						}
					}
				if(!ok) continue;
				}
			
			for(int i=3;i<token.size() && i< header.size();++i)
				{
				marker.getFeatures().put(header.get(i), token.get(i));
				}
				
			
			StringBinding.stringToEntry(marker.getName(), key);
			if(this.markerDB.get(null,key,value,null )== OperationStatus.SUCCESS)
				{
				Marker other = Marker.BINDING.entryToObject(value);
				if(!other.equals(marker))
					{
					System.err.println("Marker "+other+" was previously defined as "+marker);
					}
				continue;
				}
			Marker.BINDING.objectToEntry(marker, value);
			if(this.markerDB.put(null, key,value)!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot insert "+marker);
				}
			this.markerCount++;
			}
		
		if(header==null)
			{
			System.err.println("Empty data in marker file");
			}
		}
	
	
	void readExclusedMarkerDB(Iterator<List<String>> lines)
	throws IOException, DatabaseException
		{
		List<String> token=null;
		List<String> header=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while(lines.hasNext())
			{
			token=lines.next();
			
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Name");
				continue;
				}
			
			
			StringBinding.stringToEntry(token.get(0).trim(), key);
			if(this.markerDB.get(null,key,value,null )!= OperationStatus.SUCCESS)
				{
				continue;
				}
			
			Giraf._LOG.fine("removing "+header.get(0));
			if(this.markerDB.delete(null, key)!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot remove "+token.get(0));
				}
			this.markerCount--;
			}
		}
	
	
	void readPedigreeDB(Iterator<List<String>> lines)
		throws IOException, DatabaseException
		{
		List<String> header=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while(lines.hasNext())
			{
			List<String> token= lines.next();
			
			if(token.isEmpty()) continue;
			
			if(token.size() <5) throw new IOException("In "+token+" expected 5 columns but found "+token.size());
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Family","fam");
				assertToken(header,1,"Individual","indi","name");
				assertToken(header,2,"Father","f");
				assertToken(header,3,"Mother","m");
				assertToken(header,4,"Gender","sex");
				for(int i=5;i<token.size() && i< header.size();++i)
					{
					Giraf._LOG.info("add phenotype "+header.get(i));
					this.allPhenotypes.add(header.get(i));
					}
				continue;
				}
			
			if(token.get(0).trim().length()==0) throw new IOException("bad family name in "+token);
			if(!Cast.Integer.isA(token.get(1))) throw new IOException("bad name in "+token);
			if(token.get(2).trim().length()==0) token.set(2,"0");
			if(!Cast.Integer.isA(token.get(2))) throw new IOException("bad father in "+token);
			if(token.get(3).trim().length()==0) token.set(3,"0");
			if(!Cast.Integer.isA(token.get(3))) throw new IOException("bad mother in "+token);
			if(token.get(4).trim().length()==0) token.set(4,"0");
			int gender= Cast.Integer.cast(token.get(4));
			if(gender<0 || gender>2) throw new IOException("bad gender in "+token);
			Individual indi= new Individual(
					token.get(0),
					Cast.Integer.cast(token.get(1)),
					Cast.Integer.cast(token.get(2)),
					Cast.Integer.cast(token.get(3)),
					gender
					);
			
			for(int i=5;i<token.size() && i< header.size();++i)
				{
				indi.getPhenotypes().put(header.get(i), token.get(i));
				}
			
			indi.getName().copyToEntry(key);
			if(this.individualDB.get(null,key,value,null )== OperationStatus.SUCCESS)
				{
				Individual other = Individual.BINDING.entryToObject(value);
				if(!other.equals(other))
					{
					System.err.println("Individual "+other+" was previously defined as "+other);
					}
				continue;
				}
			Individual.BINDING.objectToEntry(indi, value);
			if(this.individualDB.put(null, key,value)!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot insert "+indi);
				}
			this.individualCount++;
			}
	
		if(header==null)
			{
			System.err.println("Nothing found in input");
			}
		}
	 void updateMakerAlleleCount()
	throws IOException, DatabaseException
		{
		Cursor cursor= markerDB.openCursor(null, null);
		DatabaseEntry markerKey=new DatabaseEntry();
		DatabaseEntry markerValue=new DatabaseEntry();
		DatabaseEntry genotypesEntry=new DatabaseEntry();
		while((cursor.getNext(markerKey, markerValue, null))==OperationStatus.SUCCESS)
			{
			Marker marker= Marker.BINDING.entryToObject(markerValue);
			if(this.mkr2genotypesDB.get(null, markerKey, genotypesEntry, null)!=OperationStatus.SUCCESS)
				{
				Giraf._LOG.info("marker is not in database");
				continue;
				}
			List<DistinctGenotype> genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesEntry);
			assert(genotypes.size()==this.individualCount);
			for(int i=0;i< genotypes.size();++i)
				{
				DistinctGenotype array=genotypes.get(i);
				if(array==null || array.size()!=1) continue;
				Genotype g=array.first();
				for(int j=0;j< 2;++j)
					{
					String a= (j==0?g.A1():g.A2());
					Integer count = marker.getAllele2Count().get(a);
					if(count==null) count=0;
					marker.getAllele2Count().put(a,count+1);
					}
				}
			Giraf._LOG.info("marker alleles "+marker.getAllele2Count());
			Marker.BINDING.objectToEntry(marker,markerValue);
			cursor.putCurrent(markerValue);
			}
		cursor.close();
		}
	
	 void validatePedigree()
	throws  DatabaseException
		{
		DatabaseEntry key = new DatabaseEntry();
	    DatabaseEntry value = new DatabaseEntry();
	    
	    DatabaseEntry pKey = new DatabaseEntry();
	    DatabaseEntry pValue = new DatabaseEntry();
	   
	    DatabaseEntry indexOfEntry= new DatabaseEntry();
	    int column=0;
		Cursor cursor= null;
		try
			{
			cursor=this.individualDB.openCursor(null, null);
			while (cursor.getNext(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS)
				{
				Individual indi= Individual.BINDING.entryToObject(value);
				//UPDATE THE COLUMN
				
				IntegerBinding.intToEntry(column, indexOfEntry);
				this.index2indiviualName.put(null, indexOfEntry, key);
				indi.setColumn(column++);
				
				
				for(int i=0;i< 2;++i)
					{
					Name parentName= (i==0?indi.getFather():indi.getMother());
					if(parentName.getFirstName()==0) continue;
					parentName.copyToEntry(pKey);
					if(this.individualDB.get(null, pKey, pValue, null)!= OperationStatus.SUCCESS)
						{
						System.err.println(indi.toString()+" has a missing parent "+parentName);
						continue;
						}
					Individual parent= Individual.BINDING.entryToObject(pValue);
					if( (i==0 && parent.getGender()==2)  &&
						(i==1 && parent.getGender()==1)
						)
						{
						System.err.println(indi.toString()+" bad gender with parent "+parentName);
						continue;
						}
					}
				
				//column was updated, save this
				Individual.BINDING.objectToEntry(indi,value);
				if(cursor.putCurrent(value)!= OperationStatus.SUCCESS)
					{
					throw new DatabaseException("Cannot re-put the individual "+indi);
					}
				
				}
			}
		catch(DatabaseException err)
			{
			throw err;
			}
		finally
			{
			if(cursor!=null) cursor.close();
			}
		}
	
	
		 void makeIndividualStats(ZipOutputStream zout)
		throws IOException, DatabaseException
			{
			DatabaseEntry markerKey= new DatabaseEntry();
			DatabaseEntry genotypesValue= new DatabaseEntry();
			DatabaseEntry indiKey= new DatabaseEntry();
			DatabaseEntry indiValue= new DatabaseEntry();
			
			Stat stats[]=new Stat[this.individualCount];
			for(int i=0;i< this.individualCount;++i)
				{
				stats[i]=new Stat();
				stats[i].total=this.markerCount;
				}
			Cursor cursor= mkr2genotypesDB.openCursor(null, null);
			while((cursor.getNext(markerKey, genotypesValue, null))==OperationStatus.SUCCESS)
				{
				List<DistinctGenotype> genotypes=GenotypesBinding.INSTANCE.entryToObject(genotypesValue);
				for(int i=0;i< stats.length;++i)
					{
					DistinctGenotype array= genotypes.get(i);
					if(array==null)
						{
						//ignore
						}
					else if(array.size()==1)
						{
						stats[i].genotyped++;
						}
					else
						{
						stats[i].errors++;
						}
					}
				}
			cursor.close();
			
			TmpWriter out= new TmpWriter(this.giraf.envHome);
			out.println("Family\tName\tTotal\tGenotyped\t%\tErrors");
			cursor= individualDB.openCursor(null, null);
			while((cursor.getNext(indiKey, indiValue, null))==OperationStatus.SUCCESS)
				{
				Individual indi= Individual.BINDING.entryToObject(indiValue);
				Stat stat= stats[indi.getColumn()];
				out.print(indi.getFamily());
				out.print("\t");
				out.print(indi.getFirstName());
				out.print("\t");
				out.print(stat.total);
				out.print("\t");
				out.print(stat.genotyped);
				out.print("\t");
				out.print(stat.success());
				out.print("\t");
				out.print(stat.errors);
				out.println();
				}
			cursor.close();
			out.copyToZip(zout, this.zipPrefix+"individuals.txt");
			out.delete();
			}
		
	
	void assertToken(List<String> token,int index,String ... labels)
	throws IOException
		{
		token.set(index, token.get(index).trim());
		if(token.get(index).startsWith("#"))
			{
			token.set(index, token.get(index).substring(1).trim());
			}
		for(String L:labels)
			{
			if(token.get(index).equalsIgnoreCase(L)) return;
			}
		
		
		throw new IOException("Error in header: expected "+labels[0]+
				" at column "+(index+1)+" but found "+token.get(index));	
		}
	
	

	
	 void makeLinkage(ZipOutputStream zout)
		throws IOException, DatabaseException
		{
		
		DatabaseEntry positionKey=new DatabaseEntry();
		DatabaseEntry markerKey=new DatabaseEntry();
		DatabaseEntry markerValue=new DatabaseEntry();
		DatabaseEntry genotypesEntry=new DatabaseEntry();
		DatabaseEntry index4indiKey=new DatabaseEntry();
		DatabaseEntry indiValue=new DatabaseEntry();
		DatabaseEntry indiKey=new DatabaseEntry();
		DatabaseEntry indiNameEntry= new DatabaseEntry();
		
		Individual individualArray[]=new Individual[this.individualCount];
		Map<Name,Integer> name2column = new HashMap<Name, Integer>(this.individualCount);
		
		//load the individuals
		Cursor cursor=null;
		cursor= this.individualDB.openCursor(null, null);
		while((cursor.getNext(indiKey, indiValue, null))==OperationStatus.SUCCESS)
			{
			Individual child= Individual.BINDING.entryToObject(indiValue);
			assert(child.getColumn()>=0);
			assert(child.getColumn()<individualArray.length);
			assert(individualArray[child.getColumn()]==null);
			individualArray[child.getColumn()]= child;
			name2column.put(child.getName(), child.getColumn());
			}
		cursor.close();
		cursor=null;
		
		
		
		try {
			positionKey = new DatabaseEntry();
			markerValue = new DatabaseEntry();
			//get all the chromosomes
			Set<String> all_chrom=new TreeSet<String>();
			cursor= this.pos2markerDB.openCursor(null, null);
			while((cursor.getNext(positionKey, markerValue, null))==OperationStatus.SUCCESS)
				{
				all_chrom.add(Position.BINDING.entryToObject(positionKey).getChromosome());
				}
			cursor.close();
			
			Giraf._LOG.info(all_chrom.toString());
			
			for(String chromosome:all_chrom)
				{
				Giraf._LOG.info(chromosome);
				TmpWriter linkage = new TmpWriter(giraf.envHome);
				TmpWriter markers= new TmpWriter(giraf.envHome);
				TmpWriter incompats= new TmpWriter(giraf.envHome);
				TmpWriter multiple= null;
				Stat statistics= new Stat();
				statistics.total= this.individualCount;
				int nLine=0;
				
				markers.println("#index\tName\tChrom\tPosition\tTotal\tgenotyped\tSuccess\tErrors\tAlleles\tchi2");
				
				
				cursor= this.pos2markerDB.openCursor(null, null);
				//loop over the positions
				while((cursor.getNext(positionKey, markerValue, null))==OperationStatus.SUCCESS)
					{
					Position pos= Position.BINDING.entryToObject(positionKey);
					//continue if position is not the same chrom
					if(!pos.getChromosome().equals(chromosome))
						{
						continue;
						}
					
					Marker marker= Marker.BINDING.entryToObject(markerValue);
					HardyWeinberg hw=(marker.getAllele2Count().size()==2?new HardyWeinberg(marker):null);
					StringBinding.stringToEntry(marker.getName(), markerKey);
					List<DistinctGenotype> genotypes= null;
					//create the list if we don't have any genotype
					if(this.mkr2genotypesDB.get(null, markerKey, genotypesEntry, null)!=OperationStatus.SUCCESS)
						{
						genotypes= new ArrayList<DistinctGenotype>(individualCount);
						while(genotypes.size()< individualCount)
							{
							genotypes.add(null);
							}
						}
					else
						{
						genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesEntry);
						assert(genotypes.size()== individualCount);
						}
					
					
					
					markers.print(
							String.valueOf(nLine++)+"\t"+
							marker.getName()+"\t"+
							pos.getChromosome()+"\t"+
							pos.getPosition()
							);
					
					
					linkage.print(
							marker.getName()+"\t"+
							pos.getChromosome()+"\t"+
							pos.getPosition()
							);
					
					for(int i=0;i< genotypes.size();++i)
						{
						linkage.print("\t");
						DistinctGenotype array= genotypes.get(i);
						if(array==null)
							{
							linkage.print("0 0");
							}
						else if(array.size()!=1)
							{
							if(multiple==null)
								{
								multiple=new TmpWriter(giraf.envHome);
								}
							
							//retrieve individual's name from index
							IntegerBinding.intToEntry(i,index4indiKey);
							if(this.index2indiviualName.get(null, index4indiKey, indiNameEntry, null)!=OperationStatus.SUCCESS)
								{
								throw new DatabaseException("Cannot retrieve "+i+"th individual");
								}
							//retrieve individual from name
							if(this.individualDB.get(null, indiNameEntry, indiValue, null)!=OperationStatus.SUCCESS)
								{
								throw new DatabaseException("Cannot retrieve "+i+"th individual");
								}
							
							Individual indi= Individual.BINDING.entryToObject(indiValue);
							multiple.print(marker.getName()+"\t"+ indi.getName()+"\tmultiple_genotypes:");
							for(Genotype g: array) multiple.print(" ("+g+")");
							multiple.println();
							
							statistics.errors++;
							linkage.print("0 0");
							}
						else
							{
							statistics.genotyped++;
							Genotype g=array.first();
							hw.see(g);
							linkage.print(marker.allele2index(g.A1()));
							linkage.print(" ");
							linkage.print(marker.allele2index(g.A2()));
							}
						
						}
					
					
					markers.print("\t"+statistics.total+"\t"+
							statistics.genotyped+"\t"+
							statistics.success()+"\t"+
							statistics.errors+"\t"
							);
					
					for(String al:marker.getAllele2Count().keySet())
						{
						markers.print("\""+al+"\"="+marker.allele2index(al)+";");
						}
					markers.print("\t");
					if(marker.getAllele2Count().isEmpty())
						{
						markers.print("never_genotyped");
						}
					else if(marker.getAllele2Count().size()==1)
						{
						markers.print("homozygote");
						}
					else if(marker.getAllele2Count().size()!=2)
						{
						markers.print("not_biallelic");
						}
					else
						{
						assert(hw!=null);
						markers.print(hw.toString());
						}
					
					markers.println();
					linkage.println();
					
					
					/* test the inconpats */
						{
						/* for this marker, loop over all the individuals */
						for(int i=0;i< genotypes.size();++i)
							{
							DistinctGenotype array= genotypes.get(i);
							if(array==null || array.size()!=1) continue;
							
							if(individualArray[i]==null) throw new DatabaseException("individual missing");
							
							Individual child= individualArray[i];
							Genotype gChild=array.first();
							Individual parents[]=new Individual[]{null,null};
							Genotype gParents[]=new Genotype[]{null,null};
							
							for(int side=0;side< 2;++side)
								{
								if(!child.hasParent(side)) continue;
								
								Integer x = name2column.get(child.getParent(side));
								if(x==null) continue;
								
								parents[side] = individualArray[x];
								array= genotypes.get(parents[side].getColumn());
								if(array==null || array.size()!=1) continue;
								gParents[side]=array.first();
								if(FamilyIncompat.childIsNotCompatibleWithOneParent(
										gParents[side].A1(),
										gParents[side].A2(),
										gChild.A1(),
										gChild.A2()))
									{
									incompats.println("For marker "+marker.getName()+
											" Incompat between "+child.getName()+"("+ gChild+") and his "+
											(i==0?"Father":"Mother")+
											" "+parents[side].getName()+
											" ("+ gParents[side]+")"
											);
									break;
									}
								}
							
							if(gParents[0]==null || gParents[1]==null) continue;
							if(FamilyIncompat.parentCannotHaveThisChild(
									gParents[0].A1(),
									gParents[0].A2(),
									gParents[0].A1(),
									gParents[1].A2(),
									gChild.A1(),
									gChild.A2()))
								{
								incompats.println("For marker "+marker.getName()+
										" Incompat between "+child.getName()+"("+ gChild+") and his parent "+
										parents[0].getName()+": ("+ gParents[0]+") /"+
										parents[1].getName()+": ("+ gParents[1]+")"
										);
								break;
								}
							}
						}
					}
				cursor.close();
				
				linkage.copyToZip(zout, this.zipPrefix+chromosome+"/linkage.dat");
				markers.copyToZip(zout, this.zipPrefix+chromosome+"/markers.txt");
				linkage.delete();
				markers.delete();
				if(multiple!=null && !multiple.isEmpty())
					{
					multiple.copyToZip(zout, this.zipPrefix+chromosome+"/multiple.dat");
					multiple.delete();
					}
				if(incompats!=null && !incompats.isEmpty())
					{
					incompats.copyToZip(zout, this.zipPrefix+chromosome+"/incompats.dat");
					incompats.delete();
					}
				}			
			
			} 
		catch (Exception e)
			{
			giraf.errorLog.printStackTrace(e);	
			}
		}
	

	
	
	}

/**
 * Giraf
 *
 */
public class Giraf
	{
	/** tab delimiter */
	private static final Pattern TAB= Pattern.compile("[\t]");
	/** logging */
	static final Logger _LOG= Logger.getLogger("org.lindenb.giraf.Giraf");
	/** number of individual */
	private int individualCount=0;
	/** number of markers */
	private int markerCount=0;
	/** files of markers */
	private Set<String> markerFiles= new HashSet<String>();
	/** files of pedigrees */
	private Set<String> individualsFiles= new HashSet<String>();
	/** files of genotypes */
	private Set<String> genotypesFiles= new HashSet<String>();
	/** files of markers to be excluded */
	private Set<String> eXcludedMarkerFiles= new HashSet<String>();
	/** files of individuals to be excluded */
	private Set<String> eXcludedIndividualFiles= new HashSet<String>();
	
	
	/** limit by position */
	 List<Range> limitByPosition= new ArrayList<Range>();
	
	
	
	
	Environment berkeleyEnv=null;
	TmpWriter errorLog=null;
	TmpWriter logLog=null;
	File envHome;
	
	
	private Giraf()
		{
		this.envHome= new File(System.getProperty("java.io.tmpdir"));
		}

	


	
	private void build(Config config,File fileout)
		throws IOException, DatabaseException
		{
		ZipOutputStream zout=null;
		String random = "_"+System.currentTimeMillis();
		Handler logHandler=null;
		try {
			this.errorLog= new TmpWriter(this.envHome);
			this.logLog= new TmpWriter(this.envHome);
			logHandler= new Handler()
				{
				@Override
				public void flush() {}
				@Override
				public void close() throws SecurityException {}
				@Override
				public void publish(LogRecord record)
					{
					if(logLog==null || logLog.isClosed()) return;
					logLog.println(
						"["+record.getLevel().getName()+"]"+
						"\""+record.getSourceClassName()+"\" "+
						" in "+record.getSourceMethodName()+" :"+
						record.getMessage()
						);
					}
				};
			_LOG.addHandler(logHandler);
			
			/* create BDB environment */
			EnvironmentConfig envCfg= new EnvironmentConfig();
			envCfg.setAllowCreate(true);
			this.berkeleyEnv= new Environment(this.envHome,envCfg);
			
		
			
			Linkage linkage= new Linkage(this);
			
			_LOG.info("read markers");
			for(String f: this.markerFiles)
				{
				_LOG.info("read marker file "+f);
				BufferedReader r= IOUtils.openReader(f);
				linkage.readMarkerDB(new LineIterator(r,TAB));
				r.close();
				}
			_LOG.info("markers.size="+this.markerCount);
			
			//read markers
			for(String f: this.eXcludedMarkerFiles)
				{
				_LOG.info("read excluded marker file "+f);
				BufferedReader r= IOUtils.openReader(f);
				linkage.readExclusedMarkerDB(new LineIterator(r,TAB));
				r.close();
				}
			_LOG.info("markers.size="+this.markerCount);
			
			//read individuals
			for(String f: this.individualsFiles)
				{
				_LOG.info("read individual file "+f);
				BufferedReader r= IOUtils.openReader(f);
				linkage.readPedigreeDB(new LineIterator(r,TAB));
				r.close();
				}
			_LOG.info("individuals.size="+this.individualCount);
			
			//read excluded individuals
			for(String f: this.eXcludedIndividualFiles)
				{
				_LOG.info("read individuals exclusion file "+f);
				BufferedReader r= IOUtils.openReader(f);
				linkage.readXPedigreeDB(new LineIterator(r,TAB));
				r.close();
				}
			_LOG.info("individuals.size="+this.individualCount);
			
			linkage.validatePedigree();
			
			//read genotypes"
			for(String f: this.genotypesFiles)
				{
				_LOG.info("read genotype file "+f);
				BufferedReader r= IOUtils.openReader(f);
				linkage.readGenotypes(new LineIterator(r,TAB));
				r.close();
				}
			
			linkage.updateMakerAlleleCount();
			
			zout= new ZipOutputStream(new FileOutputStream(fileout));
			linkage.makeLinkage(zout);
			linkage.makeIndividualStats(zout);
			if(this.errorLog!=null && !this.errorLog.isEmpty()) 
				{
				this.errorLog.copyToZip(zout, linkage.zipPrefix+"errors.txt");
				}
			if(this.logLog!=null && !this.logLog.isEmpty()) 
				{
				this.logLog.copyToZip(zout, linkage.zipPrefix+"logs.txt");
				}
			zout.flush();
			zout.close();

			_LOG.info("returning "+fileout);
			}
		catch (DatabaseException e)
			{
			IOUtils.safeClose(zout);
			throw e;
			}
		catch (IOException e)
			{
			IOUtils.safeClose(zout);
			throw e;
			}
		finally
			{
			if(logHandler!=null) _LOG.removeHandler(logHandler);
			if(this.errorLog!=null) this.errorLog.delete();
			if(this.logLog!=null) this.logLog.delete();
			if(this.berkeleyEnv!=null) this.berkeleyEnv.close();
			}
		}
	
	/** read a configuration file */
	private void readConfigFile(BufferedReader r,ConfigShuttle config)
		throws IOException
		{
		if(config==null) config=new ConfigShuttle();
		String line;
		String directive=null;
		while((line=r.readLine())!=null)
			{
			if(line.startsWith("#") || line.trim().length()==0) continue;
			if(line.startsWith("[") && line.trim().endsWith("]"))
				{
				directive= line.trim().toLowerCase();
				continue;
				}
			while(true)
				{
				int i= line.indexOf("${");
				if(i==-1) break;
				int j=line.indexOf("}",i+1);
				if(j==-1) break;
				line= line.substring(0,i)+
					config.getProperty(line.substring(i+2,j))+
					(j+1==line.length()?"":line.substring(j+1));
				}
			
			
			if(directive==null)
				{
				System.err.println("Warning: in config file, no [directive] for "+line);
				continue;
				}
			else if(directive.equals("[echo]"))
				{
				System.out.println(line);
				}
			else if(directive.equals("[define]"))
				{
				int i= line.indexOf('=');
				if(i<1)
					{
					System.err.println("Bad [define] : "+line);
					}
				else
					{
					config.properties.setProperty(
							line.substring(0,i).trim(),
							line.substring(i+1).trim());
					}
				}
			else if(directive.equals("[markers]"))
				{
				this.markerFiles.add(line);
				}
			else if(directive.equals("[exclude-markers]"))
				{
				this.eXcludedMarkerFiles.add(line);
				}
			else if(directive.equals("[individuals]"))
				{
				this.individualsFiles.add(line);
				}
			else if(directive.equals("[genotypes]"))
				{
				this.genotypesFiles.add(line);
				}
			else if(directive.equals("[include]"))
				{
				if(config.visitedFiles.contains(line))
					{
					throw new IOException("Config: Loop detected with file "+line);
					}
				BufferedReader fin= IOUtils.openReader(line);
				readConfigFile(fin,config);
				fin.close();
				}
			else
				{
				System.err.println("[Warning] unknown config directive "+directive);
				}
			}
		}
	
	public static void main(String[] args)
		{
		try
			{
			Giraf._LOG.setLevel(Level.ALL);
			Config configuration= null;
			ConfigShuttle config=new ConfigShuttle();
			File fileout=null;
			Giraf app= new Giraf();
			int optind=0;
			
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Options:");
					System.err.println(" -w <dir> db directory. Default: "+app.envHome);
					System.err.println(" -D property=value");
					System.err.println(" -debug <value=[OFF,SEVERE,FINE,FINER,FINEST,INFO]>");
					System.err.println(" -o <file-out.zip> [REQUIRED]");
					System.err.println(" -L limit by position chr1:0-10;chr3:10-20 ");
					System.err.println(" -m <uri> add a marker url/file (may be gzipped) ");
					System.err.println(" -xm <uri> add a marker url/file to be excluded (may be gzipped) ");
					System.err.println(" -p <uri> add a pedigree url/file (may be gzipped) ");
					System.err.println(" -g <uri> add a genotype url/file (may be gzipped) ");
					
					return;
					}
				else if(args[optind].equals("-o"))
					{
					fileout= new File(args[++optind]);
					}
				else if(args[optind].equals("-m"))
					{
					app.markerFiles.add(args[++optind]);
					}
				else if(args[optind].equals("-xm"))
					{
					app.eXcludedMarkerFiles.add(args[++optind]);
					}
				else if(args[optind].equals("-p"))
					{
					app.individualsFiles.add(args[++optind]);
					}
				else if(args[optind].equals("-g"))
					{
					app.genotypesFiles.add(args[++optind]);
					}
				else if(args[optind].equals("-L"))
					{
					for(String s: args[++optind].split("[;]"))
						{
						s=s.trim();
						if(s.length()==0) continue;
						int loc1= s.indexOf(':');
						int loc2= s.indexOf('-',loc1+1);
						if(loc1==-1 || loc2==-1) 
							{
							System.err.println("Bad range "+s);
							return;
							}
						Range r= new Range(
							s.substring(0,loc1).trim(),
							Integer.parseInt(s.substring(0,loc2).substring(loc1+1).trim()),
							Integer.parseInt(s.substring(loc2+1).trim())
							);
						app.limitByPosition.add(r);
						}
					}
				else if(args[optind].equals("-w"))
					{
					File dir= new File(args[++optind]);
					if(!dir.exists())
						{
						System.err.println(dir.toString()+" doesn't exists");
						return;
						}
					if(!dir.isDirectory())
						{
						System.err.println(dir.toString()+" is not a directory");
						return;
						}
					app.envHome= dir;
					}
				else if(args[optind].equals("-debug"))
					{
					try
						{
						Giraf._LOG.setLevel(Level.parse(args[++optind]));
						}
					catch(Exception err)
						{
						System.err.println("otion -debug failed: "+err.getMessage());
						return;
						}
					}
				else if(args[optind].equals("-D"))
					{
					String line= args[++optind];
					int i= line.indexOf('=');
					if(i<=0)
						{
						System.err.println("Bad definition "+line);
						return;
						}
					else
						{
						String left= line.substring(0,i).trim();
						config.properties.setProperty(left, line.substring(i+1).trim());

						}
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					return;
					}
				else 
					{
					break;
					}
				++optind;
				}
			
			if(fileout==null)
				{
				System.err.println("Output file name was not declared");
				return;
				}
			
			if(optind==args.length)
				{
				_LOG.info("reading stdin");
				app.readConfigFile(new BufferedReader(new InputStreamReader(System.in)),config);
				_LOG.info("reading done");
				}	
			else
				{
				while(optind< args.length)
					{
					String uri=args[optind++];
					config.visitedFiles.add(uri);
					BufferedReader fin=IOUtils.openReader(uri);
					app.readConfigFile(fin,config);
					fin.close();
					}
				}
			_LOG.info("start building");
			app.build(configuration,fileout);
			_LOG.info("Done."+fileout);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
