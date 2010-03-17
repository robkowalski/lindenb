package org.lindenb.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

public class BeanUtils
	{
	public static Method getGetter(Class<?> clazz,String name)
		{
		for(Method m:clazz.getMethods())
			{
			if(!isGetter(m))
				{
				continue;
				}
			if( m.getName().startsWith("is") &&
				m.getName().substring(2).equalsIgnoreCase(name)) return m;
			if( m.getName().startsWith("get") &&
					m.getName().substring(3).equalsIgnoreCase(name)) return m;
			}
		return null;
		}
	
	public static Method getSetter(Class<?> clazz,String name)
		{
		for(Method m:clazz.getMethods())
			{
			if(!isGetter(m)) continue;
			if(m.getName().substring(3).equalsIgnoreCase(name)) return m;
			}
		
		return null;
		}
	
	public static boolean hasSetterAndGetter(Class<?> clazz,String name)
		{
		return getSetter(clazz, name)!=null &&
			   getGetter(clazz, name)!=null
			   ;
		}
	
	
	public static Set<String> getSetterAndGetters(Class<?> clazz)
		{
		Set<String> set=new TreeSet<String>();
		for(Method m:getSetters(clazz))
			{
			String name= m.getName().substring(3);
			if(name.length()>1)
				{
				name=name.substring(0,1).toLowerCase()+name.substring(1);
				}
			if(getGetter(clazz, name)!=null) set.add(name);
			}
		return set;
		}
	
	public static Method[] getGetters(Class<?> clazz)
		{
		List<Method> list=new ArrayList<Method>();
		for(Method m:clazz.getMethods())
			{
			if(isGetter(m)) list.add(m);
			}
		return list.toArray(new Method[list.size()]);
		}
	
	public static Method[] getSetters(Class<?> clazz)
		{
		List<Method> list=new ArrayList<Method>();
		for(Method m:clazz.getMethods())
			{
			if(isSetter(m)) list.add(m);
			}
		return list.toArray(new Method[list.size()]);
		}
	
	
	
	
	public static boolean isGetter(Method m)
		{
		try
			{
			
			if(m.getName().startsWith("is") )
				{
				if(m.getName().length()==2) return false;
				if(!(m.getReturnType().equals(Boolean.TYPE) || m.getReturnType().equals(Boolean.class)) )  return false;
				}
			else if(m.getName().startsWith("get"))
				{
				if(m.getName().length()==3) return false;
				if(m.getReturnType().equals(Void.TYPE))  return false;
				}
			else
				{
				return false;
				}
			//non pas pour private scope
			if(Modifier.isStatic(m.getModifiers()))  return false;
			if(m.getParameterTypes().length!=0) return false;
			}
		catch(Exception err)
			{
			return false;
			}
		return true;
		}
	public static boolean isSetter(Method m)
		{
		try
			{
			if(!m.getName().startsWith("set") ) return false;
			if(m.getName().length()==3) return false;
			if(!m.getReturnType().equals(Void.TYPE))  return false;
			if(Modifier.isStatic(m.getModifiers()))  return false;
			if(m.getParameterTypes().length!=1) return false;
			}
		catch(Exception err)
			{
			return false;
			}
		return true;
		}
	public static void main(String[] args)
		{
		try
			{
			
			
			for(String m: getSetterAndGetters(JPanel.class))
				{
				System.err.println(m);
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	}
