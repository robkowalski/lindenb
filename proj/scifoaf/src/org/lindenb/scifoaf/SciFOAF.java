/**
 * 
 */
package org.lindenb.scifoaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.lindenb.io.IOUtils;
import org.lindenb.jena.JenaUtils;
import org.lindenb.jena.vocabulary.FOAF;
import org.lindenb.lang.ResourceUtils;
import org.lindenb.lang.RunnableObject;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.sw.vocabulary.KML;
import org.lindenb.sw.vocabulary.SVG;
import org.lindenb.sw.vocabulary.XHTML;
import org.lindenb.sw.vocabulary.XLINK;
import org.lindenb.swing.ConstrainedAction;
import org.lindenb.swing.FileExtensionFilter;
import org.lindenb.swing.ObjectAction;
import org.lindenb.swing.SwingUtils;
import org.lindenb.swing.layout.InputLayout;
import org.lindenb.swing.table.GenericTableModel;
import org.lindenb.util.C;
import org.lindenb.util.Compilation;
import org.lindenb.util.Couple;
import org.lindenb.util.Pair;
import org.lindenb.util.SHA1;
import org.lindenb.util.TimeUtils;
import org.lindenb.xml.XMLUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

class Event
	{
	public static final String NS="http://purl.org/NET/c4dm/event.owl#";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Event = m.createResource(NS+ "Event" );
	public static final Property place = m.createProperty(NS, "place" );
	public static final Property agent = m.createProperty(NS, "agent" );
	public static final Property isAgentIn = m.createProperty(NS, "isAgentIn" );
	public static final Property sub_event = m.createProperty(NS, "sub_event" );
	}

/** description of a project */
class DOAP
	{
	public static final String NS="http://usefulinc.com/ns/doap#";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Project = m.createResource(NS+ "Project" );
	public static final Property name = m.createProperty(NS, "name" );
	public static final Property homepage = m.createProperty(NS, "homepage" );
	public static final Property shortdesc = m.createProperty(NS, "shortdesc" );
	public static final Property description = m.createProperty(NS, "description" );
	public static final Property created = m.createProperty(NS, "created" );
	public static final Property maintainer = m.createProperty(NS, "description" );
	public static final Property developer = m.createProperty(NS, "developer" );
	public static final Property documenter = m.createProperty(NS, "documenter" );
	public static final Property translator = m.createProperty(NS, "translator" );
	public static final Property screenshots = m.createProperty(NS, "screenshots" );
	}
/**
 * Geography ontology, fields inspired from geonames.org
 * @author pierre
 *
 */
class Geo
	{
	static final String NS="http://ontology.lindenb.org/geo#";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Place = m.createResource(NS+ "Place" );
	public static final Property lon = m.createProperty(NS, "long" );
	public static final Property lat = m.createProperty(NS, "lat" );
	public static final Property country = m.createProperty(NS, "country" );
	public static final Property adminCode1 = m.createProperty(NS, "adminCode1" );
	public static final Property adminName1 = m.createProperty(NS, "adminName1" );
	public static final Property adminCode2 = m.createProperty(NS, "adminCode2" );
	public static final Property adminName2 = m.createProperty(NS, "adminName2" );
	public static final Property postalCode = m.createProperty(NS, "postalCode" );
	}

class Image
	{
	static final String NS="http://ontology.lindenb.org/img#";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Property width = m.createProperty(NS, "width" );
	public static final Property height = m.createProperty(NS, "height" );
	}

/** see bibiography ontology */
class BIBO
	{
	static final String NS="http://purl.org/ontology/bibo/";
	static final String PUBMED_PREFIX="http://www.ncbi.nlm.nih.gov/pubmed/";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Article = m.createResource(NS+"Article" );
	public static final Resource Journal = m.createResource(NS+"Journal" );
	public static final Property volume = m.createProperty(NS, "volume" );
	public static final Property issue = m.createProperty(NS, "number" );
	public static final Property issn = m.createProperty(NS, "issn" );
	public static final Property pages = m.createProperty(NS, "pages" );
	public static final Property doi = m.createProperty(NS, "doi" );
	public static final Property pmid = m.createProperty(NS, "pmid" );
	public static final Property shortTitle = m.createProperty(NS, "shortTitle" );
	
	}

/**
 * @author SciFOAF
 *
 */
