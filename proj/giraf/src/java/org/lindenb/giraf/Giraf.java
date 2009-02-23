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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import org.lindenb.io.IOUtils;
import org.lindenb.io.TmpWriter;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;
import org.lindenb.util.SmartComparator;
import org.lindenb.util.TimeUtils;

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
	private static final NameBinding BINDING_NAME= new NameBinding();
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
		a1=a1.toLowerCase();
		a2=a2.toLowerCase();
		if(a1.compareTo(a2)<=0)
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
	@Override
	public String toString() {
		return A1()+"/"+A2();
		}
	}

class GenotypesBinding
	extends TupleBinding<List<Genotype[]>>
	{
	static public final GenotypesBinding INSTANCE=new GenotypesBinding();
	
	@Override
	public List<Genotype[]> entryToObject(TupleInput tuple)
		{
		int n= tuple.readInt();
		List<Genotype[]> list= new ArrayList<Genotype[]>(n);
		for(int i=0;i< n;++i)
			{
			boolean b= tuple.readBoolean();
			if(!b)
				{
				list.add(null);
				}
			else
				{
				Genotype array[]=new Genotype[tuple.readInt()];
				for(int k=0;k< array.length;++k)
					{
					array[k] = new Genotype(tuple.readString(),tuple.readString());
					}
				list.add(array);
				}
			}
		return list;
		}
	@Override
	public void objectToEntry(List<Genotype[]> list, TupleOutput tuple)
		{
		tuple.writeInt(list.size());
		for(int i=0;i< list.size();++i)
			{
			if(list.get(i)==null || list.get(i).length==0)
				{
				tuple.writeBoolean(false);
				}
			else
				{
				tuple.writeBoolean(true);
				tuple.writeInt(list.get(i).length);
				for(Genotype g: list.get(i))
					{
					tuple.writeString(g.A1());
					tuple.writeString(g.A2());
					}
				}
			}
		}
	}

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
 * @author pierre
 *
 */
