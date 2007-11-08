/**
 * 
 */
package org.lindenb.tool.scifoaf;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.lindenb.sw.vocabulary.Namespace;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sun.misc.JavaLangAccess;

/**
 * An author in a NCBI Pubmed paper
 * @author pierre
 *
 */
class Author
	{
	String Suffix="";
	String LastName="";
	String FirstName="";
	String MiddleName="";
	String Initials="";

	static Author parse(Element root)
		{
		Author author= new Author();
		for(Node n= root.getFirstChild();n!=null;n=n.getNextSibling())
			{
			if(n.getNodeType()!=Node.ELEMENT_NODE) continue;
			String tag= n.getNodeName();
			String content= n.getTextContent().trim();
			if(tag.equals("LastName"))
				{
				author.LastName= content;
				}
			else if(tag.equals("FirstName") || tag.equals("ForeName"))
				{
				author.FirstName= content;
				}
			else if(tag.equals("Initials"))
				{
				author.Initials= content;
				}
			else if(tag.equals("MiddleName"))
				{
				author.MiddleName= content;
				}
			else if(tag.equals("CollectiveName"))
				{
				return null;
				}
			else if(tag.equals("Suffix"))
				{
				author.Suffix= content;
				}
			else
				{
				Debug.debug("ignoring "+tag+"="+content);
				}
			}
		return author;
		}
	String toHTML()
		{
		return "<a>"+LastName+" "+FirstName+"</a>";
		}
	@Override
	public String toString() {
		return ""+LastName+" "+FirstName;
		}
	}

/**
 * A paper from PUBMED
 * @author pierre
 *
 */
class Paper
	{
	Vector<Author> authors= new Vector<Author>(10);
	HashSet<String> meshes= new HashSet<String>();
	String PMID=null;
	String ArticleTitle=null;
	String Volume=null;
	String Issue=null;
	String PubDate=null;
	String MedlinePgn=null;
	String JournalTitle=null;
	String DOI=null;
	
	Paper()
		{
		
		}
	
	public String toHTML()
		{
		StringBuilder b= new StringBuilder("<div>");
		if(ArticleTitle!=null) b.append("<h3>"+ArticleTitle+"</h3>");
		b.append("<h4>");
		for(int i=0;i< authors.size();++i)
			{
			if(i!=0 && i+1!=authors.size()) b.append(",");
			if(i!=0 && i+1==authors.size()) b.append(" and ");
			b.append(authors.elementAt(i).toHTML());
			}
		b.append("</h4>");
		
		
		b.append("<p>");
		if(JournalTitle!=null) b.append("<b>"+JournalTitle+"</b>. ");
		if(PubDate!=null) b.append(" <i>"+PubDate+"</i>. ");
		if(Volume!=null) b.append(" ("+Volume+") ");
		if(Issue!=null) b.append(" "+Issue+", ");
		if(MedlinePgn!=null) b.append(" pp."+MedlinePgn+". ");
		b.append("</p>");
		b.append("<i>PMID.</i>"+PMID+"<br>");
		if(DOI!=null) b.append("<i>DOI.</i>"+DOI+"<br>");
		b.append("</div>");
		return b.toString();
		}
	
	static Paper parse(Element root)
		{
		Paper paper= new Paper();
		parse(paper,root,0);
		return paper;
		}
	
	static private void parse(Paper paper,Element node,int depth)
		{
		if(node==null) return;
		String name=node.getNodeName();
		if(name.equals("PMID")) { paper.PMID= node.getTextContent().trim();}
		else if(name.equals("Volume")) { paper.Volume= node.getTextContent().trim();}
		else if(name.equals("Issue")) { paper.Issue= node.getTextContent().trim();}
		else if(name.equals("MedlinePgn")) { paper.MedlinePgn= node.getTextContent().trim();}
		else if(name.equals("Title")) { paper.JournalTitle= node.getTextContent().trim();}
		else if(node.getParentNode()!=null &&
				node.getParentNode().getNodeName().equals("PubDate"))
			{
			if(paper.PubDate==null) paper.PubDate="";
			if(paper.PubDate.length()>0) paper.PubDate+="-";
			paper.PubDate+=node.getTextContent().trim();
			}
		else if(name.equals("ArticleTitle")) { paper.ArticleTitle= node.getTextContent().trim();}
		else if(name.equals("Author"))
			{
			Author author= Author.parse(node);
			if(author!=null && author.LastName!=null) paper.authors.addElement(author);
			}
		else if(name.equals("QualifierName"))
			{
			paper.meshes.add(node.getTextContent());
			}
		else if(name.equals("ArticleId"))
			{
			String doi= node.getAttribute("IdType");
			if("doi".equals(doi))
				{
				paper.DOI=node.getTextContent().trim();
				}
			}
		
			{
			for(int i=0;i< depth;++i) System.err.print("  ");
			System.err.print(name);
			for(int i=0;i< node.getAttributes().getLength();++i)
				{
				System.err.print(" "+node.getAttributes().item(i).getNodeName()+"="+node.getAttributes().item(i).getNodeValue()+" ");
				}
			if(node.getFirstChild()!=null && node.getFirstChild().getNodeType()==Node.TEXT_NODE)
				{
				System.err.print(" "+node.getFirstChild().getTextContent());
				}
			System.err.println();
			}
		
		
		for(Node n= node.getFirstChild();n!=null;n=n.getNextSibling())
			{
			if(n.getNodeType()!=Node.ELEMENT_NODE) continue;
			parse(paper,Element.class.cast(n),depth+1);
			}
		}
	}

class NCBI extends Namespace
	{
	public static final String NS="http://www.ncbi.nlm.nih.gov/rdf/";
	public static final String IsNCBIAuthor="isNCBIAuthor";
	public static final String lastName="lastName";
	public static final String firstName="firstName";
	public static final String Author="Author";
	;
	}

/**
 * SciFOAF
 * @author pierre
 *
 */