public class SciFOAF extends JFrame
	{
	private static final long serialVersionUID = 1L;
	/** world cat prefix */
	private static final String WORLD_CAT_PREFIX="http://www.worldcat.org/issn/";
	/** pattern for date */
	private static Pattern DATE_PATTERN=Pattern.compile("\\d\\d\\d\\d(\\-\\d\\d(\\-\\d\\d)?)?");
	
	
	/** model */
	private Model rdfModel;
	/** file */
	private File file=null;
	/** content pane */
	private JPanel contentPane;
	/** history of instance edited */
	private Stack<EditHistory> history=new Stack<EditHistory>();
	/** log **/
	private org.apache.commons.logging.Log _log= LogFactory.getLog(SciFOAF.class); 
	/** tmp File */
	private File tmpDirectory=new File("/tmp/");
	/** icon size */
	private int iconSize=64;
	
	
	
	/** GenericMouseAdapter */
	private static class GenericMouseAdapter<T>
		extends MouseAdapter
		{
		private T object;
		GenericMouseAdapter(T object) { this.object=object;}
		public T getObject() {
			return object;
			}
		}
	


	
	/** named Resource Pair(Resource,name for this resource ) */
	private class NamedResource
		extends Pair<Resource, String>
		{
		NamedResource(Resource r,String n) { super(r,n);}
		NamedResource(Resource r) { this(r,shortForm(r));}
		@Override
		public String toString() {
			return second();
			}
		}
	
	/**  instance edited */
	private class EditHistory
		{
		Resource subject=null;
		Resource rdfType=null;
		}
	public static final String GEO_NAME_HEADERS[]={
		"Postal Code","Name","Country","Lat","Long",
		"Admin Code 1","Admin Name 1",
		"Admin Code 2","Admin Name 2"
		};
	/** handles GeoNames */
	private class GeoNamePane extends JPanel
		{
		private static final long serialVersionUID = 1L;

		class GeoName
			{
			private String name="";
			private String postalCode;
			private String country;
			private Double latitude;
			private Double longitude;
			private String adminCode1;
			private String adminName1;
			private String adminCode2;
			private String adminName2;
			}
		
		
		
		private JTextField postCodeField;
		private JTextField placeNameField;
		private JTextField countryField;
		private SpinnerNumberModel maxRows;
		private Thread thread=null;
		private JTable table=null;
		private AbstractAction goAction;
		
		private class SearchPlace implements Runnable
			{
			public void run() {
				try {
					StringBuilder urlStr=new StringBuilder(
							"http://ws.geonames.org/postalCodeSearch?style=FULL"
							);
					String s= postCodeField.getText();
					if(s.trim().length()>0)
						{
						urlStr.append("&postalcode="+URLEncoder.encode(s, "UTF-8"));
						}
					s= placeNameField.getText();
					if(s.trim().length()>0)
						{
						urlStr.append("&placename="+URLEncoder.encode(s, "UTF-8"));
						}
					s= countryField.getText();
					if(s.trim().length()>0)
						{
						urlStr.append("&country="+URLEncoder.encode(s, "UTF-8"));
						}
					urlStr.append("&maxRows="+maxRows.getNumber().intValue());
					
					DocumentBuilderFactory f= DocumentBuilderFactory.newInstance();
					DocumentBuilder b= f.newDocumentBuilder();
					Document doc= b.parse(urlStr.toString());
					Element root=doc.getDocumentElement();
					if(root==null) return;
					GenericTableModel<GeoName> t= newTableModel();
					for(Node n1= root.getFirstChild();n1!=null;n1=n1.getNextSibling())
						{
						if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
						if(!n1.getNodeName().equals("code")) continue;
						
						GeoName newRow= new GeoName();
						for(Node n2= n1.getFirstChild();n2!=null;n2=n2.getNextSibling())
							{
							if(n2.getNodeType()!=Node.ELEMENT_NODE) continue;
							s= n2.getNodeName();
							String txt=n2.getTextContent();
								if(s.equals("postalcode")) newRow.postalCode=txt;
							else if(s.equals("name")) newRow.name=txt;
							else if(s.equals("countryCode")) newRow.country=txt;
							else if(s.equals("lat")) newRow.latitude=new Double(txt);
							else if(s.equals("lng")) newRow.longitude=new Double(txt);
							else if(s.equals("adminCode1")) newRow.adminCode1=txt;
							else if(s.equals("adminName1")) newRow.adminName1=txt;
							else if(s.equals("adminCode2")) newRow.adminCode2=txt;
							else if(s.equals("adminName2")) newRow.adminName2=txt;
							}
						t.addElement(newRow);
						}
					table.setModel(t);
					
					} catch (Exception e) {
						Toolkit.getDefaultToolkit().beep();
					}
				thread=null;
				goAction.setEnabled(true);
				}
			}
		
		public GeoNamePane()
			{
			super(new BorderLayout(5,5));
			this.goAction=new AbstractAction("Go")
				{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
					{
					searchPlace();
					}
				};
			JPanel top=new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.add(top,BorderLayout.NORTH);
			JLabel label=new JLabel("Postal Code:",JLabel.RIGHT);
			
			top.add(label);
			this.postCodeField= new JTextField("",10);
			this.postCodeField.addActionListener(goAction);
			top.add(this.postCodeField);
			label=new JLabel("Place Name:",JLabel.RIGHT);
			top.add(label);
			this.placeNameField= new JTextField("",20);
			this.placeNameField.addActionListener(goAction);
			top.add(this.placeNameField);
			label=new JLabel("Country:",JLabel.RIGHT);
			top.add(label);
			this.countryField= new JTextField("",3);
			top.add(this.countryField);
			label=new JLabel("Max Rows:",JLabel.RIGHT);
			top.add(label);
			top.add(new JSpinner(this.maxRows=new SpinnerNumberModel(10,1,50,1)));
			top.add(new JButton(goAction));
			
			this.table= new JTable(newTableModel());
			this.table.setShowVerticalLines(false);
			this.table.setFont(new Font("Dialog",Font.PLAIN,18));
			this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			add(new JScrollPane(this.table),BorderLayout.CENTER);
			}
		
		private void searchPlace()
			{
			if(this.thread!=null) return;
			this.goAction.setEnabled(false);
			SearchPlace run=new SearchPlace();
			this.thread=new Thread(run);
			this.thread.start();
			}
		
		
		@SuppressWarnings("unchecked")
		public GeoName getSelectedGeoName()
			{
			int i= this.table.getSelectedRow();
			if(i==-1) return null;
			GenericTableModel<GeoName> m=(GenericTableModel<GeoName>)(this.table.getModel());
			return m.elementAt(i);
			}
		
		
		private GenericTableModel<GeoName> newTableModel()
			{
			return new GenericTableModel<GeoName>()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public int getColumnCount() {
					return GEO_NAME_HEADERS.length;
					}
				@Override
				public String getColumnName(int c) {
					return GEO_NAME_HEADERS[c];
					}
				@Override
				public Object getValueOf(GeoName p, int column)
					{
					switch(column)
						{
						case 0 : return p.postalCode;
						case 1 : return p.name;
						case 2 : return p.country;
						case 3 : return p.latitude;
						case 4 : return p.longitude;
						case 5 : return p.adminCode1;
						case 6 : return p.adminName1;
						case 7 : return p.adminCode2;
						case 8 : return p.adminName2;
						}
					return null;
					}
				};
			}
		
		}
	
	
	/** RDFEditor */
	private abstract class RDFEditor
		{
		/** subject of this editor  */
		private Resource subject;
		/** property of this editor */
		private Property property;
		
		public void setSubject(Resource subject) {
			this.subject = subject;
			}
		public Resource getSubject() {
			return subject;
			}
		public void setProperty(Property property) {
			this.property = property;
			}
		public Property getProperty() {
			return property;
			}
		public abstract JComponent getComponent();
		public abstract boolean isEmpty();
		public boolean isValid() { return getValidationMessage()==null;}
		public abstract String getValidationMessage();
		public abstract void loadFromModel();
		public abstract void saveToModel();
		}
	
	private class AnonymousResource
	extends RDFEditor
		{
		private Resource anonymousResource=null;
		private Resource myRdfType;
		private RDFEditorsList editorList=new RDFEditorsList()
			{
			private static final long serialVersionUID = 1L;

			@Override
			public Resource getSubject() {
				return AnonymousResource.this.anonymousResource;
				}
			};
		
		AnonymousResource(Resource myRdfType)
			{
			this.myRdfType=myRdfType;
			}
		
		protected RDFEditorsList getEditorsList()
			{
			return this.editorList;
			}
		
		@Override
		public JComponent getComponent() {
			return getEditorsList();
			}
		
		@Override
		public String getValidationMessage() {
			for(RDFEditor ed:getEditorsList().editors)
				{
				String s= ed.getValidationMessage();
				if(s!=null)
					{
					log().debug("validation message says "+s);
					return s;
					}
				}
			return  null;
			}
		@Override
		public boolean isEmpty() {
			for(RDFEditor ed:getEditorsList().editors)
				{
				if(!ed.isEmpty()) return false;
				}
			return true;
			}
		@Override
		public void loadFromModel()
			{
			this.anonymousResource=null;
			StmtIterator iter= getModel().listStatements(getSubject(), getProperty(), (RDFNode)null);
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				if(!getModel().contains(stmt.getResource(),RDF.type,this.myRdfType)) continue;
				this.anonymousResource=stmt.getResource();
				break;
				}
			iter.close();
			if(this.anonymousResource==null)
				{
				this.anonymousResource=getModel().createResource(AnonId.create());
				}
			for(RDFEditor ed:editorList.editors)
				{
				ed.setSubject(this.anonymousResource);
				}
			this.editorList.loadFromModel();
			}
		
		@Override
		public void saveToModel()
			{
			if(this.anonymousResource==null) return;
			if(isEmpty())
				{	
				JenaUtils.remove(getModel(), this.anonymousResource, null, null);
				JenaUtils.remove(getModel(),getSubject(), getProperty(), this.anonymousResource);
				}
			else
				{
				getModel().add(getSubject(),getProperty(),this.anonymousResource);
				getModel().add(this.anonymousResource,RDF.type,this.myRdfType);
				this.editorList.saveToModel();
				}
			}
		
		}
	
	
	
	
	
	/**
	 * 
	 * ComboRDFEditor
	 *
	 */
	private  class ComboRDFEditor
	extends RDFEditor
		{
		private JComboBox combo;
		private Property reverseProperty;
		
		private ComboRDFEditor()
			{
			this.combo= new JComboBox(new DefaultComboBoxModel());
			}
		
		public ComboRDFEditor(Resource rdfType)
			{
			this();
			setModel(rdfType);
			}
		
		@Override
		public JComponent getComponent() {
			return this.combo;
			}
		
		private void setModel(NamedResource keys[])
			{
			this.combo.setModel(new DefaultComboBoxModel(keys));
			}
		
		private void setModel(Resource rdfType)
			{
			DefaultComboBoxModel cbm=new DefaultComboBoxModel();
			//first always null
			cbm.addElement(null);
			ResIterator iter= getModel().listSubjectsWithProperty(RDF.type, rdfType);
			while(iter.hasNext())
				{
				Resource r= iter.nextResource();
				String s= JenaUtils.findTitle(getModel(), r);
				if(s==null) s= shortForm(r);
				cbm.addElement(new NamedResource(r,s));
				}
			iter.close();
			this.combo.setModel(cbm);
			}
		
		
		
		
		public void setReverseProperty(Property reverseProperty)
			{
			this.reverseProperty=reverseProperty;
			}
		
		public Property getReverseProperty() {
			return reverseProperty;
			}
		
		@Override
		public String getValidationMessage() {
			return null;
			}
		
		public JComboBox getComboBox() {
			return combo;
			}
		
		@Override
		public void loadFromModel()
			{
			ComboBoxModel comboModel = getComboBox().getModel();
			getComboBox().setSelectedIndex(-1);
			StmtIterator iter= getModel().listStatements(getSubject(),getProperty(),RDFNode.class.cast(null));
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				if(stmt.getResource().isAnon()) continue;
				boolean found=false;
				for(int i=0;i< comboModel.getSize();++i)
					{
					NamedResource k=NamedResource.class.cast(comboModel.getElementAt(i));
					if(k==null) continue;
					if(k.first().equals(stmt.getResource()))
						{
						getComboBox().setSelectedIndex(i);
						found=true;
						break;
						}
					}

				if(found) break;
				}
			iter.close();
			}

		@Override
		public void saveToModel()
			{
			NamedResource s= NamedResource.class.cast(getComboBox().getSelectedItem());
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s!=null) getModel().add(
					getSubject(),
					getProperty(),
					s.first()
					);
			}
		
		@Override
		public boolean isEmpty() {
			return getComboBox().getSelectedIndex()==-1 || getComboBox().getSelectedItem()==null;
			}
		
		}


	
	private abstract class AbstractListEditor
	extends RDFEditor
		{
		private JPanel pane;
		private JTextField inputField;
		protected JList list;
		AbstractListEditor()
			{
			this.pane= new JPanel(new BorderLayout());
			this.pane.setBorder(new LineBorder(Color.GRAY));
			JPanel top=new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.pane.add(top,BorderLayout.NORTH);
			top.add(new JLabel("Add:",JLabel.RIGHT));
			this.inputField= new JTextField(10);
			top.add(this.inputField);
			AbstractAction action= new AbstractAction("OK")
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String s=inputField.getText().trim();
					if(!accept(s))
						{
						Toolkit.getDefaultToolkit().beep();
						return;
						}
					DefaultListModel model= DefaultListModel.class.cast(list.getModel());
					int i=0;
					for(i=0;i< model.getSize();++i)
						{
						String x=String.class.cast(model.elementAt(i));
						if(x.equalsIgnoreCase(s))
							{
							Toolkit.getDefaultToolkit().beep();
							return;
							}
						}
					model.addElement(s);
					inputField.setText("");
					}
				};
			this.inputField.addActionListener(action);
			
			
			
			top.add(new JButton(action));
			this.list= new JList(new DefaultListModel());
			this.pane.add(new JScrollPane(this.list),BorderLayout.CENTER);
			
			
			ConstrainedAction<JList> remove= new ConstrainedAction<JList>(this.list,"Remove")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent arg0)
					{
					int i= list.getSelectedIndex();
					if(i==-1) return;
					DefaultListModel m= DefaultListModel.class.cast(list.getModel());
					m.removeElementAt(i);
					}
				};
			top.add(new JButton(remove));
			remove.mustHaveOneRowSelected(list);
			SwingUtils.setFontDeep(this.pane, new Font("Dialog",Font.PLAIN,9));
			}
		/** no edition is possible (e.g. FOAF.mbox_sha1sum */
		public void disable()
			{
			this.inputField.setEnabled(false);
			this.list.setEnabled(false);
			}
		
		@Override
		public JComponent getComponent() {
			return this.pane;
			}
		
		@Override
		public String getValidationMessage() {
			return null;
			}
		@Override
		public boolean isEmpty() {
			return this.list.getModel().getSize()==0;
			}
		
		protected abstract void loadNode(RDFNode node);
		protected abstract void saveNode(String s);
		//added this for PersonEditor, then I can save mbox and mbox_sha1 at the same time
		protected void cleanModel()
			{
			JenaUtils.remove(getModel(), getSubject(), getProperty(), null);
			}
		protected abstract boolean accept(String s);
		
		@Override
		public void loadFromModel() {
			DefaultListModel model= DefaultListModel.class.cast(list.getModel());
			inputField.setText("");
			model.setSize(0);
			NodeIterator iter=getModel().listObjectsOfProperty(getSubject(),getProperty());
			while(iter.hasNext())
				{
				loadNode(iter.nextNode());
				}
			iter.close();
			}
		
		
		
		@Override
		public void saveToModel()
			{
			DefaultListModel model= DefaultListModel.class.cast(list.getModel());
			cleanModel();
			for(int i=0;i< model.getSize();++i)
				{
				saveNode(String.class.cast(model.elementAt(i)));
				}
			}
		}
	
	
	private class LiteralListEditor
	extends AbstractListEditor
		{
		LiteralListEditor()
			{
			}
		
		@Override
		protected boolean accept(String s) {
			return s!=null && s.length()>0;
			}
		
		@Override
		protected void loadNode(RDFNode node)
			{
			DefaultListModel model= DefaultListModel.class.cast(list.getModel());
			if(!node.isLiteral()) return;
			model.addElement(Literal.class.cast(node).getString());
			}

		@Override
		protected void saveNode(String s)
			{
			if(s==null) return;
			getModel().add(getSubject(),getProperty(),getModel().createLiteral(String.class.cast(s)));
			}
		}
	/** editor specialized in editing resource */
	private class ResourceListEditor
	extends AbstractListEditor
		{
		ResourceListEditor()
			{
			}
		
		@Override
		protected boolean accept(String s)
			{
			try {
				new URI(s);
				return true;
				} 
			catch (URISyntaxException e) {
				return false;
				}			
			}
		
		@Override
		protected void loadNode(RDFNode node)
			{
			log().debug("found node="+node);
			DefaultListModel model= DefaultListModel.class.cast(list.getModel());
			if(!node.isResource()) return;
			if(node.isAnon()) { log().info("Node is anon");return;}
			model.addElement(Resource.class.cast(node).getURI());
			}

		@Override
		protected void saveNode(String s)
			{
			if(s==null) return;
			getModel().add(getSubject(),getProperty(),getModel().createResource(String.class.cast(s)));
			}
		}
	
	private class URLListEditor
	extends ResourceListEditor
		{
		@Override
		protected boolean accept(String s)
			{
			if(s==null) return false;
			try {
				new URL(s);
				return true;
				} 
			catch (MalformedURLException e) {
				return false;
				}			
			}
		}
	
	/**
	 * AbstractTextRDFEditor
	 * @author pierre
	 *
	 */
	private abstract class AbstractTextRDFEditor
	 	extends RDFEditor
		{
		/** text field */
		private JTextField tf= new JTextField(15);
		
		@Override
		public void setProperty(Property prop) {
			super.setProperty(prop);
			this.tf.setName(shortForm(prop));
			}
		JTextField getTextField() { return this.tf;}
		@Override
		public boolean isEmpty() {
			return getTextField().getText().trim().length()==0;
			}
		
		@Override
		public JComponent getComponent() {
			return getTextField();
			}
		}
	
	/**
	 * UrlRDFEditor
	 *
	 */
	private class UrlRDFEditor extends AbstractTextRDFEditor
		{
		@Override
		public String getValidationMessage() {
			String s= super.tf.getText().trim();
			if(s.length()==0) return null;
			try
				{
				new URL(s);
				}
			catch(MalformedURLException err)
				{
				return "Malformed URL "+s;
				}
			return null;
			}
		@Override
		public void loadFromModel()
			{
			super.tf.setText("");
			StmtIterator iter= getModel().listStatements(getSubject(),getProperty(),RDFNode.class.cast(null));
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				if(!stmt.getResource().isAnon()) continue;
				getTextField().setText(stmt.getResource().getURI());
				getTextField().setCaretPosition(0);
				break;
				}
			iter.close();
			}
		
		@Override
		public void saveToModel() {
			String s= getTextField().getText().trim();
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s.length()!=0) getModel().add(
					getSubject(),
					getProperty(),
					getModel().createResource(s)
					);
			}
		
		}
	
	/**
	 * MultiRDFTextEditor
	 *
	 */
	private class MultiRDFTextEditor extends  AbstractTextRDFEditor
		{
		MultiRDFTextEditor()
			{
			}
		
		
		@Override
		public String getValidationMessage() {
			return null;
			}
		
		@Override
		public void loadFromModel() {
			super.tf.setText("");
			StringBuilder b= new StringBuilder();
			StmtIterator iter= getModel().listStatements(getSubject(),getProperty(),RDFNode.class.cast(null));
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isLiteral()) continue;
				if(b.length()>0) b.append(" ");
				b.append(stmt.getLiteral().getString());
				}
			iter.close();
			getTextField().setText(b.toString());
			getTextField().setCaretPosition(0);
			}
		@Override
		public void saveToModel() {
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			for(String s: super.tf.getText().trim().split("[ \n\t\r]+"))
				{
				getModel().add(getSubject(),getProperty(),s);
				}
			}
		}
	
	/**
	 * TextField used a a RDFEditor
	 * @author pierre
	 *
	 */
	private class TextRDFEditor extends AbstractTextRDFEditor
		{
		private Vector<Pattern> patterns= new Vector<Pattern>();
		private Vector<Class<?>> classes= new Vector<Class<?>>();
		
		
		
		TextRDFEditor() {}
		
		public TextRDFEditor addPattern(Pattern pat) { this.patterns.add(pat); return this;}
		public TextRDFEditor addClass(Class<?> clazz) { this.classes.add(clazz); return this;}
		
		
		@Override
		public String getValidationMessage() {
			String s= this.getTextField().getText().trim();
			if(s.length()==0) return null;
			for(Pattern pat: this.patterns)
				{
				if(!pat.matcher(s).matches())
					{
					return ""+getTextField().getName()+" should match regex "+pat.pattern();
					}
				}
			for(Class<?> clazz: this.classes)
				{
				try {
					Constructor<?> cst= clazz.getConstructor(String.class);
					cst.newInstance(s);
					
					
					}
				catch (Exception e) {
					return "Cannot cast "+s+" to "+clazz+" : "+e.getMessage();
					}
				}
			return null;
			}
		
		@Override
		public void loadFromModel() {
			getTextField().setText("");
			StmtIterator iter= getModel().listStatements(getSubject(),getProperty(),RDFNode.class.cast(null));
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isLiteral()) continue;
				getTextField().setText(stmt.getLiteral().getString());
				getTextField().setCaretPosition(0);
				break;
				}
			iter.close();
			}
		
		@Override
		public void saveToModel() {
			
			String s= getTextField().getText().trim();
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s.length()!=0)
				{
				log().debug("saving ("+getSubject()+","+getProperty()+","+s+")");
				getModel().add(getSubject(),getProperty(),s);
				}
			}
		}
	
	/**
	 * 
	 * RDFTableCellRenderer
	 *
	 */
	private class RDFTableCellRenderer
	extends DefaultTableCellRenderer
		{
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable arg0,
				Object value, boolean arg2, boolean arg3, int arg4, int arg5)
			{
			if(value==null)
				{
				//nothing
				}
			else if(value instanceof RDFNode)
				{
				RDFNode node= RDFNode.class.cast(value);
				if(node.isLiteral())
					{
					value = Literal.class.cast(value).getString();
					}
				else if(node.isResource())
					{
					value =shortForm(Resource.class.cast(value));
					}
				}
			Component c= super.getTableCellRendererComponent(arg0, value, arg2, arg3, arg4, arg5);
			return c;
			}
		}
	
	/**
	 * 
	 * RDFEditorsList
	 *
	 */
	private abstract class RDFEditorsList
		extends JPanel
		{
		private static final long serialVersionUID = 1L;
		protected Vector<RDFEditor> editors= new Vector<RDFEditor>();
		
		protected RDFEditorsList()
			{
			super(new BorderLayout());
			}
		
		public abstract Resource getSubject();
		
		protected void saveToModel()
			{
			log().debug("saving editors to model ");
			for(RDFEditor ed:this.editors)
				{
				ed.saveToModel();
				}
			}
		
		protected void loadFromModel()
			{
			for(RDFEditor ed:this.editors)
				{
				ed.loadFromModel();
				}
			}
		
		protected boolean isEditorsValid()
			{
			for(RDFEditor ed: this.editors)
				{
				String msg= ed.getValidationMessage();
				if(msg!=null)
					{
					JOptionPane.showMessageDialog(RDFEditorsList.this, msg,"Error",JOptionPane.WARNING_MESSAGE,null);
					return false;
					}
				}
			log().debug("isEditorsValid returns true");
			return true;
			}
		
		protected  RDFEditor addRDFField(JComponent input,Property prop,RDFEditor ed)
			{
			JLabel tf= new JLabel(shortForm(prop)+":",JTextField.RIGHT);
			input.add(tf);
			tf.setToolTipText(prop.getURI());
			tf.addMouseListener(new GenericMouseAdapter<RDFEditor>(ed)
				{
				@Override
				public void mousePressed(MouseEvent arg0) {
					getObject().getComponent().requestFocus();
					}
				});
			ed.setSubject(getSubject());
			ed.setProperty(prop);
			input.add(ed.getComponent());
			this.editors.add(ed);
			return ed;
			}
		
		
		
		protected TextRDFEditor addInputField(JComponent input,Property prop)
			{
			return TextRDFEditor.class.cast(addRDFField(input,prop,new TextRDFEditor()));
			}
		
		protected MultiRDFTextEditor addMultiInputField(JComponent input,Property prop)
			{
			return MultiRDFTextEditor.class.cast(addRDFField(input,prop,new MultiRDFTextEditor()));
			}
		
		protected UrlRDFEditor addResourceField(JComponent input,Property prop)
			{
			return UrlRDFEditor.class.cast(addRDFField(input,prop,new UrlRDFEditor()));
			}
		
		}
	
	
	/** InstanceEditor */
	private class InstanceEditor
		extends RDFEditorsList
		{
		private static final long serialVersionUID = 1L;
		/** subject of this editor */
		private Resource subject;
		
		protected InstanceEditor(Resource subject)
			{
			this.subject=subject;
			JPanel top= new JPanel(new BorderLayout());
			this.add(top,BorderLayout.NORTH);
			JLabel label= new JLabel(shortForm(subject),JLabel.CENTER);
			label.setFont(new Font("Helvetica",Font.BOLD,24));
			label.setToolTipText(subject.getURI());
			top.add(label,BorderLayout.CENTER);
			
			try {
				new URL(subject.getURI());
				URI url=new URI(subject.getURI());
				label.setForeground(Color.BLUE);
				label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				label.addMouseListener(new GenericMouseAdapter<URI>(url)
					{
					@Override
					public void mouseClicked(MouseEvent me) {
						
						if (Desktop.isDesktopSupported() &&
							JOptionPane.showConfirmDialog(InstanceEditor.this, "Open "+getObject()+" ?")==JOptionPane.OK_OPTION) 
							{
							try
								{
								Desktop.getDesktop().browse(getObject());
								}
							catch(IOException err)
								{
								ThrowablePane.show(InstanceEditor.this, err);
								}
							}
						}
					});
				}
			catch (MalformedURLException e)
				{
				//ignore
				}
			catch (URISyntaxException e)
				{
				//ignore
				}
			
			
			//find depiction
			ImageIcon icon= findDepiction(subject);
			if(icon!=null) top.add(new JLabel(icon),BorderLayout.WEST);
					
			
			JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
			this.add(bot,BorderLayout.SOUTH);
			
			
			String title=(SciFOAF.this.history.isEmpty()?
				"Back to Main Pane":
				"Back to " + shortForm(SciFOAF.this.history.elementAt(SciFOAF.this.history.size()-1).subject) );
			bot.add(new JButton(new AbstractAction(title)
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					doOKPressed();
					}
				}));
			
			}
		
		@Override
		public Resource getSubject() {
			return subject;
			}
		
		
		/* right bottom button in editor call this method */
		protected boolean doOKPressed()
			{
			log().debug("OK pressed");
			//cancel action if one editor is not valide
			if(!isEditorsValid()) return false;
			//save all editors in model
			saveToModel();
			//pop history
			SciFOAF.this.history.pop();
			//history is empty
			if(SciFOAF.this.history.isEmpty())
				{
				log().debug("Go back to main Pane");
				installMainPane();
				}
			else
				{
				EditHistory eh=SciFOAF.this.history.pop();
				log().debug("go back to "+eh.subject);
				installInstancePane(
					eh.subject,
					eh.rdfType
					);
				}
			return true;
			}
		

		/** createTable
		 * @param title title of this pane 
		 * @param the table model used in a JTable
		 * @param rdfType rdftype of the items in the tables
		 * */
		protected JComponent createSelectTable(
			String title,
			SelectRsrcTableModel tm
			)
			{
			JPanel p= new JPanel(new BorderLayout());
			p.setPreferredSize(new Dimension(100,getIconSize()*4));
			p.setBorder(new TitledBorder(title));
			JTable table= new JTable(tm);
			table.setShowVerticalLines(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			RDFTableCellRenderer render= new RDFTableCellRenderer();
			/** starting from 1 because first column is select on/off */
			for(int i=1;i< table.getColumnModel().getColumnCount();++i)
				{
				if(Icon.class.equals(tm.getColumnClass(i)))
					{
					table.setRowHeight(SciFOAF.this.getIconSize());
					continue;
					}
				table.getColumnModel().getColumn(i).setCellRenderer(render);
				}
			p.add(new JScrollPane(table), BorderLayout.CENTER);
			JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
			p.add(bot,BorderLayout.SOUTH);
			
			/** adding table */
			
				{
				class Shuttle1 { JTable table; Resource rdfType;}
				Shuttle1 sh1= new Shuttle1();
				sh1.table = table;
				sh1.rdfType = tm.getRdfTypeInRange();
				ConstrainedAction<Shuttle1> action= new ConstrainedAction<Shuttle1>(sh1,"View")
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent ae)
						{
						if(!InstanceEditor.this.isEditorsValid()) return;
						InstanceEditor.this.saveToModel();
						int i= getObject().table.getSelectedRow();
						if(i==-1) return;
						log().debug(getObject().table.getModel().getClass());
						SelectRsrcTableModel tm=SelectRsrcTableModel.class.cast(getObject().table.getModel());
						Resource r= tm.elementAt(i);
						installInstancePane(r,getObject().rdfType);
						}
					};
				action.mustHaveOneRowSelected(table);
				bot.add(new JButton(action));
				
				table.addMouseListener(new GenericMouseAdapter<Shuttle1>(sh1)
					{
					@Override
					public void mousePressed(MouseEvent me) {
						if(me.getClickCount()<2) return;
						int i= getObject().table.getSelectedRow();
						if(i==-1 || getObject().table.rowAtPoint(me.getPoint())!=i) return;
						if(!InstanceEditor.this.isEditorsValid()) return;
						InstanceEditor.this.saveToModel();
						SelectRsrcTableModel tm=SelectRsrcTableModel.class.cast(getObject().table.getModel());
						Resource r= tm.elementAt(i);
						installInstancePane(r,getObject().rdfType);
						}
					});
				
				ObjectAction<Shuttle1> action2= new ObjectAction<Shuttle1>(sh1, "New "+shortForm(tm.getRdfTypeInRange()))
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(!InstanceEditor.this.isEditorsValid()) return;
						Resource r= askNewURI(InstanceEditor.this,
								"Give me a new URI for a new "+shortForm(getObject().rdfType),
								getObject().rdfType);
						if(r==null) return;
						
						InstanceEditor.this.saveToModel();
						log().debug("create links from "+r+" to "+getSubject());
						SelectRsrcTableModel tm=SelectRsrcTableModel.class.cast(getObject().table.getModel());
						getModel().add(InstanceEditor.this.getSubject(),tm.getPredicate(),r);
						if(tm.getReversePredicate()!=null)
							{
							getModel().add(r,tm.getReversePredicate(),InstanceEditor.this.getSubject());
							}
						
						createInstance(r, getObject().rdfType);
						installInstancePane(r, getObject().rdfType);
						}
					};
				bot.add(new JButton(action2));
				}
			SwingUtils.setFontDeep(p, new Font("Dialog",Font.PLAIN,9));
			return p;
			}
		
		/** createTable */
		protected JComponent createTable(
				ResourceTableModel tm,
				Property predicate,
				Resource targetRDFType
				)
				{
				JPanel p= new JPanel(new BorderLayout());
				p.setPreferredSize(new Dimension(100,getIconSize()*4));
				p.setBorder(new TitledBorder(shortForm(predicate)));
				JTable table= new JTable(tm);
				table.setShowVerticalLines(false);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				RDFTableCellRenderer render= new RDFTableCellRenderer();
				
				for(int i=0;i< table.getColumnModel().getColumnCount();++i)
					{
					if(Icon.class.equals(tm.getColumnClass(i)))
						{
						table.setRowHeight(SciFOAF.this.getIconSize());
						continue;
						}
					table.getColumnModel().getColumn(i).setCellRenderer(render);
					}
				p.add(new JScrollPane(table), BorderLayout.CENTER);
				JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
				p.add(bot,BorderLayout.SOUTH);
				
				
					{
					class Shuttle1 { JTable table; Resource rdfType;}
					Shuttle1 sh1= new Shuttle1();
					sh1.table = table;
					sh1.rdfType = targetRDFType;
					ConstrainedAction<Shuttle1> action= new ConstrainedAction<Shuttle1>(sh1,"View")
						{
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent ae)
							{
							if(!InstanceEditor.this.isEditorsValid()) return;
							InstanceEditor.this.saveToModel();
							int i= getObject().table.getSelectedRow();
							if(i==-1) return;
							log().debug(getObject().table.getModel().getClass());
							ResourceTableModel tm=ResourceTableModel.class.cast(getObject().table.getModel());
							Resource r= tm.elementAt(i);
							installInstancePane(r,getObject().rdfType);
							}
						};
					action.mustHaveOneRowSelected(table);
					bot.add(new JButton(action));
					
					table.addMouseListener(new  GenericMouseAdapter<Shuttle1>(sh1)
						{
						@Override
						public void mousePressed(MouseEvent me) {
							if(me.getClickCount()<2) return;
							int i= getObject().table.getSelectedRow();
							if(i==-1 || getObject().table.rowAtPoint(me.getPoint())!=i) return;
							if(!InstanceEditor.this.isEditorsValid()) return;
							InstanceEditor.this.saveToModel();
							SelectRsrcTableModel tm=SelectRsrcTableModel.class.cast(getObject().table.getModel());
							Resource r= tm.elementAt(i);
							installInstancePane(r,getObject().rdfType);
							}
						});
					
					
					class Shuttle2 { Property predicate; Resource rdfType;}
					Shuttle2 sh2= new Shuttle2();
					sh2.predicate= predicate;
					sh2.rdfType=targetRDFType;
					
					ObjectAction<Shuttle2> action2= new ObjectAction<Shuttle2>(sh2, "New "+shortForm(targetRDFType))
						{
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent arg0) {
							if(!InstanceEditor.this.isEditorsValid()) return;
							Resource r= askNewURI(InstanceEditor.this,
									"Give me a new URI for a new "+shortForm(getObject().rdfType),
									getObject().rdfType);
							if(r==null) return;
							InstanceEditor.this.saveToModel();
							createInstance(r, getObject().rdfType);
							getModel().add(getSubject(),getObject().predicate,r);
							installInstancePane(r, getObject().rdfType);
							}
						};
					bot.add(new JButton(action2));
					}
				SwingUtils.setFontDeep(p, new Font("Dialog",Font.PLAIN,9));
				return p;
				}
			
		/** create a simple table Table. Just option 'edit' is allowed */
		protected JComponent createTable(
				ResourceTableModel tm,
				Resource targetRDFType
				)
				{
				log().debug("creating simple table from "+shortForm(targetRDFType));
				JPanel p= new JPanel(new BorderLayout());
				p.setPreferredSize(new Dimension(100,getIconSize()*4));
				p.setBorder(new TitledBorder(shortForm(targetRDFType)));
				JTable table= new JTable(tm);
				table.setShowVerticalLines(false);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				RDFTableCellRenderer render= new RDFTableCellRenderer();
				
				for(int i=0;i< table.getColumnModel().getColumnCount();++i)
					{
					if(Icon.class.equals(tm.getColumnClass(i)))
						{
						table.setRowHeight(SciFOAF.this.getIconSize());
						continue;
						}
					table.getColumnModel().getColumn(i).setCellRenderer(render);
					}
				p.add(new JScrollPane(table), BorderLayout.CENTER);
				JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
				p.add(bot,BorderLayout.SOUTH);
				
				
					{
					class Shuttle1 { JTable table; Resource rdfType;}
					Shuttle1 sh1= new Shuttle1();
					sh1.table = table;
					sh1.rdfType = targetRDFType;
					ConstrainedAction<Shuttle1> action= new ConstrainedAction<Shuttle1>(sh1,"View")
						{
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent ae)
							{
							if(!InstanceEditor.this.isEditorsValid()) return;
							InstanceEditor.this.saveToModel();
							int i= getObject().table.getSelectedRow();
							if(i==-1) return;
							log().debug(getObject().table.getModel().getClass());
							ResourceTableModel tm=ResourceTableModel.class.cast(getObject().table.getModel());
							Resource r= tm.elementAt(i);
							installInstancePane(r,getObject().rdfType);
							}
						};
					action.mustHaveOneRowSelected(table);
					bot.add(new JButton(action));
					
					table.addMouseListener(new  GenericMouseAdapter<Shuttle1>(sh1)
						{
						@Override
						public void mousePressed(MouseEvent me) {
							if(me.getClickCount()<2) return;
							int i= getObject().table.getSelectedRow();
							if(i==-1 || getObject().table.rowAtPoint(me.getPoint())!=i) return;
							if(!InstanceEditor.this.isEditorsValid()) return;
							InstanceEditor.this.saveToModel();
							SelectRsrcTableModel tm=SelectRsrcTableModel.class.cast(getObject().table.getModel());
							Resource r= tm.elementAt(i);
							installInstancePane(r,getObject().rdfType);
							}
						});

					}
				SwingUtils.setFontDeep(p, new Font("Dialog",Font.PLAIN,9));
				return p;
				}	
		
		}
	
	
	/** ImageEditor */
	private class ImageEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		private ImageIcon icn=null;
		public ImageEditor(Resource subject)
			{
			super(subject);
			JPanel grid= new JPanel(new GridLayout(1,0,1,1));
			this.add(grid,BorderLayout.CENTER);
			JPanel left= new JPanel(new GridLayout(2,0,1,1));
			grid.add(left);
			JLabel label=null;
			try 
				{
				this.icn= new ImageIcon(new URL(getSubject().getURI()),getSubject().getURI());
				label=new JLabel(this.icn);
				label.setOpaque(true);
				label.setBackground(Color.BLACK);
				}
			catch(Exception err)
				{
				label= new JLabel(getSubject().getURI(),JLabel.CENTER);
				label.setFont(new Font("Helvetica",Font.BOLD,14));
				label.setForeground(Color.RED);
				log().warn("problem with image"+ getSubject(),err);
				}
			left.add(new JScrollPane(label));
			
			JPanel pane2= new JPanel(new InputLayout());
			left.add(pane2);
			TextRDFEditor ed=addInputField(pane2,FOAF.name);
			ed=addInputField(pane2,Image.width); ed.getTextField().setEnabled(false);
			ed=addInputField(pane2,Image.height);ed.getTextField().setEnabled(false);
			ed=addInputField(pane2,DC.description);
			addRDFField(pane2, DC.subject, new LiteralListEditor());
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			grid.add(right);
			PersonTableModel tm=new PersonTableModel();
			JComponent c=createSelectTable(shortForm(FOAF.depicts),
					new SelectRsrcTableModel(tm,subject,FOAF.depicts,FOAF.depiction,FOAF.Person)
					);
			right.add(c);
			}
		
		@Override
		protected void saveToModel()
			{
			log().debug("save 2 model for image");
			super.saveToModel();
			JenaUtils.remove(getModel(), this.getSubject(), Image.width, null);
			JenaUtils.remove(getModel(), this.getSubject(), Image.height, null);
			log().debug(""+(this.icn!=null)+" "+ this.icn.getImageLoadStatus()+"="+MediaTracker.COMPLETE);
			if(this.icn!=null && this.icn.getImageLoadStatus()==MediaTracker.COMPLETE)
				{
				int height=this.icn.getImage().getHeight(null);
				int width=this.icn.getImage().getWidth(null);
				log().debug(height+" "+width);
				if(height>0 && width>0)
					{
					getModel().add(getSubject(),Image.height,String.valueOf(height));
					getModel().add(getSubject(),Image.width,String.valueOf(width));
					}
				}
			}
		
		}
	
	/** PlaceEditor */
	private class PlaceEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;

		public PlaceEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, DC.title);
			addInputField(left, Geo.lon);
			addInputField(left, Geo.lat);
			addInputField(left, Geo.country);
			addInputField(left, Geo.postalCode);
			addInputField(left, Geo.adminCode1);
			addInputField(left, Geo.adminName1);
			addInputField(left, Geo.adminCode2);
			addInputField(left, Geo.adminName2);
			}
		}
	
	/** PlaceEditor */
	private class ArticleEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;

		public ArticleEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			TextRDFEditor ed= addInputField(left, DC.title); ed.getTextField().setEditable(false);
			ed= addInputField(left, DC.date);  ed.getTextField().setEditable(false);
			ed= addInputField(left, BIBO.volume); ed.getTextField().setEditable(false);
			ed= addInputField(left, BIBO.issue); ed.getTextField().setEditable(false);
			ed= addInputField(left, BIBO.pages); ed.getTextField().setEditable(false);
			ed= addInputField(left, BIBO.pmid); ed.getTextField().setEditable(false);
			ed= addInputField(left, BIBO.doi); ed.getTextField().setEditable(false);
			ed= addInputField(left, DC.description);
			
			
			
			
			//find journal
			NodeIterator iter=getModel().listObjectsOfProperty(subject,DCTerms.isPartOf);
			while(iter.hasNext())
				{
				RDFNode n= iter.nextNode();
				if(!n.isResource()) continue;
				Resource journal = Resource.class.cast(n);
				if(!getModel().contains(journal,RDF.type,BIBO.Journal)) continue;

				
				left.add(new JLabel("Journal",JLabel.RIGHT));
				JPanel journalPane= new JPanel(new InputLayout());
				journalPane.setBorder(new TitledBorder("Journal Info."));
				left.add(journalPane);
				for(Property pred: new Property[]{
						DC.title,
						BIBO.shortTitle,
						BIBO.issn})
					{
					journalPane.add(new JLabel(shortForm(pred),JLabel.RIGHT));
					JTextField f= new JTextField(JenaUtils.getString(getModel(), journal, pred,""));
					f.setEditable(false);
					journalPane.add(f);
					}
				
				break;
				}
			iter.close();
			addRDFField(left, DC.subject, new LiteralListEditor());
			
			
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			pane.add(right);
			
				{
				PersonTableModel tm= new PersonTableModel();
					
				right.add(createSelectTable(
					shortForm(FOAF.maker),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.maker,FOAF.made,FOAF.Person)
					)
					);
				}
			
			
			}
		}
	
	
	/** PlaceEditor */
	private class JournalEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;

		public JournalEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			//Journal should be created from an article, set all fields disabled
			TextRDFEditor ed=addInputField(left, DC.title);
			ed.getTextField().setEnabled(false);
			ed=addInputField(left, BIBO.shortTitle);
			ed.getTextField().setEnabled(false);
			ed=addInputField(left, BIBO.issn);
			ed.getTextField().setEnabled(false);
			
			
			JPanel right= new JPanel(new BorderLayout());
			pane.add(right);
			HashSet<Resource> set= new HashSet<Resource>();
			ResIterator iter= getModel().listResourcesWithProperty(DCTerms.isPartOf,subject);
			while(iter.hasNext())
				{
				Resource r= iter.nextResource();
				if(!getModel().contains(r,RDF.type,BIBO.Article)) continue;
				set.add(r);
				}
			iter.close();
			
			right.add( createTable(new ArticleTableModel(set), BIBO.Article) );
			}
		}
	
	/** OnlineAccountEditor */
	private class OnlineAccountEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		OnlineAccountEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new InputLayout());
			this.add(pane,BorderLayout.CENTER);
			addResourceField(pane, FOAF.accountServiceHomepage);
			addInputField(pane, FOAF.accountName);
			}
		
		}
	/** EventEditor */
	private class EventEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		public EventEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, DC.title);
			addInputField(left, DC.date).addPattern(DATE_PATTERN);
			addInputField(left, DC.description);
			addRDFField(left, DC.subject, new LiteralListEditor());
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			pane.add(right);
			
				{
				PersonTableModel tm= new PersonTableModel();
					
				right.add(createSelectTable(
					shortForm(Event.agent),
					new SelectRsrcTableModel(tm,getSubject(),Event.agent,Event.isAgentIn,FOAF.Person)
					)
					);
				}
			
				{
				PlaceTableModel tm= new PlaceTableModel();
				right.add(createSelectTable(
					shortForm(Event.place),
					new SelectRsrcTableModel(tm,getSubject(),Event.place,null,Geo.Place)
					)
					);
				}
				
				
				{
				EventTableModel tm= new EventTableModel();
				tm.removeElement(this.getSubject());
				right.add(createSelectTable(
					shortForm(Event.sub_event),
					new SelectRsrcTableModel(tm,getSubject(),Event.sub_event,null,Event.Event)
					)
					);
				}
			}
		}	
	/** GroupEditor */
	private class GroupEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		public GroupEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, FOAF.name);
			addResourceField(left, FOAF.homepage);
			addInputField(left, DC.description);
			addRDFField(left, FOAF.based_near,new ComboRDFEditor(Geo.Place));
			
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			pane.add(right);
			
				{
				PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
					
				right.add(createSelectTable(
					shortForm(FOAF.member),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.member,null,FOAF.Person)
					)
					);
				}
			
			
			}
		}
	
	/** OrganizationEditor */
	private class OrganizationEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		public OrganizationEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, FOAF.name);
			addResourceField(left, FOAF.homepage);
			addInputField(left, DC.description);
			addRDFField(left, FOAF.based_near,new ComboRDFEditor(Geo.Place));
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			pane.add(right);
			
				{
				GroupTableModel tm= new GroupTableModel();
					
				right.add(createSelectTable(
					shortForm(FOAF.member),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.member,null,FOAF.Group)
					)
					);
				}
			
			
			}
		}
	
	/** DOAP editor */
	private class ProjectEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		public ProjectEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			TextRDFEditor ed;
			addInputField(left, DOAP.name);
			ed=addInputField(left, DOAP.created); ed.addPattern(DATE_PATTERN);
			addInputField(left, DOAP.description);
			addInputField(left, DOAP.shortdesc);
			addResourceField(left, DOAP.homepage);
			
			}
		}
	
	
	/** PersonEditor */
	private class PersonEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;

		public PersonEditor(Resource subject)
			{
			super(subject);
			JPanel pane= new JPanel(new GridLayout(0,3,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, FOAF.title);
			addInputField(left, FOAF.name);
			addInputField(left, FOAF.firstName);
			addInputField(left, FOAF.family_name);
			addInputField(left, FOAF.givenname);
			addInputField(left, FOAF.surname);
			addInputField(left, FOAF.birthday).addPattern(DATE_PATTERN);
			addRDFField(left, FOAF.weblog, new URLListEditor());
			
			
			
			addRDFField(left,FOAF.mbox,new ResourceListEditor()
				{
				@Override
				protected void cleanModel() {
					super.cleanModel();
					JenaUtils.remove(getModel(), getSubject(), FOAF.mbox_sha1sum, null);
					}
				
				@Override
				protected void saveNode(String uri)
					{
					log().info(uri+" "+SHA1.isImplemented());
					if(uri==null) return;
					super.saveNode(uri);
					if(SHA1.isImplemented())
						{
						String sha1= SHA1.encrypt(uri);
						log().info(sha1);
						getModel().add(
								getSubject(),
								FOAF.mbox_sha1sum,
								getModel().createLiteral(sha1)
								);
						
						}
					}
				
				@Override
				protected boolean accept(String arg0)
					{
					if(!arg0.startsWith("mailto:"))
						{
						JOptionPane.showMessageDialog(PersonEditor.this, "Mail shoudl be entered as an uri starting with mailto:");
						return false;
						}
					if(!arg0.substring(7).matches("[0-9a-z\\-\\.A-Z]+@[0-9a-z\\-\\.A-Z]+"))
						{
						JOptionPane.showMessageDialog(PersonEditor.this, "Doesn't look like an e-mail");
						return false;
						}
					return super.accept(arg0);
					}
				});
			
			left= new JPanel(new InputLayout());
			pane.add(left);
			LiteralListEditor sha1= new LiteralListEditor();
			sha1.disable();
			addRDFField(left, FOAF.mbox_sha1sum, sha1);
			
			
			addResourceField(left, FOAF.homepage);
			addResourceField(left, FOAF.schoolHomepage);
			addResourceField(left, FOAF.workInfoHomepage);
			addRDFField(left, FOAF.interest, new URLListEditor());
			addRDFField(left, FOAF.based_near,new ComboRDFEditor(Geo.Place));
			addResourceField(left, FOAF.geekcode);
			addResourceField(left, FOAF.myersBriggs);
			addResourceField(left, FOAF.dnaChecksum);
			/*
			 * test for anonymous resource
			AnonymousResource adrs=new AnonymousResource(VCARD.ADRTYPES);
			adrs.setProperty(VCARD.AGENT);
			adrs.setSubject(subject);
			this.editors.add(adrs);
			left.add(new JLabel("Adresses:",JLabel.RIGHT));
			
			JPanel input2= new JPanel(new InputLayout());
			input2.setBorder(new TitledBorder("Hello world"));
			adrs.getEditorsList().addInputField(input2,VCARD.TITLE);
			adrs.getEditorsList().addInputField(input2,VCARD.ROLE);
			adrs.getEditorsList().addInputField(input2,VCARD.TEL);
			left.add(input2);*/
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			
			
				{
				PersonTableModel tm= new PersonTableModel();
				tm.removeElement(getSubject());
					
				right.add(createSelectTable(
					shortForm(FOAF.knows),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.knows,FOAF.knows,FOAF.Person)
					)
					);
				}
			
				
				{
				Set<Resource> set= listResourceHavingRDFType(getSubject(),FOAF.holdsAccount,null,FOAF.OnlineAccount);
				OnlineAccountTableModel tm= new OnlineAccountTableModel(set);
					
				right.add(createTable(
					tm,
					FOAF.holdsAccount,
					FOAF.OnlineAccount)
					);
				}
			
				
				{
				ImageTableModel tm= new ImageTableModel();
					
				right.add(createSelectTable(
					shortForm(FOAF.depiction),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.depiction,FOAF.depicts,FOAF.Image)
					)
					);
				}
				
				{
				ArticleTableModel tm= new ArticleTableModel();
					
				right.add(createSelectTable(
					shortForm(FOAF.made),
					new SelectRsrcTableModel(tm,getSubject(),FOAF.made,FOAF.maker,BIBO.Article)
					)
					);
				}
				
				{
				EventTableModel tm= new EventTableModel();
				right.add(createSelectTable(
						shortForm(Event.isAgentIn),
						new SelectRsrcTableModel(tm,getSubject(),Event.isAgentIn,Event.agent,Event.Event)
						)
						);
				}
			
			for(Property proj:new Property[]{FOAF.pastProject,FOAF.currentProject})
				{
				ProjectTableModel tm= new ProjectTableModel();
				right.add(createSelectTable(
						shortForm(proj),
						new SelectRsrcTableModel(tm,getSubject(),proj,null,DOAP.Project)
						)
						);
				}
				
			pane.add(new JScrollPane(right));
			}
		}
	
	/** ResourceTableModel */
	private abstract class ResourceTableModel
		extends GenericTableModel<Resource>
		{
		private static final long serialVersionUID = 1L;
		
		private class ResourceComparator
			implements Comparator<Resource>
			{
			private Property properties[];
			
			ResourceComparator(Property properties[])
				{
				this.properties=properties;
				}
			
			@Override
			public int compare(Resource a1, Resource a2)
				{
				for(Property p:this.properties)
					{
					RDFNode n1=getProperty(a1,p);
					RDFNode n2=getProperty(a2,p);
					int i= JenaUtils.compare(n1, n2);
					if(i!=0) return i;
					}	
				return 0;
				}
			
			private RDFNode getProperty(Resource subject,Property p)
				{
				RDFNode rez=null;
				NodeIterator iter=getModel().listObjectsOfProperty(subject, p);
				while(iter.hasNext())
					{
					rez=iter.nextNode();
					break;
					}
				iter.close();
				return rez;
				}
			
			}
		
		
		protected ResourceTableModel(ResIterator iter)
			{
			while(iter.hasNext())
				{
				Resource r= iter.nextResource();
				if(r.isAnon()) continue;
				this.addElement(r);
				}
			iter.close();	
			}
		
		protected ResourceTableModel(Collection<Resource> col)
			{
			for(Resource r: col)
				{
				this.addElement(r);
				}
			}
		
		protected void sort(Property...properties )
			{
			this.sort(new ResourceComparator(properties));
			}
		
		/** returns a literal string from the given subject/prop */
		protected String getString(Resource subject,Property prop)
			{
			String value=null;
			StmtIterator iter=getModel().listStatements(subject, prop,(RDFNode)null);
			while(iter.hasNext())
				{
				Statement stmt=iter.nextStatement();
				if(!stmt.getObject().isLiteral()) continue;
				value=stmt.getLiteral().getString();
				}
			iter.close();
			return value;
			}
		/** returns a literal string from the given subject/prop */
		protected String getURI(Resource subject,Property prop)
			{
			String value=null;
			StmtIterator iter=getModel().listStatements(subject, prop,(RDFNode)null);
			while(iter.hasNext())
				{
				Statement stmt=iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				value=stmt.getResource().getURI();
				}
			iter.close();
			return value;
			}
		}
	/**
	 *  Project TableModel
	 */
	private class ProjectTableModel
		extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		ProjectTableModel()
			{
			super(getModel().listSubjectsWithProperty(RDF.type, DOAP.Project));
			sort(DOAP.name,DC.title,DOAP.shortdesc);
			}
		@Override
		public int getColumnCount() {
			return 3;
			}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
			}
		@Override
		public String getColumnName(int column) {
			switch(column)
				{
				case 0: return "URI";
				case 1: return shortForm(DOAP.name);
				case 2: return shortForm(DOAP.shortdesc);
				}
			return null;
			}
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0: return subject.getURI();
				case 1: return getString(subject, DOAP.name);
				case 2: return getString(subject, DOAP.shortdesc);
				}
			return null;
			}
		}
	
	/**
	 * specialized table model for Foaf:Person
	 * @author pierre
	 *
	 */
	private class PersonTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		private HashMap<Resource, Icon> rsrc2icn;
		
		PersonTableModel()
			{
			this(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
			}
		
		PersonTableModel(ResIterator iter)
			{
			super(iter);
			this.rsrc2icn=new HashMap<Resource, Icon>(this.getElementCount());
			for(int i=0;i< this.getElementCount();++i)
				{
				ImageIcon icn=SciFOAF.this.findDepiction(this.elementAt(i));
				if(icn==null) continue;
				this.rsrc2icn.put(this.elementAt(i), icn);
				}
			sort(FOAF.family_name,FOAF.firstName,FOAF.name);
			}
		@Override
		public int getColumnCount() {
			return 5;
			}
		
		@Override
		public Class<?> getColumnClass(int col)
			{
			if(col==0) return Icon.class;
			return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0: return "Icon";
				case 1 : return "Id";
				case 2 : return "foaf:name";
				case 3 : return "foaf:firstName";
				case 4 : return "foaf:family_name";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return this.rsrc2icn.get(subject);
				case 1 : return subject.getURI();
				case 2 : return getString(subject,FOAF.name);
				case 3 : return getString(subject,FOAF.firstName);
				case 4 : return getString(subject,FOAF.family_name);
				}
			return null;
			}
		}
	

	
	/** EventTableModel */
	private class EventTableModel  extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		EventTableModel()
			{
			super(getModel().listSubjectsWithProperty(RDF.type, Event.Event));
			sort(DC.title,DC.date);
			}
		
		
		@Override
		public String getColumnName(int col) {
			switch(col)
				{
				case 0: return "Id";
				case 1 : return "Name";
				case 2 : return "Date";
				}
			return null;
			}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
				case 1 : return getString(subject,DC.title);
				case 2 : return getString(subject,DC.date);
				}
			return null;
			}

		@Override
		public int getColumnCount()
			{
			return 3;
			}
		
		}
	
	/** GroupTableModel */
	private class GroupTableModel  extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		GroupTableModel()
			{
			super(getModel().listSubjectsWithProperty(RDF.type, FOAF.Group));
			sort(FOAF.name);
			}
		
		
		@Override
		public String getColumnName(int col) {
			switch(col)
				{
				case 0: return "Id";
				case 1 : return "Name";
				}
			return null;
			}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
				case 1 : return getString(subject,FOAF.name);
				}
			return null;
			}

		@Override
		public int getColumnCount()
			{
			return 2;
			}
		
		}
	
	
	
	
	private class OrganizationTableModel  extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		OrganizationTableModel()
			{
			super(getModel().listSubjectsWithProperty(RDF.type, FOAF.Organization));
			sort(FOAF.name);
			}
		
		
		@Override
		public String getColumnName(int col) {
			switch(col)
				{
				case 0: return "Id";
				case 1 : return "Name";
				}
			return null;
			}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
				case 1 : return getString(subject,FOAF.name);
				}
			return null;
			}
	
		@Override
		public int getColumnCount()
			{
			return 2;
			}
		
		}
	
	/** 
	 * ImageTableModel
	 */
	private class ImageTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
		private HashMap<Resource, Icon> rsrc2icon;
		
		ImageTableModel()
			{
			this(getModel().listSubjectsWithProperty(RDF.type, FOAF.Image));
			}
		
		ImageTableModel(ResIterator iter)
			{
			super(iter);
			this.rsrc2icon= new HashMap<Resource, Icon>( this.getElementCount());
			for(int i=0;i< this.getElementCount();++i)
				{
				Icon icn= findDepiction(this.elementAt(i));
				if(icn!=null) this.rsrc2icon.put(this.elementAt(i),icn);
				}
			sort(DC.title);
			}
		@Override
		public int getColumnCount() {
			return 2;
			}
		
		@Override
		public Class<?> getColumnClass(int col)
			{
			if(col==0) return Icon.class;
			return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0 : return "Icon";
				case 1 : return "Id";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0: return this.rsrc2icon.get(subject);
				case 1 : return subject.getURI();
				}
			return null;
			}
		}	
	
	/** 
	 * PlaceTableModel
	 */
	private class PlaceTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
	
		PlaceTableModel(ResIterator iter)
			{
			super(iter);
			sort(Geo.country,DC.title);
			}
		
		public PlaceTableModel() {
			this(getModel().listSubjectsWithProperty(RDF.type, Geo.Place));
			}
		
		@Override
		public int getColumnCount() {
			return 4;
			}
		
		@Override
		public Class<?> getColumnClass(int col) {
				return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0 : return "Title";
				case 1 : return "Country";
				case 2 : return "Long";
				case 3 : return "Lat";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return getString(subject,DC.title);
				case 1 : return getString(subject,Geo.country);
				case 2 : return getString(subject,Geo.lon);
				case 3 : return getString(subject,Geo.lat);
				}
			return null;
			}
		}
	
	
	/** 
	 * ArticleTableModel
	 */
	private class ArticleTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
	
		ArticleTableModel()
			{
			this(getModel().listSubjectsWithProperty(RDF.type, BIBO.Article));
			}
		
		ArticleTableModel(ResIterator iter)
			{
			super(iter);
			sort(DC.date,DC.title);
			}
		
		ArticleTableModel(Collection<Resource> set)
			{
			super(set);
			}
		
		@Override
		public int getColumnCount() {
			return 8;
			}
		
		@Override
		public Class<?> getColumnClass(int col) {
				return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0 : return "Date";
				case 1 : return "Journal";
				case 2 : return "Title";
				case 3 : return "Vol.";
				case 4 : return "Issue";
				case 5 : return "pp.";
				case 6 : return "PMID";
				case 7 : return "doi";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return getString(subject,DC.date);
				case 1 :
					{
					String s=null;
					NodeIterator iter=getModel().listObjectsOfProperty(subject, DCTerms.isPartOf);
					while(iter.hasNext())
						{
						RDFNode n= iter.nextNode();
						if(!n.isResource()) continue;
						Resource r= Resource.class.cast(n);
						if(!getModel().contains(r,RDF.type,BIBO.Journal)) continue;
						s= JenaUtils.getString(getModel(), r, DC.title,null);
						if(s!=null) break;
						}
					iter.close();
					return s;
					}
				case 2 : return getString(subject,DC.title);
				case 3 : return getString(subject,BIBO.volume);
				case 4 : return getString(subject,BIBO.issue);
				case 5 : return getString(subject,BIBO.pages);
				case 6 : return getString(subject,BIBO.pmid);
				case 7 : return getString(subject,BIBO.doi);
				default: return null;
				}
			}
		}
	
	/** 
	 * JournalTableModel
	 */
	private class JournalTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
	
		JournalTableModel(ResIterator iter)
			{
			super(iter);
			sort(DC.title);
			}
		@Override
		public int getColumnCount() {
			return 3;
			}
		
		@Override
		public Class<?> getColumnClass(int col) {
				return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0 : return "Title";
				case 1 : return "Abbr.";
				case 2 : return "ISSN";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return getString(subject,DC.title);
				case 1 : return getString(subject,BIBO.shortTitle);
				case 2 : return getString(subject,BIBO.issn);
				default: return null;
				}
			}
		}
	
	
	/** 
	 * Article
	 */
	private class OnlineAccountTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
	
		OnlineAccountTableModel(Set<Resource> set)
			{
			super(set);
			sort(FOAF.accountServiceHomepage,FOAF.accountName);
			}
		@Override
		public int getColumnCount() {
			return 3;
			}
		
		@Override
		public Class<?> getColumnClass(int col) {
				return String.class;
			}
		
		@Override
		public String getColumnName(int col)
			{
			switch(col)
				{
				case 0 : return "URL";
				case 1 : return shortForm(FOAF.accountServiceHomepage);
				case 2 : return shortForm(FOAF.accountName);
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
				case 1 : return getURI(subject,FOAF.accountServiceHomepage);
				case 2 : return getString(subject,FOAF.accountName);
				default: return null;
				}
			}
		}
	
	
	/**
	 * SelectRsrcTableModel
	 *
	 */
	private class SelectRsrcTableModel extends AbstractTableModel
		{
		private static final long serialVersionUID = 1L;
		/** delegate model */
		private ResourceTableModel delegate;
		/** which resource are selected in this table ? */
		private HashMap<Resource, Boolean> selected;
		/** subject of this table */
		private Resource subject;
		/** property observed */
		private Property property;
		/** reverse property observed */
		private Property reverseProperty;
		/** rdfType in range */
		private Resource rangeRdfType;
		
		SelectRsrcTableModel(
				ResourceTableModel delegate,
				Resource subject,
				Property property,
				Property reverseProperty,
				Resource rangeRdfType
				)
			{
			this.subject=subject;
			this.property=property;
			this.reverseProperty=reverseProperty;
			this.delegate=delegate;
			this.rangeRdfType=rangeRdfType;
			this.selected=new HashMap<Resource, Boolean>(delegate.getRowCount());
			// loop over the model to see if statements exist
			for(int i=0;i< delegate.getRowCount();++i)
				{
				Resource r= delegate.elementAt(i);
				this.selected.put(r,Boolean.FALSE);
				//test if statements (s,p,o) exists
				if( (this.property!=null && getModel().contains(this.subject,this.property,r)) ||
					(this.reverseProperty!=null && getModel().contains(r,this.reverseProperty,subject)))
					{
					log().debug("exists("+subject+","+property+","+r+")");
					this.selected.put(r,Boolean.TRUE);
					}
				}
			}
		Property getPredicate() { return this.property;}
		Property getReversePredicate() { return this.reverseProperty;}
		Resource getRdfTypeInRange() { return this.rangeRdfType;}
		
		public Resource elementAt(int row)
			{
			return getDelegate().elementAt(row);
			}
		
		public ResourceTableModel getDelegate() { return this.delegate;}
		
		@Override
		public int getColumnCount() { return getDelegate().getColumnCount()+1; }
		
		@Override
		public int getRowCount() {
			return getDelegate().getRowCount();
			}
		
		@Override
		public String getColumnName(int col) {
			if(col==0) return "Sel.";
			return getDelegate().getColumnName(col-1);
			}
		
		@Override
		public Class<?> getColumnClass(int col) {
			if(col==0) return Boolean.class;
			return getDelegate().getColumnClass(col-1);
			}
		
		@Override
		public Object getValueAt(int row, int col) {
			if(col==0) return  this.selected.get(getDelegate().elementAt(row));
			return getDelegate().getValueAt(row, col-1);
			}
		@Override
		public boolean isCellEditable(int row, int col) {
			return col==0;
			}
		@Override
		public void setValueAt(Object val, int row, int col)
			{
			Boolean b=Boolean.class.cast(val);
			this.selected.put(getDelegate().elementAt(row), b);
			
			
			if(this.property!=null) getModel().remove(this.subject, property,getDelegate().elementAt(row));
			if(this.reverseProperty!=null) getModel().remove(getDelegate().elementAt(row),this.reverseProperty,this.subject);
			if(b)
				{
				log().debug("create("+this.subject+","+this.property+","+getDelegate().elementAt(row)+")");
				if(this.property!=null) getModel().add(this.subject, property,getDelegate().elementAt(row));
				if(this.reverseProperty!=null) getModel().add(getDelegate().elementAt(row),this.reverseProperty,this.subject);
				}
			super.fireTableCellUpdated(row, col);
			}
		}
	
	
	
	/** SciFOAF */
	public SciFOAF(Model model,File file)
		{ 
		super("SciFOAF");
		this.rdfModel = model;
		this.file=file;
		this.tmpDirectory= new File(System.getProperty("java.io.tmpdir"),"scifoaf");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel mainPane= new JPanel(new BorderLayout());
		this.setContentPane(mainPane);
		this.contentPane = new JPanel(new BorderLayout(5,5));
		mainPane.add(this.contentPane,BorderLayout.CENTER);
		
		
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowOpened(WindowEvent e) {
				SwingUtils.center(e.getWindow(),50);
				}
			
			@Override
				public void windowClosed(WindowEvent e) {
					doMenuSave(SciFOAF.this.file);
					}
			
			});
		
		JMenuBar bar= new JMenuBar();
		setJMenuBar(bar);
		JMenu menu= new JMenu("File");
		bar.add(menu);
		
		menu.add(new AbstractAction("About...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				JOptionPane.showMessageDialog(SciFOAF.this,
					"<html><body><div align=\"center\">"+
					"<h1>SciFOAF</h1>"+
					"<h3>2008 Pierre Lindenbaum 2008</h3><h6>"+
					Compilation.getLabel()+
					"</h6></div></body></html>",
					"About",JOptionPane.PLAIN_MESSAGE,null);
				}
			});
		
		menu.add(new JSeparator());
		
		menu.add(new JMenuItem(new AbstractAction("Import")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				JFileChooser fc= new JFileChooser(SciFOAF.this.file);
				fc.setFileFilter(new FileExtensionFilter("RDF files","xml","rdf","foaf","txt"));
				if(fc.showOpenDialog(SciFOAF.this)!=JFileChooser.APPROVE_OPTION) return;
				File f= fc.getSelectedFile();
				try {
					Model model2= ModelFactory.createDefaultModel();
					FileReader fr= new FileReader(f);
					model2.read(fr,f.toURI().toURL().toString());
					fr.close();
					getModel().add(model2);
					} 
				catch (Exception e) {
					ThrowablePane.show(SciFOAF.this	, e);
					}
				}
			}));
		
		menu.add(new JMenuItem(new AbstractAction("Save As...")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				doMenuSaveAs();
				}
			}));
		
		menu.add(new JMenuItem(new AbstractAction("Save")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				doMenuSave(SciFOAF.this.file);
				}
			}));
		menu.add(new JMenuItem(new AbstractAction("Export as KML")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				doMenuExportKML();
				}
			}));
		
		AbstractAction action;
		menu.add(new JMenuItem(action=new AbstractAction("Export as XHTML")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				doMenuExportXHTML();
				}
			}));
		//need to work on this...
		action.setEnabled(false);
		
		
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new AbstractAction("Quit")
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae)
				{
				setVisible(false);
				dispose();
				}
			}));
		
		
		installMainPane();
		}
	
	private org.apache.commons.logging.Log log()
		{
		return this._log;
		}
	
	
	private Set<Resource> listResourceHavingRDFType(
			Resource subject,
			Property predicate,
			RDFNode object,
			Resource havingRDFType
			)
			{
			HashSet<Resource> set= new HashSet<Resource>();
			StmtIterator iter= getModel().listStatements(subject, predicate, object);
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				if(!getModel().contains(stmt.getResource(),RDF.type,havingRDFType)) continue;
				set.add(stmt.getResource());
				}
			iter.close();
			return set;
			}
	
	
	private Model getModel()
		{
		return this.rdfModel;
		}
	
	private File getTmpDirectory()
		{
		return this.tmpDirectory;
		}
	
	private int getIconSize() 
		{
		return this.iconSize;
		}
	
	private String shortForm(Resource rsrc)
		{
		if(rsrc==null) return null;
		return getModel().shortForm(rsrc.getURI());
		}
	
	private void installComponent(JComponent c)
		{
		this.contentPane.removeAll();
		this.contentPane.add(c);
		this.contentPane.validate();
		this.contentPane.repaint();
		}
	
	private JComponent createMainTab(
			ResourceTableModel rtm,
			Resource rdfType
			)
		{
		String title= shortForm(rdfType);
		/**  pane */
		JPanel pane= new JPanel(new BorderLayout());
		JTable table= new JTable(rtm);
		table.setFont(new Font("Dialog",Font.BOLD,24));
		table.setRowHeight(26);
		
		for(int i=0;i< rtm.getColumnCount();++i)
			{
			if(Icon.class.equals(rtm.getColumnClass(i)))
				{
				table.setRowHeight(SciFOAF.this.getIconSize());
				break;
				}
			}
		
		table.setShowVerticalLines(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pane.add(new JScrollPane(table));
		
		JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEADING));
		bottom.add(new JLabel(String.valueOf(rtm.getRowCount())+" items."));
		pane.add(bottom,BorderLayout.SOUTH);
		
		JPanel top= new JPanel(new FlowLayout(FlowLayout.LEADING));
		pane.add(top,BorderLayout.NORTH);
		
		class Shuttle1 { JTable table; Resource rdfType;}
		Shuttle1 sh1=new Shuttle1();
		sh1.table = table;
		sh1.rdfType = rdfType;
		
		
		ConstrainedAction<Shuttle1> action=new ConstrainedAction<Shuttle1>(sh1,"Edit "+title)
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent ae)
				{
				int i= getObject().table.getSelectedRow();
				if(i==-1) return;
				Resource r= ResourceTableModel.class.cast(getObject().table.getModel()).elementAt(i);
				installInstancePane(r, getObject().rdfType);
				}
			};
			
		action.mustHaveOneRowSelected(table);	
		
		top.add(new JButton(action));
		
		table.addMouseListener(new GenericMouseAdapter<Shuttle1>(sh1)
			{
			@Override
			public void mouseClicked(MouseEvent me)
				{
				if(me.getClickCount()<2) return;
				JTable t=JTable.class.cast(me.getComponent());
				int i= t.rowAtPoint(me.getPoint());
				if(i==-1 || i!=t.getSelectedRow()) return;
				Resource r= ResourceTableModel.class.cast(getObject().table.getModel()).elementAt(i);
				installInstancePane(r, getObject().rdfType);
				}
			});
		
		top.add(new JButton(new ObjectAction<Resource>(rdfType,"New "+title)
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae)
				{
				Resource uri=askNewURI(SciFOAF.this, "Enter a new URI for this "+shortForm(getObject()),getObject());
				if(uri==null) return;
				createInstance(uri, getObject());
				installInstancePane(uri,getObject());
				}
			}));
		
		if(BIBO.Article.equals(rdfType))
			{
			top.add(new JButton(new AbstractAction("Load Batch")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent ae)
					{
					String s="";
					while(true)
						{
						JTextArea area= new JTextArea(s,25,10);
						if(JOptionPane.showConfirmDialog(SciFOAF.this,new JScrollPane(area),"Insert PMID",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null)!=JOptionPane.OK_OPTION) return;
						s=area.getText();
						String ss[]=s.split("[\n\r ]");
						for(String pmid:ss)
							{
							try {
								new Integer(pmid);
								if(getModel().containsResource(getModel().createResource(BIBO.PUBMED_PREFIX+pmid)))
									{
									log().debug("pmid "+pmid+" already in model");
									}
								loadPMID(pmid);
								}
							catch (NumberFormatException e)
								{
								JOptionPane.showMessageDialog(SciFOAF.this, "Illegal pmid: "+pmid);
								continue;
								}
							}
						break;
						}
					installMainPane();
					}
				}));
			}
		
		
		return pane;
		}
	
	/** install main panel */
	private void installMainPane()
		{
		JTabbedPane tabbed= new JTabbedPane();
			
			{
			/** person pane */
			PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
			tabbed.addTab(shortForm(FOAF.Person),createMainTab(tm,FOAF.Person));
			}
			
			{
			/** image pane */
			ImageTableModel tm= new ImageTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Image));
			tabbed.addTab(shortForm(FOAF.Image),createMainTab(tm,FOAF.Image));
			}
			
			{
			/** place pane */
			PlaceTableModel tm= new PlaceTableModel();
			tabbed.addTab(shortForm(Geo.Place),createMainTab(tm,Geo.Place));
			}
			
			{
			/** article pane */
			ArticleTableModel tm= new ArticleTableModel();
			tabbed.addTab(shortForm(BIBO.Article),createMainTab(tm, BIBO.Article));
			}
			
			{
			/** journals pane */
			JournalTableModel tm= new JournalTableModel(getModel().listSubjectsWithProperty(RDF.type, BIBO.Journal));
			tabbed.addTab(shortForm(BIBO.Journal),createMainTab(tm, BIBO.Journal));
			}
				
			{
			/** group pane */
			GroupTableModel tm= new GroupTableModel();
			tabbed.addTab(shortForm(FOAF.Group),createMainTab(tm, FOAF.Group));
			}
			
			{
			/** org pane */
			OrganizationTableModel tm= new OrganizationTableModel();
			tabbed.addTab(shortForm(FOAF.Organization),createMainTab(tm, FOAF.Organization));
			}
			
			{
			/** event pane */
			EventTableModel tm= new EventTableModel();
			tabbed.addTab(shortForm(Event.Event),createMainTab(tm, Event.Event));
			}
			
			{
			/** project pane */
			ProjectTableModel tm= new ProjectTableModel();
			tabbed.addTab(shortForm(DOAP.Project),createMainTab(tm, DOAP.Project));
			}
			
		installComponent(tabbed);
		}
	
	/**  @return first child element with givek name */
	private Element firstOf(Element root,String name)
		{
		if(root==null) return null;
		log().debug("searching "+name+" in "+root.getNodeName());
		for(Node n1=root.getFirstChild();n1!=null;n1=n1.getNextSibling())
			{
			if(n1.getNodeType()==Node.ELEMENT_NODE &&
			   n1.getNodeName().equals(name))
				{
				return Element.class.cast(n1);
				}
			}
		log().debug("searching "+name+" in "+root.getNodeName()+" returns null");
		return null;
		}
	
	/** add XML text node */
	private void addXML(Resource r,Property property,Element node)
		{
		if(node==null) return;
		String s= node.getTextContent().trim();
		if(s.length()==0) return ;
		getModel().add(r,property,s);
		}
	
	/** load PMID */
	private Resource loadPMID(String pmid)
		{
		Resource subject= getModel().createResource(BIBO.PUBMED_PREFIX+pmid);
		try {
			DocumentBuilderFactory f= DocumentBuilderFactory.newInstance();
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(true);
			f.setIgnoringElementContentWhitespace(true);
			f.setValidating(false);
			f.setXIncludeAware(false);
			f.setCoalescing(true);
			f.setNamespaceAware(false);
			DocumentBuilder builder=f.newDocumentBuilder();
			Document dom= builder.parse("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+pmid+"&retmode=xml&rettype=abstract");
			Element root= dom.getDocumentElement();
			if(root==null) return subject;
			Element PubmedArticle = firstOf(root,"PubmedArticle");
			Element  MedlineCitation =  firstOf(PubmedArticle,"MedlineCitation");
			Element Article =  firstOf(MedlineCitation,"Article");
			Element Journal =  firstOf(Article,"Journal");
			Element ISSN = firstOf(Journal,"ISSN");
			
			Element JournalIssue = firstOf(Journal,"JournalIssue");
			
			Element Volume = firstOf(JournalIssue,"Volume");
			addXML(subject,BIBO.volume,Volume);
			
			Element Issue = firstOf(JournalIssue,"Issue");
			addXML(subject,BIBO.issue,Issue);
			
			Element PubDate = firstOf(JournalIssue,"PubDate");
			//Element PubYear=null;
			StringBuilder sb= new StringBuilder();
			for(Node n1=(PubDate==null?null:PubDate.getFirstChild());
				n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				//if(n1.getNodeName().equals("Year")) PubYear=Element.class.cast(n1);
				sb.append(" ");
				sb.append(n1.getTextContent());
				}
			if(sb.toString().trim().length()>0)
				{
				getModel().add(subject,DC.date,sb.toString().trim());
				}
			
			
			
			Resource journalResource=null;
			if(ISSN!=null)
				{
				//create journal
				journalResource= getModel().createResource(WORLD_CAT_PREFIX+ISSN.getTextContent().trim());
				getModel().add(journalResource,RDF.type,BIBO.Journal);
				Element JournalTitle = firstOf(Journal,"Title");
				Element ISOAbbreviation= firstOf(Journal,"ISOAbbreviation");
				addXML(journalResource, DC.title, JournalTitle);
				addXML(journalResource,BIBO.shortTitle,ISOAbbreviation);
				addXML(journalResource, BIBO.issn, ISSN);
				//link article to its journal
				getModel().add(subject,DCTerms.isPartOf,journalResource);
				}
			
			
			
			Element ArticleTitle = firstOf(Article,"ArticleTitle");
			addXML(subject,DC.title,ArticleTitle);
			Element Pagination = firstOf(Article,"Pagination");
			Element MedlinePgn = firstOf(Pagination,"MedlinePgn");
			addXML(subject,BIBO.pages,MedlinePgn);
			
			Element MeshHeadingList = firstOf(MedlineCitation,"MeshHeadingList");
			for(Node n1=(MeshHeadingList==null?null:MeshHeadingList.getFirstChild());
				n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!n1.getNodeName().equals("MeshHeading"))continue;
				Element DescriptorName = firstOf(Element.class.cast(n1),"DescriptorName");
				addXML(subject,DC.subject,DescriptorName);
				}
			
			Element  PubmedData =  firstOf(PubmedArticle,"PubmedData");
			Element ArticleIdList =  firstOf(PubmedData,"ArticleIdList");
			for(Node n1=(ArticleIdList==null?null:ArticleIdList.getFirstChild());
				n1!=null;n1=n1.getNextSibling())
				{
				if(n1.getNodeType()!=Node.ELEMENT_NODE) continue;
				if(!n1.getNodeName().equals("ArticleId"))continue;
				Attr att= (Attr)n1.getAttributes().getNamedItem("IdType");
				if(att==null)
					{
					log().debug("Cannot find @IdType in "+n1.getNodeName());
					continue;
					}
				if(att.getValue().equals("doi"))
					{
					getModel().add(subject,BIBO.doi,n1.getTextContent().trim());
					}
				}
			
		} catch (Exception e) {
			ThrowablePane.show(SciFOAF.this, e);
			}
		getModel().add(subject,BIBO.pmid,pmid);
		getModel().add(subject,RDF.type,BIBO.Article);
		return subject;
		}
	
	
	/** create instance */
	private void createInstance(Resource subject,Resource rdfType)
		{
		if(rdfType.equals(FOAF.Image))
			{
			getModel().add(subject,FOAF.name,subject.getURI());
			}
		else if(rdfType.equals(BIBO.Article) && subject.getURI().startsWith(BIBO.PUBMED_PREFIX))
			{
			String pmid=subject.getURI().substring(BIBO.PUBMED_PREFIX.length()).trim();
			loadPMID(pmid);
			}
		getModel().add(subject,RDF.type,rdfType);
		if(!BIBO.Article.equals(rdfType))
			{
			getModel().add(subject,DC.date,TimeUtils.toYYYYMMDD('-'));
			}
		}
	
	/**  installInstancePane */
	private void installInstancePane(
			Resource subject,			
			Resource rdfType
			)
		{
		InstanceEditor ed=null;
		if(rdfType.equals(FOAF.Person))
			{
			ed= new PersonEditor(subject);
			}
		else if(rdfType.equals(FOAF.Image))
			{
			ed= new ImageEditor(subject);
			}
		else if(rdfType.equals(Geo.Place))
			{
			ed= new PlaceEditor(subject);
			}
		else if(rdfType.equals(FOAF.OnlineAccount))
			{
			ed= new OnlineAccountEditor(subject);
			}
		else if(rdfType.equals(BIBO.Article))
			{
			ed= new ArticleEditor(subject);
			}
		else if(rdfType.equals(BIBO.Journal))
			{
			ed= new JournalEditor(subject);
			}
		else if(rdfType.equals(FOAF.Group))
			{
			ed= new GroupEditor(subject);
			}
		else if(rdfType.equals(FOAF.Organization))
			{
			ed= new OrganizationEditor(subject);
			}
		else if(rdfType.equals(Event.Event))
			{
			ed= new EventEditor(subject);
			}
		else if(rdfType.equals(DOAP.Project))
			{
			ed= new ProjectEditor(subject);
			}
		else
			{
			JOptionPane.showMessageDialog(this, "unknown rdf:type "+rdfType,"Error",JOptionPane.ERROR_MESSAGE,null);
			}
		if(ed==null) return;
		ed.loadFromModel();
		EditHistory event= new EditHistory();
		event.subject=subject;
		event.rdfType=rdfType;
		this.history.push(event);
		installComponent(ed);
		}
	
	/** ask URI */
	private Resource askNewURI(Component owner,String title,Resource rdfType)
		{
		if(rdfType.equals(Geo.Place))
			{
			return askGeoName(owner);
			}
		if( rdfType.equals(FOAF.Image) ||
			rdfType.equals(Event.Event))
			{
			return JenaUtils.askNewURL(getModel(), owner, "enter a new URL for this "+shortForm(rdfType));
			}
		else if( rdfType.equals(FOAF.Person))
				{
				return JenaUtils.askNewURL(getModel(), owner,
					"<html><body>"	+
					"<p>Enter an URL for this <b>foaf:Person<b></p>"+
					"<p>If possible , the URL should be URL <br/>where this person stores his/her FOAF profile<b></p>"+
					"</body></html>"
					);
				}
		else if(rdfType.equals(BIBO.Journal))
			{
			JOptionPane.showMessageDialog(owner, "Journal are created from articles");
			return null;
			}
		else if(rdfType.equals(BIBO.Article))
			{
			String pmid="0";
			while(true)
				{
				pmid=JOptionPane.showInputDialog(owner, "Enter a PMID", pmid);
				if(pmid==null) return null;
				pmid=pmid.trim();
				try {
					new Integer(pmid);
					} 
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(owner, "Not an integer:"+pmid,"Error",JOptionPane.ERROR_MESSAGE,null);
					continue;
					}
				Resource r= getModel().createResource(BIBO.PUBMED_PREFIX +pmid);
				if(getModel().containsResource(r))
					{
					JOptionPane.showMessageDialog(owner,
							"Resource already contains "+r.getURI(),
							"Error",JOptionPane.ERROR_MESSAGE,null);
					continue;
					}
				return r;
				}
			}
		else
			{
			return JenaUtils.askNewURI(getModel(), owner, title);
			}
		}
	
	/** @return true if filename was saved as RDF */
	private boolean doMenuSave(File filename)
		{
		if(filename==null)
			{
			doMenuSaveAs();
			return true;
			}
		try {
			log().debug("saving as "+filename);
			FileWriter fw= new FileWriter(filename);
			getModel().write(fw);
			fw.flush();
			fw.close();
		} catch (Exception e)
			{
			log().warn(filename, e);
			ThrowablePane.show(SciFOAF.this,e);
			}
		return true;
		}
	
	private File askFile(File file)
		{
		JFileChooser chooser= new JFileChooser(file);
		if( chooser.showSaveDialog(SciFOAF.this)!=JFileChooser.APPROVE_OPTION) return null;
		File f= chooser.getSelectedFile();
		if(f.exists() &&
			JOptionPane.showConfirmDialog(SciFOAF.this, f.toString()+" exists. Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null)!=JOptionPane.OK_OPTION
			)
			{
			return null;
			}
		return f;
		}
	
	/** find a ImageIcon for the given Resource */
	private ImageIcon findDepiction(Resource subject)
		{
		if(subject==null) return null;
		
		if(getModel().contains(subject,RDF.type,FOAF.Image))
			{
			return getIcon(subject);
			}
		
		ImageIcon icon=null;
		NodeIterator iter= getModel().listObjectsOfProperty(
				subject,
				FOAF.depiction);
		while(iter.hasNext())
			{
			RDFNode n= iter.nextNode();
			if(!n.isResource()) continue;
			Resource r= Resource.class.cast(n);
			if(!getModel().contains(r,RDF.type,FOAF.Image)) continue;
			try {
				icon= getIcon(r);
				if(icon!=null) break;
				break;
				}
			catch (Exception e)
				{
				//ignore
				}
			}
		iter.close();
		return icon;
		}
	
	/** find a ImageIcon for the given Resource */
	private File findDepictionFile(Resource subject)
		{
		if(subject==null) return null;
		
		if(getModel().contains(subject,RDF.type,FOAF.Image))
			{
			return makeIcon(subject);
			}
		
		File icon=null;
		NodeIterator iter= getModel().listObjectsOfProperty(
				subject,
				FOAF.depiction);
		while(iter.hasNext())
			{
			RDFNode n= iter.nextNode();
			if(!n.isResource()) continue;
			Resource r= Resource.class.cast(n);
			if(!getModel().contains(r,RDF.type,FOAF.Image)) continue;
			try {
				icon= makeIcon(r);
				if(icon!=null) break;
				break;
				}
			catch (Exception e)
				{
				//ignore
				}
			}
		iter.close();
		return icon;
		}
	
	/** save menu as ... */
	private void doMenuSaveAs()
		{
		File f=askFile(this.file);
		if(f==null) return;
		if(doMenuSave(f))
			{
			this.file=f;
			}
		}	
	
	private ImageIcon getIcon(Resource url)
		{
		try {
			URL u=new URL(url.getURI());
			File f= makeIcon(u);
			if(f==null) return null;
			return new ImageIcon(f.toString());
		} catch (Exception e) {
			log().warn("cannot get Icon",e);
			return null;
			}
		
		}
	
	
	 private File makeIcon(Resource r)
	 	{
		 try {
				URL u=new URL(r.getURI());
				return makeIcon(u);
			} catch (MalformedURLException e) {
				return null;
				}
	 	}

	
    /** takes as input a freebase image id and save it into this.tmpFolder */
    private File makeIcon(URL url)
		{
    	if(!getTmpDirectory().exists())
    		{
    		if(!getTmpDirectory().mkdir())
    			{
    			log().warn("Cannot create "+getTmpDirectory());
    			return null;
    			}
    		log().debug("Created tmp directory in "+getTmpDirectory());
    		}
    	String id= SHA1.encrypt(url.toString())+".png";
    	File dest= new File(getTmpDirectory(),id);
		
		//ignore if exists
		if(dest.exists()) return dest;
		
		try {
			//load image
			BufferedImage src= ImageIO.read(url);
			//create icon
			BufferedImage img=new BufferedImage(getIconSize(),getIconSize(),BufferedImage.TYPE_INT_RGB);
			Graphics2D g= img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getIconSize(), getIconSize());
	
			
			
			
			//center the icon
			if(src.getWidth()< src.getHeight())
				{
				double ratio= src.getWidth()/(double)src.getHeight();//<0
				int len=  (int)(getIconSize()*ratio);
				int x= (getIconSize()-len)/2;
				int y=0;
				g.drawImage(src,x,y,len,getIconSize(),null);
				}
			else
				{
				double ratio= src.getHeight()/(double)src.getWidth();//<0
				int len=  (int)(getIconSize()*ratio);
				int y= (getIconSize()-len)/2;
				int x=0;
				g.drawImage(src,x,y,getIconSize(),len,null);
				}
			
			g.dispose();
			//save the icon
			ImageIO.write(img, "png", dest);
			//return the newly created icon
			return dest;
			} 
		catch (Exception e) {
			//ignore log().warn("Cannot create icon for "+id+" at url="+url,e);
			return null;
			}
		
		}
	
    private Resource askGeoName(Component owner)
    	{
    	String choices[]={"Using an URI","Using http://www.geonames.org"};
    	int i=JOptionPane.showOptionDialog(owner, "Type of Input", "Question", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
    	if(i==JOptionPane.CLOSED_OPTION) return null;
    	if(i==0)
    		{
    		return JenaUtils.askNewURI(getModel(), owner, "Enter a URI for this place. Best would be a wikipedia URL");
    		}
    	GeoNamePane pane= new GeoNamePane();
    	Dimension d= SciFOAF.this.getSize();
    	if(d.width>100) d.width-=50;
    	if(d.height>100) d.height-=50;
    	pane.setPreferredSize(d);
    	if(JOptionPane.showConfirmDialog(owner, pane,"Search Place",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null)!=JOptionPane.OK_OPTION) return null;
    	GeoNamePane.GeoName place= pane.getSelectedGeoName();
    	if(place==null || place.name.trim().length()==0) return null;
    	String placename=null;
    	try {
    		placename="http://en.wikipedia.org/wiki/"+URLEncoder.encode(place.name.trim().replace(' ', '_'),"UTF-8");
    		}
    	catch (Exception e) {
			throw new Error(e);
			}
    	String s=placename;
    	
    	while(true)
    		{
    		s= JOptionPane.showInputDialog(
    				owner,
    				"Enter a new URI for this place. Best would be a wikipedia URL",
    				s
    				);
    		if(s==null) return null;
    		s=s.trim();
			try {
				URI loc=new URI(s);
				if(!loc.isAbsolute())
					{
					JOptionPane.showMessageDialog(
							owner,
							"URI should be absolute  "+s);
					continue;
					}
				} 
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(owner, "Not a valid URI "+s);
				continue;
				}
 
			Resource r=getModel().createResource(s);
        	if(getModel().containsResource(r))
    			{
    			JOptionPane.showMessageDialog(owner, "Already exists "+place.name);
    			return null;
    			}
        	getModel().add(r,RDF.type,Geo.Place);
        	getModel().add(r,Geo.lat,String.valueOf(place.latitude));
        	getModel().add(r,Geo.lon,String.valueOf(place.longitude));
        	getModel().add(r,Geo.country,String.valueOf(place.country));
        	getModel().add(r,Geo.postalCode,String.valueOf(place.postalCode));
        	getModel().add(r,Geo.adminCode1,String.valueOf(place.adminCode1));
        	getModel().add(r,Geo.adminCode2,String.valueOf(place.adminCode2));
        	getModel().add(r,Geo.adminName1,String.valueOf(place.adminName1));
        	getModel().add(r,Geo.adminName2,String.valueOf(place.adminName2));
        	getModel().add(r,DC.title,String.valueOf(place.name));
        	return r;
    		}
 
    	}
    
	/**  doMenuExportKML */
	private void doMenuExportKML()
		{
		File f=askFile(null);
		if(f==null) return;
		try {
			PrintWriter w= new PrintWriter(new FileWriter(f));
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<kml xmlns=\""+KML.NS+"\">");
			w.println("<Document>");
            w.println("<name>SciFOAF KML</name>");
            
            ResIterator iter1= getModel().listSubjectsWithProperty(RDF.type, Geo.Place);
            while(iter1.hasNext())
            	{
            	Resource place= iter1.nextResource();
            	String longitude=  JenaUtils.getString(getModel(), place, Geo.lon, null);
            	if(longitude==null) continue;
            	String latitude = JenaUtils.getString(getModel(), place, Geo.lat, null);
            	if(latitude==null) continue;
            	
            	ResIterator iter2= getModel().listSubjectsWithProperty(FOAF.based_near, place);
                while(iter2.hasNext())
                	{
                	Resource person= iter2.nextResource();
                	String name=JenaUtils.getString(getModel(), person, FOAF.name);
                	if(name==null) continue;
                	if(!getModel().contains(person,RDF.type,FOAF.Person)) continue;
                    w.println("<Placemark>");
                    w.println("<name>"+XMLUtilities.escape(name)+"</name>");
                    w.print("<description>");
                    w.print(XMLUtilities.escape("<div><a href=\""+XMLUtilities.escape(person.getURI()) +"\">"+name+"</a></div>"));
                    w.println("</description>");
                    w.print("<Point><coordinates>");
                    w.print(XMLUtilities.escape(longitude)+","+XMLUtilities.escape(latitude));
                    w.println("</coordinates></Point>");
                    w.println("</Placemark>");
                	}
                iter2.close();
            	}
            iter1.close();
            w.println("</Document>");
			w.println("</kml>");
			w.flush();
			w.close();
			} 
		catch (Exception e) {
			ThrowablePane.show(SciFOAF.this, e);
			}
		}
	
	/**  doMenuExportXHTML */
	private void doMenuExportXHTML()
		{
		File f=null;
		
		while(true)
			{
			f=askFile(f);
			if(f==null) return;
			if(f.getName().toLowerCase().endsWith(".zip")) break;
			JOptionPane.showMessageDialog(this, "Filename should end with *.zip","Error",JOptionPane.WARNING_MESSAGE,null);
			}
		FileOutputStream fout=null;
		HashSet<File> iconFiles= new HashSet<File>();
		try {
			boolean found=false;
			fout= new FileOutputStream(f);
			ZipOutputStream zout= new ZipOutputStream(fout);
			ZipEntry entry= new ZipEntry("scifoaf/nn.js");
			zout.putNextEntry(entry);
			PrintWriter out= new PrintWriter(new OutputStreamWriter(zout));
			out.println("var network={");
			//save FOAF.Person
			found=false;
			out.println("\"profile\":[");
			ResIterator iter= getModel().listResourcesWithProperty(RDF.type,FOAF.Person);
			while(iter.hasNext())
				{
				Resource r= iter.nextResource();
				if(found) out.print(",");
				found=true;
				File iconFile = findDepictionFile(r);
				
				out.println("{");
				out.println("\"id\":\""+ C.escape(r.getURI())+"\",");
				out.println("\"name\":\""+ C.escape(JenaUtils.getString(getModel(), r, FOAF.name, r.getURI()))+"\",");
				out.println("\"job\":\"\",");
				out.println("\"affiliation\":\"\",");
				out.println("\"www\":\"\",");
				if(iconFile==null)
					{
					out.println("\"img\":\"\",");
					}
				else
					{
					iconFiles.add(iconFile);
					out.println("\"img\":\""+ C.escape(iconFile.getName()) +"\",");
					}
				
				out.println("\"tags\":[],");
				out.println("\"pos\":{\"x\":0,\"y\":0},");
				out.println("\"goal\":{\"x\":0,\"y\":0},");
				out.println("\"g\":null");
				out.println("}");
				}
			iter.close();
			out.println("],");
			
			//save FOAF.Group
			found=false;
			out.println("\"group\":[");
			iter= getModel().listResourcesWithProperty(RDF.type,FOAF.Group);
			while(iter.hasNext())
				{
				Resource r= iter.nextResource();
				if(found) out.print(",");
				found=true;
				out.println("{");
				out.println("\"uri\":\""+ C.escape(r.getURI())+"\",");
				out.println("\"name\":\""+ C.escape(JenaUtils.getString(getModel(), r, FOAF.name, r.getURI()))+"\",");
				out.println("\"members\":[");
				boolean found2=false;
				StmtIterator iter2=getModel().listStatements(r, FOAF.member, (RDFNode)null);
				while(iter2.hasNext())
					{
					Statement stmt= iter2.nextStatement();
					if(!stmt.getObject().isResource()) continue;
					if(found2) out.print(",");
					found2=true;
					out.print("\""+C.escape(stmt.getResource().getURI())+"\"");
					}
				iter2.close();
				out.println("]");
				out.println("}");
				}
			iter.close();
			out.println("],");
			
			//save FOAF.knows
			found=false;
			out.println("\"link\":[");
			HashSet<Couple<Resource>> links= new HashSet<Couple<Resource>>();
			StmtIterator iter3= getModel().listStatements(null,FOAF.knows,(RDFNode)null);
			while(iter3.hasNext())
				{
				Statement stmt= iter3.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				if(!getModel().contains(stmt.getSubject(),RDF.type,FOAF.Person)) continue;
				if(!getModel().contains(stmt.getResource(),RDF.type,FOAF.Person)) continue;
				links.add(new Couple<Resource>(stmt.getSubject(),stmt.getResource()));
				}
			iter3.close();
			
			for(Couple<Resource> c: links)
				{
				if(found) out.println(",");
				found=true;
				out.print("[\""+ C.escape(c.first().getURI())+ "\",\"" +C.escape(c.second().getURI())+"\"]");
				}
			
			out.println("]");
			
			out.println("}");
			out.flush();
			zout.closeEntry();
			
			/* save images */
			for(File icn: iconFiles)
				{
				entry= new ZipEntry("scifoaf/"+icn.getName());
				zout.putNextEntry(entry);
				FileInputStream fin=new FileInputStream(icn);
				IOUtils.copyTo(fin, zout);
				fin.close();
				zout.flush();
				zout.closeEntry();
				}
			
			/* save main page */
			entry= new ZipEntry("scifoaf/index.xml");
			zout.putNextEntry(entry);
			//save xhtml
			out= new PrintWriter(new OutputStreamWriter(zout));
			String html=ResourceUtils.getContent(SciFOAF.class, "network.xml");
			html=html.replaceAll("__HTML_NS__", XHTML.NS)
					.replaceAll("__SVG_NS__", SVG.NS)
					.replaceAll("__XLINK_NS__", XLINK.NS)
					.replaceAll("__ICON_SIZE__",String.valueOf(getIconSize()))
					;
			out.print(html);
			out.flush();
			zout.closeEntry();
			
			
			zout.flush();
			fout.close();
			} 
		catch (Exception e)
			{
			if(f!=null)
				{
				IOUtils.safeClose(fout);
				f.delete();
				}
			ThrowablePane.show(SciFOAF.this, e);
			}
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			int optind=0;
			boolean debug=false;
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
					System.err.println("Usage SciFOAF [options] (rdf.file|<default=~/foaf.rdf>)");
					System.err.println("  -h help (this screen)");
					System.err.println("  -d debug");
					return;
					}
				else if(args[optind].equals("-d"))
					{
					debug=true;
					}
				else if(args[optind].equals("--"))
					{
					++optind;
					break;
					}
				else if(args[optind].startsWith("-"))
					{
					System.err.println("unknown option "+args[optind]);
					}
				else
					{
					break;
					}
				++optind;
				}
			
			File fileIn = null;
			Model model = ModelFactory.createDefaultModel();
			if(optind==args.length)
				{
				fileIn= new File(System.getProperty("user.home", "."),"foaf.rdf");
				if(fileIn.exists())
					{
					model.read(fileIn.toURI().toString());
					}
				else
					{
					System.err.println("Creating a new FOAF profile in "+fileIn);
					
					Resource me= JenaUtils.askNewURI(model, null, "Give yourself an URI");
					if(me==null) return;
					model.add(me,RDF.type,FOAF.Person);
					Resource root = model.createResource(AnonId.create(""));
					model.add(root,RDF.type,FOAF.PersonalProfileDocument);
					model.add(root,FOAF.maker,me);
					model.add(root,FOAF.primaryTopic,me);
					model.add(root,DC.date,TimeUtils.toYYYYMMDD('-'));
					model.add(root,DC.creator,System.getProperty("user.name","me"));
					}
				
				}
			else if(optind+1==args.length)
				{
				fileIn= new File(args[optind]);
				if(fileIn.exists())
					{
					model.read(fileIn.toURI().toString());
					}
				}
			else
				{
				System.err.println("Illegal number of arguments");
				System.exit(-1);
				}
			
			model.setNsPrefix("foaf", FOAF.NS);
			model.setNsPrefix("geo", Geo.NS);
			model.setNsPrefix("bib", BIBO.NS);
			model.setNsPrefix("vcard", VCARD.getURI());
			model.setNsPrefix("dcterms", DCTerms.getURI());
			model.setNsPrefix("img", Image.NS);
			model.setNsPrefix("doap", DOAP.NS);
			model.setNsPrefix("event", Event.NS);
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			SciFOAF app= new SciFOAF(model,fileIn);
			
			
			SwingUtilities.invokeAndWait(new RunnableObject<SciFOAF>(app)
				{
				@Override
				public void run() {
					getObject().setVisible(true);
					}
				});
			
			}
		catch(Throwable err)
			{
			err.printStackTrace();
			}
		}

}
