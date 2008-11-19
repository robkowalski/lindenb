package org.lindenb.swapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lindenb.io.PreferredDirectory;
import org.lindenb.sql.SQLUtilities;
import org.lindenb.swing.FileExtensionFilter;
import org.lindenb.swing.SimpleDialog;
import org.lindenb.swing.layout.InputLayout;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;

public class ModelChooser extends SimpleDialog
	{
	private static final long serialVersionUID = 1L;
	private File choosenFile= null;
	private JTextField tfFile= null;
	private JTextField tfJdbcURL= null;
	private JTextField tfJdbcLogin= null;
	private JPasswordField tfJdbcPassword= null;
	private JTextField tfJdbcDriver= null;
	private int selIndex=0;
	private JTabbedPane tabbed;
	
	private static class FileSource
		implements ModelSource
		{
		private File file;
		private Model model;
		FileSource(File file)
			{
			this.file=file;
			}
		@Override
		public Model getModel()
			{
			if(this.model==null)
				{
				this.model=ModelFactory.createDefaultModel();
				this.model.read(FileUtils.toURL(this.file.toString()));
				}
			return this.model;
			}
		@Override
		public void save() throws IOException {
			
			}
		}
	
	private static class JDBCSource
		implements ModelSource
		{
		private String url=null;
		private String login=null;
		private String password=null;
		private String driver=null;
		private Model model;
		
		JDBCSource(String url,String login,String password,String driver)
			{
			this.url=url;
			this.login=login;
			this.password=password;
			this.driver=driver;
			}
		
		
		@Override
		public Model getModel()
			{
			if(this.model==null)
				{
				try {Class.forName(this.driver);} catch(Throwable err) { throw new JenaException(err);}
				IDBConnection conn = new DBConnection(this.url, this.login, this.password, "MySQL");
				ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
				this.model = maker.createDefaultModel();

				}
			return model;
			}
		@Override
		public void save() throws IOException
			{
			
			}
		}
	
	
	public ModelChooser(Component owner)
		{
		super(owner,"Model Chooser");
		this.tabbed = new JTabbedPane();
		super.contentPane.add(tabbed,BorderLayout.CENTER);
		
		JPanel pane= new JPanel(new BorderLayout());
		tabbed.addTab("FILE", pane);
		JPanel pane2= new JPanel(new FlowLayout(FlowLayout.LEADING));
		pane.add(pane2);
		pane2.add(new JLabel("Choose File:",JLabel.RIGHT));
		tfFile=new JTextField(20);
		tfFile.setEditable(false);
		pane2.add(tfFile);
		pane2.add(new JButton(new AbstractAction("Choose")
			{
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser= new JFileChooser(PreferredDirectory.getPreferredDirectory());
				chooser.setFileFilter(new FileExtensionFilter("RDF files","xml","rdf","foaf"));
				if(chooser.showOpenDialog(ModelChooser.this)!=JFileChooser.APPROVE_OPTION) return;
				ModelChooser.this.choosenFile= chooser.getSelectedFile();
				tfFile.setText(choosenFile.getName());
				tfFile.setCaretPosition(0);
				}
			}));
		
		
		
		pane= new JPanel(new BorderLayout());
		tabbed.addTab("JDBC", pane);
		pane2= new JPanel(new InputLayout());
		pane.add(pane2);
		pane2.add(new JLabel("URL:",JLabel.RIGHT));
		pane2.add(this.tfJdbcURL=new JTextField("jdbc:mysql://localhost/test"));
		pane2.add(new JLabel("User:",JLabel.RIGHT));
		pane2.add(this.tfJdbcLogin=new JTextField("anonymous"));
		pane2.add(new JLabel("Password:",JLabel.RIGHT));
		pane2.add(this.tfJdbcPassword=new JPasswordField(""));
		pane2.add(new JLabel("Driver:",JLabel.RIGHT));
		pane2.add(this.tfJdbcDriver=new JTextField("com.mysql.jdbc.Driver"));
		
		pane2= new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		tabbed.addChangeListener(new ChangeListener()
			{
			@Override
			public void stateChanged(ChangeEvent e) {
				selIndex =JTabbedPane.class.cast(e.getSource()).getSelectedIndex();
				}	
			});
		}
	
	public void load(Preferences prefs)
		{
		this.tabbed.setSelectedIndex( prefs.getInt("pane", 0));
		tfFile.setText("");
		String s= prefs.get("selfile", null);
		if(s!=null)
			{
			this.choosenFile= new File(s);
			tfFile.setText(choosenFile.getName());
			tfFile.setCaretPosition(0);
			}
		this.tfJdbcURL.setText(prefs.get("jdbc-url", ""));
		this.tfJdbcLogin.setText(prefs.get("jdbc-login", ""));
		this.tfJdbcDriver.setText(prefs.get("jdbc-driver", ""));
		
		this.tfJdbcURL.setCaretPosition(0);
		this.tfJdbcLogin.setCaretPosition(0);
		this.tfJdbcDriver.setCaretPosition(0);
		}
	
	public void save(Preferences prefs)
		{
		prefs.putInt("pane", this.tabbed.getSelectedIndex());
		if(this.choosenFile!=null)
			{
			prefs.put("selfile"	,this.choosenFile.toString());
			}
		prefs.put("jdbc-url",this.tfJdbcURL.getText().trim());
		prefs.put("jdbc-login",this.tfJdbcLogin.getText().trim());
		prefs.put("jdbc-driver",this.tfJdbcDriver.getText().trim());
		}
	
	public ModelSource getModelChooser()
		{
		switch(this.tabbed.getSelectedIndex())
			{
			case 0:
				{
				if(this.choosenFile!=null)
					{
					return new FileSource(this.choosenFile);
					}
				break;
				}
			case 1:
				{
				return new JDBCSource(
						this.tfJdbcURL.getText().trim(),
						this.tfJdbcLogin.getText().trim(),
						new String(this.tfJdbcPassword.getPassword()).trim(),
						this.tfJdbcDriver.getText().trim()
						);

				}
			}
		return null;
		}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ModelChooser app= new ModelChooser(null);
			app.setVisible(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
