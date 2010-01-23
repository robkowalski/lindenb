package org.lindenb.tinytools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lindenb.lang.InvalidXMLException;
import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.sw.vocabulary.RDFS;
import org.lindenb.sw.vocabulary.XSD;
import org.lindenb.swing.DocumentAdapter;
import org.lindenb.swing.SwingUtils;
import org.lindenb.swing.layout.VerticalLayout;
import org.lindenb.swing.table.GenericTableModel;
import org.lindenb.util.StringUtils;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;




public class ModelingTool
{
/**
 * RDFException
 *
 */
private static class RDFException
	extends Exception
	{
	private static final long serialVersionUID = 1L;
	public RDFException()
		{
		this("RDFException");
		}
	public RDFException(String s)
		{
		super(s);
		}
	
	public RDFException(String s,Throwable cause)
		{
		super(s,cause);
		}
	public RDFException(Throwable cause)
		{
		this(cause.getMessage(),cause);
		}
	}

	/**
	 * 
	 * OntNode
	 *
	 */
	public abstract static class OntNode
		implements Comparable<OntNode>
		{
		private String uri;
		private String label;
		private String comment;
		
		void setURI(String uri)
			{
			this.uri=uri;
			}
			
		void setLabel(String label)
			{
			this.label=label;
			}
		public String getLabel()
			{
			return this.label==null?getShortName():this.label;
			}
		
		void setComment(String comment)
			{
			this.comment=comment;
			}
			
		public String getComment()
			{
			return this.comment==null?getLabel():this.comment;
			}
		
		public String getURI()
			{
			return this.uri;
			}
		
		public String getLocalName()
			{
			int i=getURI().lastIndexOf('#');
			if(i==-1) i=getURI().lastIndexOf('/');
			return getURI().substring(i+1);
			}
		
		public String getPrefix()
			{
			for(String pfx: getSchema().prefixMapping.getPrefixes())
				{
				if(getURI().startsWith( getSchema().prefixMapping.getNsPrefixURI(pfx)))
					{
					return pfx;
					}
				}
			return null;
			}
		
		public String getShortName()
			{
			String s=getSchema().prefixMapping.qnameFor(getURI());
			if(s!=null) return s;
			return getLocalName();
			}
			
		@Override
		public int compareTo(OntNode c)
			{
			return getShortName().compareToIgnoreCase(c.getShortName());
			}
			
		@Override
		public boolean equals(Object o)
			{
			if(o==this) return true;
			if(o==null || o.getClass()!=this.getClass()) return false;
			return getURI().equals(OntNode.class.cast(o).getURI());
			}
		
		@Override
		public int hashCode()
			{
			return getURI().hashCode();
			}
		
		@Override
		public String toString()
			{
			return getShortName();
			}
		public abstract Schema getSchema();
		}
	
	/**
	 *
	 *  OntClass
	 * 
	 */
	public abstract static class OntClass
		extends OntNode
		{
		private Set<String> parentClasses=new HashSet<String>();
		private Set<OntProperty> declaredProperties= new HashSet<OntProperty>();
		public Set<OntProperty> getDeclaredProperties()
			{
			return this.declaredProperties;
			}
		
		
		public Set<OntClass> getDirectChildClasses()
			{
			Set<OntClass> set=new HashSet<OntClass>();
			for(String s:getSchema().uri2class.keySet())
				{
				if(s.equals(getURI())) continue;
				OntClass c= getSchema().uri2class.get(s);
				if(c.parentClasses.contains(this.getURI()))
					{
					set.add(c);
					}
				}
			return set;
			}
		
		public Set<OntClass> getAllDescendants()
			{
			Set<OntClass> set= getDirectChildClasses();
			Set<OntClass> rez=new HashSet<OntClass>(set);
			for(OntClass child: set)
				{
				rez.addAll(child.getAllDescendants());
				}
			return rez;
			}
		
		public Set<OntClass> getParentClasses()
			{
			Set<OntClass> set=new HashSet<OntClass>();
			return _parent(set);
			}
			
		private Set<OntClass> _parent(Set<OntClass> set)
			{
			if(set.contains(this)) return set;
			for(String s:parentClasses)
				{
				OntClass p=getSchema().uri2class.get(s);
				if(p==null) throw new RuntimeException();
				set.add(p);
				p._parent(set);
				}
			return set;
			}
		
		public Set<OntProperty> getParentProperties()
			{
			Set<OntProperty> set= new HashSet<OntProperty>();
			for(OntClass p:getParentClasses())
				{
				set.addAll(p.getDeclaredProperties());
				}
			return set;
			}
		
		public Set<OntProperty> getAllProperties()
			{
			Set<OntProperty> set= new HashSet<OntProperty>(getDeclaredProperties());
			set.addAll(getParentProperties());
			return set;
			}
			
		public Set<OntProperty> getSpecificProperties()
			{
			Set<OntProperty> set= new HashSet<OntProperty>(getDeclaredProperties());
			set.removeAll(getParentProperties());
			return set;
			}
		}
	
	/**
	 * OntProperty
	 *
	 */
	public abstract static class OntProperty
		extends OntNode
		{
		private int minCardinality=0;
		private Integer maxCardinality=null;
		/**
		 * An abstract implementation of a PropertyInput
		 *
		 */
		protected abstract class AbstractPropertyInput
			extends PropertyInput
			{
			private static final long serialVersionUID = 1L;

			@Override
			public OntProperty getOntProperty()
				{
				return OntProperty.this;
				}
			
			List<Action> extraActions()
				{
				AbstractAction action=new AbstractAction("?")
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0)
						{
						setValue("?");
						}
					};
				List<Action> array=new ArrayList<Action>();
				while(array.size()<4) array.add(action);
				return array;
				}
			
			void buttons()
				{
				List<Action> array=extraActions();
				if(array.isEmpty()) return;
				JPanel right=new JPanel(new FlowLayout(FlowLayout.TRAILING));
				for(Action a:array)
					{
					right.add(makeSqButton(a));
					}
				this.add(right,BorderLayout.EAST);
				}
			
