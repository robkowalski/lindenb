/**
 * 
 */
package org.lindenb.scifoaf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.logging.LogFactory;
import org.lindenb.jena.JenaUtils;
import org.lindenb.jena.vocabulary.FOAF;
import org.lindenb.lang.RunnableObject;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.swing.ConstrainedAction;
import org.lindenb.swing.ObjectAction;
import org.lindenb.swing.SwingUtils;
import org.lindenb.swing.layout.InputLayout;
import org.lindenb.swing.table.GenericTableModel;
import org.lindenb.util.Compilation;
import org.lindenb.util.NamedKey;
import org.lindenb.util.TimeUtils;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;


class Geo
	{
	static final String NS="http://ontology.lindenb.org/geo#";
	private static Model m = ModelFactory.createDefaultModel();
	public static final Resource Place = m.createResource(NS+ "Place" );
	public static final Property lon = m.createProperty(NS, "long" );
	public static final Property lat = m.createProperty(NS, "lat" );
	public static final Property country = m.createProperty(NS, "country" );
	public static final Property placename = m.createProperty(NS, "placename" );
	public static final Property narrower = m.createProperty(NS, "narrower" );
	public static final Property broader = m.createProperty(NS, "broader" );
	}

/**
 * @author SciFOAF
 *
 */
