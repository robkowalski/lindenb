/**
 * http://www.oracle.com/technology/documentation/berkeley-db/je/GettingStartedGuide/index.html
 */
package org.lindenb.giraf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.lindenb.io.IOUtils;
import org.lindenb.io.TmpWriter;
import org.lindenb.util.Cast;
import org.lindenb.util.Compilation;
import org.lindenb.util.Couple;
import org.lindenb.util.SmartComparator;
import org.lindenb.util.TimeUtils;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
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
 
class Name
	implements Comparable<Name>
	{
	public static final NameBinding BINDING= new NameBinding();
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
	}

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

class Individual
	extends Name
	{
	public static final IndividualBinding BINDING= new IndividualBinding();
	private int father;
	private int mother;
	private int gender;
	private SortedMap<String, String> phenotypes=new TreeMap<String, String>();
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
		return 100.0*(genotyped/total);
		}
	}

/**
 * @author pierre
 *
 */
public class Giraf
	{
	private Pattern TAB= Pattern.compile("[\t]");
	private Set<File> markerFiles= new HashSet<File>();
	private Set<File> individualsFiles= new HashSet<File>();
	private Set<File> genotypesFiles= new HashSet<File>();
	private Set<String> allPhenotypes= new TreeSet<String>();
	private Set<String> allMarkerFeatures= new TreeSet<String>();
	private Environment berkeleyEnv=null;
	private Database markerDB = null;
	private SecondaryDatabase pos2markerDB= null;
	private Database individualDB = null;
	private Database mkr2genotypesDB = null;
	private TmpWriter errorLog=null;
	private File envHome;
	private final String zipPrefix= "GIRAF"+TimeUtils.toYYYYMMDDHHMMSS()+"/";
	private Giraf()
		{
		this.envHome= new File(System.getProperty("java.io.tmpdir"));
		}
	
	private static void trim(String token[])
		{
		for(int i=0;i< token.length;++i) token[i]=token[i].trim();
		}
	
	private static void assertToken(String token[],String label,int index)
	throws IOException
		{
		token[index]=token[index].trim();
		if(token[index].startsWith("#"))
			{
			token[index]=token[index].substring(1).trim();
			}
		if(!token[index].equals(label))
			{
			throw new IOException("Error in header: expected "+label+" at column "+(index+1)+" but found "+token[index]);
			}
		}
	
	
	private void readPedigreeDB(File pedFile)
	throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openFile(pedFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0) continue;
			String token[] = TAB.split(line);
			if(token.length<5) throw new IOException("In "+pedFile+" expected 5 columns but found "+token.length+" in "+line);
			if(header==null)
				{
				header= token;
				assertToken(header,"Family",0);
				assertToken(header,"Individual",1);
				assertToken(header,"Father",2);
				assertToken(header,"Mother",3);
				assertToken(header,"Gender",4);
				for(int i=5;i<token.length && i< header.length;++i)
					{
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
			
			Name.BINDING.objectToEntry(indi.getName(), key);
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
		Cursor cursor= this.individualDB.openCursor(null, null);
		while (cursor.getNext(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
			Individual indi= Individual.BINDING.entryToObject(value);
			//UPDATE THE COLUMN
			indi.setColumn(column++);
			
			
			for(int i=0;i< 2;++i)
				{
				Name parentName= (i==0?indi.getFather():indi.getMother());
				if(parentName.getFirstName()==0) continue;
				Name.BINDING.objectToEntry(parentName, pKey);
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
		cursor.close();
		}
	
	
	private void readMarkerDB(File markerFile)
		throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openFile(markerFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry value=new DatabaseEntry();
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0 || line.startsWith("#")) continue;
			String token[] = TAB.split(line);
			if(token.length<3) throw new IOException("In "+markerFile+" expected 3 columns but found "+token.length+" in "+line);
			
			if(header==null)
				{
				header= token;
				assertToken(header,"Name",0);
				assertToken(header,"Chromosome",1);
				assertToken(header,"Position",2);
				for(int i=3;i<token.length && i< header.length;++i)
					{
					this.allMarkerFeatures.add(header[i]);
					}
				continue;
				}
			
			if(token[0].trim().length()==0) throw new IOException("In "+markerFile+" bad marker name in "+line);
			
			if(token[1].trim().length()==0 ||
			   token[2].trim().length()==0)
				{
				token[2]="-1";
				}
			if(!Cast.Integer.isA(token[2])) throw new IOException("In "+markerFile+" bad position in "+line);
			Marker marker = new Marker(token[0].trim(),token[1],Cast.Integer.cast(token[2]));
			
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
			}
		r.close();
		if(header==null)
			{
			System.err.println("Empty data in "+markerFile);
			}
		}
	
	private  void readGenotypes(File genFile)
		throws IOException, DatabaseException
		{
		BufferedReader r= IOUtils.openFile(genFile);
		String line;
		String header[]=null;
		DatabaseEntry key=new DatabaseEntry();
		DatabaseEntry markerValue =new DatabaseEntry();
		DatabaseEntry indiValue =new DatabaseEntry();
		DatabaseEntry genotypesValue =new DatabaseEntry();
		int individualCount= (int)this.individualDB.count();
		String prevMarker=null;
		List<Genotype[]> genotypes= null;
		
		while((line=r.readLine())!=null)
			{
			if(line.trim().length()==0 || line.startsWith("#")) continue;
			String token[] = TAB.split(line);
			
			trim(token);
			if(header==null)
				{
				if(token.length<5) throw new IOException("In "+genFile+" expected 5 columns but found "+token.length+" in "+line);
				header= token;
				assertToken(header,"Marker",0);
				assertToken(header,"Family",1);
				assertToken(header,"Individual",2);
				assertToken(header,"Allele-1",3);
				assertToken(header,"Allele-2",4);
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
				System.err.println("In "+genFile+" ignoring "+line);
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
			
			Name.BINDING.objectToEntry(new Name(token[1],Cast.Integer.cast(token[2])), key);
			if(this.individualDB.get(null,key,indiValue,null )!= OperationStatus.SUCCESS)
				{
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
				continue;
				}
			List<Genotype[]> genotypes= GenotypesBinding.INSTANCE.entryToObject(genotypesEntry);
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
			Marker.BINDING.objectToEntry(marker,markerValue);
			cursor.putCurrent(markerValue);
			}
		}
	
	private void makeLinkage(ZipOutputStream zout)
		throws IOException, DatabaseException
		{
		
		DatabaseEntry positionKey=new DatabaseEntry();
		DatabaseEntry markerKey=new DatabaseEntry();
		DatabaseEntry markerValue=new DatabaseEntry();
		DatabaseEntry genotypesEntry=new DatabaseEntry();
		int individualCount= (int)individualDB.count();
		Cursor cursor;
		try {
			//get all the chromosomes
			Set<String> all_chrom=new TreeSet<String>();
			cursor= this.pos2markerDB.openCursor(null, null);
			while((cursor.getNext(positionKey, markerValue, null))==OperationStatus.SUCCESS)
				{
				all_chrom.add(Position.BINDING.entryToObject(positionKey).getChromosome());
				}
			cursor.close();
			
			for(String chromosome:all_chrom)
				{
				TmpWriter linkage = new TmpWriter();
				cursor= this.pos2markerDB.openCursor(null, null);
				while((cursor.getNext(positionKey, markerValue, null))==OperationStatus.SUCCESS)
					{
					Position pos= Position.BINDING.entryToObject(positionKey);
					if(!pos.getChromosome().equals(chromosome)) continue;
					
					Marker marker= Marker.BINDING.entryToObject(markerValue);
					
					StringBinding.stringToEntry(marker.getName(), markerKey);
					List<Genotype[]> genotypes= null;
					if(this.mkr2genotypesDB.get(null, markerKey, genotypesEntry, null)!=OperationStatus.SUCCESS)
						{
						genotypes= new ArrayList<Genotype[]>(individualCount);
						while(genotypes.size()< individualCount)
							{
							genotypes.add(null);
							}
						}
					
					
					
					linkage.print(
							marker.getName()+"\t"+
							pos.getChromosome()+"\t"+
							pos.getPosition()
							);
					
					for(int i=0;i< genotypes.size();++i)
						{
						linkage.print("\t");
						Genotype array[]= genotypes.get(i);
						if(array==null || array.length!=1)
							{
							linkage.print("0 0");
							}
						else
							{
							Genotype g=array[0];
							linkage.print(marker.allele2index(g.A1()));
							linkage.print(" ");
							linkage.print(marker.allele2index(g.A2()));
							}
						
						}
					linkage.println();
					
					}
				cursor.close();
				
				linkage.copyToZip(zout, this.zipPrefix+chromosome+"/linkage.dat");
				linkage.delete();
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
		int countIndividual= (int)individualDB.count();
		int countMarker= (int)markerDB.count();
		Stat stats[]=new Stat[countIndividual];
		for(int i=0;i< countIndividual;++i)
			{
			stats[i]=new Stat();
			stats[i].total=countMarker;
			}
		Cursor cursor= mkr2genotypesDB.openCursor(null, null);
		while((cursor.getNext(markerKey, genotypesValue, null))==OperationStatus.SUCCESS)
			{
			List<Genotype[]> genotypes=GenotypesBinding.INSTANCE.entryToObject(genotypesValue);
			for(int i=0;i< stats.length;++i)
				{
				Genotype array[]= genotypes.get(i);
				if(array[i]==null)
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
		TmpWriter out= new TmpWriter();
		out.println("Family\tName\tTotal\tGenotyped\t%\tErrors");
		cursor= individualDB.openCursor(null, null);
		while((cursor.getNext(indiKey, indiValue, null))==OperationStatus.SUCCESS)
			{
			Individual indi= Individual.BINDING.entryToObject(indiValue);
			Stat stat= stats[indi.getColumn()];
			out.print(indi.getFamily());
			out.print("\t");
			out.print(indi.getName());
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
		
		out.delete();
		}
	
	public void build()
		throws IOException, DatabaseException
		{
		try {
			EnvironmentConfig envCfg= new EnvironmentConfig();
			envCfg.setAllowCreate(true);
			this.berkeleyEnv= new Environment(this.envHome,envCfg);
			
			DatabaseConfig cfg= new DatabaseConfig();
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			this.markerDB= this.berkeleyEnv.openDatabase(null, "markers", cfg);
			
			SecondaryConfig secondCfg= new SecondaryConfig();
			secondCfg.setAllowCreate(true);
			secondCfg.setSortedDuplicates(true);
			secondCfg.setKeyCreator(new PositionKeyCreator());
			this.pos2markerDB= this.berkeleyEnv.openSecondaryDatabase(null, "pos", this.markerDB, secondCfg);
			
			
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			this.individualDB= this.berkeleyEnv.openDatabase(null, "individuals", cfg);
			
			cfg.setAllowCreate(true);
			cfg.setTemporary(true);
			cfg.setSortedDuplicates(false);
			mkr2genotypesDB= this.berkeleyEnv.openDatabase(null, "individuals", cfg);
			
			this.errorLog= new TmpWriter();
			} 
		catch (DatabaseException e)
			{
			throw e;
			}
		catch (IOException e)
			{
			throw e;
			}
		finally
			{
			if(this.errorLog!=null) this.errorLog.delete();
			if(this.mkr2genotypesDB!=null) this.mkr2genotypesDB.close();
			if(this.individualDB!=null) this.individualDB.close();
			if(this.pos2markerDB!=null) this.pos2markerDB.close();
			if(this.markerDB!=null) this.markerDB.close();
			if(this.berkeleyEnv!=null) this.berkeleyEnv.close();
			}
		}
	
	
	public static void main(String[] args)
		{
		try
			{
			Giraf app= new Giraf();
			int optind=0;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					return;
					}
				else if(args[optind].equals("--"))
					{
					optind++;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("Unknown option "+args[optind]);
					}
				else 
					{
					break;
					}
				++optind;
				}
			} 
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}
	}
