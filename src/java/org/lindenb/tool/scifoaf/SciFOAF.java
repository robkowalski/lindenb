/**
 * 
 */
package org.lindenb.tool.scifoaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lindenb.io.PreferredDirectory;
import org.lindenb.lang.RunnableObject;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.sw.PrefixMapping;
import org.lindenb.sw.vocabulary.DC;
import org.lindenb.sw.vocabulary.FOAF;
import org.lindenb.sw.vocabulary.RDF;
import org.lindenb.swing.ConstrainedAction;
import org.lindenb.swing.DocumentAdapter;
import org.lindenb.swing.ObjectAction;
import org.lindenb.swing.SimpleDialog;
import org.lindenb.swing.SwingUtils;
import org.lindenb.swing.layout.InputLayout;
import org.lindenb.swing.table.GenericTableModel;
import org.lindenb.util.Assert;
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;
import org.lindenb.util.IStringComparator;
import org.lindenb.util.Pair;
import org.lindenb.util.XObject;

/**
 * @author pierre
 *
 */
public class SciFOAF extends JFrame
	{
	private static final long serialVersionUID = 1L;
	static private final IStringComparator ISTRING_COMPARATOR= new IStringComparator();
	
	private abstract static class AbstractPredicate<X> extends XObject
		{
		private URI uri;
		private X value;
		protected AbstractPredicate(URI uri,X value)
			{
			this.uri=uri;
			this.value=value;
			}

		public URI getPredicate() { return this.uri; }
		public X getValue() { return this.value;}
		@Override
		public int hashCode() {
			return getPredicate().hashCode()+getValue().hashCode();
			}
		}
		
	private static class LinkPredicate extends AbstractPredicate<URI>
		{
		LinkPredicate(URI uri,URI value)
			{
			super(uri,value);
			}
		
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof LinkPredicate)) return false;
			LinkPredicate cp= LinkPredicate.class.cast(obj);
			return getPredicate().equals(cp.getPredicate()) &&
					getValue().equals(cp.getValue());
			}
		
		@Override
		public String toString() {
			return "<"+getPredicate()+"> <"+getValue()+">";
			}
		}
	
	private abstract static class Predicate<T> extends AbstractPredicate<T>
		{
		Predicate(URI uri,T value)
			{
			super(uri,value);
			}
		}
	
	private static class DataPredicate extends Predicate<String>
		{
		DataPredicate(URI uri,String value)
			{
			super(uri,value);
			}
		public boolean isObjectProperty() { return false;}
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof DataPredicate)) return false;
			DataPredicate cp= DataPredicate.class.cast(obj);
			return getPredicate().equals(cp.getPredicate()) &&
					SciFOAF.ISTRING_COMPARATOR.compare(getValue(), cp.getValue())==0;
			}
		@Override
		public String toString() {
			return "<"+getPredicate()+"> \""+getValue()+"\"";
			}
		}
	
	private static class ObjectPredicate extends Predicate<Instance>
		{
		ObjectPredicate(URI uri,Instance value)
			{
			super(uri,value);
			}
		public boolean isObjectProperty() { return true;}
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof ObjectPredicate)) return false;
			ObjectPredicate cp= ObjectPredicate.class.cast(obj);
			return getPredicate().equals(cp.getPredicate()) &&
					getValue().equals(cp.getValue());
			}
		
		@Override
		public String toString() {
			return "<"+getPredicate()+"> <"+getValue()+">";
			}
		}
	
	private static abstract class Instance extends XObject
		{
		private static final long serialVersionUID = 1L;
		private boolean anonymousURI;
		private URI uri=null;
		private URI rdfType;
		private Vector<DataPredicate> dataPredicates= new Vector<DataPredicate>(1,1);
		private Vector<ObjectPredicate> objectPredicates= new Vector<ObjectPredicate>(1,1);
		
		Instance(URI rdfType)
			{
			this.rdfType=rdfType;
			}
		
		public URI getRDFType() { return this.rdfType;}
		public URI getID()
			{
			return this.uri;
			}
		public void setID(URI uri)
			{
			this.uri=uri;
			this.anonymousURI=false;
			}
		
		public boolean addDataProperty(URI predicate,String value)
			{
			DataPredicate p=new DataPredicate(predicate,value);
			if(this.dataPredicates.contains(p)) return false;
			this.dataPredicates.addElement(p);
			return true;
			}
		
		public boolean addObjectProperty(URI predicate,Instance value)
			{
			ObjectPredicate p=new ObjectPredicate(predicate,value);
			if(this.objectPredicates.contains(p)) return false;
			this.objectPredicates.addElement(p);
			return true;
			}
		
		@Override
		public int hashCode() {
			return getID().hashCode();
			}
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof Instance)) return false;
			Instance cp= Instance.class.cast(obj);
			return this.getRDFType().equals(cp.getRDFType()) &&
				   this.getID().equals(cp.getID())
				   ;
			}
		
		public String getDataProperty(URI predicate)
			{
			for(DataPredicate p:this.dataPredicates)
				{
				if(p.getPredicate().equals(predicate)) return p.getValue();
				}
			return null;
			}
		
		public Collection<DataPredicate> getDataProperties(URI predicate)
			{
			Vector<DataPredicate> v= new Vector<DataPredicate>();
			for(DataPredicate p:this.dataPredicates)
				{
				if(p.getPredicate().equals(predicate))  v.addElement(p);
				}
			return v;
			}
		
		public String getName()
			{
			String s= getDataProperty(URI.create(FOAF.NS+"name"));
			if(s==null) s= getDataProperty(URI.create(DC.NS+"title"));
			if(s==null) s= getID().toString();
			return s;
			}
		
		@Override
		public String toString() {
			return getRDFType()+":"+getID();
			}
		
		private Pair<String,String>  split(URI uri)
			{
			String s= uri.toString();
			int n= s.lastIndexOf('#');
			if(n==-1) n= s.lastIndexOf('/');
			return new Pair<String, String>(s.substring(0,n+1),s.substring(n+1));
			}
		
		public void write(XMLStreamWriter w) throws XMLStreamException
			{
			Pair<String,String> p= split(getRDFType());
			w.writeStartElement(getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeAttribute(RDF.NS, "ID", getID().toString());
			w.writeCharacters("\n");
			
			for(DataPredicate prop:this.dataPredicates)
				{
				p= split(prop.getPredicate());
				w.writeCharacters("\t");
				w.writeStartElement(getModel().getNsURIPrefix(p.first()),p.second(),p.first());
				w.writeCharacters(prop.getValue());
				w.writeEndElement();
				w.writeCharacters("\n");
				}
			
			w.writeEndElement();
			w.writeCharacters("\n");
			}
		
		
		public abstract RDFModel getModel();
		}
	
	
	private static class BidirectionalStatement
		{
		private Instance from;
		private Instance to;
		private URI fromTo;
		private URI toFrom;
		BidirectionalStatement(Instance from,URI fromTo,Instance to)
			{
			this(from,fromTo,fromTo,to);
			}
		
		BidirectionalStatement(Instance from,URI fromTo,URI toFrom,Instance to)
			{
			this.from=from; this.to=to;
			this.fromTo=fromTo; this.toFrom=toFrom;
			}
		}
	
	private static class RDFModel
		extends PrefixMapping
		{
		private Vector<Instance> instances= new Vector<Instance>(1000,100);
		
		
		public RDFModel()
			{
			super(true);
			}
		
		public void saveTo(File file) throws IOException
			{
			FileWriter fout = new FileWriter(file);
			
			try
				{
				XMLOutputFactory f=XMLOutputFactory.newInstance();
				XMLStreamWriter w= f.createXMLStreamWriter(fout);
				w.writeStartDocument();
				
				
				
				w.writeStartElement("rdf", "RDF", RDF.NS);
				for(String prefix: this.getPrefixes())
					{
					w.setPrefix(prefix, getNsPrefixURI(prefix));
					w.writeAttribute("xmlns:"+prefix, getNsPrefixURI(prefix));
					}
				
				w.writeCharacters("\n");
				
				for(Instance instance: getInstanceVector())
					{
					instance.write(w);
					}
				
				w.writeEndElement();
				w.writeEndDocument();
				}
			catch(XMLStreamException err)
				{
				err.printStackTrace();
				throw new IOException(err);
				}
			
			
			fout.flush();
			fout.close();
			}
		
		public void read(File file) throws XMLStreamException
			{
			XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            boolean foundRoot=false;
            try
	            {
	            XMLEventReader parser = factory.createXMLEventReader(new FileInputStream(file));
	            while(parser.hasNext())
					{
					XMLEvent event = parser.nextEvent();
					if(event.isStartElement())
						{
						QName name= event.asStartElement().getName();
						if(!foundRoot)
							{
							if(!(name.getNamespaceURI().equals(RDF.NS) && name.getLocalPart().equals("RDF")))
								{
								throw new XMLStreamException("expected rdf:RDF as root");
								}
							foundRoot=true;
							}
						else if(name.getNamespaceURI().equals(RDF.NS) && name.getLocalPart().equals("RDF"))
							{
							
							}
						else
							{
							Instance i= parseInstance(parser, event.asStartElement());
							getInstanceVector().addElement(i);
							}
						}
					}
	            }
            catch(IOException err)
            	{
            	throw new XMLStreamException(err);
            	}
			}
		
		public URI createAnonymousURI()
			{
			long n= System.currentTimeMillis();
			while(true)
				{
				URI uri= URI.create("_"+n);
				if(findInstanceByURI(uri)==null) return uri;
				++n;
				}
			}
		
		public Instance newInstance(URI rdfType)
			{
			return new Instance(rdfType)
				{
				@Override
				public RDFModel getModel() {
					return RDFModel.this;
					}
				};
			}
		
		protected Vector<Instance> getInstanceVector()
			{	
			return this.instances;
			}
		
		public Instance findInstanceByURI(URI uri)
			{
			for(Instance i:getInstanceVector())
				{
				if(i.getID().equals(uri)) return i;
				}
			return null;
			}
		
		public Instance findInstanceByTypeAndURI(URI rdfType,URI uri)
			{
			for(Instance i:getInstanceVector())
				{
				if( i.getRDFType().equals(rdfType) &&
					i.getID().equals(uri)) return i;
				}
			return null;
			}
		
		public Collection<Instance> findInstancesByType(URI rdfType)
			{
			Vector<Instance> v= new Vector<Instance>();
			for(Instance i:getInstanceVector())
				{
				if(i.getRDFType().equals(rdfType)) v.addElement(i);
				}
			return v;
			}
		
		private Instance parseInstance(XMLEventReader parser,StartElement element) throws XMLStreamException
			{
			try {
				URI rdfType= new URI(element.getName().getNamespaceURI()+element.getName().getLocalPart());
				Instance instance= newInstance(rdfType);
				Attribute att=element.getAttributeByName(new QName(RDF.NS,"about"));
				if(att==null) att=element.getAttributeByName(new QName(RDF.NS,"ID"));
				if(att==null) throw new XMLStreamException("Cannot find rdf:ID/about in "+element.getName());
				URI id=new URI(att.getValue());
				if(findInstanceByURI(id)!=null)
					{
					throw new XMLStreamException("Instance id "+id+" defined twice");
					}
				instance.setID(id);
				while(parser.hasNext())
					{
					XMLEvent event = parser.nextEvent();
					if(event.isEndElement())
						{
						break;
						}
					else if(event.isStartElement())
						{
						StartElement start2= event.asStartElement();
						URI predicateURI= new URI(start2.getName().getNamespaceURI()+start2.getName().getLocalPart());
						StringBuilder textContent=new StringBuilder();
						while(parser.hasNext())
							{
							event = parser.nextEvent();
							if(event.isEndElement())
								{
								if(textContent!=null)
									{
									instance.addDataProperty(predicateURI,textContent.toString());
									}
								break;
								}
							else if(event.isStartElement())
								{
								if(textContent!=null && textContent.toString().trim().length()!=0)
									{
									throw new XMLStreamException("found no white content in "+instance.getID()+" "+predicateURI+" "+textContent);
									}
								textContent=null;
								instance.addObjectProperty(predicateURI,parseInstance(parser, event.asStartElement()));
								}
							else if(event.isCharacters() && textContent!=null)
								{
								textContent.append(event.asCharacters().getData());
								}
							}
						}
					}
				return instance;
				}
			catch (URISyntaxException e) {
				throw new XMLStreamException(e);
				}
			}
		}
	

	static private class PropertyOption
		{
		String name;
		URI uri;
		int minOcc=0;
		int maxOcc=Integer.MAX_VALUE;
		PropertyOption(String ns,String prefix,String local)
			{
			this.name=prefix+":"+local;
			this.uri=URI.create(ns+local);
			}
		@Override
		public String toString() {
			return this.name;
			}
		}
	
	static private class DataPropOption extends PropertyOption
		{
		Pattern pattern=Pattern.compile(".*");
		String defaultValue="";
		Class<?> clazz=String.class;
		DataPropOption(String ns,String prefix,String local)
			{
			super(ns,prefix,local);
			}
		}
	static private class ObjectPropOption extends PropertyOption
		{
		Class<Instance> EditorClass=null;
		URI rdfEditedType=null;
		ObjectPropOption(String ns,String prefix,String local)
			{
			super(ns,prefix,local);
			}
		}
	
	static private class LinkOption extends PropertyOption
		{
		URI scopeRdfType;
		URI reverseURI;
		LinkOption(String ns,String prefix,String local,String targetNS,String targetLocal)
			{
			super(ns,prefix,local);
			this.scopeRdfType= URI.create(targetNS+targetLocal);
			this.reverseURI=scopeRdfType;
			}
		
		}
	
	
	private class InstanceEditor extends SimpleDialog
		{
		private static final long serialVersionUID = 1L;
		Vector<PropertyOption> dataPropOptions= new Vector<PropertyOption>();
		JLabel iconLabel;
		JLabel titleLabel;
		JLabel idLabel;
		URI rdfType;
		JTabbedPane tabbedPane;
		JTable predicateTable;
		JTextField dataPredicateValueField;
		GenericTableModel<AbstractPredicate<?>> predicateModel;
		AbstractAction actionAddPredicate;
		JComboBox predicateCombo;
		protected URI instanceID=null;
		
		InstanceEditor(java.awt.Component owner,URI rdfType)
			{
			super(owner,rdfType.toString());
			this.rdfType=rdfType;
			buildOptions(dataPropOptions);
			JPanel main= new JPanel(new BorderLayout());
			JPanel top= new JPanel(new BorderLayout());
			main.add(top,BorderLayout.NORTH);
			top.add(iconLabel=new JLabel(),BorderLayout.WEST);
			top.add(this.titleLabel= new JLabel("",JLabel.LEFT),BorderLayout.CENTER);
			this.titleLabel.setFont(new Font("Dialog",Font.BOLD,24));
			Font smallFont = new Font("Dialog",Font.PLAIN,9);
			top.add(this.idLabel= new JLabel("",JLabel.RIGHT),BorderLayout.SOUTH);
			this.idLabel.setFont(smallFont);
			top.add(SwingUtils.withFont( new JLabel(rdfType.toString(),JLabel.RIGHT),smallFont),BorderLayout.NORTH);
			
			
			this.tabbedPane= new JTabbedPane(JTabbedPane.SCROLL_TAB_LAYOUT);
			main.add(tabbedPane,BorderLayout.CENTER);
			
			JPanel pane2= new JPanel(new BorderLayout());
			pane2.add(new JScrollPane(predicateTable=new JTable(this.predicateModel=buildDataPredicateModel())),BorderLayout.CENTER);
			top= new JPanel(new FlowLayout(FlowLayout.LEADING));
			pane2.add(top,BorderLayout.NORTH);
			top.add(new JLabel("Add Property:",JLabel.RIGHT));
			this.predicateCombo=new JComboBox(new DefaultComboBoxModel(this.dataPropOptions));
			top.add(predicateCombo);
			top.add(dataPredicateValueField=new JTextField(30));
			this.actionAddPredicate=new AbstractAction("Add")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					PropertyOption opt= PropertyOption.class.cast(predicateCombo.getSelectedItem());
					if(opt==null) return;
					if(DataPropOption.class.isInstance(opt))
						{
						DataPredicate p=new DataPredicate(opt.uri,dataPredicateValueField.getText().trim());
						if(predicateModel.contains(p)) return;
						predicateModel.addElement(p);
						dataPredicateValueField.setText("");
						}
					else if(ObjectPropOption.class.isInstance(opt))
						{
						InstanceEditor ed= getInstanceEditor(InstanceEditor.this,
								ObjectPropOption.class.cast(opt).rdfEditedType
								);
						ed.setInstanceURI(getModel().createAnonymousURI());
						if(ed.showDialog()!=OK_OPTION) return;
						Instance instance= getModel().newInstance(ed.instanceID);
						ObjectPredicate p= new ObjectPredicate(opt.uri,instance);
						if(predicateModel.contains(p)) return;
						predicateModel.addElement(p);
						}
					else if(LinkOption.class.isInstance(opt))
						{
						SimpleDialog d= new SimpleDialog(InstanceEditor.this,"Link...");
						GenericTableModel<Instance> m= new GenericTableModel<Instance>()
							{
							private static final long serialVersionUID = 1L;
							@Override
							public String getColumnName(int column) {
								switch(column)
									{
									case 0: return "ID";
									case 1: return "Name";
									}
								return null;
								}
							@Override
							public int getColumnCount() {
								return 2;
								}
							@Override
							public Object getValueOf(Instance object,
									int columnIndex)
								{
								switch(columnIndex)
									{
									case 0: return object.getID();
									case 1: return object.getName();
									}
								return null;
								}
							};
						for(Instance i:getModel().getInstanceVector())
							{
							if(i.getRDFType().equals(LinkOption.class.cast(opt).scopeRdfType))
								{
								m.addElement(i);
								}
							}
						JPanel pane= new JPanel(new BorderLayout());
						JTable t= new JTable(m);
						pane.add(new JScrollPane(pane));
						d.getContentPane().add(pane);
						d.getOKAction().mustHaveRowsSelected(t, 1, Integer.MAX_VALUE);
						if(d.showDialog()!=OK_OPTION) return;
						for(int i: t.getSelectedRows())
							{
							Instance x= m.elementAt(t.convertRowIndexToModel(i));
							LinkPredicate p= new LinkPredicate(opt.uri,x.getID());
							if(predicateModel.contains(p)) return;
							predicateModel.addElement(p);
							}
						}
					}
				};
			top.add(new JButton(actionAddPredicate));
			actionAddPredicate.setEnabled(false);
			dataPredicateValueField.getDocument().addDocumentListener(new DocumentAdapter()
				{
				@Override
				public void documentChanged(DocumentEvent e) {
					updateAddDataPredicateAction();
					}	
				});
			predicateCombo.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent e)
					{
					PropertyOption opt= PropertyOption.class.cast(predicateCombo.getSelectedItem());
					dataPredicateValueField.setEnabled(opt!=null && DataPropOption.class.isInstance(opt));
					updateAddDataPredicateAction();
					}
				});
			this.predicateCombo.setSelectedIndex(-1);
			
			ConstrainedAction<InstanceEditor> action= new ConstrainedAction<InstanceEditor>(this,"Remove")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					int i= predicateTable.getSelectedRow();
					if(i==-1) return;
					i= predicateTable.convertRowIndexToModel(i);
					if(i!=-1) predicateModel.removeElementAt(i);
					}
				};
			top.add(Box.createHorizontalGlue());
			top.add(new JButton(action));
			action.mustHaveOneRowSelected(predicateTable);
			
			this.tabbedPane.addTab("Data Properties", pane2);
			
			pane2= new JPanel(new BorderLayout());
			this.tabbedPane.addTab("Object Properties", pane2);
			
			getContentPane().add(main);
			}
		
		void updateAddDataPredicateAction()
			{
			boolean enabled=false;
			String s=this.dataPredicateValueField.getText().trim();
			
			PropertyOption option1=PropertyOption.class.cast(predicateCombo.getSelectedItem());
			if(option1==null)
				{
				Debug.debug();
				}
			else if(DataPropOption.class.isInstance(option1) &&
				s.length()>0)
				{
				Debug.debug();
				DataPropOption option=DataPropOption.class.cast(option1);
				DataPredicate p= new DataPredicate(option.uri,s.trim());
				if(!this.predicateModel.contains(p))
					{
					if(option.pattern.matcher(s).matches())
						{
						enabled=true;
						}
					
					if(enabled && option.clazz!=String.class)
						{
						try {
							option.clazz.getConstructor(String.class).newInstance(s);
							enabled=true;
							}
						catch (Exception e) {
							enabled=false;
							}
						}
					
					if(enabled)
						{
						int count= 0;
						for(Iterator<AbstractPredicate<?>> i= this.predicateModel.listElements();
							i.hasNext();)
							{
							if(i.next().getPredicate().equals(option.uri))
								{
								++count;
								}
							}
						if(count<option.maxOcc)
							{
							enabled=true;
							}
						}
					}
				}
			else if(ObjectPropOption.class.isInstance(option1))
				{
				Debug.debug();
				enabled=true;
				}
			else if(LinkPredicate.class.isInstance(option1))
				{
				Debug.debug();
				enabled=true;
				}
			this.actionAddPredicate.setEnabled(enabled);
			}
		
		public void setInstance(Instance instance)
			{
			setInstanceURI(instance.getID());
			this.idLabel.setText(instance.getID().toString());
			this.titleLabel.setText(instance.getName());
			for(DataPredicate p:instance.dataPredicates)
				{
				this.predicateModel.addElement(p);
				}
			for(ObjectPredicate p:instance.objectPredicates)
				{
				this.predicateModel.addElement(p);
				}
			}
		
		public void setInstanceURI(URI instanceID)
			{
			this.instanceID= instanceID;
			this.titleLabel.setText(this.instanceID.toString());
			this.idLabel.setText(this.instanceID.toString());
			}
		
		void buildOptions(Vector<PropertyOption> options)
			{
			DataPropOption option= new DataPropOption(DC.NS,"dc","title");
			options.addElement(option);
			}
		
		public GenericTableModel<AbstractPredicate<?>> buildDataPredicateModel()
			{
			return new GenericTableModel<AbstractPredicate<?>>()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public String getColumnName(int column) {
					switch(column)
					{
					case 0: return "Predicate";
					case 1: return "Value";
					}
					return null;
					}
				@Override
				public int getColumnCount() {
					return 2;
					}
				@Override
				public Object getValueOf(AbstractPredicate<?> object,
						int columnIndex) {
					switch(columnIndex)
						{
						case 0: return object.getPredicate();
						case 1: return object.getValue();
						}
					return null;
					}
				};
			}
		
		public Instance updateModel()
			{
			Assert.assertNotNull(this.instanceID);
			Instance instance=getModel().findInstanceByTypeAndURI(this.rdfType, this.instanceID);
			if(instance==null)
				{
				instance= getModel().newInstance(this.rdfType);
				instance.setID(this.instanceID);
				getModel().getInstanceVector().addElement(instance);
				}
			instance.dataPredicates.clear();
			instance.objectPredicates.clear();
			for(Iterator<AbstractPredicate<?>> iter=this.predicateModel.listElements();
				iter.hasNext();
				)
				{
				AbstractPredicate<?> p=iter.next();
				if(DataPredicate.class.isInstance(p))
					{
					instance.dataPredicates.addElement(DataPredicate.class.cast(p));
					}
				else if(ObjectPredicate.class.isInstance(p))
					{
					instance.objectPredicates.addElement(ObjectPredicate.class.cast(p));
					}
				}
			return instance;
			}
		}
	
	private class AgentEditor extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		protected AgentEditor(Component owner,String localName)
			{
			super(owner,URI.create(FOAF.NS+localName));
			}
		@Override
		void buildOptions(Vector<PropertyOption> options) {
			DataPropOption opt= new DataPropOption(FOAF.NS,"foaf","name");
			options.add(opt);
			super.buildOptions(options);
			}
		}
	
	private class PersonEditor extends AgentEditor
		{
		private static final long serialVersionUID = 1L;

		PersonEditor(Component owner)
			{
			super(owner,"Person");
			}
		
		@Override
		void buildOptions(Vector<PropertyOption> options) {
			PropertyOption opt= new DataPropOption(FOAF.NS,"foaf","firstName");
			options.add(opt);
			LinkOption lkopt= new LinkOption(FOAF.NS,"foaf","knows",FOAF.NS,"Person");

			options.add(lkopt);
			super.buildOptions(options);
			}
		}
	
	
	private RDFModel model;
	private URI currentRDFType=null;
	private JTable instanceTable;
	private Vector<JButton> setRDFTypeButtons = new Vector<JButton>(10,1);
	private GenericTableModel<Instance> intancesTableModel;
	private File saveAsFile=null;
	private SciFOAF(File saveAsFile,RDFModel model)
		{
		super("SciFOAF");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.model=model;
		this.saveAsFile=saveAsFile;
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent e) {
				doMenuQuit();
				}
			});
		JMenuBar bar= new JMenuBar();
		setJMenuBar(bar);
		JMenu menu= new JMenu("File");
		bar.add(menu);
		menu.add(new JMenuItem(new AbstractAction("About...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(SciFOAF.this,
					"<html><body>"+
					"<h1 align='center'>SciFOAF 2.0</h1>"+
					"<h2 align='center'>"+Compilation.getLabel()+"</h2>"+
					"</body></html>",
					"About",JOptionPane.PLAIN_MESSAGE,null
					);
				}
			}));
		
		menu.add(new JMenuItem(new AbstractAction("Save As...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuSaveAs();
				}
			}));
		
		menu.add(new JMenuItem(new AbstractAction("Save")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuSave(SciFOAF.this.saveAsFile);
				}
			}));
		
		menu.add(new JMenuItem(new AbstractAction("Quit")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuQuit();
				}
			}));
		
		JPanel pane1= new JPanel(new BorderLayout());
		setContentPane(pane1);
		
		JPanel top= new JPanel(new GridLayout(1,0,5,2));
		pane1.add(top,BorderLayout.NORTH);
		top.setBorder(new EmptyBorder(2,2,2,2));
		top.add(createChooseRDFTypeButton(FOAF.NS,"foaf","Person"));
		top.add(createChooseRDFTypeButton(FOAF.NS,"foaf","Group"));
		top.add(createChooseRDFTypeButton(FOAF.NS,"foaf","Image"));
		top.add(createChooseRDFTypeButton(FOAF.NS,"foaf","Document"));
		
		this.intancesTableModel= new GenericTableModel<Instance>()
			{
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int column) {
				switch(column)
					{
					case 0: return "ID";
					case 1: return "Name";
					}
				return null;
				}
			
			@Override
			public int getColumnCount() {
				return 2;
				}
			
			@Override
			public Object getValueOf(Instance object, int columnIndex) {
				switch(columnIndex)
					{
					case 0: return object.getID();
					case 1: return object.getName();
					}
				return null;
				}
			};
		
		
		
		instanceTable= new JTable(this.intancesTableModel);
		pane1.add(new JScrollPane(instanceTable));
		
		
		JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));	
		pane1.add(bot,BorderLayout.SOUTH);
		bot.add(new JButton(new AbstractAction("New...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuNewInstance(SciFOAF.this,SciFOAF.this.currentRDFType);
				}
			}));
		ConstrainedAction<JTable> action= new ConstrainedAction<JTable>(instanceTable,"Edit...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuEditInstance(SciFOAF.this,intancesTableModel.elementAt(instanceTable.convertRowIndexToModel(instanceTable.getSelectedRow())));
				}
			};
		
		action.mustHaveOneRowSelected(instanceTable);
		bot.add(new JButton(action));
		action= new ConstrainedAction<JTable>(instanceTable,"Remove...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				
				}
			};
		action.mustHaveOneRowSelected(instanceTable);
		bot.add(new JButton(action));
		
		
		
		setRDFType(URI.create(FOAF.NS+"Person"));
		SwingUtils.center(this,50);
		}
	private void doMenuSave(File f)
		{
		if(f==null)
			{
			doMenuSaveAs();
			return;
			}
		
		try {
			getModel().saveTo(f);
			this.saveAsFile=f;
			PreferredDirectory.setPreferredDirectory(f);
			Preferences prefs= Preferences.userNodeForPackage(SciFOAF.class);
			prefs.put("save.as.file",f.toString());
			prefs.sync();
			}
		catch (Exception err)
			{
			ThrowablePane.show(this, err);
			}
		}
	
	private void doMenuSaveAs()
		{
		JFileChooser chooser= new JFileChooser();
		
		chooser.setSelectedFile(
				this.saveAsFile!=null?
				this.saveAsFile:
				new File(PreferredDirectory.getPreferredDirectory(),"foaf.rdf")
				);
		
		if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		File  f= chooser.getSelectedFile();
		if(f==null || (f.exists() && JOptionPane.showConfirmDialog(this, f.toString()+" exists. Overwrite.","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.OK_OPTION))
			{
			return;
			}
		doMenuSave(f);
		}
	
	private JButton createChooseRDFTypeButton(String ns,String prefix,String local)
		{
		JButton but= new JButton(new ObjectAction<URI>(URI.create(ns+local),prefix+":"+local)
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setRDFType(getObject());
				}
			});
		this.setRDFTypeButtons.addElement(but);
		return but;
		}
	
	private void setRDFType(URI rdfType)
		{
		this.currentRDFType=rdfType;
		for(JButton b:setRDFTypeButtons)
			{
			b.setFont( new Font("Dialog",( ((ObjectAction<?>)b.getAction()).getObject().equals(rdfType)?Font.BOLD:Font.PLAIN),18));
			}
		updateInstancesTable();
		}
	
	private void updateInstancesTable()
		{
		this.intancesTableModel.clear();
		for(Instance i:getModel().getInstanceVector())
			{
			if(i.getRDFType().equals(this.currentRDFType))
				{
				this.intancesTableModel.addElement(i);
				}
			}
		
		this.intancesTableModel.sort(new Comparator<Instance>()
			{
			@Override
			public int compare(Instance o1, Instance o2) {
				return SciFOAF.ISTRING_COMPARATOR.compare(o1.getName(), o2.getName());
				}
			});
		}
	
	private RDFModel getModel()
		{
		return this.model;
		}
	
	private void doMenuNewInstance(Component owner,URI rdfType)
		{
		SimpleDialog d= new SimpleDialog(this,"Enter a URI for this new Instance")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public String getErrorMessage()
				{
				String s= SwingUtils.findComponentByName(this.getContentPane(), "instance-uri", JTextField.class).getText();
				if(getModel().findInstanceByURI(URI.create(s))!=null)
					{
					return "URI "+s+" already exists";
					}
				return super.getErrorMessage();
				}
			};
		JPanel pane= new JPanel(new InputLayout());
		pane.add(new JLabel("URI",JLabel.RIGHT));
		JTextField f= new JTextField("",20);
		f.setName("instance-uri");
		pane.add(f);
		d.getContentPane().add(pane);
		d.getOKAction().mustBeURI(f);
		d.getOKAction().mustNotEmpty(f,true);
		f.setText(String.valueOf(getModel().createAnonymousURI().toString()));
		if(d.showDialog()!=InstanceEditor.OK_OPTION) return;
		InstanceEditor ed=getInstanceEditor(owner,rdfType);
		
		if(ed!=null)
			{
			ed.setInstanceURI(URI.create(f.getText()));
			if(ed.showDialog()==InstanceEditor.OK_OPTION)
				{
				ed.updateModel();
				}
			}
		updateInstancesTable();
		}
	
	private void doMenuEditInstance(Component owner,Instance instance)
		{
		InstanceEditor ed=getInstanceEditor(owner,instance.getRDFType());
		
		if(ed!=null)
			{
			ed.setInstance(instance);
			if(ed.showDialog()==InstanceEditor.OK_OPTION)
				{
				ed.updateModel();
				}
			}
		updateInstancesTable();
		}
	
	private void doMenuQuit()
		{
		this.setVisible(true);
		this.dispose();
		}
	
	private InstanceEditor getInstanceEditor(Component owner,URI rdfType)
		{
		InstanceEditor ed=null;
		if(rdfType.equals(URI.create(FOAF.NS+"Person")))
			{
			ed= new PersonEditor(owner);
			}
		return ed;
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Debug.setDebugging(true);
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
	    	int optind=0;
	    	
	    	while(optind<args.length)
			        {
			        if(args[optind].equals("-h"))
			           {
			        	System.err.println(Compilation.getLabel());
			        	System.err.println("\t-h this screen");
			        	System.err.println("\t-d turns debugging on");
						return;
			           	}
			        else if(args[optind].equals("-d"))
			        	{
			        	Debug.setDebugging(true);
			        	}
			       else if(args[optind].equals("--"))
			            {
			            ++optind;
			            break;
			            }
			        else if(args[optind].startsWith("-"))
			            {
			            throw new IllegalArgumentException("Unknown option "+args[optind]);
			            }
			        else
			            {
			            break;    
			            }
			        ++optind;
			        }
	    	File fileIn=null;
	    	
	    	if(optind+1==args.length)
	    		{	
	    		fileIn= new File(args[++optind]);
				}
	    	else if(optind==args.length)
	    		{
	    		Preferences prefs= Preferences.userNodeForPackage(SciFOAF.class);
				String f = prefs.get("save.as.file",null);
	    		
	    		JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
	    		if(f!=null) chooser.setSelectedFile(new File(f));
	    		chooser.setFileFilter(new FileFilter()
	    			{
	    			@Override
	    			public boolean accept(File f)
	    				{
	    				String name=f.getName().toLowerCase();
	    				return f.isDirectory() || name.endsWith("rdf") || name.endsWith("rdf.gz");
	    				}
	    			@Override
	    			public String getDescription() {
	    				return "*.rdf";
	    				}
	    			});
	    		if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
		    		{
		    		fileIn= chooser.getSelectedFile();
		    		prefs.put("save.as.file",f.toString());
		    		PreferredDirectory.setPreferredDirectory(fileIn);
		    		prefs.sync();
		    		}
	    		}
	    	else
	    		{	
	    		throw new IllegalArgumentException("Bad number of argments.");
				}
	    	
	    	
	    		
			SwingUtilities.invokeAndWait(new RunnableObject<File>(fileIn)
				{
				@Override
				public void run()
					{
					RDFModel model= new RDFModel();
					if(getObject()!=null)
						{
						try {
							model.read(getObject());
						} catch (Exception e) {
							ThrowablePane.show(null, e);
							return;
							}
						}
					SciFOAF win= new SciFOAF(getObject(),model);
					win.setVisible(true);
					}
				});
				
			
			
			//LoginDialog ed= new LoginDialog(null);
			//ed.showDialog();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

}