			JButton makeSqButton(Action action)
				{
				JButton but= new JButton(action);
				but.setPreferredSize(new Dimension(32,32));
				return but;
				}
			}
		
		/**
		 * An abstract implementation of a PropertyInput
		 * having a text component as input
		 *
		 */
		protected abstract class AbstractTextPropertyInput
		extends AbstractPropertyInput
			{
			private static final long serialVersionUID = 1L;
			protected JTextComponent textComponent;
			
			/** adds a listener that will hilight the textComponent if
			 * if it is not valid
			 */
			protected void addValidationListener()
				{
				this.textComponent.getDocument().addDocumentListener(
					new DocumentAdapter()
						{
						@Override
						public void documentChanged(DocumentEvent e)
							{
							validateText();
							}
						}
					);
				}
			
			/** do highlight the text if it is not valid */ 
			protected void validateText()
				{
				this.textComponent.setForeground(
					this.hasValidInput()?
					Color.BLACK:Color.RED		
					);
				}
			
			@Override
			public String getValue()
				{
				return this.textComponent.getText();
				}
			@Override
			public void setValue(String v)
				{
				this.textComponent.setText(v);
				this.textComponent.setCaretPosition(0);
				}
			@Override
			public JComponent getMainComponent()
				{
				return this.textComponent;
				}
			
			/** creates the gui */
			PropertyInput make()
				{
				this.textComponent=createJTextComponent();
				addValidationListener();
				if(this.textComponent instanceof JTextArea )
					{
					JScrollPane scroll=new JScrollPane(this.textComponent);
					this.add(scroll,BorderLayout.CENTER);
					}
				else
					{
					this.add(this.textComponent,BorderLayout.CENTER);
					}
				
				buttons();
				return this;
				}
			
			/** creates the text Component */
			JTextComponent createJTextComponent()
				{
				return new JTextField(20);
				}
			}
		
		public abstract PropertyInput createPropertyInput();
		
		public int getMinCardinality()
			{
			return minCardinality;
			}
		
		/** returns max cardinality (inclusive) */
		public Integer getMaxCardinality()
			{
			return maxCardinality;
			}
		
		/** returns null of the argument 'count' is compatible with
		 *  the min and max cardinality else it returns an error message */
		public String getCardinalityMessage(int count)
			{
			if(count<getMinCardinality())
				{
				return "Expect at least "+getMinCardinality()+" instance(s) of '"+
					getShortName()+
					"' but found "+count;
				}
			if(this.getMaxCardinality()!=null && count>getMaxCardinality())
				{
				return "Expect at most "+getMaxCardinality()+" instance(s) of '"+
					getShortName()+
					"' but found "+count;
				}
			return null;
			}
		}
	
	/**
	 * DataProperty
	 * A property holding a literal type (String, integer...)
	 */
	public abstract static class DataProperty<T>
		extends OntProperty
		{
		/**
		 * 
		 * DataPropertyInput
		 *
		 */
		protected class DataPropertyInput
			extends AbstractTextPropertyInput
			{
			private static final long serialVersionUID = 1L;
			
		
			
			@Override
			public String getErrorMessage()
				{
				return getValidationMessage(this.getValue());
				}
			}
		
		public abstract Class<?>  getJavaClass();
		
		@Override
		public PropertyInput createPropertyInput()
			{
			return new DataPropertyInput().make();
			}
		@SuppressWarnings("unchecked")
		public T cast(String input)
			{
			Class <?> c= getJavaClass();
			try
				{
				return (T) c.getConstructor(String.class).newInstance(input);
				}
			catch (Exception e)
				{
				return null;
				}
			}
		
		public String getValidationMessage(String input)
			{
			Class <?> c= getJavaClass();
			try
				{
				c.getConstructor(String.class).newInstance(input);
				return null;
				}
			catch (Exception e)
				{
				return "Cannot cast \""+input +"\" to "+getShortName()+":"+
						String.valueOf(e.getMessage());
				}
			}
		}
		
	public abstract static class StringProperty
		extends DataProperty<String>
		{
		Integer min_length=null;
		Integer max_length=null;
		Pattern regex=Pattern.compile("[a-z]*");//TODO
		public Class<?> getJavaClass() { return String.class;}
		@Override
		public String getValidationMessage(String input)
			{
			if(min_length!=null && input.length()< min_length)
				{
				return "Expected size-of("+input+") >="+min_length;
				}
			if(max_length!=null && input.length()> max_length)
				{
				return "Expected size-of("+input+") <="+max_length;
				}
			if(regex!=null && !regex.matcher(input).matches())
				{
				return "Expected ("+input+") matches "+regex.pattern();
				}
			return null;
			}
		}
		
	public abstract static class BooleanProperty
		extends DataProperty<Boolean>
		{
		public Class<?> getJavaClass() { return Boolean.class;}
		}
	
	public abstract static class CharacterProperty
		extends DataProperty<Character>
		{
		public Class<?> getJavaClass() { return Character.class;}
		}
	
	public abstract static class DataTimeProperty
		extends DataProperty<Object>
		{
		public Class<?> getJavaClass() { return Object.class;}
		}
	
	public abstract static class NumericProperty<T extends Number & Comparable<T> >
		extends DataProperty<T>
		{
		T minValue=null;
		T maxValue=null;
		@Override
		public String getValidationMessage(String input)
			{
			String s= super.getValidationMessage(input);
			if(s!=null) return s;
			if(minValue==null && maxValue==null) return null;
			try
				{
				T value=cast(input);
				if(minValue!=null && value.compareTo(minValue)<0)
					{
					return getShortName()+": expected value:"+input+" >= "+minValue;
					}
				if(maxValue!=null && maxValue.compareTo(value)<0)
					{
					return getShortName()+": expected value:"+input+" <= "+maxValue;
					}
				return null;
				}
			catch (Exception e)
				{
				return "Errror:"+e.getMessage();
				}
			}
		}
	
	public abstract static class ByteProperty
		extends NumericProperty<Byte>
		{
		public Class<?> getJavaClass() { return Byte.class;}
		}
	public abstract static class ShortProperty
		extends NumericProperty<Short>
		{
		public Class<?> getJavaClass() { return Short.class;}
		}
	public abstract static class IntegerProperty
		extends NumericProperty<Integer>
		{
		public Class<?> getJavaClass() { return Integer.class;}
		}
	
	public abstract static class LongProperty
		extends NumericProperty<Long>
		{
		public Class<?> getJavaClass() { return Long.class;}
		}
	
	public abstract static class FloatProperty
		extends NumericProperty<Float>
		{
		public Class<?> getJavaClass() { return Float.class;}
		}
		
	public abstract static class DoubleProperty
		extends NumericProperty<Double>
		{
		public Class<?> getJavaClass() { return Double.class;}
		}
	
	public abstract static class ObjectProperty
		extends OntProperty
		{
		protected class ObjectPropertyInput
			extends AbstractTextPropertyInput
			{
			private static final long serialVersionUID = 1L;

			@Override
			public String getErrorMessage()
				{
				return null;
				}

			
			}
		private OntClass range;
		public void setRange(OntClass range)
			{
			this.range=range;
			}
		public OntClass getRange()
			{
			return this.range;
			}
		
		@Override
		public PropertyInput createPropertyInput()
			{
			return new ObjectPropertyInput().make();
			}
		}
	
	
	
	/**
	 *
	 * Individual
	 *
	 */
	public static class Individual
		{
		private String uri;
		private String name=null;
		
		private Map<OntClass,Map<OntProperty,Set<String> > > instances=new HashMap<OntClass,Map<OntProperty,Set<String>>>();
		public Individual(String uri)
			{
			this.uri=uri;
			}
		
		public Set<OntClass> getOntClasses()
			{
			return new HashSet<OntClass>(instances.keySet());
			}
		
		public boolean put(OntClass ontClass,OntProperty property,String value)
			{
			if(ontClass.getSpecificProperties().contains(property))
				{
				Map<OntProperty,Set<String> > props= this.instances.get(ontClass);
				if(props==null)
					{
					props=new HashMap<OntProperty, Set<String> >();
					this.instances.put(ontClass,props);
					}
				Set<String> values= props.get(property);
				if(values==null)
					{
					values=new HashSet<String>();
					props.put(property, values);
					}
				values.add(value);
				return true;
				}
			for(OntClass parent: ontClass.getParentClasses())
				{
				if(put(parent,property,value)==true) return true;
				}
			return false;
			}
		
		public Set<String> get(OntClass ontClass,OntProperty property)
			{
			Set<String> set=new HashSet<String>();
			
			Map<OntProperty,Set<String> > props= this.instances.get(ontClass);
			if(props==null)
				{
				return set;
				}
			if(props.containsKey(property))
				{
				set.addAll(props.get(property));
				}
			return set;
			}
		
		public Set<OntProperty> getOntPropertiesForOntClass(OntClass ontClass)
			{
			Set<OntProperty> set=new HashSet<OntProperty>();
			Map<OntProperty,Set<String> > props= this.instances.get(ontClass);
			if(props!=null)
				{
				set.addAll(props.keySet());
				}
			return set;
			}
		
		public boolean contains(OntClass c)
			{
			return this.instances.containsKey(c);
			}
		
		public String getURI()
			{
			return this.uri;
			}

		
		public String getTitle()
			{
			return name==null?getURI():name;
			}
		
		public int hashCode()
			{
			return getURI().hashCode();
			}
		
		public boolean equals(Object o)
			{
			if(o==this) return true;
			if(o==null || o.getClass()!=this.getClass()) return false;
			return getURI().equals(Individual.class.cast(o).getURI());
			}
		
		@Override
		public String toString()
			{
			return getTitle();
			}
		
		public boolean matches(Pattern regex)
			{
			return true;
			}
		}
	
	
		
	public static class Schema
		{
		private PrefixMapping prefixMapping=new PrefixMapping(true);
		private String base="";
		private Map<String,OntClass> uri2class=new HashMap<String,OntClass>();
		public Schema()
			{
			}
		
		
		public void parse(File file) throws InvalidXMLException,IOException
			{
			try {
				DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
				f.setCoalescing(true);
				f.setNamespaceAware(true);
				f.setValidating(false);
				f.setExpandEntityReferences(true);
				f.setIgnoringComments(true);
				f.setIgnoringElementContentWhitespace(true);
				this.base=file.toURI().toURL().toString();
				parse(f.newDocumentBuilder().parse(file));
				}
			catch (IOException e)
				{
				throw e;
				}
			catch ( InvalidXMLException e)
				{
				throw e;
				}
			catch (Exception e)
				{
				throw new IOException(e);
				}

			}
		
		public void parse(Document dom) throws InvalidXMLException
			{
			Element root=dom.getDocumentElement();
			if(root==null ||
			   !RDF.NS.equals(root.getNamespaceURI()) ||
			   !root.getLocalName().equals("RDF")
			   )
				{
				throw new InvalidXMLException("not a rdf:RDF root");
				}
			this.prefixMapping=new PrefixMapping(dom);
			Attr att= root.getAttributeNode("xml:base");
			if(att!=null) this.base=att.getValue();
			
			for(Element e: XMLUtilities.forEach(root,RDFS.NS,"Class"))
				{
				OntClass c=_class(e);
				if(this.uri2class.containsKey(c.getURI()))
					{
					throw new InvalidXMLException("uri defined twice:"+c.getURI());
					}
				this.uri2class.put(c.getURI(),c);
				}
			for(Element e: XMLUtilities.forEach(root,RDF.NS,"Property"))
				{
				_prop(e);
				}
			}
		private OntClass _class(Element root)
			throws InvalidXMLException
			{
			OntClass c=new OntClass()
				{
				@Override
				public Schema getSchema()
					{
					return Schema.this;
					}
				};
			_comment(c,root);
			_label(c,root);
			
			c.setURI(_uri(root));
			for(Element s: XMLUtilities.forEach(root, RDFS.NS,"subClassOf"))
				{
				c.parentClasses.add(_uri(s));
				}
			return c;
			}
		
		private void _comment(OntNode node,Element root)
			{
			Element c= XMLUtilities.firstChild(root, RDFS.NS, "comment");
			if(c!=null) node.setComment(c.getTextContent());
			}
		private void _label(OntNode node,Element root)
			{
			Element c= XMLUtilities.firstChild(root, RDFS.NS, "label");
			if(c!=null) node.setLabel(c.getTextContent());
			}
		private String _uri(Element root)
			throws InvalidXMLException
			{
			Attr att= root.getAttributeNodeNS(RDF.NS, "ID");
			if(att==null) att= root.getAttributeNodeNS(RDF.NS, "about");
			if(att==null) att= root.getAttributeNodeNS(RDF.NS, "resource");
			if(att==null) throw new InvalidXMLException(root,"No rdf:ID or rdf:about or rdf:resource");
			String uri= att.getValue();
			if(att.getLocalName().equals("ID") && !uri.startsWith("#"))
				{
				uri=base+"#"+uri;
				}
			else if(uri.startsWith("#"))
				{
				uri=base+uri;
				}
		
			return uri;
			}
		private OntProperty _prop(Element root)
			throws InvalidXMLException
			{
			OntProperty prop=null;
			Element rangeNode= XMLUtilities.one(root, RDFS.NS, "range");
			if(rangeNode.hasChildNodes())
				{
				//TODO
				return null;
				}
			String range= _uri(rangeNode);
			if(range.equals(XSD.NS+"#string"))
				{
				prop= new StringProperty()
					{
					public Schema getSchema() { return Schema.this;};
					};
				}
			else if(range.equals(XSD.NS+"#float"))
				{
				prop= new FloatProperty()
					{
					public Schema getSchema() { return Schema.this;};
					};
				}
			else if(range.equals(XSD.NS+"#integer"))
				{
				prop= new IntegerProperty()
					{
					public Schema getSchema() { return Schema.this;};
					};
				}
			else if(range.equals(XSD.NS+"#dateTime"))
				{
				prop= new DataTimeProperty()
					{
					public Schema getSchema() { return Schema.this;};
					};
				}
			else if(range.startsWith(XSD.NS))
				{
				throw new InvalidXMLException(rangeNode,"xsd:type not handled:\""+range+"\".");
				}
			else
				{
				 OntClass c=this.uri2class.get(range);
				 prop= new ObjectProperty()
						{
						public Schema getSchema() { return Schema.this;};
						};
				 ((ObjectProperty)prop).setRange(c);
				}
			
			prop.setURI(_uri(root));
			_comment(prop,root);
			_label(prop,root);
		
			for(Element d: XMLUtilities.forEach(root,RDFS.NS,"domain"))
				{
				OntClass domain=this.uri2class.get(_uri(d));
				if(domain==null) throw new IllegalArgumentException(
						"Cannot find OntClass="+_uri(d));
				domain.getDeclaredProperties().add(prop);
				}
			return prop;
			}
		}
	
	
	
	
	public ModelingTool()
		{
		}
	
	

	
	private static abstract  class PropertyInput
		extends JPanel
		{
		private static final long serialVersionUID = 1L;
		public PropertyInput()
			{
			super(new BorderLayout(2,2));
			}
		
		public boolean hasValidInput()
			{
			return getErrorMessage()==null;
			}
		public abstract String getErrorMessage();		
		public abstract OntProperty getOntProperty();
		public abstract String getValue();
		public abstract void setValue(String v);
		public abstract JComponent getMainComponent();
		}
	
		
	private static class InstanceOfOntClassPane
		extends JPanel
		{
		private static final long serialVersionUID = 1L;
		private DialogEditor editorOwner;
		private OntClass ontClass;
		private Vector<OntProperty> properties;
		private AbstractAction removePaneAction;
		//private AbstractAction addPropertyAction;
		private JComboBox addPropertyCombo;
		private JPanel mainPane;
		//private Map<OntProperty,OntPropertiesPane> prop2instances;
		
		private class Row
			extends JPanel
			{
			private static final long serialVersionUID = 1L;
			PropertyInput propertyInput;
			Row(OntProperty property)
				{
				super(new BorderLayout(5,5));
				setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
				JLabel label=new JLabel(property.getLabel(),JLabel.RIGHT);
				label.setToolTipText(property.getComment());
				label.setPreferredSize(new Dimension(200,30));
				this.add(label,BorderLayout.WEST);
				this.propertyInput= property.createPropertyInput();
				this.add(this.propertyInput,BorderLayout.CENTER);
				}
			public PropertyInput getPropertyInput()
				{
				return propertyInput;
				}
			
			public OntProperty getOntProperty()
				{
				return getPropertyInput().getOntProperty();
				}
			
			String getValidationMessage()
				{
				return getPropertyInput().getErrorMessage();
				}
			}
		
		InstanceOfOntClassPane(DialogEditor editorOwner,OntClass ontClass)
			{
			super(new BorderLayout(5,5));
			this.setBorder(new BevelBorder(BevelBorder.RAISED));
			this.ontClass=ontClass;
			this.editorOwner=editorOwner;
			
			JPanel top=new JPanel();
			top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
			this.add(top,BorderLayout.NORTH);
			
			//put a label in the top 
			JPanel flow=new JPanel(new FlowLayout(FlowLayout.LEFT));
			top.add(flow);
			JLabel lab=new JLabel(ontClass.getLabel());
			lab.setFont(new Font("Dialog",Font.BOLD,24));
			lab.setForeground(Color.BLUE);
			lab.setToolTipText(ontClass.getComment());
			flow.add(lab);
			if(!ontClass.getLabel().equals(ontClass.getComment()))
				{
				JTextArea area=new JTextArea(ontClass.getComment());
				//area.setPreferredSize(new Dimension(320,200));
				area.setEditable(false);
				area.setLineWrap(true);
				area.setWrapStyleWord(true);
				top.add(area);
				}
			
			this.properties=new Vector<OntProperty>(ontClass.getSpecificProperties());
			flow=new JPanel(new FlowLayout(FlowLayout.LEFT));
			top.add(flow);
			
			JButton removePane=new JButton(this.removePaneAction=new AbstractAction("Delete")
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent ae)
					{
					InstanceOfOntClassPane.this.editorOwner.removeOntClass(InstanceOfOntClassPane.this.ontClass);
					}
				});
			removePane.setForeground(Color.RED);
			top.add(removePane);
			
			
			if(!this.properties.isEmpty())
				{
				//top.add(Box.createHorizontalStrut(5));
				//top.add(new JSeparator(JSeparator.HORIZONTAL));
				//top.add(Box.createHorizontalStrut(5));
				lab=new JLabel("Property:",JLabel.RIGHT);
				flow.add(lab);
				
				Collections.sort(this.properties);
				this.addPropertyCombo= new JComboBox(this.properties);
				flow.add(addPropertyCombo);
				flow.add(new JButton(new AbstractAction("Add")
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0)
						{
						Object o=addPropertyCombo.getSelectedItem();
						if(o==null) return;
						addPropertyPane(OntProperty.class.cast(o));
						}
					}));
				}
			this.add(top,BorderLayout.NORTH);
			
			this.mainPane=new JPanel(new VerticalLayout());
			JScrollPane scroll=new JScrollPane(mainPane);
			if(!this.properties.isEmpty())
				{
				this.add(scroll,BorderLayout.CENTER);
				}
			}
		
		private Row addPropertyPane(OntProperty p)
			{
			if(p==null) return null;
			Row row= new Row(p);
			this.mainPane.add(row);
			this.editorOwner.validate();
			this.editorOwner.repaint();
			return row;
			}
		
		private int countPropertyUsage(OntProperty p)
			{
			int count=0;
			for(int i=0;i< this.mainPane.getComponentCount();++i)
				{
				Component c=this.mainPane.getComponent(i);
				if(!(c instanceof Row )) continue;
				Row row=(Row)c;
				if(row.getOntProperty().equals(p)) 
					{
					count++;
					}
				}
			return count;
			}
		
		Map<OntProperty,Set<String>> toHash()
			{
			Map<OntProperty,Set<String>> h=new HashMap<OntProperty, Set<String>>();
			for(int i=0;i< this.mainPane.getComponentCount();++i)
				{
				Component c=this.mainPane.getComponent(i);
				if(!(c instanceof Row )) continue;
				Row row=(Row)c;
				Set<String> set=h.get(row.getOntProperty()); 
				if(set==null)
					{
					set=new HashSet<String>();
					h.put(row.getOntProperty(),set);
					}
				set.add(row.getPropertyInput().getValue());
				}
			return h;
			}
		
		public String getValidationMessage()
			{
			for(int i=0;i< this.mainPane.getComponentCount();++i)
				{
				Component c=this.mainPane.getComponent(i);
				if(!(c instanceof Row )) continue;
				Row row=(Row)c;
				String msg= row.getValidationMessage();
				if(msg!=null) return msg;
				}
			
			for(OntProperty p: this.ontClass.getSpecificProperties())
				{
				int count=countPropertyUsage(p);
				String msg= p.getCardinalityMessage(count);
				if(msg!=null) return msg;
				}
			
			return null;
			}
		}
	
	
	
	private static class IndividualsPane
		extends JPanel
		{
		private static final long serialVersionUID = 1L;
		
		private RDFStore rdfStore;
		private JTable table;
		private TableModel tableModel;
		private JTextField tfSearch;
		private JCheckBox cbRegex;
		private SearchStore currentSearch=null;
		private JSpinner spinLimit;
		private Set<OntClass> ontClassInScope=new HashSet<OntClass>();
		
		private class TableModel
			extends GenericTableModel<Individual>
			{
			private static final long serialVersionUID = 1L;


			@Override
			public String getColumnName(int column)
				{
				switch(column)
					{
					case 0: return "URI";
					case 1: return "Title";
					case 2: return "Classes";
					}
				return null;
				}
			@Override
			public int getColumnCount()
				{
				return 3;
				}
		
			
			@Override
			public Object getValueOf(Individual indi, int columnIndex)
				{
				switch(columnIndex)
					{
					case 0: return indi.getURI();
					case 1: return indi.getTitle();
					case 2: return StringUtils.join(indi.getOntClasses(), " ; ");
					}
				return null;
				}
			}
		
		IndividualsPane(RDFStore rdfStore)
			{
			super(new BorderLayout(3,3));
			this.rdfStore=rdfStore;
			JPanel top=new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.add(top,BorderLayout.NORTH);
			
			
			AbstractAction searchAction=new AbstractAction("Search")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent ae)
					{
					doMenuSearch();
					}
				};
			
			JLabel label= new JLabel("Search:",JLabel.RIGHT);
			top.add(label);
			this.tfSearch= new JTextField(10);
			this.tfSearch.addActionListener(searchAction);
			
			top.add(this.tfSearch);
			this.cbRegex= new JCheckBox("Regex",false);
			top.add(this.cbRegex);
			label= new JLabel("Limit:",JLabel.RIGHT);
			top.add(label);
			this.spinLimit= new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE-2,1));
			top.add(this.spinLimit);
			JButton search= new JButton(searchAction);
			top.add(search);
			
			/* creates an action searching for the previous page of result */ 
			AbstractAction action=new AbstractAction("Prev")
				{
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent ae)
				 	{
				 	if(IndividualsPane.this.currentSearch==null) return;
				 	//move the search index backward
				 	IndividualsPane.this.currentSearch.start_index=
				 		Math.max(0,
				 				IndividualsPane.this.currentSearch.start_index-IndividualsPane.this.currentSearch.max_return
				 		);
				 	doMenuSearchAgain();
				 	}
				};
			getActionMap().put("SEARCH_PREV", action);
			action.setEnabled(false);
			top.add(new JButton(action));
			
			/* creates an action searching for the next page of result */ 
			action=new AbstractAction("Next")
				{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae)
				 	{
				 	if(IndividualsPane.this.currentSearch==null) return;
				 	//move the search index forward
				 	IndividualsPane.this.currentSearch.start_index+=IndividualsPane.this.currentSearch.max_return;
				 	doMenuSearchAgain();
				 	}
				};
			getActionMap().put("SEARCH_NEXT", action);
			action.setEnabled(false);
			top.add(new JButton(action));
			
			
			this.table=new JTable(this.tableModel=new TableModel());
			JScrollPane scroll=new JScrollPane(this.table);
			this.add(scroll,BorderLayout.CENTER);
			
			JPanel bot=new JPanel(new FlowLayout(FlowLayout.TRAILING));
			this.add(bot,BorderLayout.SOUTH);
			
			
			
			this.tfSearch.getDocument().addDocumentListener(new DocumentAdapter()
				{
				@Override
				public void documentChanged(DocumentEvent e)
					{
					IndividualsPane.this.currentSearch=null;
					AbstractAction action=(AbstractAction)getActionMap().get("SEARCH_PREV");
					action.setEnabled(false);
					action=(AbstractAction)getActionMap().get("SEARCH_NEXT");
					action.setEnabled(false);
					}
				});
			}
		
		public RDFStore getStore()
			{
			return rdfStore;
			}
		
		void doMenuSearch()
			{
			SearchStore newsearch=new SearchStore();
			String s=this.tfSearch.getText();
			if(!s.trim().isEmpty())
				{
				if(!this.cbRegex.isSelected())
					{
					s=Pattern.quote(s);
					}
				try
					{
					newsearch.regex= Pattern.compile(s,Pattern.CASE_INSENSITIVE);
					}
				catch (Exception e)
					{
					JOptionPane.showMessageDialog(this,""+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					return;
					}
				}
			newsearch.havingOntClass=new HashSet<OntClass>();
			if(!ontClassInScope.isEmpty())
				{
				for(OntClass o:ontClassInScope)
					{
					newsearch.havingOntClass.add(OntClass.class.cast(o));
					}
				}
			
			newsearch.max_return= Math.max(1,((Number)this.spinLimit.getValue()).intValue());
			newsearch.start_index=0;
			this.currentSearch=newsearch;
			doMenuSearchAgain();
			}
		
		void doMenuSearchAgain()
			{
			if(this.currentSearch==null) return;
			
			this.currentSearch.individuals.clear();
			getStore().search(this.currentSearch);
			
			this.currentSearch.start_index+=this.currentSearch.individuals.size();
			
			AbstractAction action=(AbstractAction)getActionMap().get("SEARCH_PREV");
			action.setEnabled(this.currentSearch.start_index>0);
			action=(AbstractAction)getActionMap().get("SEARCH_NEXT");
			action.setEnabled(this.currentSearch.individuals.size()>=this.currentSearch.max_return);
			
			this.currentSearch.individuals.clear();
			}
		
		}
	
	/**
	 * DialogEditor
	 *
	 */
	private static class DialogEditor
		extends JDialog
		{
		private static final long serialVersionUID = 1L;
		private int exitStatus=JOptionPane.CANCEL_OPTION;
		private Frame frameOwner;
		private JTextField labelTitle;
		private JTextField labelURI;
		private JButton addClassButton;
		private JComboBox addClassCombo;
		private JPanel mainPane;
		private Map<OntClass,InstanceOfOntClassPane> class2pane=new HashMap<OntClass, InstanceOfOntClassPane>();
		
		
		DialogEditor(Frame frameOwner)
			{
		 	super(frameOwner,"Edit",true);
		 	this.frameOwner=frameOwner;
		 	this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	        JPanel pane=new JPanel(new BorderLayout(5,5));
	        setContentPane(pane);
	        pane.setBorder(new EmptyBorder(5,5,5,5));
	        this.labelTitle=new JTextField();
	        this.labelURI=new JTextField();
	        
	        this.addWindowListener(new WindowAdapter()
				{
				@Override
				public void windowClosing(WindowEvent e)
					{
					doDialogClosingOK();
					}
				});
	        
	        JPanel pane3= new JPanel();
	        pane3.setLayout(new BoxLayout(pane3, BoxLayout.Y_AXIS));
	        pane.add(pane3,BorderLayout.NORTH);
	        
	        JPanel pane2=new JPanel(new BorderLayout());
	        pane3.add(pane2,BorderLayout.NORTH);
	        pane2.add(new JLabel("URI:",JLabel.RIGHT),BorderLayout.WEST);
	        pane2.add(this.labelURI,BorderLayout.CENTER);
	  
	        pane2=new JPanel(new BorderLayout());
	        pane3.add(pane2,BorderLayout.NORTH);
	        pane2.add(new JLabel("Title:",JLabel.RIGHT),BorderLayout.WEST);
	        pane2.add(this.labelTitle,BorderLayout.CENTER);
	        
	        pane2=new JPanel(new BorderLayout(5,5));
	        pane.add(pane2,BorderLayout.CENTER);
	        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
	        pane2.add(top,BorderLayout.NORTH);
	        top.add(new JLabel("Add"));
	       
	        top.add(this.addClassCombo=new JComboBox(new DefaultComboBoxModel()));
	        top.add(this.addClassButton=new JButton("Add"));
	        
	        mainPane=new JPanel(new VerticalLayout());
	        mainPane.setOpaque(true);
	        mainPane.setBackground(Color.DARK_GRAY);
	        mainPane.setBorder(new EmptyBorder(5,5,5,5));
	        
	        JScrollPane scroll=new JScrollPane(mainPane);
	        pane2.add(scroll,BorderLayout.CENTER);
	        
	        
	        pane2=new JPanel(new BorderLayout());
	    	pane.add(pane2,BorderLayout.EAST);
	        JList linked=new JList();
	        scroll=new JScrollPane(linked);
	        pane2.add(scroll,BorderLayout.CENTER);
	        
	        JPanel bot=new JPanel(new FlowLayout(FlowLayout.RIGHT));
	        bot.add(new JButton(new AbstractAction("OK")
	        	{
	        	private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent actionEvent)
	        		{
	        		doDialogClosingOK();
	        		
	        		}
	        	}));
	        bot.add(new JButton(new AbstractAction("Cancel")
	        	{
	        	private static final long serialVersionUID = 1L;
	        	public void actionPerformed(ActionEvent actionEvent)
	        		{
	        		exitStatus=JOptionPane.CANCEL_OPTION;
	        		DialogEditor.this.setVisible(false);
	        		DialogEditor.this.dispose();
	        		}
	        	}));
	        pane.add(bot,BorderLayout.SOUTH);
	        
	        
			this.addClassButton.addActionListener(
	                    	new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent)
				{
				OntClass c= OntClass.class.cast(addClassCombo.getSelectedItem());
				if(c==null) return ;
				addOntClass(c);
				}});
			
			for(OntClass c: frameOwner.getSchema().uri2class.values())
				{
				InstanceOfOntClassPane ioc= new InstanceOfOntClassPane(this,c);
				this.class2pane.put(c, ioc);
				mainPane.add(ioc);
				break;
				}
			
			mainPane.addContainerListener(new ContainerAdapter()
				{
				@Override
				public void componentAdded(ContainerEvent e)
					{
					updateDeleteButtons();
					updateCombo();
					}
				@Override
				public void componentRemoved(ContainerEvent e)
					{
					componentAdded(e);
					}
				});
			updateCombo();
			updateDeleteButtons();
		 	}
		
	DialogEditor(Frame f,Individual individual)
		{
		this(f);
		setTitle(individual.getTitle());
		this.labelURI.setText(individual.getURI());
		this.labelTitle.setText(individual.getTitle());
		this.labelURI.setEditable(false);
		this.labelURI.setEnabled(false);
		for(OntClass c: individual.getOntClasses())
			{
			InstanceOfOntClassPane iocp=addOntClass(c);
			for(OntProperty prop: c.getSpecificProperties())
				{
				for(String value: individual.get(c, prop))
					{
					InstanceOfOntClassPane.Row row=iocp.addPropertyPane(prop);
					row.getPropertyInput().setValue(value);
					}
				}
		
			}
		}
	
	void doDialogClosingOK()
		{
		String msg=getValidationMessage();
		
		if(msg!=null)
			{
			JOptionPane.showMessageDialog(
				DialogEditor.this, msg,"Error",JOptionPane.ERROR_MESSAGE,null);
			return;
			}
		exitStatus=JOptionPane.OK_OPTION;
		this.setVisible(false);
		this.dispose();
		}
	
	public int getExitStatus()
		{
		return exitStatus;
		}
	
	InstanceOfOntClassPane addOntClass(OntClass c)
		{
		for(OntClass p: c.getParentClasses())
			{
			addOntClass(p);
			}
		InstanceOfOntClassPane iocp=class2pane.get(c);
		if(iocp!=null) return iocp;
		iocp=new InstanceOfOntClassPane(this, c);
		class2pane.put(c, iocp);
		mainPane.add(iocp);
		return iocp;
		}
	
	void removeOntClass(OntClass c)
		{
		InstanceOfOntClassPane iocp=class2pane.get(c);
		if(iocp==null) return;
		class2pane.remove(c);
		mainPane.remove(iocp);
		mainPane.validate();
		mainPane.repaint();
		updateCombo();
		updateDeleteButtons();
		}
	
	private String getIsRemovableMessage(OntClass c)
		{
		for(OntClass child:c.getAllDescendants())
			{
			if(this.class2pane.containsKey(child))
				{
				return "Cannot remove "+
						c.getShortName()+
						" because it is required by "+
						child.getShortName()
						;
				}
			}
		return null;
		}
	
	void updateDeleteButtons()
		{
		for(OntClass c: this.class2pane.keySet())
			{
			InstanceOfOntClassPane iocp= this.class2pane.get(c);
			String msg=getIsRemovableMessage(c);
			iocp.removePaneAction.setEnabled(msg==null);
			iocp.removePaneAction.putValue(AbstractAction.SHORT_DESCRIPTION, msg==null?"Remove":msg);
			}
		}
	
	void updateCombo()
		{
		Vector<OntClass> v=new Vector<OntClass>(this.frameOwner.getSchema().uri2class.values());
		v.removeAll(class2pane.keySet());
        Collections.sort(v);
        this.addClassCombo.setModel(new DefaultComboBoxModel(v));
        this.addClassButton.setEnabled(!v.isEmpty());
		}
	
	public String getValidationMessage()
		{
		for(InstanceOfOntClassPane p:this.class2pane.values())
			{
			String msg=p.getValidationMessage();
			if(msg!=null) return msg;
			}
		return null;
		}
	
	public Individual makeIndividual()
		{
		Individual indi=new Individual(this.labelURI.getText().trim());
		indi.name= this.labelTitle.getText();
		for(OntClass c:this.class2pane.keySet())
			{
			indi.instances.put(c, this.class2pane.get(c).toHash());
			}
		return indi;
		}
	}
	/**
	 *
	 * Frame
	 *
	 */
	private static class Frame
		extends JFrame
		{
		private ActionMap actionMap=new ActionMap();
		private static final long serialVersionUID = 1L;
		private RDFStore store;
		private IndividualsPane individualsPane;
		private JList listOfOntClasses;
		
		Frame(RDFStore store)
			{
			super(ModelingTool.class.getSimpleName());
			this.store=store;
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			this.addWindowListener(new WindowAdapter()
				{
				@Override
				public void windowClosing(WindowEvent e) {
					doMenuQuit();
					}
				});

			
			JMenuBar bar= new JMenuBar();
			setJMenuBar(bar);
			JMenu menu=new JMenu("File");
			bar.add(menu);
			menu.add(new AbstractAction("Quit")
				{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae)
				 	{
				 	doMenuQuit();
				 	}
				});
			JPanel content= new JPanel(new BorderLayout(5,5));
			content.setBorder(new EmptyBorder(5,5,5,5));
			this.setContentPane(content);
			Vector<OntClass> v=new Vector<OntClass>();
			v.addAll(store.getSchema().uri2class.values());
			Collections.sort(v);
			
			listOfOntClasses= new JList(v);
			listOfOntClasses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JScrollPane scroll=new JScrollPane(listOfOntClasses);
			content.add(scroll,BorderLayout.WEST);
			
			JPanel pane2=new JPanel(new BorderLayout(5,5));
			content.add(pane2,BorderLayout.CENTER);
			
			pane2.add(
				this.individualsPane=new IndividualsPane(store),
				BorderLayout.CENTER
				);
			
			JPanel top= new JPanel(new FlowLayout(FlowLayout.LEFT));
			pane2.add(top, BorderLayout.NORTH);
			
			AbstractAction action=new AbstractAction("Create")
				{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae)
				 	{
				 	Object array[]=Frame.this.listOfOntClasses.getSelectedValues();
				 	if(array.length==0) return;
				 	DialogEditor dialog= new DialogEditor(Frame.this);
				 	SwingUtils.center(dialog, 150);
                	for(Object o:array)
                		{
                		dialog.addOntClass(OntClass.class.cast(o));
                		}
                	dialog.setVisible(true);
                	if(dialog.getExitStatus()==JOptionPane.OK_OPTION)
                		{
                		Individual indi= dialog.makeIndividual();
                		if(indi!=null)
                			{
                			individualsPane.tableModel.addElement(indi);
                			getStore().add(indi);
                			}
                		}
				 	}
				};
			action.setEnabled(false);
			this.actionMap.put("CREATE_INSTANCE",action);
			top.add(new JButton(action));
			top.add(new JSeparator(SwingConstants.VERTICAL));
			
			
			
			
			
			
			listOfOntClasses.addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e)
					{
					if(e.getValueIsAdjusting() ) return;
					Object array[]=listOfOntClasses.getSelectedValues();
					if(array.length==0)
						{
						
						}
					else
						{
						
						}
					Frame.this.actionMap.get("CREATE_INSTANCE").setEnabled(array.length!=0);
					}
				});
			
			
			individualsPane.table.addMouseListener(new MouseAdapter()
				{
				@Override
				public void mouseClicked(MouseEvent e)
					{
					if(e.getClickCount()<2) return;
					int rowIndex= individualsPane.table.rowAtPoint(e.getPoint());
					if(rowIndex==-1) return;
					rowIndex= individualsPane.table.convertRowIndexToView(rowIndex);
					if(rowIndex==-1 || rowIndex>= individualsPane.tableModel.getRowCount()) return;
					Individual indi= individualsPane.tableModel.elementAt(rowIndex);
					editIndividual(indi);
					
					individualsPane.tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
					}
				});
			
			}
		
		private void editIndividual(Individual indi)
			{
			DialogEditor dialog= new DialogEditor(Frame.this,indi);
		 	SwingUtils.center(dialog, 150);
        	
        	dialog.setVisible(true);
        	if(dialog.getExitStatus()==JOptionPane.OK_OPTION)
        		{
        		indi= dialog.makeIndividual();
        		getStore().update(indi);
        		}
			}

			
		void doMenuQuit()
			{
			this.setVisible(false);
			this.dispose();
			}
		
		
		
		RDFStore getStore()
			{
			return store; 
			}
		Schema getSchema()
			{
			return getStore().getSchema(); 
			}
		}
	
	public static class SearchStore
		{
		List<Individual> individuals= new ArrayList<Individual>();
		int start_index=0;
		int max_return=100;
		Pattern regex=null;
		Set<OntClass> havingOntClass=null;
		}
	
	/**
	 * Store
	 * stores some Individuals
	 */
	public static interface RDFStore
		extends Iterable<Individual>
		{
		/** answers the schema associated to this store */
		public Schema getSchema();
		public Individual findIndividualByURI(String uri);
		public void search(SearchStore param);
		public void add(Individual i);
		public void update(Individual i);
		}
	
	/**
	 * Default implementation of a RDFStore
	 *
	 */
	public static class DefaultRDFStore
	implements RDFStore
		{
		private Schema schema;
		private java.util.List<Individual> individuals=new ArrayList<Individual>();
		DefaultRDFStore(Schema schema)
			{
			this.schema=schema;
			}
		
		@Override
		public void update(Individual indi)
			{
			for(int i=0;i< individuals.size();++i)
				{
				if(this.individuals.get(i).getURI().equals(indi.getURI()))
					{
					individuals.set(i, indi);
					return;
					}
				}
			this.individuals.add(indi);
			}
		
		@Override
		public void add(Individual indi)
			{
			for(int i=0;i< individuals.size();++i)
				{
				if(this.individuals.get(i).getURI().equals(indi.getURI()))
					{
					throw new IllegalArgumentException("URI already defined :"+indi.getURI());
					}
				}
			this.individuals.add(indi);
			}
		@Override
		public Schema getSchema()
			{
			return this.schema;
			}
		public java.util.List<Individual> getIndividuals()
			{
			return this.individuals;
			}
		
		
		@Override
		public Individual findIndividualByURI(String uri)
			{
			for(Individual i: getIndividuals())
				{
				if(i.getURI().equals(uri)) return i;
				}
			return null;
			}
		
		public Iterator<Individual> iterator()
			{
			return getIndividuals().iterator();
			}
		
		public void search(SearchStore param)
			{
			int current_index=-1;
			int i=0;
			while(i< getIndividuals().size())
				{
				Individual indi= getIndividuals().get(i);
				boolean ok=true;
				if(param.regex!=null && !indi.matches(param.regex))
					{
					ok=false;
					}
				if(ok && param.havingOntClass!=null && !param.havingOntClass.isEmpty())
					{
					for(OntClass c: param.havingOntClass)
						{
						if(!indi.contains(c))
							{
							ok=false;
							break;
							}
						}
					}
				if(ok)
					{
					++current_index;
					if(current_index>= param.start_index)
						{
						param.individuals.add(indi);
						if(param.individuals.size()>=param.max_return)
							{
							return;
							}
						}
					}
				++i;
				}
			}
		}
	
	public static void main(String args[])
		{
		RDFStore store=null;
		Schema schema=new Schema();
		
		try
			{
			JFrame.setDefaultLookAndFeelDecorated(true);

			int optind=0;
            while(optind< args.length)
                    {
                    if(args[optind].equals("-h"))
                            {
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
            	
             schema.parse(new File("/home/pierre/tmp/XSLT/schema.rdf"));
            
             store=new DefaultRDFStore(schema);
             
			Frame f= new Frame(store);
		    SwingUtils.center(f,100);
			SwingUtils.show(f); 
			}
		catch(Exception err)
			{
			err.printStackTrace();
			}
		}
	}

