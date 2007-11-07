/**
 * 
 */
package org.lindenb.tool.scifoaf;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
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
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
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
import org.lindenb.util.Observed;
import org.lindenb.util.ObserverObject;
import org.lindenb.util.Pair;
import org.lindenb.util.TimeUtils;
import org.lindenb.util.XObject;


/**
 * @author pierre
 *
 */
public class SciFOAF extends JFrame
	{
	private static final long serialVersionUID = 1L;
	static private final IStringComparator ISTRING_COMPARATOR= new IStringComparator();
	static private Comparator<Instance> INSTANCE_COMPARATOR=new Comparator<Instance>()
		{
		@Override
		public int compare(Instance o1, Instance o2)
			{
			return o1.compareTo(o2);
			}
		};
	
	
	private enum PREDICATE_TYPE
		{
		DATA,INSTANCE,LINK
		}
	
	
	private abstract static class Predicate<X> extends XObject
		{
		private URI uri;
		private X value;
		protected Predicate(URI uri,X value)
			{
			this.uri=uri;
			this.value=value;
			}
		public abstract PREDICATE_TYPE getType();
		public URI getPredicate() { return this.uri; }
		public X getValue() { return this.value;}
		@Override
		public int hashCode() {
			return getPredicate().hashCode()+getValue().hashCode();
			}

		public abstract void write(XMLStreamWriter w,Instance owner,int depth) throws XMLStreamException;
		}
		
	private static class LinkPredicate extends Predicate<Instance>
		{
		LinkPredicate(URI uri,Instance value)
			{
			super(uri,value);
			}
		
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.LINK;
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
		
		@Override
		public void write(XMLStreamWriter w,Instance owner,int depth) throws XMLStreamException
			{
			Pair<String,String> p= owner.split(this.getPredicate());
			w.writeComment("[LINK END]\n");
			w.writeCharacters(" ");
			w.writeEmptyElement(owner.getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeAttribute("rdf",RDF.NS,"resource",this.getValue().getID().toString());
			//w.writeEndElement();
			w.writeCharacters("\n");
			w.writeComment("[LINK END]\n");
			}
		
		}
	

	
	private static class DataPredicate extends Predicate<String>
		{
		DataPredicate(URI uri,String value)
			{
			super(uri,value);
			}

		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof DataPredicate)) return false;
			DataPredicate cp= DataPredicate.class.cast(obj);
			return getPredicate().equals(cp.getPredicate()) &&
					SciFOAF.ISTRING_COMPARATOR.compare(getValue(), cp.getValue())==0;
			}
		
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.DATA;
			}
		
		@Override
		public String toString() {
			return "<"+getPredicate()+"> \""+getValue()+"\"";
			}
		
		@Override
		public void write(XMLStreamWriter w,Instance owner,int depth) throws XMLStreamException
			{
			Pair<String,String> p= owner.split(this.getPredicate());
			w.writeCharacters(" ");
			w.writeStartElement(owner.getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeCharacters(this.getValue());
			w.writeEndElement();
			w.writeCharacters("\n");
			}
		}
	
	private static class ObjectPredicate extends Predicate<Instance>
		{
		ObjectPredicate(URI uri,Instance value)
			{
			super(uri,value);
			}
		
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.INSTANCE;
			}
		
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
		
		@Override
		public void write(XMLStreamWriter w,Instance owner,int depth) throws XMLStreamException
			{
			Pair<String,String> p= owner.split(this.getPredicate());
			w.writeCharacters(" ");
			w.writeStartElement(owner.getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeCharacters("\n");
			getValue().write(w, depth+1);
			w.writeEndElement();
			w.writeCharacters("\n");
			}
		}
	
	private static abstract class Instance extends XObject
		implements Comparable<Instance>
		{
		private static final long serialVersionUID = 1L;
		private URI uri=null;
		private URI rdfType;
		private Vector<Predicate<?>> _predicates = new Vector<Predicate<?>>(1,1);
		private boolean anonymous;
		Instance(URI rdfType,boolean anonymous)
			{
			this.rdfType=rdfType;
			this.anonymous=anonymous;
			}
		
		
		public boolean isAnonymous() {
			return anonymous;
			}
		public URI getRDFType() { return this.rdfType;}
		public URI getID()
			{
			return this.uri;
			}
		public void setID(URI uri)
			{
			this.uri=uri;
			}
		
		public Vector<Predicate<?>> getPredicates()
			{
			return this._predicates;
			}
		
		@Override
		public int compareTo(Instance o) {
			int i= SciFOAF.ISTRING_COMPARATOR.compare(this.getName(), o.getName());
			if(i!=0) return i;
			return this.getID().compareTo(o.getID());
			}
		
		//TODO ugly change this
		private <C extends Predicate<?>> Iterator<C> listPredicatesOfClass(Class<C> clazz)
			{
			Vector<C> v= new Vector<C>();
			for(Predicate<?> p: getPredicates())
				{
				if(clazz.isInstance(p)) v.addElement(clazz.cast(p));
				}
			return v.iterator();
			}
		
		public Iterator<DataPredicate> listDataPredicates()
			{
			return listPredicatesOfClass(DataPredicate.class);
			}
		
		public Iterator<ObjectPredicate> listObjectPredicates()
			{
			return listPredicatesOfClass(ObjectPredicate.class);
			}
		
		public Iterator<LinkPredicate> listLinkPredicates()
			{
			return listPredicatesOfClass(LinkPredicate.class);
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
			for(Iterator<DataPredicate> iter=this.listDataPredicates();iter.hasNext();)
				{
				DataPredicate p= iter.next();
				if(p.getPredicate().equals(predicate)) return p.getValue();
				}
			return null;
			}
		
		public Collection<DataPredicate> getDataProperties(URI predicate)
			{
			Vector<DataPredicate> v= new Vector<DataPredicate>();
			for(Iterator<DataPredicate> iter=this.listDataPredicates();iter.hasNext();)
				{
				DataPredicate p= iter.next();
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
		public String toString()
			{
			return getName()+" ("+getModel().shortForm(getRDFType().toString())+")";
			}
		
		private Pair<String,String>  split(URI uri)
			{
			String s= uri.toString();
			int n= s.lastIndexOf('#');
			if(n==-1) n= s.lastIndexOf('/');
			return new Pair<String, String>(s.substring(0,n+1),s.substring(n+1));
			}
		
		public void write(XMLStreamWriter w,int depth) throws XMLStreamException
			{
			String space=" ";
			for(int i=0;i<depth;++i) space+=" ";
			Pair<String,String> p= split(getRDFType());
			w.writeStartElement(getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			if(depth==0 || isAnonymous()) w.writeAttribute(RDF.NS, "ID", getID().toString());
			w.writeCharacters("\n");
			
			for(Predicate<?> pred:getPredicates())
				{
				pred.write(w,this,depth);
				}
			

			w.writeEndElement();
			w.writeCharacters("\n");
			}
		
		
		public abstract RDFModel getModel();
		}
	

	
	private static class LinkWrapper
		{
		URI from;
		URI predicate;
		URI to;
		public LinkWrapper(URI from,URI predicate,URI to)
			{
			this.from=from; this.to=to;
			this.predicate=predicate;
			}
		
		
		}
	
	
	/**
	 * 
	 * RDFModel
	 *
	 */
	private static class RDFModel
		extends PrefixMapping
		{
		private Vector<Instance> instances= new Vector<Instance>(1000,100);
		
		
		public RDFModel()
			{
			super(true);
			}
		
		public URI reverse(URI uri)
			{
			return uri;
			}
		
		
		public void saveTo(File file) throws IOException
			{
			File parent=file.getParentFile();
			File tmpFile= File.createTempFile("tmp", ".rdf", parent);
			
			FileWriter fout = new FileWriter(tmpFile);
			
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
				w.writeComment(Compilation.getLabel()+" "+TimeUtils.toYYYYMMDD());
				w.writeCharacters("\n");
				
				
				for(Instance instance: getInstanceVector())
					{
					instance.write(w,0);
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
			file.delete();
			File tmpFile2= File.createTempFile("tmp", ".rdf", parent);
			file.renameTo(tmpFile2);
			tmpFile.renameTo(file);
			tmpFile2.delete();
			}
		
		public void read(File file) throws XMLStreamException
			{
			XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            boolean foundRoot=false;
            Vector<LinkWrapper> links= new Vector<LinkWrapper>();
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
						else
							{
							Instance i= parseInstance(parser, event.asStartElement(),links,0);
							getInstanceVector().addElement(i);
							}
						}
					}
	            for(LinkWrapper link:links)
	            	{
	            	Instance from = findInstanceByURI(link.from);
	            	if(from==null)		
	            		{
	            		Debug.debug("Cannot find instance URI="+link.from);
	            		}
	            	Instance to = findInstanceByURI(link.to);
	            	if(to==null)		
	            		{
	            		Debug.debug("Cannot find instance URI="+link.to);
	            		}
	            	LinkPredicate p= new LinkPredicate(
	            		link.predicate,
	            		to
	            		);
	            	
	            	if(!from.getPredicates().contains(p))
	            		{
	            		from.getPredicates().addElement(p);
	            		}
	            	
	            	
	            	if(reverse(link.predicate)!=null)
		            	{
		            	p= new LinkPredicate(
		    	            		reverse(link.predicate),
		    	            		from
		    	            		);
		            	if(!to.getPredicates().contains(p))
		            		{
		            		to.getPredicates().addElement(p);
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
		
		public Instance newInstance(URI rdfType,boolean anonymous)
			{
			return new Instance(rdfType,anonymous)
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
		
		private Instance parseInstance(XMLEventReader parser,StartElement element,Vector<LinkWrapper> links,int depth) throws XMLStreamException
			{
			try {
				URI rdfType= new URI(element.getName().getNamespaceURI()+element.getName().getLocalPart());
				Instance instance= newInstance(rdfType,depth!=0);
				Attribute att=element.getAttributeByName(new QName(RDF.NS,"about"));
				if(att==null) att=element.getAttributeByName(new QName(RDF.NS,"ID"));
				if(att==null && depth==0) throw new XMLStreamException("Cannot find rdf:ID/about in "+element.getName());
				
				URI id=null;
				if(att!=null)
					{
					id=new URI(att.getValue());
					}
				else
					{
					id= createAnonymousURI();
					}
				
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
						Attribute resource =start2.getAttributeByName(new QName(RDF.NS,"resource"));
						Debug.debug(resource);
						if(resource!=null)
							{
							LinkWrapper lw= new LinkWrapper(id,predicateURI,new URI(resource.getValue()));
							links.addElement(lw);
							if(!parser.hasNext()) throw new XMLStreamException("Expected an event");
							event= parser.nextEvent();
							if(!event.isEndElement()) throw new XMLStreamException("Expected End Element");
							
							}
						else
							{
							StringBuilder textContent=new StringBuilder();
							while(parser.hasNext())
								{
								event = parser.nextEvent();
								if(event.isEndElement())
									{
									if(textContent!=null)
										{
										DataPredicate dataPred= new DataPredicate(
												predicateURI,textContent.toString()
												);
										
										if(!instance.getPredicates().contains(dataPred))
											{
											instance.getPredicates().addElement(dataPred);
											}
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
									ObjectPredicate op= new ObjectPredicate(predicateURI,parseInstance(parser, event.asStartElement(),links,depth+1));
									if(!instance.getPredicates().contains(op))
										{
										instance.getPredicates().addElement(op);
										}
									}
								else if(event.isCharacters() && textContent!=null)
									{
									textContent.append(event.asCharacters().getData());
									}
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
	

	static private abstract class PropertyOption
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
		public abstract PREDICATE_TYPE getType();
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
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.DATA;
			}
		
		public boolean isValid(String s)
			{
			if(!this.pattern.matcher(s).matches())
				{
				return false;
				}
			
			if(this.clazz!=String.class)
				{
				try {
					clazz.getConstructor(String.class).newInstance(s);
					}
				catch (Exception e) {
					return false;
					}
				}
			return true;
			}
		
		}
	static private abstract class ObjectPropOption extends PropertyOption
		{
		URI rdfEditedType=null;
		ObjectPropOption(String ns,String prefix,String local)
			{
			super(ns,prefix,local);
			}
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.INSTANCE;
			}
		
		public abstract void editInstance(Instance i);
		public abstract Instance createAndEditInstance(RDFModel rdfModel);
		
		}
	
	static private class LinkOption extends PropertyOption
		{
		Set<URI> range;
		LinkOption(String ns,String prefix,String local,Set<URI> range)
			{
			super(ns,prefix,local);
			this.range=range;
			}
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.LINK;
			}
		}
	
	/**
	 * 
	 * @author lindenb
	 *
	 */
	private class InstanceEditor extends SimpleDialog
		{
		private static final long serialVersionUID = 1L;
		protected Vector<PropertyOption> dataPropOptions= new Vector<PropertyOption>();
		protected JLabel iconLabel;
		protected JLabel titleLabel;
		protected JLabel idLabel;
		protected URI rdfType;
		protected JTabbedPane tabbedPane;
		protected JTable predicateTable;
		protected JTextField dataPredicateValueField;
		protected GenericTableModel<Predicate<?>> predicateModel;
		protected AbstractAction actionAddPredicate;
		protected JComboBox predicateCombo;
		protected JComboBox linkInstanceCombo;
		protected URI instanceID=null;
		private boolean readOnly=false;
		private JPanel cardPane;
		
		/**
		 * InstanceEditor
		 * @param owner
		 * @param rdfType
		 * @param readOnly
		 */
		InstanceEditor(java.awt.Component owner,URI rdfType,boolean readOnly)
			{
			super(owner,rdfType.toString());
			this.rdfType=rdfType;
			this.readOnly=readOnly;
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
			pane2.add(new JScrollPane(predicateTable=buildPredicateTable(this.predicateModel=buildPredicateModel())),BorderLayout.CENTER);
			this.predicateTable.addMouseListener(new MouseAdapter()
				{
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()<2) return;
					int i=InstanceEditor.this.predicateTable.rowAtPoint(e.getPoint());
					if(i==-1) return;
					i= InstanceEditor.this.predicateTable.convertRowIndexToModel(i);
					if(i==-1) return;
					
					//Predicate<?> t= InstanceEditor.this.predicateModel.elementAt(i);
					//TODO
					}
				});
			
			
			
			
			
			top= new JPanel(new FlowLayout(FlowLayout.LEADING));
			top.setBorder(new LineBorder(Color.GRAY,1));
			pane2.add(top,BorderLayout.NORTH);
			top.add(new JLabel("Add Property:",JLabel.RIGHT));
			this.predicateCombo=new JComboBox(new DefaultComboBoxModel(this.dataPropOptions));
			this.predicateCombo.setEnabled(!isReadOnly());
			top.add(predicateCombo);
			
			
			this.cardPane=new JPanel(new CardLayout(1,1));
			top.add(cardPane);
			JPanel pane3= new JPanel(new BorderLayout());
			this.cardPane.add(pane3,"EMPTY");
			pane3= new JPanel(new BorderLayout());
			this.cardPane.add(pane3,PREDICATE_TYPE.DATA.toString());
			pane3.add(dataPredicateValueField=new JTextField(30));
			this.dataPredicateValueField.setEnabled(!isReadOnly());
			
			pane3= new JPanel(new BorderLayout());
			this.cardPane.add(pane3,PREDICATE_TYPE.INSTANCE.toString());
			
			pane3= new JPanel(new BorderLayout());
			this.cardPane.add(pane3,PREDICATE_TYPE.LINK.toString());
			pane3.add(this.linkInstanceCombo= new JComboBox(new DefaultComboBoxModel()));
			this.linkInstanceCombo.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					actionAddPredicate.setEnabled(isPredicateActionEnabled());
					}
				});
			
			this.actionAddPredicate=new AbstractAction("Add")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					
					doMenuAddPredicate();	
					}
				};
			top.add(new JButton(actionAddPredicate));
			this.dataPredicateValueField.addActionListener(actionAddPredicate);
			actionAddPredicate.setEnabled(false);
			dataPredicateValueField.getDocument().addDocumentListener(new DocumentAdapter()
				{
				@Override
				public void documentChanged(DocumentEvent e) {
					actionAddPredicate.setEnabled(isPredicateActionEnabled());
					}	
				});
			predicateCombo.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent e)
					{
					PropertyOption opt= PropertyOption.class.cast(predicateCombo.getSelectedItem());
					CardLayout layout= CardLayout.class.cast(cardPane.getLayout());
					
					if(opt!=null && !isReadOnly())
						{
						switch(opt.getType())
							{
							case DATA: 
								{
								layout.show(cardPane, opt.getType().toString());
								dataPredicateValueField.setEnabled(true);
								break;
								}
							case INSTANCE:
								{
								layout.show(cardPane,  opt.getType().toString());
								break;
								}
							case LINK:
								{
								LinkOption lkopt= LinkOption.class.cast(opt);
								DefaultComboBoxModel m= DefaultComboBoxModel.class.cast(linkInstanceCombo.getModel());
								m.removeAllElements();
								Vector<Instance> v= new Vector<Instance>();
								for(Instance i:getModel().instances)
									{
									if(lkopt.range.contains(i.getRDFType()))
										{
										v.addElement(i);
										}
									}
								Collections.sort(v,INSTANCE_COMPARATOR);
								for(Instance i: v) m.addElement(i);
								linkInstanceCombo.setSelectedIndex(-1);
								layout.show(cardPane,  opt.getType().toString());
								break;
								}
							
							default:
								{
								layout.show(cardPane, "EMPTY");
								break;
								}
							}
						}
					else
						{
						layout.show(cardPane, "EMPTY");
						}
					
					actionAddPredicate.setEnabled(isPredicateActionEnabled());
					}
				});
			this.predicateCombo.setSelectedIndex(-1);
			
			ConstrainedAction<InstanceEditor> action= new ConstrainedAction<InstanceEditor>(this,"Remove")
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
					{
					if(isReadOnly()) return;
					int i= predicateTable.getSelectedRow();
					if(i==-1) return;
					i= predicateTable.convertRowIndexToModel(i);
					if(i!=-1) predicateModel.removeElementAt(i);
					}
				};
			action.setEnabled(!isReadOnly());
			top.add(Box.createHorizontalGlue());
			top.add(new JButton(action));
			
			action.mustHaveOneRowSelected(predicateTable);
			
			this.tabbedPane.addTab("Data Properties", pane2);
			
			pane2= new JPanel(new BorderLayout());
			this.tabbedPane.addTab("Object Properties", pane2);
			
			getContentPane().add(main);
			}
		
		protected void doMenuAddPredicate()
			{
			PropertyOption opt= PropertyOption.class.cast(predicateCombo.getSelectedItem());
			if(opt==null) return;
			switch(opt.getType())
				{
				case DATA:
					{
					DataPredicate p=new DataPredicate(opt.uri,dataPredicateValueField.getText().trim());
					if(predicateModel.contains(p)) return;
					predicateModel.addElement(p);
					dataPredicateValueField.setText("");
					break;
					}
				case INSTANCE:
					{
					InstanceEditor ed= getInstanceEditor(InstanceEditor.this,
							ObjectPropOption.class.cast(opt).rdfEditedType,
							isReadOnly()
							);
					ed.setInstanceURI(getModel().createAnonymousURI());
					if(ed.showDialog()!=OK_OPTION) return;
					Instance instance= getModel().newInstance(ed.instanceID,true);
					ObjectPredicate p= new ObjectPredicate(opt.uri,instance);
					if(predicateModel.contains(p)) return;
					predicateModel.addElement(p);
					break;
					}
				case LINK:
					{
					Instance i= Instance.class.cast(linkInstanceCombo.getSelectedItem());
					if(i==null) return;
					LinkPredicate p= new LinkPredicate(opt.uri,i);
					if(!this.predicateModel.contains(p))
						{
						predicateModel.addElement(p);
						}
					linkInstanceCombo.setSelectedIndex(-1);
					break;
					}
				}
			
			}
		
		protected void doMenuEditPredicate(Predicate<?> predicate)
			{
			
			}
		
		
		private int countPredicate(URI predicateURI)
			{
			int n=0;
			for(Iterator<Predicate<?>> iter=this.predicateModel.listElements();
				iter.hasNext();
				)
				{
				if(iter.next().getPredicate().equals(predicateURI))
					{
					++n;
					}
				}
			return n;
			}
		
		protected boolean isPredicateActionEnabled()
			{
			if(isReadOnly()) return false;
			PropertyOption option1=PropertyOption.class.cast(predicateCombo.getSelectedItem());
			if(option1==null)
				{
				return false;
				}
		
			if(countPredicate(option1.uri)>= option1.maxOcc)
				{
				return false;
				}
			
			switch(option1.getType())
				{
				case DATA:
					{
					String s=this.dataPredicateValueField.getText().trim();
					
					DataPropOption option=DataPropOption.class.cast(option1);
					DataPredicate p= new DataPredicate(option.uri,s.trim());
					if(this.predicateModel.contains(p)) return false;
					if(!option.isValid(s)) return false;
					return true;
					}
				case INSTANCE:
					{
					Debug.debug();
					return true;
					}
				case LINK:
					{
					Debug.debug();
					Instance i= Instance.class.cast(linkInstanceCombo.getSelectedItem());
					Debug.debug(i);
					if(i==null) return false;
					Debug.debug();
					LinkPredicate p= new LinkPredicate(option1.uri,i);
					Debug.debug();
					if(this.predicateModel.contains(p)) return false;
					Debug.debug();
					return true;
					}
				}
				
			
			

			return false;
			}
		
		public boolean isReadOnly()
			{
			return this.readOnly;
			}
		
		public void setInstance(Instance instance)
			{
			setInstanceURI(instance.getID());
			this.idLabel.setText(instance.getID().toString());
			this.titleLabel.setText(instance.getName());
			this.predicateModel.addAll(instance.getPredicates());
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
		
		
		protected JTable buildPredicateTable( GenericTableModel<Predicate<?>> model)
			{
			JTable table= new JTable(model);
			table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer()
				{
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column)
					{
					value= (value==null?null:getModel().shortForm(value.toString()));
					Component c= super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
							row, column);
					return c;
					}
				});
			DefaultTableCellRenderer tcr= new DefaultTableCellRenderer()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column)
					{
					Component c= super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
							row, column);
					
					if(value instanceof String)
						{
						this.setForeground(Color.BLACK);
						}
					else if(value instanceof Instance)
						{
						Instance i= Instance.class.cast(value);
						this.setForeground(i.isAnonymous()?
							Color.BLUE:
							Color.MAGENTA
							);
						}
					else
						{
						this.setForeground(Color.GREEN);
						}
						
					return c;
					}
				};
			
			table.getColumnModel().getColumn(1).setCellRenderer(tcr);
			
			table.setFont(new Font("Dialog",Font.PLAIN,18));
			table.setShowVerticalLines(false);
			table.setRowHeight(22);
			return table;
			}
		
		protected GenericTableModel<Predicate<?>> buildPredicateModel()
			{
			return new GenericTableModel<Predicate<?>>()
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
				public Object getValueOf(Predicate<?> object,
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
				instance= getModel().newInstance(this.rdfType,false);
				instance.setID(this.instanceID);
				getModel().getInstanceVector().addElement(instance);
				}
			instance.getPredicates().clear();
			for(Iterator<Predicate<?>> iter=this.predicateModel.listElements();
				iter.hasNext();
				)
				{
				instance.getPredicates().addElement( iter.next() );
				}
			return instance;
			}
		}
	
	/**
	 * 
	 * AgentEditor
	 *
	 */
	private class AgentEditor extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		protected AgentEditor(Component owner,String localName,boolean readOnly)
			{
			super(owner,URI.create(FOAF.NS+localName),readOnly);
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

		PersonEditor(Component owner,boolean readOnly)
			{
			super(owner,"Person",readOnly);
			}
		
		@Override
		void buildOptions(Vector<PropertyOption> options) {
			PropertyOption opt= new DataPropOption(FOAF.NS,"foaf","firstName");
			options.add(opt);
			HashSet<URI> range = new HashSet<URI>();
			range.add(URI.create(FOAF.NS+"Person"));
			LinkOption lkopt= new LinkOption(FOAF.NS,"foaf","knows",range);
			options.add(lkopt);
			super.buildOptions(options);
			}
		}
	
	
	private RDFModel model;
	private URI currentRDFType=null;
	private JTable instanceTable;
	private Vector<JButton> setRDFTypeButtons = new Vector<JButton>(10,1);
	private GenericTableModel<Instance> intancesTableModel;
	private Observed<File> saveAsFile=new Observed<File>();
	private SciFOAF(File saveAsFile,RDFModel model)
		{
		super("SciFOAF");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.model=model;
		
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
		AbstractAction saveAction;
		menu.add(new JMenuItem(saveAction=new AbstractAction("Save")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuSave(SciFOAF.this.saveAsFile.getValue());
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
		
		
		
		this.instanceTable= new JTable(this.intancesTableModel);
		this.instanceTable.setFont(new Font("Dialog",Font.PLAIN,18));
		this.instanceTable.setShowVerticalLines(false);
		this.instanceTable.setRowHeight(22);
		
		pane1.add(new JScrollPane(instanceTable));
		this.instanceTable.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()<2) return;
				int i=SciFOAF.this.instanceTable.rowAtPoint(e.getPoint());
				if(i==-1) return;
				i= SciFOAF.this.instanceTable.convertRowIndexToModel(i);
				if(i==-1) return;
				Instance t= SciFOAF.this.intancesTableModel.elementAt(i);
				doMenuEditInstance(SciFOAF.this, t,false);
				}
			});
		
		
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
				doMenuEditInstance(SciFOAF.this,
						intancesTableModel.elementAt(instanceTable.convertRowIndexToModel(instanceTable.getSelectedRow())),
						false
						);
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
		
		
		
		this.saveAsFile.addObserver(new ObserverObject<AbstractAction>(saveAction)
			{
			@Override
			public void update(Observable o, Object arg) {
				getObject().setEnabled(arg!=null);
				}
			});
		this.saveAsFile.setValue(saveAsFile);
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
			this.saveAsFile.setValue(f);
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
				this.saveAsFile.getValue()!=null?
				this.saveAsFile.getValue():
				new File(PreferredDirectory.getPreferredDirectory(),"scifoaf.rdf")
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
		
		this.intancesTableModel.sort(INSTANCE_COMPARATOR);
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
		InstanceEditor ed=getInstanceEditor(owner,rdfType,false);
		
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
	
	private void doMenuEditInstance(Component owner,Instance instance,boolean readOnly)
		{
		InstanceEditor ed=getInstanceEditor(owner,instance.getRDFType(),readOnly);
		
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
	
	private InstanceEditor getInstanceEditor(Component owner,URI rdfType,boolean readOnly)
		{
		InstanceEditor ed=null;
		if(rdfType.equals(URI.create(FOAF.NS+"Person")))
			{
			ed= new PersonEditor(owner,readOnly);
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
