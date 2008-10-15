package org.lindenb.sql.schema;

import org.lindenb.util.Cast;

abstract public class AbstractIdentifier {
private String name=null;
private String description=null;

protected AbstractIdentifier()
	{
	
	}

public void setName(String name) {
	this.name = name;
	}

public String getName() {
	return name;
	}

public String getDescription() {
	return description==null?getName():description;
	}

public void setDescription(String description) {
	this.description = description;
	}

protected String escapedJavaName()
	{
	String s=getName();
	if(s.equals("+")) s="PLUS";
	else if(s.equals("-")) s="MINUS";
	else if(s.equals("?")) s="UNKNOWN";
	else if(s.equals("class")) s="clazz";
	
	s=s.replace("%", "percent_");
	s=s.replace("*", "any_");
	s=s.replace("/", "_per_");
	s=s.replace("µ", "_micro_");
	
	s=s.replaceAll("[^0-9a-zA-Z]", "_");
	
	if(Cast.Integer.isA(s))
		{
		s="_"+s;
		}
	return s;
	}

public String getJavaName()
	{
	if(getName()==null) return null;
	boolean isUpper=true;
	String s= escapedJavaName();
	s=s.replace('-', '_');
	for(int i=0;i< s.length();++i)
		{
		if(Character.isDigit(s.charAt(i)) ||
			s.charAt(i)=='_' ||
			Character.isUpperCase(s.charAt(i))) continue;
		isUpper=false;
		break;
		}

	if(isUpper)
		{
		s= s.toLowerCase();
		s= s.substring(0,1).toUpperCase()+(s.length()>1 ? s.substring(1).toLowerCase():"");
		}
	else
		{
		s= s.substring(0,1).toUpperCase()+(s.length()>1 ? s.substring(1):"");
		}

	for(char c='A';c<='Z';++c)
		{
		s= s.replaceAll("_"+c, String.valueOf(c)).
		     replaceAll("_"+Character.toLowerCase(c), String.valueOf(c));
		}
	return s;
	}

@Override
public int hashCode() {
	return getName()==null?-1:getName().hashCode();
	}

@Override
public String toString() {
	return getClass().getName()+":"+getName();
	}
}
