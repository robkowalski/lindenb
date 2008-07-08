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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
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
	
	/** used to create a chained list of instance editions */
	private class EditHistory
		{
		Resource subject=null;
		Class<? extends InstanceEditor> editorClass=null;
		EditHistory prev=null;
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
	
	
	private abstract class AbstractTextRDFEditor
	 	extends RDFEditor
		{
		/** text field */
		protected JTextField tf= new JTextField(20);
		
		@Override
		public void setProperty(Property prop) {
			super.setProperty(prop);
			this.tf.setName(getModel().shortForm(prop.getURI()));
			}
		
		}
	
	private class UrlRDFEditor extends AbstractTextRDFEditor
		{
		@Override
		public String getValidationMessage() {
			String s= super.tf.getText().trim();
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
				tf.setText(stmt.getResource().getURI());
				break;
				}
			iter.close();
			}
		
		@Override
		public void saveToModel() {
			String s= tf.getText().trim();
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s.length()!=0) getModel().add(
					getSubject(),
					getProperty(),
					getModel().createResource(s)
					);
			}
		
		}
	
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
			tf.setText(b.toString());
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
			String s= this.tf.getText().trim();
			if(s.length()==0) return null;
			for(Pattern pat: this.patterns)
				{
				if(!pat.matcher(s).matches())
					{
					return ""+tf.getName()+" should match regex "+pat.pattern();
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
			tf.setText("");
			StmtIterator iter= getModel().listStatements(getSubject(),getProperty(),RDFNode.class.cast(null));
			while(iter.hasNext())
				{
				Statement stmt= iter.nextStatement();
				if(!stmt.getObject().isLiteral()) continue;
				tf.setText(stmt.getLiteral().getString());
				break;
				}
			iter.close();
			}
		
		@Override
		public void saveToModel() {
			String s= tf.getText().trim();
			JenaUtils.remove(getModel(), getSubject(), getProperty(),null);
			if(s.length()!=0) getModel().add(getSubject(),getProperty(),s);
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
		private EditHistory prevHistory;
		InstanceEditor(Resource subject,EditHistory prevHistory)
			{
			super(new BorderLayout());
			this.subject=subject;
			this.prevHistory=prevHistory;
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
			return true;
			}
		
		protected void doOKPressed()
			{
			if(!isEditorsValid()) return;
			saveToModel();
			
			if(this.prevHistory==null)
				{
				installMainPane();
				}
			else
				{
				installInstancePane(
					this.prevHistory.subject,
					this.prevHistory.editorClass,
					this.prevHistory.prev
					);
				}
			}
		
		private <T extends AbstractTextRDFEditor> AbstractTextRDFEditor addRDFField(JComponent input,Property prop,Class<T> clazz)
			{
			JTextField tf= new JTextField(getModel().shortForm(prop.getURI())+":",JTextField.RIGHT);
			input.add(tf);
			tf.setToolTipText(prop.getURI());
			try {
				AbstractTextRDFEditor ed=clazz.newInstance();
				ed.setSubject(getSubject());
				ed.setProperty(prop);
				input.add(ed.tf);
				return ed;
			} catch (Exception e)
				{
				throw new RuntimeException(e);
				}
			
			}
		
		
		
		protected TextRDFEditor addInputField(JComponent input,Property prop)
			{
			return TextRDFEditor.class.cast(addRDFField(input,prop,TextRDFEditor.class));
			}
		
		protected MultiRDFTextEditor addMultiInputField(JComponent input,Property prop)
			{
			return MultiRDFTextEditor.class.cast(addRDFField(input,prop,MultiRDFTextEditor.class));
			}
		
		protected UrlRDFEditor addResourceField(JComponent input,Property prop)
			{
			return UrlRDFEditor.class.cast(addRDFField(input,prop,UrlRDFEditor.class));
			}
		
		protected JComponent createTable(
			String title,
			ResourceTableModel tm,
			Class<?  extends InstanceEditor> editor,
			Resource targetRDFType
			)
			{
			JPanel p= new JPanel(new BorderLayout());
			p.setBorder(new TitledBorder(title));
			JTable table= new JTable(tm);
			table.setShowVerticalLines(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			RDFTableCellRenderer render= new RDFTableCellRenderer();
			for(int i=0;i< table.getColumnModel().getColumnCount();++i)
				{
				table.getColumnModel().getColumn(i).setCellRenderer(render);
				}
			p.add(new JScrollPane(table), BorderLayout.CENTER);
			JPanel bot= new JPanel(new FlowLayout());
			p.add(bot,BorderLayout.SOUTH);
			
			if(editor!=null)
				{
				class Shuttle1 { JTable table; Class<?  extends InstanceEditor> editorClazz;}
				Shuttle1 sh1= new Shuttle1();
				sh1.table = table;
				sh1.editorClazz = editor;
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
						EditHistory h= new EditHistory();
						h.subject= getSubject();
						h.editorClass= InstanceEditor.this.getClass();
						h.prev= InstanceEditor.this.prevHistory;
						installInstancePane(r, getObject().editorClazz, h);
						}
					};
				action.mustHaveOneRowSelected(table);
				bot.add(new JButton(action));
				
				class Shuttle2 { Resource rdfType; Class<?  extends InstanceEditor> editorClazz;}
				Shuttle2 sh2= new Shuttle2();
				sh2.rdfType = targetRDFType;
				sh2.editorClazz = editor;
				ObjectAction<Shuttle2> action2= new ObjectAction<Shuttle2>(sh2, "New "+getModel().shortForm(targetRDFType.getURI()))
					{
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(!InstanceEditor.this.isEditorsValid()) return;
						Resource r= JenaUtils.askNewURI(getModel(), InstanceEditor.this,
								"Give me a new URI for a new "+getModel().shortForm(getObject().rdfType.getURI()));
						if(r==null) return;
						InstanceEditor.this.saveToModel();
						
						getModel().add(r,RDF.type,getObject().rdfType);
						EditHistory h= new EditHistory();
						h.subject= getSubject();
						h.editorClass= InstanceEditor.this.getClass();
						h.prev= InstanceEditor.this.prevHistory;
						installInstancePane(r, getObject().editorClazz, h);
						}
					};
				bot.add(new JButton(action2));
				}
			return p;
			}
		
		}
	
	/** PersonEditor */
	private class PersonEditor
		extends InstanceEditor
		{
		private static final long serialVersionUID = 1L;

		PersonEditor(Resource subject,EditHistory history)
			{
			super(subject,history);
			JPanel pane= new JPanel(new GridLayout(0,2,1,1));
			this.add(pane,BorderLayout.CENTER);
			JPanel left= new JPanel(new InputLayout());
			pane.add(left);
			addInputField(left, FOAF.name);
			addInputField(left, FOAF.firstName);
			addInputField(left, FOAF.family_name);
			addInputField(left, FOAF.givenname);
			addInputField(left, FOAF.birthday).addPattern(Pattern.compile("[\\d\\d\\d\\d(\\-\\d\\d(\\-\\d\\d)?)?"));
			addResourceField(left, FOAF.homepage);
			addResourceField(left, FOAF.schoolHomepage);
			
			JPanel right= new JPanel(new GridLayout(0,1,1,1));
			
			
				{
				/** foaf knows */
				JPanel p= new JPanel(new BorderLayout());
				PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
				JTable table= new JTable(tm);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				p.add(new JScrollPane(table), BorderLayout.CENTER);
				JPanel bot= new JPanel(new FlowLayout());
				p.add(bot,BorderLayout.SOUTH);
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
	
	private void installMainPane()
		{
		JTabbedPane tabbed= new JTabbedPane();
			
			{
			/** person pane */
			JPanel pane= new JPanel(new BorderLayout());
			tabbed.addTab("foaf:Person", pane);
			PersonTableModel tm= new PersonTableModel(getModel().listSubjectsWithProperty(RDF.type, FOAF.Person));
			JTable table= new JTable(tm);
			table.setShowVerticalLines(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			pane.add(new JScrollPane(table));
			
			JPanel top= new JPanel(new FlowLayout(FlowLayout.LEADING));
			pane.add(top,BorderLayout.NORTH);
			ConstrainedAction<JTable> action=new ConstrainedAction<JTable>(table,"Edit Person")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent ae)
					{
					int i= getObject().getSelectedRow();
					if(i==-1) return;
					Resource r= ResourceTableModel.class.cast(getObject().getModel()).elementAt(i);
					installInstancePane(r, PersonEditor.class, null);
					}
				};
			action.mustHaveOneRowSelected(table);	
			
			top.add(new JButton(action));
			top.add(new JButton(new AbstractAction("New Person")
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent ae)
					{
					Resource uri=JenaUtils.askNewURI(getModel(), SciFOAF.this, "Enter a new URI for this Person");
					if(uri==null) return;
					getModel().add(uri,RDF.type,FOAF.Person);
					getModel().add(uri,DC.date,TimeUtils.toYYYYMMDD('-'));
					installInstancePane(uri,PersonEditor.class,null);
					}
				}));
			}
			
		installComponent(tabbed);
		}
	
	/**  */
	private void installInstancePane(
			Resource subject,			
			Class<? extends InstanceEditor> editorClass,
			EditHistory prev
			)
		{
		try {
			Constructor<? extends InstanceEditor> cst= editorClass.getConstructor(Resource.class,EditHistory.class);
			InstanceEditor ed=cst.newInstance(subject,prev);
			ed.loadFromModel();
			installComponent(ed);
			}
		catch (Exception e)
			{
			e.printStackTrace();
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
			FileWriter fw= new FileWriter(filename);
			getModel().write(fw);
			fw.flush();
			fw.close();
		} catch (Exception e) {
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
