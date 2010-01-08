package org.lindenb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lindenb.io.IOUtils;
import org.lindenb.lang.NotFoundException;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * XMLBeanFactory
 * lightweight implementation of a XML bean factory
 */
public class XMLBeanFactory
	{
	private static final String NS="http://www.springframework.org/schema/beans";
	private static Logger LOG= Logger.getLogger("org.lindenb");
	private Document dom;
	private HashMap<String, Object> persistence=new HashMap<String, Object>();
	
	protected XMLBeanFactory(Document dom)
		{
		this.dom=dom;
		Element root= this.dom.getDocumentElement();
		if(root==null) throw new IllegalArgumentException("no root in document");
		if(!NS.equals(root.getNamespaceURI())) 
			 throw new IllegalArgumentException("namespace is not "+NS);
		if(!root.getLocalName().equals("beans")) 
			 throw new IllegalArgumentException("root is not a <beans/>");
		
		//performs eager initialisations
		
		
			for(Element e:XMLUtilities.forEach(root,NS,"bean"))
				{
				Attr att= e.getAttributeNode("abstract");
				if(att!=null && att.getValue().equals("true")) continue;
				
				att=root.getAttributeNode("lazy-init");
				if(att!=null && att.getValue().equals("true")) continue;
				
				att= e.getAttributeNode("id");
				if(att==null) att= e.getAttributeNode("name");
				if(att==null) continue;
				String name=att.getValue();
				Object object=_bean(e);
				if(!e.getAttribute("singleton").equals("false"))
					{
					if(this.persistence.containsKey("name"))
						{
						continue;//was initialized by another bean
						}
					this.persistence.put(name, object);
					}
				}
			
		}
	
	
	
	private Element findBeanElement(String name)
		{
		if(StringUtils.isBlank(name)) throw new IllegalArgumentException("bad name ");
		
		for(Element e:XMLUtilities.forEach(this.dom.getDocumentElement(), NS,"bean"))
			{
			Attr att= e.getAttributeNode("id");
			if(att==null) att= e.getAttributeNode("name");
			if(att==null) continue;
			if(att.getValue().equals(name)) return e;
			}
		return null;
		}
	
	public <T> T getBean(String name,Class<T> clazz)
		{
		return clazz.cast(getBean(name));
		}
	
	public Object getBean(String name)
		{
		if(this.persistence.containsKey(name))//value may be null
			{
			return this.persistence.get(name);
			}
		Element e= findBeanElement(name);
		if(e==null) throw new NotFoundException("Cannot find bean named '"+name+"'");
		
		Object object=_bean(e);
		
		
		if(!e.getAttribute("singleton").equals("false"))
			{
			this.persistence.put(name, object);
			}
		return object;
		}
	
	
	
	
	private Object _constructor_arg(Element cstorArg)
		{
		Element e=XMLUtilities.one(cstorArg,NS,null);
		return _eval(e);
		}
	
	private Object _eval(Element any)
		{
		String local=any.getLocalName();
		if(local.equals("ref"))
			{
			return _ref(any);
			}
		else if(local.equals("value"))
			{
			return _value(any);
			}
		else if(local.equals("null"))
			{
			return _null(any);
			}
		else if(local.equals("props"))
			{
			return _props(any);
			}
		else if(local.equals("list"))
			{
			return _list(any);
			}
		else if(local.equals("map"))
			{
			return _map(any);
			}
		else if(local.equals("set"))
			{
			return _set(any);
			}
		else if(local.equals("key"))
			{
			return _key(any);
			}
		else if(local.equals("bean"))
			{
			return _bean(any);
			}
		else if(local.equals("idref"))
			{
			return _idref(any);
			}
		throw new IllegalArgumentException("bad node:"+any.getNodeName());
		}
	
	
	private Object _ref(Element ref)
		{
		Attr att=ref.getAttributeNode("bean");
		if(att==null) throw new IllegalArgumentException("<ref> missing @bean");
		return getBean(att.getValue());
		}
	
	private Object _castValue(Node attOwner,String content)
		{
		Attr attType=null;
		if(attOwner!=null) attType=(Attr)attOwner.getParentNode().getAttributes().getNamedItem("type");
		
		if(attType==null) return content;
		String type=attType.getValue();
		if(type.equals("int")) return new Integer(content.trim());
		if(type.equals("long")) return new Long(content.trim());
		if(type.equals("float")) return new Float(content.trim());
		if(type.equals("double")) return new Double(content.trim());
		if(type.equals("byte")) return new Byte(content.trim());
		if(type.equals("bool")) return new Boolean(content.trim());
		if(type.equals("char"))
			{
			if(content.length()!=1) throw new IllegalArgumentException("not a char");
			return new Character(content.charAt(0));
			}
		
		try
			{
			Class<?> clazz=Class.forName(type);
			Constructor<?> ctor=clazz.getConstructor(String.class);
			return ctor.newInstance(content);
			}
		catch (Exception e)
			{
			throw new IllegalArgumentException(e);
			}
		}
	
	private Object _value(Element ref)
		{
		String content=ref.getTextContent();
		return _castValue(ref.getParentNode(), content);
		
		//throw new IllegalArgumentException("bad <value>");
		}
	
	private Object _null(Element none)
		{
		return null;
		}
	
	private Properties _props(Element root)
		{
		Properties ppties=new Properties();
		for(Element prop:XMLUtilities.forEach(root,NS,"prop"))
			{
			Attr att=prop.getAttributeNode("key");
			if(att==null) throw new IllegalArgumentException("@key missing in prop");
			String key=att.getValue();
			if(ppties.containsKey(key))
				{
				throw new IllegalArgumentException("@key defined twice:"+key);
				}
			ppties.setProperty(key, prop.getTextContent());
			}
		return ppties;
		}
	
	private List<?> _list(Element root)
		{
		List<Object> L=new ArrayList<Object>();
		for(Element li:XMLUtilities.forEach(root,NS,null))
			{
			L.add(_eval(li));
			}
		return L;
		}
	
	private Object _bean(Element e)
		{
		Attr classAtt= e.getAttributeNode("class");
		
		Attr abstractAtt= e.getAttributeNode("abstract");
		//Attr autoWireAtt= e.getAttributeNode("autowire");
		
		
		Object object=null;
		if(classAtt==null)
			{
			throw new IllegalArgumentException("missing @class in bean");
			}
		if(abstractAtt!=null && abstractAtt.getValue().equals("true"))
			{
			throw new IllegalArgumentException("bean is abstract");
			}
		String className= classAtt.getValue();
		if(StringUtils.isBlank(className)) throw new IllegalArgumentException("Bad @class in bean");
		try
			{
			Class<?> clazz= Class.forName(className);
			List<Object> arguments= new ArrayList<Object>();
			
			for(Element cstor:XMLUtilities.forEach(e,NS,"constructor-arg"))
				{
				if(XMLUtilities.count(cstor)==0)
					{
					throw new NotFoundException("Empty constructor-arg");
					}
				arguments.add(_constructor_arg(cstor));
				}
			
			if(arguments.isEmpty())
				{
				object=clazz.newInstance();
				}
			else
				{
				for(Constructor<?> ctor:clazz.getConstructors())
					{
					Class<?> paramType[]=ctor.getParameterTypes();
					if(paramType.length!=arguments.size()) continue;
					int i=0;
					for(i=0;i< arguments.size();++i)
						{
						Object param=arguments.get(i);
						if(param==null && paramType[i].isPrimitive()) break;
						if(param!=null && !paramType[i].isInstance(param)) break;
						}
					if(i!=arguments.size()) continue;
					object=ctor.newInstance(arguments.toArray());
					break;
					}
				if(object==null) throw new RuntimeException("Cannot initialize object ctor");
				}
			}
		catch (Exception e2)
			{
			throw new RuntimeException(e2);
			}
		
		_abstractBean(e,object);
		
		return object;
		}
	
	private void _abstractBean(Element root,Object object)
		{
		Attr parentAtt= root.getAttributeNode("parent");
		if(parentAtt!=null)
			{
			for(String parentName:parentAtt.getValue().split("[, ]+"))
				{
				Element p= findBeanElement(parentName);
				if(p==null) throw new NotFoundException("cannot find parent bean "+parentName);
				_abstractBean(p,object);
				}
			}
		for(Element prop:XMLUtilities.forEach(root,NS,"property"))
			{
			applyProperty(prop,object);
			}
		}
	
	private void applyProperty(Element root,Object bean)
		{
		Attr nameAtt=root.getAttributeNode("name");
		if(nameAtt==null) throw new RuntimeException("@name misssing in property");
		String name=nameAtt.getValue();
		if(!name.matches("[a-z_][a-zA-Z0-9_]*")) throw new RuntimeException("bad name :"+name);
		Object value=null;
		if(!root.hasChildNodes())
			{
			Attr valueAttr=root.getAttributeNode("value");
			if(valueAttr==null) throw new RuntimeException("@value misssing in property");
			//TODO
			}
		else
			{
			value=_eval(XMLUtilities.one(root));
			}
		
		String setter="set"+(name.length()==1?
				name.toUpperCase():
				name.substring(0,1).toUpperCase()+name.substring(1)
				);
		try
			{
			Method method=null;
			for(Method m: bean.getClass().getMethods())
				{
				if(!m.getName().equals(setter)) continue;
				int modifiers=m.getModifiers();
				if(Modifier.isStatic(modifiers)) continue;
				if(!Modifier.isPublic(modifiers)) continue;
				Class<?> types[]=m.getParameterTypes();
				if(types.length!=1) continue;
				Class<?> type=types[0];
				if(value==null && type.isPrimitive()) continue;
				if(value!=null && !type.isInstance(value)) continue;
				if(method!=null) throw new RuntimeException("ambigous method "+setter+" to "+bean.getClass());
				method=m;
				}
			if(method==null) throw new RuntimeException("Cannot find method "+setter+" to "+bean.getClass());
			method.invoke(bean, value);
			}
		catch (Exception e)
			{
			throw new RuntimeException(e);
			}
		}
	
	
	private Map<?, ?> _map(Element root)
		{
		Map<Object,Object> M=new HashMap<Object,Object>();
		for(Element entry:XMLUtilities.forEach(root,NS,"entry"))
			{
			Pair<Object,Object> p=_mapEntry(entry);
			M.put(p.first(), p.second());
			}
		return M;
		}
	
	private Set<?> _set(Element root)
		{
		Set<Object> S=new HashSet<Object>();
		for(Element li:XMLUtilities.forEach(root,NS,null))
			{
			S.add(_eval(li));
			}
		return S;
		}
	
	private Object _key(Element root)
		{
		Element value=XMLUtilities.one(root,NS,null);
		return _eval(value);
		}
	
	private String _idref(Element root)
		{
		Attr bean=root.getAttributeNode("bean");
		if(bean==null) bean=root.getAttributeNode("local");
		if(bean==null) throw new IllegalArgumentException("@bean missing under idref");
		String name=bean.getName();
		//only check it exists 
		getBean(name);
		//but return the name
		return name;
		}
	
	private Pair<Object,Object> _mapEntry(Element root)
		{
		if(!root.hasChildNodes() &&
			root.hasAttribute("key") &&
			root.hasAttribute("value"))
			{
			return new Pair<Object,Object>(
					root.getAttribute("key"),
					root.getAttribute("value")
					);
			}
		Element eK=null;
		Element eV=null;
		for(Element e:XMLUtilities.forEach(root,NS,null))
			{
			if(e.getNodeName().equals("key"))
				{
				if(eK!=null) throw new IllegalArgumentException("key defined twice in entry");
				eK=e;
				continue;
				}
			if(eV!=null) throw new IllegalArgumentException("value defined twice in entry");
			eV=e;
			}
		if(eK==null)
			{
			throw new IllegalArgumentException("key missing in entry");
			}
		if(eV==null) throw new IllegalArgumentException("value missing in entry");
		
		Object key= _key(eK);
		Object value= _eval(eV);
		return new Pair<Object,Object>(key,value);
		}
	
	/*private Object _invoke(Element root)
		{
		Attr beanAtt=root.getAttributeNode("bean");
		if(beanAtt==null)  throw new IllegalArgumentException("@bean missing under "+root.getNodeName());
		Attr nameAtt=root.getAttributeNode("name");
		if(nameAtt==null)  throw new IllegalArgumentException("@name missing under "+root.getNodeName());
		Object instance=getBean(beanAtt.getValue());
		
		}*/
	
	public static XMLBeanFactory loadInstance(File file)
		throws IOException
		{
		LOG.info("reading:"+file);
		FileInputStream in= new FileInputStream(file);
		XMLBeanFactory f= loadInstance(in);
		in.close();
		return f;
		}
	
	public static XMLBeanFactory loadInstance(String uri)
	throws IOException
		{
		InputStream in= IOUtils.openInputStream(uri);
		XMLBeanFactory f= loadInstance(in);
		in.close();
		return f;
		}
	
	public static XMLBeanFactory loadInstance(InputStream in)
		throws IOException
		{
		try {
				DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
				f.setCoalescing(true);
				f.setNamespaceAware(true);
				f.setValidating(false);
				f.setExpandEntityReferences(true);
				f.setIgnoringComments(false);
				f.setIgnoringElementContentWhitespace(true);
				Document dom= f.newDocumentBuilder().parse(in);
				
				XMLBeanFactory xbf=new XMLBeanFactory(dom);
				return xbf;
			} catch (ParserConfigurationException e)
				{
				throw new IOException(e);
				}
			catch (SAXException e) {
			throw new IOException(e);
				}
		}
	}
