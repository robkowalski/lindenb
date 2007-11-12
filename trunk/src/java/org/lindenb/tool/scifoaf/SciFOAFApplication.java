package org.lindenb.tool.scifoaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;

import org.lindenb.io.PreferredDirectory;
import org.lindenb.lang.RunnableObject;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.sw.model.DerbyModel;
import org.lindenb.sw.model.DerbyModel.CloseableIterator;
import org.lindenb.sw.model.DerbyModel.RDFNode;
import org.lindenb.sw.model.DerbyModel.Statement;
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
import org.lindenb.util.Compilation;
import org.lindenb.util.Debug;
import org.lindenb.util.Observed;


/**
 * SciFOAFApplication
 * @author pierre
 *
 */
public class SciFOAFApplication extends JFrame {
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktopPane;
	private JMenu windowsMenu;
	private Observed<DerbyModel> derbyModel= new Observed<DerbyModel>();
	private AbstractAction menuNewAction;
	private AbstractAction menuOpenAction;
	private AbstractAction menuCloseAction;
	
	/**
	 * any internal frame in the this.desktopPane
	 * @author pierre
	 *
	 */
	private class MyInternalFrame extends JInternalFrame
		{
		private static final long serialVersionUID = 1L;

		MyInternalFrame(String title)
			{
			super(title,true,true,true,true);
			setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			addInternalFrameListener(new InternalFrameAdapter()
				{
				@Override
				public void internalFrameOpened(InternalFrameEvent e) {
					updateMenuWindow();
					}
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					updateMenuWindow();
					}
				});
			if(desktopPane!=null)
				{
				Dimension dim= desktopPane.getSize();
				setBounds(10,10,
						dim.width-20,
						dim.height-20);
				}
			}
		}
	
	private class StatementTableModel extends GenericTableModel<DerbyModel.Statement>
		{
		private static final long serialVersionUID = 1L;
		
		@Override
		public Class<?> getColumnClass(int columnIndex)
			{
			switch(columnIndex)
				{
				case 0: return DerbyModel.Resource.class;
				case 1: return DerbyModel.Resource.class;
				case 2: return DerbyModel.RDFNode.class;
				default:return null;
				}
			}
		
		@Override
		public String getColumnName(int columnIndex)
			{
			switch(columnIndex)
				{
				case 0: return "Subject";
				case 1: return "Predicate";
				case 2: return "Value";
				default:return null;
				}
			}
		
		@Override
		public int getColumnCount() {
			return 3;
			}
		@Override
		public Object getValueOf(Statement object, int columnIndex) {
			switch(columnIndex)
				{
				case 0: return object.getSubject();
				case 1: return object.getPredicate();
				case 2: return object.getValue();
				default:return null;
				}
			}
		}
	
	private class StatementTable extends JTable
		{
		private static final long serialVersionUID = 1L;
		StatementTable()
			{
			super(new StatementTableModel());
			setShowVerticalLines(false);
			setRowHeight(22);
			setFont(new Font("Dialog",Font.BOLD,18));
			
			DefaultTableCellRenderer cr= new DefaultTableCellRenderer()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column)
					{
					value = DerbyModel.Resource.class.cast(value).getShortName();
					Component c= super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
							row, column);
					
					return c;
					}
				};
			cr.setForeground(Color.BLUE);
			getColumnModel().getColumn(0).setCellRenderer(cr);
			getColumnModel().getColumn(1).setCellRenderer(cr);
			
			cr= new DefaultTableCellRenderer()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column)
					{
					DerbyModel.RDFNode n = DerbyModel.RDFNode.class.cast(value);
					value= (n.isLiteral()?
							n.asLiteral().getString()
							:n.asResource().getShortName()
							);
					setForeground(n.isLiteral()?Color.GRAY:Color.BLUE);
					Component c= super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
							row, column);
					
					return c;
					}
				};
			getColumnModel().getColumn(2).setCellRenderer(cr);
			setToolTipText("");
			}
		
		@Override
		public String getToolTipText(MouseEvent event) {
			int r= this.rowAtPoint(event.getPoint());
			if(r==-1) return null;
			if((r=convertRowIndexToModel(r))==-1) return null;
			int c= this.columnAtPoint(event.getPoint());
			if(c==-1) return null;
			if((c=convertColumnIndexToModel(c))==-1) return null;
			Object o= getModel().getValueAt(r, c);
			return o==null?null:o.toString();
			}
		
		public StatementTableModel getStmtTable()
			{
			return StatementTableModel.class.cast(getModel());
			}
		
		public Vector<Statement> getSelectedStatements()
			{
			int indexes[]=getSelectedRows();
			 Vector<Statement> sel= new  Vector<Statement>(indexes.length);
			 for(int i=0;i< indexes.length;++i)
			 	{
				indexes[i]= convertRowIndexToModel( indexes[i]);
				if( indexes[i]==-1) continue;
				Statement stmt= getStmtTable().elementAt(indexes[i]);
				if(stmt==null) continue;
				sel.addElement(stmt);
			 	}
			return sel; 
			}
		
		}
	
	
	private static String _shortName(RDFNode r)
	{
	if(r==null) return "*";
	String s=r.isLiteral()?r.asLiteral().getString():r.asResource().getShortName();
	s=(s.length()>20?s.substring(0,20)+"...":s);
	return r.isLiteral()?"\""+s+"\"":"<"+s+">";
	}
	
	/**
	 * 
	 * @author pierre
	 *
	 */
	private class StatementsFrame extends MyInternalFrame
		{
		private static final long serialVersionUID = 1L;
		
		class SuggestPredicateAction extends ObjectAction<String>
			{
			private static final long serialVersionUID = 1L;
			SuggestPredicateAction(String ns,String prefix,String local)
				{
				super(ns+local,prefix+":"+local);
				}
			@Override
			public void actionPerformed(ActionEvent e)
				{
				if(predicateTextField==null) return;
				predicateTextField.setText(getObject());
				predicateTextField.setCaretPosition(0);
				}
			}
		
		class SuggestValueAction extends ObjectAction<String>
			{
			private static final long serialVersionUID = 1L;
			SuggestValueAction(String ns,String prefix,String local)
				{
				super(ns+local,prefix+":"+local);
				}
			@Override
			public void actionPerformed(ActionEvent e)
				{
				if(valueTextField==null || valueTypeCombo.getSelectedIndex()!=0) return;
				valueTextField.setText(getObject());
				valueTextField.setCaretPosition(0);
				}
			}
		
		
		
		private class DeployStatementAction extends ObjectAction<DerbyModel.RDFNode[]>
			{
			private static final long serialVersionUID = 1L;
			DeployStatementAction(
					DerbyModel.Resource S,
					DerbyModel.Resource P,
					DerbyModel.RDFNode V)
				{
				super(new DerbyModel.RDFNode[]{S,P,V},
						"("+
						_shortName(S)+","+
						_shortName(P)+","+
						_shortName(V)+
						")");
				}
		
		
			private boolean _same(RDFNode n1,RDFNode n2)
				{
				if(n1==null && n2==null) return true;
				if(n1==null && n2!=null) return false;
				if(n1!=null && n2==null) return false;
				return n1.equals(n2);
				}
			
			@Override
			public void actionPerformed(ActionEvent e)
				{
				for(JInternalFrame frames: desktopPane.getAllFrames())
					{
					if(!(frames instanceof StatementsFrame)) continue;
					StatementsFrame cp=StatementsFrame.class.cast(frames);
					if( _same(getObject()[0],cp.subject) &&
						_same(getObject()[1],cp.predicate) &&
						_same(getObject()[2],cp.value)
						)
						{
						cp.moveToFront();
						return;
						}
					
					
					}
				StatementsFrame f= new StatementsFrame(
					DerbyModel.Resource.class.cast(getObject()[0]),
					DerbyModel.Resource.class.cast(getObject()[1]),
					getObject()[2]
					);
				f.setClosable(false);
				f.setTitle(this.getValue(AbstractAction.NAME).toString());
				SciFOAFApplication.this.desktopPane.add(f);
				
				f.setVisible(true);
				}
			}
		
		
		ConstrainedAction<StatementTable> removeStatementAction;
		DerbyModel.Resource subject;
		DerbyModel.Resource predicate;
		DerbyModel.RDFNode value;
		protected StatementTable table;
		private AbstractAction goPrevPageAction;
		private AbstractAction goNextPageAction;
		private Observed<Integer> startPage=new Observed<Integer>(0);
		JTextField subjectTextField;
		JTextField predicateTextField;
		JTextField valueTextField;
		JComboBox valueTypeCombo;
		AbstractAction setAnonymouseNodeAction;
		AbstractAction chooseSubjectAction;
		AbstractAction choosePredicateAction;
		AbstractAction chooseValueAsResourceAction;
		AbstractAction createStatementAction;
		private JMenu exploreMenu;
		StatementsFrame()
			{
			this(null,null,null);	
			}
		
		StatementsFrame(DerbyModel.Resource subject,DerbyModel.Resource predicate,DerbyModel.RDFNode value)
			{
			super("Statements");
			this.subject=subject;
			this.predicate=predicate;
			this.value=value;
			setClosable(true);
			JMenuBar bar= new JMenuBar();
			setJMenuBar(bar);
			JMenu menu= new JMenu("RDF");
			bar.add(menu);
			this.exploreMenu= new JMenu("Browse");
			bar.add(exploreMenu);
			
			
			addInternalFrameListener(new InternalFrameAdapter()
				{
				@Override
				public void internalFrameOpened(InternalFrameEvent e) {
					StatementsFrame.this.fireRDFModelUpdated();
					}
				});
			
			
			JPanel contentPane= new JPanel(new BorderLayout(5,5));
			setContentPane(contentPane);
			contentPane.add(new JScrollPane(this.table= new StatementTable()));
			this.table.addMouseListener(new MouseAdapter()
				{
				@Override
				public void mousePressed(MouseEvent e) {
					if(!e.isPopupTrigger()) return;
					int i=table.rowAtPoint(e.getPoint());
					if(i==-1) return;
					i= table.convertRowIndexToModel(i);
					if(i==-1) return;
					JPopupMenu menu= new JPopupMenu();
					DerbyModel.Statement stmt= table.getStmtTable().elementAt(i);
					for(DeployStatementAction action :getDeployStatementAction(stmt))
						{
						menu.add(new JMenuItem(action));
						}
					menu.show(e.getComponent(), e.getX(), e.getY());
					}
				});
			
			
			
			JPanel pane1= new JPanel(new GridLayout(1,0,0,0));
			contentPane.add(pane1,BorderLayout.NORTH);
			
			JPanel pane2= new JPanel(new InputLayout());
			pane1.add(pane2);
			pane2.add(new JLabel("Subject:",JLabel.RIGHT));
			pane2.add(new JLabel(this.subject==null?"*":this.subject.getShortName()));
			pane2.add(new JLabel("Predicate:",JLabel.RIGHT));
			pane2.add(new JLabel(this.predicate==null?"*":this.predicate.getShortName()));
			pane2.add(new JLabel("Value:",JLabel.RIGHT));
			pane2.add(new JLabel(this.value==null?"*":this.value.isResource()?value.asResource().getURI():value.asLiteral().getString()));
			SwingUtils.setFontSize(pane2,9);
			pane2.setBorder(new LineBorder(Color.GRAY,1));
			
			pane2= new JPanel(new FlowLayout(FlowLayout.LEADING));
			pane1.add(pane2);
			pane2.add(new JLabel("Row Count:",JLabel.RIGHT));
			pane2.add(new JSpinner(new SpinnerNumberModel(100,0,Integer.MAX_VALUE,1)));
			pane2.add(new JButton(new AbstractAction("Reload")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					fireRDFModelUpdated();
					}
				}));
			
			this.goPrevPageAction= new AbstractAction("Prev")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					
					}
				};
			pane2.add(new JButton(this.goPrevPageAction));
			this.goPrevPageAction.setEnabled(false);
			
			
			this.goNextPageAction= new AbstractAction("Next")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					
					}
				};
			pane2.add(new JButton(this.goNextPageAction));
			this.goNextPageAction.setEnabled(false);
			
			this.removeStatementAction= new ConstrainedAction<StatementTable>(this.table,"Remove")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Vector<DerbyModel.Statement> toremove= table.getSelectedStatements();
					if(toremove.isEmpty()) return;
					
					try {getRDFModel().remove(toremove);
					} catch (SQLException er) {
						ThrowablePane.show(table, er);}
					
					fireRDFModelUpdated();
					}
				};
			this.removeStatementAction.setEnabled(false);
			this.removeStatementAction.mustBeSelected(this.table);
			pane2.add(new JButton(removeStatementAction));
			menu.add(new JMenuItem(removeStatementAction));
			
			
			
			this.createStatementAction=new AbstractAction("Create Statement")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					doMenuCreateStatement();
					}
				};
			this.createStatementAction.setEnabled(false);
			
			JPanel bottom= new JPanel(new BorderLayout());
			Font smallFont= new Font("Dialog",Font.PLAIN,10);
			
			this.add(bottom,BorderLayout.SOUTH);
			JPanel pane3= new JPanel(new InputLayout());
			bottom.add(pane3,BorderLayout.CENTER);
			
			if(this.subject==null)
				{
				pane3.add(SwingUtils.withFont(new JLabel("Subject",JLabel.RIGHT),smallFont));
				JPanel pane4= new JPanel(new BorderLayout());
				pane3.add(pane4);
				pane4.add(SwingUtils.withFont(this.subjectTextField=new JTextField(20),smallFont),BorderLayout.CENTER);
				
				JPanel pane5= new JPanel(new FlowLayout(FlowLayout.LEADING));
				pane4.add(pane5,BorderLayout.EAST);
				pane5.add(SwingUtils.withFont(new JButton(setAnonymouseNodeAction=new AbstractAction("AnId")
					{
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							DerbyModel.Resource r=getRDFModel().createResource();
							subjectTextField.setText(r.getURI());
							subjectTextField.setCaretPosition(0);
							} 
						catch (SQLException err)
							{
							ThrowablePane.show(StatementsFrame.this, err);
							}
						}
					}),smallFont));
				
				pane5.add(SwingUtils.withFont(new JButton(chooseSubjectAction=new AbstractAction("Choose")
					{
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						ResourceSelector sel= new ResourceSelector(StatementsFrame.this,0);
						sel.reloadModel();
						if(sel.showDialog()!=ResourceSelector.OK_OPTION|| sel.getSelectedResource()==null) return;
						subjectTextField.setText(sel.getSelectedResource().getURI());
						subjectTextField.setCaretPosition(0);
						}
					}),smallFont));
				}
			
			if(this.predicate==null)
				{
				pane3.add(SwingUtils.withFont(new JLabel("Predicate",JLabel.RIGHT),smallFont));
				JPanel pane4= new JPanel(new BorderLayout());
				pane3.add(pane4);
				pane4.add(SwingUtils.withFont(this.predicateTextField=new JTextField(20),smallFont));
				JPanel pane5= new JPanel(new FlowLayout(FlowLayout.RIGHT));
				pane4.add(pane5,BorderLayout.EAST);
				
				JButton suggestPredicate= new JButton("[...]");
				suggestPredicate.addMouseListener(new MouseAdapter()
					{
					@Override
					public void mousePressed(MouseEvent e)
						{
						if(!predicateTextField.isEnabled()) return;
						JPopupMenu menu= new JPopupMenu();
						makeSuggestPredicateAction(menu,RDF.NS,"rdf","type");
						makeSuggestPredicateAction(menu,DC.NS,"dc","title");
						makeSuggestPredicateAction(menu,DC.NS,"dc","date");
						makeSuggestPredicateAction(menu,FOAF.NS,"foaf","name");
						makeSuggestPredicateAction(menu,FOAF.NS,"foaf","firstname");
						makeSuggestPredicateAction(menu,FOAF.NS,"foaf","family_name");
						menu.show(e.getComponent(), e.getX(), e.getY());
						}
					});
				pane5.add(SwingUtils.withFont(suggestPredicate,smallFont));
				
				pane5.add(SwingUtils.withFont(new JButton(choosePredicateAction=new AbstractAction("Choose")
					{
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						ResourceSelector sel= new ResourceSelector(StatementsFrame.this,1);
						sel.reloadModel();
						if(sel.showDialog()!=ResourceSelector.OK_OPTION|| sel.getSelectedResource()==null) return;
						predicateTextField.setText(sel.getSelectedResource().getURI());
						predicateTextField.setCaretPosition(0);
						}
					}),smallFont));
				
				}
			
			if(this.value==null)
				{
				pane3.add(this.valueTypeCombo=new JComboBox(new String[]{"Resource","Literal"}));
				this.valueTypeCombo.setFont(smallFont);
				JPanel pane4= new JPanel(new BorderLayout());
				pane3.add(pane4);
				pane4.add(SwingUtils.withFont(this.valueTextField=new JTextField(20),smallFont));
				JPanel pane5= new JPanel(new FlowLayout(FlowLayout.RIGHT));
				pane4.add(pane5,BorderLayout.EAST);
				
				JButton suggestValue= new JButton("[...]");
				suggestValue.addMouseListener(new MouseAdapter()
					{
					@Override
					public void mousePressed(MouseEvent e)
						{
						if(!predicateTextField.isEnabled()) return;
						JPopupMenu menu= new JPopupMenu();
						makeSuggestValueAction(menu,FOAF.NS,"foaf","Person");
						makeSuggestValueAction(menu,FOAF.NS,"foaf","Group");
						makeSuggestValueAction(menu,FOAF.NS,"foaf","Image");
						menu.show(e.getComponent(), e.getX(), e.getY());
						}
					});
				pane5.add(SwingUtils.withFont(suggestValue,smallFont));
				
				pane5.add(SwingUtils.withFont(new JButton(chooseValueAsResourceAction=new AbstractAction("Choose")
					{
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e)
						{
						ResourceSelector sel= new ResourceSelector(StatementsFrame.this,0);
						sel.reloadModel();
						if(sel.showDialog()!=ResourceSelector.OK_OPTION|| sel.getSelectedResource()==null) return;
						valueTextField.setText(sel.getSelectedResource().getURI());
						valueTextField.setCaretPosition(0);
						}
					}),smallFont),BorderLayout.EAST);
				
				
				this.valueTypeCombo.addActionListener(new ActionListener()
					{
					@Override
					public void actionPerformed(ActionEvent e) {
						chooseValueAsResourceAction.setEnabled(valueTypeCombo.getSelectedIndex()==0);
						updateControls();	
					}
					});
				
				
				this.valueTextField.getDocument().addDocumentListener(new DocumentAdapter()
					{
					@Override
					public void documentChanged(DocumentEvent e) {
						updateControls();
						}
					});
				}
			
			
		if(this.predicateTextField!=null)
			{
			this.predicateTextField.getDocument().addDocumentListener(new DocumentAdapter()
				{
				@Override
				public void documentChanged(DocumentEvent e) {
					updateControls();
					}
				});
			}
		
			if(this.subject==null)
				{
				this.subjectTextField.getDocument().addDocumentListener(new DocumentAdapter()
					{
					@Override
					public void documentChanged(DocumentEvent e) {
						updateControls();
						}
					});
				}
			
			JPanel pane5= new JPanel(new FlowLayout(FlowLayout.RIGHT));
			bottom.add(pane5,BorderLayout.SOUTH);
			pane5.add(new JButton(createStatementAction));
			menu.add(new JMenuItem(createStatementAction));
			
			
			this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e) {
					exploreMenu.removeAll();
					if(table.getSelectedRowCount()==1)
						{
						for(DerbyModel.Statement stmt:table.getSelectedStatements())
							{
							for(DeployStatementAction a:getDeployStatementAction(stmt))
								{
								exploreMenu.add(new JMenuItem(a));
								}
							if(subjectTextField!=null)
								{
								subjectTextField.setText(stmt.getSubject().getURI());
								subjectTextField.setCaretPosition(0);
								}
							if(predicateTextField!=null)
								{
								predicateTextField.setText(stmt.getPredicate().getURI());
								predicateTextField.setCaretPosition(0);
								}
							if(valueTextField!=null)
								{
								valueTextField.setText(stmt.getValue().isLiteral()?
									stmt.getValue().asLiteral().getString():
									stmt.getValue().asResource().getURI()
									);
								valueTypeCombo.setSelectedIndex(stmt.getValue().isLiteral()?1:0);
								valueTextField.setCaretPosition(0);
								}
							break;
							}
						}
					}
				});
			
			}
		
		private void makeSuggestPredicateAction(JPopupMenu menu,String ns,String prefix,String local)
			{
			menu.add(new JMenuItem(new SuggestPredicateAction(ns,prefix,local)));
			}
		
		private void makeSuggestValueAction(JPopupMenu menu,String ns,String prefix,String local)
			{
			menu.add(new JMenuItem(new SuggestValueAction(ns,prefix,local)));
			}
		
		private void doMenuCreateStatement()
			{
			if(!isActionCreateShouldBeEnabled()) return;
			try
				{
				DerbyModel.Resource S= 
					(this.subject!=null?this.subject:getRDFModel().createResource(this.subjectTextField.getText().trim()));
				
				DerbyModel.Resource P=
					(this.predicate!=null?this.predicate:getRDFModel().createResource(this.predicateTextField.getText().trim()));
				DerbyModel.RDFNode V=this.value;
				
				if(this.value==null)
					{
					switch(this.valueTypeCombo.getSelectedIndex())
						{
						case -1: return;
						case 0:
							{
							V=  getRDFModel().createResource(this.valueTextField.getText().trim());
							break;
							}
						case 1:
							{
							V=  getRDFModel().createLiteral(this.valueTextField.getText().trim());
							break;
							}
						default:return;
						}
					}
				getRDFModel().addStatement(S, P, V);
				if(this.subjectTextField!=null) this.subjectTextField.setText("");
				if(this.predicateTextField!=null) this.predicateTextField.setText("");
				if(this.valueTextField!=null) this.valueTextField.setText("");
				if(valueTypeCombo!=null) this.valueTypeCombo.setSelectedIndex(-1);
				SciFOAFApplication.this.fireRDFModelUpdated();
				}
			catch(Exception err)
				{
				ThrowablePane.show(this, err);
				}
			}
		
		
		private Vector<DeployStatementAction> getDeployStatementAction(DerbyModel.Statement stmt)
			{
			Vector<DeployStatementAction> x= new Vector<DeployStatementAction>(10);
			for(int i=0;i<2;++i)
				for(int j=0;j<2;++j)
					for(int k=0;k<2;++k)
						{
						if(i==0 && j==0 && k==0) continue;
						x.addElement(new DeployStatementAction(
							i==0?null:stmt.getSubject(),
							j==0?null:stmt.getPredicate(),
							k==0?null:stmt.getValue()
							));
						}
			return x;
			}
		
		
		private boolean isActionCreateShouldBeEnabled()
			{
			if(this.subject==null)
				{
				try {
					String s= this.subjectTextField.getText().trim();
					if(s.length()==0 || s.length()>= getRDFModel().getResourceMaxLength()) return false;
					new URI(s.trim());
					} catch (URISyntaxException e) {
					return false;
					}
				}
			
			if(this.predicate==null)
				{
				try {
					String s= this.predicateTextField.getText().trim();
					if(s.length()==0 || s.length()>= getRDFModel().getResourceMaxLength()) return false;
					new URL(s.trim());
					} catch (MalformedURLException e) {
					return false;
					}
				}
			
			if(this.value==null)
				{
				String s= this.valueTextField.getText().trim();
				switch(this.valueTypeCombo.getSelectedIndex())
					{
					case -1: return false;
					case 0:
						{
						try {
							if(s.length()==0 || s.length()>= getRDFModel().getResourceMaxLength()) return false;
							new URI(s);
							} catch (URISyntaxException e) {
							return false;
							}
						break;
						}
					case 1: return s.length()>0  &&   s.length()< getRDFModel().getLiteralMaxLength();
					}
				}
			return true;
			}
		
		
		private void updateControls()
			{
			createStatementAction.setEnabled(isActionCreateShouldBeEnabled());
			}
		
		int getStatementPerPageCount()
			{
			return 100;
			}
		
		public void fireRDFModelUpdated()
			{
			Cursor oldCursor= this.getCursor();
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Vector<DerbyModel.Statement> stmts= new Vector<Statement>();
			try
				{
				
				DerbyModel.CloseableIterator<DerbyModel.Statement> iter=
					getRDFModel().listStatements(
							this.subject,
							this.predicate,
							this.value
							);
				while(iter.hasNext() && stmts.size()< getStatementPerPageCount())
					{
					stmts.add(iter.next());
					}
				iter.close();
				}
			catch(SQLException err)
				{
				ThrowablePane.show(this, err);
				}
			this.table.getStmtTable().clear();
			this.table.getStmtTable().addAll(stmts);
			setCursor(oldCursor);
			}
		}
	
	/**
	 * ResourceSelector
	 * @author pierre
	 *
	 */
	private class ResourceSelector extends SimpleDialog
		{
		private static final long serialVersionUID = 1L;
		private JList list;
		private JTextField regexTextField;
		private ConstrainedAction<ResourceSelector> filterAction;
		private JSpinner spinLimit;
		private DerbyModel.Resource selected=null;
		private int resourceType;
		ResourceSelector(Component owner,int resourceType)
			{
			super(owner,"Resource Selector");
			this.resourceType=resourceType;
			JPanel contentPane= new JPanel(new BorderLayout());
			getContentPane().add(contentPane);
			
			Font small= new Font("Dialog",Font.PLAIN,10);
			JPanel top= new JPanel(new FlowLayout(FlowLayout.LEADING));
			contentPane.add(top,BorderLayout.NORTH);
			top.add(SwingUtils.withFont(new JLabel("Filter:",JLabel.RIGHT),small));
			top.add(this.regexTextField=new JTextField(".*",15));
			this.regexTextField.setFont(small);
			top.add(SwingUtils.withFont(new JLabel("Limit:",JLabel.RIGHT),small));
			top.add(this.spinLimit= new JSpinner(new SpinnerNumberModel(10,1,Integer.MAX_VALUE,1)));
			this.spinLimit.setFont(small);
			top.add(SwingUtils.withFont(new JButton(this.filterAction= new ConstrainedAction<ResourceSelector>(this,"Filter")
				{
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					reloadModel();
					}
				}),small));
			this.filterAction.mustBeARegexPattern(this.regexTextField);
			contentPane.add(new JScrollPane(this.list=new JList(new DefaultListModel())
				{
				private static final long serialVersionUID = 1L;

				@Override
				public String getToolTipText(MouseEvent event) {
					int i=this.locationToIndex(event.getPoint());
					if(i==-1) return null;
					return getModel().getElementAt(i).toString();
					}
				}),BorderLayout.CENTER);
			this.list.setToolTipText("");
			this.list.setFont(new Font("Dialog",Font.BOLD,18));
			this.list.setFixedCellHeight(22);
			this.list.setName("Resources List");
			this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.list.setCellRenderer(new DefaultListCellRenderer()
				{
				private static final long serialVersionUID = 1L;
				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus)
					{
					DerbyModel.Resource r=DerbyModel.Resource.class.cast(value);
					value=r.getShortName();
					Component c= super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					return c;
					}
				});
			this.list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e) {
					selected=DerbyModel.Resource.class.cast(list.getSelectedValue());
					}
				});
			getOKAction().mustHaveOneRowSelected(this.list);
			}
		
				
		public DerbyModel.Resource getSelectedResource()
			{
			return this.selected;
			}
		
		public void reloadModel()
			{
			try {
				int limit= Number.class.cast(this.spinLimit.getValue()).intValue();
				Vector<DerbyModel.Resource> resources= new Vector<DerbyModel.Resource>();
				Pattern pattern= null;
				String regex = this.regexTextField.getText().trim();
				if(regex.length()>0)
					{
					try {
						pattern=Pattern.compile(this.regexTextField.getText(),Pattern.CASE_INSENSITIVE);
					} catch (PatternSyntaxException e)
						{
						Toolkit.getDefaultToolkit().beep();
						pattern=null;
						}
					}
				
				
				CloseableIterator<DerbyModel.Resource> iter;
				if(this.resourceType==0)
					{
					iter= getRDFModel().listSubjects();
					}
				else
					{
					iter = getRDFModel().listProperties();
					}
				while(iter.hasNext())
					{
					DerbyModel.Resource r= iter.next();
					
					if(pattern!=null)
						{
						if( !pattern.matcher(r.getURI()).matches() &&
							!pattern.matcher(r.getShortName()).matches())
							{
							continue;
							}
						}
					resources.addElement(r);
					if(resources.size()>=limit) break;
					}
				iter.close();
				DefaultListModel m = DefaultListModel.class.cast(this.list.getModel());
				m.clear();
				for(DerbyModel.Resource r:resources)
					{
					m.addElement(r);
					}
				} 
			catch (SQLException err)
				{
				}
			}
		
		}
	
	
	public SciFOAFApplication() {
		super("SciFOAF");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosed(WindowEvent e) {
				doMenuClose();
				}
			});
		JMenuBar bar= new JMenuBar();
		setJMenuBar(bar);
		JMenu menu= new JMenu("File");
		bar.add(menu);
		menu.add(this.menuNewAction=new AbstractAction("New")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuNew();
				}
			});
		menu.add(this.menuOpenAction=new AbstractAction("Open")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuOpen();
				}
			});
		menu.add(this.menuCloseAction=new AbstractAction("Close")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuClose();
				}
			});
		this.windowsMenu= new JMenu("Windows");
		bar.add(this.windowsMenu);
		
		JPanel contentPane= new JPanel(new BorderLayout());
		setContentPane(contentPane);
		this.desktopPane= new  JDesktopPane();
		contentPane.add(this.desktopPane,BorderLayout.CENTER);
		
		
		this.derbyModel.addObserver(new Observer()
			{
			@Override
			public void update(Observable o, Object arg) {
				menuNewAction.setEnabled(arg==null);
				menuOpenAction.setEnabled(arg==null);
				menuCloseAction.setEnabled(arg!=null);
				if(arg!=null)
					{
					StatementsFrame f= new StatementsFrame();
					f.setClosable(false);
					f.setTitle("All Statements");
					f.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
					desktopPane.add(f);
					f.setVisible(true);
					}
				fireRDFModelUpdated();
				}
			});
		this.derbyModel.fireValueChanged();
		SwingUtils.center(this, 50);
		}

	private void updateMenuWindow()
		{
		this.windowsMenu.removeAll();
		for(JInternalFrame f: this.desktopPane.getAllFrames())
			{
			this.windowsMenu.add(new JMenuItem(new ObjectAction<JInternalFrame>(f,f.getTitle())
				{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
					{
					if(getObject().isIcon())
						{
						try {getObject().setIcon(false);} catch (PropertyVetoException e1) {}
						}
					getObject().toFront();
					}
				}));
			}
		}
	
	private void doMenuNew()
		{
		JFileChooser chooser=new JFileChooser(PreferredDirectory.getPreferredDirectory());
		if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		File f= chooser.getSelectedFile();
		if(f==null || (f.exists() || JOptionPane.showConfirmDialog(this, f.toString()+" exist. Overwrite.", "Overwrite?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null)!=JOptionPane.OK_OPTION)) return;
		PreferredDirectory.setPreferredDirectory(f);
		try {
			DerbyModel model= new DerbyModel(f);
			
			try {
				if(this.derbyModel.hasValue())
					{
					this.derbyModel.getValue().close();
					}
				} catch (SQLException e) {ThrowablePane.show(this, e); }
			this.derbyModel.setValue(model);
			} 
		catch (Exception e2) {
			ThrowablePane.show(this, e2);
			}
		}
	
	private void doMenuOpen()
		{
		JFileChooser chooser=new JFileChooser(PreferredDirectory.getPreferredDirectory());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		File f= chooser.getSelectedFile();
		if(f==null) return;
		PreferredDirectory.setPreferredDirectory(f);
		try {
			DerbyModel model= new DerbyModel(f);
			try {
				if(this.derbyModel.hasValue())
					{
					this.derbyModel.getValue().close();
					}
				} catch (SQLException e) {ThrowablePane.show(this, e); }
			this.derbyModel.setValue(model);
			} 
		catch (Exception e2) {
			ThrowablePane.show(this, e2);
			}
		}
	
	private void doMenuClose()
		{
		try {
			if(this.derbyModel.hasValue())
				{
				this.derbyModel.getValue().close();
				}
			}
		catch (Exception e) {
			ThrowablePane.show(this, e);
			}
		for(JInternalFrame f:this.desktopPane.getAllFrames())
			{
			f.setVisible(false);
			f.dispose();
			}
		this.desktopPane.removeAll();
		this.derbyModel.setValue(null);
		}
	
	private void fireRDFModelUpdated()
		{
		for(JInternalFrame f: this.desktopPane.getAllFrames())
			{
			if(!StatementsFrame.class.isInstance(f)) continue;
			StatementsFrame.class.cast(f).fireRDFModelUpdated();
			}
		}
	
	private DerbyModel getRDFModel()
		{
		return this.derbyModel.getValue();
		}
	
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
	    	else if(optind!=args.length)
	    		{	
	    		throw new IllegalArgumentException("Bad number of argments.");
				}
	    	
	    	try {
	    		Class.forName(DerbyModel.JDBC_DRIVER_NAME);
	    		} 
	    	catch (ClassNotFoundException e)
	    		{
	    		JOptionPane.showMessageDialog(null,
	    				"Cannot find "+DerbyModel.JDBC_DRIVER_NAME+" in the java $CLASSPATH",
	    				e.getClass().getName(),
	    				JOptionPane.ERROR_MESSAGE,
	    				null
	    				);
	    		return;
	    		}
	    	
	    		
			SwingUtilities.invokeAndWait(new RunnableObject<File>(fileIn)
				{
				@Override
				public void run()
					{
					SciFOAFApplication win= new SciFOAFApplication();
					win.setVisible(true);
					}
				});
				
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

}
