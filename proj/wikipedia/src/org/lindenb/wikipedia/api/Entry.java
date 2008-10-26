package org.lindenb.wikipedia.api;

import java.io.IOException;
import java.net.URLEncoder;

public abstract class Entry
implements Comparable<Entry>
{
private String localName;
protected Entry( String name)
	{
	if(name==null) throw new NullPointerException("name is null");
	int i=name.indexOf(':');
	if(i!=-1 && name.substring(0,i).equals(getNamespace().name()))
		{
		name=name.substring(i+1);
		}
	
	if(name.length()==0) throw new IllegalArgumentException("empty name");
	
	this.localName=name.substring(0,1).toUpperCase();//1st letter is not case sensitive in MW
	if(name.length()>1) this.localName+= name.substring(1);
	}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + getLocalName().hashCode();
	result = prime * result + getNamespace().hashCode();
	return result;
	}

public String getQName()
	{
	return getNamespace().name()+":"+getLocalName();
	}

public String getLocalName()
	{
	return this.localName;
	}

@Override
public int compareTo(Entry o)
	{
	if(o==this) return 0;
	if(o==null) return -1;	
	int i= getNamespace().compareTo(o.getNamespace());
	if(i!=0) return i;
	return getLocalName().compareTo(o.getLocalName());
	}

@Override
public boolean equals(Object obj) {
	if(obj==this) return true;
	if(obj==null || obj.getClass()!=getClass()) return false;
	Entry e= Entry.class.cast(obj);
	return e.getNamespace().equals(this.getNamespace())
			&& e.getLocalName().equals(this.getLocalName())
			;
	}

@Override
public String toString() {
	return getQName();
	}

public abstract MWNamespace getNamespace();

public String getQNameEncoded()
	{
	try
		{
		return URLEncoder.encode(getQName().replace(' ', '_'),"UTF-8");
		}
	catch(IOException e)
		{
		throw new RuntimeException(e);
		}
	}

public static Entry create(MWNamespace ns,String localName)
	{
	switch(ns)
		{
		case Main: return new Page(localName); 
		case Category: return new Category(localName);
		case User: return new User(localName); 
		case Template: return new Template(localName);
		default: throw new IllegalArgumentException("not implemented "+ns);
		}
	}

public static Entry create(String qName)
	{
	int i=qName.indexOf(':');
	if(i==-1) return create(MWNamespace.Main,qName);
	return create(MWNamespace.valueOf(qName.substring(0,i)),qName.substring(i+1));
	}

}