public class SciFOAF extends JFrame
	{
	private static final long serialVersionUID = 1L;
	static private final IStringComparator ISTRING_COMPARATOR= new IStringComparator();
	/** comparator used in Generic Table Model of Instance */
	static private Comparator<Instance> INSTANCE_COMPARATOR=new Comparator<Instance>()
		{
		@Override
		public int compare(Instance o1, Instance o2)
			{
			return o1.compareTo(o2);
			}
		};
	
	/** type of oject stored in a Predicate<X> */
	private enum PREDICATE_TYPE
		{
		/** plain text data */
		DATA,
		/** a instance that just belong to the current instance */
		INSTANCE,
		/** another instance IN the model */
		LINK,
		/** an external URL */
		EXTERNAL_URL
		}
	
	private class PaperEditor extends SimpleDialog
		{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Vector<Pair<JComboBox,Author>> joinAuthor2Instance;
		private Vector<Pair<JCheckBox,String>> meshes;
		private Paper paper;
		
		PaperEditor(Component c,Paper paper)
			{
			super(c,"Pubmed PMID."+paper.PMID);
			Debug.debug();
			this.paper=paper;
			this.joinAuthor2Instance= new Vector<Pair<JComboBox,Author>>(paper.authors.size(),1); 
			this.meshes= new Vector<Pair<JCheckBox,String>>(paper.meshes.size());
			int predWidth= (int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.75);
			JPanel pane= new JPanel(new BorderLayout(5,5));
			getContentPane().add(pane);
			JPanel top = new JPanel(new BorderLayout(5,5));
			pane.add(top,BorderLayout.NORTH);
			JEditorPane editor=new JEditorPane("text/html",
					"<html><body width='"+predWidth+"'>"+paper.toHTML()+"</body></html>");
			editor.setEditable(false);
			top.add(new JScrollPane(editor));
			top.add(new JSeparator(JSeparator.HORIZONTAL),BorderLayout.SOUTH);
			
			JPanel pane2= new JPanel(new GridLayout(1,0,10,10));
			pane.add(pane2);
			
			JPanel pane3= new JPanel(new GridLayout(0,2,3,3));
			pane3.setBorder(new TitledBorder("Authors"));
			pane2.add(new JScrollPane(pane3));
			for(Author author:paper.authors)
				{
				JLabel label= new JLabel(author.toString(),JLabel.RIGHT);
				pane3.add(label);
				JComboBox cbox= createCombo(author);
				pane3.add(cbox);
				}
			
			pane3= new JPanel(new GridLayout(0,3,3,3));
			pane3.setBorder(new TitledBorder("Mesh Terms"));
			pane2.add(new JScrollPane(pane3));
			for(String mesh:paper.meshes)
				{
				JCheckBox cb= new JCheckBox(mesh,false);
				this.meshes.add(new Pair<JCheckBox,String>(cb,mesh));
				pane3.add(cb);
				}
			}
		private JComboBox createCombo(Author author)
			{
			DefaultComboBoxModel m= new DefaultComboBoxModel();
			
			int selectedIndex=-1;
			m.addElement("-- Ignore --");
			m.addElement("Create New foaf:Person");
			m.addElement("As Literal");
			URI person= URI.create(FOAF.NS+"Person");
			URI isNCBIAuthor= URI.create(NCBI.NS+NCBI.IsNCBIAuthor);
			URI lastNameURI= URI.create(NCBI.NS+NCBI.lastName);
			for(Instance i: getModel().getInstanceVector())
				{
				if(!i.getRDFType().equals(person)) continue;
				m.addElement(i);
				
				for(Iterator<ObjectPredicate> iter=i.listObjectPredicates();
					selectedIndex==-1 && iter.hasNext();)
					{
					ObjectPredicate p= iter.next();
					if(!p.getPredicate().equals(isNCBIAuthor)) continue;
					Instance value=p.getValue();
					String name= value.getDataProperty(lastNameURI);
					if(name!=null && author.LastName!=null && ISTRING_COMPARATOR.compare(name, author.LastName)==0)
						{
						selectedIndex=m.getSize()-1;
						}
					}
				}
			
			JComboBox cbox= new JComboBox(m);
			cbox.setName(author.toString());
			cbox.setSelectedIndex(selectedIndex);
			getOKAction().mustBeSelected(cbox);
			joinAuthor2Instance.add(new Pair<JComboBox,Author>(cbox,author));
			return cbox;
			}
		
		
		public void create()
			{
			if(SciFOAF.this.containsPaperPMID(paper.PMID)) return;
			Instance instance= getModel().newInstance(URI.create(FOAF.NS+"Document"),false);
			instance.setID(URI.create("http://view.ncbi.nlm.nih.gov/pubmed/"+paper.PMID));
			getModel().getInstanceVector().add(instance);
			instance.getPredicates().add(new DataPredicate(URI.create(NCBI.NS+"pmid"),paper.PMID));
			if(paper.ArticleTitle!=null) instance.getPredicates().add(new DataPredicate(URI.create(DC.NS+"title"),paper.ArticleTitle));
			if(paper.Issue!=null)  instance.getPredicates().add(new DataPredicate(URI.create(NCBI.NS+"issue"),paper.Issue));
			if(paper.Volume!=null)  instance.getPredicates().add(new DataPredicate(URI.create(NCBI.NS+"volume"),paper.Volume));
			if(paper.MedlinePgn!=null)  instance.getPredicates().add(new DataPredicate(URI.create(NCBI.NS+"medlinePgn"),paper.MedlinePgn));
			if(paper.PubDate!=null)  instance.getPredicates().add(new DataPredicate(URI.create(DC.NS+"date"),paper.PubDate));
			if(paper.JournalTitle!=null)  instance.getPredicates().add(new DataPredicate(URI.create(NCBI.NS+"journalTitle"),paper.JournalTitle));
			if(paper.DOI!=null)
				{
				try {
					instance.getPredicates().add(new ExternalURLPredicate(URI.create(NCBI.NS+"doi"),new URL("http://dx.doi.org/"+paper.DOI)));
				} catch (MalformedURLException e) {
					Debug.debug(e);
				}
				}
			
			for(Pair<JCheckBox,String> p:this.meshes)
				{
				if(!p.first().isSelected()) continue;
				instance.getPredicates().add(new DataPredicate(URI.create(DC.NS+"title"),p.second()));
				}
			
			for(Pair<JComboBox,Author> p:this.joinAuthor2Instance)
				{
				DefaultComboBoxModel m= DefaultComboBoxModel.class.cast(p.first().getModel());
				int index= p.first().getSelectedIndex();
				if(index==-1) continue;
				else if(index==0) continue;//ignore
				else if(index==1)//new author
					{
					Instance author= createAuthor(p.second());
					if(author!=null)
						{
						linkAuthorAndPaper(instance,author);
						}
					}
				else if(index==2)//literal
					{
					instance.getPredicates().add(new DataPredicate(URI.create(FOAF.NS+"maker"),p.second().toString()));
					}
				else// 
					{
					Instance author=Instance.class.cast(m.getElementAt(index));
					if(author!=null)
						{
						linkAuthorAndPaper(instance,author);
						}
					}
				}
			
			
			}
		
		private void linkAuthorAndPaper(Instance paper,Instance author)
			{
			LinkPredicate link= new LinkPredicate(URI.create(FOAF.NS+"maker"),author);
			if(!paper.getPredicates().contains(link))
				{
				paper.getPredicates().addElement(link);
				}
			link= new LinkPredicate(URI.create(FOAF.NS+"made"),paper);
			if(!author.getPredicates().contains(link))
				{
				author.getPredicates().addElement(link);
				}
			}
		
		private Instance createAuthor(Author o)
			{
			if(o.FirstName==null) o.FirstName="";
			Instance author= getModel().newInstance(URI.create(FOAF.NS+"Person"), false);
			URI uri=URI.create((o.FirstName+o.LastName).replaceAll("[^a-zA-Z]", ""));
			if(getModel().findInstanceByURI(uri)!=null)
				{
				uri=getModel().createAnonymousURI();
				}
			author.setID(uri);
			getModel().getInstanceVector().addElement(author);
			_addpredicate(author,FOAF.NS,"name",(o.FirstName+" "+o.LastName).trim());
			_addpredicate(author,FOAF.NS,"firstName",o.FirstName);
			_addpredicate(author,FOAF.NS,"family_name",o.LastName);
			
			Instance ncbiName= createNCBIAuthor(o);
			if(ncbiName!=null)
				{
				author.getPredicates().add(new ObjectPredicate(URI.create(NCBI.NS+NCBI.IsNCBIAuthor),ncbiName));
				}
			return author;
			}
		
		private Instance createNCBIAuthor(Author o)
			{
			Instance author= getModel().newInstance(URI.create(NCBI.NS+"Author"), true);
			_addpredicate(author,NCBI.NS,NCBI.firstName,o.FirstName);
			_addpredicate(author,NCBI.NS,NCBI.lastName,o.LastName);
			_addpredicate(author,NCBI.NS,"initials",o.Initials);
			_addpredicate(author,NCBI.NS,"middleName",o.MiddleName);
			_addpredicate(author,NCBI.NS,"suffix",o.Suffix);
			return author;
			}
		
		private void _addpredicate(Instance instance,String ns,String local,String value)
			{
			if(value==null || value.trim().length()==0) return;
			instance.getPredicates().add(new DataPredicate(URI.create(ns+local),value.trim()));
			}
		}
	
	/**
	 * Predicate<X>
	 * @author pierre
	 *
	 * @param <X> type of value in a RDF statement
	 */
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
		
	/**
	 * 
	 * @author pierre
	 *
	 */
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
			w.writeCharacters(" ");
			w.writeEmptyElement(owner.getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeAttribute("rdf",RDF.NS,"resource","#"+this.getValue().getID().toString());
			//w.writeEndElement();
			w.writeCharacters("\n");
			}
		
		}
	
	private static class ExternalURLPredicate extends Predicate<URL>
		{
		ExternalURLPredicate(URI uri,URL value)
			{
			super(uri,value);
			}
		
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.EXTERNAL_URL;
			}
		
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null || !(obj instanceof ExternalURLPredicate)) return false;
			ExternalURLPredicate cp= ExternalURLPredicate.class.cast(obj);
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
			w.writeEmptyElement(owner.getModel().getNsURIPrefix(p.first()),p.second(),p.first());
			w.writeAttribute("rdf",RDF.NS,"resource",this.getValue().toString());
			//w.writeEndElement();
			w.writeCharacters("\n");
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
			if(this.anonymous)
				{
				uri= getModel().createAnonymousURI();
				}
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
		
		public boolean hasPredicate(URI uri)
			{
			for(Predicate<?> p: getPredicates())
				{
				if(p.getPredicate().equals(uri)) return true;
				}
			return false;
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
			setNsPrefix("ncbi", NCBI.NS);
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
	            		continue;
	            		}
	            	Instance to = findInstanceByURI(link.to);
	            	if(to==null)		
	            		{
	            		URL url= null;
	            		try {
	            			url=link.to.toURL();
	            			ExternalURLPredicate p= new ExternalURLPredicate(link.predicate,url);
	            			if(!from.getPredicates().contains(p))
		            			{
		            			from.getPredicates().addElement(p);
								}
	            			}	
	            		catch (Exception e) {
							Debug.debug("Cannot find instance URI="+link.to);
							}
	            		continue;
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
							String to= resource.getValue();
							if(to.startsWith("#")) to=to.substring(1);
							LinkWrapper lw= new LinkWrapper(id,predicateURI,new URI(to));
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
	
	static private class ExternalURLPropOption extends PropertyOption
		{
		ExternalURLPropOption(String ns,String prefix,String local)
			{
			super(ns,prefix,local);
			}
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.EXTERNAL_URL;
			}
		
		public URL validateURL(String s)
			{
			URL url=null;
			try {
				url= new URL(s);
			} catch (Exception e) {
				return null;
				}
			return url;
			}
		}

	
	static private class ObjectPropOption extends PropertyOption
		{
		URI rdfEditedType=null;
		ObjectPropOption(String ns,String prefix,String local,URI rdfEditedType)
			{
			super(ns,prefix,local);
			this.rdfEditedType=rdfEditedType;
			}
		@Override
		public PREDICATE_TYPE getType() {
			return PREDICATE_TYPE.INSTANCE;
			}
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
	 * AbsractInstanceEditor
	 *
	 */
	private abstract class AbsractInstanceEditor extends SimpleDialog
		{
		private URI _rdfType;
		private boolean readOnly=false;
		protected AbsractInstanceEditor(java.awt.Component owner,URI rdfType,boolean readOnly)
			{
			super(owner,rdfType.toString());
			this._rdfType=rdfType;
			this.readOnly=readOnly;	
			}
		
		public URI getRDFType()
			{
			return this._rdfType;
			}
		
		public boolean isReadOnly()
			{
			return this.readOnly;
			}
		
		public abstract void setInstance(Instance instance);
		public abstract void updateInstance(Instance instance);
		}
	
	private class TinyInstanceEditor extends AbsractInstanceEditor
		{
		protected JPanel inputPane=null;
		
		private Vector<Pair<URI,JComponent>> fields= new Vector<Pair<URI,JComponent>>(10,1);
		
		TinyInstanceEditor(java.awt.Component owner,URI rdfType,boolean readOnly)
			{
			super(owner,rdfType,readOnly);
			JPanel pane= new JPanel(new BorderLayout());
			pane.setBorder(new TitledBorder(getModel().shortForm(getRDFType().toString())));
			getContentPane().add(pane);
			this.inputPane= new JPanel(new InputLayout());
			pane.add(this.inputPane,BorderLayout.CENTER);
			}
		
		void addField(String label,String tooltip,Pair<URI,JComponent> f)
			{
			if(label==null) label= getModel().shortForm(f.first().toString());
			if(tooltip==null) tooltip= f.first().toString();
			JLabel label1= new JLabel(label,JLabel.RIGHT);
			label1.setToolTipText(tooltip);
			this.fields.add(f);
			this.inputPane.add(label1);
			this.inputPane.add(f.second());
			
			}
		
		public void addField(URI predicate,String label,String tooltip,Pattern pattern)
			{
			JTextField f= new JTextField("",30);
			f.setEnabled(!isReadOnly());
			if(pattern!=null && !isReadOnly()) getOKAction().mustMatchPattern(f, pattern);
			Pair<URI,JComponent> p= new Pair<URI, JComponent>(predicate,f);
			addField(label,tooltip,p);
			}
		
		public void addField(URI predicate,String label,String tooltip,Class<?> clazz)
			{
			JTextField f= new JTextField("",30);
			f.setEnabled(!isReadOnly());
			if(clazz!=null && !isReadOnly()) getOKAction().mustBeAClass(f, clazz);
			Pair<URI,JComponent> p= new Pair<URI, JComponent>(predicate,f);
			addField(label,tooltip,p);
			}
		
		@Override
		public void setInstance(Instance instance)
			{
			for(Pair<URI,JComponent> p:fields)
				{
				if(JTextComponent.class.isInstance(p.second()))
					{
					JTextComponent tc= JTextComponent.class.cast(p.second());
					String value=instance.getDataProperty(p.first());
					tc.setText(value==null?"":value);
					}
				}
			}
		@Override
		public void updateInstance(Instance instance)
			{
			if(isReadOnly()) return;
			instance.getPredicates().clear();
			for(Pair<URI,JComponent> p:fields)
				{
				if(JTextComponent.class.isInstance(p.second()))
					{
					JTextComponent tc= JTextComponent.class.cast(p.second());
					String s= tc.getText().trim();
					if(s.length()==0) continue;
					instance.getPredicates().addElement(new DataPredicate(p.first(),s));
					}
				}
			}
		
		
		}
	
	
	/**
	 * 
	 * @author lindenb
	 *
	 */
	private class InstanceEditor extends AbsractInstanceEditor
		{
		private static final long serialVersionUID = 1L;
		protected Vector<PropertyOption> dataPropOptions= new Vector<PropertyOption>();
		protected JLabel iconLabel;
		protected JLabel titleLabel;
		protected JLabel idLabel;
		
		protected JTabbedPane tabbedPane;
		protected JTable predicateTable;
		protected JTextField dataPredicateValueField;
		protected GenericTableModel<Predicate<?>> predicateModel;
		protected AbstractAction actionAddPredicate;
		protected JComboBox predicateCombo;
		protected JComboBox linkInstanceCombo;
		protected URI instanceID=null;
		
		private JPanel cardPane;
		
		/**
		 * InstanceEditor
		 * @param owner
		 * @param rdfType
		 * @param readOnly
		 */
		InstanceEditor(java.awt.Component owner,URI rdfType,boolean readOnly)
			{
			super(owner,rdfType,readOnly);
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
							case EXTERNAL_URL: 
								{
								layout.show(cardPane, PREDICATE_TYPE.DATA.toString());
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
								Assert.assertUnreachableStatement();
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
				case EXTERNAL_URL:
					{
					URL url=null;
					try {
						url = new URL(dataPredicateValueField.getText().trim());
						} 
					catch (MalformedURLException e) {
						Toolkit.getDefaultToolkit().beep();
						Debug.debug(e);
						return;
						}
					ExternalURLPredicate p=new ExternalURLPredicate(opt.uri,url);
					if(predicateModel.contains(p)) return;
					predicateModel.addElement(p);
					dataPredicateValueField.setText("");
					break;
					}
				case INSTANCE:
					{
					AbsractInstanceEditor ed= getInstanceEditor(InstanceEditor.this,
							ObjectPropOption.class.cast(opt).rdfEditedType,
							isReadOnly()
							);
					
					if(ed.showDialog()!=OK_OPTION) return;
					
					Instance child= getModel().newInstance(ObjectPropOption.class.cast(opt).rdfEditedType, true);
					ed.updateInstance(child);
					ObjectPredicate p= new ObjectPredicate(opt.uri,child);
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
				case EXTERNAL_URL:
					{
					String s=this.dataPredicateValueField.getText().trim();
					try
						{
						new URL(s);
						}
					catch(MalformedURLException err)
						{
						return false;
						}
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
				default: Assert.assertUnreachableStatement();break;
				}
				
			
			

			return false;
			}
		
		
		@Override
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
					else if(value instanceof URL)
						{
						this.setForeground(Color.PINK);
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
		
		@Override
		public void updateInstance(Instance instance) {
			instance.getPredicates().clear();
			for(Iterator<Predicate<?>> iter=this.predicateModel.listElements();
				iter.hasNext();
				)
				{
				instance.getPredicates().addElement( iter.next() );
				}
			}
		
		public Instance updateModel()
			{
			Assert.assertNotNull(this.instanceID);
			Instance instance=getModel().findInstanceByTypeAndURI(getRDFType(), this.instanceID);
			if(instance==null)
				{
				instance= getModel().newInstance(getRDFType(),false);
				instance.setID(this.instanceID);
				getModel().getInstanceVector().addElement(instance);
				}
			updateInstance(instance);
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
			
			ObjectPropOption oo= new ObjectPropOption(NCBI.NS,"ncbi",NCBI.IsNCBIAuthor,URI.create(NCBI.NS+NCBI.Author));
			options.add(oo);
			super.buildOptions(options);
			}
		}
	
	private class ImageEditor extends AgentEditor
		{
		private static final long serialVersionUID = 1L;
		private JLabel iconLabel;
		ImageEditor(Component owner,boolean readOnly)
			{
			super(owner,"Person",readOnly);
			JPanel pane= new JPanel(new BorderLayout());
			super.tabbedPane.addTab("Image", pane);
			pane.add(new JScrollPane(this.iconLabel=new JLabel()));
			}
		
		public void setURL(URL imageURL)
			{
			this.iconLabel.setIcon(new ImageIcon(imageURL));
			}
		
		}
	
	
	/** the rdf model holding all instanes */
	private RDFModel model;
	private AbstractAction addPubmedReferenceAction;
	private URI currentRDFType=null;
	private JTable instanceTable;
	private Vector<JButton> setRDFTypeButtons = new Vector<JButton>(10,1);
	private GenericTableModel<Instance> intancesTableModel;
	private Observed<File> saveAsFile=new Observed<File>();
	private int pubmedStart=0;
	private int pubmedCount=50;
	
	/**
	 * SCIFOAF
	 * @param saveAsFile
	 * @param model
	 */
	private SciFOAF(File saveAsFile,RDFModel model)
		{
		super("SciFOAF");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.model=model;
		
		
		Preferences prefs= Preferences.userNodeForPackage(SciFOAF.class);
		this.pubmedStart= prefs.getInt("pubmedStart", this.pubmedStart);
		this.pubmedCount= prefs.getInt("pubmedCount", this.pubmedCount);
		
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
		
		
		menu.add(new JMenuItem(new AbstractAction("Preferences")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
				{
				SimpleDialog d= new SimpleDialog(SciFOAF.this,"Preferences");
				JPanel pane= new JPanel(new InputLayout());
				d.getContentPane().add(pane);
				pane.add(new JLabel("Pubmed Max Return:",JLabel.RIGHT));
				JSpinner spin1= new JSpinner(new SpinnerNumberModel(SciFOAF.this.pubmedCount,1,100,1));
				pane.add(spin1);
				pane.add(new JLabel("Pubmed Start From:",JLabel.RIGHT));
				JSpinner spin2= new JSpinner(new SpinnerNumberModel(SciFOAF.this.pubmedStart,0,Integer.MAX_VALUE,1));
				pane.add(spin2);
				if(d.showDialog()!=SimpleDialog.OK_OPTION) return;
				SciFOAF.this.pubmedCount= Number.class.cast(spin1.getValue()).intValue();
				SciFOAF.this.pubmedStart= Number.class.cast(spin2.getValue()).intValue();
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
				Instance t= getSelectedInstance(SciFOAF.this.instanceTable.rowAtPoint(e.getPoint()));
				if(t==null) return;
				doMenuEditInstance(SciFOAF.this, t,false);
				}
			});
		
		Font bigFont = new Font("Dialog",Font.BOLD,32);
		JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));	
		pane1.add(bot,BorderLayout.SOUTH);
		
		
		bot.add(SwingUtils.withFont(new JButton(this.addPubmedReferenceAction = new AbstractAction("Add Pubmed Reference")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e)
				{
				Instance i= getSelectedInstance(SciFOAF.this.instanceTable.getSelectedRow());
				if(i==null) return;
				doMenuAddPubmedReference(SciFOAF.this,i);
				}
			}),bigFont));
		
		this.addPubmedReferenceAction.setEnabled(false);
		
		bot.add(SwingUtils.withFont(new JButton(new AbstractAction("New Instance")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuNewInstance(SciFOAF.this,SciFOAF.this.currentRDFType);
				}
			}),bigFont));
		
		
		
		ConstrainedAction<JTable> action= new ConstrainedAction<JTable>(instanceTable,"Edit...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e)
				{
				Instance i= getSelectedInstance(instanceTable.getSelectedRow());
				if(i==null) return;
				doMenuEditInstance(SciFOAF.this,i,false);
				}
			};
		
		action.mustHaveOneRowSelected(this.instanceTable);
		
		this.instanceTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener()
			{
			@Override
			public void valueChanged(ListSelectionEvent e)
				{
				Instance i= getSelectedInstance(instanceTable.getSelectedRow());
				if(i!=null)
					{
					if(URI.create(FOAF.NS+"Person").equals(i.getRDFType()))
						{
						if(i.hasPredicate(URI.create(NCBI.NS+NCBI.IsNCBIAuthor)))
							{
							SciFOAF.this.addPubmedReferenceAction.setEnabled(true);
							}
						return;
						}
					}
				SciFOAF.this.addPubmedReferenceAction.setEnabled(false);
				}
			});
		
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
	
	private Instance getSelectedInstance(int rowIndex)
		{
		if(rowIndex==-1) return null;
		rowIndex= SciFOAF.this.instanceTable.convertRowIndexToModel(rowIndex);
		if(rowIndex==-1) return null;
		Instance t= SciFOAF.this.intancesTableModel.elementAt(rowIndex);
		return t;
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
	
	/** @return the associated RDFModel */
	private RDFModel getModel()
		{
		return this.model;
		}
	
	/**
	 * creates a new Instance of the given rdf:type
	 */
	private void doMenuNewInstance(Component owner,URI rdfType)
		{
		if(rdfType.equals(URI.create(FOAF.NS+"Document")))
			{
			SimpleDialog dialog= new SimpleDialog(this,"Enter a PMID");
			JPanel pane= new JPanel(new InputLayout());
			dialog.getContentPane().add(pane);
			pane.add(new JLabel("Enter a Pubmed Identifier (PMID)",JLabel.RIGHT));
			JTextField f= new JTextField(20);
			if(Debug.isDebugging()) f.setText("23123");
			f.setName("PMID");
			dialog.getOKAction().mustBeInRange(f,1,Integer.MAX_VALUE);
			pane.add(f);
			if(dialog.showDialog()!=SimpleDialog.OK_OPTION) return;
			int pmid= Integer.parseInt(f.getText().trim());
			loadPubmedPapersByPMID(owner,pmid);
			}
		else
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
			
			InstanceEditor ed=InstanceEditor.class.cast(getInstanceEditor(owner,rdfType,false));
			if(ed!=null)
				{
				ed.setInstanceURI(URI.create(f.getText()));
				if(ed.showDialog()==InstanceEditor.OK_OPTION)
					{
					ed.updateModel();
					}
				}
			}
		updateInstancesTable();
		}
	
	private void doMenuEditInstance(Component owner,Instance instance,boolean readOnly)
		{
		InstanceEditor ed=InstanceEditor.class.cast(getInstanceEditor(owner,instance.getRDFType(),readOnly));

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
	
	private void doMenuAddPubmedReference(Component owner,Instance i)
		{
		boolean found=false;
		if(i==null) return;
		URI IsNCBIAuthorURI=URI.create(NCBI.NS+NCBI.IsNCBIAuthor);
		if(!i.hasPredicate(IsNCBIAuthorURI)) return;
		StringBuilder terms= new StringBuilder();
		for(Iterator<ObjectPredicate> iter=i.listObjectPredicates();
			iter.hasNext();
			)
			{
			ObjectPredicate p= iter.next();
			if(!p.getPredicate().equals(IsNCBIAuthorURI)) continue;
			Instance child= p.getValue();
			if(!child.getRDFType().equals(URI.create(NCBI.NS+NCBI.Author))) continue;
			String firstName= child.getDataProperty(URI.create( NCBI.NS+NCBI.firstName));
			String lastName= child.getDataProperty(URI.create( NCBI.NS+NCBI.lastName));
			if(lastName==null) continue;
			if(terms.length()!=0) terms.append(" OR ");
			terms.append("\"");
			terms.append(lastName);
			
			if(firstName!=null)
				{
				if(firstName.length()>1) firstName=firstName.substring(0,1);
				terms.append(" "+firstName);
				}
			
			terms.append("\"[Author]");
			found=true;
			}
		if(!found) return;
		
		class Param extends WindowAdapter
			{
			String term ;
			JDialog dialog;
			Document dom;
			Throwable error=null;
			
			@Override
			public void windowOpened(WindowEvent e)
				{
				Runnable runner= new Runnable()
					{
					@Override
					public void run()
						{
						try {
							 if(term.length()==0) return;
				                URL url= new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed"+
				                        "&term="+ URLEncoder.encode(term,"UTF-8") +
				                        "&tool=scifoaf"+
				                        "&email=plindenbaum_at_yahoo.fr"+
				                        "&retmode=xml&usehistory=y&retmax="+SciFOAF.this.pubmedCount+"&retstart="+SciFOAF.this.pubmedStart
				                        ) ;
				               Debug.debug(url);
				               
			                    InputStream in= url.openStream();
			                   
			                    XMLInputFactory xfactory = XMLInputFactory.newInstance();
			                    xfactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
			                    xfactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
			                    xfactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			           
			                    XMLEventReader parser = xfactory.createXMLEventReader(in);
			                   
			                    String QueryKey=null;
			                    String WebEnv= null;
			                    int idCount=0;
			                    /** loop over the events */
			                    while(parser.hasNext())
			                        {
			                        XMLEvent event = parser.nextEvent();
			                        if(event.isStartElement())
			                            {
			                            StartElement element = event.asStartElement();
			                            if(element.getName().getLocalPart().equals("QueryKey"))
			                                {
			                                QueryKey= parser.getElementText().trim();
			                                }
			                            else if(element.getName().getLocalPart().equals("WebEnv"))
			                                {
			                                WebEnv = parser.getElementText().trim();
			                                }
			                            else  if(element.getName().getLocalPart().equals("Id"))
			                            	{
			                            	++idCount;
			                            	}
			                            }
			                        }
			                    in.close();
			                    if(QueryKey==null || WebEnv==null)
			                        {
			                        throw new IOException("Cannot find QueryKey or WebEnv in "+url);
			                        }
				                   
			                    url= new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed"+
			                                "&rettype=full"+
			                                "&tool=scifoaf"+
			                                "&email=plindenbaum_at_yahoo.fr"+
			                                "&retmode=xml"+
			                                "&WebEnv="+ WebEnv +
			                                "&query_key="+QueryKey+
			                                "&retmode=xml&usehistory=y&retmax="+SciFOAF.this.pubmedCount+"&retstart="+SciFOAF.this.pubmedStart
			                                ) ;
			                    Debug.debug(url);
			                    Debug.debug(idCount);
			                    if(idCount>0)
				                    {
									DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
									factory.setCoalescing(true);
									factory.setExpandEntityReferences(true);
									factory.setValidating(false);
									factory.setIgnoringComments(true);
									factory.setIgnoringElementContentWhitespace(true);
									factory.setXIncludeAware(false);
									DocumentBuilder builder= factory.newDocumentBuilder();
									Debug.debug(url);
									Param.this.dom=builder.parse(url.toString());
									Debug.debug("OK, downloaded dom==null ?"+(Param.this.dom==null));
				                    }
							}
						catch (Exception e)
							{
							Debug.debug(e);
							Param.this.error=e;
							Param.this.dom=null;
							}
						Debug.debug("End Thread");
						Param.this.dialog.setVisible(false);
						Param.this.dialog.dispose();
						}	
					};
				Thread t= new Thread(runner);
				t.start();
				}
			}
		Param param= new Param();
		param.term=terms.toString();
		
		
		
		param.dialog= new JDialog(owner==null?null:SwingUtilities.getWindowAncestor(owner),"Fetching Paper",Dialog.ModalityType.APPLICATION_MODAL);
		param.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		JPanel pane= new JPanel(new BorderLayout());
		pane.setBorder(new EmptyBorder(10,10,10,10));
		param.dialog.setContentPane(pane);
		pane.add(SwingUtils.withFont(new JLabel(" Fetching "+terms,JLabel.CENTER),new Font("Dialog",Font.BOLD,24)),BorderLayout.NORTH);
		JProgressBar bar= new JProgressBar();
		bar.setIndeterminate(true);
		pane.add(bar,BorderLayout.CENTER);
		SwingUtils.packAndCenter(param.dialog);
		param.dialog.addWindowListener(param);
		param.dialog.setVisible(true);
		
		if(param.dom!=null)
			{
			Element root=param.dom.getDocumentElement();
			if(root!=null && !root.getNodeName().equals("PubmedArticleSet"))
				{
				param.error= new IOException(root.getNodeName()+":"+root.getTextContent());
				param.dom=null;
				}
			}
	
		if(param.error!=null)
			{
			ThrowablePane.show(owner, param.error);
			return;
			}
		
		if(param.dom==null)
			{
			Debug.debug("dom==null");
			return;
			}
		
		class PaperShuttle
			{
			boolean selected=false;
			Paper paper;
			}
		
		GenericTableModel<PaperShuttle> selectPapersModel = new GenericTableModel<PaperShuttle>()
			{
			private static final long serialVersionUID = 1L;
			
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if(columnIndex==0) return Boolean.class;
				return super.getColumnClass(columnIndex);
				}
			
			@Override
			public int getColumnCount() {
				return 5;
				}
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex==0;
				}
			
			@Override
			public void setValueAt(Object value, int rowIndex,
					int columnIndex) {
				elementAt(rowIndex).selected= Boolean.class.cast(value);
				fireTableCellUpdated(rowIndex, columnIndex);
				}
			
			
			@Override
			public String getColumnName(int columnIndex) {
				switch(columnIndex)
					{
					case 0: return "Select";
					case 1: return "First Author";
					case 2: return "Date";
					case 3: return "Author";
					case 4: return "Title";
					}
				return null;
				}
			
			@Override
			public Object getValueOf(PaperShuttle s, int columnIndex)
				{
				switch(columnIndex)
					{
					case 0: return s.selected;
					case 1: return (s.paper.authors.isEmpty()?null:s.paper.authors.firstElement().toString());
					case 2: return s.paper.PubDate;
					case 3: return s.paper.JournalTitle;
					case 4: return s.paper.ArticleTitle;
					}
				return null;
				}
			};
		
		Element root=param.dom.getDocumentElement();
		for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
			{
			if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
			Paper paper= Paper.parse(Element.class.cast(n1));
			if(paper==null || paper.PMID==null || containsPaperPMID(paper.PMID))
				{
				continue;
				}
			PaperShuttle s= new PaperShuttle();
			s.paper=paper;
			selectPapersModel.addElement(s);
			}
		
		SimpleDialog d= new SimpleDialog(owner,"Select Papers");
		pane= new JPanel(new BorderLayout());
		d.getContentPane().add(pane);
		pane.add(new JScrollPane(new JTable(selectPapersModel)));
		if(d.showDialog()!=SimpleDialog.OK_OPTION) return;
		
		
		for(Iterator<PaperShuttle> iter=selectPapersModel.listElements();
			iter.hasNext();)
			{
			PaperShuttle s= iter.next();
			Debug.debug();
			if(!s.selected) continue;
			Debug.debug(s.paper.PMID);
			PaperEditor paperEditor= new PaperEditor(owner,s.paper);
			Debug.debug();
			if(paperEditor.showDialog()!=PaperEditor.OK_OPTION) continue;
			paperEditor.create();
			}
		
		updateInstancesTable();
		}
	
	private boolean containsPaperPMID(String pmid)
		{
		return false;
		}
	
	
	private void doMenuQuit()
		{
		Preferences prefs= Preferences.userNodeForPackage(SciFOAF.class);
		prefs.putInt("pubmedStart", this.pubmedStart);
		prefs.putInt("pubmedCount", this.pubmedCount);
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			Debug.debug(e);
		}
		
		
		this.setVisible(true);
		this.dispose();
		}
	
	private AbsractInstanceEditor getInstanceEditor(Component owner,URI rdfType,boolean readOnly)
		{
		AbsractInstanceEditor ed=null;
		if(rdfType.equals(_uri(FOAF.NS,"Person")))
			{
			ed= new PersonEditor(owner,readOnly);
			}
		else if(rdfType.equals(_uri(FOAF.NS,"Image")))
			{
			URL url=null;
			String choices[]=new String[]{"From File","From URL"};
			int choice;
			if((choice=JOptionPane.showOptionDialog(owner, "Load Image...", "foaf:Image", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null, choices,choices[0]))==JOptionPane.CLOSED_OPTION)
				{
				return null;
				}
			switch(choice)
				{
				case 0:
					{
					JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
					chooser.setFileFilter(new FileFilter()
						{
						@Override
						public boolean accept(File f)
							{
							if(f.isDirectory()) return true;
							String name=f.getName().toLowerCase();
							return name.endsWith(".gif") ||
									name.endsWith(".png") ||
									name.endsWith(".jpg") ||
									name.endsWith(".jpeg");
							}
						@Override
						public String getDescription() {
							return "Images";
							}
						});
					if(chooser.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
					File f= chooser.getSelectedFile();
					PreferredDirectory.setPreferredDirectory(f);
					try {
						url=f.toURI().toURL();
					} catch (MalformedURLException e) {
						ThrowablePane.show(owner, e);
						return null;
						}
					break;
					}
				case 1:
					{
					SimpleDialog d= new SimpleDialog(owner,"Choose URL");
					JPanel pane= new JPanel(new InputLayout());
					d.getContentPane().add(pane);
					pane.add(new JLabel("Image URL:",JLabel.RIGHT));
					JTextField tf= new JTextField(30);
					pane.add(tf);
					d.getOKAction().mustBeURL(tf);
					if(d.showDialog()!=SimpleDialog.OK_OPTION) return null;
					try {
						url=new URL(tf.getText().trim());
					} catch (MalformedURLException e) {
						ThrowablePane.show(owner, e);
						return null;
						}
					
					break;
					}
				default:Assert.assertUnreachableStatement();break;
				}
			
			ImageEditor ie= new ImageEditor(owner,readOnly);
			ed=ie;
			ie.setURL(url);
			
			}
		else if(rdfType.equals(_uri(NCBI.NS,NCBI.Author)))
			{
			TinyInstanceEditor te= new TinyInstanceEditor(owner,rdfType,readOnly);
			te.addField(_uri(NCBI.NS,"suffix"), "Suffix", null,Pattern.compile(".*"));
			te.addField(_uri(NCBI.NS,NCBI.firstName), "First Name", null,Pattern.compile(".+"));
			te.addField(_uri(NCBI.NS,NCBI.lastName), "Last Name", null,Pattern.compile(".+"));
			te.addField(_uri(NCBI.NS,"middleName"), "Middle Name", null,Pattern.compile(".*"));
			ed=te;
			}
		
		if(ed==null)
			{
			Debug.debug("cannot create editor for "+rdfType);
			}
		
		return ed;
		}
	private Document loadPubmedPapersByPMID(Component owner, int pmid)
		{
		try {
			class Param extends WindowAdapter
				{
				int pmid;
				JDialog dialog;
				Document dom;
				Throwable error=null;
				
				@Override
				public void windowOpened(WindowEvent e)
					{
					Runnable runner= new Runnable()
						{
						@Override
						public void run()
							{
							try {	
								DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
								factory.setCoalescing(true);
								factory.setExpandEntityReferences(true);
								factory.setValidating(false);
								factory.setIgnoringComments(true);
								factory.setIgnoringElementContentWhitespace(true);
								factory.setXIncludeAware(false);
								DocumentBuilder builder= factory.newDocumentBuilder();
							
							
								String url="http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+
								Param.this.pmid+"&retmode=xml";
								Debug.debug(url);
								Param.this.dom=builder.parse(url);
								Debug.debug("OK, downloaded");
								
								}
							catch (Exception e)
								{
								Debug.debug(e);
								Param.this.error=e;
								Param.this.dom=null;
								}
							Debug.debug("End Thread");
							Param.this.dialog.setVisible(false);
							Param.this.dialog.dispose();
							}	
						};
					Thread t= new Thread(runner);
					t.start();
					}
				
				}
			Param param= new Param();
			param.pmid=pmid;
			
			
			
			param.dialog= new JDialog(owner==null?null:SwingUtilities.getWindowAncestor(owner),"Fetching Paper",Dialog.ModalityType.APPLICATION_MODAL);
			param.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			JPanel pane= new JPanel(new BorderLayout());
			pane.setBorder(new EmptyBorder(10,10,10,10));
			param.dialog.setContentPane(pane);
			pane.add(SwingUtils.withFont(new JLabel(" Fetching PMID."+pmid+" from NCBI ",JLabel.CENTER),new Font("Dialog",Font.BOLD,24)),BorderLayout.NORTH);
			JProgressBar bar= new JProgressBar();
			bar.setIndeterminate(true);
			pane.add(bar,BorderLayout.CENTER);
			SwingUtils.packAndCenter(param.dialog);
			param.dialog.addWindowListener(param);
			param.dialog.setVisible(true);
			
			if(param.dom!=null)
				{
				Element root=param.dom.getDocumentElement();
				if(root!=null && !root.getNodeName().equals("PubmedArticleSet"))
					{
					param.error= new IOException(root.getNodeName()+":"+root.getTextContent());
					param.dom=null;
					}
				}
			
			if(param.error!=null)
				{
				ThrowablePane.show(owner, param.error);
				return null;
				}
			
			Debug.debug("Done");
			Paper test=Paper.parse(param.dom.getDocumentElement());
			PaperEditor ed= new PaperEditor(owner,test);
			if(ed.showDialog()!=PaperEditor.OK_OPTION) return null;
			ed.create();
			updateInstancesTable();
			return param.dom;
			}
		catch (Exception e) {
			ThrowablePane.show(owner, e);
			return null;
			}
		}
	
	private static URI _uri(String ns,String local)
		{
		return _uri(ns+local);
		}
	
	private static URI _uri(String uri)
		{
		return URI.create(uri);
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
