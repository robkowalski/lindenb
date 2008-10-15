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
extends AbstractIdentifier
{

private ArrayList<Field> fields= new ArrayList<Field>();

public Table()
	{
	}


public List<Field> getFields() {
	return fields;
	}

@Override
public boolean equals(Object obj) {
	if(obj==this) return true;
	if(obj==null || getClass()!=obj.getClass()) return false;
	Table cp= Table.class.cast(obj);
	return getName().equals(cp.getName()) &&
			getFields().equals(cp.getFields())
			;
	}

public List<Field> getPrimaryKeys()
	{
	ArrayList<Field> x= new ArrayList<Field>();
	for(Field f:getFields())
		{
		if(f.isPrimary()) x.add(f);
		}
	return x;
	}

public Field getIdField()
	{
	Field id = null;
	for(Field f:getFields())
		{
		if(f.isPrimary() && f.isAutoIncrement())
			{
			if(id!=null)
				{
				System.err.println("Warning two primary keys in "+getName()+ " "+id +" and "+f);
				return null;
				}
			id=f;
			}
		}
	return id;
	}

public boolean hasIdField()
	{
	return getIdField()!=null;
	}

@Override
public String toString() {
	return getClass().getName()+"{ name:"+getName()+", fields:"+getFields()+"}";
	}
}
