package org.lindenb.sql.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Field
	extends AbstractIdentifier
	{
	private static class EnumItem extends AbstractIdentifier{
		@Override
		public String getJavaName() {
			return escapedJavaName().toUpperCase();
			}
	}
	private String type;
	private String Null;
	private String Key;
	private String Default;
	private String Extra;
	
	public Field()
		{
		}
 
	public Field(ResultSet row) throws SQLException
		{
		setName( row.getString("Field") );
		this.type= row.getString("Type");
		this.Null= row.getString("Null");
		this.Key= row.getString("Key");
		this.Default= row.getString("Default");
		this.Extra=row.getString("Extra");
		}
	
	


	
	
	public boolean isIndexed()
		{
		return getKey().equals("MUL") || isUnique() || isPrimary();
		}
	
	public boolean isUnique()
		{
		return getKey().equals("UNI");
		}
	
	public boolean isEnum()
		{
		return getType().startsWith("enum(") && getType().endsWith(")");
		}
	
	public boolean isSet()
		{
		return getType().startsWith("set(") && getType().endsWith(")");
		}
	
	/** returns the components of a SET or an ITEM */
	public List<AbstractIdentifier> getItems()
		{
		ArrayList<AbstractIdentifier> _items= new ArrayList<AbstractIdentifier>();
		if(isSet() || isEnum())
			{
			String s= getType();
			int i=s.indexOf('(');
			
			s=s.substring(i+1);
			s=s.substring(0,s.length()-1);
			String tokens[]= s.split("[,]");
			for(String t:tokens)
				{
				if(t.startsWith("'") && t.endsWith("'"))
					{
					t=t.substring(1,t.length()-1);
					}
				EnumItem item= new EnumItem();
				item.setName(t);
				_items.add(item);
				}
			}
		return _items;
		}
	
	
	public boolean isPrimary()
		{
		return getKey().equals("PRI");
		}
	
	public boolean isAutoIncrement()
		{
		return getExtra().equals("auto_increment");
		}
	
	public boolean isNull()
		{
		return this.Null.equals("YES");
		}

	
	public int getLength()
		{
		String t=getType();
		int i= t.indexOf('(');
		if(i==-1) return -1;
		int j= t.indexOf(',',i+1);
		if(j==-1) j= t.indexOf(')',i+1);
		if(j==-1) return -1;
		return Integer.parseInt( t.substring(i+1,j));
		}
	
	public int getPrecision()
		{
		String t=getType();
		int i= t.indexOf('(');
		if(i==-1) return -1;
		i= t.indexOf(',',i+1);
		if(i==-1) return -1;
		int j= t.indexOf(')',i+1);
		if(j==-1) return -1;
		return Integer.parseInt( t.substring(i+1,j));
		}
	
	/** returns the sql type without the length/precision */
	public String getSqlType()
		{
		String t=getType();
		int i= t.indexOf('(');
		if(i==-1) return t;
		return t.substring(0,i).trim();
		}
	
	public String getKey() {
		return Key;
		}

	public String getDefault() {
		return Default;
		}
	
	/** return type as defined in the sql engine */
	public String getType()
		{
		return this.type;
		}
	
	public String getExtra() {
		return Extra;
		}
	}