public class Giraf
	{
	/** tab delimiter */
	private static final Pattern TAB= Pattern.compile("[\t]");
	/** logging */
	private static final Logger _LOG= Logger.getLogger("org.lindenb.giraf.Giraf");
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
	private List<Range> limitByPosition= new ArrayList<Range>();
	
	private Set<String> allPhenotypes= new TreeSet<String>();
	private Set<String> allMarkerFeatures= new TreeSet<String>();
	
	
	private Environment berkeleyEnv=null;
	private Database markerDB = null;
	private SecondaryDatabase pos2markerDB= null;
	private Database individualDB = null;
	private SecondaryDatabase index2individual= null;
	private Database mkr2genotypesDB = null;
	private TmpWriter errorLog=null;
	private TmpWriter logLog=null;
	private File envHome;
	private boolean test_the_incompats=false;
	
	private final String zipPrefix= "GIRAF"+TimeUtils.toYYYYMMDDHHMMSS()+"/";
	private Giraf()
		{
		this.envHome= new File(System.getProperty("java.io.tmpdir"));
		}
	
	private static void trim(String token[])
		{
		for(int i=0;i< token.length;++i) token[i]=token[i].trim();
		}
	
	private static void assertToken(String token[],int index,String ... labels)
	throws IOException
		{
		token[index]=token[index].trim();
		if(token[index].startsWith("#"))
			{
			token[index]=token[index].substring(1).trim();
			}
		for(String L:labels)
			{
			if(token[index].equalsIgnoreCase(L)) return;
			}
		
		
		throw new IOException("Error in header: expected "+labels[0]+
				" at column "+(index+1)+" but found "+token[index]);	
		}
	
	/** read pedigrees to be excluded */
	private void readXPedigreeDB(String pedFile)
	throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openReader(pedFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0) continue;
			String token[] = TAB.split(line);
			if(header!=null && line.startsWith("#")) continue;
			if(token.length<2) throw new IOException("In "+pedFile+" expected 5 columns but found "+token.length+" in "+line);
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Family","fam");
				assertToken(header,1,"Individual","indi");
				continue;
				}
			trim(token);
			if(token[0].length()==0 || token[1].length()==0 || !Cast.Integer.isA(token[1])) continue;
			
			
			Name name=new Name(token[0],Cast.Integer.cast(token[1]));
			if(this.individualDB.delete(null,name.copyToEntry(key))== OperationStatus.SUCCESS)
				{
				_LOG.info("removing "+name);
				this.individualCount--;
				assert(this.individualCount>=0);
				continue;
				}
			}
		r.close();
		if(header==null)
			{
			System.err.println("Nothing found in "+pedFile);
			}
		}
	
	private void readPedigreeDB(String pedFile)
	throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openReader(pedFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0) continue;
			String token[] = TAB.split(line);
			if(header!=null && line.startsWith("#")) continue;
			if(token.length<5) throw new IOException("In "+pedFile+" expected 5 columns but found "+token.length+" in "+line);
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Family","fam");
				assertToken(header,1,"Individual","indi","name");
				assertToken(header,2,"Father","f");
				assertToken(header,3,"Mother","m");
				assertToken(header,4,"Gender","sex");
				for(int i=5;i<token.length && i< header.length;++i)
					{
					_LOG.info("add phenotype "+header[i]);
					this.allPhenotypes.add(header[i]);
					}
				continue;
				}
			
			if(token[0].trim().length()==0) throw new IOException("In "+pedFile+" bad family name in "+line);
			if(!Cast.Integer.isA(token[1])) throw new IOException("In "+pedFile+" bad name in "+line);
			if(token[2].trim().length()==0) token[2]="0";
			if(!Cast.Integer.isA(token[2])) throw new IOException("In "+pedFile+" bad father in "+line);
			if(token[3].trim().length()==0) token[3]="0";
			if(!Cast.Integer.isA(token[3])) throw new IOException("In "+pedFile+" bad mother in "+line);
			if(token[4].trim().length()==0) token[4]="0";
			int gender= Cast.Integer.cast(token[4]);
			if(gender<0 || gender>2) throw new IOException("In "+pedFile+" bad gender in "+line);
			Individual indi= new Individual(
					token[0],
					Cast.Integer.cast(token[1]),
					Cast.Integer.cast(token[2]),
					Cast.Integer.cast(token[3]),
					gender
					);
			
			for(int i=5;i<token.length && i< header.length;++i)
				{
				indi.getPhenotypes().put(header[i], token[i]);
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
		r.close();
		if(header==null)
			{
			System.err.println("Nothing found in "+pedFile);
			}
		}
	
	private void validatePedigree()
	throws IOException, DatabaseException
		{
		DatabaseEntry key = new DatabaseEntry();
	    DatabaseEntry value = new DatabaseEntry();
	    
	    DatabaseEntry pKey = new DatabaseEntry();
	    DatabaseEntry pValue = new DatabaseEntry();
	    
	    int column=0;
		Cursor cursor= null;
		try
			{
			cursor=this.individualDB.openCursor(null, null);
			while (cursor.getNext(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS)
				{
				Individual indi= Individual.BINDING.entryToObject(value);
				//UPDATE THE COLUMN
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
	
	
	/**
	 * readMarkerDB
	 * read a file of marker
	 * @param markerSrcURI
	 * @throws IOException
	 * @throws DatabaseException
	 */
	private void readMarkerDB(String markerSrcURI)
	throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openReader(markerSrcURI);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if( line.trim().length()==0 ||
				(header!=null && line.startsWith("#"))) continue;
			
			String token[] = TAB.split(line);
			if(token.length<3) throw new IOException("In "+markerSrcURI+" expected 3 columns but found "+token.length+" in "+line);
			
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Name","rs","snp","rs#","marker");
				assertToken(header,1,"Chromosome","k","chr","chrom");
				assertToken(header,2,"Position","pos","loc","location");
				for(int i=3;i<token.length && i< header.length;++i)
					{
					_LOG.info("adding current snp feature "+header[i]);
					this.allMarkerFeatures.add(header[i]);
					}
				continue;
				}
			
			
			if(token[0].trim().length()==0) throw new IOException("In "+markerSrcURI+" bad marker name in "+line);
			
			if(token[1].trim().length()==0 ||
			   token[2].trim().length()==0)
				{
				token[2]="-1";
				}
			if(!Cast.Integer.isA(token[2])) throw new IOException("In "+markerSrcURI+" bad position in "+line);
			Marker marker = new Marker(token[0].trim(),token[1],Cast.Integer.cast(token[2]));
			
			if(!this.limitByPosition.isEmpty())
				{
				boolean ok=false;
				for(Range range: this.limitByPosition)
					{
					if(range.contains(marker.getPosition()))
						{
						ok=true;
						break;
						}
					}
				if(!ok) continue;
				}
			
			for(int i=3;i<token.length && i< header.length;++i)
				{
				marker.getFeatures().put(header[i], token[i]);
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
		r.close();
		if(header==null)
			{
			System.err.println("Empty data in "+markerSrcURI);
			}
		}
	
	
	private void readExclusedMarkerDB(String markerFile)
		throws IOException, DatabaseException
		{
		_LOG.fine("readExclusedMarkerDB:" + markerFile);
		BufferedReader r= IOUtils.openReader(markerFile);
		String line=null;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0) continue;
			String token[] = TAB.split(line);
			if(header!=null && line.startsWith("#")) continue;
			
			if(header==null)
				{
				header= token;
				assertToken(header,0,"Name");
				continue;
				}
			
			
			StringBinding.stringToEntry(token[0].trim(), key);
			if(this.markerDB.get(null,key,value,null )!= OperationStatus.SUCCESS)
				{
				continue;
				}
			
			_LOG.fine("removing "+header[0]);
			if(this.markerDB.delete(null, key)!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot remove "+token[0]);
				}
			this.markerCount--;
			}
		r.close();
		}
	/**
	 * Read a genotype file
	 * @param genFile
	 * @throws IOException
	 * @throws DatabaseException
	 */
	private  void readGenotypes(String genFile)
		throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openReader(genFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry markerValue =new DatabaseEntry();
		DatabaseEntry indiValue =new DatabaseEntry();
		DatabaseEntry genotypesValue =new DatabaseEntry();
		String prevMarker=null;
		List<Genotype[]> genotypes= null;
		
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0 ) continue;
			if(header!=null && line.startsWith("#")) continue;
			
			String token[] = TAB.split(line);
			
			trim(token);
			if(header==null)
				{
				if(token.length<5) throw new IOException("In "+genFile+" expected 5 columns but found "+token.length+" in "+line);
				header= token;
				assertToken(header,0,"Marker","rs","snp","rs#");
				assertToken(header,1,"Family","fam");
				assertToken(header,2,"Individual","indi","name");
				assertToken(header,3,"Allele-1","A1");
				assertToken(header,4,"Allele-2","A2");
				continue;
				}
			
			if(token.length!=3 && token.length!=5) throw new IOException("In "+genFile+" expected 5 columns but found "+token.length+" in "+line);
			
			if(token.length==3 ||
			   token[0].length()==0 ||
			   token[1].length()==0 ||
			   token[2].length()==0 ||
			   token[3].length()==0 ||
			   token[4].length()==0 ||
			   token[3].equalsIgnoreCase("N") ||
			   token[3].equals("-") ||
			   token[3].equals("?") ||
			   token[3].equals("0") ||
			   token[4].equalsIgnoreCase("N") ||
			   token[4].equals("-") ||
			   token[4].equals("?") ||
			   token[4].equals("0")
			 	)
				{
				prevMarker=null;
				genotypes=null;
				//System.err.println("In "+genFile+" ignoring "+line);
				continue;
				}
			
			
			if(!Cast.Integer.isA(token[2]))
				{
				throw new IOException("Bad family (not name=integer) in "+line+" in "+genFile);
				}
			
			if(prevMarker!=null &&
				prevMarker.equals(token[0]) &&
				genotypes==null)
				{
				//marker is the same but it is not in our listing
				continue;
				}
			
			new Name(token[1],Cast.Integer.cast(token[2])).copyToEntry(key);
			if(this.individualDB.get(null,key,indiValue,null )!= OperationStatus.SUCCESS)
				{
				//individual is not in our collection
				continue;
				}
			
			Individual individual= Individual.BINDING.entryToObject(indiValue);
			
			//now key is for marker, need this to save AND load the genotypes
			StringBinding.stringToEntry(token[0], key);
			
			/* if marker is same as previous no need to retrieve the array */
			if(prevMarker==null || !prevMarker.equals(token[0]))
				{
				prevMarker=token[0];
				if(this.markerDB.get(null,key,markerValue,null )!= OperationStatus.SUCCESS)
					{
					//marker is noty in out listing
					genotypes=null;
					continue;
					}

				if(this.mkr2genotypesDB.get(null,key,genotypesValue,null )!= OperationStatus.SUCCESS)
					{
					genotypes = new ArrayList<Genotype[]>(individualCount);
					while(genotypes.size()< individualCount) genotypes.add(null);
					}
				else
					{
					genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesValue);
					}
				}
			else
				{
				prevMarker=token[0];
				assert(genotypes!=null);
				}
			
			Genotype genotype=new Genotype(token[3],token[4]);
			Genotype gen_array[]= genotypes.get(individual.getColumn());
			if(gen_array==null || gen_array.length==0)
				{
				gen_array=new Genotype[]{genotype};
				}
			else
				{
				boolean found=false;
				for(Genotype other:gen_array)
					{	
					if(other.equals(genotype))
						{
						found=true;
						break;
						}
					}
				if(found) continue;
				Genotype copy[]=new  Genotype[gen_array.length+1];
				System.arraycopy( gen_array, 0, copy, 0,gen_array.length);
				copy[gen_array.length]=genotype;
				gen_array=copy;
				}
			genotypes.set(individual.getColumn(),gen_array);
			GenotypesBinding.INSTANCE.objectToEntry(genotypes, genotypesValue);
			if(this.mkr2genotypesDB.put(null,key,genotypesValue )!= OperationStatus.SUCCESS)
				{
				throw new DatabaseException("Cannot put genotypes for "+token[0]);
				}
			}
		r.close();
		
		
		if(header==null)
			{
			System.err.println("Empty data in "+genFile);
			}
		}
	
	private void updateMakerAlleleCount()
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
				_LOG.info("marker is not in database");
				continue;
				}
			List<Genotype[]> genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesEntry);
			assert(genotypes.size()==this.individualCount);
			for(int i=0;i< genotypes.size();++i)
				{
				Genotype array[]=genotypes.get(i);
				if(array==null || array.length!=1) continue;
				Genotype g=array[0];
				for(int j=0;j< 2;++j)
					{
					String a= (j==0?g.A1():g.A2());
					Integer count = marker.getAllele2Count().get(a);
					if(count==null) count=0;
					marker.getAllele2Count().put(a,count+1);
					}
				}
			_LOG.info("marker alleles "+marker.getAllele2Count());
			Marker.BINDING.objectToEntry(marker,markerValue);
			cursor.putCurrent(markerValue);
			}
		cursor.close();
		}
	
	private void makeLinkage(ZipOutputStream zout)
		throws IOException, DatabaseException
		{
		
		DatabaseEntry positionKey=new DatabaseEntry();
		DatabaseEntry markerKey=new DatabaseEntry();
		DatabaseEntry markerValue=new DatabaseEntry();
		DatabaseEntry genotypesEntry=new DatabaseEntry();
		DatabaseEntry index4indiKey=new DatabaseEntry();
		DatabaseEntry indiValue=new DatabaseEntry();
		DatabaseEntry indiKey=new DatabaseEntry();
		
		Cursor cursor=null;
		try {
			//get all the chromosomes
			Set<String> all_chrom=new TreeSet<String>();
			cursor= this.pos2markerDB.openCursor(null, null);
			while((cursor.getNext(positionKey, markerValue, null))==OperationStatus.SUCCESS)
				{
				all_chrom.add(Position.BINDING.entryToObject(positionKey).getChromosome());
				}
			cursor.close();
			
			_LOG.info(all_chrom.toString());
			
			for(String chromosome:all_chrom)
				{
				_LOG.info(chromosome);
				TmpWriter linkage = new TmpWriter(this.envHome);
				TmpWriter markers= new TmpWriter(this.envHome);
				TmpWriter incompats= (this.test_the_incompats? new TmpWriter(this.envHome):null);
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
					List<Genotype[]> genotypes= null;
					//create the list if we don't have any genotype
					if(this.mkr2genotypesDB.get(null, markerKey, genotypesEntry, null)!=OperationStatus.SUCCESS)
						{
						genotypes= new ArrayList<Genotype[]>(individualCount);
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
						Genotype array[]= genotypes.get(i);
						if(array==null)
							{
							linkage.print("0 0");
							}
						else if(array.length!=1)
							{
							if(multiple==null)
								{
								multiple=new TmpWriter(this.envHome);
								}
							//retrieve individual
							IntegerBinding.intToEntry(i,index4indiKey);
							if(this.index2individual.get(null, index4indiKey, indiValue, null)!=OperationStatus.SUCCESS)
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
							Genotype g=array[0];
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
					
					
					if(this.test_the_incompats)
						{
						/* for this marker, loop over all the individuals */
						for(int i=0;i< genotypes.size();++i)
							{
							Genotype array[]= genotypes.get(i);
							if(array==null || array.length!=1) continue;
							
							IntegerBinding.intToEntry(i,index4indiKey);
							if(this.index2individual.get(null, index4indiKey, indiValue, null)!=OperationStatus.SUCCESS)
								{
								errorLog.println("Cannot retrieve "+i+"th individual");
								continue;
								}
							
							Individual child= Individual.BINDING.entryToObject(indiValue);
							Genotype gChild=array[0];
							Individual parents[]=new Individual[]{null,null};
							Genotype gParents[]=new Genotype[]{null,null};
							
							for(int side=0;side< 2;++side)
								{
								if(!child.hasParent(side)) continue;
								child.getParent(side).copyToEntry(indiKey);
								if(this.individualDB.get(null, indiKey, indiValue, null)!=OperationStatus.SUCCESS)
									{
									errorLog.println("Cannot retrieve "+child.getParent(side)+" individual");
									continue;
									}
								parents[side] = Individual.BINDING.entryToObject(indiValue);
								array= genotypes.get(parents[side].getColumn());
								if(array==null || array.length!=1) continue;
								gParents[side]=array[0];
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
			errorLog.printStackTrace(e);	
			}
		}
	
	private void makeIndividualStats(ZipOutputStream zout)
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
			List<Genotype[]> genotypes=GenotypesBinding.INSTANCE.entryToObject(genotypesValue);
			for(int i=0;i< stats.length;++i)
				{
				Genotype array[]= genotypes.get(i);
				if(array==null)
					{
					//ignore
					}
				else if(array.length==1)
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
		
		TmpWriter out= new TmpWriter(this.envHome);
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
	
	
	
	private void build(File fileout)
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
			
			/* create marker database: map<rs,marker> */
			DatabaseConfig cfg= new DatabaseConfig();
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			this.markerDB= this.berkeleyEnv.openDatabase(null, "markers"+random, cfg);
			
			/* create position-to-marker 2nd-database: map<position,marker> */
			SecondaryConfig secondCfg= new SecondaryConfig();
			secondCfg.setAllowCreate(true);
			secondCfg.setSortedDuplicates(true);
			secondCfg.setTemporary(true);
			secondCfg.setKeyCreator(new PositionKeyCreator());
			this.pos2markerDB= this.berkeleyEnv.openSecondaryDatabase(null, "pos"+random, this.markerDB, secondCfg);
			
			/* create marker database: map<name,individual> */
			cfg= new DatabaseConfig();
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			this.individualDB= this.berkeleyEnv.openDatabase(null, "individuals"+random, cfg);
			
			/* create marker database: map<rs,list<genotype[]> > */
			cfg= new DatabaseConfig();
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			mkr2genotypesDB= this.berkeleyEnv.openDatabase(null, "genotypes"+random, cfg);

			secondCfg= new SecondaryConfig();
			secondCfg.setAllowCreate(true);
			secondCfg.setSortedDuplicates(true);
			secondCfg.setTemporary(true);
			secondCfg.setKeyCreator(new SecondaryKeyCreator()
				{
				@Override
				public boolean createSecondaryKey(SecondaryDatabase arg0,
						DatabaseEntry keyEntry, DatabaseEntry dataEntry, DatabaseEntry resultEntry)
						throws DatabaseException
					{
					Individual indi = Individual.BINDING.entryToObject(dataEntry);
					IntegerBinding.intToEntry(indi.getColumn(),  resultEntry);
					return true;
					}
				});
			this.index2individual= this.berkeleyEnv.openSecondaryDatabase(null, "id2individual"+random, this.individualDB, secondCfg);
			
			
			
			_LOG.info("read markers");
			for(String f: this.markerFiles)
				{
				_LOG.info("read marker file "+f);
				readMarkerDB(f);
				}
			_LOG.info("markers.size="+this.markerCount);
			
			//read markers
			for(String f: this.eXcludedMarkerFiles)
				{
				_LOG.info("read excluded marker file "+f);
				readExclusedMarkerDB(f);
				}
			_LOG.info("markers.size="+this.markerCount);
			
			//read individuals
			for(String f: this.individualsFiles)
				{
				_LOG.info("read individual file "+f);
				readPedigreeDB(f);
				}
			_LOG.info("individuals.size="+this.individualCount);
			
			//read excluded individuals
			for(String f: this.eXcludedIndividualFiles)
				{
				_LOG.info("read individuals exclusion file "+f);
				readXPedigreeDB(f);
				}
			_LOG.info("individuals.size="+this.individualCount);
			
			validatePedigree();
			
			//read genotypes"
			for(String f: this.genotypesFiles)
				{
				_LOG.info("read genotype file "+f);
				readGenotypes(f);
				}
			
			updateMakerAlleleCount();
			
			zout= new ZipOutputStream(new FileOutputStream(fileout));
			makeLinkage(zout);
			makeIndividualStats(zout);
			if(this.errorLog!=null && !this.errorLog.isEmpty()) 
				{
				this.errorLog.copyToZip(zout, this.zipPrefix+"errors.txt");
				}
			if(this.logLog!=null && !this.logLog.isEmpty()) 
				{
				this.logLog.copyToZip(zout, this.zipPrefix+"logs.txt");
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
			if(this.mkr2genotypesDB!=null) this.mkr2genotypesDB.close();
			
			if(this.pos2markerDB!=null) this.pos2markerDB.close();
			if(this.markerDB!=null) this.markerDB.close();
			
			
			if(this.index2individual!=null) this.index2individual.close();
			if(this.individualDB!=null) this.individualDB.close();
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
					System.err.println(" -inc test the incompats ");
					System.err.println(" -L limit by position chr1:0-10;chr3:10-20 ");
					return;
					}
				else if(args[optind].equals("-o"))
					{
					fileout= new File(args[++optind]);
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
				else if(args[optind].equals("-inc"))
					{
					app.test_the_incompats=true;
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
			app.build(fileout);
			_LOG.info("Done."+fileout);
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
