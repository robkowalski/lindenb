package org.lindenb.sql.schema;

import java.util.ArrayList;
import java.util.List;

/** 
 * describe the schema of a database
 * useful for code generation
 * @author lindenb
 *
 */
public class Table
{
private String name=null;
private ArrayList<Field> fields= new ArrayList<Field>();

public Table()
	{
	}

public String getName() {
	return name;
	}

public void setName(String name) {
	this.name = name;
	}

public List<Field> getFields() {
	return fields;
	}

public String getDescription()
	{
	return getName();
	}

public String getJavaName()
	{
	if(getName()==null) return null;
	String s= getName().substring(0,1).toUpperCase()+getName().substring(1);
	return s.replace('-', '_');
	}
}
