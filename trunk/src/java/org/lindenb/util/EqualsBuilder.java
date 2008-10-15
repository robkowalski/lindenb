package org.lindenb.util;

/**
 * EqualsBuilder inspired from the apache.common class
 * @author lindenb
 *
 */
public class EqualsBuilder
	{
	private boolean isEquals=true;

	public EqualsBuilder()
		{
		
		}
	
	public EqualsBuilder and(Object a,Object b)
		{
		if(!isEquals) return this;
		if(a==b) return this;
		if(a==null || b==null) { isEquals=false; return this;}
		isEquals= a.equals(b);
		return this;
		}
	
	
	public EqualsBuilder and(double a,double b)
		{
		if(isEquals) { isEquals= (Double.compare(a,b)==0);}
		return this;
		}
	
	public EqualsBuilder and(float a,float b)
		{
		if(isEquals) { isEquals= (Float.compare(a,b)==0);}
		return this;
		}	
	
	public EqualsBuilder and(long a,long b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}	
	public EqualsBuilder and(int a,int b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}
	public EqualsBuilder and(short a,short b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}
	public EqualsBuilder and(char a,char b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}
	
	public EqualsBuilder and(boolean a,boolean b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}
	
	public EqualsBuilder and(byte a,byte b)
		{
		if(isEquals) { isEquals= (a==b);}
		return this;
		}
	
	
	public EqualsBuilder and(double a[],double b[])
		{
		if(!isEquals) return this;
			
	    if (a == b)  return this;
	    if (a == null || b == null) { isEquals = false; return this;}
	        if (a.length != b.length) {
	            isEquals = false;
	            return this;
	        }
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	            and(a[i], b[i]);
	        }
		   
		return this;
		}
	
	public EqualsBuilder and(float a[],float b[])
		{
		if(!isEquals) return this;
			
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}
	
	public EqualsBuilder and(long a[],long b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}	
	
	public EqualsBuilder and(int a[],int b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}
	
	public EqualsBuilder and(short a[],short b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}
	
	public EqualsBuilder and(char a[],char b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}
	public EqualsBuilder and(boolean a[],boolean b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		return this;
		}
	
	public EqualsBuilder and(byte a[],byte b[])
		{
		if(!isEquals) return this;
		
	    if (a == b)  return this;
	    if (a == null || b == null || a.length != b.length)
	    		{ isEquals = false; return this;}
	     for (int i = 0; i < a.length && isEquals; ++i)
	     	{
	    	and(a[i], b[i]);
	        }
		
		return this;
		}
	
	
	public boolean isEquals()
		{
		return this.isEquals;
		}
	
	}