public class SciFOAF extends JFrame
	{
	private static final long serialVersionUID = 1L;
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
	
	
	/**  instance edited */
	private class EditHistory
		{
		Resource subject=null;
		Resource rdfType=null;
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
		public boolean isValid() { return getValidationMessage()==null;}
		public abstract String getValidationMessage();
		public abstract void loadFromModel();
		public abstract void saveToModel();
		}
	
	
	private class ComboRDFEditor
		extends RDFEditor
		{
		private JComboBox combo;
		private ComboRDFEditor(NamedKey<String> keys[])
			{
			this.combo= new JComboBox(keys);
			}
		public JComboBox getComboBox() {
			return combo;
			}
		
		@Override
		public String getValidationMessage() {
			return null;
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
				if(!stmt.getResource().isAnon()) continue;
				String uri=stmt.getResource().getURI();
				boolean found=false;
				for(int i=0;i< comboModel.getSize();++i)
					{
					NamedKey<?> k= (NamedKey<?>)(comboModel.getElementAt(i));
					if(k==null) continue;
					if(k.getId().equals(uri))
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
			Object s= getComboBox().getSelectedItem();
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s!=null) getModel().add(
					getSubject(),
					getProperty(),
					getModel().createResource(((NamedKey<?>)s).getId().toString())
					);
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
			this.tf.setName(getModel().shortForm(prop.getURI()));
			}
		JTextField getTextField() { return this.tf;}
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
				break;
				}
			iter.close();
			}
		
		@Override
		public void saveToModel() {
			
			String s= getTextField().getText().trim();
			log().info("save to model "+this.getClass()+" "+s);
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s.length()!=0)
				{
				log().info("saving ("+getSubject()+","+getProperty()+","+s+")");
				getModel().add(getSubject(),getProperty(),s);
				}
			}
		}
	
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
					value = getModel().shortForm(Resource.class.cast(value).getURI());
					}
				}
			Component c= super.getTableCellRendererComponent(arg0, value, arg2, arg3, arg4, arg5);
			return c;
			}
		}
	
	/** InstanceEditor */
	private class InstanceEditor
		extends JPanel
		{
		private static final long serialVersionUID = 1L;
		protected Vector<RDFEditor> editors= new Vector<RDFEditor>();
		private Resource subject;

		InstanceEditor(Resource subject)
			{
			super(new BorderLayout());
			this.subject=subject;
			JPanel top= new JPanel(new FlowLayout(FlowLayout.CENTER));
			this.add(top,BorderLayout.NORTH);
			JLabel label= new JLabel(getModel().shortForm(subject.getURI()));
			label.setFont(new Font("Helvetica",Font.BOLD,24));
			label.setToolTipText(subject.getURI());
			top.add(label);
			
			JPanel bot= new JPanel(new FlowLayout(FlowLayout.TRAILING));
			this.add(bot,BorderLayout.SOUTH);
			bot.add(new JButton(new AbstractAction("OK")
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					doOKPressed();
					}
				}));
			
			}
		
		public Resource getSubject() {
			return subject;
			}
		
		protected void saveToModel()
			{
			log().info("saving editors to model ");
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
					JOptionPane.showMessageDialog(InstanceEditor.this, msg,"Error",JOptionPane.WARNING_MESSAGE,null);
					return false;
					}
				}
			log().info("isEditorsValid returns true");
			return true;
			}
		
		protected void doOKPressed()
			{
			log().info("OK pressed");
			if(!isEditorsValid()) return;
			saveToModel();
			SciFOAF.this.history.pop();
			if(SciFOAF.this.history.isEmpty())
				{
				installMainPane();
				}
			else
				{
				EditHistory eh=SciFOAF.this.history.peek();
				installInstancePane(
					eh.subject,
					eh.rdfType
					);
				}
			}
		
		private  AbstractTextRDFEditor addRDFField(JComponent input,Property prop,AbstractTextRDFEditor ed)
			{
			JLabel tf= new JLabel(getModel().shortForm(prop.getURI())+":",JTextField.RIGHT);
			input.add(tf);
			tf.setToolTipText(prop.getURI());
			
			ed.setSubject(getSubject());
			ed.setProperty(prop);
			input.add(ed.tf);
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
		
		protected JComponent createTable(
			String title,
			TableModel tm,
			Resource targetRDFType
			)
			{
			JPanel p= new JPanel(new BorderLayout());
			p.setBorder(new TitledBorder(title));
			JTable table= new JTable(tm);
			table.setShowVerticalLines(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			RDFTableCellRenderer render= new RDFTableCellRenderer();
			/** starting from 1 */
			for(int i=1;i< table.getColumnModel().getColumnCount();++i)
				{
				table.getColumnModel().getColumn(i).setCellRenderer(render);
				}
			p.add(new JScrollPane(table), BorderLayout.CENTER);
			JPanel bot= new JPanel(new FlowLayout());
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
						ResourceTableModel tm=ResourceTableModel.class.cast(getObject().table.getModel());
						Resource r= tm.elementAt(i);
						installInstancePane(r,getObject().rdfType);
						}
					};
				action.mustHaveOneRowSelected(table);
				bot.add(new JButton(action));
				
				
				
				ObjectAction<Resource> action2= new ObjectAction<Resource>(targetRDFType, "New "+getModel().shortForm(targetRDFType.getURI()))
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(!InstanceEditor.this.isEditorsValid()) return;
						Resource r= askNewURI(InstanceEditor.this,
								"Give me a new URI for a new "+getModel().shortForm(getObject().getURI()),
								getObject());
						if(r==null) return;
						InstanceEditor.this.saveToModel();
						getModel().add(r,RDF.type,getObject());
						installInstancePane(r, getObject());
						}
					};
				bot.add(new JButton(action2));
				}
			return p;
			}
		
		}
	/** ImageEditor */
	private class ImageEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;
		public ImageEditor(Resource subject)
			{
			super(subject);
			JPanel grid= new JPanel(new GridLayout(0,2,1,1));
			this.add(grid,BorderLayout.CENTER);
			JPanel left= new JPanel(new GridLayout(2,0,1,1));
			grid.add(left);
			JLabel label=null;
			try 
				{
				ImageIcon icn= new ImageIcon(new URL(getSubject().getURI()),getSubject().getURI());
				label=new JLabel(icn);
				}
			catch(Exception err)
				{
				label= new JLabel(getSubject().getURI(),JLabel.CENTER);
				log().warn("problem with image"+ getSubject(),err);
				}
			left.add(new JScrollPane(label));
			
			JPanel pane2= new JPanel(new InputLayout());
			left.add(pane2);
			TextRDFEditor ed=addInputField(pane2,FOAF.knows);
			ed.getTextField().setEnabled(false);
			ed=addInputField(pane2,FOAF.knows);
			ed.getTextField().setEnabled(false);
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			grid.add(pane2);
			
				{
				
				}
			
			}
		}
	
	/** PersonEditor */
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
			addInputField(left, Geo.placename);
			addInputField(left, Geo.lon);
			addInputField(left, Geo.lat);
			addInputField(left, Geo.country);

			
			
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
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, FOAF.name);
			addInputField(left, FOAF.firstName);
			addInputField(left, FOAF.family_name);
			addInputField(left, FOAF.givenname);
			addInputField(left, FOAF.birthday).addPattern(Pattern.compile("\\d\\d\\d\\d(\\-\\d\\d(\\-\\d\\d)?)?"));
			addResourceField(left, FOAF.homepage);
			addResourceField(left, FOAF.schoolHomepage);
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			
			
				{
				PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
				tm.removeElement(getSubject());
					
				right.add(createTable(
					"foaf:knows",
					new SelectRsrcTableModel(tm,getSubject(),FOAF.knows,FOAF.knows),
					FOAF.Person)
					);
				
				
				}
			
			
			pane.add(right);
			}
		}
	
	/** ResourceTableModel */
	private abstract class ResourceTableModel
		extends GenericTableModel<Resource>
		{
		private static final long serialVersionUID = 1L;
		ResourceTableModel(ResIterator iter)
			{
			while(iter.hasNext())
				{
				this.addElement(iter.nextResource());
				}
			iter.close();
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
		}
	
	/**
	 * specialized table model for Foaf:Person
	 * @author pierre
	 *
	 */
	private class PersonTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;

		PersonTableModel(ResIterator iter)
			{
			super(iter);
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
				case 0 : return "Id";
				case 1 : return "foaf:name";
				case 2 : return "foaf:firstName";
				case 3 : return "foaf:family_name";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
				case 1 : return getString(subject,FOAF.name);
				case 2 : return getString(subject,FOAF.firstName);
				case 3 : return getString(subject,FOAF.family_name);
				}
			return null;
			}
		}
	
	/** 
	 * ImageTableModel
	 */
	private class ImageTableModel extends ResourceTableModel
		{
		private static final long serialVersionUID = 1L;
	
		ImageTableModel(ResIterator iter)
			{
			super(iter);
			}
		@Override
		public int getColumnCount() {
			return 1;
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
				case 0 : return "Id";
				}
			return null;
			}
		
		@Override
		public Object getValueOf(Resource subject, int column)
			{
			switch(column)
				{
				case 0 : return subject.getURI();
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
				case 0 : return getString(subject,Geo.placename);
				case 1 : return getString(subject,Geo.country);
				case 2 : return getString(subject,Geo.lon);
				case 3 : return getString(subject,Geo.lat);
				}
			return null;
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
		
		SelectRsrcTableModel(
				ResourceTableModel delegate,
				Resource subject,
				Property property,
				Property reverseProperty
				)
			{
			this.subject=subject;
			this.property=property;
			this.reverseProperty=reverseProperty;
			this.delegate=delegate;
			this.selected=new HashMap<Resource, Boolean>(delegate.getRowCount());
			// loop over the model to see if statements exist
			for(int i=0;i< delegate.getRowCount();++i)
				{
				Resource r= delegate.elementAt(i);
				this.selected.put(r,Boolean.FALSE);
				//test if statements (s,p,o) exists
				if( getModel().contains(subject,property,r) ||
					(reverseProperty!=null || getModel().contains(r,reverseProperty,subject)))
					{
					this.selected.put(r,Boolean.TRUE);
					}
				}
			}
		
		private ResourceTableModel getDelegate() { return this.delegate;}
		
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
			getModel().remove(this.subject, property,getDelegate().elementAt(row));
			if(this.reverseProperty!=null) getModel().remove(getDelegate().elementAt(row),this.reverseProperty,this.subject);
			if(b)
				{
				getModel().add(this.subject, property,getDelegate().elementAt(row));
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
				JOptionPane.showMessageDialog(SciFOAF.this, Compilation.getLabel(),"About",JOptionPane.PLAIN_MESSAGE,null);
				}
			});
		
		menu.add(new JSeparator());
		
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
	
	private Model getModel()
		{
		return this.rdfModel;
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
		String title= getModel().shortForm(rdfType.getURI());
		/** person pane */
		JPanel pane= new JPanel(new BorderLayout());
		JTable table= new JTable(rtm);
		table.setShowVerticalLines(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pane.add(new JScrollPane(table));
		
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
		
		table.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent me)
				{
				if(me.getClickCount()<2) return;
				JTable t=JTable.class.cast(me.getComponent());
				int i= t.rowAtPoint(me.getPoint());
				if(i==-1 || i!=t.getSelectedRow()) return;
				//Resource r= ResourceTableModel.class.cast(getObject().table.getModel()).elementAt(i);
				//installInstancePane(r, getObject().rdfType);
				//TODO
				}
			});
		
		top.add(new JButton(new ObjectAction<Resource>(rdfType,"New "+title)
			{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae)
				{
				Resource uri=askNewURI(SciFOAF.this, "Enter a new URI for this "+getModel().shortForm(getObject().getURI()),getObject());
				if(uri==null) return;
				getModel().add(uri,RDF.type,getObject());
				getModel().add(uri,DC.date,TimeUtils.toYYYYMMDD('-'));
				installInstancePane(uri,getObject());
				}
			}));
		return pane;
		}
	
	
	private void installMainPane()
		{
		JTabbedPane tabbed= new JTabbedPane();
			
			{
			/** person pane */
			PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
			tabbed.addTab("foaf:Person",createMainTab(tm,FOAF.Person));
			}
			
			{
			/** image pane */
			ImageTableModel tm= new ImageTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Image));
			tabbed.addTab("foaf:Image",createMainTab(tm,FOAF.Image));
			}
			
			{
			/** place pane */
			PlaceTableModel tm= new PlaceTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Image));
			tabbed.addTab("geo:Place",createMainTab(tm,Geo.Place));
			}
			
		installComponent(tabbed);
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
		if(rdfType.equals(FOAF.Image))
			{
			return JenaUtils.askNewURL(getModel(), owner, title);
			}
		else
			{
			return JenaUtils.askNewURI(getModel(), owner, title);
			}
		}
	
	
	private boolean doMenuSave(File filename)
		{
		if(filename==null)
			{
			doMenuSaveAs();
			return true;
			}
		try {
			log().info("saving as "+filename);
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
	
	private void doMenuSaveAs()
		{
		JFileChooser chooser= new JFileChooser(this.file);
		if( chooser.showSaveDialog(SciFOAF.this)!=JFileChooser.APPROVE_OPTION) return;
		File f= chooser.getSelectedFile();
		if(f.exists() &&
			JOptionPane.showConfirmDialog(SciFOAF.this, f.toString()+" exists. Overwrite ?","Overwrite ?",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null)!=JOptionPane.OK_OPTION
			)
			{
			return;
			}
		if(doMenuSave(f))
			{
			this.file=f;
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
			while(optind< args.length)
				{
				if(args[optind].equals("-h"))
					{
					System.err.println(Compilation.getLabel());
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
					model.setNsPrefix("foaf", FOAF.NS);
					model.setNsPrefix("geo", Geo.NS);
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
				fileIn= new File(args[optind+1]);
				}
			else
				{
				System.err.println("Illegal number of arguments");
				System.exit(-1);
				}
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
